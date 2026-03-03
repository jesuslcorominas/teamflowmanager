package com.jesuslcorominas.teamflowmanager.ui.components

import androidx.compose.runtime.Composable

@Composable
expect fun AppBackHandler(enabled: Boolean = true, onBack: () -> Unit)
