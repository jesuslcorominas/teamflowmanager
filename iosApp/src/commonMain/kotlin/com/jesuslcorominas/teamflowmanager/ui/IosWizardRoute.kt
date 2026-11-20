package com.jesuslcorominas.teamflowmanager.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner

/**
 * Hosts a full-screen wizard route (outside the MainScreen shell) on iOS.
 *
 * - Applies the system bar insets, which the shell would otherwise provide, so the
 *   wizard title is not drawn under the status bar.
 * - When [scopedViewModels] is true, provides a ViewModelStore that lives only while the
 *   route is shown. iOS has no NavBackStackEntry per screen, so koinViewModel() would
 *   otherwise reuse the instance cached in the root store across openings.
 */
@Composable
internal fun IosWizardRoute(
    scopedViewModels: Boolean = false,
    content: @Composable () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars),
    ) {
        if (scopedViewModels) {
            val owner =
                remember {
                    object : ViewModelStoreOwner {
                        override val viewModelStore = ViewModelStore()
                    }
                }
            DisposableEffect(owner) {
                onDispose { owner.viewModelStore.clear() }
            }
            CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
                content()
            }
        } else {
            content()
        }
    }
}
