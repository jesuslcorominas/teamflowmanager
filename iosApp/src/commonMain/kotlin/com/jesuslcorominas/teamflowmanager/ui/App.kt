package com.jesuslcorominas.teamflowmanager.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.jesuslcorominas.teamflowmanager.ui.login.LoginScreen
import com.jesuslcorominas.teamflowmanager.ui.matches.MatchListScreen
import com.jesuslcorominas.teamflowmanager.ui.splash.SplashScreen

private enum class Screen { SPLASH, LOGIN, MATCHES }

@Composable
fun App() {
    var screen by remember { mutableStateOf(Screen.SPLASH) }

    when (screen) {
        Screen.SPLASH -> SplashScreen(
            onNavigateToLogin = { screen = Screen.LOGIN },
            onNavigateToMatches = { screen = Screen.MATCHES },
        )

        Screen.LOGIN -> LoginScreen(
            onSignInWithGoogle = { /* KMP-17: Google Sign-In for iOS */ },
            onLoginSuccess = { screen = Screen.SPLASH },
        )

        Screen.MATCHES -> MatchListScreen()
    }
}
