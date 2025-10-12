package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.usecase.GetMatchByIdUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetPlayersUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class MatchDetailViewModel(
    private val getMatchByIdUseCase: GetMatchByIdUseCase,
    private val getPlayersUseCase: GetPlayersUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<MatchDetailUiState>(MatchDetailUiState.Loading)
    val uiState: StateFlow<MatchDetailUiState> = _uiState.asStateFlow()

    fun loadMatch(matchId: Long?) {
        viewModelScope.launch {
            if (matchId == null) {
                // New match creation mode
                getPlayersUseCase.invoke().collect { players ->
                    _uiState.value = MatchDetailUiState.Create(players)
                }
            } else {
                // Edit existing match mode
                combine(
                    getMatchByIdUseCase.invoke(matchId),
                    getPlayersUseCase.invoke(),
                ) { match, players ->
                    if (match != null) {
                        MatchDetailUiState.Edit(match, players)
                    } else {
                        MatchDetailUiState.NotFound
                    }
                }.collect { state ->
                    _uiState.value = state
                }
            }
        }
    }
}

sealed class MatchDetailUiState {
    data object Loading : MatchDetailUiState()
    data object NotFound : MatchDetailUiState()
    data class Create(val availablePlayers: List<Player>) : MatchDetailUiState()
    data class Edit(val match: Match, val availablePlayers: List<Player>) : MatchDetailUiState()
}
