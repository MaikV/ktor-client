package com.dorcaapps.android.ktorclient.ui.destinations

import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.navArgument

interface Destination {
    val route: String
    val arguments: List<NamedNavArgument>

    val content: @Composable (NavBackStackEntry) -> Unit
}

object Destinations {
    data class Login(val navController: NavController): Destination {
        companion object {
            const val route: String = "Login"
        }
        override val route: String = Companion.route
        override val arguments: List<NamedNavArgument>
            get() = emptyList()
        override val content: @Composable (NavBackStackEntry) -> Unit
            get() = {
                LoginDestination {
                    navController.navigate(PagedGrid.route) {
                        popUpTo(route) { inclusive = true }
                    }
                }
            }
    }
    data class PagedGrid(val navController: NavController) : Destination {
        companion object {
            const val route: String = "PagedGrid"
        }

        override val route: String
            get() = Companion.route
        override val arguments: List<NamedNavArgument>
            get() = emptyList()
        override val content: @Composable (NavBackStackEntry) -> Unit
            get() = {
                PagedGridDestination(
                    onImageMediaClicked = {
                        navController.navigate(
                            route = ImageDetail.route.replace(
                                "{id}",
                                it.toString()
                            )
                        )
                    },
                    onVideoMediaClicked = {
                        navController.navigate(
                            route = VideoDetail.route.replace(
                                "{id}",
                                it.toString()
                            )
                        )
                    }
                )
            }


        object VideoDetail : Destination {
            override val route: String = "VideoDetail/{id}"
            override val arguments: List<NamedNavArgument> = listOf(
                navArgument("id") { type = NavType.IntType }
            )
            override val content: @Composable (NavBackStackEntry) -> Unit
                get() = {
                    VideoDetailDestination(it.arguments!!.getInt("id"))
                }
        }

        object ImageDetail : Destination {
            override val route: String = "ImageDetail/{id}"
            override val arguments: List<NamedNavArgument> = listOf(
                navArgument("id") { type = NavType.IntType }
            )
            override val content: @Composable (NavBackStackEntry) -> Unit
                get() = {
                    ImageDetailDestination(it.arguments!!.getInt("id"))
                }
        }
    }
}
