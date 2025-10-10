package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.usecase.AddPlayerUseCase
import com.jesuslcorominas.teamflowmanager.usecase.DeletePlayerUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetPlayersUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val getPlayersUseCase: GetPlayersUseCase,
    private val addPlayerUseCase: AddPlayerUseCase,
    private val deletePlayerUseCase: DeletePlayerUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<PlayerUiState>(PlayerUiState.Loading)
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private val _deleteConfirmationState = MutableStateFlow<DeleteConfirmationState>(DeleteConfirmationState.None)
    val deleteConfirmationState: StateFlow<DeleteConfirmationState> = _deleteConfirmationState.asStateFlow()

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

    fun showDeleteConfirmation(player: Player) {
        _deleteConfirmationState.value = DeleteConfirmationState.Confirming(player)
    }

    fun dismissDeleteConfirmation() {
        _deleteConfirmationState.value = DeleteConfirmationState.None
    }

    fun deletePlayer(playerId: Long) {
        viewModelScope.launch {
            deletePlayerUseCase.invoke(playerId)
            _deleteConfirmationState.value = DeleteConfirmationState.None
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

sealed class DeleteConfirmationState {
    data object None : DeleteConfirmationState()

    data class Confirming(
        val player: Player,
    ) : DeleteConfirmationState()
}
