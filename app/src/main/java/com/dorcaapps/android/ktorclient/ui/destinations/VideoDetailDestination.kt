package com.dorcaapps.android.ktorclient.ui.destinations

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.ui.PlayerView
import com.dorcaapps.android.ktorclient.model.Resource
import com.dorcaapps.android.ktorclient.ui.detail.DetailVideoViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun VideoDetailDestination(id: Int) {
    val viewModel: DetailVideoViewModel = hiltViewModel()
    LaunchedEffect(key1 = true) {
        viewModel.setVideoId(id)
    }
    val mediaSource by viewModel.mediaSource.collectAsState()
//    AnimatedContent(targetState = videoByteArrayResource) { targetState ->

    mediaSource.let {
        when (it) {
            is Resource.Error -> VideoLoadingError { viewModel.setVideoId(id) }
            is Resource.Loading -> VideoLoadingComposable(it.progressPercent)
            is Resource.Success -> VideoComposable(it.data)
        }
    }
//    }
}

@Composable
fun VideoLoadingError(
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    onRetry: suspend CoroutineScope.() -> Unit
) {
    Button(onClick = {
        coroutineScope.launch(block = onRetry)
    }) {
        Text(text = "Retry")
    }
}

@Composable
fun VideoLoadingComposable(progressPercent: Int) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        LinearProgressIndicator(
            progress = (progressPercent / 100.0).toFloat(),
            color = Color.Yellow
        )
    }
}

@OptIn(UnstableApi::class)
@Composable
fun VideoComposable(mediaSource: MediaSource) {
    AndroidView(modifier = Modifier.fillMaxSize(), factory = { context ->
        PlayerView(context).also {
            it.player = ExoPlayer.Builder(context).build().apply {
                setMediaSource(mediaSource)
                repeatMode = Player.REPEAT_MODE_ALL
                prepare()
                play()
            }
        }
    })
}