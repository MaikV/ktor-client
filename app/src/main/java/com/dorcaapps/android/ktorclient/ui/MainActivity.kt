package com.dorcaapps.android.ktorclient.ui

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.dorcaapps.android.ktorclient.ui.destinations.Destinations
import com.dorcaapps.android.ktorclient.ui.shared.extensions.composableDestination
import com.dorcaapps.android.ktorclient.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        setContent {
            AppTheme {
                Scaffold(containerColor = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = Destinations.Login.route
                    ) {
                        composableDestination(Destinations.Login(navController))
                        composableDestination(Destinations.PagedGrid(navController))
                        composableDestination(Destinations.PagedGrid.ImageDetail)
                        composableDestination(Destinations.PagedGrid.VideoDetail)
                    }
                }
            }
        }
    }
}