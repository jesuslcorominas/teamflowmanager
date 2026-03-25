package com.jesuslcorominas.teamflowmanager.ui.components

import androidx.compose.runtime.compositionLocalOf

/**
 * Controller that allows a child composable to intercept the back-press action
 * triggered by the top bar's back button.
 *
 * On iOS there is no system back gesture (the app uses a single UIViewController);
 * the only back trigger is the top-bar button in [MainScreen]. This controller
 * bridges the gap so that [AppBackHandler] (iOS actual) can intercept that tap,
 * giving screens like [TeamScreen] a chance to show an "unsaved changes" dialog
 * before navigating away.
 *
 * Usage:
 *  - [MainScreen] creates an instance, provides it via [LocalBackPressController],
 *    and routes its top-bar `onBack` through [handleBack].
 *  - [AppBackHandler] (iOS actual) calls [register] on composition and [unregister]
 *    on disposal via [DisposableEffect].
 */
class BackPressController {
    private var isEnabled: (() -> Boolean)? = null
    private var handler: (() -> Unit)? = null

    fun register(
        isEnabled: () -> Boolean,
        handler: () -> Unit,
    ) {
        this.isEnabled = isEnabled
        this.handler = handler
    }

    fun unregister() {
        isEnabled = null
        handler = null
    }

    /**
     * If a handler is registered and currently enabled, invokes it.
     * Otherwise falls through to [defaultAction].
     */
    fun handleBack(defaultAction: () -> Unit) {
        val enabled = isEnabled?.invoke() ?: false
        if (enabled && handler != null) {
            handler!!.invoke()
        } else {
            defaultAction()
        }
    }
}

val LocalBackPressController = compositionLocalOf<BackPressController?> { null }
