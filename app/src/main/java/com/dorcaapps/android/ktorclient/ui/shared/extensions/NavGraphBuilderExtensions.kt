package com.dorcaapps.android.ktorclient.ui.shared.extensions

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.dorcaapps.android.ktorclient.ui.destinations.Destination

fun NavGraphBuilder.composableDestination(destination: Destination) {
    composable(
        route = destination.route,
        arguments = destination.arguments,
        content = destination.content
    )
}