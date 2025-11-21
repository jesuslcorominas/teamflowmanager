
package com.jesuslcorominas.teamflowmanager.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.jesuslcorominas.teamflowmanager.domain.notification.MatchNotificationController
import com.jesuslcorominas.teamflowmanager.service.MatchNotificationManager
import com.jesuslcorominas.teamflowmanager.ui.theme.LightColorScheme
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMAppTheme
import com.jesuslcorominas.teamflowmanager.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private var pendingMatchNavigation by mutableStateOf<MatchNavigation?>(null)
    private val matchNotificationController: MatchNotificationController by inject()
    private val mainViewModel: MainViewModel by viewModel()

    private var pendingIntent by mutableStateOf<Intent?>(null)

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Save that we've requested permission using ViewModel
        mainViewModel.setNotificationPermissionRequested(true)
    }

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

        // Request notification permission on Android 13+ if not already requested
        requestNotificationPermissionIfNeeded()

        // Handle intents
        handleIntent(intent)

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
        setIntent(intent) // Important: update the activity's intent
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val action = intent?.action
        val data = intent?.data

        when {
            // Handle match deep link from notification
            action == Intent.ACTION_VIEW && data != null && data.scheme == "teamflowmanager" && data.host == "match" -> {
                pendingIntent = intent
            }
            // Handle file deep links (.tfm files)
            action == Intent.ACTION_VIEW && data != null && (
                data.toString().endsWith(".tfm") ||
                intent.type == "application/octet-stream" ||
                intent.type == "application/x-tfm"
            ) -> {
                pendingIntent = intent
            }
            // Handle legacy notification intents (for backwards compatibility)
            else -> {
                handleNotificationIntent(intent)
            }
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

    private fun requestNotificationPermissionIfNeeded() {
        // Only request on Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasRequestedBefore = mainViewModel.hasNotificationPermissionBeenRequested()

            // Check if permission is not granted and we haven't requested before
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED && !hasRequestedBefore
            ) {
                // Request permission
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    companion object {
        // Removed constants as we're now using ViewModel with proper architecture
    }
}

data class MatchNavigation(
    val matchId: Long,
    val openGoalDialog: Boolean? = null
)
