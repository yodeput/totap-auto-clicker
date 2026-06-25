package com.kunnn.totap.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kunnn.totap.feature.home.HomeRoute

object Routes {
    const val HOME = "home"
}

@Composable
fun TotapNavHost() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeRoute()
        }
    }
}
