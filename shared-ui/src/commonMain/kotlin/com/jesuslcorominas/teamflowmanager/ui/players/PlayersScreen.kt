package com.jesuslcorominas.teamflowmanager.ui.players

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.jesuslcorominas.teamflowmanager.domain.analytics.ScreenName
import com.jesuslcorominas.teamflowmanager.ui.analytics.TrackScreenView
import com.jesuslcorominas.teamflowmanager.ui.components.EmptyContent
import com.jesuslcorominas.teamflowmanager.ui.components.Loading
import com.jesuslcorominas.teamflowmanager.ui.players.components.PlayerList
import com.jesuslcorominas.teamflowmanager.ui.players.components.dialog.DeleteConfirmationDialog
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import com.jesuslcorominas.teamflowmanager.viewmodel.DeleteConfirmationState
import com.jesuslcorominas.teamflowmanager.viewmodel.PlayerUiState
import com.jesuslcorominas.teamflowmanager.viewmodel.PlayerViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import teamflowmanager.shared_ui.generated.resources.Res
import teamflowmanager.shared_ui.generated.resources.add_player_title
import teamflowmanager.shared_ui.generated.resources.no_players_message

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
        when (val state = uiState) {
            is PlayerUiState.Loading -> Loading()
            is PlayerUiState.Empty -> EmptyContent(stringResource(Res.string.no_players_message))
            is PlayerUiState.Success ->
                PlayerList(
                    modifier = Modifier.fillMaxSize(),
                    players = state.players.sortedBy { it.number },
                    onEditClick = { player -> onNavigateToEditPlayer(player.id) },
                    onDeleteClick = { player -> viewModel.showDeleteConfirmation(player) },
                )
        }

        FloatingActionButton(
            onClick = onNavigateToCreatePlayer,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(TFMSpacing.spacing04),
        ) {
            Icon(Icons.Outlined.Add, contentDescription = stringResource(Res.string.add_player_title))
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
