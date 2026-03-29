package com.jesuslcorominas.teamflowmanager.ui.team

import android.content.Intent
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
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.analytics.ScreenName
import com.jesuslcorominas.teamflowmanager.domain.model.ClubRole
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.ui.analytics.TrackScreenView
import com.jesuslcorominas.teamflowmanager.ui.components.Loading
import com.jesuslcorominas.teamflowmanager.ui.components.card.AppCard
import com.jesuslcorominas.teamflowmanager.ui.main.LocalContentBottomPadding
import com.jesuslcorominas.teamflowmanager.ui.main.search.LocalSearchState
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import com.jesuslcorominas.teamflowmanager.viewmodel.TeamListViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun TeamListScreen(
    viewModel: TeamListViewModel = koinViewModel(),
    onTeamClick: (Team) -> Unit = {},
) {
    TrackScreenView(screenName = ScreenName.TEAM, screenClass = "TeamListScreen")

    val uiState by viewModel.uiState.collectAsState()
    val shareEvent by viewModel.shareEvent.collectAsState()
    val sharingTeamId by viewModel.sharingTeamId.collectAsState()
    val assigningCoachToTeamId by viewModel.assigningCoachToTeamId.collectAsState()
    val currentUserRole by viewModel.currentUserRole.collectAsState()
    val coachFilter by viewModel.coachFilter.collectAsState()
    val searchState = LocalSearchState.current
    val context = LocalContext.current

    LaunchedEffect(searchState.query) {
        viewModel.onSearchQueryChanged(searchState.query)
    }

    // Handle share event
    LaunchedEffect(shareEvent) {
        shareEvent?.let { event ->
            val shareIntent =
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.share_team_subject, event.teamName))
                    putExtra(
                        Intent.EXTRA_TEXT,
                        context.getString(R.string.share_team_message, event.teamName, event.invitationLink),
                    )
                }
            context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_team_title)))
            viewModel.onShareEventConsumed()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is TeamListViewModel.UiState.Loading -> {
                Loading()
            }
            is TeamListViewModel.UiState.Success -> {
                val isPresident = currentUserRole == ClubRole.PRESIDENT.roleName
                Column(modifier = Modifier.fillMaxSize()) {
                    if (isPresident) {
                        Row(
                            modifier = Modifier.padding(horizontal = TFMSpacing.spacing04, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            FilterChip(
                                selected = coachFilter == TeamListViewModel.CoachFilter.ALL,
                                onClick = { viewModel.onCoachFilterChanged(TeamListViewModel.CoachFilter.ALL) },
                                label = { Text(stringResource(R.string.team_filter_all)) },
                            )
                            FilterChip(
                                selected = coachFilter == TeamListViewModel.CoachFilter.WITH_COACH,
                                onClick = { viewModel.onCoachFilterChanged(TeamListViewModel.CoachFilter.WITH_COACH) },
                                label = { Text(stringResource(R.string.team_filter_with_coach)) },
                            )
                            FilterChip(
                                selected = coachFilter == TeamListViewModel.CoachFilter.WITHOUT_COACH,
                                onClick = { viewModel.onCoachFilterChanged(TeamListViewModel.CoachFilter.WITHOUT_COACH) },
                                label = { Text(stringResource(R.string.team_filter_without_coach)) },
                            )
                        }
                    }
                    if (state.teams.isEmpty()) {
                        EmptyTeamsMessage(modifier = Modifier.weight(1f).fillMaxWidth())
                    } else {
                        TeamsListContent(
                            teams = state.teams,
                            modifier = Modifier.weight(1f),
                            onTeamClick = onTeamClick,
                            onShareTeam = { team -> viewModel.shareTeam(team) },
                            onSelfAssignAsCoach = { team -> viewModel.selfAssignAsCoachToTeam(team) },
                            sharingTeamId = sharingTeamId,
                            assigningCoachToTeamId = assigningCoachToTeamId,
                            isPresident = isPresident,
                        )
                    }
                }
            }
            is TeamListViewModel.UiState.Error -> {
                ErrorMessage(
                    message = stringResource(R.string.error_loading_teams),
                    modifier = Modifier.fillMaxSize(),
                )
            }
            is TeamListViewModel.UiState.NoClubMembership -> {
                ErrorMessage(
                    message = stringResource(R.string.no_club_membership_teams_error),
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        // Show full-screen loading overlay while sharing or assigning
        if (sharingTeamId != null || assigningCoachToTeamId != null) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                        .clickable(enabled = false) { },
                // Block all clicks
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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.no_teams_message),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
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
                top = TFMSpacing.spacing04,
                start = TFMSpacing.spacing04,
                end = TFMSpacing.spacing04,
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

                // Show share icon button only if team has no coach assigned
                if (team.coachId == null) {
                    IconButton(
                        onClick = onShare,
                        enabled = !isSharing,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = stringResource(R.string.share_team_button),
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
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.coach_name) + ": ",
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
                        text = stringResource(R.string.delegate_name) + ": ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = team.delegateName,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            // Show self-assign button for Presidents when team has no coach
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
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.self_assign_as_coach_button))
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
