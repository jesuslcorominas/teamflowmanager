package com.jesuslcorominas.teamflowmanager.ui.matches

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryScrollableTabRow
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.analytics.ScreenName
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.domain.model.PeriodType
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerActivityInterval
import com.jesuslcorominas.teamflowmanager.domain.model.ScorePoint
import com.jesuslcorominas.teamflowmanager.domain.model.TimelineEvent
import com.jesuslcorominas.teamflowmanager.ui.analytics.TrackScreenView
import com.jesuslcorominas.teamflowmanager.ui.components.AppIconButton
import com.jesuslcorominas.teamflowmanager.ui.components.Loading
import com.jesuslcorominas.teamflowmanager.ui.components.card.MatchTimeCard
import com.jesuslcorominas.teamflowmanager.ui.components.card.SubstitutionCard
import com.jesuslcorominas.teamflowmanager.ui.components.dialog.AppAlertDialog
import com.jesuslcorominas.teamflowmanager.ui.components.dragdrop.DragDropContainer
import com.jesuslcorominas.teamflowmanager.ui.components.dragdrop.DraggablePlayerItem
import com.jesuslcorominas.teamflowmanager.ui.components.dragdrop.DropTargetPlayerItem
import com.jesuslcorominas.teamflowmanager.ui.components.dragdrop.rememberDragDropState
import com.jesuslcorominas.teamflowmanager.ui.components.form.PlayerSortOrderBy
import com.jesuslcorominas.teamflowmanager.ui.components.form.PlayerSortOrderSelector
import com.jesuslcorominas.teamflowmanager.ui.matches.components.PlayerActivityChart
import com.jesuslcorominas.teamflowmanager.ui.matches.components.TimelineContent
import com.jesuslcorominas.teamflowmanager.ui.players.components.PlayerItem
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMAppTheme
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import com.jesuslcorominas.teamflowmanager.viewmodel.ExportState
import com.jesuslcorominas.teamflowmanager.viewmodel.MatchUiState
import com.jesuslcorominas.teamflowmanager.viewmodel.MatchViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.PlayerTimeItem
import com.jesuslcorominas.teamflowmanager.viewmodel.SubstitutionItem
import org.koin.androidx.compose.koinViewModel
import androidx.core.net.toUri

private const val TAB_SUMMARY = 0
//private const val TAB_SUBSTITUTIONS = 1
private const val TAB_TIMELINE = 1
private const val TAB_STATISTICS = 2

@Composable
fun MatchScreen(viewModel: MatchViewModel = koinViewModel(), onTitleChange: (String?) -> Unit) {
    TrackScreenView(screenName = ScreenName.MATCH_DETAIL, screenClass = "MatchScreen")

    val uiState by viewModel.uiState.collectAsState()
    val exportState by viewModel.exportState.collectAsState()

    // TODO try to extract this from viewmodel
    val selectedPlayerOut by viewModel.selectedPlayerOut.collectAsState()
    val showInvalidSubstitutionAlert by viewModel.showInvalidSubstitutionAlert.collectAsState()
    val showStopConfirmation by viewModel.showStopConfirmation.collectAsState()
    val showPauseConfirmation by viewModel.showPauseConfirmation.collectAsState()
    val showGoalScorerDialog by viewModel.showGoalScorerDialog.collectAsState()
    val showOpponentGoalDialog by viewModel.showOpponentGoalDialog.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current

    var currentSortOrder: PlayerSortOrderBy by remember { mutableStateOf(PlayerSortOrderBy.BY_ACTIVE_FIRST) }

    // Handle export state
    LaunchedEffect(exportState) {
        if (exportState is ExportState.Ready) {
            val state = exportState as ExportState.Ready
            val uri = state.uri.toUri()

            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(
                android.content.Intent.createChooser(
                    shareIntent,
                    context.getString(R.string.export_share_title)
                )
            )

            viewModel.exportCompleted()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        when (val state = uiState) {
            is MatchUiState.Loading -> Loading()
            is MatchUiState.NoMatch -> NoMatchState()
            is MatchUiState.Success -> SuccessState(
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
                onDragDropSubstitute = { playerInId, playerOutId ->
                    viewModel.substitutePlayerDirect(playerInId, playerOutId)
                },
                onSortOrderChange = { currentSortOrder = it },
                onAddGoal = { viewModel.showGoalScorerDialog() },
                onAddOpponentGoal = { viewModel.showOpponentGoalDialog() },
                onBeginMatch = { viewModel.beginMatch(state.match.id) },
                onTitleChange = onTitleChange
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
                    onTitleChange = onTitleChange
                )
            }
        }

        // Show alert if trying to select an inactive player
        if (showInvalidSubstitutionAlert) {
            InvalidSubstitutionAlertDialog(
                onDismiss = { dontShowAgain ->
                    viewModel.dismissInvalidSubstitutionAlert(dontShowAgain)
                }
            )
        }

        // Show confirmation dialog if stopping match early
        if (showStopConfirmation) {
            StopMatchEarlyConfirmationDialog(
                onConfirm = { viewModel.confirmStopMatch() },
                onDismiss = { viewModel.dismissStopConfirmation() }
            )
        }

        // Show confirmation dialog if pausing match early
        showPauseConfirmation?.let {
            PauseMatchEarlyConfirmationDialog(
                isBreak = it.isBreak,
                onConfirm = { if (it.isBreak) viewModel.confirmPauseMatch() else viewModel.confirmStopMatch() },
                onDismiss = { viewModel.dismissPauseConfirmation() }
            )
        }

        // Show goal scorer selection dialog
        if (showGoalScorerDialog) {
            val state = uiState
            if (state is MatchUiState.Success) {
                GoalScorerSelectionDialog(
                    players = state.playerTimes.filter { it.isRunning }.map { it.player },
                    onGoal = { playerId -> viewModel.registerGoal(playerId) },
                    onDismiss = { viewModel.dismissGoalScorerDialog() }
                )
            }
        }

        // Show opponent goal confirmation dialog
        if (showOpponentGoalDialog) {
            OpponentGoalConfirmationDialog(
                onConfirm = { viewModel.registerOpponentGoal() },
                onDismiss = { viewModel.dismissOpponentGoalDialog() }
            )
        }
    }
}

@Composable
private fun NoMatchState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.no_match_message),
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
    onDragDropSubstitute: (playerInId: Long, playerOutId: Long) -> Unit,
    onSortOrderChange: (PlayerSortOrderBy) -> Unit,
    onAddGoal: () -> Unit,
    onAddOpponentGoal: () -> Unit,
    onBeginMatch: () -> Unit,
    onTitleChange: (String?) -> Unit,
) {
    LaunchedEffect(state.match.id, state.match.teamName, state.match.opponent) {
        onTitleChange("${state.match.teamName} - ${state.match.opponent}")
    }

    DisposableEffect(Unit) {
        onDispose { onTitleChange(null) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = TFMSpacing.spacing04,
                end = TFMSpacing.spacing04,
                bottom = TFMSpacing.spacing02
            )
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
            onDragDropSubstitute = onDragDropSubstitute,
            onSortOrderChange = onSortOrderChange,
            onAddGoal = onAddGoal,
            onAddOpponentGoal = onAddOpponentGoal,
            onBeginMatch = onBeginMatch
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
    onDragDropSubstitute: (playerInId: Long, playerOutId: Long) -> Unit,
    onSortOrderChange: (PlayerSortOrderBy) -> Unit,
    onAddGoal: () -> Unit,
    onAddOpponentGoal: () -> Unit,
    onBeginMatch: () -> Unit
) {
    val dragDropState = rememberDragDropState()
    val listState = rememberLazyListState()

    Column(modifier = Modifier.fillMaxSize()) {
        MatchTimeCard(match = state.match, currentTime = state.currentTime)

        PlayerSortOrder(
            currentSortOrder = currentSortOrder,
            onSortOrderChange = onSortOrderChange
        )

        DragDropContainer(
            dragDropState = dragDropState,
            listState = listState,
            modifier = Modifier.weight(1f)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(TFMSpacing.spacing02),
            ) {
                items(
                    items = state.playerTimes.sortedBy(currentSortOrder, state.match),
                    key = { it.player.id }
                ) { playerTimeItem ->
                    val isPlaying = if (state.match.isInProgress) playerTimeItem.isRunning else false

                    // Wrap each player item with drag-drop functionality
                    if (state.match.isInProgress) {
                        if (isPlaying) {
                            // Active players are drop targets
                            DropTargetPlayerItem(
                                playerId = playerTimeItem.player.id,
                                isPlaying = true,
                                dragDropState = dragDropState,
                                onDrop = {
                                    dragDropState.draggedPlayer?.let { draggedPlayer ->
                                        onDragDropSubstitute(
                                            draggedPlayer.id,
                                            playerTimeItem.player.id
                                        )
                                    }
                                    dragDropState.reset()
                                }
                            ) {
                                PlayerItem(
                                    modifier = Modifier.animateItem(
                                        fadeInSpec = spring(stiffness = Spring.StiffnessLow),
                                        placementSpec = spring(),
                                        fadeOutSpec = tween(durationMillis = 300)
                                    ),
                                    player = playerTimeItem.player,
                                    showPositions = false,
                                    isPlaying = true,
                                    timeMillis = playerTimeItem.timeMillis,
                                    showCaptainBadge = playerTimeItem.player.id == state.match.captainId,
                                    showGoalkeeperBadge = playerTimeItem.player.positions.any { it == Position.Goalkeeper },
                                    isSelected = selectedPlayerOut == playerTimeItem.player.id,
                                    onClick = { onPlayerClick(playerTimeItem.player.id) },
                                )
                            }
                        } else {
                            // Inactive players are draggable
                            DraggablePlayerItem(
                                player = playerTimeItem.player,
                                isPlaying = false,
                                dragDropState = dragDropState,
                                // onDragEnd is optional - container handles the actual drag end
                            ) {
                                PlayerItem(
                                    modifier = Modifier.animateItem(
                                        fadeInSpec = spring(stiffness = Spring.StiffnessLow),
                                        placementSpec = spring(),
                                        fadeOutSpec = tween(durationMillis = 300)
                                    ),
                                    player = playerTimeItem.player,
                                    showPositions = false,
                                    isPlaying = false,
                                    timeMillis = playerTimeItem.timeMillis,
                                    showCaptainBadge = playerTimeItem.player.id == state.match.captainId,
                                    showGoalkeeperBadge = playerTimeItem.player.positions.any { it == Position.Goalkeeper },
                                    isSelected = selectedPlayerOut == playerTimeItem.player.id,
                                    onClick = { onPlayerClick(playerTimeItem.player.id) },
                                )
                            }
                        }
                    } else {
                        // Match not in progress - no drag-drop
                        PlayerItem(
                            modifier = Modifier.animateItem(
                                fadeInSpec = spring(stiffness = Spring.StiffnessLow),
                                placementSpec = spring(),
                                fadeOutSpec = tween(durationMillis = 300)
                            ),
                            player = playerTimeItem.player,
                            showPositions = false,
                            isPlaying = false,
                            timeMillis = playerTimeItem.timeMillis,
                            showCaptainBadge = playerTimeItem.player.id == state.match.captainId,
                            showGoalkeeperBadge = playerTimeItem.player.positions.any { it == Position.Goalkeeper },
                            isSelected = false,
                            onClick = null,
                        )
                    }
                }
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
            onBeginMatch = onBeginMatch
        )
    }
}

private fun List<PlayerTimeItem>.sortedBy(sortOrder: PlayerSortOrderBy, match: Match): List<PlayerTimeItem> =
    when (sortOrder) {
        PlayerSortOrderBy.BY_NUMBER -> sortedBy { it.player.number }
        PlayerSortOrderBy.BY_TIME_DESC -> sortedWith(
            compareByDescending<PlayerTimeItem> { it.timeMillis }
                .thenBy { it.player.number }
        )

        PlayerSortOrderBy.BY_TIME_ASC -> sortedWith(
            compareBy<PlayerTimeItem> { it.timeMillis }
                .thenBy { it.player.number }
        )

        PlayerSortOrderBy.BY_ACTIVE_FIRST -> {
            when (match.status) {
                MatchStatus.SCHEDULED -> sortedWith(
                    compareByDescending<PlayerTimeItem> { match.startingLineupIds.contains(it.player.id) }
                        .thenBy { it.player.number }
                )

                MatchStatus.PAUSED, MatchStatus.TIMEOUT -> sortedWith(
                    compareByDescending<PlayerTimeItem> { it.isPaused }
                        .thenBy { it.player.number }
                )

                else -> sortedWith(
                    compareByDescending<PlayerTimeItem> { it.isRunning }
                        .thenBy { it.player.number }
                )
            }
        }
    }


@Composable
fun PlayerSortOrder(
    availableSorts: List<PlayerSortOrderBy> = PlayerSortOrderBy.entries,
    currentSortOrder: PlayerSortOrderBy,
    onSortOrderChange: (PlayerSortOrderBy) -> Unit,
) {
    Row(
        modifier = Modifier
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
    onBeginMatch: () -> Unit
) {
    if (state.match.isStarted) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(TFMSpacing.spacing02)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(TFMSpacing.spacing02),
            ) {
                GoalButton(
                    modifier = Modifier.weight(1F),
                    enabled = state.match.isInProgress,
                    onAddGoal = onAddGoal
                )

                TimeoutButton(
                    enabled = state.match.isInProgress || state.match.status == MatchStatus.TIMEOUT,
                    isTimeout = state.match.status == MatchStatus.TIMEOUT,
                    onClick = if (state.match.status == MatchStatus.TIMEOUT) onEndTimeout else onStartTimeout
                )

                AppIconButton(
                    imageVector = if (state.match.isInProgress || state.match.status == MatchStatus.TIMEOUT) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (state.match.isInProgress) R.string.pause_match_button else R.string.resume_match_button,
                    tint = if ((state.match.canPause() && state.match.isInProgress) || state.match.status == MatchStatus.PAUSED) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    },
                    enabled = if (state.match.status == MatchStatus.TIMEOUT) false else if (state.match.isInProgress) state.match.canPause() else true,
                    onClick = if (state.match.isInProgress) onPauseMatch else onResumeMatch
                )

                AppIconButton(
                    imageVector = Icons.Filled.Stop,
                    contentDescription = stringResource(R.string.finish_match_button),
                    tint = MaterialTheme.colorScheme.error,
                    onClick = onSaveMatch,
                )

                GoalButton(
                    modifier = Modifier.weight(1F),
                    enabled = state.match.isInProgress,
                    isOpponent = true,
                    onAddGoal = onAddOpponentGoal
                )
            }
        }
    } else {
        Button(
            onClick = onBeginMatch,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = stringResource(R.string.begin_match),
            )
            Spacer(modifier = Modifier.width(TFMSpacing.spacing02))
            Text(text = stringResource(R.string.begin_match))
        }
    }
}

@Composable
private fun TimeoutButton(
    enabled: Boolean,
    isTimeout: Boolean = false,
    onClick: () -> Unit
) {
    AppIconButton(
        internalModifier = Modifier.size(32.dp),
        icon = if (isTimeout) R.drawable.ic_whistle else R.drawable.ic_timeout,
        contentDescription = if (isTimeout) R.string.end_timeout_button else R.string.timeout_button,
        enabled = enabled,
        tint = when {
            !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            isTimeout -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.primary
        },
        onClick = onClick
    )
}

@Composable
private fun GoalButton(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    isOpponent: Boolean = false,
    onAddGoal: () -> Unit
) {
    AppIconButton(
        modifier = modifier.size(64.dp),
        internalModifier = Modifier
            .size(48.dp)
            .then(if (isOpponent) Modifier.graphicsLayer(scaleX = -1f) else Modifier),
        imageVector = ImageVector.vectorResource(R.drawable.ic_goal),
        contentDescription = stringResource(R.string.add_goal_button),
        enabled = enabled,
        tint = when {
            !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            isOpponent -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.primary
        },
        onClick = onAddGoal
    )
}

@Composable
private fun FinishedMatchState(
    state: MatchUiState.Finished,
    currentSortOrder: PlayerSortOrderBy,
    onSortOrderChange: (PlayerSortOrderBy) -> Unit,
    onExport: () -> Unit,
    onTitleChange: (String?) -> Unit
) {
    LaunchedEffect(state.match.id, state.match.teamName, state.match.opponent) {
        onTitleChange("${state.match.teamName} - ${state.match.opponent}")
    }

    DisposableEffect(Unit) {
        onDispose { onTitleChange(null) }
    }

    var selectedTab by remember { mutableIntStateOf(TAB_SUMMARY) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Match Time Card at top (always visible) with share button
        Box(
            modifier = Modifier.padding(
                horizontal = TFMSpacing.spacing04,
                vertical = TFMSpacing.spacing02
            )
        ) {
            MatchTimeCard(
                match = state.match,
                currentTime = state.currentTime,
                onExport = onExport,
            )
        }

        // Scrollable Tab Row with 4 tabs
        SecondaryScrollableTabRow (
            modifier = Modifier.fillMaxWidth(),
            selectedTabIndex = selectedTab,
            edgePadding = TFMSpacing.spacing04,
        ) {
            Tab(
                selected = selectedTab == TAB_SUMMARY,
                onClick = { selectedTab = TAB_SUMMARY },
                text = {
                    Text(
                        text = stringResource(R.string.summary_tab),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            )
            // Removed substitutions tab for finished matches
//            Tab(
//                selected = selectedTab == TAB_SUBSTITUTIONS,
//                onClick = { selectedTab = TAB_SUBSTITUTIONS },
//                text = {
//                    Text(
//                        text = stringResource(R.string.substitutions_tab),
//                        style = MaterialTheme.typography.titleMedium
//                    )
//                }
//            )
            Tab(
                selected = selectedTab == TAB_TIMELINE,
                onClick = { selectedTab = TAB_TIMELINE },
                text = {
                    Text(
                        text = stringResource(R.string.timeline_tab),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            )
            Tab(
                selected = selectedTab == TAB_STATISTICS,
                onClick = { selectedTab = TAB_STATISTICS },
                text = {
                    Text(
                        text = stringResource(R.string.statistics_tab),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            )
        }

        // Tab Content
        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedTab) {
                TAB_SUMMARY -> SummaryTabContent(
                    state = state,
                    currentSortOrder = currentSortOrder,
                    onSortOrderChange = onSortOrderChange,
                )
//                TAB_SUBSTITUTIONS -> SubstitutionsTabContent(
//                    substitutions = state.substitutions,
//                )
                TAB_TIMELINE -> TimelineTabContent(
                    timelineEvents = state.timelineEvents,
                )
                TAB_STATISTICS -> StatisticsTabContent(
                    scoreEvolution = state.scoreEvolution,
                    playerActivity = state.playerActivity,
                    teamName = state.match.teamName,
                    opponentName = state.match.opponent,
                )
            }
        }
    }
}

@Composable
private fun SummaryTabContent(
    state: MatchUiState.Finished,
    currentSortOrder: PlayerSortOrderBy,
    onSortOrderChange: (PlayerSortOrderBy) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = TFMSpacing.spacing04),
        contentPadding = PaddingValues(
            top = TFMSpacing.spacing03,
            bottom = TFMSpacing.spacing04
        ),
        verticalArrangement = Arrangement.spacedBy(TFMSpacing.spacing03),
    ) {
        item {
            PlayerSortOrder(
                availableSorts = PlayerSortOrderBy.entries.minus(PlayerSortOrderBy.BY_ACTIVE_FIRST),
                currentSortOrder = currentSortOrder,
                onSortOrderChange = onSortOrderChange,
            )
        }

        items(
            items = state.playerTimes.sortedBy(currentSortOrder, state.match),
            key = { it.player.id }
        ) { playerTimeItem ->
            PlayerItem(
                modifier = Modifier.animateItem(
                    fadeInSpec = spring(stiffness = Spring.StiffnessLow),
                    placementSpec = spring(),
                    fadeOutSpec = tween(durationMillis = 300)
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
private fun SubstitutionsTabContent(
    substitutions: List<SubstitutionItem>,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = TFMSpacing.spacing04),
        contentPadding = PaddingValues(
            top = TFMSpacing.spacing03,
            bottom = TFMSpacing.spacing04
        ),
        verticalArrangement = Arrangement.spacedBy(TFMSpacing.spacing03),
    ) {
        items(
            items = substitutions,
            key = { "${it.playerIn.id}_${it.playerOut.id}_${it.matchElapsedTimeMillis}" }
        ) { substitution ->
            SubstitutionCard(substitution = substitution)
        }
    }
}

@Composable
private fun TimelineTabContent(
    timelineEvents: List<TimelineEvent>,
) {
    // Timeline Events only (no chart)
    TimelineContent(
        events = timelineEvents,
        modifier = Modifier.fillMaxSize(),
    )
}

@Composable
private fun StatisticsTabContent(
    scoreEvolution: List<ScorePoint>,
    playerActivity: List<PlayerActivityInterval>,
    teamName: String,
    opponentName: String,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = TFMSpacing.spacing04),
        contentPadding = PaddingValues(
            top = TFMSpacing.spacing03,
            bottom = TFMSpacing.spacing04
        ),
        verticalArrangement = Arrangement.spacedBy(TFMSpacing.spacing04),
    ) {
        // Player Activity Chart with toggleable lines
        if (scoreEvolution.size > 1 || playerActivity.isNotEmpty()) {
            item {
                PlayerActivityChart(
                    scoreEvolution = scoreEvolution,
                    playerActivity = playerActivity,
                    teamName = teamName,
                    opponentName = opponentName,
                )
            }
        }
    }
}

// region Dialogs

@Composable
private fun InvalidSubstitutionAlertDialog(
    onDismiss: (dontShowAgain: Boolean) -> Unit,
) {
    var dontShowAgain by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { onDismiss(false) },
        title = {
            Text(
                stringResource(R.string.invalid_substitution_title),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column {
                Text(
                    stringResource(R.string.invalid_substitution_message),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.padding(TFMSpacing.spacing02))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = TFMSpacing.spacing02)
                ) {
                    Checkbox(
                        checked = dontShowAgain,
                        onCheckedChange = { dontShowAgain = it }
                    )
                    Spacer(modifier = Modifier.padding(TFMSpacing.spacing01))
                    Text(
                        stringResource(R.string.dont_show_again),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onDismiss(dontShowAgain) }
            ) {
                Text(stringResource(R.string.close))
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
                stringResource(R.string.stop_match_early_title),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Text(
                stringResource(R.string.stop_match_early_period_message),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text(stringResource(R.string.yes))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(stringResource(R.string.no))
            }
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
        confirmText = stringResource(R.string.yes),
        dismissText = stringResource(R.string.no),
        title = stringResource(if (isBreak) R.string.pause_match_early_title else R.string.stop_match_early_title),
        message = stringResource(if (isBreak) R.string.pause_match_early_message else R.string.stop_match_early_message),
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
                stringResource(R.string.select_goal_scorer_title),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            LazyColumn {
                items(players) { player ->
                    ScorerItem(
                        number = player.number.toString(),
                        name = "${player.firstName} ${player.lastName}",
                        onScorerSelected = { onGoal(player.id) }
                    )
                }

                // Own Goal option at the bottom
                item {
                    ScorerItem(
                        number = "-",
                        name = stringResource(R.string.own_goal_option),
                        onScorerSelected = { onGoal(null) }
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(stringResource(R.string.cancel))
            }
        },
        shape = MaterialTheme.shapes.medium,
    )
}

@Composable
private fun ScorerItem(number: String, name: String, onScorerSelected: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = TFMSpacing.spacing01),
        onClick =  onScorerSelected,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(TFMSpacing.spacing03),
            horizontalArrangement = Arrangement.spacedBy(TFMSpacing.spacing02),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
            )
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
                stringResource(R.string.add_opponent_goal_title),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Text(
                stringResource(R.string.add_opponent_goal_message),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text(stringResource(R.string.add))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(stringResource(R.string.cancel))
            }
        },
        shape = MaterialTheme.shapes.medium,
    )
}

// endregion

@Preview(showBackground = true)
@Composable
private fun OngoingMatchViewPreview() {
    TFMAppTheme {
        MatchDetailContent(
            state = MatchUiState.Success(
                match = Match(
                    id = 1,
                    teamName = "Loyola D",
                    opponent = "EFRO",
                    location = "FUNDOMA",
                    status = MatchStatus.IN_PROGRESS,
                    pauseCount = 0,
                    goals = 1,
                    captainId = 2L,
                    opponentGoals = 0,
                    periodType = PeriodType.HALF_TIME,
                    periods = listOf()
                ),
                playerTimes = (1..3).map {
                    PlayerTimeItem(
                        player = Player(
                            id = 1L,
                            firstName = "John",
                            lastName = "Doe",
                            number = 10,
                            positions = listOf(Position.Forward),
                            teamId = 1,
                            isCaptain = false
                        ),
                        timeMillis = it * 5 * 60 * 1000L,
                        isCaptain = it == 1,
                        isRunning = it % 2 == 0,
                        isPaused = false,
                        substitutionCount = 2
                    )
                },
                currentTime = System.currentTimeMillis(),
            ),
            selectedPlayerOut = 1L,
            currentSortOrder = PlayerSortOrderBy.BY_NUMBER,
            onSaveMatch = {},
            onPauseMatch = {},
            onResumeMatch = {},
            onStartTimeout = {},
            onEndTimeout = {},
            onPlayerClick = {},
            onDragDropSubstitute = { _, _ -> },
            onSortOrderChange = {},
            onAddGoal = {},
            onAddOpponentGoal = {},
            onBeginMatch = {}
        )
    }
}
