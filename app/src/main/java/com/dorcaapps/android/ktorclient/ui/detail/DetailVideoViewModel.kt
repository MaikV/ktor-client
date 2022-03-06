package com.dorcaapps.android.ktorclient.ui.detail

import android.net.Uri
import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import com.dorcaapps.android.ktorclient.di.HttpClientModule
import com.dorcaapps.android.ktorclient.model.repository.Repository
import com.dorcaapps.android.ktorclient.model.repository.VideoStreamingDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DetailVideoViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    @OptIn(UnstableApi::class)
    fun getVideoMediaSource(id: Int): MediaSource {
        val protocol = HttpClientModule.DEFAULT_URL_PROTOCOL.name
        val host = HttpClientModule.DEFAULT_HOST
        val port = HttpClientModule.DEFAULT_REQUEST_PORT

        return DefaultMediaSourceFactory {
            VideoStreamingDataSource(
                getContentLengthOfResource = {
                    repository.getContentLengthOfResource(it)
                },
                getBytesOfPartialResource = { uri, nullRange ->
                    repository.getBytesOfPartialResource(uri, nullRange)
                })
        }.createMediaSource(
            MediaItem.fromUri(
                Uri.parse("$protocol://$host:$port/media/$id")
            )
        )
    }
}
