package com.dorcaapps.android.ktorclient.ui.paging

import android.content.Context
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import androidx.paging.cachedIn
import androidx.recyclerview.widget.GridLayoutManager
import com.dorcaapps.android.ktorclient.model.Repository
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.http.*
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class PagingViewModel @ViewModelInject constructor(
    repository: Repository,
    @ApplicationContext context: Context
) : ViewModel() {
    private val navigationChannel = ConflatedBroadcastChannel<NavDirections>()
    val navigation = navigationChannel.asFlow()
    val adapter = PagingAdapter { navigate(it) }
    val gridLayoutManager: GridLayoutManager = GridLayoutManager(context, 3)
    private val pagingFlow = repository.getPaging()

    init {
        pagingFlow
            .onEach(adapter::submitData)
            .cachedIn(viewModelScope)
            .launchIn(viewModelScope)
        adapter.loadStateFlow
            .onEach {
                Log.e("MTest", it.refresh.toString())
                Log.e("MTest", it.append.toString())
            }.launchIn(viewModelScope)
    }

    private fun navigate(mediaData: MediaData) {
        when (mediaData.contentType.contentType) {
            ContentType.Video.Any.contentType -> navigationChannel.offer(
                PagingFragmentDirections.actionPagingFragmentToDetailVideoFragment(
                    mediaData.id
                )
            )
            ContentType.Image.Any.contentType -> navigationChannel.offer(
                PagingFragmentDirections.actionPagingFragmentToDetailFragment()
            )
        }
    }
}