package com.jesuslcorominas.teamflowmanager.ui.players.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing

@Composable
fun PlayerList(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(TFMSpacing.spacing04),
    captainId: Long? = null,
    showCaptainBadge: Boolean = false,
    showGoalKeeperBadge: Boolean = false,
    players: List<Player>,
    showPositions: Boolean = true,
    selectedPlayerIds: Set<Long> = setOf(),
    onEditClick: ((Player) -> Unit)? = null,
    onDeleteClick: ((Player) -> Unit)? = null,
    onMultiSelectionChange: ((Player, Boolean) -> Unit)? = null,
    onSingleSelectionChange: ((Player) -> Unit)? = null,
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
                showCaptainBadge = if (showCaptainBadge) {
                    if (captainId != null) player.id == captainId else player.isCaptain
                } else {
                    false
                },
                showGoalkeeperBadge = showGoalKeeperBadge && player.positions.any { it == Position.Goalkeeper },
                showPositions = showPositions,
                onEditClick = onEditClick?.let { { onEditClick(player) } },
                onDeleteClick = onDeleteClick?.let { { onDeleteClick(player) } },
                onMultiSelectionChange = onMultiSelectionChange?.let { { isSelected -> onMultiSelectionChange(player, isSelected) } },
                onSingleSelectionChange = onSingleSelectionChange?.let { { onSingleSelectionChange(player) } },
            )
        }
    }
}
