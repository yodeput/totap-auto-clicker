package com.kunnn.totap.feature.onboarding

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kunnn.totap.core.designsystem.components.TotapBrandMark
import com.kunnn.totap.core.designsystem.theme.TotapInk
import com.kunnn.totap.core.designsystem.theme.totapGradientBrush
import com.kunnn.totap.core.domain.permission.PermissionType

/**
 * The Totap permission-consent flow (spec §6.2 onboarding).
 *
 * Branded, modern: full-screen yellow gradient, hero brand mark + tagline, then
 * three permission cards with live status chips. A bottom CTA unlocks once all
 * three are granted.
 */
@Composable
fun OnboardingScreen(
    onAllGranted: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

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
            contentPadding = PaddingValues(
                start = 24.dp, end = 24.dp,
                top = 16.dp, bottom = 96.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                Spacer(Modifier.statusBarsPadding())
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TotapBrandMark(size = 88.dp)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Set up Totap",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = TotapInk,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Three quick permissions unlock automatic tapping.\nYour data stays private — Totap never reads your screen.",
                        fontSize = 14.sp,
                        color = TotapInk.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(20.dp))
                }
            }

            items(state.permissions, key = { it.type.name }) { status ->
                PermissionCard(
                    status = status,
                    onOpenSettings = {
                        viewModel.openSettingsFor(
                            when (status.type) {
                                PermissionType.OVERLAY -> PermissionType.OVERLAY
                                PermissionType.ACCESSIBILITY -> PermissionType.ACCESSIBILITY
                                PermissionType.BATTERY -> PermissionType.BATTERY
                            }
                        )
                    },
                )
            }
        }

        // Bottom CTA
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(24.dp),
        ) {
            Button(
                onClick = {
                    viewModel.refresh()
                    if (state.allGranted) onAllGranted()
                },
                enabled = state.allGranted,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TotapInk,
                    contentColor = androidx.compose.ui.graphics.Color.White,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            ) {
                Text(
                    if (state.allGranted) "Start tapping" else "Grant all permissions to continue",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
            }
        }
    }
}
