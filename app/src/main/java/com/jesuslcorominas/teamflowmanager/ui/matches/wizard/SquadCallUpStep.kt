package com.jesuslcorominas.teamflowmanager.ui.matches.wizard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.ui.components.AppAlertDialog
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing

@Composable
fun SquadCallUpStep(
    players: List<Player>,
    selectedPlayerIds: Set<Long>,
    onSelectionChanged: (Set<Long>) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var currentSelection by remember(selectedPlayerIds) { mutableStateOf(selectedPlayerIds) }
    var showGoalkeeperWarning by remember { mutableStateOf(false) }
    var pendingNext by remember { mutableStateOf(false) }
    
    val hasGoalkeeper = players.any { player ->
        player.id in currentSelection && player.positions.any { it is Position.Goalkeeper }
    }
    
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(TFMSpacing.spacing03)
    ) {
        Text(
            text = stringResource(R.string.squad_callup_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = stringResource(R.string.squad_callup_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = stringResource(R.string.squad_callup_count, currentSelection.size),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = if (currentSelection.size >= 5) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.error
            }
        )
        
        Spacer(modifier = Modifier.height(TFMSpacing.spacing02))
        
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(players) { player ->
                    PlayerCheckboxItem(
                        player = player,
                        isSelected = player.id in currentSelection,
                        onSelectionChange = { isSelected ->
                            currentSelection = if (isSelected) {
                                currentSelection + player.id
                            } else {
                                currentSelection - player.id
                            }
                        },
                    )
                }
            }
        }
        
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
                    if (currentSelection.size < 5) {
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
                enabled = currentSelection.size >= 5,
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
            }
        )
    }
}

@Composable
private fun PlayerCheckboxItem(
    player: Player,
    isSelected: Boolean,
    onSelectionChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelectionChange(!isSelected) }
            .padding(TFMSpacing.spacing02),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = onSelectionChange,
        )
        Column(
            modifier = Modifier.padding(start = TFMSpacing.spacing02),
        ) {
            Text(
                text = "${player.number} - ${player.firstName} ${player.lastName}",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
