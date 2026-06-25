package com.kunnn.totap.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kunnn.totap.core.designsystem.components.TotapBrandMark
import com.kunnn.totap.core.designsystem.theme.TotapInk
import com.kunnn.totap.core.designsystem.theme.totapGradientBrush

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    // Brand gradient as the screen background, drawn via drawWithCache so the
    // brush is computed once per size change (cheap recomposition).
    Box(
        modifier = modifier
            .fillMaxSize()
            .drawWithCache {
                val brush = totapGradientBrush(size)
                onDrawBehind { drawRect(brush) }
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp),
        ) {
            TotapBrandMark(size = 120.dp)
            Text(
                text = "Totap",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = TotapInk,
            )
            Text(
                text = "Auto Tap,\nrest your finger while you can",
                fontSize = 16.sp,
                color = TotapInk,
                textAlign = TextAlign.Center,
            )
        }
    }
}
