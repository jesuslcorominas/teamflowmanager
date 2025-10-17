package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.usecase.GetAllPlayerTimesUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetMatchSummaryUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetPlayersUseCase
import com.jesuslcorominas.teamflowmanager.usecase.FinishMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.PauseMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.RegisterPlayerSubstitutionUseCase
import com.jesuslcorominas.teamflowmanager.usecase.ResumeMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.StartMatchTimerUseCase
import com.jesuslcorominas.teamflowmanager.usecase.StartPlayerTimerUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

interface TimeTicker {
    val timeFlow: Flow<Long>
}

class RealTimeTicker : TimeTicker {
    override val timeFlow: Flow<Long> = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(1000)
        }
    }
}

class MatchViewModel(
    private val getMatchUseCase: GetMatchUseCase,
    private val getAllPlayerTimesUseCase: GetAllPlayerTimesUseCase,
    private val getPlayersUseCase: GetPlayersUseCase,
    private val saveMatchUseCase: FinishMatchUseCase,
    private val pauseMatchUseCase: PauseMatchUseCase,
    private val resumeMatchUseCase: ResumeMatchUseCase,
    private val startMatchTimerUseCase: com.jesuslcorominas.teamflowmanager.usecase.StartMatchTimerUseCase,
    private val startPlayerTimerUseCase: com.jesuslcorominas.teamflowmanager.usecase.StartPlayerTimerUseCase,
    private val registerPlayerSubstitutionUseCase: RegisterPlayerSubstitutionUseCase,
    private val getMatchSummaryUseCase: GetMatchSummaryUseCase,
    private val registerGoalUseCase: com.jesuslcorominas.teamflowmanager.usecase.RegisterGoalUseCase,
    private val getGoalsForMatchUseCase: com.jesuslcorominas.teamflowmanager.usecase.GetGoalsForMatchUseCase,
    private val preferencesRepository: PreferencesRepository,
    private val timeTicker: TimeTicker,
) : ViewModel() {
    private val _uiState = MutableStateFlow<MatchUiState>(MatchUiState.Loading)
    val uiState: StateFlow<MatchUiState> = _uiState.asStateFlow()

    private val _currentTime = MutableStateFlow(System.currentTimeMillis())

    private val _selectedPlayerOut = MutableStateFlow<Long?>(null)
    val selectedPlayerOut: StateFlow<Long?> = _selectedPlayerOut.asStateFlow()

    private val _showInvalidSubstitutionAlert = MutableStateFlow(false)
    val showInvalidSubstitutionAlert: StateFlow<Boolean> = _showInvalidSubstitutionAlert.asStateFlow()

    private val _showStopConfirmation = MutableStateFlow(false)
    val showStopConfirmation: StateFlow<Boolean> = _showStopConfirmation.asStateFlow()

    private val _currentSortOrder = MutableStateFlow(PlayerSortOrder.BY_ACTIVE_FIRST)
    val currentSortOrder: StateFlow<PlayerSortOrder> = _currentSortOrder.asStateFlow()

    private val _showGoalScorerDialog = MutableStateFlow(false)
    val showGoalScorerDialog: StateFlow<Boolean> = _showGoalScorerDialog.asStateFlow()

    init {
        loadMatchData()
        observeTime()
    }

    fun beginMatch() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is MatchUiState.Success && !currentState.isMatchStarted) {
                val currentTime = System.currentTimeMillis()
                // Start the match timer and player timers for starting lineup
                startMatchTimerUseCase(currentTime)
                
                // Get the current match to access starting lineup
                val match = getMatchUseCase().first()
                if (match != null) {
                    // Start timers for all players in the starting lineup
                    match.startingLineupIds.forEach { playerId ->
                        startPlayerTimerUseCase(playerId, currentTime)
                    }
                }
                
                _currentTime.value = currentTime
            }
        }
    }

    fun saveMatch() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is MatchUiState.Success) {
                // Check if we're in the last period
                if (!currentState.isLastPeriod) {
                    _showStopConfirmation.value = true
                } else {
                    confirmStopMatch()
                }
            } else {
                saveMatchUseCase()
            }
        }
    }

    fun confirmStopMatch() {
        viewModelScope.launch {
            _showStopConfirmation.value = false
            saveMatchUseCase()
        }
    }

    fun dismissStopConfirmation() {
        _showStopConfirmation.value = false
    }

    fun pauseMatch() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is MatchUiState.Success && currentState.canPause) {
                val currentTime = System.currentTimeMillis()
                pauseMatchUseCase(currentTime)
                // Update the current time immediately to avoid race conditions
                _currentTime.value = currentTime
            }
        }
    }

    fun resumeMatch() {
        viewModelScope.launch {
            val currentTime = System.currentTimeMillis()
            resumeMatchUseCase(currentTime)
            // Update the current time immediately to avoid race conditions
            _currentTime.value = currentTime
        }
    }

    fun setSortOrder(sortOrder: PlayerSortOrder) {
        _currentSortOrder.value = sortOrder
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
        viewModelScope.launch {
            val playerOutId = _selectedPlayerOut.value
            val currentState = _uiState.value
            if (playerOutId != null && currentState is MatchUiState.Success) {
                val currentTime = System.currentTimeMillis()
                registerPlayerSubstitutionUseCase(
                    matchId = currentState.matchId,
                    playerOutId = playerOutId,
                    playerInId = playerInId,
                    currentTimeMillis = currentTime,
                )
                _selectedPlayerOut.value = null
                _currentTime.value = currentTime
            }
        }
    }

    fun showGoalScorerDialog() {
        _showGoalScorerDialog.value = true
    }

    fun dismissGoalScorerDialog() {
        _showGoalScorerDialog.value = false
    }

    fun registerGoal(scorerId: Long) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is MatchUiState.Success) {
                val currentTime = System.currentTimeMillis()
                registerGoalUseCase(
                    matchId = currentState.matchId,
                    scorerId = scorerId,
                    currentTimeMillis = currentTime,
                )
                _showGoalScorerDialog.value = false
                _currentTime.value = currentTime
            }
        }
    }

    private fun loadMatchData() {
        viewModelScope.launch {
            combine(
                getMatchUseCase(),
                getAllPlayerTimesUseCase(),
                getPlayersUseCase(),
                _currentTime,
                _currentSortOrder,
            ) { match, playerTimes, players, currentTime, sortOrder ->
                if (match == null) {
                    MatchUiState.NoMatch
                } else if (match.status == MatchStatus.FINISHED) {
                    // Match is finished, load summary from history
                    null // Will be handled separately
                } else {
                    val matchTime = calculateCurrentTime(
                        match.elapsedTimeMillis,
                        match.isRunning,
                        match.lastStartTimeMillis,
                        currentTime,
                    )

                    // Get goals count for the match
                    val goalsCount = getGoalsForMatchUseCase(match.id).first().size

                    val playerTimeItems = players.map { player ->
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
                            isCaptain = player.id == match.captainId,
                        )
                    }

                    // Sort players based on current sort order
                    val sortedPlayers = when (sortOrder) {
                        PlayerSortOrder.BY_TIME_DESC -> playerTimeItems.sortedByDescending { it.timeMillis }
                        PlayerSortOrder.BY_TIME_ASC -> playerTimeItems.sortedBy { it.timeMillis }
                        PlayerSortOrder.BY_ACTIVE_FIRST -> playerTimeItems.sortedWith(
                            compareByDescending<PlayerTimeItem> { it.isRunning }
                                .thenByDescending { it.timeMillis }
                        )
                    }

                    MatchUiState.Success(
                        matchId = match.id,
                        matchTimeMillis = matchTime,
                        matchIsRunning = match.isRunning,
                        playerTimes = sortedPlayers,
                        numberOfPeriods = match.numberOfPeriods,
                        currentPeriod = match.currentPeriod,
                        pauseCount = match.pauseCount,
                        canPause = match.canPause(),
                        isLastPeriod = match.isLastPeriod(),
                        sortOrder = sortOrder,
                        isMatchStarted = match.elapsedTimeMillis > 0 || match.isRunning,
                        goalsCount = goalsCount,
                    )
                }
            }.collect { state ->
                if (state != null) {
                    _uiState.value = state
                } else {
                    // Load finished match summary
                    getMatchUseCase().collect { match ->
                        if (match != null && match.status == MatchStatus.FINISHED) {
                            getMatchSummaryUseCase(match.id).collect { summary ->
                                if (summary != null) {
                                    _uiState.value = MatchUiState.Finished(
                                        matchId = summary.match.id,
                                        matchTimeMillis = summary.match.elapsedTimeMillis,
                                        opponent = summary.match.opponent ?: "",
                                        location = summary.match.location ?: "",
                                        playerTimes = summary.playerTimes.map { playerTimeSummary ->
                                            PlayerTimeItem(
                                                player = playerTimeSummary.player,
                                                timeMillis = playerTimeSummary.elapsedTimeMillis,
                                                isRunning = false,
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
                                    )
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
}

data class PlayerTimeItem(
    val player: Player,
    val timeMillis: Long,
    val isRunning: Boolean,
    val substitutionCount: Int = 0,
    val isCaptain: Boolean = false,
)

enum class PlayerSortOrder {
    BY_TIME_DESC,      // Most time played first
    BY_TIME_ASC,       // Least time played first
    BY_ACTIVE_FIRST,   // Active players first, then by time
}

sealed class MatchUiState {
    data object Loading : MatchUiState()
    data object NoMatch : MatchUiState()
    data class Success(
        val matchId: Long,
        val matchTimeMillis: Long,
        val matchIsRunning: Boolean,
        val playerTimes: List<PlayerTimeItem>,
        val numberOfPeriods: Int = 2,
        val currentPeriod: Int = 1,
        val pauseCount: Int = 0,
        val canPause: Boolean = true,
        val isLastPeriod: Boolean = false,
        val sortOrder: PlayerSortOrder = PlayerSortOrder.BY_ACTIVE_FIRST,
        val isMatchStarted: Boolean = false,
        val goalsCount: Int = 0,
    ) : MatchUiState()
    data class Finished(
        val matchId: Long,
        val matchTimeMillis: Long,
        val opponent: String,
        val location: String,
        val playerTimes: List<PlayerTimeItem>,
        val substitutions: List<SubstitutionItem>,
    ) : MatchUiState()
}

data class SubstitutionItem(
    val playerOut: Player,
    val playerIn: Player,
    val matchElapsedTimeMillis: Long,
)
