package com.jesuslcorominas.teamflowmanager.ui.players.components.dialog

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.ui.components.AppAlertDialog

@Composable
fun DeleteConfirmationDialog(
    player: Player,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AppAlertDialog(
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        confirmText = stringResource(R.string.delete_player_confirm),
        dismissText = stringResource(R.string.delete_player_cancel),
        title = stringResource(R.string.delete_player_title),
        message =
            stringResource(
                R.string.delete_player_message,
                player.firstName,
                player.lastName,
            ),
        isDestructive = true,
    )
}

@Preview(showBackground = true)
@Composable
private fun DeleteConfirmationDialogPreview() {
    MaterialTheme {
        DeleteConfirmationDialog(
            player =
                Player(
                    id = 1,
                    firstName = "John",
                    lastName = "Doe",
                    number = 10,
                    positions = listOf(Position.Forward),
                ),
            onConfirm = {},
            onDismiss = {},
        )
    }
}
