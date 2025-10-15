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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing

@Composable
fun CaptainSelectionStep(
    players: List<Player>,
    selectedCaptainId: Long?,
    onCaptainChanged: (Long?) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var currentCaptainId by remember(selectedCaptainId) { mutableStateOf(selectedCaptainId) }
    
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(TFMSpacing.spacing03)
    ) {
        Text(
            text = stringResource(R.string.captain_selection_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = stringResource(R.string.captain_selection_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    PlayerRadioItem(
                        player = player,
                        isSelected = player.id == currentCaptainId,
                        onSelectionChange = {
                            currentCaptainId = player.id
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
                    onCaptainChanged(currentCaptainId)
                    onNext()
                },
                enabled = currentCaptainId != null,
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.next))
            }
        }
    }
}

@Composable
private fun PlayerRadioItem(
    player: Player,
    isSelected: Boolean,
    onSelectionChange: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelectionChange() }
            .padding(TFMSpacing.spacing02),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onSelectionChange,
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
