package com.jesuslcorominas.teamflowmanager.ui.team

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.analytics.ScreenName
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.ui.analytics.TrackScreenView
import com.jesuslcorominas.teamflowmanager.ui.components.Loading
import com.jesuslcorominas.teamflowmanager.ui.components.card.AppCard
import com.jesuslcorominas.teamflowmanager.viewmodel.TeamListViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun TeamListScreen(
    viewModel: TeamListViewModel = koinViewModel(),
    onTeamClick: (Team) -> Unit = {}
) {
    TrackScreenView(screenName = ScreenName.TEAM, screenClass = "TeamListScreen")

    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is TeamListViewModel.UiState.Loading -> {
            Loading()
        }
        is TeamListViewModel.UiState.Success -> {
            if (state.teams.isEmpty()) {
                EmptyTeamsMessage(modifier = Modifier.fillMaxSize())
            } else {
                TeamsListContent(
                    teams = state.teams,
                    modifier = Modifier.fillMaxSize(),
                    onTeamClick = onTeamClick
                )
            }
        }
        is TeamListViewModel.UiState.Error -> {
            ErrorMessage(
                message = "An error occurred",
                modifier = Modifier.fillMaxSize()
            )
        }
        is TeamListViewModel.UiState.NoClubMembership -> {
            ErrorMessage(
                message = "No club membership found",
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun EmptyTeamsMessage(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.no_teams_message),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TeamsListContent(
    teams: List<Team>,
    modifier: Modifier = Modifier,
    onTeamClick: (Team) -> Unit
) {
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(teams, key = { it.id }) { team ->
            TeamCard(
                team = team,
                onClick = { onTeamClick(team) }
            )
        }
    }
}

@Composable
private fun TeamCard(
    team: Team,
    onClick: () -> Unit
) {
    AppCard(
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = team.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            if (team.coachName.isNotBlank()) {
                Row(
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.coach_name) + ": ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = team.coachName,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            if (team.delegateName.isNotBlank()) {
                Row {
                    Text(
                        text = stringResource(R.string.delegate_name) + ": ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = team.delegateName,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorMessage(
    message: String = "An error occurred",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
    }
}
