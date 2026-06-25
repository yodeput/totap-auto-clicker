package com.kunnn.totap.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush

private val TotapColorScheme = lightColorScheme(
    primary = TotapInk,
    onPrimary = TotapOnInk,
    secondary = TotapYellow,
    onSecondary = TotapInk,
    background = TotapYellow,
    onBackground = TotapInk,
    surface = TotapSurface,
    onSurface = TotapInk,
)

/**
 * Brand background brush: the Totap gradient at the configured angle.
 * Angle 288deg → top-right (yellow) to bottom-left (chartreuse).
 */
fun totapGradientBrush(areaSize: Size): Brush {
    val rad = Math.toRadians(TotapGradientAngleDeg.toDouble())
    val dx = Math.cos(rad).toFloat()
    val dy = Math.sin(rad).toFloat()
    val cx = areaSize.width / 2f
    val cy = areaSize.height / 2f
    val half = (areaSize.width + areaSize.height) / 4f
    return Brush.linearGradient(
        colors = listOf(TotapYellow, TotapChartreuse),
        start = Offset(cx - dx * half, cy - dy * half),
        end = Offset(cx + dx * half, cy + dy * half),
    )
}

@Composable
fun TotapTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TotapColorScheme,
        typography = TotapTypography,
        content = content,
    )
}
