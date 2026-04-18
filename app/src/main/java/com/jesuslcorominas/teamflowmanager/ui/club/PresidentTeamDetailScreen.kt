package com.jesuslcorominas.teamflowmanager.ui.club

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.model.GlobalNotificationState
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.ui.components.EmptyContent
import com.jesuslcorominas.teamflowmanager.ui.components.Loading
import com.jesuslcorominas.teamflowmanager.ui.components.card.AppCard
import com.jesuslcorominas.teamflowmanager.ui.matches.card.PlayedMatchCard
import com.jesuslcorominas.teamflowmanager.ui.players.components.PlayerList
import com.jesuslcorominas.teamflowmanager.viewmodel.PresidentTeamDetailUiState
import com.jesuslcorominas.teamflowmanager.viewmodel.PresidentTeamDetailViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.PresidentTeamStats
import com.jesuslcorominas.teamflowmanager.viewmodel.PresidentTeamTab
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun PresidentTeamDetailScreen(
    teamId: String,
    onNavigateBack: () -> Unit = {},
    viewModel: PresidentTeamDetailViewModel = koinViewModel(parameters = { parametersOf(teamId) }),
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()

    BackHandler {
        if (selectedTab != PresidentTeamTab.SUMMARY) {
            viewModel.selectTab(PresidentTeamTab.SUMMARY)
        } else {
            onNavigateBack()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is PresidentTeamDetailUiState.Loading -> Loading()
            is PresidentTeamDetailUiState.Error -> {
                EmptyContent(stringResource(R.string.president_team_detail_error))
            }
            is PresidentTeamDetailUiState.Ready -> {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    edgePadding = 0.dp,
                ) {
                    TitleMediumTab(
                        selected = selectedTab == PresidentTeamTab.SUMMARY,
                        onClick = { viewModel.selectTab(PresidentTeamTab.SUMMARY) },
                        text = stringResource(R.string.summary_tab),
                    )
                    TitleMediumTab(
                        selected = selectedTab == PresidentTeamTab.PLAYERS,
                        onClick = { viewModel.selectTab(PresidentTeamTab.PLAYERS) },
                        text = stringResource(R.string.president_team_detail_players_tab),
                    )
                    TitleMediumTab(
                        selected = selectedTab == PresidentTeamTab.MATCHES,
                        onClick = { viewModel.selectTab(PresidentTeamTab.MATCHES) },
                        text = stringResource(R.string.president_team_detail_matches_tab),
                    )
                    TitleMediumTab(
                        selected = selectedTab == PresidentTeamTab.STATS,
                        onClick = { viewModel.selectTab(PresidentTeamTab.STATS) },
                        text = stringResource(R.string.president_team_detail_stats_tab),
                    )
                    TitleMediumTab(
                        selected = selectedTab == PresidentTeamTab.NOTIFICATIONS,
                        onClick = { viewModel.selectTab(PresidentTeamTab.NOTIFICATIONS) },
                        text = stringResource(R.string.president_team_detail_notifications_tab),
                    )
                }

                when (selectedTab) {
                    PresidentTeamTab.SUMMARY -> SummaryTab(state)
                    PresidentTeamTab.PLAYERS -> PlayersTab(state)
                    PresidentTeamTab.MATCHES -> MatchesTab(state)
                    PresidentTeamTab.STATS -> StatsTab(state.stats)
                    PresidentTeamTab.NOTIFICATIONS -> {
                        val teamNotificationState by viewModel.teamNotificationState.collectAsState()
                        NotificationsTab(
                            state = teamNotificationState,
                            onMatchEventsChanged = viewModel::updateTeamMatchEvents,
                            onGoalsChanged = viewModel::updateTeamGoals,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TitleMediumTab(
    selected: Boolean,
    onClick: () -> Unit,
    text: String,
) {
    Tab(
        selected = selected,
        onClick = onClick,
        text = { Text(text = text, style = MaterialTheme.typography.titleMedium) },
    )
}

@Composable
private fun SummaryTab(state: PresidentTeamDetailUiState.Ready) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            AppCard {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = state.team.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    if (state.team.coachName.isNotBlank()) {
                        LabelValueRow(
                            label = stringResource(R.string.coach_name),
                            value = state.team.coachName,
                        )
                    }
                    if (state.team.delegateName.isNotBlank()) {
                        LabelValueRow(
                            label = stringResource(R.string.delegate_name),
                            value = state.team.delegateName,
                        )
                    }
                    LabelValueRow(
                        label = stringResource(R.string.president_team_stats_squad_size),
                        value = state.players.size.toString(),
                    )
                }
            }
        }
        item {
            AppCard {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.president_team_detail_stats_tab),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    LabelValueRow(
                        label = stringResource(R.string.president_team_stats_played),
                        value = state.stats.totalMatches.toString(),
                    )
                    LabelValueRow(
                        label = stringResource(R.string.president_team_stats_wins),
                        value = state.stats.wins.toString(),
                    )
                    LabelValueRow(
                        label = stringResource(R.string.president_team_stats_draws),
                        value = state.stats.draws.toString(),
                    )
                    LabelValueRow(
                        label = stringResource(R.string.president_team_stats_losses),
                        value = state.stats.losses.toString(),
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayersTab(state: PresidentTeamDetailUiState.Ready) {
    if (state.players.isEmpty()) {
        EmptyContent(stringResource(R.string.president_team_detail_no_players))
    } else {
        PlayerList(players = state.players)
    }
}

@Composable
private fun MatchesTab(state: PresidentTeamDetailUiState.Ready) {
    if (state.matches.isEmpty()) {
        EmptyContent(stringResource(R.string.president_team_detail_no_matches))
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(state.matches, key = { it.id }) { match ->
                if (match.status == MatchStatus.FINISHED) {
                    PlayedMatchCard(match = match, showArchiveAction = false)
                } else {
                    ScheduledMatchCard(match = match)
                }
            }
        }
    }
}

@Composable
private fun StatsTab(stats: PresidentTeamStats) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            AppCard {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    LabelValueRow(
                        label = stringResource(R.string.president_team_stats_played),
                        value = stats.totalMatches.toString(),
                    )
                    LabelValueRow(
                        label = stringResource(R.string.president_team_stats_wins),
                        value = stats.wins.toString(),
                    )
                    LabelValueRow(
                        label = stringResource(R.string.president_team_stats_draws),
                        value = stats.draws.toString(),
                    )
                    LabelValueRow(
                        label = stringResource(R.string.president_team_stats_losses),
                        value = stats.losses.toString(),
                    )
                    LabelValueRow(
                        label = stringResource(R.string.president_team_stats_goals_scored),
                        value = stats.goalsScored.toString(),
                    )
                    LabelValueRow(
                        label = stringResource(R.string.president_team_stats_goals_conceded),
                        value = stats.goalsConceded.toString(),
                    )
                    LabelValueRow(
                        label = stringResource(R.string.president_team_stats_squad_size),
                        value = stats.squadSize.toString(),
                    )
                }
            }
        }
    }
}

@Composable
private fun ScheduledMatchCard(match: Match) {
    AppCard {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = match.opponent,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            if (match.location.isNotBlank()) {
                Text(
                    text = match.location,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            match.dateTime?.let { dateTime ->
                Text(
                    text = com.jesuslcorominas.teamflowmanager.ui.util.DateFormatter.formatDateTime(dateTime),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun NotificationsTab(
    state: PresidentTeamDetailViewModel.TeamNotificationPreferencesState,
    onMatchEventsChanged: (Boolean) -> Unit,
    onGoalsChanged: (Boolean) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            AppCard {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.president_notifications_match_events),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text =
                            when (state.globalMatchEventsState) {
                                GlobalNotificationState.ALL_ON -> stringResource(R.string.president_notifications_global_on)
                                GlobalNotificationState.ALL_OFF -> stringResource(R.string.president_notifications_global_off)
                                GlobalNotificationState.MIXED -> stringResource(R.string.president_notifications_global_mixed)
                            },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Switch(
                        checked = state.matchEvents,
                        onCheckedChange = onMatchEventsChanged,
                    )
                }
            }
        }
        item {
            AppCard {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.president_notifications_goals),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text =
                            when (state.globalGoalsState) {
                                GlobalNotificationState.ALL_ON -> stringResource(R.string.president_notifications_global_on)
                                GlobalNotificationState.ALL_OFF -> stringResource(R.string.president_notifications_global_off)
                                GlobalNotificationState.MIXED -> stringResource(R.string.president_notifications_global_mixed)
                            },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Switch(
                        checked = state.goals,
                        onCheckedChange = onGoalsChanged,
                    )
                }
            }
        }
    }
}

@Composable
private fun LabelValueRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
