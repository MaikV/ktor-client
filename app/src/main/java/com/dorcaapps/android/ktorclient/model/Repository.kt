package com.dorcaapps.android.ktorclient.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.core.net.toUri
import androidx.core.util.lruCache
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.dorcaapps.android.ktorclient.extensions.throwOnError
import com.dorcaapps.android.ktorclient.ui.paging.MediaData
import com.dorcaapps.android.ktorclient.ui.paging.MediaPagingSource
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.HttpClient
import io.ktor.client.features.ClientRequestException
import io.ktor.client.request.delete
import io.ktor.client.request.forms.append
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.HttpStatement
import io.ktor.client.statement.readBytes
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentLength
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.ExperimentalTime

@Singleton
class Repository @Inject constructor(
    private val client: HttpClient,
    private val authManager: AuthManager,
    @ApplicationContext private val context: Context
) {
    private val mediaCache = lruCache<Int, File>(
        maxSize = 20_000_000,
        sizeOf = { _, file ->
            file.length().toInt()
        },
        onEntryRemoved = { _, _, oldValue, _ ->
            oldValue.delete()
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

    @OptIn(ExperimentalTime::class)
    fun getMediaFileUri(id: Int): Flow<Resource<Uri>> = flow {
        mediaCache[id]?.let {
            emit(Resource.Success(it.toUri()))
            return@flow
        }
        val responseFile = File.createTempFile("prefix$id", null)
        client.get<HttpStatement>(path = "media/$id").execute { httpResponse ->
            httpResponse.throwOnError()

            val channel = httpResponse.content
            val contentLength = httpResponse.contentLength()?.toInt()
            requireNotNull(contentLength) { "Header needs to be set by server" }
            var total = 0
            var readBytes: Int
            val buffer = ByteArray(contentLength)
            do {
                readBytes = channel.readAvailable(buffer, total, 4096)
                total += readBytes
                emit(Resource.Loading((total.toDouble() / contentLength.toDouble() * 100.0).toInt()))
                yield()
            } while (readBytes > 0)
            responseFile.writeBytes(buffer)
        }
        mediaCache.put(id, responseFile)
        emit(Resource.Success(responseFile.toUri()))
    }.addRetryWithLogin().addResourceHandling()

    fun getThumbnailFileUri(id: Int): Flow<Resource<Bitmap>> = flow<Resource<Bitmap>> {
        val response = client.get<HttpResponse>(path = "media/$id/thumbnail").throwOnError()
        yield()
        val bytes = response.readBytes()
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
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

    suspend fun uploadFiles(fileUris: List<Uri>) = withContext(Dispatchers.IO) {
        for (fileUri in fileUris) {
            val contentType = ContentType.parse(context.contentResolver.getType(fileUri)!!)
            val fileName = getFileName(fileUri)

            flow<Unit> {
                val myFormData = formData {
                    append(
                        "test",
                        fileName,
                        contentType
                    ) {
                        context.contentResolver.openInputStream(fileUri)?.use {
                            writeFully(it.readBytes())
                        }
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

    private fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            context.contentResolver.query(
                uri, null, null, null, null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        if (result == null) {
            result = uri.path
        }
        return result!!
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
            Log.e("MTest", "catch ${it.message}")
            emit(Resource.Error(it))
        }.flowOn(Dispatchers.IO)

    private fun <T> Flow<T>.addRetryWithLogin() =
        retry(1) {
            val shouldRetry =
                (it as? ClientRequestException)?.response?.status == HttpStatusCode.Unauthorized
            if (shouldRetry) startAndWaitForLogin()
            shouldRetry
        }
}
