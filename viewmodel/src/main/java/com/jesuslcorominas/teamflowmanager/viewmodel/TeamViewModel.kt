package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.usecase.CreateTeamUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetCaptainPlayerUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetTeamUseCase
import com.jesuslcorominas.teamflowmanager.usecase.UpdateTeamUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TeamViewModel(
    private val getTeamUseCase: GetTeamUseCase,
    private val createTeamUseCase: CreateTeamUseCase,
    private val updateTeamUseCase: UpdateTeamUseCase,
    private val getCaptainPlayerUseCase: GetCaptainPlayerUseCase,
    private val playerRepository: PlayerRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<TeamUiState>(TeamUiState.Loading)
    val uiState: StateFlow<TeamUiState> = _uiState.asStateFlow()

    init {
        loadTeam()
    }

    private fun loadTeam() {
        viewModelScope.launch {
            getTeamUseCase.invoke().collect { team ->
                if (team == null) {
                    _uiState.value = TeamUiState.NoTeam
                } else {
                    val captain = getCaptainPlayerUseCase.invoke()
                    _uiState.value = TeamUiState.TeamExists(team, captain)
                }
            }
        }
    }

    fun createTeam(team: Team) {
        viewModelScope.launch {
            createTeamUseCase.invoke(team)
        }
    }

    fun updateTeam(team: Team) {
        viewModelScope.launch {
            updateTeamUseCase.invoke(team)
        }
    }

    fun removeCaptain() {
        viewModelScope.launch {
            val captain = getCaptainPlayerUseCase.invoke()
            if (captain != null) {
                playerRepository.removePlayerAsCaptain(captain.id)
            }
        }
    }
}

sealed class TeamUiState {
    data object Loading : TeamUiState()

    data object NoTeam : TeamUiState()

    data class TeamExists(val team: Team, val captain: Player? = null) : TeamUiState()
}
