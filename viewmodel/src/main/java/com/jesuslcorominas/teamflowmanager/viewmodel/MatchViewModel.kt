package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTime
import com.jesuslcorominas.teamflowmanager.usecase.GetAllPlayerTimesUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetPlayersUseCase
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
) : ViewModel() {
    private val _uiState = MutableStateFlow<MatchUiState>(MatchUiState.Loading)
    val uiState: StateFlow<MatchUiState> = _uiState.asStateFlow()

    private val _currentTime = MutableStateFlow(System.currentTimeMillis())

    init {
        loadMatchData()
        startTimeUpdater()
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
        val matchTimeMillis: Long,
        val matchIsRunning: Boolean,
        val playerTimes: List<PlayerTimeItem>,
    ) : MatchUiState()
}
