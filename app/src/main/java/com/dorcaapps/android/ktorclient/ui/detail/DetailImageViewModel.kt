package com.dorcaapps.android.ktorclient.ui.detail

import android.graphics.BitmapFactory
import androidx.annotation.OptIn
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.dorcaapps.android.ktorclient.model.Resource
import com.dorcaapps.android.ktorclient.model.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DetailImageViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {
    private val imageId =
        MutableSharedFlow<Int?>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    @OptIn(UnstableApi::class)
    val imageResource: StateFlow<Resource<ImageBitmap>> =
        imageId.filterNotNull().flatMapLatest { imageId ->
            repository.getMediaByteArray(imageId)
                .map { byteArrayResource ->
                    when (byteArrayResource) {
                        is Resource.Failure -> Resource.Failure(
                            byteArrayResource.exception
                        )
                        is Resource.Loading -> Resource.Loading(
                            byteArrayResource.progressPercent
                        )
                        is Resource.Success -> {
                            val byteArray = byteArrayResource.data
                            Resource.Success(
                                BitmapFactory.decodeByteArray(
                                    byteArray,
                                    0,
                                    byteArray.size
                                ).asImageBitmap()
                            )
                        }
                    }

                }
        }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.Lazily, Resource.Loading(0))

    suspend fun setImageId(id: Int) {
        imageId.emit(id)
    }
}