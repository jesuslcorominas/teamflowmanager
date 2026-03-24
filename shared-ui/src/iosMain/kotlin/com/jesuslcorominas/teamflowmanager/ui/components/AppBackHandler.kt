package com.jesuslcorominas.teamflowmanager.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState

/**
 * iOS actual for [AppBackHandler].
 *
 * On iOS the only back trigger is the top-bar back button in [MainScreen].
 * This actual registers a callback in [LocalBackPressController] so that
 * [MainScreen] routes the tap through it before falling back to the default
 * popBackStack action.
 *
 * [rememberUpdatedState] is used so the effect re-reads the latest [enabled]
 * and [onBack] values on every recomposition without restarting the effect.
 */
@Composable
actual fun AppBackHandler(enabled: Boolean, onBack: () -> Unit) {
    val controller = LocalBackPressController.current
    val currentEnabled by rememberUpdatedState(enabled)
    val currentOnBack by rememberUpdatedState(onBack)

    DisposableEffect(controller) {
        controller?.register(
            isEnabled = { currentEnabled },
            handler = { currentOnBack() },
        )
        onDispose {
            controller?.unregister()
        }
    }
}
