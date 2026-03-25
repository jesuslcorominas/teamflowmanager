package com.jesuslcorominas.teamflowmanager.ui.players.components.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.ui.components.dialog.AppAlertDialog
import com.jesuslcorominas.teamflowmanager.viewmodel.CaptainConfirmationState

@Composable
fun CaptainConfirmationDialog(
    state: CaptainConfirmationState.ConfirmReplace,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AppAlertDialog(
        title = stringResource(R.string.captain_confirm_title),
        message =
            stringResource(
                R.string.captain_confirm_message,
                state.currentCaptain.firstName,
                state.currentCaptain.lastName,
                state.newCaptain.firstName,
                state.newCaptain.lastName,
            ),
        confirmText = stringResource(R.string.yes),
        dismissText = stringResource(R.string.cancel),
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
        title = { Text(stringResource(R.string.captain_confirm_title)) },
        text = {
            Text(
                stringResource(
                    R.string.captain_replace_with_matches_message,
                    state.currentCaptain.firstName,
                    state.currentCaptain.lastName,
                    state.newCaptain.firstName,
                    state.newCaptain.lastName,
                    state.matchCount,
                ),
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(true) }) {
                Text(stringResource(R.string.update_captain_in_matches))
            }
        },
        dismissButton = {
            TextButton(onClick = { onConfirm(false) }) {
                Text(stringResource(R.string.keep_current_captain_in_matches))
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
        title = stringResource(R.string.captain_remove_title),
        message =
            stringResource(
                R.string.captain_remove_message,
                state.player.firstName,
                state.player.lastName,
            ),
        confirmText = stringResource(R.string.yes),
        dismissText = stringResource(R.string.cancel),
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
        title = { Text(stringResource(R.string.captain_remove_title)) },
        text = {
            Text(
                stringResource(
                    R.string.captain_remove_with_matches_message,
                    state.player.firstName,
                    state.player.lastName,
                    state.matchCount,
                ),
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(true) }) {
                Text(stringResource(R.string.keep_captain_for_matches))
            }
        },
        dismissButton = {
            TextButton(onClick = { onConfirm(false) }) {
                Text(stringResource(R.string.remove_captain_from_matches))
            }
        },
    )
}
