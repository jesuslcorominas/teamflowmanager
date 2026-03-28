package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetMatchesByTeamUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetPlayersByTeamUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetTeamByFirestoreIdUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

enum class PresidentTeamTab { SUMMARY, PLAYERS, MATCHES, STATS }

data class PresidentTeamStats(
    val totalMatches: Int,
    val wins: Int,
    val draws: Int,
    val losses: Int,
    val goalsScored: Int,
    val goalsConceded: Int,
    val squadSize: Int,
)

sealed interface PresidentTeamDetailUiState {
    data object Loading : PresidentTeamDetailUiState

    data class Ready(
        val team: Team,
        val players: List<Player>,
        val matches: List<Match>,
        val stats: PresidentTeamStats,
    ) : PresidentTeamDetailUiState

    data object Error : PresidentTeamDetailUiState
}

class PresidentTeamDetailViewModel(
    private val teamFirestoreId: String,
    private val getTeamByFirestoreId: GetTeamByFirestoreIdUseCase,
    private val getPlayersByTeam: GetPlayersByTeamUseCase,
    private val getMatchesByTeam: GetMatchesByTeamUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<PresidentTeamDetailUiState>(PresidentTeamDetailUiState.Loading)
    val uiState: StateFlow<PresidentTeamDetailUiState> = _uiState.asStateFlow()

    private val _selectedTab = MutableStateFlow(PresidentTeamTab.SUMMARY)
    val selectedTab: StateFlow<PresidentTeamTab> = _selectedTab.asStateFlow()

    init {
        load()
    }

    fun selectTab(tab: PresidentTeamTab) {
        _selectedTab.value = tab
    }

    private fun load() {
        viewModelScope.launch {
            val team = getTeamByFirestoreId(teamFirestoreId)
            if (team == null) {
                _uiState.value = PresidentTeamDetailUiState.Error
                return@launch
            }

            combine(
                getPlayersByTeam(teamFirestoreId),
                getMatchesByTeam(teamFirestoreId),
            ) { players, matches ->
                val finishedMatches = matches.filter { it.status == MatchStatus.FINISHED }
                val stats =
                    PresidentTeamStats(
                        totalMatches = finishedMatches.size,
                        wins = finishedMatches.count { it.goals > it.opponentGoals },
                        draws = finishedMatches.count { it.goals == it.opponentGoals },
                        losses = finishedMatches.count { it.goals < it.opponentGoals },
                        goalsScored = finishedMatches.sumOf { it.goals },
                        goalsConceded = finishedMatches.sumOf { it.opponentGoals },
                        squadSize = players.size,
                    )
                PresidentTeamDetailUiState.Ready(
                    team = team,
                    players = players,
                    matches = matches.filter { !it.archived }.sortedByDescending { it.dateTime },
                    stats = stats,
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}
