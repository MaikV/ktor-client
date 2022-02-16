package com.dorcaapps.android.ktorclient.ui

import android.net.Uri
import com.dorcaapps.android.ktorclient.model.Repository
import com.dorcaapps.android.ktorclient.model.Resource
import com.dorcaapps.android.ktorclient.ui.shared.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.last
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: Repository
) : BaseViewModel() {
    private val _fileUploadResourceFlow = MutableSharedFlow<Resource<ByteArray>?>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val fileUploadResourceFlow = _fileUploadResourceFlow.asSharedFlow()

    suspend fun uploadFiles(uris: List<Uri>) {
        repository.uploadFilesFlow(uris)
            .publishFailure()
            .collect {
                _fileUploadResourceFlow.tryEmit(it)
            }
        _fileUploadResourceFlow.tryEmit(null)
    }

    suspend fun delete(mediaId: Int): Boolean =
        repository.delete(mediaId).publishFailure().last() is Resource.Success
}