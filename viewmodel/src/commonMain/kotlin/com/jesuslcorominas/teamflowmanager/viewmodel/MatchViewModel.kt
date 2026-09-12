package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker
import com.jesuslcorominas.teamflowmanager.domain.analytics.CrashReporter
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionMode
import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionPair
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetTeamUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.MatchEventNotification
import com.jesuslcorominas.teamflowmanager.domain.usecase.NotifyPresidentMatchEventUseCase
import com.jesuslcorominas.teamflowmanager.viewmodel.utils.TimeTicker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Constructor is `internal`: its collaborators are internal to this module, and a public
 * constructor cannot expose them. Build it from outside with [createMatchViewModel].
 */
class MatchViewModel internal constructor(
    private val matchId: String,
    private val timeTicker: TimeTicker,
    private val analyticsTracker: AnalyticsTracker,
    private val crashReporter: CrashReporter,
    private val notifyPresidentMatchEvent: NotifyPresidentMatchEventUseCase,
    private val getTeamUseCase: GetTeamUseCase,
    private val reportExporter: MatchReportExporter,
    private val goalRecorder: MatchGoalRecorder,
    private val stateLoader: MatchStateLoader,
    private val substitutions: MatchSubstitutionCoordinator,
    private val clock: MatchClockController,
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

    val playerAlreadyScheduledAlert: StateFlow<PlayerAlreadyScheduledAlert?> get() = substitutions.playerAlreadyScheduledAlert

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
                clock.begin(matchId, _currentTime.value)?.let { started ->
                    notificationCoordinator.fireNotification(
                        scope = viewModelScope,
                        team = teamFlow.value,
                        matchId = started.id,
                    ) { MatchEventNotification.Start(started.teamName, started.opponent) }
                }
            }
        }
    }

    fun saveMatch() {
        viewModelScope.launch {
            (_uiState.value as? MatchUiState.Success)?.let { currentState ->
                if (!currentState.match.isLastPeriod()) {
                    _showStopConfirmation.value = true
                } else if (clock.needsEndPeriodConfirmation(currentState.match, _currentTime.value)) {
                    _showPauseConfirmation.value = EndPeriodState(false)
                } else {
                    confirmStopMatch()
                }
            }
        }
    }

    fun confirmStopMatch() {
        viewModelScope.launch {
            try {
                (_uiState.value as? MatchUiState.Success)?.let { currentState ->
                    clock.finish(currentState.match.id, _currentTime.value)

                    // A finished match takes no more substitutions: anything still scheduled is
                    // dead weight that would reappear if the screen were reopened.
                    substitutions.clearPending()

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
                        if (clock.needsEndPeriodConfirmation(currentState.match, _currentTime.value)) {
                            _showPauseConfirmation.value = EndPeriodState(true)
                            return@launch
                        }

                        // If no active period or less than 1 minute remains, pause immediately
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
                    clock.pause(currentState.match.id, _currentTime.value)
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
                // The order here is the point of the whole scheduled-substitutions feature.
                // clock.resume() suspends until the match is running again AND the paused players
                // are back to PLAYING; RegisterPlayerSubstitutionUseCase selects on PLAYING, so
                // running the queue any earlier would drop every pair on PLAYER_OUT_NOT_PLAYING
                // and lose the coach's changes without a word.
                if (clock.resume(matchId, _currentTime.value)) {
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
     * Runs whatever was scheduled during the break. Must be called after the match has been
     * resumed — see [resumeMatch].
     */
    private suspend fun runPendingSubstitutionsAfterResume() {
        try {
            runSubstitutions(substitutions.queuedPairs(), SubstitutionExecutionTrigger.RESUME)
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
                        clock.startTimeout(currentState.match.id, _currentTime.value)
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
                        clock.endTimeout(currentState.match.id, _currentTime.value)
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
            substitutions.selectPlayerOut(
                playerId = playerId,
                playerTimes = currentState.playerTimes,
                mode = substitutionMode.value,
                pendingPairs = pendingSubstitutions.value.map { it.pair },
            )
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
            pendingPairs = pendingSubstitutions.value.map { it.pair },
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

    fun confirmPlayerAlreadyScheduled() {
        substitutions.confirmPlayerAlreadyScheduled()
    }

    fun dismissPlayerAlreadyScheduled() {
        substitutions.dismissPlayerAlreadyScheduled()
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
