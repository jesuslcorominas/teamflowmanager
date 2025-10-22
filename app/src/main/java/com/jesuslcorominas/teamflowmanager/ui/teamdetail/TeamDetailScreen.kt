package com.jesuslcorominas.teamflowmanager.ui.teamdetail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.ui.components.Loading
import com.jesuslcorominas.teamflowmanager.ui.navigation.BackHandlerController
import com.jesuslcorominas.teamflowmanager.ui.team.TeamForm
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import com.jesuslcorominas.teamflowmanager.viewmodel.TeamUiState
import com.jesuslcorominas.teamflowmanager.viewmodel.TeamViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun TeamDetailScreen(
    onNavigateBackRequest: () -> Unit,
    currentBackHandler: BackHandlerController,
    viewModel: TeamViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val showExitDialog by viewModel.showExitDialog.collectAsState()

    val hasUnsavedChanges = remember { mutableStateOf(true) }

    val latestAction = rememberUpdatedState {
        viewModel.requestBack(onNavigateBackRequest, hasUnsavedChanges.value)
    }

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
        viewModel.requestBack(onNavigateBackRequest, hasUnsavedChanges.value)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        when (val state = uiState) {
            is TeamUiState.Loading -> Loading()
            is TeamUiState.TeamExists -> {
                TeamDetailContent(
                    team = state.team,
                    captain = state.captain
                )
            }

            is TeamUiState.NoTeam -> {
                // Should not happen if navigation is correct
                Text(
                    text = stringResource(R.string.no_players_message),
                    modifier = Modifier.padding(TFMSpacing.spacing04),
                )
            }

            is TeamUiState.EditTeam -> {
                TeamForm(team = state.team) {
                    viewModel.updateTeam(it)
                    onNavigateBackRequest()
                }
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

@Composable
private fun TeamDetailContent(team: Team, captain: Player? = null) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(TFMSpacing.spacing04),
        horizontalAlignment = Alignment.Start,
    ) {
        InfoRow(
            label = stringResource(R.string.team_name),
            value = team.name,
        )

        InfoRow(
            label = stringResource(R.string.coach_name),
            value = team.coachName,
        )

        InfoRow(
            label = stringResource(R.string.delegate_name),
            value = team.delegateName,
        )

        if (captain != null) {
            InfoRow(
                label = stringResource(R.string.team_captain),
                value = "${captain.firstName} ${captain.lastName} (#${captain.number})",
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = TFMSpacing.spacing02),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = TFMSpacing.spacing01),
        )
    }
}
