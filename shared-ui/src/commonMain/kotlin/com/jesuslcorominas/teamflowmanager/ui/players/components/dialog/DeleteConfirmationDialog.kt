package com.jesuslcorominas.teamflowmanager.ui.players.components.dialog

import androidx.compose.runtime.Composable
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.ui.components.dialog.AppAlertDialog
import org.jetbrains.compose.resources.stringResource
import teamflowmanager.shared_ui.generated.resources.Res
import teamflowmanager.shared_ui.generated.resources.cancel
import teamflowmanager.shared_ui.generated.resources.delete
import teamflowmanager.shared_ui.generated.resources.delete_player_message
import teamflowmanager.shared_ui.generated.resources.delete_player_title

@Composable
fun DeleteConfirmationDialog(
    player: Player,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AppAlertDialog(
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        confirmText = stringResource(Res.string.delete),
        dismissText = stringResource(Res.string.cancel),
        title = stringResource(Res.string.delete_player_title),
        message = stringResource(Res.string.delete_player_message, player.firstName, player.lastName),
        isDestructive = true,
    )
}
