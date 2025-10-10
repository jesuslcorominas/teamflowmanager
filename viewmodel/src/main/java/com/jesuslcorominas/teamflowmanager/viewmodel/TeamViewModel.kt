package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.usecase.CreateTeamUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetTeamUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TeamViewModel(
    private val getTeamUseCase: GetTeamUseCase,
    private val createTeamUseCase: CreateTeamUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<TeamUiState>(TeamUiState.Loading)
    val uiState: StateFlow<TeamUiState> = _uiState.asStateFlow()

    init {
        loadTeam()
    }

    private fun loadTeam() {
        viewModelScope.launch {
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

    fun createTeam(
        name: String,
        coachName: String,
        delegateName: String,
    ) {
        viewModelScope.launch {
            val team =
                Team(
                    id = 0,
                    name = name,
                    coachName = coachName,
                    delegateName = delegateName,
                )
            createTeamUseCase.invoke(team)
        }
    }
}

sealed class TeamUiState {
    data object Loading : TeamUiState()

    data object NoTeam : TeamUiState()

    data class TeamExists(
        val team: Team,
    ) : TeamUiState()
}
