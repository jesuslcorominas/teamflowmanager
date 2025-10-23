package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.usecase.FinishMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetAllPlayerTimesUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetMatchByIdUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetMatchSummaryUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetPlayersUseCase
import com.jesuslcorominas.teamflowmanager.usecase.PauseMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.RegisterGoalUseCase
import com.jesuslcorominas.teamflowmanager.usecase.RegisterPlayerSubstitutionUseCase
import com.jesuslcorominas.teamflowmanager.usecase.ResumeMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.StartMatchTimerUseCase
import com.jesuslcorominas.teamflowmanager.usecase.StartPlayerTimerUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PreferencesRepository
import com.jesuslcorominas.teamflowmanager.viewmodel.utils.TimeTicker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MatchViewModel(
    private val getMatchById: GetMatchByIdUseCase,
    private val getAllPlayerTimesUseCase: GetAllPlayerTimesUseCase,
    private val getPlayersUseCase: GetPlayersUseCase,
    private val saveMatchUseCase: FinishMatchUseCase,
    private val pauseMatch: PauseMatchUseCase,
    private val resumeMatchUseCase: ResumeMatchUseCase,
    private val startMatchTimerUseCase: StartMatchTimerUseCase,
    private val startPlayerTimerUseCase: StartPlayerTimerUseCase,
    private val registerPlayerSubstitutionUseCase: RegisterPlayerSubstitutionUseCase,
    private val getMatchSummaryUseCase: GetMatchSummaryUseCase,
    private val registerGoal: RegisterGoalUseCase,
    private val preferencesRepository: PreferencesRepository,
    private val timeTicker: TimeTicker,
    savedStateHandle: SavedStateHandle,
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

    private val _showGoalScorerDialog = MutableStateFlow(false)
    val showGoalScorerDialog: StateFlow<Boolean> = _showGoalScorerDialog.asStateFlow()

    private val _showOpponentGoalDialog = MutableStateFlow(false)
    val showOpponentGoalDialog: StateFlow<Boolean> = _showOpponentGoalDialog.asStateFlow()

    init {
        val matchId: Long =
            savedStateHandle["matchId"]
                ?: throw IllegalArgumentException("matchId is required")

        loadMatchData(matchId)
        observeTime()
    }

    fun beginMatch(matchId: Long) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is MatchUiState.Success && !currentState.match.isStarted) {
                val currentTime = System.currentTimeMillis()
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
                    confirmStopMatch()
                }
            }
        }
    }

    fun confirmStopMatch() {
        viewModelScope.launch {
            (_uiState.value as? MatchUiState.Success)?.let { currentState ->

                saveMatchUseCase(currentState.match.id)
            }

            _showStopConfirmation.value = false
        }
    }

    fun dismissStopConfirmation() {
        _showStopConfirmation.value = false
    }

    fun pauseMatch() {
        viewModelScope.launch {
            (_uiState.value as? MatchUiState.Success)?.let { currentState ->
                if (currentState.match.canPause()) {
                    val currentTime = System.currentTimeMillis()
                    pauseMatch(currentState.match.id, currentTime)
                }
            }
        }
    }

    fun resumeMatch(matchId: Long) {
        viewModelScope.launch {
            val currentTime = System.currentTimeMillis()
            getMatchById(matchId).first()?.let {
                resumeMatchUseCase(it.id, currentTime)
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
        viewModelScope.launch {
            val playerOutId = _selectedPlayerOut.value
            val currentState = _uiState.value
            if (playerOutId != null && currentState is MatchUiState.Success) {
                val currentTime = System.currentTimeMillis()
                registerPlayerSubstitutionUseCase(
                    matchId = currentState.match.id,
                    playerOutId = playerOutId,
                    playerInId = playerInId,
                    currentTimeMillis = currentTime,
                )
                _selectedPlayerOut.value = null
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
                registerGoal(
                    matchId = currentState.match.id,
                    scorerId = scorerId,
                    currentTimeMillis = currentTime,
                    isOpponentGoal = false,
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
            val currentState = _uiState.value
            if (currentState is MatchUiState.Success) {
                val currentTime = System.currentTimeMillis()
                // For opponent goals, scorerId is null since opponent players are not tracked
                registerGoal(
                    matchId = currentState.match.id,
                    scorerId = null,
                    currentTimeMillis = currentTime,
                    isOpponentGoal = true,
                )
                _showOpponentGoalDialog.value = false
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
                if (match == null) {
                    MatchUiState.NoMatch
                } else if (match.status == MatchStatus.FINISHED) {
                    // Match is finished, load summary from history
                    null // Will be handled separately
                } else {
                    val matchTime = calculateCurrentTime(
                        match.elapsedTimeMillis,
                        match.status == MatchStatus.IN_PROGRESS,
                        match.lastStartTimeMillis,
                        currentTime,
                    )

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

                    MatchUiState.Success(
                        match = match,
                        matchTimeMillis = matchTime,
                        playerTimes = playerTimeItems,
                    )
                }
            }.collect { state ->
                if (state != null) {
                    _uiState.value = state
                } else {
                    // Load finished match summary
                    getMatchById(matchId).collect { match ->
                        if (match != null && match.status == MatchStatus.FINISHED) {
                            getMatchSummaryUseCase(match.id).collect { summary ->
                                if (summary != null) {
                                    _uiState.value = MatchUiState.Finished(
                                        match = match,
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

enum class PlayerSortOrderBy {
    BY_NUMBER,
    BY_TIME_DESC,
    BY_TIME_ASC,
    BY_ACTIVE_FIRST,
}

sealed class MatchUiState {
    data object Loading : MatchUiState()
    data object NoMatch : MatchUiState()
    data class Success(
        val match: Match,
        val matchTimeMillis: Long,
        val playerTimes: List<PlayerTimeItem>,
    ) : MatchUiState()

    data class Finished(
        val match: Match,
        val playerTimes: List<PlayerTimeItem>,
        val substitutions: List<SubstitutionItem>,
    ) : MatchUiState()
}

data class SubstitutionItem(
    val playerOut: Player,
    val playerIn: Player,
    val matchElapsedTimeMillis: Long,
)
