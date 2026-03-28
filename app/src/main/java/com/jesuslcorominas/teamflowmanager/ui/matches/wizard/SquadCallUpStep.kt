package com.jesuslcorominas.teamflowmanager.ui.matches.wizard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.ui.components.dialog.AppAlertDialog
import com.jesuslcorominas.teamflowmanager.ui.players.components.PlayerList
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMAppTheme
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing

@Composable
fun SquadCallUpStep(
    players: List<Player>,
    selectedPlayerIds: Set<Long>,
    onSelectionChanged: (Set<Long>) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    modifier: Modifier = Modifier,
    minPlayers: Int = 5,
) {
    var currentSelection by remember(selectedPlayerIds) { mutableStateOf(selectedPlayerIds) }
    var showGoalkeeperWarning by remember { mutableStateOf(false) }
    var pendingNext by remember { mutableStateOf(false) }

    val hasGoalkeeper =
        players.any { player ->
            player.id in currentSelection && player.positions.any { it == Position.Goalkeeper }
        }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(TFMSpacing.spacing03),
    ) {
        Text(
            text = stringResource(R.string.squad_callup_subtitle, minPlayers),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Text(
            text = stringResource(R.string.squad_callup_count, currentSelection.size),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color =
                if (currentSelection.size >= minPlayers) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
        )

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable {
                        currentSelection =
                            if (currentSelection.size == players.size) {
                                emptySet()
                            } else {
                                players.map { it.id }.toSet()
                            }
                    }
                    .padding(vertical = TFMSpacing.spacing02),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = currentSelection.size == players.size,
                onCheckedChange = { isChecked ->
                    currentSelection =
                        if (isChecked) {
                            players.map { it.id }.toSet()
                        } else {
                            emptySet()
                        }
                },
            )
            Text(
                text = stringResource(R.string.squad_callup_select_all),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = TFMSpacing.spacing02),
            )
        }

        PlayerList(
            modifier = Modifier.weight(1F),
            paddingValues = PaddingValues(TFMSpacing.spacing02),
            players = players.sortedBy { it.number },
            showPositions = false,
            showGoalKeeperBadge = true,
            selectedPlayerIds = currentSelection,
            onMultiSelectionChange = { player, isSelected ->
                currentSelection =
                    if (isSelected) {
                        currentSelection + player.id
                    } else {
                        currentSelection - player.id
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
                Text(stringResource(R.string.previous))
            }

            Button(
                onClick = {
                    if (currentSelection.size < minPlayers) {
                        // Show error, cannot proceed
                        return@Button
                    }

                    onSelectionChanged(currentSelection)

                    if (!hasGoalkeeper) {
                        pendingNext = true
                        showGoalkeeperWarning = true
                    } else {
                        onNext()
                    }
                },
                enabled = currentSelection.size >= minPlayers,
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.next))
            }
        }
    }

    // Goalkeeper warning dialog
    if (showGoalkeeperWarning) {
        AppAlertDialog(
            title = stringResource(R.string.squad_callup_no_goalkeeper_warning),
            message = stringResource(R.string.squad_callup_no_goalkeeper_message),
            confirmText = stringResource(R.string.yes),
            dismissText = stringResource(R.string.no),
            onConfirm = {
                showGoalkeeperWarning = false
                if (pendingNext) {
                    onNext()
                }
            },
            onDismiss = {
                showGoalkeeperWarning = false
                pendingNext = false
            },
        )
    }
}

@Preview(
    name = "Pixel 7 Pro",
    device = "spec:width=1440px,height=3120px,dpi=512",
    showSystemUi = true,
    showBackground = true,
)
@Composable
private fun SquadCallUpStepPreview() {
    TFMAppTheme {
        SquadCallUpStep(
            players =
                (1..3).map {
                    Player(
                        id = it.toLong(),
                        firstName = "John",
                        lastName = "Doe",
                        number = 10,
                        positions = listOf(Position.Forward, Position.Midfielder),
                        teamId = 1,
                        isCaptain = false,
                    )
                },
            selectedPlayerIds = emptySet(),
            onSelectionChanged = {},
            onNext = {},
            onPrevious = {},
        )
    }
}
