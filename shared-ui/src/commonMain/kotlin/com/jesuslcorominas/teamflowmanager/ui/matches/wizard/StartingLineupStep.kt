package com.jesuslcorominas.teamflowmanager.ui.matches.wizard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.ui.components.dialog.AppAlertDialog
import com.jesuslcorominas.teamflowmanager.ui.players.components.PlayerList
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import org.jetbrains.compose.resources.stringResource
import teamflowmanager.shared_ui.generated.resources.Res
import teamflowmanager.shared_ui.generated.resources.close
import teamflowmanager.shared_ui.generated.resources.no
import teamflowmanager.shared_ui.generated.resources.previous
import teamflowmanager.shared_ui.generated.resources.save
import teamflowmanager.shared_ui.generated.resources.starting_lineup_count
import teamflowmanager.shared_ui.generated.resources.starting_lineup_max_error
import teamflowmanager.shared_ui.generated.resources.starting_lineup_no_goalkeeper_message
import teamflowmanager.shared_ui.generated.resources.starting_lineup_no_goalkeeper_warning
import teamflowmanager.shared_ui.generated.resources.starting_lineup_subtitle
import teamflowmanager.shared_ui.generated.resources.starting_lineup_title
import teamflowmanager.shared_ui.generated.resources.yes

@Composable
fun StartingLineupStep(
    players: List<Player>,
    selectedPlayerIds: Set<Long>,
    captainId: Long?,
    hasGoalkeepersInSquad: Boolean,
    onSelectionChanged: (Set<Long>) -> Unit,
    onCreate: () -> Unit,
    onPrevious: () -> Unit,
    modifier: Modifier = Modifier,
    requiredPlayers: Int = 5,
) {
    var currentSelection by remember(selectedPlayerIds) { mutableStateOf(selectedPlayerIds) }
    var showMaxError by remember { mutableStateOf(false) }
    var showGoalkeeperWarning by remember { mutableStateOf(false) }
    var pendingCreate by remember { mutableStateOf(false) }

    val hasGoalkeeperSelected =
        players.any { player ->
            player.id in currentSelection && player.positions.any { it == Position.Goalkeeper }
        }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(TFMSpacing.spacing03),
    ) {
        Text(
            text = stringResource(Res.string.starting_lineup_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        Text(
            text = stringResource(Res.string.starting_lineup_subtitle, requiredPlayers),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Text(
            text = stringResource(Res.string.starting_lineup_count, currentSelection.size, requiredPlayers),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color =
                if (currentSelection.size == requiredPlayers) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
        )

        Spacer(modifier = Modifier.height(TFMSpacing.spacing02))

        PlayerList(
            modifier = Modifier.weight(1F),
            players = players.sortedBy { it.number },
            selectedPlayerIds = currentSelection,
            showPositions = false,
            captainId = captainId,
            showCaptainBadge = true,
            showGoalKeeperBadge = true,
            paddingValues = PaddingValues(TFMSpacing.spacing02),
            onMultiSelectionChange = { player, isSelected ->
                if (isSelected) {
                    if (currentSelection.size >= requiredPlayers) {
                        showMaxError = true
                    } else {
                        currentSelection = currentSelection + player.id
                    }
                } else {
                    currentSelection = currentSelection - player.id
                }
            },
        )

        Spacer(modifier = Modifier.height(TFMSpacing.spacing02))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(TFMSpacing.spacing02),
        ) {
            Button(
                onClick = onPrevious,
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(Res.string.previous))
            }

            Button(
                onClick = {
                    if (currentSelection.size != requiredPlayers) return@Button

                    onSelectionChanged(currentSelection)

                    if (hasGoalkeepersInSquad && !hasGoalkeeperSelected) {
                        pendingCreate = true
                        showGoalkeeperWarning = true
                    } else {
                        onCreate()
                    }
                },
                enabled = currentSelection.size == requiredPlayers,
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(Res.string.save))
            }
        }
    }

    // Max selection error dialog
    if (showMaxError) {
        AppAlertDialog(
            title = stringResource(Res.string.starting_lineup_max_error, requiredPlayers),
            message = stringResource(Res.string.starting_lineup_max_error, requiredPlayers),
            confirmText = stringResource(Res.string.close),
            onConfirm = { showMaxError = false },
        )
    }

    // Goalkeeper warning dialog
    if (showGoalkeeperWarning) {
        AppAlertDialog(
            title = stringResource(Res.string.starting_lineup_no_goalkeeper_warning),
            message = stringResource(Res.string.starting_lineup_no_goalkeeper_message),
            confirmText = stringResource(Res.string.yes),
            dismissText = stringResource(Res.string.no),
            onConfirm = {
                showGoalkeeperWarning = false
                if (pendingCreate) onCreate()
            },
            onDismiss = {
                showGoalkeeperWarning = false
                pendingCreate = false
            },
        )
    }
}
