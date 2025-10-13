package com.jesuslcorominas.teamflowmanager.ui.players

import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.ui.players.components.PlayerList
import com.jesuslcorominas.teamflowmanager.ui.players.components.dialog.DeleteConfirmationDialog
import com.jesuslcorominas.teamflowmanager.ui.players.components.dialog.PlayerDialog
import com.jesuslcorominas.teamflowmanager.viewmodel.DeleteConfirmationState
import com.jesuslcorominas.teamflowmanager.viewmodel.PlayerUiState
import com.jesuslcorominas.teamflowmanager.viewmodel.PlayerViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayersScreen(
    viewModel: PlayerViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val deleteConfirmationState by viewModel.deleteConfirmationState.collectAsState()
    var showAddPlayerDialog by remember { mutableStateOf(false) }
    var playerToEdit by remember { mutableStateOf<Player?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            when (uiState) {
                is PlayerUiState.Loading -> LoadingState()
                is PlayerUiState.Empty -> EmptyState()
                is PlayerUiState.Success ->
                    PlayerList(
                        players = (uiState as PlayerUiState.Success).players,
                        onEditClick = { player -> playerToEdit = player },
                        onDeleteClick = { player -> viewModel.showDeleteConfirmation(player) },
                    )
            }
        }

        FloatingActionButton(
            onClick = { showAddPlayerDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(TFMSpacing.spacing04),
        ) {
            Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.add_player_title))
        }

        playerToEdit?.let { player ->
            PlayerDialog(
                player = player,
                onDismiss = { playerToEdit = null },
                onSave = { updatedPlayer ->
                    viewModel.updatePlayer(updatedPlayer)
                    playerToEdit = null
                }
            )
        }

        when (val state = deleteConfirmationState) {
            is DeleteConfirmationState.Confirming ->
                DeleteConfirmationDialog(
                    player = state.player,
                    onConfirm = { viewModel.deletePlayer(state.player.id) },
                    onDismiss = { viewModel.dismissDeleteConfirmation() },
                )

            DeleteConfirmationState.None -> {}
        }
    }

    if (showAddPlayerDialog) {
        PlayerDialog(
            onDismiss = { showAddPlayerDialog = false },
            onSave = { player ->
                viewModel.addPlayer(player)
                showAddPlayerDialog = false
            },
        )
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.no_players_message),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyStatePreview() {
    MaterialTheme {
        EmptyState()
    }
}
