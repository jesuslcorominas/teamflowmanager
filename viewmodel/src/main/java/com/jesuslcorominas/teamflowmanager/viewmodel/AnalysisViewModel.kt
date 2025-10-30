package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerGoalStats
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeStats
import com.jesuslcorominas.teamflowmanager.usecase.GetPlayerGoalStatsUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetPlayerTimeStatsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AnalysisViewModel(
    private val getPlayerTimeStats: GetPlayerTimeStatsUseCase,
    private val getPlayerGoalStats: GetPlayerGoalStatsUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<AnalysisUiState>(AnalysisUiState.Loading)
    val uiState: StateFlow<AnalysisUiState> = _uiState.asStateFlow()

    private val _selectedTab = MutableStateFlow(AnalysisTab.TIMES)
    val selectedTab: StateFlow<AnalysisTab> = _selectedTab.asStateFlow()

    init {
        loadPlayerTimeStats()
        loadPlayerGoalStats()
    }

    fun selectTab(tab: AnalysisTab) {
        _selectedTab.value = tab
    }

    private fun loadPlayerTimeStats() {
        viewModelScope.launch {
            getPlayerTimeStats().collect { stats ->
                _uiState.value = if (stats.isEmpty()) {
                    AnalysisUiState.Empty
                } else {
                    (_uiState.value as? AnalysisUiState.Success)?.copy(playerTimeStats = stats)
                        ?: AnalysisUiState.Success(playerTimeStats = stats, playerGoalStats = emptyList())
                }
            }
        }
    }

    private fun loadPlayerGoalStats() {
        viewModelScope.launch {
            getPlayerGoalStats().collect { stats ->
                val currentState = _uiState.value
                _uiState.value = when (currentState) {
                    is AnalysisUiState.Success -> currentState.copy(playerGoalStats = stats)
                    else -> AnalysisUiState.Success(playerTimeStats = emptyList(), playerGoalStats = stats)
                }
            }
        }
    }
}

enum class AnalysisTab {
    TIMES,
    GOALS
}

sealed interface AnalysisUiState {
    data object Loading : AnalysisUiState
    data object Empty : AnalysisUiState
    data class Success(
        val playerTimeStats: List<PlayerTimeStats>,
        val playerGoalStats: List<PlayerGoalStats>
    ) : AnalysisUiState
}
