package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.usecase.GetMatchSummaryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MatchSummaryViewModel(
    private val getMatchSummaryUseCase: GetMatchSummaryUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<MatchSummaryUiState>(MatchSummaryUiState.Loading)

    fun loadMatchSummary(matchId: Long) {
        viewModelScope.launch {
            getMatchSummaryUseCase(matchId).collect { summary ->
                if (summary == null) {
                    _uiState.value = MatchSummaryUiState.NotFound
                } else {
                    _uiState.value = MatchSummaryUiState.Success(
                        match = summary.match,
                        playerTimes = summary.playerTimes.map { playerTimeSummary ->
                            PlayerTimeItem(
                                player = playerTimeSummary.player,
                                timeMillis = playerTimeSummary.elapsedTimeMillis,
                                isRunning = false,
                                substitutionCount = playerTimeSummary.substitutionCount,
                            )
                        },
                        substitutions = summary.substitutions.map { sub ->
                            SubstitutionItem(
                                playerOut = sub.playerOut,
                                playerIn = sub.playerIn,
                                matchElapsedTimeMillis = sub.matchElapsedTimeMillis,
                            )
                        },
                    )
                }
            }
        }
    }
}

sealed class MatchSummaryUiState {
    data object Loading : MatchSummaryUiState()
    data object NotFound : MatchSummaryUiState()
    data class Success(
        val match: Match,
        val playerTimes: List<PlayerTimeItem>,
        val substitutions: List<SubstitutionItem>,
    ) : MatchSummaryUiState()
}
