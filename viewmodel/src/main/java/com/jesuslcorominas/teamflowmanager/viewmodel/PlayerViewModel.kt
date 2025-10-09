package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.usecase.GetPlayersUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing player list UI state
 */
class PlayerViewModel(
    private val getPlayersUseCase: GetPlayersUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<PlayerUiState>(PlayerUiState.Loading)
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    init {
        loadPlayers()
    }

    private fun loadPlayers() {
        viewModelScope.launch {
            getPlayersUseCase.invoke().collect { players ->
                _uiState.value = if (players.isEmpty()) {
                    PlayerUiState.Empty
                } else {
                    PlayerUiState.Success(players)
                }
            }
        }
    }
}

/**
 * UI state for player list screen
 */
sealed class PlayerUiState {
    data object Loading : PlayerUiState()
    data object Empty : PlayerUiState()
    data class Success(val players: List<Player>) : PlayerUiState()
}
