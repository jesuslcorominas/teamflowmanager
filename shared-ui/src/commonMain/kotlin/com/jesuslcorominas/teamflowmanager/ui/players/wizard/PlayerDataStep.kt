package com.jesuslcorominas.teamflowmanager.ui.players.wizard

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun PlayerDataStep(
    initialFirstName: String,
    initialLastName: String,
    initialNumber: String,
    initialIsCaptain: Boolean,
    initialImageUri: String?,
    onDataChanged: (String, String, String, Boolean, String?) -> Unit,
    onNext: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
)
