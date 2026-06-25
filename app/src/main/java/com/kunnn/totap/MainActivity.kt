package com.kunnn.totap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.kunnn.totap.core.designsystem.theme.TotapTheme
import com.kunnn.totap.navigation.TotapNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TotapTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TotapNavHost()
                }
            }
        }
    }
}
