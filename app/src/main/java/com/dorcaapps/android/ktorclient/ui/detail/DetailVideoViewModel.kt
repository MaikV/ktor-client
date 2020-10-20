package com.dorcaapps.android.ktorclient.ui.detail

import android.net.Uri
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.dorcaapps.android.ktorclient.model.Repository
import com.dorcaapps.android.ktorclient.model.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class DetailVideoViewModel @ViewModelInject constructor(
    private val repository: Repository
) : ViewModel() {
    private val _videoUri = MutableStateFlow<Uri?>(null)
    val videoUri = _videoUri.filterNotNull().asLiveData(context = viewModelScope.coroutineContext)

    fun setVideoId(id: Int) {
        repository.getFileUri(id)
            .onEach { resource ->
                when (resource) {
                    is Resource.Success -> _videoUri.value = resource.data
                    is Resource.Loading -> {
                    }
                    is Resource.Error -> {
                    }
                }
            }.launchIn(viewModelScope)
    }
}