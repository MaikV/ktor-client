package com.dorcaapps.android.ktorclient.ui.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import com.dorcaapps.android.ktorclient.model.Resource
import com.dorcaapps.android.ktorclient.ui.destinations.Destinations
import io.ktor.http.ContentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Composable
fun MainAppBar(
    backStackEntry: NavBackStackEntry?,
    scrollBehavior: TopAppBarScrollBehavior,
    navigateBack: () -> Unit,
    deleteMedia: suspend (id: Int) -> Boolean,
    uploadFlow: Flow<Resource<ByteArray>?>,
    uploadFiles: suspend (List<Uri>) -> Unit,
    triggerRefresh: () -> Unit
) {
    LaunchedEffect(key1 = backStackEntry?.destination?.route) {
        if (backStackEntry?.destination?.route == Destinations.PagedGrid.route) {
            triggerRefresh()
        }
    }
    val coroutineScope = rememberCoroutineScope()
    val (uploading, changeUploading) = remember {
        mutableStateOf(false)
    }
    val fileLauncher: ActivityResultLauncher<String> = rememberLauncherForActivityResult(
        contract = getFileRequestContract(),
        onResult = { filesToUpload ->
            if (filesToUpload.isEmpty()) {
                return@rememberLauncherForActivityResult
            }
            coroutineScope.launch {
                changeUploading(true)
                uploadFiles(filesToUpload)
                changeUploading(false)
                triggerRefresh()
            }
        }
    )
    val currentRoute = backStackEntry?.destination?.route
    Column {
        SmallTopAppBar(
            title = { Text(text = "Title") },
            scrollBehavior = scrollBehavior,
            navigationIcon = {
                val canNavigateUp =
                    currentRoute != null &&
                            currentRoute != Destinations.Login.route &&
                            currentRoute != Destinations.PagedGrid.route
                AnimatedVisibility(visible = canNavigateUp) {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            },
            actions = {
                MainAppBarActions(
                    isUploading = uploading,
                    startFileUpload = { fileLauncher.launch("*/*") },
                    backStackEntry = backStackEntry,
                    navigateBack = navigateBack,
                    deleteMedia = deleteMedia,
                    triggerRefresh = triggerRefresh
                )
            })

        val uploadResource by uploadFlow.collectAsState(initial = null)
        val castUploadResource = uploadResource as? Resource.Loading
        AnimatedVisibility(visible = castUploadResource != null) {
            LinearProgressIndicator(
                progress = (castUploadResource?.progressPercent ?: 0) / 100f,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainAppBarActions(
    isUploading: Boolean,
    startFileUpload: () -> Unit,
    backStackEntry: NavBackStackEntry?,
    navigateBack: () -> Unit,
    deleteMedia: suspend (id: Int) -> Boolean,
    triggerRefresh: () -> Unit
) {
    val currentRoute = backStackEntry?.destination?.route

    AnimatedContent(targetState = currentRoute) { targetState ->
        when (targetState) {
            Destinations.PagedGrid.route -> {
                Row {
                    IconButton(onClick = {
                        startFileUpload()
                    }, enabled = !isUploading) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Upload"
                        )
                    }
                    IconButton(onClick = {
                        triggerRefresh()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                }
            }
            Destinations.PagedGrid.VideoDetail.route,
            Destinations.PagedGrid.ImageDetail.route -> {
                val coroutineScope = rememberCoroutineScope()
                IconButton(
                    onClick = {
                        val id = backStackEntry?.arguments?.getInt("id")
                            ?: return@IconButton
                        coroutineScope.launch {
                            val success = deleteMedia(id)
                            val route =
                                backStackEntry.destination.route
                            if (
                                success
                                && (route == Destinations.PagedGrid.VideoDetail.route
                                        || route == Destinations.PagedGrid.ImageDetail.route)
                            ) {
                                navigateBack()
                            }
                        }
                    }) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete"
                    )
                }
            }
            else -> {}
        }
    }
}

private fun getFileRequestContract(): ActivityResultContracts.GetMultipleContents {
    return object : ActivityResultContracts.GetMultipleContents() {
        override fun createIntent(context: Context, input: String): Intent =
            super.createIntent(context, input).apply {
                putExtra(
                    Intent.EXTRA_MIME_TYPES,
                    arrayOf(
                        ContentType.Video.Any.toString(),
                        ContentType.Image.Any.toString()
                    )
                )
            }
    }
}