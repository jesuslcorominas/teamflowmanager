package com.jesuslcorominas.teamflowmanager.ui.matches

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.domain.analytics.ScreenName
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerActivityInterval
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.domain.model.ScorePoint
import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionMode
import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionPair
import com.jesuslcorominas.teamflowmanager.domain.model.TimelineEvent
import com.jesuslcorominas.teamflowmanager.ui.analytics.TrackScreenView
import com.jesuslcorominas.teamflowmanager.ui.components.AppIconButton
import com.jesuslcorominas.teamflowmanager.ui.components.Loading
import com.jesuslcorominas.teamflowmanager.ui.components.card.MatchTimeCard
import com.jesuslcorominas.teamflowmanager.ui.components.dialog.AppAlertDialog
import com.jesuslcorominas.teamflowmanager.ui.components.form.PlayerSortOrderBy
import com.jesuslcorominas.teamflowmanager.ui.components.form.PlayerSortOrderSelector
import com.jesuslcorominas.teamflowmanager.ui.main.LocalContentBottomPadding
import com.jesuslcorominas.teamflowmanager.ui.matches.components.PendingSubstitutionCard
import com.jesuslcorominas.teamflowmanager.ui.matches.components.PlayerActivityChart
import com.jesuslcorominas.teamflowmanager.ui.matches.components.TimelineContent
import com.jesuslcorominas.teamflowmanager.ui.players.components.PlayerItem
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import com.jesuslcorominas.teamflowmanager.viewmodel.DiscardedSubstitutionItem
import com.jesuslcorominas.teamflowmanager.viewmodel.ExportState
import com.jesuslcorominas.teamflowmanager.viewmodel.MatchUiState
import com.jesuslcorominas.teamflowmanager.viewmodel.MatchViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.PendingSubstitutionItem
import com.jesuslcorominas.teamflowmanager.viewmodel.PlayerTimeItem
import com.jesuslcorominas.teamflowmanager.viewmodel.SubstitutionExecutionResult
import com.jesuslcorominas.teamflowmanager.viewmodel.SubstitutionExecutionTrigger
import org.jetbrains.compose.resources.painterResource
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
import teamflowmanager.shared_ui.generated.resources.ic_goal
import teamflowmanager.shared_ui.generated.resources.ic_substitution_arrows
import teamflowmanager.shared_ui.generated.resources.ic_timeout
import teamflowmanager.shared_ui.generated.resources.ic_whistle
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
import teamflowmanager.shared_ui.generated.resources.pending_substitutions_clear_all
import teamflowmanager.shared_ui.generated.resources.pending_substitutions_clear_all_message
import teamflowmanager.shared_ui.generated.resources.pending_substitutions_clear_all_title
import teamflowmanager.shared_ui.generated.resources.pending_substitutions_collapse
import teamflowmanager.shared_ui.generated.resources.pending_substitutions_conflict_confirm
import teamflowmanager.shared_ui.generated.resources.pending_substitutions_conflict_title
import teamflowmanager.shared_ui.generated.resources.pending_substitutions_execute_all
import teamflowmanager.shared_ui.generated.resources.pending_substitutions_expand
import teamflowmanager.shared_ui.generated.resources.pending_substitutions_paused_hint
import teamflowmanager.shared_ui.generated.resources.pending_substitutions_player_already_scheduled
import teamflowmanager.shared_ui.generated.resources.pending_substitutions_title
import teamflowmanager.shared_ui.generated.resources.resume_match_button
import teamflowmanager.shared_ui.generated.resources.scorers_dialog_title
import teamflowmanager.shared_ui.generated.resources.scorers_tab
import teamflowmanager.shared_ui.generated.resources.select_goal_scorer_title
import teamflowmanager.shared_ui.generated.resources.statistics_tab
import teamflowmanager.shared_ui.generated.resources.stop_match_early_message
import teamflowmanager.shared_ui.generated.resources.stop_match_early_period_message
import teamflowmanager.shared_ui.generated.resources.stop_match_early_title
import teamflowmanager.shared_ui.generated.resources.substitution_discard_subject_either
import teamflowmanager.shared_ui.generated.resources.substitution_result_applied_header
import teamflowmanager.shared_ui.generated.resources.substitution_result_discarded_header
import teamflowmanager.shared_ui.generated.resources.substitution_result_manual_title
import teamflowmanager.shared_ui.generated.resources.substitution_result_pair
import teamflowmanager.shared_ui.generated.resources.substitution_result_pair_discarded
import teamflowmanager.shared_ui.generated.resources.substitution_result_resume_intro
import teamflowmanager.shared_ui.generated.resources.substitution_result_resume_title
import teamflowmanager.shared_ui.generated.resources.substitution_result_snackbar_applied
import teamflowmanager.shared_ui.generated.resources.summary_tab
import teamflowmanager.shared_ui.generated.resources.timeline_tab
import teamflowmanager.shared_ui.generated.resources.timeout_button
import teamflowmanager.shared_ui.generated.resources.unknown_scorer_label
import teamflowmanager.shared_ui.generated.resources.yes

private const val TAB_SCORERS = 0
private const val TAB_SUMMARY = 1
private const val TAB_TIMELINE = 2
private const val TAB_STATISTICS = 3

@Composable
fun MatchScreen(
    matchId: String,
    readOnly: Boolean = false,
    onTitleChange: (String?) -> Unit = {},
    onExportReady: (uri: String) -> Unit = {},
    viewModel: MatchViewModel = koinViewModel(key = matchId, parameters = { parametersOf(matchId) }),
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
    val substitutionMode by viewModel.substitutionMode.collectAsState()
    val pendingSubstitutions by viewModel.pendingSubstitutions.collectAsState()
    val playerAlreadyScheduledAlert by viewModel.playerAlreadyScheduledAlert.collectAsState()

    var currentSortOrder: PlayerSortOrderBy by remember { mutableStateOf(PlayerSortOrderBy.BY_ACTIVE_FIRST) }

    val snackbarHostState = remember { SnackbarHostState() }
    var substitutionResultDialog: SubstitutionExecutionResult? by remember { mutableStateOf(null) }
    var appliedSnackbarCount by remember { mutableIntStateOf(0) }
    var appliedSnackbarSeq by remember { mutableIntStateOf(0) }

    // One effect that cleans up first and observes afterwards, on purpose: two separate effects
    // would race, and the cleanup losing that race would swallow a fresh result.
    //
    // The cleanup exists because on iOS the ViewModel is cached in the root ViewModelStore and is
    // never cleared on back navigation, so a conflict or a result left behind would pop up again
    // on re-entry. Same LaunchedEffect(Unit) pattern the project already uses for this.
    LaunchedEffect(Unit) {
        viewModel.dismissPlayerAlreadyScheduled()
        viewModel.consumeLastSubstitutionResult()
        viewModel.lastSubstitutionResult.collect { result ->
            if (result == null) return@collect
            // Consumed before it is painted: the dialog is driven by screen-local state, so
            // holding it in the ViewModel would only leave it there to reappear later.
            viewModel.consumeLastSubstitutionResult()
            when (presentationFor(result)) {
                SubstitutionResultPresentation.DIALOG -> substitutionResultDialog = result
                SubstitutionResultPresentation.SNACKBAR -> {
                    appliedSnackbarCount = result.applied.size
                    appliedSnackbarSeq++
                }
            }
        }
    }

    val appliedSnackbarMessage =
        stringResource(Res.string.substitution_result_snackbar_applied, appliedSnackbarCount)
    // Keyed on a counter, not on the message: two identical batches in a row would otherwise be
    // one string and the second would never be shown.
    LaunchedEffect(appliedSnackbarSeq) {
        if (appliedSnackbarSeq > 0) {
            snackbarHostState.showSnackbar(appliedSnackbarMessage)
        }
    }

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
                        readOnly = readOnly,
                        selectedPlayerOut = selectedPlayerOut,
                        currentSortOrder = currentSortOrder,
                        substitutionMode = substitutionMode,
                        pendingSubstitutions = pendingSubstitutions,
                        onExecutePendingSubstitution = { viewModel.executePendingSubstitution(it) },
                        onExecuteAllPendingSubstitutions = { viewModel.executeAllPendingSubstitutions() },
                        onRemovePendingSubstitution = { viewModel.removePendingSubstitution(it) },
                        onClearPendingSubstitutions = { viewModel.clearPendingSubstitutions() },
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

        playerAlreadyScheduledAlert?.let {
            AppAlertDialog(
                title = stringResource(Res.string.pending_substitutions_conflict_title),
                message = stringResource(Res.string.pending_substitutions_player_already_scheduled),
                confirmText = stringResource(Res.string.pending_substitutions_conflict_confirm),
                dismissText = stringResource(Res.string.cancel),
                onConfirm = { viewModel.confirmPlayerAlreadyScheduled() },
                onDismiss = { viewModel.dismissPlayerAlreadyScheduled() },
            )
        }

        substitutionResultDialog?.let { result ->
            SubstitutionResultDialog(
                result = result,
                onDismiss = { substitutionResultDialog = null },
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = LocalContentBottomPadding.current),
        )

        if (isSubstitutionInProgress) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                        .pointerInput(Unit) { detectTapGestures { } },
                contentAlignment = Alignment.Center,
            ) {
                SubstitutionProgressIndicator()
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
    readOnly: Boolean,
    selectedPlayerOut: String?,
    currentSortOrder: PlayerSortOrderBy,
    substitutionMode: SubstitutionMode,
    pendingSubstitutions: List<PendingSubstitutionItem>,
    onExecutePendingSubstitution: (SubstitutionPair) -> Unit,
    onExecuteAllPendingSubstitutions: () -> Unit,
    onRemovePendingSubstitution: (SubstitutionPair) -> Unit,
    onClearPendingSubstitutions: () -> Unit,
    onSaveMatch: () -> Unit,
    onPauseMatch: () -> Unit,
    onResumeMatch: () -> Unit,
    onStartTimeout: () -> Unit,
    onEndTimeout: () -> Unit,
    onPlayerClick: (String) -> Unit,
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
            readOnly = readOnly,
            selectedPlayerOut = selectedPlayerOut,
            currentSortOrder = currentSortOrder,
            substitutionMode = substitutionMode,
            pendingSubstitutions = pendingSubstitutions,
            onExecutePendingSubstitution = onExecutePendingSubstitution,
            onExecuteAllPendingSubstitutions = onExecuteAllPendingSubstitutions,
            onRemovePendingSubstitution = onRemovePendingSubstitution,
            onClearPendingSubstitutions = onClearPendingSubstitutions,
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
    readOnly: Boolean,
    selectedPlayerOut: String?,
    currentSortOrder: PlayerSortOrderBy,
    substitutionMode: SubstitutionMode,
    pendingSubstitutions: List<PendingSubstitutionItem>,
    onExecutePendingSubstitution: (SubstitutionPair) -> Unit,
    onExecuteAllPendingSubstitutions: () -> Unit,
    onRemovePendingSubstitution: (SubstitutionPair) -> Unit,
    onClearPendingSubstitutions: () -> Unit,
    onSaveMatch: () -> Unit,
    onPauseMatch: () -> Unit,
    onResumeMatch: () -> Unit,
    onStartTimeout: () -> Unit,
    onEndTimeout: () -> Unit,
    onPlayerClick: (String) -> Unit,
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

        val pendingCards = pendingCardsToShow(readOnly, pendingSubstitutions)

        if (shouldShowPendingSubstitutionsSection(readOnly, substitutionMode, pendingCards)) {
            PendingSubstitutionsSection(
                items = pendingCards,
                match = state.match,
                onExecute = onExecutePendingSubstitution,
                onExecuteAll = onExecuteAllPendingSubstitutions,
                onRemove = onRemovePendingSubstitution,
                onClearAll = onClearPendingSubstitutions,
            )
        }

        // Below the queue and directly above the list it sorts. It is a control *of* the squad
        // list, so it belongs against it; with the queue in between, it read as if it sorted the
        // scheduled changes.
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
                val isPlaying = isOnPitchForDisplay(substitutionMode, state.match, playerTimeItem)
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
                        if (canSelectPlayerForSubstitution(substitutionMode, state.match, readOnly)) {
                            { onPlayerClick(playerTimeItem.player.id) }
                        } else {
                            null
                        },
                )
            }
        }

        if (!readOnly) {
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
            modifier = Modifier.fillMaxWidth().padding(bottom = LocalContentBottomPadding.current),
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
        Button(
            onClick = onBeginMatch,
            modifier = Modifier.fillMaxWidth().padding(bottom = LocalContentBottomPadding.current),
        ) {
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
        painter = painterResource(if (isTimeout) Res.drawable.ic_whistle else Res.drawable.ic_timeout),
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
        painter = painterResource(Res.drawable.ic_goal),
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
                        teamName = state.match.teamName,
                        opponentName = state.match.opponent,
                    )
            }
        }
    }
}

internal data class ScorerEntry(val name: String, val count: Int)

internal data class ScorerAggregation(
    val scorers: List<ScorerEntry>,
    val ownGoalCount: Int,
    val unknownScorerCount: Int,
)

internal fun aggregateScorers(events: List<TimelineEvent>): ScorerAggregation {
    val scorerMap = mutableMapOf<String, ScorerEntry>()
    var ownGoalCount = 0
    var unknownScorerCount = 0
    events.filterIsInstance<TimelineEvent.GoalScored>()
        .filter { !it.isOpponentGoal }
        .forEach { goal ->
            if (goal.isOwnGoal) {
                ownGoalCount++
            } else {
                val player = goal.scorer
                if (player == null) {
                    unknownScorerCount++
                } else {
                    val name = "${player.firstName} ${player.lastName}"
                    val current = scorerMap[player.id]
                    scorerMap[player.id] = ScorerEntry(name, (current?.count ?: 0) + 1)
                }
            }
        }
    val scorers = scorerMap.values.sortedByDescending { it.count }
    return ScorerAggregation(scorers, ownGoalCount, unknownScorerCount)
}

@Composable
private fun ScorersTabContent(events: List<TimelineEvent>) {
    val (scorers, ownGoalCount, unknownScorerCount) = remember(events) { aggregateScorers(events) }
    val noGoals = scorers.isEmpty() && ownGoalCount == 0 && unknownScorerCount == 0

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
            if (unknownScorerCount > 0) {
                item(key = "unknown_scorer") {
                    ScorerRow(name = stringResource(Res.string.unknown_scorer_label), count = unknownScorerCount)
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
                painter = painterResource(Res.drawable.ic_goal),
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
    val (scorers, ownGoalCount, unknownScorerCount) = remember(events) { aggregateScorers(events) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(Res.string.scorers_dialog_title),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            if (scorers.isEmpty() && ownGoalCount == 0 && unknownScorerCount == 0) {
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
                    if (unknownScorerCount > 0) {
                        item(key = "unknown_scorer") {
                            ScorerItem(
                                number = unknownScorerCount.toString(),
                                name = stringResource(Res.string.unknown_scorer_label),
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
    teamName: String,
    opponentName: String,
) {
    if (scoreEvolution.isEmpty() && playerActivity.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(Res.string.no_match_message),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding =
                PaddingValues(
                    horizontal = TFMSpacing.spacing04,
                    vertical = TFMSpacing.spacing03,
                ),
            verticalArrangement = Arrangement.spacedBy(TFMSpacing.spacing03),
        ) {
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
    onGoal: (String?) -> Unit,
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

/**
 * The queue of scheduled changes, above the squad list so that tapping two players and seeing the
 * count move is one glance, not a trip to another surface.
 *
 * One card, collapsed by default, and that is the whole point of it. Cards the height of a player's
 * row are readable but expensive, and with four or five queued they pushed the squad list off the
 * screen — the list being the thing a coach actually watches during a match. Closed, the section
 * costs a single row, and the queue is still countable, runnable and clearable from that row alone.
 *
 * The tap target is the header row rather than the whole card. Collapsed those are the same thing,
 * which is what "tap anywhere to open it" means in practice; expanded, the extra area is the queued
 * cards, and each of those carries its own play and delete buttons. Folding the section away under
 * someone reaching for one of those buttons would be a small betrayal, so the cards are not part of
 * the switch. The two header buttons sit inside the clickable row and swallow their own taps.
 *
 * Whether this is painted at all is [shouldShowPendingSubstitutionsSection]'s decision, taken by
 * the caller; with an empty queue there is a header here and no cards.
 */
@Composable
private fun PendingSubstitutionsSection(
    items: List<PendingSubstitutionItem>,
    match: Match,
    onExecute: (SubstitutionPair) -> Unit,
    onExecuteAll: () -> Unit,
    onRemove: (SubstitutionPair) -> Unit,
    onClearAll: () -> Unit,
) {
    var showClearAllConfirmation by remember { mutableStateOf(false) }

    // Closed to begin with. The section opening itself the moment anything is queued would hand the
    // squad list's space straight back to the cards and leave the chevron as a chore to be repeated
    // after every substitution — which is the complaint this whole screen is being reworked for.
    // Nothing is hidden by starting closed: the counter grows and settles to say the tap landed.
    var expanded by rememberSaveable { mutableStateOf(false) }

    // What the section actually is, as opposed to what the switch was last left at. An empty queue
    // has nothing to show, so it stays shut however the coach left it — see
    // [canExpandPendingSubstitutions].
    val canExpand = canExpandPendingSubstitutions(items)
    val isExpanded = expanded && canExpand

    if (showClearAllConfirmation) {
        AppAlertDialog(
            title = stringResource(Res.string.pending_substitutions_clear_all_title),
            message = stringResource(Res.string.pending_substitutions_clear_all_message, items.size),
            confirmText = stringResource(Res.string.yes),
            dismissText = stringResource(Res.string.no),
            isDestructive = true,
            onConfirm = {
                showClearAllConfirmation = false
                onClearAll()
            },
            onDismiss = { showClearAllConfirmation = false },
        )
    }

    // The card wraps the header row, and only while the section is closed.
    //
    // Closed, the section IS that row, so a card around it is a card around the whole thing: the
    // surface and its shadow say "this opens", and set it apart from the match card above and the
    // squad list below. Open, the same surface became a card wrapped around cards — the queued
    // changes are cards in their own right — and two frames around one piece of content read as a
    // mistake. Painting it transparent was not enough: a Surface casts its shadow whatever colour
    // it is filled with, and on the device the outline of a box nobody could see was still there.
    //
    // What must NOT move is the [AnimatedVisibility] below. Putting the whole section inside the
    // branch instead of just the header placed that call at two different points in the tree,
    // Compose stopped recognising it across the switch, and the open/close animation disappeared —
    // the list snapped in and out. Same call site in every state: the container around the header
    // may change, the list may not.
    //
    // Deliberately not AppCard when it is closed either: AppCard rings every card in the app with a
    // 1dp outline, and here that line ran the full width of the screen, reading as a rule drawn
    // across it rather than as the edge of a thing.
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = TFMSpacing.spacing03)) {
        val header: @Composable () -> Unit = {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .then(
                            if (canExpand) Modifier.clickable { expanded = !expanded } else Modifier,
                        )
                        .padding(
                            start = TFMSpacing.spacing04,
                            end = TFMSpacing.spacing02,
                            top = TFMSpacing.spacing02,
                            bottom = TFMSpacing.spacing02,
                        ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PendingSubstitutionsCounter(pairs = items.map { it.pair })

                Spacer(modifier = Modifier.weight(1f))

                val executeAllEnabled = canExecuteAllPendingSubstitutions(match, items)
                val clearAllEnabled = canClearAllPendingSubstitutions(items)

                // FastForward and DeleteSweep rather than PlayArrow and Delete: the cards below use
                // those two for the single change they belong to, and the same glyph meaning "this
                // one" in one place and "all of them" in another is how a coach empties a queue by
                // accident. The doubled and the swept variants say "all" on sight.
                AppIconButton(
                    modifier = Modifier.size(44.dp),
                    internalModifier = Modifier.size(26.dp),
                    imageVector = Icons.Filled.FastForward,
                    contentDescription = stringResource(Res.string.pending_substitutions_execute_all),
                    enabled = executeAllEnabled,
                    onClick = onExecuteAll,
                )

                AppIconButton(
                    modifier = Modifier.size(44.dp),
                    internalModifier = Modifier.size(26.dp),
                    imageVector = Icons.Filled.DeleteSweep,
                    contentDescription = stringResource(Res.string.pending_substitutions_clear_all),
                    enabled = clearAllEnabled,
                    tint = if (clearAllEnabled) MaterialTheme.colorScheme.error else null,
                    onClick = { showClearAllConfirmation = true },
                )

                // An indicator, not a control — the whole header row is the control. It stays
                // because a row that opens on touch and says so nowhere is a row nobody touches,
                // and it goes when there is nothing to open, because pointing at an empty drawer is
                // worse than not pointing at all.
                if (canExpand) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription =
                            stringResource(
                                if (isExpanded) {
                                    Res.string.pending_substitutions_collapse
                                } else {
                                    Res.string.pending_substitutions_expand
                                },
                            ),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        if (isExpanded) {
            header()
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            ) {
                header()
            }
        }

        // A greyed-out button with no explanation reads as a bug; say why, and say what will happen
        // instead — the queue runs on its own when the match is resumed. Only while the pause is
        // what is actually blocking it: see shouldShowPausedHint.
        if (shouldShowPausedHint(match, items)) {
            Text(
                text = stringResource(Res.string.pending_substitutions_paused_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier =
                    Modifier.padding(
                        start = TFMSpacing.spacing04,
                        end = TFMSpacing.spacing04,
                        top = TFMSpacing.spacing02,
                    ),
            )
        }

        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(max = 260.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(top = TFMSpacing.spacing02),
                verticalArrangement = Arrangement.spacedBy(TFMSpacing.spacing02),
            ) {
                items.forEach { item ->
                    PendingSubstitutionCard(
                        item = item,
                        executeEnabled = canExecutePendingSubstitutions(match),
                        onExecute = { onExecute(item.pair) },
                        onRemove = { onRemove(item.pair) },
                    )
                }
            }
        }
    }
}

/**
 * The queue's size, ringed by the app icon's swap arrows, which turn a full circle whenever a
 * change is added.
 *
 * The arrows stand in for the words "Cambios programados" rather than sitting beside them. Beside
 * them the count appeared twice — once inside the ring and once in the label's "(2)" — and the row
 * had to carry a title, two buttons and a chevron on a 411dp phone. The mark says what the section
 * is without spelling it, which is the whole reason an app has an icon.
 *
 * The spin is the receipt: the card that was just queued may be behind a collapsed section or below
 * the fold, so this is the one thing a coach can always see move. Only on the way up — deleting a
 * change is not something to celebrate. See [shouldPulseCounter].
 *
 * The mark is centred in its own square viewport, so it turns on its axis rather than swinging
 * around an off-centre point; see ic_substitution_arrows.xml.
 *
 * With the words gone the row has no text left to read aloud, so the count carries the full label
 * as its content description.
 */
@Composable
private fun PendingSubstitutionsCounter(
    pairs: List<SubstitutionPair>,
    modifier: Modifier = Modifier,
) {
    val turns = remember { Animatable(0f) }
    var previousPairs by remember { mutableStateOf(pairs) }

    LaunchedEffect(pairs) {
        val pulse = shouldPulseCounter(previousPairs, pairs)
        previousPairs = pairs
        if (pulse) {
            turns.snapTo(0f)
            turns.animateTo(1f, animationSpec = tween(durationMillis = 500))
        }
    }

    val label = stringResource(Res.string.pending_substitutions_title, pairs.size)

    Box(
        modifier = modifier.semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        Image(
            modifier =
                Modifier
                    .size(48.dp)
                    .graphicsLayer { rotationZ = turns.value * 360f },
            painter = painterResource(Res.drawable.ic_substitution_arrows),
            contentDescription = null,
        )
        Text(
            text = pairs.size.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

/**
 * The app's swap arrows, turning, while a batch is being written.
 *
 * The same mark the queue's counter carries, spinning the same way, instead of a generic circular
 * indicator: what is happening is substitutions being made, and this says so. A plain spinner could
 * equally have meant loading the match, saving a goal, or anything else.
 */
@Composable
private fun SubstitutionProgressIndicator() {
    val transition = rememberInfiniteTransition()
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 900, easing = LinearEasing)),
    )

    Image(
        modifier = Modifier.size(72.dp).graphicsLayer { rotationZ = angle },
        painter = painterResource(Res.drawable.ic_substitution_arrows),
        contentDescription = null,
    )
}

/**
 * What a batch actually did.
 *
 * A run triggered on resume gets its own wording, not a variant of the manual one: it happened
 * while the coach was not looking, and its discarded cards are deleted from the store before this
 * is ever painted, so this dialog is the only record that will remain of the team having changed.
 */
@Composable
private fun SubstitutionResultDialog(
    result: SubstitutionExecutionResult,
    onDismiss: () -> Unit,
) {
    val isResume = result.trigger == SubstitutionExecutionTrigger.RESUME

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text =
                    stringResource(
                        if (isResume) {
                            Res.string.substitution_result_resume_title
                        } else {
                            Res.string.substitution_result_manual_title
                        },
                    ),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (isResume) {
                    Text(
                        text = stringResource(Res.string.substitution_result_resume_intro),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(modifier = Modifier.padding(TFMSpacing.spacing01))
                }

                if (result.applied.isNotEmpty()) {
                    Text(
                        text = stringResource(Res.string.substitution_result_applied_header, result.applied.size),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    result.applied.forEach { applied ->
                        Text(
                            text = substitutionPairText(applied),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }

                if (result.discarded.isNotEmpty()) {
                    Spacer(modifier = Modifier.padding(TFMSpacing.spacing01))
                    Text(
                        text = stringResource(Res.string.substitution_result_discarded_header, result.discarded.size),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error,
                    )
                    result.discarded.forEach { discarded ->
                        Text(
                            text =
                                stringResource(
                                    Res.string.substitution_result_pair_discarded,
                                    discarded.substitution.playerOut.number,
                                    "${discarded.substitution.playerOut.firstName} ${discarded.substitution.playerOut.lastName}",
                                    discarded.substitution.playerIn.number,
                                    "${discarded.substitution.playerIn.firstName} ${discarded.substitution.playerIn.lastName}",
                                    discardReasonText(discarded),
                                ),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(text = stringResource(Res.string.close)) }
        },
        shape = MaterialTheme.shapes.medium,
    )
}

/**
 * The reason a pair was dropped, with the player it is actually about named in it.
 *
 * Without the name the fragment attaches itself to whoever is nearest in the line, and
 * "7 Juan -> 11 Pedro: ya estaba jugando" reads as an accusation against Juan when it is about
 * Pedro. Naming the wrong player is worse than jargon: jargon confuses, this misleads.
 */
@Composable
private fun discardReasonText(discarded: DiscardedSubstitutionItem): String {
    val substitution = discarded.substitution
    val subject =
        when (discardReasonSubject(discarded.reason)) {
            DiscardReasonSubject.PLAYER_OUT ->
                "${substitution.playerOut.firstName} ${substitution.playerOut.lastName}"
            DiscardReasonSubject.PLAYER_IN ->
                "${substitution.playerIn.firstName} ${substitution.playerIn.lastName}"
            DiscardReasonSubject.EITHER -> stringResource(Res.string.substitution_discard_subject_either)
        }
    return stringResource(discardReasonRes(discarded.reason), subject)
}

@Composable
private fun substitutionPairText(item: PendingSubstitutionItem): String =
    stringResource(
        Res.string.substitution_result_pair,
        item.playerOut.number,
        "${item.playerOut.firstName} ${item.playerOut.lastName}",
        item.playerIn.number,
        "${item.playerIn.firstName} ${item.playerIn.lastName}",
    )
