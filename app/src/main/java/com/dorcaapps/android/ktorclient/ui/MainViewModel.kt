package com.dorcaapps.android.ktorclient.ui

import android.net.Uri
import com.dorcaapps.android.ktorclient.model.Repository
import com.dorcaapps.android.ktorclient.model.Resource
import com.dorcaapps.android.ktorclient.ui.shared.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.last
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: Repository
): BaseViewModel() {

    fun uploadFilesFlow(uris: List<Uri>): Flow<Resource<ByteArray>> =
        repository.uploadFilesFlow(uris)

    suspend fun delete(mediaId: Int): Boolean =
        repository.delete(mediaId).publishFailure().last() is Resource.Success
}