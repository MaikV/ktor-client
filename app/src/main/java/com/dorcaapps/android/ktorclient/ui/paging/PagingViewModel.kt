package com.dorcaapps.android.ktorclient.ui.paging

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import androidx.paging.cachedIn
import com.dorcaapps.android.ktorclient.model.Repository
import com.dorcaapps.android.ktorclient.utils.LiveEvent
import io.ktor.http.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class PagingViewModel @ViewModelInject constructor(
    repository: Repository
) : ViewModel() {
    val navigation = LiveEvent<NavDirections>()

    val adapter = PagingAdapter(repository) { navigate(it) }
    private val pagingFlow = repository.getPaging()

    private val filesToUpload = LiveEvent<List<Uri>>()

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
        filesToUpload.asFlow()
            .onEach { repository.uploadFiles(it) }
            .launchIn(viewModelScope)
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun uploadFiles(data: Intent?) {
        val fileUris = data?.data?.inList() ?: data?.clipData?.run {
            buildList {
                for (i in 0 until itemCount) {
                    add(getItemAt(i).uri)
                }
            }
        } ?: return
        filesToUpload.value = fileUris
    }

    private fun Uri.inList() = listOf(this)

    private fun navigate(mediaData: MediaData) {
        when (mediaData.contentType.contentType) {
            ContentType.Video.Any.contentType ->
                navigation.value =
                    PagingFragmentDirections.actionPagingFragmentToDetailVideoFragment(mediaData.id)

            ContentType.Image.Any.contentType ->
                navigation.value =
                    PagingFragmentDirections.actionPagingFragmentToDetailFragment()
        }
    }
}