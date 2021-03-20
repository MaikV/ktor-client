package com.dorcaapps.android.ktorclient.ui.detail

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.dorcaapps.android.ktorclient.model.Repository
import com.dorcaapps.android.ktorclient.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class DetailVideoViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {
    private val uri = MutableStateFlow<Resource<Uri>?>(null)
    private val deletion = MutableStateFlow<Resource<Unit>?>(null)
    private var videoId: Int? = null

    val videoUri =
        uri
            .filterIsInstance<Resource.Success<Uri>>()
            .map { it.data }
            .asLiveData(context = viewModelScope.coroutineContext)

    val isDownloading =
        uri
            .map { it is Resource.Loading }
            .asLiveData(viewModelScope.coroutineContext)

    val isDeleting =
        deletion
            .map { it is Resource.Loading }
            .asLiveData(viewModelScope.coroutineContext)

    fun setVideoId(id: Int) {
        videoId = id
        repository.getMediaFileUri(id)
            .onEach { uri.value = it }
            .launchIn(viewModelScope)
    }

    fun delete() {
        val videoId = videoId ?: return
        repository.delete(videoId)
            .onEach { deletion.value = it }
            .launchIn(viewModelScope)
    }
}
