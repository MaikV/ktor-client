package com.dorcaapps.android.ktorclient.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.dorcaapps.android.ktorclient.model.Repository
import com.dorcaapps.android.ktorclient.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: Repository
): ViewModel() {

    fun uploadFilesFlow(uris: List<Uri>): Flow<Resource<ByteArray>> =
        repository.uploadFilesFlow(uris)

    // TODO: ErrorHandling non-existent
    suspend fun delete(mediaId: Int) {
        repository.delete(mediaId).collect()
    }
}