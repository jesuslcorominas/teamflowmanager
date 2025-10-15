package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.usecase.GetMatchSummaryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MatchSummaryViewModel(
    private val getMatchSummaryUseCase: GetMatchSummaryUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<MatchSummaryUiState>(MatchSummaryUiState.Loading)
    val uiState: StateFlow<MatchSummaryUiState> = _uiState.asStateFlow()

    fun loadMatchSummary(matchId: Long) {
        viewModelScope.launch {
            getMatchSummaryUseCase(matchId).collect { summary ->
                if (summary == null) {
                    _uiState.value = MatchSummaryUiState.NotFound
                } else {
                    _uiState.value = MatchSummaryUiState.Success(
                        matchId = summary.match.id,
                        opponent = summary.match.opponent ?: "",
                        location = summary.match.location ?: "",
                        matchTimeMillis = summary.match.elapsedTimeMillis,
                        playerTimes = summary.playerTimes.map { playerTimeSummary ->
                            PlayerTimeItem(
                                player = playerTimeSummary.player,
                                timeMillis = playerTimeSummary.elapsedTimeMillis,
                                isRunning = false,
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
        val matchId: Long,
        val opponent: String,
        val location: String,
        val matchTimeMillis: Long,
        val playerTimes: List<PlayerTimeItem>,
        val substitutions: List<SubstitutionItem>,
    ) : MatchSummaryUiState()
}
