package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.usecase.AddPlayerUseCase
import com.jesuslcorominas.teamflowmanager.usecase.DeletePlayerUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetCaptainPlayerUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetPlayersUseCase
import com.jesuslcorominas.teamflowmanager.usecase.UpdatePlayerUseCase
import com.jesuslcorominas.teamflowmanager.usecase.UpdateScheduledMatchesCaptainUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val getPlayersUseCase: GetPlayersUseCase,
    private val addPlayerUseCase: AddPlayerUseCase,
    private val updatePlayerUseCase: UpdatePlayerUseCase,
    private val deletePlayerUseCase: DeletePlayerUseCase,
    private val getCaptainPlayerUseCase: GetCaptainPlayerUseCase,
    private val updateScheduledMatchesCaptainUseCase: UpdateScheduledMatchesCaptainUseCase,
    private val playerRepository: PlayerRepository,
    private val matchRepository: MatchRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<PlayerUiState>(PlayerUiState.Loading)
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private val _deleteConfirmationState = MutableStateFlow<DeleteConfirmationState>(DeleteConfirmationState.None)
    val deleteConfirmationState: StateFlow<DeleteConfirmationState> = _deleteConfirmationState.asStateFlow()

    private val _captainConfirmationState = MutableStateFlow<CaptainConfirmationState>(CaptainConfirmationState.None)
    val captainConfirmationState: StateFlow<CaptainConfirmationState> = _captainConfirmationState.asStateFlow()

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

    fun savePlayer(player: Player) {
        viewModelScope.launch {
            val currentCaptain = getCaptainPlayerUseCase.invoke()
            
            if (player.isCaptain && currentCaptain != null && currentCaptain.id != player.id) {
                // Show confirmation to replace captain
                _captainConfirmationState.value = CaptainConfirmationState.ConfirmReplace(
                    currentCaptain = currentCaptain,
                    newCaptain = player
                )
            } else if (!player.isCaptain && currentCaptain != null && currentCaptain.id == player.id) {
                // Player is being updated to no longer be captain
                val scheduledMatches = matchRepository.getScheduledMatches()
                if (scheduledMatches.isNotEmpty()) {
                    _captainConfirmationState.value = CaptainConfirmationState.ConfirmRemoveWithMatches(
                        player = player,
                        matchCount = scheduledMatches.size
                    )
                } else {
                    _captainConfirmationState.value = CaptainConfirmationState.ConfirmRemove(player)
                }
            } else {
                // No confirmation needed, save directly
                savePlayerDirectly(player)
            }
        }
    }

    fun confirmCaptainChange(keepInMatches: Boolean = false) {
        viewModelScope.launch {
            when (val state = _captainConfirmationState.value) {
                is CaptainConfirmationState.ConfirmReplace -> {
                    // Save the new captain
                    savePlayerDirectly(state.newCaptain)
                    // Check if we should update scheduled matches
                    val scheduledMatches = matchRepository.getScheduledMatches()
                    if (scheduledMatches.isNotEmpty()) {
                        updateScheduledMatchesCaptainUseCase.invoke(state.newCaptain.id)
                    }
                }
                is CaptainConfirmationState.ConfirmRemove -> {
                    savePlayerDirectly(state.player)
                }
                is CaptainConfirmationState.ConfirmRemoveWithMatches -> {
                    savePlayerDirectly(state.player)
                    if (!keepInMatches) {
                        // Remove captain from scheduled matches
                        updateScheduledMatchesCaptainUseCase.invoke(null)
                    }
                }
                else -> {}
            }
            _captainConfirmationState.value = CaptainConfirmationState.None
        }
    }

    fun cancelCaptainChange() {
        _captainConfirmationState.value = CaptainConfirmationState.None
    }

    private fun savePlayerDirectly(player: Player) {
        viewModelScope.launch {
            if (player.id == 0L) {
                addPlayerUseCase.invoke(player)
            } else {
                updatePlayerUseCase.invoke(player)
            }
            
            // Update captain status in repository
            if (player.isCaptain) {
                playerRepository.setPlayerAsCaptain(player.id)
            } else {
                val currentCaptain = getCaptainPlayerUseCase.invoke()
                if (currentCaptain?.id == player.id) {
                    playerRepository.removePlayerAsCaptain(player.id)
                }
            }
        }
    }

    fun updatePlayer(player: Player) {
        savePlayer(player)
    }

    fun addPlayer(player: Player) {
        savePlayer(player)
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

sealed class CaptainConfirmationState {
    data object None : CaptainConfirmationState()

    data class ConfirmReplace(
        val currentCaptain: Player,
        val newCaptain: Player,
    ) : CaptainConfirmationState()

    data class ConfirmReplaceWithMatches(
        val currentCaptain: Player,
        val newCaptain: Player,
        val matchCount: Int,
    ) : CaptainConfirmationState()

    data class ConfirmRemove(
        val player: Player,
    ) : CaptainConfirmationState()

    data class ConfirmRemoveWithMatches(
        val player: Player,
        val matchCount: Int,
    ) : CaptainConfirmationState()
}
