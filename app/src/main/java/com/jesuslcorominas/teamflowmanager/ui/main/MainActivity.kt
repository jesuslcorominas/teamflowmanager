
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
import com.jesuslcorominas.teamflowmanager.service.MatchNotificationManager
import com.jesuslcorominas.teamflowmanager.ui.theme.LightColorScheme
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMAppTheme

class MainActivity : ComponentActivity() {

    private var pendingMatchNavigation by mutableStateOf<MatchNavigation?>(null)

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

        // Handle intent from notification
        handleNotificationIntent(intent)

        setContent {
            TFMAppTheme {
                MainScreen(
                    pendingMatchNavigation = pendingMatchNavigation,
                    onNavigationHandled = { pendingMatchNavigation = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        val action = intent?.action
        val matchId = intent?.getLongExtra(MatchNotificationManager.EXTRA_MATCH_ID, -1L) ?: -1L

        if (matchId != -1L && action != null) {
            pendingMatchNavigation = when (action) {
                MatchNotificationManager.ACTION_OPEN_MATCH -> 
                    MatchNavigation(matchId, openGoalDialog = null)
                MatchNotificationManager.ACTION_ADD_HOME_GOAL -> 
                    MatchNavigation(matchId, openGoalDialog = true)
                MatchNotificationManager.ACTION_ADD_VISITOR_GOAL -> 
                    MatchNavigation(matchId, openGoalDialog = false)
                else -> null
            }
        }
    }
}

data class MatchNavigation(
    val matchId: Long,
    val openGoalDialog: Boolean? = null // true = home, false = visitor, null = no dialog
)
