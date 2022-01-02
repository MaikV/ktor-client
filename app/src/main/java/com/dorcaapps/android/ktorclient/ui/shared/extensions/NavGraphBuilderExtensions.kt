package com.dorcaapps.android.ktorclient.ui.shared.extensions

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.dorcaapps.android.ktorclient.ui.destinations.Destination

fun NavGraphBuilder.composableDestination(
    destination: Destination,
    content: @Composable (NavBackStackEntry) -> Unit
) {
    composable(
        route = destination.route,
        arguments = destination.arguments,
        content = content
    )
}