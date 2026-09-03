package com.jesuslcorominas.teamflowmanager.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.jesuslcorominas.teamflowmanager.IosNavController
import com.jesuslcorominas.teamflowmanager.ui.theme.LightColorScheme

@Composable
fun App(
    onSignInWithGoogle: suspend () -> String = { throw NotImplementedError("KMP-17") },
    onShareFile: (String) -> Unit = {},
) {
    MaterialTheme(colorScheme = LightColorScheme) {
        val navController = remember { IosNavController() }
        IosNavHost(
            navController = navController,
            onSignInWithGoogle = onSignInWithGoogle,
            onShareFile = onShareFile,
        )
    }
}
