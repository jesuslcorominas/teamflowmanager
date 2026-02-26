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
import com.jesuslcorominas.teamflowmanager.ui.navigation.PendingNavigation
import com.jesuslcorominas.teamflowmanager.ui.theme.LightColorScheme
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMAppTheme
import com.jesuslcorominas.teamflowmanager.viewmodel.MainViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModel()

    private var pendingNavigation by mutableStateOf<PendingNavigation?>(null)

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
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

        requestNotificationPermissionIfNeeded()

        handleIntent(intent)

        setContent {
            TFMAppTheme {
                MainScreen(
                    pendingNavigation = pendingNavigation
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return

        val action = intent.action

        if (action == Intent.ACTION_VIEW && intent.data != null) {
            pendingNavigation = PendingNavigation.DeepLink(intent)
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasRequestedBefore = mainViewModel.hasNotificationPermissionBeenRequested()

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED && !hasRequestedBefore
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
