package com.jesuslcorominas.teamflowmanager.ui.players

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.analytics.ScreenName
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.ui.analytics.TrackScreenView
import com.jesuslcorominas.teamflowmanager.ui.components.EmptyContent
import com.jesuslcorominas.teamflowmanager.ui.components.Loading
import com.jesuslcorominas.teamflowmanager.ui.main.LocalContentBottomPadding
import com.jesuslcorominas.teamflowmanager.ui.players.components.PlayerList
import com.jesuslcorominas.teamflowmanager.ui.players.components.dialog.DeleteConfirmationDialog
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import com.jesuslcorominas.teamflowmanager.viewmodel.DeleteConfirmationState
import com.jesuslcorominas.teamflowmanager.viewmodel.PlayerUiState
import com.jesuslcorominas.teamflowmanager.viewmodel.PlayerViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayersScreen(
    viewModel: PlayerViewModel = koinViewModel(),
    onNavigateToCreatePlayer: () -> Unit = {},
    onNavigateToEditPlayer: (Long) -> Unit = {},
) {
    TrackScreenView(screenName = ScreenName.PLAYERS, screenClass = "PlayersScreen")

    val uiState by viewModel.uiState.collectAsState()
    val deleteConfirmationState by viewModel.deleteConfirmationState.collectAsState()

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
                        onEditClick = { player -> onNavigateToEditPlayer(player.id) },
                        onDeleteClick = { player -> viewModel.showDeleteConfirmation(player) },
                    )
            }
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
}

@Composable
private fun PlayersListSuccess(
    players: List<Player>,
    onEditClick: (Player) -> Unit,
    onDeleteClick: (Player) -> Unit,
) {
    PlayerList(
        modifier = Modifier.fillMaxSize(),
        paddingValues = PaddingValues(
            bottom = LocalContentBottomPadding.current,
            top = TFMSpacing.spacing04,
            start = TFMSpacing.spacing04,
            end = TFMSpacing.spacing04,
        ),
        players = players.sortedBy { it.number },
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
