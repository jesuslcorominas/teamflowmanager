package com.jesuslcorominas.teamflowmanager.ui.players.components.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.jesuslcorominas.teamflowmanager.ui.components.dialog.AppAlertDialog
import com.jesuslcorominas.teamflowmanager.viewmodel.CaptainConfirmationState
import org.jetbrains.compose.resources.stringResource
import teamflowmanager.shared_ui.generated.resources.Res
import teamflowmanager.shared_ui.generated.resources.cancel
import teamflowmanager.shared_ui.generated.resources.captain_confirm_message
import teamflowmanager.shared_ui.generated.resources.captain_confirm_title
import teamflowmanager.shared_ui.generated.resources.captain_remove_message
import teamflowmanager.shared_ui.generated.resources.captain_remove_title
import teamflowmanager.shared_ui.generated.resources.captain_remove_with_matches_message
import teamflowmanager.shared_ui.generated.resources.captain_replace_with_matches_message
import teamflowmanager.shared_ui.generated.resources.keep_captain_for_matches
import teamflowmanager.shared_ui.generated.resources.keep_current_captain_in_matches
import teamflowmanager.shared_ui.generated.resources.remove_captain_from_matches
import teamflowmanager.shared_ui.generated.resources.update_captain_in_matches
import teamflowmanager.shared_ui.generated.resources.yes

@Composable
fun CaptainConfirmationDialog(
    state: CaptainConfirmationState.ConfirmReplace,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AppAlertDialog(
        title = stringResource(Res.string.captain_confirm_title),
        message = stringResource(
            Res.string.captain_confirm_message,
            state.currentCaptain.firstName,
            state.currentCaptain.lastName,
            state.newCaptain.firstName,
            state.newCaptain.lastName,
        ),
        confirmText = stringResource(Res.string.yes),
        dismissText = stringResource(Res.string.cancel),
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
}

@Composable
fun CaptainConfirmationDialog(
    state: CaptainConfirmationState.ConfirmReplaceWithMatches,
    onConfirm: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.captain_confirm_title)) },
        text = {
            Text(
                stringResource(
                    Res.string.captain_replace_with_matches_message,
                    state.currentCaptain.firstName,
                    state.currentCaptain.lastName,
                    state.newCaptain.firstName,
                    state.newCaptain.lastName,
                    state.matchCount,
                )
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(true) }) {
                Text(stringResource(Res.string.update_captain_in_matches))
            }
        },
        dismissButton = {
            TextButton(onClick = { onConfirm(false) }) {
                Text(stringResource(Res.string.keep_current_captain_in_matches))
            }
        },
    )
}

@Composable
fun CaptainConfirmationDialog(
    state: CaptainConfirmationState.ConfirmRemove,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AppAlertDialog(
        title = stringResource(Res.string.captain_remove_title),
        message = stringResource(
            Res.string.captain_remove_message,
            state.player.firstName,
            state.player.lastName,
        ),
        confirmText = stringResource(Res.string.yes),
        dismissText = stringResource(Res.string.cancel),
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
}

@Composable
fun CaptainConfirmationDialog(
    state: CaptainConfirmationState.ConfirmRemoveWithMatches,
    onConfirm: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.captain_remove_title)) },
        text = {
            Text(
                stringResource(
                    Res.string.captain_remove_with_matches_message,
                    state.player.firstName,
                    state.player.lastName,
                    state.matchCount,
                )
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(true) }) {
                Text(stringResource(Res.string.keep_captain_for_matches))
            }
        },
        dismissButton = {
            TextButton(onClick = { onConfirm(false) }) {
                Text(stringResource(Res.string.remove_captain_from_matches))
            }
        },
    )
}
