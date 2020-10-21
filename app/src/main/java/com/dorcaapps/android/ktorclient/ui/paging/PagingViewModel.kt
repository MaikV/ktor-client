package com.dorcaapps.android.ktorclient.ui.paging

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import androidx.paging.cachedIn
import com.dorcaapps.android.ktorclient.model.Repository
import io.ktor.http.*
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class PagingViewModel @ViewModelInject constructor(
    repository: Repository
) : ViewModel() {
    private val navigationChannel = ConflatedBroadcastChannel<NavDirections>()
    val navigation = navigationChannel.openSubscription()
    val adapter = PagingAdapter { navigate(it) }
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