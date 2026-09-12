package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsEvent
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsParam
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker
import com.jesuslcorominas.teamflowmanager.domain.analytics.CrashReporter
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionMode
import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionPair
import com.jesuslcorominas.teamflowmanager.domain.usecase.EndTimeoutUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.FinishMatchUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetMatchByIdUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetTeamUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.MatchEventNotification
import com.jesuslcorominas.teamflowmanager.domain.usecase.NotifyPresidentMatchEventUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.PauseMatchUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.ResumeMatchUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.StartMatchTimerUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.StartPlayerTimersBatchUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.StartTimeoutUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SynchronizeTimeUseCase
import com.jesuslcorominas.teamflowmanager.viewmodel.utils.TimeTicker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Constructor is `internal`: its collaborators are internal to this module, and a public
 * constructor cannot expose them. Build it from outside with [createMatchViewModel].
 */
class MatchViewModel internal constructor(
    private val matchId: String,
    private val getMatchById: GetMatchByIdUseCase,
    private val finishMatch: FinishMatchUseCase,
    private val pauseMatch: PauseMatchUseCase,
    private val resumeMatchUseCase: ResumeMatchUseCase,
    private val startMatchTimerUseCase: StartMatchTimerUseCase,
    private val startTimeoutUseCase: StartTimeoutUseCase,
    private val endTimeoutUseCase: EndTimeoutUseCase,
    private val synchronizeTimeUseCase: SynchronizeTimeUseCase,
    private val startPlayerTimersBatchUseCase: StartPlayerTimersBatchUseCase,
    private val timeTicker: TimeTicker,
    private val analyticsTracker: AnalyticsTracker,
    private val crashReporter: CrashReporter,
    private val notifyPresidentMatchEvent: NotifyPresidentMatchEventUseCase,
    private val getTeamUseCase: GetTeamUseCase,
    private val reportExporter: MatchReportExporter,
    private val goalRecorder: MatchGoalRecorder,
    private val stateLoader: MatchStateLoader,
    private val substitutions: MatchSubstitutionCoordinator,
) : ViewModel() {
    private val teamFlow = getTeamUseCase().stateIn(viewModelScope, SharingStarted.Eagerly, null)
    private val notificationCoordinator = MatchNotificationCoordinator(notifyPresidentMatchEvent)

    private val _uiState = MutableStateFlow<MatchUiState>(MatchUiState.Loading)
    val uiState: StateFlow<MatchUiState> = _uiState.asStateFlow()

    @Suppress("ktlint:standard:property-naming")
    private val _currentTime = MutableStateFlow(0L)

    val selectedPlayerOut: StateFlow<String?> get() = substitutions.selectedPlayerOut

    val showInvalidSubstitutionAlert: StateFlow<Boolean> get() = substitutions.showInvalidSubstitutionAlert

    private val _showStopConfirmation = MutableStateFlow(false)
    val showStopConfirmation: StateFlow<Boolean> = _showStopConfirmation.asStateFlow()

    private val _showPauseConfirmation = MutableStateFlow<EndPeriodState?>(null)
    val showPauseConfirmation: StateFlow<EndPeriodState?> = _showPauseConfirmation.asStateFlow()

    private val _showGoalScorerDialog = MutableStateFlow(false)
    val showGoalScorerDialog: StateFlow<Boolean> = _showGoalScorerDialog.asStateFlow()

    private val _showOpponentGoalDialog = MutableStateFlow(false)
    val showOpponentGoalDialog: StateFlow<Boolean> = _showOpponentGoalDialog.asStateFlow()

    val exportState: StateFlow<ExportState> get() = reportExporter.state

    val isSubstitutionInProgress: StateFlow<Boolean> get() = substitutions.isSubstitutionInProgress

    /**
     * Seeded with [SubstitutionMode.SCHEDULED], the product default, so the screen never paints a
     * frame of the live layout before preferences have emitted.
     */
    val substitutionMode: StateFlow<SubstitutionMode> =
        substitutions
            .observeMode()
            .stateIn(viewModelScope, SharingStarted.Eagerly, SubstitutionMode.SCHEDULED)

    /** The squad call-up, which is who may be painted on a pending card. */
    private val squadPlayers: StateFlow<List<Player>> = stateLoader.squadPlayers(matchId, viewModelScope)

    /**
     * The scheduled substitutions, resolved to players so the screen can paint them directly.
     * Exposed in both modes: switching to live is a device-wide setting, not an instruction to
     * throw away what was already scheduled for this match.
     */
    val pendingSubstitutions: StateFlow<List<PendingSubstitutionItem>> =
        substitutions
            .pendingItems(squadPlayers)
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val pendingSubstitutionConflict: StateFlow<PendingSubstitutionConflict?> get() = substitutions.pendingSubstitutionConflict

    /**
     * Outcome of the last batch. Held rather than emitted once: the batch that runs on resume has
     * nobody watching, and losing its result to a recomposition would lose the only notice that
     * anything happened.
     */
    val lastSubstitutionResult: StateFlow<SubstitutionExecutionResult?> get() = substitutions.lastSubstitutionResult

    init {
        loadMatchData(matchId)
        observeTime()
    }

    fun beginMatch(matchId: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is MatchUiState.Success && !currentState.match.isStarted) {
                // Synchronize time with server before starting the match
                try {
                    synchronizeTimeUseCase()
                } catch (e: Exception) {
                    crashReporter.recordException(e)
                    crashReporter.log("Error synchronizing time before match start: ${e.message}")
                    // Continue with match start even if sync fails
                }

                val currentTime = _currentTime.value
                getMatchById(matchId).first()?.let {
                    startMatchTimerUseCase(matchId = it.id, currentTime)
                    // Start all player timers at once using batch operation
                    if (it.startingLineupIds.isNotEmpty()) {
                        startPlayerTimersBatchUseCase(it.id, it.startingLineupIds, currentTime)
                    }

                    notificationCoordinator.fireNotification(
                        scope = viewModelScope,
                        team = teamFlow.value,
                        matchId = it.id,
                    ) { MatchEventNotification.Start(it.teamName, it.opponent) }
                }
            }
        }
    }

    fun saveMatch() {
        viewModelScope.launch {
            (_uiState.value as? MatchUiState.Success)?.let { currentState ->
                if (!currentState.match.isLastPeriod()) {
                    _showStopConfirmation.value = true
                } else {
                    val currentPeriod =
                        currentState.match.periods
                            .firstOrNull { it.startTimeMillis > 0L && it.endTimeMillis == 0L }

                    if (currentPeriod != null) {
                        val elapsedTime = (_currentTime.value - currentPeriod.startTimeMillis).coerceAtLeast(0L)
                        val remainingTime = currentPeriod.periodDuration - elapsedTime

                        // If more than 1 minute remains in normal time, show confirmation dialog
                        // If in additional time (remainingTime <= 0), proceed without confirmation
                        if (remainingTime > 60000L) {
                            _showPauseConfirmation.value = EndPeriodState(false)
                            return@launch
                        }
                    }

                    confirmStopMatch()
                }
            }
        }
    }

    fun confirmStopMatch() {
        viewModelScope.launch {
            try {
                (_uiState.value as? MatchUiState.Success)?.let { currentState ->
                    crashReporter.log("Finishing match: ${currentState.match.id}")
                    finishMatch(currentState.match.id, _currentTime.value)

                    // A finished match takes no more substitutions: anything still scheduled is
                    // dead weight that would reappear if the screen were reopened.
                    substitutions.clearPending()

                    analyticsTracker.logEvent(
                        AnalyticsEvent.MATCH_FINISHED,
                        mapOf(
                            AnalyticsParam.MATCH_ID to currentState.match.id,
                            AnalyticsParam.DURATION_MINUTES to (_currentTime.value / 60000).toString(),
                        ),
                    )

                    val finishedMatch = currentState.match
                    notificationCoordinator.fireNotification(
                        scope = viewModelScope,
                        team = teamFlow.value,
                        matchId = finishedMatch.id,
                    ) {
                        MatchEventNotification.End(finishedMatch.teamName, finishedMatch.opponent, finishedMatch.goals, finishedMatch.opponentGoals)
                    }
                }

                _showPauseConfirmation.value = null
                _showStopConfirmation.value = false
            } catch (e: Exception) {
                crashReporter.recordException(e)
                crashReporter.log("Error finishing match: ${e.message}")
                throw e
            }
        }
    }

    fun dismissStopConfirmation() {
        _showStopConfirmation.value = false
    }

    fun pauseMatch() {
        viewModelScope.launch {
            try {
                (_uiState.value as? MatchUiState.Success)?.let { currentState ->
                    if (currentState.match.canPause()) {
                        // Calculate remaining time in current period
                        val currentPeriod =
                            currentState.match.periods
                                .firstOrNull { it.startTimeMillis > 0L && it.endTimeMillis == 0L }

                        if (currentPeriod != null) {
                            val elapsedTime = (_currentTime.value - currentPeriod.startTimeMillis).coerceAtLeast(0L)
                            val remainingTime = currentPeriod.periodDuration - elapsedTime

                            // If more than 1 minute remains in normal time, show confirmation dialog
                            // If in additional time (remainingTime <= 0), proceed without confirmation
                            if (remainingTime > 60000L) {
                                _showPauseConfirmation.value = EndPeriodState(true)
                                return@launch
                            }
                        }

                        // If no active period or less than 1 minute remains, proceed with pausing immediately
                        confirmPauseMatch()
                    }
                }
            } catch (e: Exception) {
                crashReporter.recordException(e)
                crashReporter.log("Error pausing match: ${e.message}")
                throw e
            }
        }
    }

    fun confirmPauseMatch() {
        viewModelScope.launch {
            try {
                (_uiState.value as? MatchUiState.Success)?.let { currentState ->
                    crashReporter.log("Pausing match: ${currentState.match.id}")
                    pauseMatch(currentState.match.id, _currentTime.value)

                    analyticsTracker.logEvent(
                        AnalyticsEvent.MATCH_PAUSED,
                        mapOf(
                            AnalyticsParam.MATCH_ID to currentState.match.id,
                            AnalyticsParam.DURATION_MINUTES to (_currentTime.value / 60000).toString(),
                        ),
                    )
                }

                _showPauseConfirmation.value = null
            } catch (e: Exception) {
                crashReporter.recordException(e)
                crashReporter.log("Error pausing match: ${e.message}")
                throw e
            }
        }
    }

    fun dismissPauseConfirmation() {
        _showPauseConfirmation.value = null
    }

    fun resumeMatch(matchId: String) {
        viewModelScope.launch {
            try {
                crashReporter.log("Resuming match: $matchId")

                // Synchronize time with server before resuming
                try {
                    synchronizeTimeUseCase()
                } catch (e: Exception) {
                    crashReporter.recordException(e)
                    crashReporter.log("Error synchronizing time before match resume: ${e.message}")
                    // Continue with match resume even if sync fails
                }

                getMatchById(matchId).first()?.let {
                    // Suspends until the match is running again AND the paused players are back
                    // to PLAYING, both under ResumeMatchUseCase's own operation id.
                    resumeMatchUseCase(it.id, _currentTime.value)

                    analyticsTracker.logEvent(
                        AnalyticsEvent.MATCH_RESUMED,
                        mapOf(
                            AnalyticsParam.MATCH_ID to matchId,
                        ),
                    )

                    // Only now. RegisterPlayerSubstitutionUseCase selects on PLAYING, so running
                    // the queue before the restore above would drop every pair on
                    // PLAYER_OUT_NOT_PLAYING and lose the coach's changes without a word.
                    runPendingSubstitutionsAfterResume()
                }
            } catch (e: Exception) {
                crashReporter.recordException(e)
                crashReporter.log("Error resuming match: ${e.message}")
                throw e
            }
        }
    }

    /**
     * Runs whatever was scheduled during the break. Must be called after [ResumeMatchUseCase] has
     * returned — see [resumeMatch].
     */
    private suspend fun runPendingSubstitutionsAfterResume() {
        try {
            val pending = substitutions.queuedPairs()
            runSubstitutions(pending, SubstitutionExecutionTrigger.RESUME)
        } catch (e: Exception) {
            crashReporter.recordException(e)
            crashReporter.log("Error running scheduled substitutions on resume: ${e.message}")
            // Deliberately not rethrown: the match is already resumed, and failing the resume
            // coroutine here would leave the screen worse off than losing a few scheduled changes.
        }
    }

    fun startTimeout() {
        viewModelScope.launch {
            try {
                (_uiState.value as? MatchUiState.Success)?.let { currentState ->
                    if (currentState.match.isInProgress) {
                        crashReporter.log("Starting timeout for match: ${currentState.match.id}")
                        startTimeoutUseCase(currentState.match.id, _currentTime.value)

                        analyticsTracker.logEvent(
                            AnalyticsEvent.BUTTON_CLICKED,
                            mapOf(
                                AnalyticsParam.BUTTON_NAME to "start_timeout",
                                AnalyticsParam.MATCH_ID to currentState.match.id,
                            ),
                        )
                    }
                }
            } catch (e: Exception) {
                crashReporter.recordException(e)
                crashReporter.log("Error starting timeout: ${e.message}")
                throw e
            }
        }
    }

    fun endTimeout() {
        viewModelScope.launch {
            try {
                (_uiState.value as? MatchUiState.Success)?.let { currentState ->
                    if (currentState.match.status == MatchStatus.TIMEOUT) {
                        crashReporter.log("Ending timeout for match: ${currentState.match.id}")
                        endTimeoutUseCase(currentState.match.id, _currentTime.value)

                        analyticsTracker.logEvent(
                            AnalyticsEvent.BUTTON_CLICKED,
                            mapOf(
                                AnalyticsParam.BUTTON_NAME to "end_timeout",
                                AnalyticsParam.MATCH_ID to currentState.match.id,
                            ),
                        )
                    }
                }
            } catch (e: Exception) {
                crashReporter.recordException(e)
                crashReporter.log("Error ending timeout: ${e.message}")
                throw e
            }
        }
    }

    fun selectPlayerOut(playerId: String) {
        val currentState = _uiState.value
        if (currentState is MatchUiState.Success) {
            substitutions.selectPlayerOut(playerId, currentState.playerTimes, substitutionMode.value)
        }
    }

    fun clearPlayerOutSelection() {
        substitutions.clearPlayerOutSelection()
    }

    fun dismissInvalidSubstitutionAlert(dontShowAgain: Boolean = false) {
        substitutions.dismissInvalidSubstitutionAlert(dontShowAgain)
    }

    fun substitutePlayer(playerInId: String) {
        val currentState = _uiState.value as? MatchUiState.Success ?: return
        substitutions.substitutePlayer(
            playerInId = playerInId,
            scope = viewModelScope,
            mode = substitutionMode.value,
            playerTimes = currentState.playerTimes,
            squadPlayers = squadPlayers.value,
            currentTimeMillis = _currentTime.value,
        )
    }

    /**
     * Performs a direct substitution without requiring the two-step selection process.
     * Used for drag-and-drop substitutions.
     */
    fun substitutePlayerDirect(
        playerInId: String,
        playerOutId: String,
    ) {
        val currentState = _uiState.value as? MatchUiState.Success ?: return
        substitutions.substitutePlayerDirect(
            playerInId = playerInId,
            playerOutId = playerOutId,
            scope = viewModelScope,
            mode = substitutionMode.value,
            playerTimes = currentState.playerTimes,
            currentTimeMillis = _currentTime.value,
        )
    }

    fun confirmPendingSubstitutionConflict() {
        substitutions.confirmPendingSubstitutionConflict()
    }

    fun dismissPendingSubstitutionConflict() {
        substitutions.dismissPendingSubstitutionConflict()
    }

    fun removePendingSubstitution(pair: SubstitutionPair) {
        substitutions.removePending(pair)
    }

    fun clearPendingSubstitutions() {
        substitutions.clearPending()
    }

    /** Runs a single card. Same path as "substitute all", with a batch of one. */
    fun executePendingSubstitution(pair: SubstitutionPair) {
        viewModelScope.launch { runSubstitutions(listOf(pair), SubstitutionExecutionTrigger.MANUAL) }
    }

    /** Runs every card under a single operation id. */
    fun executeAllPendingSubstitutions() {
        viewModelScope.launch {
            // Read the store, not the resolved list. squadPlayers fills in asynchronously, so
            // there is a window where cards exist but pendingSubstitutions is still empty — and
            // taking the resolved list there would turn the button into a silent no-op.
            runSubstitutions(substitutions.queuedPairs(), SubstitutionExecutionTrigger.MANUAL)
        }
    }

    fun consumeLastSubstitutionResult() {
        substitutions.consumeLastResult()
    }

    private suspend fun runSubstitutions(
        pairs: List<SubstitutionPair>,
        trigger: SubstitutionExecutionTrigger,
    ) = substitutions.run(pairs, trigger, squadPlayers.value, _currentTime.value)

    fun showGoalScorerDialog() {
        _showGoalScorerDialog.value = true
    }

    fun dismissGoalScorerDialog() {
        _showGoalScorerDialog.value = false
    }

    fun registerGoal(scorerId: String?) {
        viewModelScope.launch {
            (_uiState.value as? MatchUiState.Success)?.let { currentState ->
                goalRecorder.record(
                    scope = viewModelScope,
                    notifications = notificationCoordinator,
                    matchId = currentState.match.id,
                    scorerId = scorerId,
                    currentTimeMillis = _currentTime.value,
                    team = teamFlow.value,
                )
                _showGoalScorerDialog.value = false
            }
        }
    }

    fun showOpponentGoalDialog() {
        _showOpponentGoalDialog.value = true
    }

    fun dismissOpponentGoalDialog() {
        _showOpponentGoalDialog.value = false
    }

    fun registerOpponentGoal() {
        viewModelScope.launch {
            (_uiState.value as? MatchUiState.Success)?.let { currentState ->
                goalRecorder.recordOpponent(
                    scope = viewModelScope,
                    notifications = notificationCoordinator,
                    matchId = currentState.match.id,
                    currentTimeMillis = _currentTime.value,
                    team = teamFlow.value,
                )
                _showOpponentGoalDialog.value = false
            }
        }
    }

    private fun loadMatchData(matchId: String) {
        viewModelScope.launch {
            stateLoader.load(matchId, _currentTime) { state -> _uiState.value = state }
        }
    }

    private fun observeTime() {
        viewModelScope.launch {
            timeTicker.timeFlow.collect { now ->
                _currentTime.value = now
            }
        }
    }

    fun requestExport() {
        reportExporter.request(viewModelScope, matchId)
    }

    fun exportCompleted() {
        reportExporter.completed()
    }

    /**
     * Whether the player counts as being on the pitch for substitution purposes.
     *
     * In scheduled mode a paused player counts: during the break the coach must see who is on and
     * be able to queue changes. In live mode the predicate stays exactly as it was — widening it
     * there would let a substitution be attempted mid-break, only for the use case to discard it
     * on PLAYER_OUT_NOT_PLAYING with nothing queued to recover it.
     */
    private fun PlayerTimeItem.isOnPitch(mode: SubstitutionMode): Boolean = if (mode == SubstitutionMode.SCHEDULED) isRunning || isPaused else isRunning

    private fun SubstitutionPair.toPendingItem(players: List<Player>): PendingSubstitutionItem? {
        val out = players.find { it.id == playerOutId } ?: return null
        val incoming = players.find { it.id == playerInId } ?: return null
        return PendingSubstitutionItem(pair = this, playerOut = out, playerIn = incoming)
    }

    companion object
}
