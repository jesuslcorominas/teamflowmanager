package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeStats
import com.jesuslcorominas.teamflowmanager.usecase.GetPlayerTimeStatsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AnalysisViewModel(
    private val getPlayerTimeStats: GetPlayerTimeStatsUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<AnalysisUiState>(AnalysisUiState.Loading)
    val uiState: StateFlow<AnalysisUiState> = _uiState.asStateFlow()

    init {
        loadPlayerTimeStats()
    }

    private fun loadPlayerTimeStats() {
        viewModelScope.launch {
            getPlayerTimeStats().collect { stats ->
                _uiState.value = if (stats.isEmpty()) {
                    AnalysisUiState.Empty
                } else {
                    AnalysisUiState.Success(stats)
                }
            }
        }
    }
}

sealed interface AnalysisUiState {
    data object Loading : AnalysisUiState
    data object Empty : AnalysisUiState
    data class Success(val playerStats: List<PlayerTimeStats>) : AnalysisUiState
}
