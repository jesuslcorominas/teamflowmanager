package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsEvent
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsParam
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker
import com.jesuslcorominas.teamflowmanager.domain.analytics.CrashReporter
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.usecase.GetArchivedMatchesUseCase
import com.jesuslcorominas.teamflowmanager.usecase.UnarchiveMatchUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ArchivedMatchesViewModel(
    private val getArchivedMatchesUseCase: GetArchivedMatchesUseCase,
    private val unarchiveMatchUseCase: UnarchiveMatchUseCase,
    private val analyticsTracker: AnalyticsTracker,
    private val crashReporter: CrashReporter,
) : ViewModel() {
    private val _uiState = MutableStateFlow<ArchivedMatchesUiState>(ArchivedMatchesUiState.Loading)
    val uiState: StateFlow<ArchivedMatchesUiState> = _uiState.asStateFlow()

    init {
        loadArchivedMatches()
    }

    private fun loadArchivedMatches() {
        viewModelScope.launch {
            // TODO archived should be a MatchStatus not a separate variable
            getArchivedMatchesUseCase.invoke().collect { matches ->
                _uiState.value =
                    if (matches.isEmpty()) {
                        ArchivedMatchesUiState.Empty
                    } else {
                        ArchivedMatchesUiState.Success(matches)
                    }
            }
        }
    }

    fun unarchiveMatch(matchId: Long) {
        viewModelScope.launch {
            try {
                crashReporter.log("Unarchiving match: $matchId")
                unarchiveMatchUseCase.invoke(matchId)
                
                analyticsTracker.logEvent(
                    AnalyticsEvent.MATCH_UNARCHIVED,
                    mapOf(
                        AnalyticsParam.MATCH_ID to matchId.toString(),
                    ),
                )
            } catch (e: Exception) {
                crashReporter.recordException(e)
                crashReporter.log("Error unarchiving match: ${e.message}")
                throw e
            }
        }
    }
}

sealed class ArchivedMatchesUiState {
    data object Loading : ArchivedMatchesUiState()
    data object Empty : ArchivedMatchesUiState()
    data class Success(val matches: List<Match>) : ArchivedMatchesUiState()
}
