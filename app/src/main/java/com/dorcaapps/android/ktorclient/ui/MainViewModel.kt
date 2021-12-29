package com.dorcaapps.android.ktorclient.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dorcaapps.android.ktorclient.model.Repository
import com.dorcaapps.android.ktorclient.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: Repository
): ViewModel() {

    fun uploadFilesFlow(uris: List<Uri>): Flow<Resource<ByteArray>> =
        repository.uploadFilesFlow(uris)

    // TODO: Make it make sense
    fun delete(mediaId: Int) {
        repository.delete(mediaId).onEach { }.launchIn(viewModelScope)
    }
}