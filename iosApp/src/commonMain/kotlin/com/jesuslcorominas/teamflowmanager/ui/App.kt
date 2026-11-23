package com.jesuslcorominas.teamflowmanager.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
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
    onKeyboardShiftsScreenChange: (Boolean) -> Unit = {},
) {
    MaterialTheme(colorScheme = LightColorScheme) {
        val navController = remember { IosNavController() }
        // Bumped on sign-out to throw away every ViewModel of the session that just ended.
        var session by remember { mutableIntStateOf(0) }
        key(session) {
            SessionViewModelStore(onKeyboardShiftsScreenChange) {
                IosNavHost(
                    navController = navController,
                    onSignInWithGoogle = onSignInWithGoogle,
                    onShareFile = onShareFile,
                    onSignInWithApple = onSignInWithApple,
                    appleSignInButton = appleSignInButton,
                    onRequestNotificationPermission = onRequestNotificationPermission,
                    onSignedOut = { session++ },
                )
            }
        }
    }
}

/**
 * Gives the screens of one session their own ViewModelStore and clears it when the session ends.
 *
 * iOS has no NavBackStackEntry per screen, so koinViewModel() would otherwise keep every ViewModel
 * in the root store for the life of the process. They would outlive a sign-out with their Firestore
 * listeners and collectors still running: the role-switch collector navigated away from the login
 * screen, and screens came back with the listeners the sign-out had killed (#526). Android gets the
 * same reset from popUpTo(0) when it navigates to the login screen.
 */
@Composable
private fun SessionViewModelStore(
    onKeyboardShiftsScreenChange: (Boolean) -> Unit,
    content: @Composable () -> Unit,
) {
    val owner =
        remember {
            object : ViewModelStoreOwner {
                override val viewModelStore = ViewModelStore()
            }
        }
    DisposableEffect(owner) {
        onDispose { owner.viewModelStore.clear() }
    }
    CompositionLocalProvider(
        LocalViewModelStoreOwner provides owner,
        LocalKeyboardShiftsScreen provides onKeyboardShiftsScreenChange,
    ) {
        content()
    }
}
