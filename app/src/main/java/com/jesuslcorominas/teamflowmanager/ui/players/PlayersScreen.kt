package com.jesuslcorominas.teamflowmanager.ui.players

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.jesuslcorominas.teamflowmanager.ui.components.EmptyContent
import com.jesuslcorominas.teamflowmanager.ui.components.Loading
import com.jesuslcorominas.teamflowmanager.ui.players.components.PlayerList
import com.jesuslcorominas.teamflowmanager.ui.players.components.dialog.CaptainConfirmationDialog
import com.jesuslcorominas.teamflowmanager.ui.players.components.dialog.DeleteConfirmationDialog
import com.jesuslcorominas.teamflowmanager.ui.players.components.dialog.PlayerDialog
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import com.jesuslcorominas.teamflowmanager.viewmodel.CaptainConfirmationState
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
    val captainConfirmationState by viewModel.captainConfirmationState.collectAsState()
    var showAddPlayerDialog by remember { mutableStateOf(false) }
    var playerToEdit by remember { mutableStateOf<Player?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            when (uiState) {
                is PlayerUiState.Loading -> Loading()
                is PlayerUiState.Empty -> EmptyContent(stringResource(R.string.no_players_message))
                is PlayerUiState.Success ->
                    PlayersListSuccess(
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

        when (val state = captainConfirmationState) {
            is CaptainConfirmationState.ConfirmReplace -> {
                CaptainConfirmationDialog(
                    state = state,
                    onConfirm = { viewModel.confirmCaptainChange() },
                    onDismiss = { viewModel.cancelCaptainChange() },
                )
            }

            is CaptainConfirmationState.ConfirmRemove -> {
                CaptainConfirmationDialog(
                    state = state,
                    onConfirm = { viewModel.confirmCaptainChange() },
                    onDismiss = { viewModel.cancelCaptainChange() },
                )
            }

            is CaptainConfirmationState.ConfirmRemoveWithMatches -> {
                CaptainConfirmationDialog(
                    state = state,
                    onConfirm = { keepInMatches -> viewModel.confirmCaptainChange(keepInMatches) },
                    onDismiss = { viewModel.cancelCaptainChange() },
                )
            }

            CaptainConfirmationState.None -> {}
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
private fun PlayersListSuccess(
    players: List<Player>,
    onEditClick: (Player) -> Unit,
    onDeleteClick: (Player) -> Unit,
) {
    PlayerList(
        modifier = Modifier.fillMaxSize(),
        players = players,
        onEditClick = onEditClick,
        onDeleteClick = onDeleteClick,
    )
}

@Preview(showBackground = true)
@Composable
private fun PlayersListSuccessPreview() {
    MaterialTheme {
        PlayersListSuccess(
            players =
                listOf(
                    Player(
                        id = 1,
                        firstName = "John",
                        lastName = "Doe",
                        number = 10,
                        positions = emptyList(),
                        teamId = 1,
                        isCaptain = false
                    ),
                    Player(
                        id = 2,
                        firstName = "Jane",
                        lastName = "Smith",
                        number = 8,
                        positions = emptyList(),
                        teamId = 1,
                        isCaptain = false
                    ),
                ),
            onEditClick = {},
            onDeleteClick = {},
        )
    }
}
