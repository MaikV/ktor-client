package com.dorcaapps.android.ktorclient.ui.main

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.dorcaapps.android.ktorclient.ui.destinations.Destinations
import com.dorcaapps.android.ktorclient.ui.destinations.ImageDetailDestination
import com.dorcaapps.android.ktorclient.ui.destinations.LoginDestination
import com.dorcaapps.android.ktorclient.ui.destinations.PagedGridDestination
import com.dorcaapps.android.ktorclient.ui.destinations.VideoDetailDestination
import com.dorcaapps.android.ktorclient.ui.shared.ExceptionDialog
import com.dorcaapps.android.ktorclient.ui.shared.extensions.composableDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryFlow.collectAsState(initial = null)

    val mainViewModel: MainViewModel = hiltViewModel()

    ExceptionDialog(exceptionManager = mainViewModel.exceptionManager)

    val scrollBehavior = remember {
        TopAppBarDefaults.enterAlwaysScrollBehavior()
    }
    val (refreshing, refreshTrigger) = remember {
        mutableStateOf(false)
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            MainAppBar(
                backStackEntry = currentBackStackEntry,
                scrollBehavior = scrollBehavior,
                navigateBack = { navController.navigateUp() },
                deleteMedia = { mainViewModel.delete(it) },
                uploadFlow = mainViewModel.fileUploadResourceFlow,
                uploadFiles = { mainViewModel.uploadFiles(it) },
                triggerRefresh = { refreshTrigger(!refreshing) }
            )
        }
    ) {
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
                    refreshTrigger = refreshing,
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