package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsEvent
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsParam
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker
import com.jesuslcorominas.teamflowmanager.domain.analytics.CrashReporter
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeStatus
import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionBatchResult
import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionMode
import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionPair
import com.jesuslcorominas.teamflowmanager.domain.usecase.AddPendingSubstitutionUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.ClearPendingSubstitutionsUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.EndTimeoutUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.FinishMatchUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetAllPlayerTimesUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetMatchByIdUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetMatchSummaryUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetMatchTimelineUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetPendingSubstitutionConflictsUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetPlayersByTeamUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetTeamUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.MatchEventNotification
import com.jesuslcorominas.teamflowmanager.domain.usecase.NotifyPresidentMatchEventUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.ObservePendingSubstitutionsUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.ObserveSubstitutionModeUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.PauseMatchUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.RegisterGoalUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.RegisterPlayerSubstitutionUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.RemovePendingSubstitutionUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.ResumeMatchUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SetShouldShowInvalidSubstitutionAlertUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.ShouldShowInvalidSubstitutionAlertUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.StartMatchTimerUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.StartPlayerTimersBatchUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.StartTimeoutUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SynchronizeTimeUseCase
import com.jesuslcorominas.teamflowmanager.viewmodel.utils.TimeTicker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Constructor is `internal`: its collaborators are internal to this module, and a public
 * constructor cannot expose them. Build it from outside with [createMatchViewModel].
 */
class MatchViewModel internal constructor(
    private val matchId: String,
    private val getMatchById: GetMatchByIdUseCase,
    private val getAllPlayerTimesUseCase: GetAllPlayerTimesUseCase,
    private val getPlayersByTeamUseCase: GetPlayersByTeamUseCase,
    private val finishMatch: FinishMatchUseCase,
    private val pauseMatch: PauseMatchUseCase,
    private val resumeMatchUseCase: ResumeMatchUseCase,
    private val startMatchTimerUseCase: StartMatchTimerUseCase,
    private val registerPlayerSubstitutionUseCase: RegisterPlayerSubstitutionUseCase,
    private val getMatchSummaryUseCase: GetMatchSummaryUseCase,
    private val getMatchTimelineUseCase: GetMatchTimelineUseCase,
    private val registerGoal: RegisterGoalUseCase,
    private val startTimeoutUseCase: StartTimeoutUseCase,
    private val endTimeoutUseCase: EndTimeoutUseCase,
    private val synchronizeTimeUseCase: SynchronizeTimeUseCase,
    private val startPlayerTimersBatchUseCase: StartPlayerTimersBatchUseCase,
    private val shouldShowInvalidSubstitutionAlertUseCase: ShouldShowInvalidSubstitutionAlertUseCase,
    private val setShouldShowInvalidSubstitutionAlertUseCase: SetShouldShowInvalidSubstitutionAlertUseCase,
    private val timeTicker: TimeTicker,
    private val analyticsTracker: AnalyticsTracker,
    private val crashReporter: CrashReporter,
    private val notifyPresidentMatchEvent: NotifyPresidentMatchEventUseCase,
    private val getTeamUseCase: GetTeamUseCase,
    private val observeSubstitutionModeUseCase: ObserveSubstitutionModeUseCase,
    private val observePendingSubstitutionsUseCase: ObservePendingSubstitutionsUseCase,
    private val getPendingSubstitutionConflictsUseCase: GetPendingSubstitutionConflictsUseCase,
    private val addPendingSubstitutionUseCase: AddPendingSubstitutionUseCase,
    private val removePendingSubstitutionUseCase: RemovePendingSubstitutionUseCase,
    private val clearPendingSubstitutionsUseCase: ClearPendingSubstitutionsUseCase,
    private val reportExporter: MatchReportExporter,
) : ViewModel() {
    private val teamFlow = getTeamUseCase().stateIn(viewModelScope, SharingStarted.Eagerly, null)
    private val notificationCoordinator = MatchNotificationCoordinator(notifyPresidentMatchEvent)

    private val _uiState = MutableStateFlow<MatchUiState>(MatchUiState.Loading)
    val uiState: StateFlow<MatchUiState> = _uiState.asStateFlow()

    @Suppress("ktlint:standard:property-naming")
    private val _currentTime = MutableStateFlow(0L)

    private val _selectedPlayerOut = MutableStateFlow<String?>(null)
    val selectedPlayerOut: StateFlow<String?> = _selectedPlayerOut.asStateFlow()

    private val _showInvalidSubstitutionAlert = MutableStateFlow(false)
    val showInvalidSubstitutionAlert: StateFlow<Boolean> = _showInvalidSubstitutionAlert.asStateFlow()

    private val _showStopConfirmation = MutableStateFlow(false)
    val showStopConfirmation: StateFlow<Boolean> = _showStopConfirmation.asStateFlow()

    private val _showPauseConfirmation = MutableStateFlow<EndPeriodState?>(null)
    val showPauseConfirmation: StateFlow<EndPeriodState?> = _showPauseConfirmation.asStateFlow()

    private val _showGoalScorerDialog = MutableStateFlow(false)
    val showGoalScorerDialog: StateFlow<Boolean> = _showGoalScorerDialog.asStateFlow()

    private val _showOpponentGoalDialog = MutableStateFlow(false)
    val showOpponentGoalDialog: StateFlow<Boolean> = _showOpponentGoalDialog.asStateFlow()

    val exportState: StateFlow<ExportState> get() = reportExporter.state

    private val _isSubstitutionInProgress = MutableStateFlow(false)
    val isSubstitutionInProgress: StateFlow<Boolean> = _isSubstitutionInProgress.asStateFlow()

    /**
     * Seeded with [SubstitutionMode.SCHEDULED], the product default, so the screen never paints a
     * frame of the live layout before preferences have emitted.
     */
    val substitutionMode: StateFlow<SubstitutionMode> =
        observeSubstitutionModeUseCase()
            .stateIn(viewModelScope, SharingStarted.Eagerly, SubstitutionMode.SCHEDULED)

    /** The squad call-up, which is who may be painted on a pending card. */
    private val squadPlayers: StateFlow<List<Player>> =
        getMatchById(matchId)
            .flatMapLatest { match ->
                if (match == null) {
                    flowOf(emptyList())
                } else {
                    getPlayersByTeamUseCase(match.teamId)
                        .map { players -> players.filter { it.id in match.squadCallUpIds } }
                }
            }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /**
     * The scheduled substitutions, resolved to players so the screen can paint them directly.
     * Exposed in both modes: switching to live is a device-wide setting, not an instruction to
     * throw away what was already scheduled for this match.
     */
    val pendingSubstitutions: StateFlow<List<PendingSubstitutionItem>> =
        combine(
            observePendingSubstitutionsUseCase(matchId),
            squadPlayers,
        ) { pairs, players ->
            // A pair neither of whose players is in the call-up cannot be painted; dropping it
            // beats showing a blank card. Corrupt data, not a normal state.
            pairs.mapNotNull { it.toPendingItem(players) }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _pendingSubstitutionConflict = MutableStateFlow<PendingSubstitutionConflict?>(null)
    val pendingSubstitutionConflict: StateFlow<PendingSubstitutionConflict?> = _pendingSubstitutionConflict.asStateFlow()

    /**
     * Outcome of the last batch. Held rather than emitted once: the batch that runs on resume has
     * nobody watching, and losing its result to a recomposition would lose the only notice that
     * anything happened.
     */
    private val _lastSubstitutionResult = MutableStateFlow<SubstitutionExecutionResult?>(null)
    val lastSubstitutionResult: StateFlow<SubstitutionExecutionResult?> = _lastSubstitutionResult.asStateFlow()

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
                    clearPendingSubstitutionsUseCase(currentState.match.id)

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
     * A pair whose players are not in the call-up cannot be named, so it is left out of the
     * published result rather than surfaced half-empty. Unreachable in practice; logged because a
     * report that quietly holds fewer entries than the batch would be hard to make sense of.
     */
    private fun reportUnresolvedInResult(result: SubstitutionBatchResult) {
        val published = _lastSubstitutionResult.value ?: return
        val missing =
            (result.applied.size - published.applied.size) +
                (result.discarded.size - published.discarded.size)
        if (missing > 0) {
            crashReporter.log("$missing substitution(s) left out of the result: players not in the call-up")
        }
    }

    /**
     * Runs whatever was scheduled during the break. Must be called after [ResumeMatchUseCase] has
     * returned — see [resumeMatch].
     */
    private suspend fun runPendingSubstitutionsAfterResume() {
        try {
            val pending = observePendingSubstitutionsUseCase(matchId).first()
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
            val player = currentState.playerTimes.find { it.player.id == playerId }
            if (player?.isOnPitch(substitutionMode.value) == true) {
                _selectedPlayerOut.value = playerId
            } else {
                // Player is not currently playing, show alert if preferences allow
                if (shouldShowInvalidSubstitutionAlertUseCase()) {
                    _showInvalidSubstitutionAlert.value = true
                }
            }
        }
    }

    fun clearPlayerOutSelection() {
        _selectedPlayerOut.value = null
    }

    fun dismissInvalidSubstitutionAlert(dontShowAgain: Boolean = false) {
        _showInvalidSubstitutionAlert.value = false
        if (dontShowAgain) {
            setShouldShowInvalidSubstitutionAlertUseCase(false)
        }
    }

    fun substitutePlayer(playerInId: String) {
        val playerOut = _selectedPlayerOut.value ?: return

        // Validate that the incoming player is not already playing
        if (!isValidSubstitution(playerInId)) {
            // Clear the selection since the substitution is invalid
            _selectedPlayerOut.value = null
            return
        }

        val pair = SubstitutionPair(playerOutId = playerOut, playerInId = playerInId)
        if (substitutionMode.value == SubstitutionMode.SCHEDULED) {
            schedule(pair)
            return
        }

        performSubstitution(
            playerIn = playerInId,
            playerOut = playerOut,
            analyticsMessage = "Two-step substitution: $playerOut -> $playerInId",
            method = "two_step",
        )
    }

    /**
     * Queues [pair] instead of applying it. When it would displace pairs already scheduled, the
     * conflict is raised first and nothing is written until the coach confirms.
     */
    private fun schedule(pair: SubstitutionPair) {
        val conflicts = getPendingSubstitutionConflictsUseCase(matchId, pair)
        if (conflicts.isEmpty()) {
            addPendingSubstitutionUseCase(matchId, pair)
            _selectedPlayerOut.value = null
            return
        }

        val players = squadPlayers.value
        val requested = pair.toPendingItem(players)
        if (requested == null) {
            // Unreachable in practice: both players were picked from the call-up list. If it ever
            // happens, honour the coach's action rather than dropping it over a missing warning.
            crashReporter.log("Scheduling a substitution whose players are not in the call-up: $pair")
            addPendingSubstitutionUseCase(matchId, pair)
            _selectedPlayerOut.value = null
            return
        }

        _pendingSubstitutionConflict.value =
            PendingSubstitutionConflict(
                requested = requested,
                displaced = conflicts.mapNotNull { it.toPendingItem(players) },
            )
    }

    /** Schedules the pair that was warned about; the store discards the ones it displaces. */
    fun confirmPendingSubstitutionConflict() {
        val conflict = _pendingSubstitutionConflict.value ?: return
        addPendingSubstitutionUseCase(matchId, conflict.requested.pair)
        _pendingSubstitutionConflict.value = null
        _selectedPlayerOut.value = null
    }

    /** Backs out of the warning: nothing is scheduled and nothing already scheduled is lost. */
    fun dismissPendingSubstitutionConflict() {
        _pendingSubstitutionConflict.value = null
        _selectedPlayerOut.value = null
    }

    fun removePendingSubstitution(pair: SubstitutionPair) {
        removePendingSubstitutionUseCase(matchId, pair)
    }

    fun clearPendingSubstitutions() {
        clearPendingSubstitutionsUseCase(matchId)
    }

    /** Runs a single card. Same path as "substitute all", with a batch of one. */
    fun executePendingSubstitution(pair: SubstitutionPair) {
        viewModelScope.launch {
            runSubstitutions(listOf(pair), SubstitutionExecutionTrigger.MANUAL)
        }
    }

    /** Runs every card under a single operation id. */
    fun executeAllPendingSubstitutions() {
        viewModelScope.launch {
            // Read the store, not the resolved list. squadPlayers fills in asynchronously, so
            // there is a window where cards exist but pendingSubstitutions is still empty — and
            // taking the resolved list there would turn the button into a silent no-op.
            runSubstitutions(
                observePendingSubstitutionsUseCase(matchId).first(),
                SubstitutionExecutionTrigger.MANUAL,
            )
        }
    }

    fun consumeLastSubstitutionResult() {
        _lastSubstitutionResult.value = null
    }

    /**
     * The one and only execution path: a card's play button, "substitute all" and the automatic
     * run after a break all land here, so a batch of one behaves exactly like a batch of many.
     *
     * Applied pairs are always unscheduled. Discarded ones survive a
     * [SubstitutionExecutionTrigger.MANUAL] run — the coach is watching, [lastSubstitutionResult]
     * tells them why, and deleting their card for them would lose work with no way back. On
     * [SubstitutionExecutionTrigger.RESUME] they are dropped: a card that outlived a resume would
     * fire again by itself at the next break, with nobody watching again.
     *
     * Running this while the match is paused is allowed and expected — the coach can schedule
     * during the break, so they can press the button there too. Every pair is then discarded with
     * PLAYER_OUT_NOT_PLAYING, because paused players are not PLAYING, the cards stay, and the
     * result says so. Nothing is lost: they apply on their own when the match resumes.
     */
    private suspend fun runSubstitutions(
        pairs: List<SubstitutionPair>,
        trigger: SubstitutionExecutionTrigger,
    ) {
        if (pairs.isEmpty()) return

        try {
            _isSubstitutionInProgress.value = true
            crashReporter.log("Running ${pairs.size} scheduled substitution(s), trigger=$trigger")

            val result =
                registerPlayerSubstitutionUseCase(
                    matchId = matchId,
                    substitutions = pairs,
                    currentTimeMillis = _currentTime.value,
                )

            result.applied.forEach { removePendingSubstitutionUseCase(matchId, it) }
            if (trigger == SubstitutionExecutionTrigger.RESUME) {
                result.discarded.forEach { removePendingSubstitutionUseCase(matchId, it.pair) }
            }

            // Resolved before publishing: on a resume run the cards are gone by the time the
            // screen reads this, so ids alone would leave it with nobody to name.
            val squad = squadPlayers.value
            _lastSubstitutionResult.value =
                SubstitutionExecutionResult(
                    trigger = trigger,
                    applied = result.applied.mapNotNull { it.toPendingItem(squad) },
                    discarded =
                        result.discarded.mapNotNull { discarded ->
                            discarded.pair.toPendingItem(squad)?.let {
                                DiscardedSubstitutionItem(substitution = it, reason = discarded.reason)
                            }
                        },
                )
            reportUnresolvedInResult(result)

            val method = if (trigger == SubstitutionExecutionTrigger.RESUME) "scheduled_resume" else "scheduled_manual"
            result.applied.forEach { pair ->
                analyticsTracker.logEvent(
                    AnalyticsEvent.SUBSTITUTION_MADE,
                    mapOf(
                        AnalyticsParam.MATCH_ID to matchId,
                        AnalyticsParam.PLAYER_OUT to pair.playerOutId,
                        AnalyticsParam.PLAYER_IN to pair.playerInId,
                        AnalyticsParam.SUBSTITUTION_MINUTE to (_currentTime.value / 60000).toString(),
                        AnalyticsParam.SUBSTITUTION_METHOD to method,
                    ),
                )
            }
        } catch (e: Exception) {
            // Reported, then swallowed. Not rethrown like performSubstitution does: that would
            // take down the app over a failed batch, and here nothing has been lost — no card was
            // unscheduled, so the coach can simply press again. Going unreported was the actual
            // problem; this path was the only substitution route invisible to diagnostics.
            crashReporter.recordException(e)
            crashReporter.log("Error running scheduled substitutions (trigger=$trigger): ${e.message}")
        } finally {
            _isSubstitutionInProgress.value = false
        }
    }

    /**
     * Performs a direct substitution without requiring the two-step selection process.
     * Used for drag-and-drop substitutions.
     *
     * @param playerInId The ID of the player coming in (was inactive/not playing)
     * @param playerOutId The ID of the player going out (was active/playing)
     */
    fun substitutePlayerDirect(
        playerInId: String,
        playerOutId: String,
    ) {
        // Validate that the incoming player is not already playing
        if (!isValidSubstitution(playerInId)) {
            // No selection state to clear for direct substitution
            return
        }

        performSubstitution(
            playerIn = playerInId,
            playerOut = playerOutId,
            analyticsMessage = "Direct substitution: $playerOutId -> $playerInId (drag-drop)",
            method = "drag_drop",
        )
    }

    /**
     * Validates that a player can be substituted in.
     * A valid substitution requires the incoming player to NOT be currently playing.
     *
     * @param playerInId The ID of the player coming in
     * @return true if the substitution is valid, false otherwise (shows alert)
     */
    private fun isValidSubstitution(playerInId: String): Boolean {
        val currentState = _uiState.value
        if (currentState is MatchUiState.Success) {
            val playerIn = currentState.playerTimes.find { it.player.id == playerInId }
            if (playerIn?.isOnPitch(substitutionMode.value) == true) {
                // Player is already playing, show alert and don't proceed with substitution
                if (shouldShowInvalidSubstitutionAlertUseCase()) {
                    _showInvalidSubstitutionAlert.value = true
                }
                return false
            }
        }
        return true
    }

    private fun performSubstitution(
        playerIn: String,
        playerOut: String,
        analyticsMessage: String,
        method: String,
    ) {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                if (currentState is MatchUiState.Success) {
                    crashReporter.log(analyticsMessage)

                    // Show blocking loading indicator during substitution
                    _isSubstitutionInProgress.value = true

                    registerPlayerSubstitutionUseCase(
                        matchId = currentState.match.id,
                        substitutions = listOf(SubstitutionPair(playerOutId = playerOut, playerInId = playerIn)),
                        currentTimeMillis = _currentTime.value,
                    )

                    analyticsTracker.logEvent(
                        AnalyticsEvent.SUBSTITUTION_MADE,
                        mapOf(
                            AnalyticsParam.MATCH_ID to currentState.match.id,
                            AnalyticsParam.PLAYER_OUT to playerOut,
                            AnalyticsParam.PLAYER_IN to playerIn,
                            AnalyticsParam.SUBSTITUTION_MINUTE to (_currentTime.value / 60000).toString(),
                            AnalyticsParam.SUBSTITUTION_METHOD to method,
                        ),
                    )

                    // Clear any existing selection
                    _selectedPlayerOut.value = null

                    // Hide loading indicator after substitution completes
                    _isSubstitutionInProgress.value = false
                }
            } catch (e: Exception) {
                // Ensure loading indicator is hidden even on error
                _isSubstitutionInProgress.value = false
                crashReporter.recordException(e)
                crashReporter.log("Error in $method substitution: ${e.message}")
                throw e
            }
        }
    }

    fun showGoalScorerDialog() {
        _showGoalScorerDialog.value = true
    }

    fun dismissGoalScorerDialog() {
        _showGoalScorerDialog.value = false
    }

    fun registerGoal(scorerId: String?) {
        viewModelScope.launch {
            try {
                (_uiState.value as? MatchUiState.Success)?.let { currentState ->
                    crashReporter.log(
                        "Registering ${scorerId?.let { "goal for player: $it" } ?: "own goal (autogol by rival)"}",
                    )
                    registerGoal(
                        matchId = currentState.match.id,
                        scorerId = scorerId,
                        currentTimeMillis = _currentTime.value,
                        isOpponentGoal = false,
                        isOwnGoal = scorerId == null,
                    )

                    analyticsTracker.logEvent(
                        AnalyticsEvent.GOAL_SCORED,
                        mapOf(
                            AnalyticsParam.MATCH_ID to currentState.match.id,
                            AnalyticsParam.PLAYER_ID to (scorerId ?: ""),
                            AnalyticsParam.GOAL_MINUTE to (_currentTime.value / 60000).toString(),
                            AnalyticsParam.TEAM_TYPE to (scorerId?.let { "own" } ?: "own_goal"),
                        ).filter { it.value.isNotBlank() },
                    )

                    val goalMatchId = currentState.match.id
                    val snapshotTime = _currentTime.value
                    notificationCoordinator.fireNotification(
                        scope = viewModelScope,
                        team = teamFlow.value,
                        matchId = goalMatchId,
                    ) {
                        val updatedMatch = getMatchById(goalMatchId).first() ?: return@fireNotification null
                        val minuteOfPlay = notificationCoordinator.minuteOfPlay(updatedMatch, snapshotTime)
                        MatchEventNotification.Goal(
                            teamName = updatedMatch.teamName,
                            opponentName = updatedMatch.opponent,
                            teamGoals = updatedMatch.goals,
                            opponentGoals = updatedMatch.opponentGoals,
                            minuteOfPlay = minuteOfPlay,
                            isOpponentGoal = false,
                        )
                    }

                    _showGoalScorerDialog.value = false
                }
            } catch (e: Exception) {
                crashReporter.recordException(e)
                crashReporter.log("Error registering goal: ${e.message}")
                throw e
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
            try {
                (_uiState.value as? MatchUiState.Success)?.let { currentState ->
                    crashReporter.log("Registering opponent goal")
                    // For opponent goals, scorerId is null since opponent players are not tracked
                    registerGoal(
                        matchId = currentState.match.id,
                        scorerId = null,
                        currentTimeMillis = _currentTime.value,
                        isOpponentGoal = true,
                    )

                    analyticsTracker.logEvent(
                        AnalyticsEvent.OPPONENT_GOAL_SCORED,
                        mapOf(
                            AnalyticsParam.MATCH_ID to currentState.match.id,
                            AnalyticsParam.GOAL_MINUTE to (_currentTime.value / 60000).toString(),
                            AnalyticsParam.TEAM_TYPE to "opponent",
                        ),
                    )

                    val goalMatchId = currentState.match.id
                    val snapshotTime = _currentTime.value
                    notificationCoordinator.fireNotification(
                        scope = viewModelScope,
                        team = teamFlow.value,
                        matchId = goalMatchId,
                    ) {
                        val updatedMatch = getMatchById(goalMatchId).first() ?: return@fireNotification null
                        val minuteOfPlay = notificationCoordinator.minuteOfPlay(updatedMatch, snapshotTime)
                        MatchEventNotification.Goal(
                            teamName = updatedMatch.teamName,
                            opponentName = updatedMatch.opponent,
                            teamGoals = updatedMatch.goals,
                            opponentGoals = updatedMatch.opponentGoals,
                            minuteOfPlay = minuteOfPlay,
                            isOpponentGoal = true,
                        )
                    }

                    _showOpponentGoalDialog.value = false
                }
            } catch (e: Exception) {
                crashReporter.recordException(e)
                crashReporter.log("Error registering opponent goal: ${e.message}")
                throw e
            }
        }
    }

    private fun loadMatchData(matchId: String) {
        viewModelScope.launch {
            combine(
                getMatchById(matchId),
                getAllPlayerTimesUseCase(matchId),
                getMatchById(matchId).flatMapLatest { match ->
                    if (match == null) flowOf(emptyList()) else getPlayersByTeamUseCase(match.teamId)
                },
                _currentTime,
                getMatchTimelineUseCase(matchId),
            ) { match, playerTimes, players, currentTime, timeline ->
                when {
                    match == null -> MatchUiState.NoMatch
                    match.status == MatchStatus.FINISHED -> {
                        // Match is finished, load summary from history
                        null // Will be handled separately
                    }

                    else -> {
                        // Only include players that are in the squad call-up
                        val squadPlayers = players.filter { it.id in match.squadCallUpIds }

                        // Filter player times to show only those from completed operations
                        // This prevents UI flicker during multi-step atomic operations
                        val filteredPlayerTimes =
                            if (match.lastCompletedOperationId != null) {
                                playerTimes.filter { playerTime ->
                                    // Show players whose lastOperationId matches the match's last completed operation
                                    // OR has null operationId (backward compatibility for pre-operation-tracking data)
                                    // OR is ON_BENCH — bench players are not updated during substitutions involving
                                    // other players, so their lastOperationId may be stale; always show them.
                                    playerTime.lastOperationId == match.lastCompletedOperationId ||
                                        playerTime.lastOperationId == null ||
                                        playerTime.status == PlayerTimeStatus.ON_BENCH
                                }
                            } else {
                                // No operations completed yet, show all player times
                                playerTimes
                            }

                        val playerTimeItems =
                            squadPlayers.toPlayerItems(
                                filteredPlayerTimes,
                                currentTime,
                                match.captainId,
                            )

                        MatchUiState.Success(
                            match = match,
                            currentTime = _currentTime.value,
                            playerTimes = playerTimeItems,
                            timelineEvents = timeline?.events ?: emptyList(),
                        )
                    }
                }
            }.collect { state ->
                if (state != null) {
                    _uiState.value = state
                } else {
                    // Load finished match summary and timeline
                    getMatchById(matchId).collect { match ->
                        if (match != null && match.status == MatchStatus.FINISHED) {
                            combine(
                                getMatchSummaryUseCase(match.id),
                                getMatchTimelineUseCase(match.id),
                            ) { summary, timeline ->
                                if (summary != null) {
                                    MatchUiState.Finished(
                                        match = match,
                                        currentTime = _currentTime.value,
                                        playerTimes =
                                            summary.playerTimes.map { playerTimeSummary ->
                                                PlayerTimeItem(
                                                    player = playerTimeSummary.player,
                                                    timeMillis = playerTimeSummary.elapsedTimeMillis,
                                                    isRunning = false,
                                                    isPaused = false,
                                                    isCaptain = playerTimeSummary.player.id == summary.match.captainId,
                                                )
                                            },
                                        substitutions =
                                            summary.substitutions.map { sub ->
                                                SubstitutionItem(
                                                    playerOut = sub.playerOut,
                                                    playerIn = sub.playerIn,
                                                    matchElapsedTimeMillis = sub.matchElapsedTimeMillis,
                                                )
                                            },
                                        timelineEvents = timeline?.events ?: emptyList(),
                                        scoreEvolution = timeline?.scoreEvolution ?: emptyList(),
                                        playerActivity = timeline?.playerActivity ?: emptyList(),
                                    )
                                } else {
                                    null
                                }
                            }.collect { finishedState ->
                                if (finishedState != null) {
                                    _uiState.value = finishedState
                                }
                            }
                        }
                    }
                }
            }
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
