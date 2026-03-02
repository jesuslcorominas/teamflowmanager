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
fun App(onSignInWithGoogle: suspend () -> String = { throw NotImplementedError("KMP-17") }) {
    var screen by remember { mutableStateOf(Screen.SPLASH) }

    when (screen) {
        Screen.SPLASH -> SplashScreen(
            onNavigateToLogin = { screen = Screen.LOGIN },
            onNavigateToMatches = { screen = Screen.MATCHES },
        )

        Screen.LOGIN -> LoginScreen(
            onSignInWithGoogle = onSignInWithGoogle,
            onLoginSuccess = { screen = Screen.MATCHES },
        )

        Screen.MATCHES -> MatchListScreen()
    }
}
