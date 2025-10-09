package com.jesuslcorominas.teamflowmanager.ui.players

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.ui.players.components.AddPlayerDialog
import com.jesuslcorominas.teamflowmanager.ui.players.components.PlayerList
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.ui.util.toLocalizedString
import com.jesuslcorominas.teamflowmanager.viewmodel.DeleteConfirmationState
import com.jesuslcorominas.teamflowmanager.viewmodel.PlayerUiState
import com.jesuslcorominas.teamflowmanager.viewmodel.PlayerViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun PlayersScreen(viewModel: PlayerViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val deleteConfirmationState by viewModel.deleteConfirmationState.collectAsState()
    var showAddPlayerDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
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
                onDeleteClick = { player -> viewModel.showDeleteConfirmation(player) }
            )}
        }

        FloatingActionButton(
            onClick = { showAddPlayerDialog = true },
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
        ) {
            Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_player))
        }

        when (val state = deleteConfirmationState) {
            is DeleteConfirmationState.Confirming -> DeleteConfirmationDialog(
                player = state.player,
                onConfirm = { viewModel.deletePlayer(state.player.id) },
                onDismiss = { viewModel.dismissDeleteConfirmation() }
            )
            DeleteConfirmationState.None -> {}
        }
    }

    if (showAddPlayerDialog) {
        AddPlayerDialog(
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
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun PlayerList(
    players: List<Player>,
    onDeleteClick: (Player) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = stringResource(R.string.players_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(players) { player ->
                PlayerItem(
                    player = player,
                    onDeleteClick = { onDeleteClick(player) }
                )
            }
        }
    }
}

@Composable
private fun PlayerItem(
    player: Player,
    onDeleteClick: () -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${player.firstName} ${player.lastName}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = player.positions.joinToString(", ") { it.toLocalizedString(context) },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete_player_button),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    player: Player,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.delete_player_title))
        },
        text = {
            Text(
                text = stringResource(
                    R.string.delete_player_message,
                    player.firstName,
                    player.lastName
                )
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(R.string.delete_player_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.delete_player_cancel))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun PlayerItemPreview() {
    MaterialTheme {
        PlayerItem(
            player = Player(
                id = 1,
                firstName = "John",
                lastName = "Doe",
                positions = listOf(Position.Forward, Position.Midfielder)
            ),
            onDeleteClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PlayerListPreview() {
    MaterialTheme {
        PlayerList(
            players = listOf(
                Player(1, "John", "Doe", listOf(Position.Forward)),
                Player(2, "Jane", "Smith", listOf(Position.Midfielder, Position.Defender)),
                Player(3, "Bob", "Johnson", listOf(Position.Goalkeeper))
            ),
            onDeleteClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EmptyStatePreview() {
    MaterialTheme {
        EmptyState()
    }
}

@Preview(showBackground = true)
@Composable
fun DeleteConfirmationDialogPreview() {
    MaterialTheme {
        DeleteConfirmationDialog(
            player = Player(
                id = 1,
                firstName = "John",
                lastName = "Doe",
                positions = listOf(Position.Forward)
            ),
            onConfirm = {},
            onDismiss = {}
        )
    }
}
