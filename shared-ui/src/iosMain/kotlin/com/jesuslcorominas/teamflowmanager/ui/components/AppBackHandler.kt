package com.jesuslcorominas.teamflowmanager.ui.components

import androidx.compose.runtime.Composable

@Composable
actual fun AppBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // no-op on iOS: back gesture handled natively by the navigation system
}
