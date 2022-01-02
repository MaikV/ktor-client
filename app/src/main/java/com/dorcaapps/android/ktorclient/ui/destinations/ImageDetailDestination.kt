package com.dorcaapps.android.ktorclient.ui.destinations

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.dorcaapps.android.ktorclient.model.Resource
import com.dorcaapps.android.ktorclient.ui.detail.DetailImageViewModel
import kotlinx.coroutines.delay

@Composable
fun ImageDetailDestination(id: Int) {
    val viewModel: DetailImageViewModel = hiltViewModel()
    val mediaSource by viewModel.imageResource.collectAsState()
    LaunchedEffect(key1 = true) {
        delay(500)
        viewModel.setImageId(id)
    }
    mediaSource.let {
        when (it) {
            is Resource.Error -> MediaLoadingError { viewModel.setImageId(id) }
            is Resource.Loading -> MediaLoadingComposable(it.progressPercent)
            is Resource.Success -> Image(bitmap = it.data, contentDescription = "Image")
        }
    }
}