package com.kunnn.totap.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kunnn.totap.feature.home.HomeRoute
import com.kunnn.totap.feature.onboarding.OnboardingRoute

object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
}

@Composable
fun TotapNavHost() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Routes.ONBOARDING) {
        composable(Routes.ONBOARDING) {
            OnboardingRoute(
                onAllGranted = {
                    nav.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.HOME) {
            HomeRoute()
        }
    }
}
