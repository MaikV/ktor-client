package com.dorcaapps.android.ktorclient.model

import android.net.Uri
import androidx.core.net.toUri
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.dorcaapps.android.ktorclient.ui.paging.MediaData
import com.dorcaapps.android.ktorclient.ui.paging.MediaPagingSource
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import java.io.File
import javax.inject.Inject

class Repository @Inject constructor(private val client: HttpClient) {

    fun getFileUri(id: Int): Flow<Resource<Uri>> = flow<Resource<Uri>> {
        val response = client.get<HttpResponse>(path = "media/$id").throwOnError()
        val responseFile = File.createTempFile("prefix$id", null)
        val writeChannel = responseFile.writeChannel()
        response.content.copyAndClose(writeChannel)
        emit(Resource.Success(responseFile.toUri()))
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

    suspend fun login() {
        client.get<Unit>(path = "login")
    }

//    private suspend fun getMediaPage(page: Int, pageSize: Int): List<MediaData> =
//        client.get(path = "media") {
//            parameter("page", page)
//            parameter("pageSize", pageSize)
//            parameter("order", OrderType.MOST_RECENT_FIRST)
//        }

    private suspend fun getMediaPage(page: Int, pageSize: Int) = flow<List<MediaData>> {
        emit(client.get(path = "media") {
            parameter("page", page)
            parameter("pageSize", pageSize)
            parameter("order", OrderType.MOST_RECENT_FIRST)
        })
    }.addRetryWithLogin()


    private fun HttpResponse.throwOnError() =
        if (status.value in 200..299) this
        else throw ClientRequestException(call.response)

    private fun <T> Flow<Resource<T>>.addResourceHandling() =
        onStart {
            emit(Resource.Loading)
        }.catch {
            emit(Resource.Error(it))
        }
            .flowOn(Dispatchers.IO)

    private fun <T> Flow<T>.addRetryWithLogin() =
        retry(1) {
            val shouldRetry =
                (it as? ClientRequestException)?.response?.status == HttpStatusCode.Unauthorized
            if (shouldRetry) login()
            shouldRetry
        }
}