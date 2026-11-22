package com.jesuslcorominas.teamflowmanager.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.jesuslcorominas.teamflowmanager.IosNavController
import com.jesuslcorominas.teamflowmanager.domain.model.AppleSignInCredential
import com.jesuslcorominas.teamflowmanager.ui.theme.LightColorScheme

@Composable
fun App(
    onSignInWithGoogle: suspend () -> String = { throw NotImplementedError("KMP-17") },
    onShareFile: (String) -> Unit = {},
    onSignInWithApple: (suspend () -> AppleSignInCredential?)? = null,
    appleSignInButton: (@Composable (enabled: Boolean, onClick: () -> Unit) -> Unit)? = null,
    onRequestNotificationPermission: (onResult: (Boolean) -> Unit) -> Unit = { it(false) },
) {
    MaterialTheme(colorScheme = LightColorScheme) {
        val navController = remember { IosNavController() }
        IosNavHost(
            navController = navController,
            onSignInWithGoogle = onSignInWithGoogle,
            onShareFile = onShareFile,
            onSignInWithApple = onSignInWithApple,
            appleSignInButton = appleSignInButton,
            onRequestNotificationPermission = onRequestNotificationPermission,
        )
    }
}
