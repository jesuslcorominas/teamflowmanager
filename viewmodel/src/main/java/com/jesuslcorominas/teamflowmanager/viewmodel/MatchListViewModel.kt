package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsEvent
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsParam
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker
import com.jesuslcorominas.teamflowmanager.domain.analytics.CrashReporter
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.utils.TimeProvider
import com.jesuslcorominas.teamflowmanager.usecase.ArchiveMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.DeleteMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetAllMatchesUseCase
import com.jesuslcorominas.teamflowmanager.usecase.ResumeMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.SynchronizeTimeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MatchListViewModel(
    private val getAllMatchesUseCase: GetAllMatchesUseCase,
    private val deleteMatchUseCase: DeleteMatchUseCase,
    private val resumeMatchUseCase: ResumeMatchUseCase,
    private val archiveMatchUseCase: ArchiveMatchUseCase,
    private val synchronizeTimeUseCase: SynchronizeTimeUseCase,
    private val timeProvider: TimeProvider,
    private val analyticsTracker: AnalyticsTracker,
    private val crashReporter: CrashReporter,
) : ViewModel() {
    private val _uiState = MutableStateFlow<MatchListUiState>(MatchListUiState.Loading)
    val uiState: StateFlow<MatchListUiState> = _uiState.asStateFlow()

    private val _deleteConfirmationState =
        MutableStateFlow<MatchDeleteConfirmationState>(MatchDeleteConfirmationState.None)
    val deleteConfirmationState: StateFlow<MatchDeleteConfirmationState> = _deleteConfirmationState.asStateFlow()

    private val _query = MutableStateFlow("")

    init {
        loadMatches()
    }

    private fun loadMatches() {
        viewModelScope.launch {
            combine(getAllMatchesUseCase(), _query) { matches, query ->
                val filtered = if (query.isBlank()) matches
                else matches.filter {
                    it.opponent.contains(query, ignoreCase = true) ||
                        it.location.contains(query, ignoreCase = true)
                }

                when {
                    matches.isEmpty() -> MatchListUiState.Empty
                    else -> MatchListUiState.Success(matches = filtered)
                }
            }.collect { newState ->
                _uiState.update { newState }
            }
        }
    }

    fun resumeMatch(matchId: Long) {
        viewModelScope.launch {
            try {
                crashReporter.log("Resuming match: $matchId")
                
                // Synchronize time with server before resuming
                try {
                    synchronizeTimeUseCase()
                } catch (e: Exception) {
                    crashReporter.recordException(e)
                    crashReporter.log("Error synchronizing time before match resume: ${e.message}")
                    // Continue with match resume even if sync fails
                }
                
                resumeMatchUseCase(matchId, timeProvider.getCurrentTime())

                analyticsTracker.logEvent(
                    AnalyticsEvent.MATCH_RESUMED,
                    mapOf(
                        AnalyticsParam.MATCH_ID to matchId.toString(),
                    ),
                )
            } catch (e: Exception) {
                crashReporter.recordException(e)
                crashReporter.log("Error resuming match: ${e.message}")
                throw e
            }
        }
    }

    fun requestDeleteMatch(match: Match) {
        _deleteConfirmationState.value = MatchDeleteConfirmationState.Requested(match)
    }

    fun confirmDeleteMatch() {
        val state = _deleteConfirmationState.value
        if (state is MatchDeleteConfirmationState.Requested) {
            _deleteConfirmationState.value = MatchDeleteConfirmationState.Deleting
            viewModelScope.launch {
                try {
                    crashReporter.log("Deleting match: ${state.match.id}")
                    deleteMatchUseCase(state.match.id)

                    analyticsTracker.logEvent(
                        AnalyticsEvent.MATCH_DELETED,
                        mapOf(
                            AnalyticsParam.MATCH_ID to state.match.id.toString(),
                        ),
                    )

                    _deleteConfirmationState.value = MatchDeleteConfirmationState.None
                } catch (e: Exception) {
                    crashReporter.recordException(e)
                    crashReporter.log("Error deleting match: ${e.message}")
                    _deleteConfirmationState.value = MatchDeleteConfirmationState.None
                }
            }
        }
    }

    fun cancelDeleteMatch() {
        _deleteConfirmationState.value = MatchDeleteConfirmationState.None
    }

    fun archiveMatch(matchId: Long) {
        viewModelScope.launch {
            try {
                crashReporter.log("Archiving match: $matchId")
                archiveMatchUseCase(matchId)

                analyticsTracker.logEvent(
                    AnalyticsEvent.MATCH_ARCHIVED,
                    mapOf(
                        AnalyticsParam.MATCH_ID to matchId.toString(),
                    ),
                )
            } catch (e: Exception) {
                crashReporter.recordException(e)
                crashReporter.log("Error archiving match: ${e.message}")
                throw e
            }
        }
    }

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }
}

sealed class MatchListUiState {
    data object Loading : MatchListUiState()
    data object Empty : MatchListUiState()
    data class Success(val matches: List<Match>) : MatchListUiState()
}

sealed class MatchDeleteConfirmationState {
    data object None : MatchDeleteConfirmationState()
    data class Requested(val match: Match) : MatchDeleteConfirmationState()
    data object Deleting : MatchDeleteConfirmationState()
}
