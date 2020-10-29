package com.dorcaapps.android.ktorclient.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.net.toUri
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.dorcaapps.android.ktorclient.extensions.throwOnError
import com.dorcaapps.android.ktorclient.ui.paging.MediaData
import com.dorcaapps.android.ktorclient.ui.paging.MediaPagingSource
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.yield
import java.io.File
import javax.inject.Inject

class Repository @Inject constructor(
    private val client: HttpClient,
    @ApplicationContext private val context: Context
) {

    fun getMediaFileUri(id: Int): Flow<Resource<Uri>> = flow<Resource<Uri>> {
        val response = client.get<HttpResponse>(path = "media/$id").throwOnError()
        yield()
        val responseFile = File.createTempFile("prefix$id", null)
        val writeChannel = responseFile.writeChannel()
        response.content.copyAndClose(writeChannel)
        yield()
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
                initialLoadSize = 12
            ),
            initialKey = 1,
            pagingSourceFactory = {
                MediaPagingSource { key, loadSize ->
                    getMediaPage(key, loadSize)
                }
            }
        ).flow
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
            }.addRetryWithLogin().launchIn(MainScope())
        }
    }

    suspend fun login() {
        client.get<Unit>(path = "login")
    }

    private suspend fun getMediaPage(page: Int, pageSize: Int) = flow<List<MediaData>> {
        emit(client.get(path = "media") {
            parameter("page", page)
            parameter("pageSize", pageSize)
            parameter("order", OrderType.MOST_RECENT_FIRST)
        })
    }.addRetryWithLogin()

    private fun <T> Flow<Resource<T>>.addResourceHandling() =
        onStart {
            emit(Resource.Loading)
        }.catch {
            emit(Resource.Error(it))
        }.flowOn(Dispatchers.IO)

    private fun <T> Flow<T>.addRetryWithLogin() =
        retry(1) {
            val shouldRetry =
                (it as? ClientRequestException)?.response?.status == HttpStatusCode.Unauthorized
            if (shouldRetry) login()
            shouldRetry
        }
}