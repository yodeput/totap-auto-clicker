package com.kunnn.totap.feature.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.kunnn.totap.core.designsystem.components.TotapBrandMark
import com.kunnn.totap.core.designsystem.theme.TotapInk
import com.kunnn.totap.core.designsystem.theme.totapGradientBrush
import com.kunnn.totap.core.domain.permission.PermissionType

private val GrantedGreen = Color(0xFF2E7D32)

/**
 * The Totap permission-consent flow (spec §6.2 onboarding) — reactive edition.
 *
 * Live-updates the moment the user grants a permission and returns: the progress
 * ring fills, the card flips to a "Granted" chip, and when all three are granted
 * the CTA bounces in. No polling, no manual refresh button — it just reacts.
 */
@Composable
fun OnboardingScreen(
    onAllGranted: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    // Reactive: re-read permission state every time the screen comes to the
    // foreground (i.e. when the user returns from system settings).
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawWithCache {
                val brush = totapGradientBrush(size)
                onDrawBehind { drawRect(brush) }
            },
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item { OnboardingHeader(progress = state.progress) }

            items(state.permissions, key = { it.type.name }) { status ->
                PermissionCard(
                    status = status,
                    onOpenSettings = { viewModel.openSettingsFor(status.type) },
                )
            }
        }

        // Bottom CTA — appears (animated) only when everything is granted.
        CompletionCta(
            allGranted = state.progress.allGranted,
            onAllGranted = onAllGranted,
            onRecheck = { viewModel.refresh() },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

// --- Header with the live progress ring -------------------------------------

@Composable
private fun OnboardingHeader(progress: com.kunnn.totap.core.domain.permission.PermissionProgress) {
    Spacer(Modifier.statusBarsPadding())
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TotapBrandMark(size = 72.dp)
        ProgressRing(progress = progress, size = 64.dp)
    }
    Spacer(Modifier.height(12.dp))
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "Set up Totap",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = TotapInk,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = if (progress.allGranted)
                "You're all set. Tap to start automating."
            else
                "${progress.remaining} of ${progress.total} permissions left. Totap never reads your screen.",
            fontSize = 14.sp,
            color = TotapInk.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
    }
}

/** A ring that animates its sweep as permissions get granted. */
@Composable
private fun ProgressRing(
    progress: com.kunnn.totap.core.domain.permission.PermissionProgress,
    size: androidx.compose.ui.unit.Dp,
) {
    val target = progress.fraction
    val animatedFraction by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = 600),
        label = "ring",
    )
    val color = if (progress.allGranted) GrantedGreen else TotapInk
    Box(
        modifier = Modifier
            .size(size)
            .background(Color.White.copy(alpha = 0.6f), CircleShape)
            .drawWithCache {
                val sweep = animatedFraction * 360f
                onDrawBehind {
                    drawArc(
                        color = color.copy(alpha = 0.15f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round),
                    )
                    if (sweep > 0f) {
                        drawArc(
                            color = color,
                            startAngle = -90f,
                            sweepAngle = sweep,
                            useCenter = false,
                            style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round),
                        )
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "${progress.granted}/${progress.total}",
            fontWeight = FontWeight.Bold,
            color = TotapInk,
            fontSize = 14.sp,
        )
    }
}

// --- Bottom call-to-action ---------------------------------------------------

@Composable
private fun CompletionCta(
    allGranted: Boolean,
    onAllGranted: () -> Unit,
    onRecheck: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // When not all granted, show a subtle "recheck" affordance so the user has
    // a way to refresh without leaving (belt-and-suspenders on the auto-refresh).
    Box(modifier = modifier.fillMaxWidth().padding(24.dp)) {
        AnimatedVisibility(
            visible = allGranted,
            enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
            exit = fadeOut(),
        ) {
            Button(
                onClick = onAllGranted,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TotapInk,
                    contentColor = Color.White,
                ),
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) {
                Text("Start tapping", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
        AnimatedVisibility(visible = !allGranted, enter = fadeIn(), exit = fadeOut()) {
            Text(
                "Grant all three permissions to continue.\nReturn here — they'll be detected automatically.",
                color = TotapInk.copy(alpha = 0.7f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            )
        }
    }
}
