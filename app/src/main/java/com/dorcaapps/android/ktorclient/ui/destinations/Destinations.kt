package com.dorcaapps.android.ktorclient.ui.destinations

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

interface Destination {
    val route: String
    val arguments: List<NamedNavArgument>
}

object Destinations {
    object Login : Destination {
        override val route: String
            get() = "Login"
        override val arguments: List<NamedNavArgument>
            get() = emptyList()
    }

    object PagedGrid : Destination {
        override val route: String
            get() = "PagedGrid"
        override val arguments: List<NamedNavArgument>
            get() = emptyList()

        object VideoDetail : Destination {
            override val route: String = "VideoDetail/{id}"
            override val arguments: List<NamedNavArgument> = listOf(
                navArgument("id") { type = NavType.IntType }
            )
        }

        object ImageDetail : Destination {
            override val route: String = "ImageDetail/{id}"
            override val arguments: List<NamedNavArgument> = listOf(
                navArgument("id") { type = NavType.IntType }
            )
        }
    }
}
