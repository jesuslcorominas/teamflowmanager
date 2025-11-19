
package com.jesuslcorominas.teamflowmanager.ui.main

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toArgb
import com.jesuslcorominas.teamflowmanager.ui.theme.LightColorScheme
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMAppTheme

class MainActivity : ComponentActivity() {
    
    private var currentIntent by mutableStateOf<Intent?>(null)

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        val lightStatusBarColor = LightColorScheme.primary.toArgb()
        val darkStatusBarColor = LightColorScheme.primary.toArgb()

        enableEdgeToEdge(
            statusBarStyle =
                SystemBarStyle.auto(
                    lightScrim = lightStatusBarColor,
                    darkScrim = darkStatusBarColor,
                ),
            navigationBarStyle =
                SystemBarStyle.auto(
                    lightScrim = LightColorScheme.background.toArgb(),
                    darkScrim = LightColorScheme.background.toArgb(),
                ),
        )

        super.onCreate(savedInstanceState)

        requestedOrientation = SCREEN_ORIENTATION_USER_PORTRAIT
        
        currentIntent = intent

        setContent {
            TFMAppTheme {
                MainScreen(intent = currentIntent)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        currentIntent = intent
    }
}
