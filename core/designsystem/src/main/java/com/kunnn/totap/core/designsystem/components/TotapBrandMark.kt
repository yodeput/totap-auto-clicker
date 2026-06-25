package com.kunnn.totap.core.designsystem.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kunnn.totap.core.designsystem.theme.TotapInk

/**
 * The Totap hand-cursor + click-ripple brand mark, traced from the launcher icon SVG.
 */
val TotapBrandMarkVector: ImageVector
    get() = ImageVector.Builder(
        name = "TotapBrandMark",
        defaultWidth = 40.dp,
        defaultHeight = 40.dp,
        viewportWidth = 40f,
        viewportHeight = 40f,
    ).apply {
        path(fill = SolidColor(TotapInk)) {
            // main hand-cursor path (from tap icon.svg, path 1)
            moveTo(37.1f, 24.5f)
            lineTo(31f, 14.6f)
            curveTo(30.4f, 13.4f, 29.3f, 12.5f, 28f, 12.2f)
            curveTo(26.8f, 12f, 25.5f, 12.5f, 24f, 13.7f)
            lineTo(19.7f, 16.7f)
            lineTo(14.2f, 11.2f)
            curveTo(12.9f, 10f, 10.8f, 10f, 9.6f, 11.2f)
            curveTo(8.3f, 12.5f, 8.3f, 14.6f, 9.6f, 15.8f)
            lineTo(20f, 26.3f)
            lineTo(20f, 26.4f)
            curveTo(20f, 26.5f, 19.9f, 26.5f, 19.9f, 26.5f)
            lineTo(14.8f, 26.3f)
            lineTo(14.5f, 26.3f)
            curveTo(13.1f, 26.5f, 12f, 27.8f, 11.9f, 29.2f)
            curveTo(11.8f, 30.7f, 12.7f, 32f, 14.1f, 32.4f)
            lineTo(23.5f, 35.3f)
            curveTo(25.3f, 35.9f, 27.2f, 35.4f, 28.5f, 34.1f)
            lineTo(32.1f, 30.5f)
            curveTo(32.5f, 30.1f, 32.6f, 29.5f, 32.2f, 29.1f)
            curveTo(31.8f, 28.7f, 31.2f, 28.6f, 30.8f, 29f)
            lineTo(30.7f, 29.1f)
            lineTo(27.1f, 32.7f)
            curveTo(26.3f, 33.5f, 25.2f, 33.8f, 24.1f, 33.4f)
            lineTo(14.7f, 30.5f)
            curveTo(14.2f, 30.4f, 13.8f, 29.8f, 13.9f, 29.3f)
            curveTo(13.9f, 28.8f, 14.3f, 28.4f, 14.8f, 28.3f)
            lineTo(19.7f, 28.5f)
            curveTo(20.9f, 28.5f, 21.8f, 27.6f, 21.9f, 26.5f)
            curveTo(21.9f, 25.9f, 21.7f, 25.4f, 21.3f, 24.9f)
            lineTo(10.9f, 14.4f)
            curveTo(10.4f, 13.9f, 10.4f, 13.1f, 10.9f, 12.6f)
            curveTo(11.4f, 12.1f, 12.2f, 12.1f, 12.7f, 12.6f)
            lineTo(18.8f, 18.7f)
            curveTo(19.1f, 19f, 19.7f, 19.1f, 20.1f, 18.8f)
            lineTo(25.2f, 15.3f)
            curveTo(26.3f, 14.4f, 27.1f, 14f, 27.7f, 14.1f)
            curveTo(28.3f, 14.2f, 28.7f, 14.7f, 29.3f, 15.6f)
            lineTo(35.4f, 25.4f)
            curveTo(35.7f, 25.9f, 36.3f, 26f, 36.8f, 25.7f)
            curveTo(37.3f, 25.6f, 37.4f, 24.9f, 37.1f, 24.5f)
            close()
        }
        path(fill = SolidColor(TotapInk)) {
            // ripple arc path (from tap icon.svg, path 2)
            moveTo(8.6f, 19.4f)
            curveTo(5.2f, 17.7f, 3.8f, 13.6f, 5.5f, 10.2f)
            curveTo(7.2f, 6.8f, 11.3f, 5.4f, 14.7f, 7.1f)
            curveTo(16f, 7.8f, 17.1f, 8.8f, 17.8f, 10.2f)
            curveTo(18.1f, 10.7f, 18.7f, 10.9f, 19.2f, 10.6f)
            curveTo(19.7f, 10.3f, 19.8f, 9.8f, 19.6f, 9.3f)
            curveTo(17.3f, 4.9f, 12f, 3.2f, 7.6f, 5.5f)
            curveTo(3.2f, 7.8f, 1.5f, 13.1f, 3.8f, 17.5f)
            curveTo(4.7f, 19.2f, 6f, 20.5f, 7.7f, 21.3f)
            curveTo(7.8f, 21.4f, 8f, 21.4f, 8.1f, 21.4f)
            curveTo(8.7f, 21.4f, 9.1f, 21f, 9.1f, 20.4f)
            curveTo(9.2f, 20f, 9f, 19.6f, 8.6f, 19.4f)
            close()
        }
    }.build()

@Composable
fun TotapBrandMark(
    modifier: Modifier = Modifier,
    tint: Color = TotapInk,
    size: Dp = 96.dp,
) {
    Icon(
        imageVector = TotapBrandMarkVector,
        contentDescription = "Totap",
        tint = tint,
        modifier = modifier.size(size),
    )
}
