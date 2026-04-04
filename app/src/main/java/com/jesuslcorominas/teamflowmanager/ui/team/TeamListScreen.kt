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
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

private const val LOADING_OVERLAY_ALPHA = 0.7f
private val LOADING_INDICATOR_SIZE = 48.dp
private val BUTTON_ICON_SIZE = 20.dp
private val BUTTON_ICON_SPACING = 8.dp

@Composable
fun TeamListScreen(
    viewModel: TeamListViewModel = koinViewModel(),
    onTeamClick: (Team) -> Unit = {},
) {
    TrackScreenView(screenName = ScreenName.TEAM, screenClass = "TeamListScreen")

    val uiState by viewModel.uiState.collectAsState()
    val assigningCoachToTeamId by viewModel.assigningCoachToTeamId.collectAsState()
    val currentUserRole by viewModel.currentUserRole.collectAsState()
    val coachFilter by viewModel.coachFilter.collectAsState()
    val assignCoachDialogTeam by viewModel.assignCoachDialogTeam.collectAsState()
    val clubMembers by viewModel.clubMembers.collectAsState()
    val assignCoachError by viewModel.assignCoachError.collectAsState()
    val searchState = LocalSearchState.current

    LaunchedEffect(searchState.query) {
        viewModel.onSearchQueryChanged(searchState.query)
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
                    val coachEmailMap = clubMembers.associate { it.userId to it.email }
                    if (state.teams.isEmpty()) {
                        EmptyTeamsMessage(modifier = Modifier.weight(1f).fillMaxWidth())
                    } else {
                        TeamsListContent(
                            teams = state.teams,
                            modifier = Modifier.weight(1f),
                            onTeamClick = onTeamClick,
                            onAssignCoach = { team -> viewModel.requestAssignCoach(team) },
                            onDeletePendingAssignment = { team -> viewModel.deletePendingAssignment(team) },
                            coachEmailMap = coachEmailMap,
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

        if (assigningCoachToTeamId != null) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = LOADING_OVERLAY_ALPHA))
                        .clickable(enabled = false) { },
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(LOADING_INDICATOR_SIZE),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }

    if (assignCoachDialogTeam != null) {
        AssignCoachDialog(
            team = assignCoachDialogTeam!!,
            members = clubMembers,
            error = assignCoachError,
            onDismiss = { viewModel.dismissAssignCoachDialog() },
            onAssignMember = { member -> viewModel.assignCoachByMember(member) },
            onAssignByEmail = { email -> viewModel.assignCoachByEmail(email) },
            onClearError = { viewModel.clearAssignCoachError() },
        )
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
    onAssignCoach: (Team) -> Unit,
    onDeletePendingAssignment: (Team) -> Unit,
    coachEmailMap: Map<String, String>,
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
                onAssignCoach = { onAssignCoach(team) },
                onDeletePendingAssignment = { onDeletePendingAssignment(team) },
                coachEmail = team.coachId?.let { coachEmailMap[it] },
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
    onAssignCoach: () -> Unit,
    onDeletePendingAssignment: () -> Unit,
    coachEmail: String? = null,
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
            Text(
                text = team.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
            )

            // TODO: Re-enable share button when invitation flow is revisited
            //  IconButton(onClick = onShare) { Icon(Icons.Default.Share, ...) }

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

            if (!coachEmail.isNullOrBlank()) {
                Row {
                    Text(
                        text = stringResource(R.string.coach_email) + ": ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = coachEmail,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            val pendingEmail = team.pendingCoachEmail
            if (isPresident && pendingEmail != null) {
                Row(modifier = Modifier.padding(top = 8.dp)) {
                    Text(
                        text = stringResource(R.string.pending_coach_assignment) + ": ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = pendingEmail,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Button(
                    onClick = onDeletePendingAssignment,
                    enabled = !isAssigning,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Autorenew,
                        contentDescription = null,
                        modifier = Modifier.size(BUTTON_ICON_SIZE),
                    )
                    Spacer(modifier = Modifier.width(BUTTON_ICON_SPACING))
                    Text(text = stringResource(R.string.reassign_coach_button))
                }
            } else if (isPresident && team.coachId == null) {
                Button(
                    onClick = onAssignCoach,
                    enabled = !isAssigning,
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = null,
                        modifier = Modifier.size(BUTTON_ICON_SIZE),
                    )
                    Spacer(modifier = Modifier.width(BUTTON_ICON_SPACING))
                    Text(text = stringResource(R.string.assign_coach_button))
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
