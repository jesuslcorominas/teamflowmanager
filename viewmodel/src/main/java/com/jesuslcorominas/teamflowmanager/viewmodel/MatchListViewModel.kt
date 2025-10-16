package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.usecase.ArchiveMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.CreateMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.DeleteMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.FilterMatchesUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetAllMatchesUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetArchivedMatchesUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.ResumeMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.StartMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.UnarchiveMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.UpdateMatchUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class MatchListViewModel(
    private val getAllMatchesUseCase: GetAllMatchesUseCase,
    private val getArchivedMatchesUseCase: GetArchivedMatchesUseCase,
    private val getMatchUseCase: GetMatchUseCase,
    private val deleteMatchUseCase: DeleteMatchUseCase,
    private val createMatchUseCase: CreateMatchUseCase,
    private val updateMatchUseCase: UpdateMatchUseCase,
    private val startMatchUseCase: StartMatchUseCase,
    private val resumeMatchUseCase: ResumeMatchUseCase,
    private val archiveMatchUseCase: ArchiveMatchUseCase,
    private val unarchiveMatchUseCase: UnarchiveMatchUseCase,
    private val filterMatchesUseCase: FilterMatchesUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<MatchListUiState>(MatchListUiState.Loading)
    val uiState: StateFlow<MatchListUiState> = _uiState.asStateFlow()

    private val _deleteConfirmationState = MutableStateFlow<MatchDeleteConfirmationState>(MatchDeleteConfirmationState.None)
    val deleteConfirmationState: StateFlow<MatchDeleteConfirmationState> = _deleteConfirmationState.asStateFlow()

    private val _filterState = MutableStateFlow<FilterState>(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    init {
        loadMatches()
    }

    private fun loadMatches() {
        viewModelScope.launch {
            filterState.flatMapLatest { filter ->
                if (filter.isActive) {
                    // Use filter use case when filtering is active
                    filterMatchesUseCase.invoke(
                        filterText = filter.searchText,
                        startDate = filter.startDate,
                        endDate = filter.endDate
                    )
                } else {
                    // Use normal getAllMatches when not filtering
                    getAllMatchesUseCase.invoke()
                }
            }.combine(getMatchUseCase.invoke()) { matches, currentMatch ->
                if (matches.isEmpty()) {
                    MatchListUiState.Empty
                } else {
                    MatchListUiState.Success(
                        matches = matches,
                        currentMatchId = currentMatch?.id
                    )
                }
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun createMatch(match: Match) {
        viewModelScope.launch {
            createMatchUseCase.invoke(match)
        }
    }

    fun updateMatch(match: Match) {
        viewModelScope.launch {
            updateMatchUseCase.invoke(match)
        }
    }

    fun startMatch(matchId: Long) {
        viewModelScope.launch {
            startMatchUseCase.invoke(matchId, System.currentTimeMillis())
        }
    }

    fun resumeMatch() {
        viewModelScope.launch {
            resumeMatchUseCase.invoke(System.currentTimeMillis())
        }
    }

    fun requestDeleteMatch(match: Match) {
        _deleteConfirmationState.value = MatchDeleteConfirmationState.Requested(match)
    }

    fun confirmDeleteMatch() {
        val state = _deleteConfirmationState.value
        if (state is MatchDeleteConfirmationState.Requested) {
            viewModelScope.launch {
                deleteMatchUseCase.invoke(state.match.id)
                _deleteConfirmationState.value = MatchDeleteConfirmationState.None
            }
        }
    }

    fun cancelDeleteMatch() {
        _deleteConfirmationState.value = MatchDeleteConfirmationState.None
    }

    fun archiveMatch(matchId: Long) {
        viewModelScope.launch {
            archiveMatchUseCase.invoke(matchId)
        }
    }

    fun unarchiveMatch(matchId: Long) {
        viewModelScope.launch {
            unarchiveMatchUseCase.invoke(matchId)
        }
    }

    fun toggleFilterMode() {
        _filterState.value = _filterState.value.copy(
            isFilterModeEnabled = !_filterState.value.isFilterModeEnabled,
            searchText = if (_filterState.value.isFilterModeEnabled) "" else _filterState.value.searchText,
            startDate = if (_filterState.value.isFilterModeEnabled) null else _filterState.value.startDate,
            endDate = if (_filterState.value.isFilterModeEnabled) null else _filterState.value.endDate,
        )
    }

    fun updateSearchText(text: String) {
        _filterState.value = _filterState.value.copy(searchText = text)
    }

    fun updateDateRange(startDate: Long?, endDate: Long?) {
        _filterState.value = _filterState.value.copy(
            startDate = startDate,
            endDate = endDate,
        )
    }

    fun clearFilters() {
        _filterState.value = FilterState(isFilterModeEnabled = _filterState.value.isFilterModeEnabled)
    }
}

sealed class MatchListUiState {
    data object Loading : MatchListUiState()
    data object Empty : MatchListUiState()
    data class Success(
        val matches: List<Match>,
        val currentMatchId: Long? = null
    ) : MatchListUiState()
}

sealed class MatchDeleteConfirmationState {
    data object None : MatchDeleteConfirmationState()
    data class Requested(val match: Match) : MatchDeleteConfirmationState()
}

data class FilterState(
    val isFilterModeEnabled: Boolean = false,
    val searchText: String = "",
    val startDate: Long? = null,
    val endDate: Long? = null,
) {
    val isActive: Boolean
        get() = isFilterModeEnabled && (searchText.isNotBlank() || (startDate != null && endDate != null))
}
