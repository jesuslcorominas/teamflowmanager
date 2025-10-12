package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.usecase.GetAllPlayerTimesUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetPlayersUseCase
import com.jesuslcorominas.teamflowmanager.usecase.FinishMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.PauseMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.RegisterPlayerSubstitutionUseCase
import com.jesuslcorominas.teamflowmanager.usecase.ResumeMatchUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MatchViewModel(
    private val getMatchUseCase: GetMatchUseCase,
    private val getAllPlayerTimesUseCase: GetAllPlayerTimesUseCase,
    private val getPlayersUseCase: GetPlayersUseCase,
    private val saveMatchUseCase: FinishMatchUseCase,
    private val pauseMatchUseCase: PauseMatchUseCase,
    private val resumeMatchUseCase: ResumeMatchUseCase,
    private val registerPlayerSubstitutionUseCase: RegisterPlayerSubstitutionUseCase,
    private val preferencesRepository: com.jesuslcorominas.teamflowmanager.usecase.repository.PreferencesRepository,
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
        startTimeUpdater()
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
                _uiState.value = state
            }
        }
    }

    private fun startTimeUpdater() {
        viewModelScope.launch {
            while (isActive) {
                delay(1000)
                _currentTime.value = System.currentTimeMillis()
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
}
