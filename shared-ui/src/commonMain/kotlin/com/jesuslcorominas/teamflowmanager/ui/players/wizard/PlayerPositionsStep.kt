package com.jesuslcorominas.teamflowmanager.ui.players.wizard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import com.jesuslcorominas.teamflowmanager.ui.util.localizedName
import org.jetbrains.compose.resources.stringResource
import teamflowmanager.shared_ui.generated.resources.Res
import teamflowmanager.shared_ui.generated.resources.player_positions_step_subtitle
import teamflowmanager.shared_ui.generated.resources.player_positions_step_title
import teamflowmanager.shared_ui.generated.resources.previous
import teamflowmanager.shared_ui.generated.resources.save

@Composable
fun PlayerPositionsStep(
    initialPositions: List<Position>,
    onPositionsChanged: (List<Position>) -> Unit,
    onSave: () -> Unit,
    onPrevious: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedPositions by remember { mutableStateOf(initialPositions) }
    val allPositions = Position.getAllPositions()

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(TFMSpacing.spacing03),
    ) {
        Text(
            text = stringResource(Res.string.player_positions_step_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        Text(
            text = stringResource(Res.string.player_positions_step_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(TFMSpacing.spacing02))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(TFMSpacing.spacing01),
        ) {
            items(allPositions) { position ->
                PositionCheckbox(
                    position = position,
                    isSelected = selectedPositions.contains(position),
                    onCheckedChange = { isChecked ->
                        selectedPositions = if (isChecked) {
                            selectedPositions + position
                        } else {
                            selectedPositions - position
                        }
                        onPositionsChanged(selectedPositions)
                    },
                )
            }
        }

        Spacer(modifier = Modifier.height(TFMSpacing.spacing02))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            OutlinedButton(onClick = onPrevious) {
                Text(stringResource(Res.string.previous))
            }
            Button(onClick = {
                onPositionsChanged(selectedPositions)
                onSave()
            }) {
                Text(stringResource(Res.string.save))
            }
        }
    }
}

@Composable
private fun PositionCheckbox(
    position: Position,
    isSelected: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isSelected) }
            .padding(vertical = TFMSpacing.spacing01),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(TFMSpacing.spacing01),
    ) {
        Checkbox(
            modifier = Modifier
                .size(TFMSpacing.spacing06)
                .scale(.9F),
            checked = isSelected,
            onCheckedChange = onCheckedChange,
        )
        Text(
            text = position.localizedName(),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
