package com.kunnn.totap.feature.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryFull
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kunnn.totap.core.designsystem.theme.TotapInk
import com.kunnn.totap.core.designsystem.theme.TotapSurface
import com.kunnn.totap.core.domain.permission.PermissionState
import com.kunnn.totap.core.domain.permission.PermissionStatus
import com.kunnn.totap.core.domain.permission.PermissionType

/**
 * One permission row: icon, title, subtitle, and a status-driven action button.
 * Granted permissions collapse to a check chip; denied ones show the grant action.
 */
@Composable
fun PermissionCard(
    status: PermissionStatus,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val granted = status.isGranted
    val cardColor = if (granted) TotapSurface.copy(alpha = 0.92f) else TotapSurface
    val accent = if (granted) Color(0xFF2E7D32) else TotapInk

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (granted) 0.dp else 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Icon badge
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(accent.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = iconFor(status.type),
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(26.dp),
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titleFor(status.type),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TotapInk,
                )
                Text(
                    text = subtitleFor(status.type),
                    style = MaterialTheme.typography.bodySmall,
                    color = TotapInk.copy(alpha = 0.7f),
                )
            }
            Spacer(Modifier.width(12.dp))

            // Animated Grant -> Granted transition so the change reads instantly.
            androidx.compose.animation.AnimatedContent(
                targetState = granted,
                transitionSpec = {
                    (fadeIn(tween(220)) + scaleIn(tween(220))) togetherWith fadeOut(tween(180))
                },
                label = "permState",
            ) { isGranted ->
                if (isGranted) GrantedChip() else GrantButton(onOpenSettings)
            }
        }
    }
}

@Composable
private fun GrantButton(onOpenSettings: () -> Unit) {
    OutlinedButton(
        onClick = onOpenSettings,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = TotapInk),
    ) {
        Text("Grant", fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun GrantedChip() {
    Row(
        modifier = Modifier
            .background(GrantedGreen.copy(alpha = 0.14f), RoundedCornerShape(50))
            .border(1.dp, GrantedGreen.copy(alpha = 0.3f), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Rounded.Check,
            contentDescription = "Granted",
            tint = GrantedGreen,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(4.dp))
        Text(
            "Granted",
            color = GrantedGreen,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

private val GrantedGreen = Color(0xFF2E7D32)

private fun iconFor(type: PermissionType): ImageVector = when (type) {
    PermissionType.OVERLAY -> Icons.Rounded.Layers
    PermissionType.ACCESSIBILITY -> Icons.Rounded.TouchApp
    PermissionType.BATTERY -> Icons.Rounded.BatteryFull
}

private fun titleFor(type: PermissionType): String = when (type) {
    PermissionType.OVERLAY -> "Display over other apps"
    PermissionType.ACCESSIBILITY -> "Accessibility service"
    PermissionType.BATTERY -> "Disable battery optimization"
}

private fun subtitleFor(type: PermissionType): String = when (type) {
    PermissionType.OVERLAY -> "Shows the floating control panel and targets on top of your games."
    PermissionType.ACCESSIBILITY -> "Performs the actual taps, swipes, and holds at the spots you choose."
    PermissionType.BATTERY -> "Keeps Totap running reliably so it doesn't get killed mid-tap."
}
