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
import com.jesuslcorominas.teamflowmanager.usecase.repository.PreferencesRepository
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
    private val registerPlayerSubstitutionUseCase: RegisterPlayerSubstitutionUseCase,
    private val getMatchSummaryUseCase: GetMatchSummaryUseCase,
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

    init {
        loadMatchData()
        observeTime()
    }

    fun saveMatch() {
        viewModelScope.launch {
            saveMatchUseCase()
        }
    }

    fun pauseMatch() {
        viewModelScope.launch {
            val currentTime = System.currentTimeMillis()
            pauseMatchUseCase(currentTime)
            // Update the current time immediately to avoid race conditions
            _currentTime.value = currentTime
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

    private fun loadMatchData() {
        viewModelScope.launch {
            combine(
                getMatchUseCase(),
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
                        match.isRunning,
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
                        )
                    }

                    MatchUiState.Success(
                        matchId = match.id,
                        matchTimeMillis = matchTime,
                        matchIsRunning = match.isRunning,
                        playerTimes = playerTimeItems,
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
)

sealed class MatchUiState {
    data object Loading : MatchUiState()
    data object NoMatch : MatchUiState()
    data class Success(
        val matchId: Long,
        val matchTimeMillis: Long,
        val matchIsRunning: Boolean,
        val playerTimes: List<PlayerTimeItem>,
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
