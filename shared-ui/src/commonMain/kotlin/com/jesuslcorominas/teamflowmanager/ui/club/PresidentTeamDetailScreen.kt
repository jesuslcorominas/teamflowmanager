package com.jesuslcorominas.teamflowmanager.ui.club

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.domain.analytics.ScreenName
import com.jesuslcorominas.teamflowmanager.domain.model.GlobalNotificationState
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.domain.model.PeriodType
import com.jesuslcorominas.teamflowmanager.ui.util.formatTime
import com.jesuslcorominas.teamflowmanager.ui.analytics.TrackScreenView
import com.jesuslcorominas.teamflowmanager.ui.components.AppBackHandler
import com.jesuslcorominas.teamflowmanager.ui.components.EmptyContent
import com.jesuslcorominas.teamflowmanager.ui.components.Loading
import com.jesuslcorominas.teamflowmanager.ui.components.card.AppCard
import com.jesuslcorominas.teamflowmanager.ui.matches.card.PlayedMatchCard
import com.jesuslcorominas.teamflowmanager.ui.players.components.PlayerList
import com.jesuslcorominas.teamflowmanager.ui.util.DateFormatter
import com.jesuslcorominas.teamflowmanager.viewmodel.PresidentTeamDetailUiState
import com.jesuslcorominas.teamflowmanager.viewmodel.PresidentTeamDetailViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.PresidentTeamStats
import com.jesuslcorominas.teamflowmanager.viewmodel.PresidentTeamTab
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import teamflowmanager.shared_ui.generated.resources.Res
import teamflowmanager.shared_ui.generated.resources.coach_name
import teamflowmanager.shared_ui.generated.resources.delegate_name
import teamflowmanager.shared_ui.generated.resources.president_notifications_global_mixed
import teamflowmanager.shared_ui.generated.resources.president_notifications_global_off
import teamflowmanager.shared_ui.generated.resources.president_notifications_global_on
import teamflowmanager.shared_ui.generated.resources.president_notifications_goals
import teamflowmanager.shared_ui.generated.resources.president_notifications_match_events
import teamflowmanager.shared_ui.generated.resources.president_team_detail_error
import teamflowmanager.shared_ui.generated.resources.president_team_detail_matches_tab
import teamflowmanager.shared_ui.generated.resources.president_team_detail_no_matches
import teamflowmanager.shared_ui.generated.resources.president_team_detail_no_players
import teamflowmanager.shared_ui.generated.resources.president_team_detail_notifications_tab
import teamflowmanager.shared_ui.generated.resources.president_team_detail_players_tab
import teamflowmanager.shared_ui.generated.resources.president_team_detail_stats_tab
import teamflowmanager.shared_ui.generated.resources.president_team_stats_draws
import teamflowmanager.shared_ui.generated.resources.president_team_stats_goals_conceded
import teamflowmanager.shared_ui.generated.resources.president_team_stats_goals_scored
import teamflowmanager.shared_ui.generated.resources.president_team_stats_losses
import teamflowmanager.shared_ui.generated.resources.president_team_stats_played
import teamflowmanager.shared_ui.generated.resources.president_team_stats_squad_size
import teamflowmanager.shared_ui.generated.resources.president_team_stats_wins
import teamflowmanager.shared_ui.generated.resources.first_half
import teamflowmanager.shared_ui.generated.resources.first_quarter
import teamflowmanager.shared_ui.generated.resources.fourth_quarter
import teamflowmanager.shared_ui.generated.resources.match_live_badge
import teamflowmanager.shared_ui.generated.resources.second_half
import teamflowmanager.shared_ui.generated.resources.second_quarter
import teamflowmanager.shared_ui.generated.resources.summary_tab
import teamflowmanager.shared_ui.generated.resources.third_quarter

@Composable
fun PresidentTeamDetailScreen(
    teamId: String,
    onNavigateBack: () -> Unit = {},
    onNavigateToMatch: (Long) -> Unit = {},
    viewModel: PresidentTeamDetailViewModel = koinViewModel(parameters = { parametersOf(teamId) }),
) {
    TrackScreenView(screenName = ScreenName.PRESIDENT_TEAM_DETAIL, screenClass = "PresidentTeamDetailScreen")

    val uiState by viewModel.uiState.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val currentTime by viewModel.currentTime.collectAsState()

    AppBackHandler {
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
                EmptyContent(stringResource(Res.string.president_team_detail_error))
            }
            is PresidentTeamDetailUiState.Ready -> {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    edgePadding = 0.dp,
                ) {
                    TitleMediumTab(
                        selected = selectedTab == PresidentTeamTab.SUMMARY,
                        onClick = { viewModel.selectTab(PresidentTeamTab.SUMMARY) },
                        text = stringResource(Res.string.summary_tab),
                    )
                    TitleMediumTab(
                        selected = selectedTab == PresidentTeamTab.PLAYERS,
                        onClick = { viewModel.selectTab(PresidentTeamTab.PLAYERS) },
                        text = stringResource(Res.string.president_team_detail_players_tab),
                    )
                    TitleMediumTab(
                        selected = selectedTab == PresidentTeamTab.MATCHES,
                        onClick = { viewModel.selectTab(PresidentTeamTab.MATCHES) },
                        text = stringResource(Res.string.president_team_detail_matches_tab),
                    )
                    TitleMediumTab(
                        selected = selectedTab == PresidentTeamTab.STATS,
                        onClick = { viewModel.selectTab(PresidentTeamTab.STATS) },
                        text = stringResource(Res.string.president_team_detail_stats_tab),
                    )
                    TitleMediumTab(
                        selected = selectedTab == PresidentTeamTab.NOTIFICATIONS,
                        onClick = { viewModel.selectTab(PresidentTeamTab.NOTIFICATIONS) },
                        text = stringResource(Res.string.president_team_detail_notifications_tab),
                    )
                }

                when (selectedTab) {
                    PresidentTeamTab.SUMMARY -> SummaryTab(state)
                    PresidentTeamTab.PLAYERS -> PlayersTab(state)
                    PresidentTeamTab.MATCHES -> MatchesTab(state, currentTime, onNavigateToMatch)
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
        contentPadding = PaddingValues(16.dp),
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
                            label = stringResource(Res.string.coach_name),
                            value = state.team.coachName,
                        )
                    }
                    if (state.team.delegateName.isNotBlank()) {
                        LabelValueRow(
                            label = stringResource(Res.string.delegate_name),
                            value = state.team.delegateName,
                        )
                    }
                    LabelValueRow(
                        label = stringResource(Res.string.president_team_stats_squad_size),
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
                        text = stringResource(Res.string.president_team_detail_stats_tab),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    LabelValueRow(
                        label = stringResource(Res.string.president_team_stats_played),
                        value = state.stats.totalMatches.toString(),
                    )
                    LabelValueRow(
                        label = stringResource(Res.string.president_team_stats_wins),
                        value = state.stats.wins.toString(),
                    )
                    LabelValueRow(
                        label = stringResource(Res.string.president_team_stats_draws),
                        value = state.stats.draws.toString(),
                    )
                    LabelValueRow(
                        label = stringResource(Res.string.president_team_stats_losses),
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
        EmptyContent(stringResource(Res.string.president_team_detail_no_players))
    } else {
        PlayerList(players = state.players)
    }
}

@Composable
private fun MatchesTab(
    state: PresidentTeamDetailUiState.Ready,
    currentTime: Long,
    onNavigateToMatch: (Long) -> Unit,
) {
    if (state.matches.isEmpty()) {
        EmptyContent(stringResource(Res.string.president_team_detail_no_matches))
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(state.matches, key = { it.id }) { match ->
                when (match.status) {
                    MatchStatus.FINISHED -> PlayedMatchCard(
                        match = match,
                        onNavigateToDetail = { onNavigateToMatch(match.id) },
                        showArchiveButton = false,
                    )
                    MatchStatus.IN_PROGRESS, MatchStatus.TIMEOUT -> LiveMatchCard(
                        match = match,
                        currentTime = currentTime,
                        onNavigateToDetail = { onNavigateToMatch(match.id) },
                    )
                    else -> ScheduledMatchCard(match = match)
                }
            }
        }
    }
}

@Composable
private fun StatsTab(stats: PresidentTeamStats) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
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
                        label = stringResource(Res.string.president_team_stats_played),
                        value = stats.totalMatches.toString(),
                    )
                    LabelValueRow(
                        label = stringResource(Res.string.president_team_stats_wins),
                        value = stats.wins.toString(),
                    )
                    LabelValueRow(
                        label = stringResource(Res.string.president_team_stats_draws),
                        value = stats.draws.toString(),
                    )
                    LabelValueRow(
                        label = stringResource(Res.string.president_team_stats_losses),
                        value = stats.losses.toString(),
                    )
                    LabelValueRow(
                        label = stringResource(Res.string.president_team_stats_goals_scored),
                        value = stats.goalsScored.toString(),
                    )
                    LabelValueRow(
                        label = stringResource(Res.string.president_team_stats_goals_conceded),
                        value = stats.goalsConceded.toString(),
                    )
                    LabelValueRow(
                        label = stringResource(Res.string.president_team_stats_squad_size),
                        value = stats.squadSize.toString(),
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveMatchCard(
    match: Match,
    currentTime: Long,
    onNavigateToDetail: () -> Unit,
) {
    val activePeriod = match.periods.firstOrNull { it.startTimeMillis > 0L && it.endTimeMillis == 0L }
    val periodLabel = activePeriod?.let { period ->
        when (match.periodType) {
            PeriodType.HALF_TIME -> when (period.periodNumber) {
                1 -> stringResource(Res.string.first_half)
                else -> stringResource(Res.string.second_half)
            }
            PeriodType.QUARTER_TIME -> when (period.periodNumber) {
                1 -> stringResource(Res.string.first_quarter)
                2 -> stringResource(Res.string.second_quarter)
                3 -> stringResource(Res.string.third_quarter)
                else -> stringResource(Res.string.fourth_quarter)
            }
        }
    } ?: ""
    val elapsedMillis = match.getTotalElapsed(currentTime)

    AppCard(modifier = Modifier.clickable { onNavigateToDetail() }) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = match.opponent,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Box(
                    modifier =
                        Modifier
                            .background(
                                color = MaterialTheme.colorScheme.error,
                                shape = MaterialTheme.shapes.extraSmall,
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.match_live_badge),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onError,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Text(
                text = "${match.goals} – ${match.opponentGoals}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = formatTime(elapsedMillis),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (periodLabel.isNotBlank()) {
                    Text(
                        text = "·",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = periodLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun ScheduledMatchCard(
    match: Match,
    onClick: (() -> Unit)? = null,
) {
    AppCard(modifier = if (onClick != null) Modifier.clickable { onClick() } else Modifier) {
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
                    text = DateFormatter.formatDateTime(dateTime),
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
                        text = stringResource(Res.string.president_notifications_match_events),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text =
                            when (state.globalMatchEventsState) {
                                GlobalNotificationState.ALL_ON -> stringResource(Res.string.president_notifications_global_on)
                                GlobalNotificationState.ALL_OFF -> stringResource(Res.string.president_notifications_global_off)
                                GlobalNotificationState.MIXED -> stringResource(Res.string.president_notifications_global_mixed)
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
                        text = stringResource(Res.string.president_notifications_goals),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text =
                            when (state.globalGoalsState) {
                                GlobalNotificationState.ALL_ON -> stringResource(Res.string.president_notifications_global_on)
                                GlobalNotificationState.ALL_OFF -> stringResource(Res.string.president_notifications_global_off)
                                GlobalNotificationState.MIXED -> stringResource(Res.string.president_notifications_global_mixed)
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
