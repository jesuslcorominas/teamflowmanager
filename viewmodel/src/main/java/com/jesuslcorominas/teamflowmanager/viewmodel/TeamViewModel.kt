package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.usecase.CreateTeamUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetTeamUseCase
import com.jesuslcorominas.teamflowmanager.usecase.UpdateTeamUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TeamViewModel(
    private val getTeamUseCase: GetTeamUseCase,
    private val createTeamUseCase: CreateTeamUseCase,
    private val updateTeamUseCase: UpdateTeamUseCase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {
    private val _uiState = MutableStateFlow<TeamUiState>(TeamUiState.Loading)
    val uiState: StateFlow<TeamUiState> = _uiState.asStateFlow()

    init {
        loadTeam()
    }

    private fun loadTeam() {
        viewModelScope.launch(dispatcher) {
            getTeamUseCase.invoke().collect { team ->
                _uiState.value =
                    if (team == null) {
                        TeamUiState.NoTeam
                    } else {
                        TeamUiState.TeamExists(team)
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
}

sealed class TeamUiState {
    data object Loading : TeamUiState()

    data object NoTeam : TeamUiState()

    data class TeamExists(val team: Team) : TeamUiState()
}
