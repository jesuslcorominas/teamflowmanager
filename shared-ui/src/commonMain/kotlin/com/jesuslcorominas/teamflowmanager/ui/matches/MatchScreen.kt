package com.jesuslcorominas.teamflowmanager.ui.matches

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TimerOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.domain.analytics.ScreenName
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerActivityInterval
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.domain.model.ScorePoint
import com.jesuslcorominas.teamflowmanager.domain.model.TimelineEvent
import com.jesuslcorominas.teamflowmanager.ui.analytics.TrackScreenView
import com.jesuslcorominas.teamflowmanager.ui.components.AppIconButton
import com.jesuslcorominas.teamflowmanager.ui.components.Loading
import com.jesuslcorominas.teamflowmanager.ui.components.card.MatchTimeCard
import com.jesuslcorominas.teamflowmanager.ui.components.dialog.AppAlertDialog
import com.jesuslcorominas.teamflowmanager.ui.components.form.PlayerSortOrderBy
import com.jesuslcorominas.teamflowmanager.ui.components.form.PlayerSortOrderSelector
import com.jesuslcorominas.teamflowmanager.ui.matches.components.TimelineContent
import com.jesuslcorominas.teamflowmanager.ui.players.components.PlayerItem
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import com.jesuslcorominas.teamflowmanager.viewmodel.ExportState
import com.jesuslcorominas.teamflowmanager.viewmodel.MatchUiState
import com.jesuslcorominas.teamflowmanager.viewmodel.MatchViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.PlayerTimeItem
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import teamflowmanager.shared_ui.generated.resources.Res
import teamflowmanager.shared_ui.generated.resources.add
import teamflowmanager.shared_ui.generated.resources.add_goal_button
import teamflowmanager.shared_ui.generated.resources.add_opponent_goal_message
import teamflowmanager.shared_ui.generated.resources.add_opponent_goal_title
import teamflowmanager.shared_ui.generated.resources.begin_match
import teamflowmanager.shared_ui.generated.resources.cancel
import teamflowmanager.shared_ui.generated.resources.close
import teamflowmanager.shared_ui.generated.resources.dont_show_again
import teamflowmanager.shared_ui.generated.resources.end_timeout_button
import teamflowmanager.shared_ui.generated.resources.finish_match_button
import teamflowmanager.shared_ui.generated.resources.invalid_substitution_message
import teamflowmanager.shared_ui.generated.resources.invalid_substitution_title
import teamflowmanager.shared_ui.generated.resources.no
import teamflowmanager.shared_ui.generated.resources.no_match_message
import teamflowmanager.shared_ui.generated.resources.no_scorers_label
import teamflowmanager.shared_ui.generated.resources.own_goal_option
import teamflowmanager.shared_ui.generated.resources.own_goal_scorer_label
import teamflowmanager.shared_ui.generated.resources.pause_match_button
import teamflowmanager.shared_ui.generated.resources.pause_match_early_message
import teamflowmanager.shared_ui.generated.resources.pause_match_early_title
import teamflowmanager.shared_ui.generated.resources.resume_match_button
import teamflowmanager.shared_ui.generated.resources.scorers_dialog_title
import teamflowmanager.shared_ui.generated.resources.scorers_tab
import teamflowmanager.shared_ui.generated.resources.select_goal_scorer_title
import teamflowmanager.shared_ui.generated.resources.statistics_tab
import teamflowmanager.shared_ui.generated.resources.stop_match_early_message
import teamflowmanager.shared_ui.generated.resources.stop_match_early_period_message
import teamflowmanager.shared_ui.generated.resources.stop_match_early_title
import teamflowmanager.shared_ui.generated.resources.summary_tab
import teamflowmanager.shared_ui.generated.resources.timeline_tab
import teamflowmanager.shared_ui.generated.resources.timeout_button
import teamflowmanager.shared_ui.generated.resources.yes

private const val TAB_SCORERS = 0
private const val TAB_SUMMARY = 1
private const val TAB_TIMELINE = 2
private const val TAB_STATISTICS = 3

@Composable
fun MatchScreen(
    matchId: Long,
    onTitleChange: (String?) -> Unit = {},
    onExportReady: (uri: String) -> Unit = {},
    viewModel: MatchViewModel = koinViewModel(key = matchId.toString(), parameters = { parametersOf(matchId) }),
) {
    TrackScreenView(screenName = ScreenName.MATCH_DETAIL, screenClass = "MatchScreen")

    val uiState by viewModel.uiState.collectAsState()
    val exportState by viewModel.exportState.collectAsState()
    val isSubstitutionInProgress by viewModel.isSubstitutionInProgress.collectAsState()
    val selectedPlayerOut by viewModel.selectedPlayerOut.collectAsState()
    val showInvalidSubstitutionAlert by viewModel.showInvalidSubstitutionAlert.collectAsState()
    val showStopConfirmation by viewModel.showStopConfirmation.collectAsState()
    val showPauseConfirmation by viewModel.showPauseConfirmation.collectAsState()
    val showGoalScorerDialog by viewModel.showGoalScorerDialog.collectAsState()
    val showOpponentGoalDialog by viewModel.showOpponentGoalDialog.collectAsState()

    var currentSortOrder: PlayerSortOrderBy by remember { mutableStateOf(PlayerSortOrderBy.BY_ACTIVE_FIRST) }

    LaunchedEffect(exportState) {
        if (exportState is ExportState.Ready) {
            onExportReady((exportState as ExportState.Ready).uri)
            viewModel.exportCompleted()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            when (val state = uiState) {
                is MatchUiState.Loading -> Loading()
                is MatchUiState.NoMatch -> NoMatchState()
                is MatchUiState.Success ->
                    SuccessState(
                        state = state,
                        selectedPlayerOut = selectedPlayerOut,
                        currentSortOrder = currentSortOrder,
                        onSaveMatch = { viewModel.saveMatch() },
                        onPauseMatch = { viewModel.pauseMatch() },
                        onResumeMatch = { viewModel.resumeMatch(state.match.id) },
                        onStartTimeout = { viewModel.startTimeout() },
                        onEndTimeout = { viewModel.endTimeout() },
                        onPlayerClick = { playerId ->
                            when (selectedPlayerOut) {
                                null -> viewModel.selectPlayerOut(playerId)
                                playerId -> viewModel.clearPlayerOutSelection()
                                else -> viewModel.substitutePlayer(playerId)
                            }
                        },
                        onSortOrderChange = { currentSortOrder = it },
                        onAddGoal = { viewModel.showGoalScorerDialog() },
                        onAddOpponentGoal = { viewModel.showOpponentGoalDialog() },
                        onBeginMatch = { viewModel.beginMatch(state.match.id) },
                        onTitleChange = onTitleChange,
                    )

                is MatchUiState.Finished -> {
                    if (currentSortOrder == PlayerSortOrderBy.BY_ACTIVE_FIRST) {
                        currentSortOrder = PlayerSortOrderBy.BY_TIME_DESC
                    }
                    FinishedMatchState(
                        state = state,
                        currentSortOrder = currentSortOrder,
                        onSortOrderChange = { currentSortOrder = it },
                        onExport = { viewModel.requestExport() },
                        onTitleChange = onTitleChange,
                    )
                }
            }
        }

        if (showInvalidSubstitutionAlert) {
            InvalidSubstitutionAlertDialog(
                onDismiss = { dontShowAgain -> viewModel.dismissInvalidSubstitutionAlert(dontShowAgain) },
            )
        }

        if (showStopConfirmation) {
            StopMatchEarlyConfirmationDialog(
                onConfirm = { viewModel.confirmStopMatch() },
                onDismiss = { viewModel.dismissStopConfirmation() },
            )
        }

        showPauseConfirmation?.let {
            PauseMatchEarlyConfirmationDialog(
                isBreak = it.isBreak,
                onConfirm = { if (it.isBreak) viewModel.confirmPauseMatch() else viewModel.confirmStopMatch() },
                onDismiss = { viewModel.dismissPauseConfirmation() },
            )
        }

        if (showGoalScorerDialog) {
            val state = uiState
            if (state is MatchUiState.Success) {
                GoalScorerSelectionDialog(
                    players = state.playerTimes.filter { it.isRunning }.map { it.player },
                    onGoal = { playerId -> viewModel.registerGoal(playerId) },
                    onDismiss = { viewModel.dismissGoalScorerDialog() },
                )
            }
        }

        if (showOpponentGoalDialog) {
            OpponentGoalConfirmationDialog(
                onConfirm = { viewModel.registerOpponentGoal() },
                onDismiss = { viewModel.dismissOpponentGoalDialog() },
            )
        }

        if (isSubstitutionInProgress) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                        .pointerInput(Unit) { detectTapGestures { } },
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(64.dp),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun NoMatchState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(Res.string.no_match_message),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun SuccessState(
    state: MatchUiState.Success,
    selectedPlayerOut: Long?,
    currentSortOrder: PlayerSortOrderBy,
    onSaveMatch: () -> Unit,
    onPauseMatch: () -> Unit,
    onResumeMatch: () -> Unit,
    onStartTimeout: () -> Unit,
    onEndTimeout: () -> Unit,
    onPlayerClick: (Long) -> Unit,
    onSortOrderChange: (PlayerSortOrderBy) -> Unit,
    onAddGoal: () -> Unit,
    onAddOpponentGoal: () -> Unit,
    onBeginMatch: () -> Unit,
    onTitleChange: (String?) -> Unit,
) {
    LaunchedEffect(state.match.id, state.match.teamName, state.match.opponent) {
        onTitleChange("${state.match.teamName} - ${state.match.opponent}")
    }
    DisposableEffect(Unit) { onDispose { onTitleChange(null) } }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(
                    start = TFMSpacing.spacing04,
                    end = TFMSpacing.spacing04,
                    bottom = TFMSpacing.spacing02,
                ),
    ) {
        MatchDetailContent(
            state = state,
            selectedPlayerOut = selectedPlayerOut,
            currentSortOrder = currentSortOrder,
            onSaveMatch = onSaveMatch,
            onPauseMatch = onPauseMatch,
            onResumeMatch = onResumeMatch,
            onStartTimeout = onStartTimeout,
            onEndTimeout = onEndTimeout,
            onPlayerClick = onPlayerClick,
            onSortOrderChange = onSortOrderChange,
            onAddGoal = onAddGoal,
            onAddOpponentGoal = onAddOpponentGoal,
            onBeginMatch = onBeginMatch,
        )
    }
}

@Composable
private fun MatchDetailContent(
    state: MatchUiState.Success,
    selectedPlayerOut: Long?,
    currentSortOrder: PlayerSortOrderBy,
    onSaveMatch: () -> Unit,
    onPauseMatch: () -> Unit,
    onResumeMatch: () -> Unit,
    onStartTimeout: () -> Unit,
    onEndTimeout: () -> Unit,
    onPlayerClick: (Long) -> Unit,
    onSortOrderChange: (PlayerSortOrderBy) -> Unit,
    onAddGoal: () -> Unit,
    onAddOpponentGoal: () -> Unit,
    onBeginMatch: () -> Unit,
) {
    var showScorersPopup by remember { mutableStateOf(false) }

    if (showScorersPopup) {
        ScorersDialog(
            events = state.timelineEvents,
            onDismiss = { showScorersPopup = false },
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        val scoreBoardClick =
            if (state.match.status != MatchStatus.SCHEDULED) {
                { showScorersPopup = true }
            } else {
                null
            }
        MatchTimeCard(
            match = state.match,
            currentTime = state.currentTime,
            onScoreBoardClick = scoreBoardClick,
        )

        PlayerSortOrderRow(
            currentSortOrder = currentSortOrder,
            onSortOrderChange = onSortOrderChange,
        )

        // Player list — click-based substitution (no drag-drop in KMP-23)
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(TFMSpacing.spacing02),
        ) {
            items(
                items = state.playerTimes.sortedBy(currentSortOrder, state.match),
                key = { it.player.id },
            ) { playerTimeItem ->
                val isPlaying = if (state.match.isInProgress) playerTimeItem.isRunning else false
                PlayerItem(
                    modifier =
                        Modifier.animateItem(
                            fadeInSpec = spring(stiffness = Spring.StiffnessLow),
                            placementSpec = spring(),
                            fadeOutSpec = tween(durationMillis = 300),
                        ),
                    player = playerTimeItem.player,
                    showPositions = false,
                    isPlaying = isPlaying,
                    timeMillis = playerTimeItem.timeMillis,
                    showCaptainBadge = playerTimeItem.player.id == state.match.captainId,
                    showGoalkeeperBadge = playerTimeItem.player.positions.any { it == Position.Goalkeeper },
                    isSelected = selectedPlayerOut == playerTimeItem.player.id,
                    onClick =
                        if (state.match.isInProgress) {
                            { onPlayerClick(playerTimeItem.player.id) }
                        } else {
                            null
                        },
                )
            }
        }

        Spacer(modifier = Modifier.padding(TFMSpacing.spacing02))

        BottomButtons(
            state = state,
            onSaveMatch = onSaveMatch,
            onPauseMatch = onPauseMatch,
            onResumeMatch = onResumeMatch,
            onStartTimeout = onStartTimeout,
            onEndTimeout = onEndTimeout,
            onAddGoal = onAddGoal,
            onAddOpponentGoal = onAddOpponentGoal,
            onBeginMatch = onBeginMatch,
        )
    }
}

private fun List<PlayerTimeItem>.sortedBy(
    sortOrder: PlayerSortOrderBy,
    match: Match,
): List<PlayerTimeItem> =
    when (sortOrder) {
        PlayerSortOrderBy.BY_NUMBER -> sortedBy { it.player.number }
        PlayerSortOrderBy.BY_TIME_DESC ->
            sortedWith(
                compareByDescending<PlayerTimeItem> { it.timeMillis }.thenBy { it.player.number },
            )
        PlayerSortOrderBy.BY_TIME_ASC ->
            sortedWith(
                compareBy<PlayerTimeItem> { it.timeMillis }.thenBy { it.player.number },
            )
        PlayerSortOrderBy.BY_ACTIVE_FIRST ->
            when (match.status) {
                MatchStatus.SCHEDULED ->
                    sortedWith(
                        compareByDescending<PlayerTimeItem> { match.startingLineupIds.contains(it.player.id) }
                            .thenBy { it.player.number },
                    )
                MatchStatus.PAUSED, MatchStatus.TIMEOUT ->
                    sortedWith(
                        compareByDescending<PlayerTimeItem> { it.isPaused }.thenBy { it.player.number },
                    )
                else ->
                    sortedWith(
                        compareByDescending<PlayerTimeItem> { it.isRunning }.thenBy { it.player.number },
                    )
            }
    }

@Composable
private fun PlayerSortOrderRow(
    availableSorts: List<PlayerSortOrderBy> = PlayerSortOrderBy.entries,
    currentSortOrder: PlayerSortOrderBy,
    onSortOrderChange: (PlayerSortOrderBy) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = TFMSpacing.spacing01),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlayerSortOrderSelector(
            availableSorts = availableSorts,
            currentSortOrder = currentSortOrder,
            onSortOrderChange = onSortOrderChange,
        )
    }
}

@Composable
private fun BottomButtons(
    state: MatchUiState.Success,
    onSaveMatch: () -> Unit,
    onPauseMatch: () -> Unit,
    onResumeMatch: () -> Unit,
    onStartTimeout: () -> Unit,
    onEndTimeout: () -> Unit,
    onAddGoal: () -> Unit,
    onAddOpponentGoal: () -> Unit,
    onBeginMatch: () -> Unit,
) {
    if (state.match.isStarted) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(TFMSpacing.spacing02),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(TFMSpacing.spacing02),
            ) {
                GoalButton(
                    modifier = Modifier.weight(1F),
                    enabled = state.match.isInProgress,
                    onAddGoal = onAddGoal,
                )

                TimeoutButton(
                    enabled = state.match.isInProgress || state.match.status == MatchStatus.TIMEOUT,
                    isTimeout = state.match.status == MatchStatus.TIMEOUT,
                    onClick = if (state.match.status == MatchStatus.TIMEOUT) onEndTimeout else onStartTimeout,
                )

                AppIconButton(
                    imageVector =
                        if (state.match.isInProgress || state.match.status == MatchStatus.TIMEOUT) {
                            Icons.Filled.Pause
                        } else {
                            Icons.Filled.PlayArrow
                        },
                    contentDescription =
                        stringResource(
                            if (state.match.isInProgress) {
                                Res.string.pause_match_button
                            } else {
                                Res.string.resume_match_button
                            },
                        ),
                    tint =
                        if ((state.match.canPause() && state.match.isInProgress) || state.match.status == MatchStatus.PAUSED) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        },
                    enabled =
                        if (state.match.status == MatchStatus.TIMEOUT) {
                            false
                        } else if (state.match.isInProgress) {
                            state.match.canPause()
                        } else {
                            true
                        },
                    onClick = if (state.match.isInProgress) onPauseMatch else onResumeMatch,
                )

                AppIconButton(
                    imageVector = Icons.Filled.Stop,
                    contentDescription = stringResource(Res.string.finish_match_button),
                    tint = MaterialTheme.colorScheme.error,
                    onClick = onSaveMatch,
                )

                GoalButton(
                    modifier = Modifier.weight(1F),
                    enabled = state.match.isInProgress,
                    isOpponent = true,
                    onAddGoal = onAddOpponentGoal,
                )
            }
        }
    } else {
        Button(onClick = onBeginMatch, modifier = Modifier.fillMaxWidth()) {
            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = stringResource(Res.string.begin_match))
            Spacer(modifier = Modifier.width(TFMSpacing.spacing02))
            Text(text = stringResource(Res.string.begin_match))
        }
    }
}

@Composable
private fun TimeoutButton(
    enabled: Boolean,
    isTimeout: Boolean = false,
    onClick: () -> Unit,
) {
    AppIconButton(
        internalModifier = Modifier.size(32.dp),
        imageVector = if (isTimeout) Icons.Default.TimerOff else Icons.Default.Timer,
        contentDescription =
            stringResource(
                if (isTimeout) Res.string.end_timeout_button else Res.string.timeout_button,
            ),
        enabled = enabled,
        tint =
            when {
                !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                isTimeout -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.primary
            },
        onClick = onClick,
    )
}

@Composable
private fun GoalButton(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    isOpponent: Boolean = false,
    onAddGoal: () -> Unit,
) {
    AppIconButton(
        modifier = modifier.size(64.dp),
        internalModifier =
            Modifier
                .size(48.dp)
                .then(if (isOpponent) Modifier.graphicsLayer(scaleX = -1f) else Modifier),
        imageVector = Icons.Default.SportsSoccer,
        contentDescription = stringResource(Res.string.add_goal_button),
        enabled = enabled,
        tint =
            when {
                !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                isOpponent -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.primary
            },
        onClick = onAddGoal,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FinishedMatchState(
    state: MatchUiState.Finished,
    currentSortOrder: PlayerSortOrderBy,
    onSortOrderChange: (PlayerSortOrderBy) -> Unit,
    onExport: () -> Unit,
    onTitleChange: (String?) -> Unit,
) {
    LaunchedEffect(state.match.id, state.match.teamName, state.match.opponent) {
        onTitleChange("${state.match.teamName} - ${state.match.opponent}")
    }
    DisposableEffect(Unit) { onDispose { onTitleChange(null) } }

    var selectedTab by remember { mutableIntStateOf(TAB_SCORERS) }
    var showScorersPopup by remember { mutableStateOf(false) }

    if (showScorersPopup) {
        ScorersDialog(
            events = state.timelineEvents,
            onDismiss = { showScorersPopup = false },
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier =
                Modifier.padding(
                    horizontal = TFMSpacing.spacing04,
                    vertical = TFMSpacing.spacing02,
                ),
        ) {
            MatchTimeCard(
                match = state.match,
                currentTime = state.currentTime,
                onExport = onExport,
                onScoreBoardClick = { showScorersPopup = true },
            )
        }

        ScrollableTabRow(
            modifier = Modifier.fillMaxWidth(),
            selectedTabIndex = selectedTab,
            edgePadding = TFMSpacing.spacing04,
        ) {
            Tab(
                selected = selectedTab == TAB_SCORERS,
                onClick = { selectedTab = TAB_SCORERS },
                text = {
                    Text(
                        text = stringResource(Res.string.scorers_tab),
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
            )
            Tab(
                selected = selectedTab == TAB_SUMMARY,
                onClick = { selectedTab = TAB_SUMMARY },
                text = {
                    Text(
                        text = stringResource(Res.string.summary_tab),
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
            )
            Tab(
                selected = selectedTab == TAB_TIMELINE,
                onClick = { selectedTab = TAB_TIMELINE },
                text = {
                    Text(
                        text = stringResource(Res.string.timeline_tab),
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
            )
            Tab(
                selected = selectedTab == TAB_STATISTICS,
                onClick = { selectedTab = TAB_STATISTICS },
                text = {
                    Text(
                        text = stringResource(Res.string.statistics_tab),
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedTab) {
                TAB_SCORERS ->
                    ScorersTabContent(events = state.timelineEvents)
                TAB_SUMMARY ->
                    SummaryTabContent(
                        state = state,
                        currentSortOrder = currentSortOrder,
                        onSortOrderChange = onSortOrderChange,
                    )
                TAB_TIMELINE ->
                    TimelineContent(
                        events = state.timelineEvents,
                        modifier = Modifier.fillMaxSize(),
                    )
                TAB_STATISTICS ->
                    StatisticsTabContent(
                        scoreEvolution = state.scoreEvolution,
                        playerActivity = state.playerActivity,
                    )
            }
        }
    }
}

private data class ScorerEntry(val name: String, val count: Int)

private fun aggregateScorers(events: List<TimelineEvent>): Pair<List<ScorerEntry>, Int> {
    val scorerMap = mutableMapOf<Long, ScorerEntry>()
    var ownGoalCount = 0
    events.filterIsInstance<TimelineEvent.GoalScored>()
        .filter { !it.isOpponentGoal }
        .forEach { goal ->
            if (goal.isOwnGoal) {
                ownGoalCount++
            } else {
                goal.scorer?.let { player ->
                    val name = "${player.firstName} ${player.lastName}"
                    val current = scorerMap[player.id]
                    scorerMap[player.id] = ScorerEntry(name, (current?.count ?: 0) + 1)
                }
            }
        }
    val scorers = scorerMap.values.sortedByDescending { it.count }
    return scorers to ownGoalCount
}

@Composable
private fun ScorersTabContent(events: List<TimelineEvent>) {
    val (scorers, ownGoalCount) = remember(events) { aggregateScorers(events) }
    val noGoals = scorers.isEmpty() && ownGoalCount == 0

    if (noGoals) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(Res.string.no_scorers_label),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = TFMSpacing.spacing04),
            contentPadding = PaddingValues(top = TFMSpacing.spacing03, bottom = TFMSpacing.spacing04),
            verticalArrangement = Arrangement.spacedBy(TFMSpacing.spacing02),
        ) {
            items(scorers, key = { it.name }) { entry ->
                ScorerRow(name = entry.name, count = entry.count)
            }
            if (ownGoalCount > 0) {
                item(key = "own_goal") {
                    ScorerRow(name = stringResource(Res.string.own_goal_scorer_label), count = ownGoalCount)
                }
            }
        }
    }
}

@Composable
private fun ScorerRow(
    name: String,
    count: Int,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = TFMSpacing.spacing02),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(TFMSpacing.spacing02),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.SportsSoccer,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(text = name, style = MaterialTheme.typography.bodyLarge)
        }
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun ScorersDialog(
    events: List<TimelineEvent>,
    onDismiss: () -> Unit,
) {
    val (scorers, ownGoalCount) = remember(events) { aggregateScorers(events) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(Res.string.scorers_dialog_title),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            if (scorers.isEmpty() && ownGoalCount == 0) {
                Text(
                    text = stringResource(Res.string.no_scorers_label),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                LazyColumn {
                    items(scorers, key = { it.name }) { entry ->
                        ScorerItem(
                            number = entry.count.toString(),
                            name = entry.name,
                            onScorerSelected = {},
                        )
                    }
                    if (ownGoalCount > 0) {
                        item(key = "own_goal") {
                            ScorerItem(
                                number = ownGoalCount.toString(),
                                name = stringResource(Res.string.own_goal_scorer_label),
                                onScorerSelected = {},
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.close)) }
        },
        shape = MaterialTheme.shapes.medium,
    )
}

@Composable
private fun SummaryTabContent(
    state: MatchUiState.Finished,
    currentSortOrder: PlayerSortOrderBy,
    onSortOrderChange: (PlayerSortOrderBy) -> Unit,
) {
    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = TFMSpacing.spacing04),
        contentPadding = PaddingValues(top = TFMSpacing.spacing03, bottom = TFMSpacing.spacing04),
        verticalArrangement = Arrangement.spacedBy(TFMSpacing.spacing03),
    ) {
        item {
            PlayerSortOrderRow(
                availableSorts = PlayerSortOrderBy.entries.minus(PlayerSortOrderBy.BY_ACTIVE_FIRST),
                currentSortOrder = currentSortOrder,
                onSortOrderChange = onSortOrderChange,
            )
        }

        items(
            items = state.playerTimes.sortedBy(currentSortOrder, state.match),
            key = { it.player.id },
        ) { playerTimeItem ->
            PlayerItem(
                modifier =
                    Modifier.animateItem(
                        fadeInSpec = spring(stiffness = Spring.StiffnessLow),
                        placementSpec = spring(),
                        fadeOutSpec = tween(durationMillis = 300),
                    ),
                player = playerTimeItem.player,
                showPositions = false,
                isPlaying = false,
                timeMillis = playerTimeItem.timeMillis,
                showCaptainBadge = playerTimeItem.player.id == state.match.captainId,
                showGoalkeeperBadge = playerTimeItem.player.positions.any { it == Position.Goalkeeper },
                isSelected = false,
            )
        }
    }
}

@Composable
private fun StatisticsTabContent(
    scoreEvolution: List<ScorePoint>,
    playerActivity: List<PlayerActivityInterval>,
) {
    // Charts deferred to KMP-28 — show placeholder when data is available
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        if (scoreEvolution.isEmpty() && playerActivity.isEmpty()) {
            Text(
                text = stringResource(Res.string.no_match_message),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            // Statistics summary: substitutions in a lazy column
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = TFMSpacing.spacing04),
                contentPadding =
                    PaddingValues(
                        top = TFMSpacing.spacing03,
                        bottom = TFMSpacing.spacing04,
                    ),
                verticalArrangement = Arrangement.spacedBy(TFMSpacing.spacing03),
            ) {
                item {
                    Text(
                        text = stringResource(Res.string.statistics_tab),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

// region Dialogs

@Composable
private fun InvalidSubstitutionAlertDialog(onDismiss: (dontShowAgain: Boolean) -> Unit) {
    var dontShowAgain by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { onDismiss(false) },
        title = {
            Text(
                stringResource(Res.string.invalid_substitution_title),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Column {
                Text(
                    stringResource(Res.string.invalid_substitution_message),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.padding(TFMSpacing.spacing02))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = TFMSpacing.spacing02),
                ) {
                    Checkbox(checked = dontShowAgain, onCheckedChange = { dontShowAgain = it })
                    Spacer(modifier = Modifier.padding(TFMSpacing.spacing01))
                    Text(
                        stringResource(Res.string.dont_show_again),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onDismiss(dontShowAgain) }) {
                Text(stringResource(Res.string.close))
            }
        },
        shape = MaterialTheme.shapes.medium,
    )
}

@Composable
private fun StopMatchEarlyConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(Res.string.stop_match_early_title),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Text(
                stringResource(Res.string.stop_match_early_period_message),
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(Res.string.yes)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.no)) }
        },
        shape = MaterialTheme.shapes.medium,
    )
}

@Composable
private fun PauseMatchEarlyConfirmationDialog(
    isBreak: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AppAlertDialog(
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        confirmText = stringResource(Res.string.yes),
        dismissText = stringResource(Res.string.no),
        title = stringResource(if (isBreak) Res.string.pause_match_early_title else Res.string.stop_match_early_title),
        message =
            stringResource(
                if (isBreak) Res.string.pause_match_early_message else Res.string.stop_match_early_message,
            ),
    )
}

@Composable
private fun GoalScorerSelectionDialog(
    players: List<Player>,
    onGoal: (Long?) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(Res.string.select_goal_scorer_title),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            LazyColumn {
                items(players) { player ->
                    ScorerItem(
                        number = player.number.toString(),
                        name = "${player.firstName} ${player.lastName}",
                        onScorerSelected = { onGoal(player.id) },
                    )
                }
                item {
                    ScorerItem(
                        number = "-",
                        name = stringResource(Res.string.own_goal_option),
                        onScorerSelected = { onGoal(null) },
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.cancel)) }
        },
        shape = MaterialTheme.shapes.medium,
    )
}

@Composable
private fun ScorerItem(
    number: String,
    name: String,
    onScorerSelected: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = TFMSpacing.spacing01),
        onClick = onScorerSelected,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(TFMSpacing.spacing03),
            horizontalArrangement = Arrangement.spacedBy(TFMSpacing.spacing02),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = number, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = name, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun OpponentGoalConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(Res.string.add_opponent_goal_title),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Text(
                stringResource(Res.string.add_opponent_goal_message),
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(Res.string.add)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.cancel)) }
        },
        shape = MaterialTheme.shapes.medium,
    )
}

// endregion
