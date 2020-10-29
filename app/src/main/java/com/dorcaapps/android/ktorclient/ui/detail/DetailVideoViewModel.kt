package com.dorcaapps.android.ktorclient.ui.detail

import android.net.Uri
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.dorcaapps.android.ktorclient.model.Repository
import com.dorcaapps.android.ktorclient.model.Resource
import kotlinx.coroutines.flow.*

class DetailVideoViewModel @ViewModelInject constructor(
    private val repository: Repository
) : ViewModel() {
    private val resource = MutableStateFlow<Resource<Uri>?>(null)

    val videoUri = resource
        .filterIsInstance<Resource.Success<Uri>>()
        .map { it.data }
        .asLiveData(context = viewModelScope.coroutineContext)

    val isLoading =
        resource
            .map { it is Resource.Loading }
            .asLiveData(viewModelScope.coroutineContext)

    fun setVideoId(id: Int) {
        repository.getMediaFileUri(id)
            .onEach { resource.value = it }
            .launchIn(viewModelScope)
    }
}