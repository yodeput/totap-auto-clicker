package com.kunnn.totap.feature.onboarding

import androidx.compose.runtime.Composable

@Composable
fun OnboardingRoute(onAllGranted: () -> Unit) {
    OnboardingScreen(onAllGranted = onAllGranted)
}
