package com.jesuslcorominas.teamflowmanager.ui.team

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.domain.analytics.ScreenName
import com.jesuslcorominas.teamflowmanager.domain.model.ClubRole
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.ui.analytics.TrackScreenView
import com.jesuslcorominas.teamflowmanager.ui.components.Loading
import com.jesuslcorominas.teamflowmanager.ui.components.card.AppCard
import com.jesuslcorominas.teamflowmanager.ui.main.LocalContentBottomPadding
import com.jesuslcorominas.teamflowmanager.viewmodel.TeamListViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import teamflowmanager.shared_ui.generated.resources.Res
import teamflowmanager.shared_ui.generated.resources.coach_name
import teamflowmanager.shared_ui.generated.resources.delegate_name
import teamflowmanager.shared_ui.generated.resources.error_loading_teams
import teamflowmanager.shared_ui.generated.resources.no_club_membership_teams_error
import teamflowmanager.shared_ui.generated.resources.no_teams_message
import teamflowmanager.shared_ui.generated.resources.self_assign_as_coach_button
import teamflowmanager.shared_ui.generated.resources.share_team_button

@Composable
fun TeamListScreen(
    viewModel: TeamListViewModel = koinViewModel(),
    onTeamClick: (Team) -> Unit = {},
    onShareTeam: (teamName: String, invitationLink: String) -> Unit = { _, _ -> },
) {
    TrackScreenView(screenName = ScreenName.TEAM, screenClass = "TeamListScreen")

    val uiState by viewModel.uiState.collectAsState()
    val shareEvent by viewModel.shareEvent.collectAsState()
    val sharingTeamId by viewModel.sharingTeamId.collectAsState()
    val assigningCoachToTeamId by viewModel.assigningCoachToTeamId.collectAsState()
    val currentUserRole by viewModel.currentUserRole.collectAsState()

    LaunchedEffect(shareEvent) {
        shareEvent?.let { event ->
            onShareTeam(event.teamName, event.invitationLink)
            viewModel.onShareEventConsumed()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is TeamListViewModel.UiState.Loading -> Loading()
            is TeamListViewModel.UiState.Success -> {
                if (state.teams.isEmpty()) {
                    EmptyTeamsMessage(modifier = Modifier.fillMaxSize())
                } else {
                    TeamsListContent(
                        teams = state.teams,
                        modifier = Modifier.fillMaxSize(),
                        onTeamClick = onTeamClick,
                        onShareTeam = { team -> viewModel.shareTeam(team) },
                        onSelfAssignAsCoach = { team -> viewModel.selfAssignAsCoachToTeam(team) },
                        sharingTeamId = sharingTeamId,
                        assigningCoachToTeamId = assigningCoachToTeamId,
                        isPresident = currentUserRole == ClubRole.PRESIDENT.roleName,
                    )
                }
            }
            is TeamListViewModel.UiState.Error -> {
                ErrorMessage(
                    message = stringResource(Res.string.error_loading_teams),
                    modifier = Modifier.fillMaxSize(),
                )
            }
            is TeamListViewModel.UiState.NoClubMembership -> {
                ErrorMessage(
                    message = stringResource(Res.string.no_club_membership_teams_error),
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        if (sharingTeamId != null || assigningCoachToTeamId != null) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                        .clickable(enabled = false) {},
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun EmptyTeamsMessage(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(Res.string.no_teams_message),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TeamsListContent(
    teams: List<Team>,
    modifier: Modifier = Modifier,
    onTeamClick: (Team) -> Unit,
    onShareTeam: (Team) -> Unit,
    onSelfAssignAsCoach: (Team) -> Unit,
    sharingTeamId: String?,
    assigningCoachToTeamId: String?,
    isPresident: Boolean,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding =
            PaddingValues(
                bottom = LocalContentBottomPadding.current,
                top = 16.dp,
                start = 16.dp,
                end = 16.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(teams, key = { it.id }) { team ->
            TeamCard(
                team = team,
                onClick = { onTeamClick(team) },
                onShare = { onShareTeam(team) },
                onSelfAssignAsCoach = { onSelfAssignAsCoach(team) },
                isSharing = team.firestoreId == sharingTeamId,
                isAssigning = team.firestoreId == assigningCoachToTeamId,
                isPresident = isPresident,
            )
        }
    }
}

@Composable
private fun TeamCard(
    team: Team,
    onClick: () -> Unit,
    onShare: () -> Unit,
    onSelfAssignAsCoach: () -> Unit,
    isSharing: Boolean = false,
    isAssigning: Boolean = false,
    isPresident: Boolean = false,
) {
    AppCard(
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = team.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )

                if (team.coachId == null) {
                    IconButton(
                        onClick = onShare,
                        enabled = !isSharing,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = stringResource(Res.string.share_team_button),
                            tint =
                                if (isSharing) {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                        )
                    }
                }
            }

            if (team.coachName.isNotBlank()) {
                Row(modifier = Modifier.padding(top = 8.dp)) {
                    Text(
                        text = stringResource(Res.string.coach_name) + ": ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = team.coachName,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            if (team.delegateName.isNotBlank()) {
                Row {
                    Text(
                        text = stringResource(Res.string.delegate_name) + ": ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = team.delegateName,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            if (isPresident && team.coachId == null) {
                Button(
                    onClick = onSelfAssignAsCoach,
                    enabled = !isAssigning,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(Res.string.self_assign_as_coach_button))
                }
            }
        }
    }
}

@Composable
private fun ErrorMessage(
    message: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
        )
    }
}
