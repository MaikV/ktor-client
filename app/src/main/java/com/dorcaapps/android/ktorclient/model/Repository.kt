package com.dorcaapps.android.ktorclient.model

import android.net.Uri
import androidx.core.net.toUri
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.dorcaapps.android.ktorclient.ui.paging.MediaData
import com.dorcaapps.android.ktorclient.ui.paging.MediaPagingSource
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
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
        responseFile.deleteOnExit()
        val writeChannel = responseFile.writeChannel()
        response.content.copyAndClose(writeChannel)
        emit(Resource.Success(responseFile.toUri()))
    }.addResourceHandling()

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

    private suspend fun getMediaPage(page: Int, pageSize: Int): List<MediaData> {
        return client.get(path = "media") {
            parameter("page", page)
            parameter("pageSize", pageSize)
            parameter("order", OrderType.MOST_RECENT_FIRST)
        }
    }

    private fun HttpResponse.throwOnError() =
        if (status.value in 200..299) this
        else throw IllegalStateException(status.value.toString() + status.description)

    private fun <T> Flow<Resource<T>>.addResourceHandling() =
        onStart {
            emit(Resource.Loading)
        }.catch {
            emit(Resource.Error(it))
        }.flowOn(Dispatchers.IO)
}