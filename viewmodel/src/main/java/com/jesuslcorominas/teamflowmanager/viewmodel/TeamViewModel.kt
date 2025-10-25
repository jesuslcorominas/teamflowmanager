package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.navigation.Route
import com.jesuslcorominas.teamflowmanager.usecase.CreateTeamUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetCaptainPlayerUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetPlayersUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetTeamUseCase
import com.jesuslcorominas.teamflowmanager.usecase.UpdateTeamUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TeamViewModel(
    private val getTeam: GetTeamUseCase,
    private val getPlayers: GetPlayersUseCase,
    private val createTeam: CreateTeamUseCase,
    private val updateTeam: UpdateTeamUseCase,
    private val getCaptainPlayer: GetCaptainPlayerUseCase,
    private val playerRepository: PlayerRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _uiState = MutableStateFlow<TeamUiState>(TeamUiState.Loading)
    val uiState: StateFlow<TeamUiState> = _uiState.asStateFlow()

    private val _showExitDialog = MutableStateFlow(false)
    val showExitDialog: StateFlow<Boolean> = _showExitDialog

    val isEditMode: Boolean = (savedStateHandle[Route.Team.ARG_MODE] as? String) == Route.Team.MODE_EDIT

    init {
        loadTeam()
    }

    private fun loadTeam() {
        viewModelScope.launch {
            combine(
                getTeam(), getPlayers()
            ) { team, players ->
                team to players
            }.collect { (team, players) ->
                _uiState.update { if (team == null) TeamUiState.NoTeam else TeamUiState.Success(team, players) }
            }
        }
    }

    fun createTeam(team: Team) {
        viewModelScope.launch {
            createTeam.invoke(team)
        }
    }

    fun updateTeam(team: Team, captainId: Long?) {
        viewModelScope.launch {
            val captain = getCaptainPlayer.invoke()
            if (captain != null && captainId == null) {
                // Remove current captain
                playerRepository.removePlayerAsCaptain(captain.id) // TODO extract to usecase
            } else if (captainId != null && (captain == null || captain.id != captainId)) {
                // Set new captain
                playerRepository.setPlayerAsCaptain(captainId) // TODO extract to usecase
            }

            updateTeam.invoke(team)
        }
    }

    fun requestBack(onNavigateBack: () -> Unit) {
        if (isEditMode) {
            // TODO check if there are unsaved changes
            _showExitDialog.value = true
        } else {
            onNavigateBack()
        }
    }

    fun discardChanges(onNavigateBack: () -> Unit) {
        _showExitDialog.value = false
        onNavigateBack()
    }

    fun dismissExitDialog() {
        _showExitDialog.value = false
    }
}

sealed interface TeamUiState {
    data object Loading : TeamUiState

    data object NoTeam : TeamUiState

    data class Success(val team: Team, val players: List<Player>) : TeamUiState
}
