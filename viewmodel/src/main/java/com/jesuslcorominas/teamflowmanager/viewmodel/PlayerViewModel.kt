package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.usecase.AddPlayerUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetPlayersUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val getPlayersUseCase: GetPlayersUseCase,
    private val addPlayerUseCase: AddPlayerUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<PlayerUiState>(PlayerUiState.Loading)
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    init {
        loadPlayers()
    }

    private fun loadPlayers() {
        viewModelScope.launch {
            getPlayersUseCase.invoke().collect { players ->
                _uiState.value =
                    if (players.isEmpty()) {
                        PlayerUiState.Empty
                    } else {
                        PlayerUiState.Success(players)
                    }
            }
        }
    }

    fun addPlayer(player: Player) {
        viewModelScope.launch {
            addPlayerUseCase.invoke(player)
        }
    }
}

sealed class PlayerUiState {
    data object Loading : PlayerUiState()

    data object Empty : PlayerUiState()

    data class Success(
        val players: List<Player>,
    ) : PlayerUiState()
}
