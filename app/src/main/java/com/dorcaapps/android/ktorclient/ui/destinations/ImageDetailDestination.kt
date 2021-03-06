package com.dorcaapps.android.ktorclient.ui.destinations

import androidx.compose.foundation.Image
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.dorcaapps.android.ktorclient.model.Resource
import com.dorcaapps.android.ktorclient.model.repository.PreviewRepository
import com.dorcaapps.android.ktorclient.ui.detail.DetailImageViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun ImageDetailDestination(id: Int, viewModel: DetailImageViewModel = hiltViewModel()) {
    val mediaSource by viewModel.imageResource.collectAsState()
    LaunchedEffect(key1 = id) {
        viewModel.setImageId(id)
    }
    mediaSource.let {
        when (it) {
            is Resource.Failure -> MediaLoadingError(it.exception) { viewModel.setImageId(id) }
            is Resource.Loading -> MediaLoadingComposable(it.progressPercent)
            is Resource.Success -> Image(bitmap = it.data, contentDescription = "Image")
        }
    }
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

@Preview
@Composable
fun ImageDetailDestination_Preview() {
    val viewModel = DetailImageViewModel(PreviewRepository())
    ImageDetailDestination(1, viewModel = viewModel)
}