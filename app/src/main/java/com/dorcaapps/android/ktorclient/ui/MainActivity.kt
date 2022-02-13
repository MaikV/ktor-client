package com.dorcaapps.android.ktorclient.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.dorcaapps.android.ktorclient.model.Resource
import com.dorcaapps.android.ktorclient.ui.destinations.Destinations
import com.dorcaapps.android.ktorclient.ui.destinations.ImageDetailDestination
import com.dorcaapps.android.ktorclient.ui.destinations.LoginDestination
import com.dorcaapps.android.ktorclient.ui.destinations.PagedGridDestination
import com.dorcaapps.android.ktorclient.ui.destinations.VideoDetailDestination
import com.dorcaapps.android.ktorclient.ui.shared.extensions.composableDestination
import com.dorcaapps.android.ktorclient.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import io.ktor.http.ContentType
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        setContent {
            AppTheme {
                val scrollBehavior = remember {
                    TopAppBarDefaults.enterAlwaysScrollBehavior()
                }
                val navController = rememberNavController()
                val backStackEntry by navController.currentBackStackEntryFlow.collectAsState(initial = null)
                val currentRoute = backStackEntry?.destination?.route

                val fileRequestContract = object : ActivityResultContracts.GetMultipleContents() {
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
                val mainViewModel: MainViewModel = hiltViewModel()
                val currentException by mainViewModel.throwableManager.currentThrowableFlow.collectAsState(
                    initial = null
                )
                currentException?.let {
                    AlertDialog(
                        onDismissRequest = { mainViewModel.throwableManager.setThrowable(null) },
                        confirmButton = {
                            TextButton(onClick = { mainViewModel.throwableManager.setThrowable(null) }) {
                                Text(text = stringResource(id = android.R.string.ok))
                            }
                        },
                        title = { Text("There was an error") },
                        text = { Text(it.message ?: "Unknown error") })
                }
                var uploadResource: Resource<ByteArray>? by remember {
                    mutableStateOf(null)
                }
                var filesToUpload by remember {
                    mutableStateOf<List<Uri>>(emptyList())
                }
                var refreshTrigger by remember {
                    mutableStateOf(false)
                }
                LaunchedEffect(key1 = backStackEntry?.destination?.route) {
                    if (backStackEntry?.destination?.route == Destinations.PagedGrid.route) {
                        refreshTrigger = !refreshTrigger
                    }
                }
                LaunchedEffect(key1 = filesToUpload) {
                    if (filesToUpload.isEmpty()) {
                        uploadResource = null
                        return@LaunchedEffect
                    }
                    mainViewModel.uploadFilesFlow(filesToUpload).collect {
                        uploadResource = it
                    }
                    filesToUpload = emptyList()
                    refreshTrigger = !refreshTrigger
                }
                val fileLauncher = rememberLauncherForActivityResult(
                    contract = fileRequestContract,
                    onResult = {
                        filesToUpload = it
                    }
                )

                Scaffold(
                    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                    containerColor = MaterialTheme.colorScheme.background,
                    topBar = {
                        SmallTopAppBar(
                            title = { Text(text = "Title") },
                            scrollBehavior = scrollBehavior,
                            navigationIcon = {
                                val canNavigateUp =
                                    currentRoute != null &&
                                            currentRoute != Destinations.Login.route &&
                                            currentRoute != Destinations.PagedGrid.route
                                AnimatedVisibility(visible = canNavigateUp) {
                                    IconButton(onClick = { navController.navigateUp() }) {
                                        Icon(
                                            imageVector = Icons.Filled.ArrowBack,
                                            contentDescription = "Back"
                                        )
                                    }
                                }
                            },
                            actions = {
                                AnimatedContent(targetState = currentRoute) { targetState ->
                                    when (targetState) {
                                        Destinations.PagedGrid.route -> {
                                            Row {
                                                IconButton(onClick = {
                                                    fileLauncher.launch("*/*")
                                                }) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Share,
                                                        contentDescription = "Upload"
                                                    )
                                                }
                                                IconButton(onClick = {
                                                    refreshTrigger = !refreshTrigger
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
                                                        val success = mainViewModel.delete(id)
                                                        val route =
                                                            backStackEntry?.destination?.route
                                                        if (
                                                            success
                                                            && (route == Destinations.PagedGrid.VideoDetail.route
                                                                    || route == Destinations.PagedGrid.ImageDetail.route)
                                                        ) {
                                                            navController.popBackStack()
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
                            })
                    }
                ) {
                    Column {
                        val castUploadResource = uploadResource as? Resource.Loading
                        AnimatedVisibility(visible = castUploadResource != null) {
                            LinearProgressIndicator(
                                progress = (castUploadResource?.progressPercent ?: 0) / 100f,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        NavHost(
                            navController = navController,
                            startDestination = Destinations.Login.route
                        ) {
                            composableDestination(Destinations.Login) {
                                LoginDestination {
                                    navController.navigate(Destinations.PagedGrid.route) {
                                        popUpTo(Destinations.Login.route) { inclusive = true }
                                    }
                                }
                            }
                            composableDestination(Destinations.PagedGrid) {
                                PagedGridDestination(
                                    refreshTrigger = refreshTrigger,
                                    onImageMediaClicked = {
                                        navController.navigate(
                                            route = Destinations.PagedGrid.ImageDetail.route.replace(
                                                "{id}",
                                                it.toString()
                                            )
                                        )
                                    },
                                    onVideoMediaClicked = {
                                        navController.navigate(
                                            route = Destinations.PagedGrid.VideoDetail.route.replace(
                                                "{id}",
                                                it.toString()
                                            )
                                        )
                                    }
                                )
                            }
                            composableDestination(Destinations.PagedGrid.ImageDetail) {
                                ImageDetailDestination(it.arguments!!.getInt("id"))
                            }
                            composableDestination(Destinations.PagedGrid.VideoDetail) {
                                VideoDetailDestination(it.arguments!!.getInt("id"))
                            }
                        }
                    }
                }
            }
        }
    }
}