package com.jesuslcorominas.teamflowmanager.ui.team

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.ui.analytics.TrackScreenView
import com.jesuslcorominas.teamflowmanager.ui.components.Loading
import com.jesuslcorominas.teamflowmanager.ui.navigation.BackHandlerController
import com.jesuslcorominas.teamflowmanager.ui.team.components.TeamDetailContent
import com.jesuslcorominas.teamflowmanager.ui.team.components.TeamForm
import com.jesuslcorominas.teamflowmanager.viewmodel.TeamUiState
import com.jesuslcorominas.teamflowmanager.viewmodel.TeamViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun TeamScreen(
    onNavigateToMatches: (String) -> Unit,
    onNavigateBackRequest: () -> Unit,
    currentBackHandler: BackHandlerController?,
    viewModel: TeamViewModel = koinViewModel(),
) {
    TrackScreenView(screenName = "Team", screenClass = "TeamScreen")
    
    val uiState by viewModel.uiState.collectAsState()
    val showExitDialog by viewModel.showExitDialog.collectAsState()

    val hasUnsavedChanges = remember { mutableStateOf(true) }

    val latestAction = rememberUpdatedState {
        viewModel.requestBack(onNavigateBackRequest)
    }

    currentBackHandler?.let {
        DisposableEffect(currentBackHandler, hasUnsavedChanges.value) {
            val newCallback: () -> Unit = { latestAction.value.invoke() }
            currentBackHandler.onBackRequested = newCallback

            onDispose {
                // Only clear the callback if this composable was the one that registered it.
                // This prevents accidentally removing a newer callback that might have replaced ours
                // due to recompositions or navigation changes happening in parallel.
                if (currentBackHandler.onBackRequested === newCallback) {
                    currentBackHandler.onBackRequested = null
                }
            }
        }

        BackHandler(enabled = !showExitDialog) {
            viewModel.requestBack(onNavigateBackRequest)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        when (val state = uiState) {
            is TeamUiState.Loading -> Loading()
            is TeamUiState.Success -> if (viewModel.isEditMode) {
                TeamForm(team = state.team, players = state.players) { team, captainId ->
                    viewModel.updateTeam(team, captainId)
                    onNavigateBackRequest()
                }
            } else {
                TeamDetailContent(
                    team = state.team,
                    captain = state.players.firstOrNull { it.isCaptain }
                )
            }

            is TeamUiState.NoTeam -> {
                TeamForm(
                    onSave = { team, _ ->
                        viewModel.createTeam(team)
                        onNavigateToMatches(team.name)
                    },
                )
            }
        }
    }

    if (showExitDialog) {
        AlertDialog(
            title = { Text(stringResource(R.string.unsaved_changes_title)) },
            onDismissRequest = { viewModel.dismissExitDialog() },
            confirmButton = {
                TextButton(onClick = { viewModel.discardChanges(onNavigateBackRequest) }) {
                    Text(stringResource(R.string.discard))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissExitDialog() }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            text = { Text(stringResource(R.string.discard_message)) }
        )
    }
}
