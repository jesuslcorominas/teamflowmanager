package com.jesuslcorominas.teamflowmanager.ui.components

import androidx.compose.runtime.Composable

@Composable
actual fun rememberShareTextAction(): (String) -> Unit =
    {
        // No-op on Android — the Android shell provides its own share callback.
    }
