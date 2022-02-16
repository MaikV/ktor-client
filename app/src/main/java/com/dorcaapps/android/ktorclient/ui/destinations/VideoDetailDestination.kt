package com.dorcaapps.android.ktorclient.ui.destinations

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.ui.PlayerView
import com.dorcaapps.android.ktorclient.model.Resource
import com.dorcaapps.android.ktorclient.ui.detail.DetailVideoViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun VideoDetailDestination(id: Int) {
    val viewModel: DetailVideoViewModel = hiltViewModel()
    val mediaSource by viewModel.mediaSource.collectAsState()
    LaunchedEffect(key1 = true) {
        delay(500)
        viewModel.setVideoId(id)
    }
//    AnimatedContent(targetState = videoByteArrayResource) { targetState ->
    mediaSource.let {
        when (it) {
            is Resource.Failure -> MediaLoadingError(it.exception) { viewModel.setVideoId(id) }
            is Resource.Loading -> MediaLoadingComposable(it.progressPercent)
            is Resource.Success -> VideoComposable(it.data)
        }
    }
//    }
}

@Composable
fun MediaLoadingError(
    throwable: Throwable,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    onRetry: suspend CoroutineScope.() -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        throwable.message?.let {
            Text(it)
        }
        Button(onClick = {
            coroutineScope.launch(block = onRetry)
        }) {
            Text(text = "Retry")
        }
    }
}

@Composable
fun MediaLoadingComposable(progressPercent: Int) {
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
    val lifecycleOwner = LocalLifecycleOwner.current
    val localContext = LocalContext.current
    val player = remember(localContext) {
        ExoPlayer.Builder(localContext).build()
    }
    LaunchedEffect(key1 = true) {
        lifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                when (event.targetState) {
                    Lifecycle.State.RESUMED -> player.play()
                    Lifecycle.State.STARTED -> player.pause()
                    Lifecycle.State.DESTROYED -> player.release()
                    else -> {}
                }
            }
        })
    }
    AndroidView(modifier = Modifier.fillMaxSize(), factory = { context ->
        PlayerView(context).also {
            it.player = player.apply {
                setMediaSource(mediaSource)
                repeatMode = Player.REPEAT_MODE_ALL
                prepare()
            }
        }
    })
}