package com.dorcaapps.android.ktorclient.ui.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.single

class MediaPagingSource(
    private val dataSource: suspend (key: Int, loadSize: Int) -> Flow<List<MediaData>>
) : PagingSource<Int, MediaData>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MediaData> {
        return try {
            val key = params.key!!
            val loadSize = params.loadSize
            val dataFlow = dataSource(key, loadSize)
            val data = dataFlow.single()
            LoadResult.Page(
                data = data,
                prevKey = if (key > 1) key - 1 else null,
                nextKey = if (data.size == loadSize) key + 1 else null
            )
        } catch (expected: Exception) {
            LoadResult.Error(expected)
        }
    }

    // Fraglich, ob das das richtige macht
    override fun getRefreshKey(state: PagingState<Int, MediaData>): Int? =
        state.anchorPosition?.let { state.pages[it].prevKey }
}
