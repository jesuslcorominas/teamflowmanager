package com.jesuslcorominas.teamflowmanager.ui.team

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.jesuslcorominas.teamflowmanager.domain.analytics.ScreenName
import com.jesuslcorominas.teamflowmanager.ui.analytics.TrackScreenView
import com.jesuslcorominas.teamflowmanager.ui.components.AppBackHandler
import com.jesuslcorominas.teamflowmanager.ui.components.Loading
import com.jesuslcorominas.teamflowmanager.ui.team.components.TeamDetailContent
import com.jesuslcorominas.teamflowmanager.ui.team.components.TeamForm
import com.jesuslcorominas.teamflowmanager.viewmodel.TeamUiState
import com.jesuslcorominas.teamflowmanager.viewmodel.TeamViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import teamflowmanager.shared_ui.generated.resources.Res
import teamflowmanager.shared_ui.generated.resources.cancel
import teamflowmanager.shared_ui.generated.resources.close
import teamflowmanager.shared_ui.generated.resources.discard
import teamflowmanager.shared_ui.generated.resources.discard_message
import teamflowmanager.shared_ui.generated.resources.save_error_message
import teamflowmanager.shared_ui.generated.resources.save_error_title
import teamflowmanager.shared_ui.generated.resources.team_creation_permission_error_message
import teamflowmanager.shared_ui.generated.resources.team_creation_permission_error_title
import teamflowmanager.shared_ui.generated.resources.team_type_change_blocked_message
import teamflowmanager.shared_ui.generated.resources.team_type_change_not_allowed
import teamflowmanager.shared_ui.generated.resources.unsaved_changes_title

@Composable
fun TeamScreen(
    mode: String,
    onNavigateToMatches: (String) -> Unit,
    onNavigateBackRequest: () -> Unit,
    onNavigateToTeamList: (() -> Unit)? = null,
    viewModel: TeamViewModel = koinViewModel(parameters = { parametersOf(mode) }),
) {
    TrackScreenView(screenName = ScreenName.TEAM, screenClass = "TeamScreen")

    val uiState by viewModel.uiState.collectAsState()
    val showExitDialog by viewModel.showExitDialog.collectAsState()
    val showTeamTypeChangeError by viewModel.showTeamTypeChangeError.collectAsState()
    val showSaveError by viewModel.showSaveError.collectAsState()

    AppBackHandler(enabled = !showExitDialog) {
        viewModel.requestBack(onNavigateBackRequest)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        when (val state = uiState) {
            is TeamUiState.Loading -> Loading()
            is TeamUiState.Success -> if (viewModel.isEditMode) {
                TeamForm(
                    team = state.team,
                    players = state.players,
                    onShowTeamTypeChangeError = { viewModel.showTeamTypeChangeError() },
                ) { team, captainId ->
                    viewModel.updateTeam(team, captainId, onNavigateBackRequest)
                }
            } else {
                TeamDetailContent(
                    team = state.team,
                    captain = state.players.firstOrNull { it.isCaptain },
                )
            }

            is TeamUiState.NoTeam -> {
                if (state.clubId != null && !state.isPresident) {
                    AlertDialog(
                        title = { Text(stringResource(Res.string.team_creation_permission_error_title)) },
                        text = { Text(stringResource(Res.string.team_creation_permission_error_message)) },
                        onDismissRequest = { onNavigateBackRequest() },
                        confirmButton = {
                            TextButton(onClick = { onNavigateBackRequest() }) {
                                Text(stringResource(Res.string.close))
                            }
                        },
                    )
                } else {
                    TeamForm(
                        clubId = state.clubId,
                        clubFirestoreId = state.clubFirestoreId,
                        isPresident = state.isPresident,
                        onSave = { team, _ ->
                            viewModel.createTeam(team) {
                                if (state.isPresident && onNavigateToTeamList != null) {
                                    onNavigateToTeamList()
                                } else {
                                    onNavigateToMatches(team.name)
                                }
                            }
                        },
                    )
                }
            }
        }
    }

    if (showExitDialog) {
        AlertDialog(
            title = { Text(stringResource(Res.string.unsaved_changes_title)) },
            onDismissRequest = { viewModel.dismissExitDialog() },
            confirmButton = {
                TextButton(onClick = { viewModel.discardChanges(onNavigateBackRequest) }) {
                    Text(stringResource(Res.string.discard))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissExitDialog() }) {
                    Text(stringResource(Res.string.cancel))
                }
            },
            text = { Text(stringResource(Res.string.discard_message)) },
        )
    }

    if (showTeamTypeChangeError) {
        AlertDialog(
            title = { Text(stringResource(Res.string.team_type_change_not_allowed)) },
            text = { Text(stringResource(Res.string.team_type_change_blocked_message)) },
            onDismissRequest = { viewModel.dismissTeamTypeChangeError() },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissTeamTypeChangeError() }) {
                    Text(stringResource(Res.string.close))
                }
            },
        )
    }

    if (showSaveError) {
        AlertDialog(
            title = { Text(stringResource(Res.string.save_error_title)) },
            text = { Text(stringResource(Res.string.save_error_message)) },
            onDismissRequest = { viewModel.dismissSaveError() },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissSaveError() }) {
                    Text(stringResource(Res.string.close))
                }
            },
        )
    }
}
