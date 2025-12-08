package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsEvent
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsParam
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker
import com.jesuslcorominas.teamflowmanager.domain.analytics.CrashReporter
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerActivityInterval
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTime
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeStatus
import com.jesuslcorominas.teamflowmanager.domain.model.ScorePoint
import com.jesuslcorominas.teamflowmanager.domain.model.TimelineEvent
import com.jesuslcorominas.teamflowmanager.domain.navigation.Route
import com.jesuslcorominas.teamflowmanager.usecase.EndTimeoutUseCase
import com.jesuslcorominas.teamflowmanager.usecase.ExportMatchReportToPdfUseCase
import com.jesuslcorominas.teamflowmanager.usecase.FinishMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetAllPlayerTimesUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetMatchByIdUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetMatchReportDataUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetMatchSummaryUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetMatchTimelineUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetPlayersUseCase
import com.jesuslcorominas.teamflowmanager.usecase.PauseMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.RegisterGoalUseCase
import com.jesuslcorominas.teamflowmanager.usecase.RegisterPlayerSubstitutionUseCase
import com.jesuslcorominas.teamflowmanager.usecase.ResumeMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.StartMatchTimerUseCase
import com.jesuslcorominas.teamflowmanager.usecase.StartPlayerTimerUseCase
import com.jesuslcorominas.teamflowmanager.usecase.StartTimeoutUseCase
import com.jesuslcorominas.teamflowmanager.usecase.SynchronizeTimeUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PreferencesRepository
import com.jesuslcorominas.teamflowmanager.viewmodel.utils.TimeTicker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class MatchViewModel(
    private val getMatchById: GetMatchByIdUseCase,
    private val getAllPlayerTimesUseCase: GetAllPlayerTimesUseCase,
    private val getPlayersUseCase: GetPlayersUseCase,
    private val finishMatch: FinishMatchUseCase,
    private val pauseMatch: PauseMatchUseCase,
    private val resumeMatchUseCase: ResumeMatchUseCase,
    private val startMatchTimerUseCase: StartMatchTimerUseCase,
    private val startPlayerTimerUseCase: StartPlayerTimerUseCase,
    private val registerPlayerSubstitutionUseCase: RegisterPlayerSubstitutionUseCase,
    private val getMatchSummaryUseCase: GetMatchSummaryUseCase,
    private val getMatchTimelineUseCase: GetMatchTimelineUseCase,
    private val registerGoal: RegisterGoalUseCase,
    private val startTimeoutUseCase: StartTimeoutUseCase,
    private val endTimeoutUseCase: EndTimeoutUseCase,
    private val getMatchReportData: GetMatchReportDataUseCase,
    private val exportMatchReportToPdf: ExportMatchReportToPdfUseCase,
    private val synchronizeTimeUseCase: SynchronizeTimeUseCase,
    private val preferencesRepository: PreferencesRepository, // TODO extract to usecases
    private val timeTicker: TimeTicker,
    private val analyticsTracker: AnalyticsTracker,
    private val crashReporter: CrashReporter,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _uiState = MutableStateFlow<MatchUiState>(MatchUiState.Loading)
    val uiState: StateFlow<MatchUiState> = _uiState.asStateFlow()

    private val _currentTime = MutableStateFlow(0L)

    private val _selectedPlayerOut = MutableStateFlow<Long?>(null)
    val selectedPlayerOut: StateFlow<Long?> = _selectedPlayerOut.asStateFlow()

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

    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()

    private val matchId: Long

    init {
        matchId =
            savedStateHandle[Route.Match.ARG_MATCH_ID]
                ?: throw IllegalArgumentException("matchId is required")

        loadMatchData(matchId)
        observeTime()
    }

    fun beginMatch(matchId: Long) {
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
                    it.startingLineupIds.forEach { playerId ->
                        startPlayerTimerUseCase(playerId, currentTime)
                    }
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
                    val currentPeriod = currentState.match.periods
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

                    analyticsTracker.logEvent(
                        AnalyticsEvent.MATCH_FINISHED,
                        mapOf(
                            AnalyticsParam.MATCH_ID to currentState.match.id.toString(),
                            AnalyticsParam.DURATION_MINUTES to (_currentTime.value / 60000).toString(),
                        ),
                    )
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
                        val currentPeriod = currentState.match.periods
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
                            AnalyticsParam.MATCH_ID to currentState.match.id.toString(),
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

    fun resumeMatch(matchId: Long) {
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
                    resumeMatchUseCase(it.id, _currentTime.value)

                    analyticsTracker.logEvent(
                        AnalyticsEvent.MATCH_RESUMED,
                        mapOf(
                            AnalyticsParam.MATCH_ID to matchId.toString(),
                        ),
                    )
                }
            } catch (e: Exception) {
                crashReporter.recordException(e)
                crashReporter.log("Error resuming match: ${e.message}")
                throw e
            }
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
                                AnalyticsParam.MATCH_ID to currentState.match.id.toString(),
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
                                AnalyticsParam.MATCH_ID to currentState.match.id.toString(),
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

    fun selectPlayerOut(playerId: Long) {
        val currentState = _uiState.value
        if (currentState is MatchUiState.Success) {
            val player = currentState.playerTimes.find { it.player.id == playerId }
            if (player?.isRunning == true) {
                _selectedPlayerOut.value = playerId
            } else {
                // Player is not currently playing, show alert if preferences allow
                if (preferencesRepository.shouldShowInvalidSubstitutionAlert()) {
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
            preferencesRepository.setShouldShowInvalidSubstitutionAlert(false)
        }
    }

    fun substitutePlayer(playerInId: Long) {
        val playerOut = _selectedPlayerOut.value ?: return

        performSubstitution(
            playerIn = playerInId,
            playerOut = playerOut,
            analyticsMessage = "Two-step substitution: $playerOut -> $playerInId",
            method = "two_step"
        )
    }

    /**
     * Performs a direct substitution without requiring the two-step selection process.
     * Used for drag-and-drop substitutions.
     * Used for drag-and-drop substitutions.
     *
     * @param playerInId The ID of the player coming in (was inactive/not playing)
     * @param playerOutId The ID of the player going out (was active/playing)
     */
    fun substitutePlayerDirect(playerInId: Long, playerOutId: Long) {
        performSubstitution(
            playerIn = playerInId,
            playerOut = playerOutId,
            analyticsMessage = "Direct substitution: $playerOutId -> $playerInId (drag-drop)",
            method = "drag_drop"
        )
    }

    private fun performSubstitution(playerIn: Long, playerOut: Long, analyticsMessage: String, method: String) {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                if (currentState is MatchUiState.Success) {
                    crashReporter.log(analyticsMessage)

                    registerPlayerSubstitutionUseCase(
                        matchId = currentState.match.id,
                        playerOutId = playerOut,
                        playerInId = playerIn,
                        currentTimeMillis = _currentTime.value,
                    )

                    analyticsTracker.logEvent(
                        AnalyticsEvent.SUBSTITUTION_MADE,
                        mapOf(
                            AnalyticsParam.MATCH_ID to currentState.match.id.toString(),
                            AnalyticsParam.PLAYER_OUT to playerOut.toString(),
                            AnalyticsParam.PLAYER_IN to playerIn.toString(),
                            AnalyticsParam.SUBSTITUTION_MINUTE to (_currentTime.value / 60000).toString(),
                            AnalyticsParam.SUBSTITUTION_METHOD to method,
                        ),
                    )

                    // Clear any existing selection
                    _selectedPlayerOut.value = null
                }
            } catch (e: Exception) {
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

    fun registerGoal(scorerId: Long?) {
        viewModelScope.launch {
            try {
                (_uiState.value as? MatchUiState.Success)?.let { currentState ->
                    crashReporter.log("Registering ${scorerId?.let { "goal for player: $it" } ?: "own goal (autogol by rival)"}")
                    registerGoal(
                        matchId = currentState.match.id,
                        scorerId = scorerId,
                        currentTimeMillis = _currentTime.value,
                        isOpponentGoal = false,
                        isOwnGoal = scorerId == null
                    )

                    analyticsTracker.logEvent(
                        AnalyticsEvent.GOAL_SCORED,
                        mapOf(
                            AnalyticsParam.MATCH_ID to currentState.match.id.toString(),
                            AnalyticsParam.PLAYER_ID to (scorerId?.toString() ?: ""),
                            AnalyticsParam.GOAL_MINUTE to (_currentTime.value / 60000).toString(),
                            AnalyticsParam.TEAM_TYPE to (scorerId?.let { "own" } ?: "own_goal"),
                        ).filter { it.value.isNotBlank() },
                    )

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
                            AnalyticsParam.MATCH_ID to currentState.match.id.toString(),
                            AnalyticsParam.GOAL_MINUTE to (_currentTime.value / 60000).toString(),
                            AnalyticsParam.TEAM_TYPE to "opponent",
                        ),
                    )

                    _showOpponentGoalDialog.value = false
                }
            } catch (e: Exception) {
                crashReporter.recordException(e)
                crashReporter.log("Error registering opponent goal: ${e.message}")
                throw e
            }
        }
    }

    private fun loadMatchData(matchId: Long) {
        viewModelScope.launch {
            combine(
                getMatchById(matchId),
                getAllPlayerTimesUseCase(),
                getPlayersUseCase(),
                _currentTime,
            ) { match, playerTimes, players, currentTime ->
                when {
                    match == null -> MatchUiState.NoMatch
                    match.status == MatchStatus.FINISHED -> {
                        // Match is finished, load summary from history
                        null // Will be handled separately
                    }

                    else -> {
                        // Only include players that are in the squad call-up
                        val squadPlayers = players.filter { it.id in match.squadCallUpIds }
                        val playerTimeItems = squadPlayers.toPlayerItems(playerTimes, currentTime, match.captainId)

                        MatchUiState.Success(
                            match = match,
                            currentTime = _currentTime.value,
                            playerTimes = playerTimeItems,
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
                                        playerTimes = summary.playerTimes.map { playerTimeSummary ->
                                            PlayerTimeItem(
                                                player = playerTimeSummary.player,
                                                timeMillis = playerTimeSummary.elapsedTimeMillis,
                                                isRunning = false,
                                                isPaused = false,
                                                isCaptain = playerTimeSummary.player.id == summary.match.captainId,
                                            )
                                        },
                                        substitutions = summary.substitutions.map { sub ->
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

    private fun List<Player>.toPlayerItems(
        playerTimes: List<PlayerTime>,
        currentTime: Long,
        captainId: Long
    ): List<PlayerTimeItem> =
        this.map { player ->
            val playerTime = playerTimes.find { it.playerId == player.id }
            val displayTime = if (playerTime != null) {
                calculateCurrentTime(
                    playerTime.elapsedTimeMillis,
                    playerTime.isRunning,
                    playerTime.lastStartTimeMillis,
                    currentTime,
                )
            } else {
                0L
            }
            PlayerTimeItem(
                player = player,
                timeMillis = displayTime,
                isRunning = playerTime?.isRunning ?: false,
                isPaused = playerTime?.status == PlayerTimeStatus.PAUSED,
                isCaptain = player.id == captainId,
            )
        }

    private fun observeTime() {
        viewModelScope.launch {
            timeTicker.timeFlow.collect { now ->
                _currentTime.value = now
            }
        }
    }

    private fun calculateCurrentTime(
        elapsedTimeMillis: Long,
        isRunning: Boolean,
        lastStartTimeMillis: Long?,
        currentTimeMillis: Long,
    ): Long {
        return if (isRunning && lastStartTimeMillis != null) {
            elapsedTimeMillis + (currentTimeMillis - lastStartTimeMillis)
        } else {
            elapsedTimeMillis
        }
    }

    fun requestExport() {
        viewModelScope.launch {
            try {
                crashReporter.log("Requesting match report export for match: $matchId")
                _exportState.value = ExportState.Loading
                val matchReportData = getMatchReportData(matchId).firstOrNull()

                if (matchReportData != null) {
                    val uri = exportMatchReportToPdf(matchReportData)
                    _exportState.value = if (uri != null) {
                        analyticsTracker.logEvent(
                            AnalyticsEvent.MATCH_REPORT_EXPORTED,
                            mapOf(
                                AnalyticsParam.MATCH_ID to matchId.toString(),
                                AnalyticsParam.EXPORT_TYPE to "pdf",
                            ),
                        )
                        ExportState.Ready(uri)
                    } else {
                        ExportState.Error
                    }
                } else {
                    _exportState.value = ExportState.Error
                }
            } catch (e: Exception) {
                crashReporter.recordException(e)
                crashReporter.log("Error exporting match report: ${e.message}")
                _exportState.value = ExportState.Error
            }
        }
    }

    fun exportCompleted() {
        _exportState.value = ExportState.Idle
    }
}

data class PlayerTimeItem(
    val player: Player,
    val timeMillis: Long,
    val isRunning: Boolean,
    val isPaused: Boolean,
    val substitutionCount: Int = 0,
    val isCaptain: Boolean = false,
)


sealed class MatchUiState {
    data object Loading : MatchUiState()
    data object NoMatch : MatchUiState()
    data class Success(
        val match: Match,
        val currentTime: Long,
        val playerTimes: List<PlayerTimeItem>,
    ) : MatchUiState()

    data class Finished(
        val match: Match,
        val currentTime: Long,
        val playerTimes: List<PlayerTimeItem>,
        val substitutions: List<SubstitutionItem>,
        val timelineEvents: List<TimelineEvent> = emptyList(),
        val scoreEvolution: List<ScorePoint> = emptyList(),
        val playerActivity: List<PlayerActivityInterval> = emptyList(),
    ) : MatchUiState()
}

data class SubstitutionItem(
    val playerOut: Player,
    val playerIn: Player,
    val matchElapsedTimeMillis: Long,
)

data class EndPeriodState(
    val isBreak: Boolean,
)
