
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
import androidx.lifecycle.lifecycleScope
import com.jesuslcorominas.teamflowmanager.domain.notification.MatchNotificationController
import com.jesuslcorominas.teamflowmanager.service.MatchNotificationManager
import com.jesuslcorominas.teamflowmanager.ui.theme.LightColorScheme
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMAppTheme
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private var pendingMatchNavigation by mutableStateOf<MatchNavigation?>(null)
    private val matchNotificationController: MatchNotificationController by inject()

    private var pendingIntent by mutableStateOf<Intent?>(null)

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

        if (intent?.action == Intent.ACTION_VIEW && intent?.data != null) {
            pendingIntent = intent
        } else {
            handleNotificationIntent(intent = intent)
        }

        setContent {
            TFMAppTheme {
                MainScreen(
                    pendingIntent = pendingIntent,
                    pendingMatchNavigation = pendingMatchNavigation,
                    onNavigationHandled = { pendingMatchNavigation = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Handle new intent when app is already running
        if (intent.action == Intent.ACTION_VIEW && intent.data != null) {
            pendingIntent = intent
        } else {
            handleNotificationIntent(intent)
        }
    }

    private fun handleNotificationIntent(intent: Intent?) {
        val action = intent?.action
        val matchId = intent?.getLongExtra(MatchNotificationManager.EXTRA_MATCH_ID, -1L) ?: -1L

        if (matchId != -1L && action != null) {
            when (action) {
                MatchNotificationManager.ACTION_FINISH_MATCH -> {
                    // Finish the match and then navigate
                    lifecycleScope.launch {
                        matchNotificationController.finishMatch(matchId, System.currentTimeMillis())
                        pendingMatchNavigation = MatchNavigation(matchId, openGoalDialog = null)
                    }
                }
                MatchNotificationManager.ACTION_OPEN_MATCH -> {
                    pendingMatchNavigation = MatchNavigation(matchId, openGoalDialog = null)
                }
                MatchNotificationManager.ACTION_ADD_HOME_GOAL -> {
                    pendingMatchNavigation = MatchNavigation(matchId, openGoalDialog = true)
                }
                MatchNotificationManager.ACTION_ADD_VISITOR_GOAL -> {
                    pendingMatchNavigation = MatchNavigation(matchId, openGoalDialog = false)
                }
            }
        }
    }
}

data class MatchNavigation(
    val matchId: Long,
    val openGoalDialog: Boolean? = null
)
