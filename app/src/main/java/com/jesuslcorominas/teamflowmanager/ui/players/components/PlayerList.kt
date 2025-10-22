package com.jesuslcorominas.teamflowmanager.ui.players.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerList(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(TFMSpacing.spacing04),
    players: List<Player>,
    showPositions: Boolean = true,
    selectedPlayerIds: Set<Long> = setOf(),
    onEditClick: ((Player) -> Unit)? = null,
    onDeleteClick: ((Player) -> Unit)? = null,
    onMultiSelectionChange: ((Player, Boolean) -> Unit)? = null,
    onSingleSelectionChange: ((Player) -> Unit)? = null
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = paddingValues,
        verticalArrangement = Arrangement.spacedBy(TFMSpacing.spacing02),
    ) {
        items(players) { player ->
            PlayerItem(
                player = player,
                isSelected = player.id in selectedPlayerIds,
                showPositions = showPositions,
                onEditClick = onEditClick?.let { { onEditClick(player) } },
                onDeleteClick = onDeleteClick?.let { { onDeleteClick(player) } },
                onMultiSelectionChange = onMultiSelectionChange?.let { { isSelected -> onMultiSelectionChange(player, isSelected) } },
                onSingleSelectionChange = onSingleSelectionChange?.let { { onSingleSelectionChange(player) } }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun PlayerListPreview() {
    MaterialTheme {
        PlayerList(
            players =
                listOf(
                    Player(1, "John", "Doe", 3, listOf(Position.Forward)),
                    Player(2, "Jane", "Smith", 2, listOf(Position.Midfielder, Position.Defender)),
                    Player(3, "Bob", "Johnson", 17, listOf(Position.Goalkeeper)),
                ),
            onSingleSelectionChange = { }
        )
    }
}




