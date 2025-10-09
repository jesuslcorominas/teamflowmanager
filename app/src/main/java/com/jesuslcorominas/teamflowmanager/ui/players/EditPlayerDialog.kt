package com.jesuslcorominas.teamflowmanager.ui.players

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.ui.util.toLocalizedString

@Composable
fun EditPlayerDialog(
    player: Player,
    onDismiss: () -> Unit,
    onSave: (Player) -> Unit
) {
    var firstName by remember { mutableStateOf(player.firstName) }
    var lastName by remember { mutableStateOf(player.lastName) }
    var selectedPositions by remember { mutableStateOf(player.positions) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.edit_player_title),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text(stringResource(R.string.first_name_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text(stringResource(R.string.last_name_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    Text(
                        text = stringResource(R.string.positions_label),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(getAllPositions()) { position ->
                    PositionCheckbox(
                        position = position,
                        isSelected = selectedPositions.contains(position),
                        onCheckedChange = { isChecked ->
                            selectedPositions = if (isChecked) {
                                selectedPositions + position
                            } else {
                                selectedPositions - position
                            }
                        }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updatedPlayer = player.copy(
                        firstName = firstName,
                        lastName = lastName,
                        positions = selectedPositions
                    )
                    onSave(updatedPlayer)
                },
                enabled = firstName.isNotBlank() && lastName.isNotBlank() && selectedPositions.isNotEmpty()
            ) {
                Text(stringResource(R.string.save_button))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_button))
            }
        }
    )
}

@Composable
private fun PositionCheckbox(
    position: Position,
    isSelected: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isSelected) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = onCheckedChange
        )
        Text(
            text = position.toLocalizedString(context),
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

private fun getAllPositions(): List<Position> = listOf(
    Position.Goalkeeper,
    Position.Defender,
    Position.RightBack,
    Position.LeftBack,
    Position.CenterBack,
    Position.Midfielder,
    Position.DefensiveMidfielder,
    Position.CentralMidfielder,
    Position.AttackingMidfielder,
    Position.Forward,
    Position.Winger,
    Position.Striker
)

@Preview
@Composable
fun EditPlayerDialogPreview() {
    MaterialTheme {
        EditPlayerDialog(
            player = Player(
                id = 1,
                firstName = "John",
                lastName = "Doe",
                positions = listOf(Position.Forward)
            ),
            onDismiss = {},
            onSave = {}
        )
    }
}
