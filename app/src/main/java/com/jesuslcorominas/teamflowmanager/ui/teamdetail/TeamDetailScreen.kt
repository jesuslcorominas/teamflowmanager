package com.jesuslcorominas.teamflowmanager.ui.teamdetail

import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
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
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.ui.components.EditTeamDialog
import com.jesuslcorominas.teamflowmanager.viewmodel.TeamUiState
import com.jesuslcorominas.teamflowmanager.viewmodel.TeamViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun TeamDetailScreen(
    viewModel: TeamViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var showEditTeam by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        when (val state = uiState) {
            is TeamUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.padding(TFMSpacing.spacing04))
            }
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
        }
    }

    if (showEditTeam) {
        val currentTeam = (uiState as? TeamUiState.TeamExists)?.team
        if (currentTeam != null) {
            EditTeamDialog(
                team = currentTeam,
                onDismiss = { showEditTeam = false },
                onSave = { team ->
                    viewModel.updateTeam(team)
                    showEditTeam = false
                },
            )
        }
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
