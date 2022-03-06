package com.dorcaapps.android.ktorclient.ui.destinations

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.ui.PlayerView
import com.dorcaapps.android.ktorclient.model.repository.PreviewRepository
import com.dorcaapps.android.ktorclient.ui.detail.DetailVideoViewModel

@Composable
fun VideoDetailDestination(id: Int, viewModel: DetailVideoViewModel = hiltViewModel()) {
    val mediaSource = remember(id) {
        viewModel.getVideoMediaSource(id)
    }
    VideoComposable(mediaSource = mediaSource)
}

@OptIn(UnstableApi::class)
@Composable
fun VideoComposable(mediaSource: MediaSource) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val localContext = LocalContext.current
    val player = remember(localContext, mediaSource) {
        ExoPlayer.Builder(localContext).build().apply {
            setMediaSource(mediaSource)
            repeatMode = Player.REPEAT_MODE_ALL
            prepare()
        }
    }
    DisposableEffect(key1 = true) {
        val observer = LifecycleEventObserver { _, event ->
            when (event.targetState) {
                Lifecycle.State.RESUMED -> player.play()
                Lifecycle.State.STARTED -> player.pause()
                Lifecycle.State.DESTROYED -> player.release()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    AndroidView(modifier = Modifier.fillMaxSize(), factory = { context ->
        PlayerView(context)
    }, update = {
        it.player = player
    })
}

@Preview
@Composable
fun VideoDetailDestination_Preview() {
    val viewModel = DetailVideoViewModel(PreviewRepository())
    VideoDetailDestination(id = 1, viewModel = viewModel)
}