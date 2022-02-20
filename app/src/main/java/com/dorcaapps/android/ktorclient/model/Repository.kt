package com.dorcaapps.android.ktorclient.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.util.lruCache
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.dorcaapps.android.ktorclient.extensions.throwOnError
import com.dorcaapps.android.ktorclient.model.shared.MediaData
import com.dorcaapps.android.ktorclient.model.shared.MediaPagingSource
import com.dorcaapps.android.ktorclient.model.shared.OutputStreamContentWithLength
import com.dorcaapps.android.ktorclient.model.shared.forceCastException
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.HttpClient
import io.ktor.client.features.ClientRequestException
import io.ktor.client.features.onDownload
import io.ktor.client.features.onUpload
import io.ktor.client.request.delete
import io.ktor.client.request.forms.append
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readBytes
import io.ktor.http.ContentDisposition
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.core.use
import io.ktor.utils.io.core.writeFully
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.yield
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Repository @Inject constructor(
    private val client: HttpClient,
    private val authManager: AuthManager,
    @ApplicationContext private val context: Context
) {
    private val mediaCacheNew = lruCache<Int, ByteArray>(
        maxSize = 60_000_000,
        sizeOf = { _, byteArray ->
            byteArray.size
        }
    )
    private val thumbnailCache = lruCache<Int, Bitmap>(
        maxSize = 1_000_000,
        sizeOf = { _, file ->
            file.allocationByteCount
        }
    )

    private var isLoggingIn = false

    private val loginTrigger = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private val loginCompleted = loginTrigger.map {
        client.get<Unit>(path = "login")
    }.shareIn(CoroutineScope(client.coroutineContext), SharingStarted.Eagerly)

    fun getMediaByteArray(id: Int): Flow<Resource<ByteArray>> = channelFlow<Resource<ByteArray>> {
        mediaCacheNew[id]?.let {
            send(Resource.Success(it))
            return@channelFlow
        }
        val result = client.get<ByteArray>(path = "media/$id") {
            onDownload { bytesSentTotal, contentLength ->
                send(Resource.Loading((bytesSentTotal.toDouble() / contentLength.toDouble() * 100.0).toInt()))
            }
        }
        mediaCacheNew.put(id, result)
        send(Resource.Success(result))
    }.addRetryWithLogin().addResourceHandling()

    fun getThumbnailBitmap(id: Int): Flow<Resource<Bitmap>> = flow<Resource<Bitmap>> {
        thumbnailCache[id]?.let { thumbnail ->
            emit(Resource.Success(thumbnail))
            return@flow
        }
        val response = client.get<HttpResponse>(path = "media/$id/thumbnail").throwOnError()
        yield()
        val bytes = response.readBytes()
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        thumbnailCache.put(id, bitmap)
        yield()
        emit(Resource.Success(bitmap))
    }.addRetryWithLogin().addResourceHandling()

    fun getPaging(): Flow<PagingData<MediaData>> {
        val pageSize = 6
        return Pager(
            PagingConfig(
                pageSize = pageSize,
                enablePlaceholders = false,
                prefetchDistance = 9,
                initialLoadSize = pageSize
            ),
            initialKey = 1,
            pagingSourceFactory = {
                MediaPagingSource { key, loadSize ->
                    getMediaPage(key, loadSize)
                }
            }
        ).flow
    }

    fun uploadFilesFlow(fileUris: List<Uri>): Flow<Resource<ByteArray>> = flow {
        for (fileUri in fileUris) {
            val flowToEmit = channelFlow<Resource<ByteArray>> {
                val contentType = ContentType.parse(context.contentResolver.getType(fileUri)!!)
                val (fileName, fileSize) = getFileNameAndSize(fileUri)
                client.post<HttpResponse>(
                    path = "media",
                    body = OutputStreamContentWithLength(
                        body = {
                            context.contentResolver.openInputStream(fileUri)!!.use {
                                it.copyTo(this)
                            }
                        },
                        contentType = contentType,
                        contentLength = fileSize
                    )
                ) {
                    onUpload { bytesSentTotal, contentLength ->
                        send(Resource.Loading((bytesSentTotal.toDouble() / contentLength.toDouble() * 100.0).toInt()))
                    }
                    header(
                        HttpHeaders.ContentDisposition,
                        ContentDisposition.File.withParameter(
                            ContentDisposition.Parameters.FileName,
                            fileName
                        )
                    )
                }.throwOnError()
            }.distinctUntilChanged()
                .addRetryWithLogin()
                .addResourceHandling()
            emitAll(flowToEmit)
        }
    }

    suspend fun uploadFilesInCache() {
        val files = context.cacheDir.listFiles() ?: return
        val contentType = ContentType.Video.Any
        for (file in files) {
            flow<Unit> {
                val myFormData = formData {
                    append(
                        "test",
                        file.name,
                        contentType
                    ) {
                        writeFully(file.readBytes())
                    }
                }

                client.submitFormWithBinaryData<HttpResponse>(
                    formData = myFormData,
                    path = "media"
                ) {
                    this.method = HttpMethod.Post
                }.throwOnError()
            }.addRetryWithLogin().launchIn(MainScope()).join()
        }
    }

    fun delete(mediaId: Int): Flow<Resource<Unit>> = flow<Resource<Unit>> {
        emit(Resource.Success(client.delete(path = "media/$mediaId")))
    }.addResourceHandling().addRetryWithLogin()

    fun loginWithNewCredentials(username: String, password: String): Flow<Resource<Unit>> {
        authManager.username = username
        authManager.password = password
        authManager.reloadAuthProvider()
        return flow<Resource<Unit>> {
            emit(Resource.Success(client.get(path = "login")))
        }.addResourceHandling()
    }


    private fun getFileNameAndSize(uri: Uri): Pair<String, Long> {
        var name = ""
        var size: Long = 0
        if (uri.scheme == "content") {
            context.contentResolver.query(
                uri, null, null, null, null
            )?.use { cursor ->
                val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst() && displayNameIndex > -1 && sizeIndex > -1) {
                    name = cursor.getString(displayNameIndex)
                    size = cursor.getLong(sizeIndex)
                }
            }
        }
        if (name.isEmpty()) {
            name = uri.path!!
        }
        return Pair(name, size)
    }

    private suspend fun getMediaPage(page: Int, pageSize: Int) = flow<List<MediaData>> {
        emit(client.get(path = "media") {
            parameter("page", page)
            parameter("pageSize", pageSize)
            parameter("order", OrderType.MOST_RECENT_FIRST)
        })
    }.addRetryWithLogin()

    private suspend fun startAndWaitForLogin() {
        synchronized(this) {
            if (isLoggingIn) return@synchronized
            isLoggingIn = true
            loginTrigger.tryEmit(Unit)
        }
        loginCompleted.first()
    }

    private fun <T> Flow<Resource<T>>.addResourceHandling() =
        onStart {
            emit(Resource.Loading(0))
        }.catch {
            emit(Resource.Failure(it.forceCastException()))
        }.flowOn(Dispatchers.IO)

    private fun <T> Flow<T>.addRetryWithLogin() =
        retry(1) {
            val shouldRetry =
                (it as? ClientRequestException)?.response?.status == HttpStatusCode.Unauthorized
            if (shouldRetry) startAndWaitForLogin()
            shouldRetry
        }
}
