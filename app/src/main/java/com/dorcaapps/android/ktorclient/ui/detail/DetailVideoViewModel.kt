package com.dorcaapps.android.ktorclient.ui.detail

import android.net.Uri
import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.ByteArrayDataSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import com.dorcaapps.android.ktorclient.model.Repository
import com.dorcaapps.android.ktorclient.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
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
class DetailVideoViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {
    private val videoId = MutableSharedFlow<Int?>()

    @OptIn(UnstableApi::class)
    val mediaSource: StateFlow<Resource<MediaSource>> =
        videoId.filterNotNull().flatMapLatest { videoId ->
            repository.getMediaByteArray(videoId)
                .map { byteArrayResource ->
                    when (byteArrayResource) {
                        is Resource.Failure -> Resource.Failure(
                            byteArrayResource.throwable
                        )
                        is Resource.Loading -> Resource.Loading(
                            byteArrayResource.progressPercent
                        )
                        is Resource.Success -> {
                            Resource.Success(DefaultMediaSourceFactory {
                                ByteArrayDataSource(byteArrayResource.data)
                            }.createMediaSource(MediaItem.fromUri(Uri.EMPTY)))
                        }
                    }

                }
        }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.Lazily, Resource.Loading(0))

    suspend fun setVideoId(id: Int) {
        videoId.emit(id)
    }
}
