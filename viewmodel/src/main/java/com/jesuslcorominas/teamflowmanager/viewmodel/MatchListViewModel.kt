package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.usecase.ArchiveMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.DeleteMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetAllMatchesUseCase
import com.jesuslcorominas.teamflowmanager.usecase.ResumeMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.UpdateMatchUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MatchListViewModel(
    private val getAllMatchesUseCase: GetAllMatchesUseCase,
    private val deleteMatchUseCase: DeleteMatchUseCase,
    private val updateMatchUseCase: UpdateMatchUseCase,
    private val resumeMatchUseCase: ResumeMatchUseCase,
    private val archiveMatchUseCase: ArchiveMatchUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<MatchListUiState>(MatchListUiState.Loading)
    val uiState: StateFlow<MatchListUiState> = _uiState.asStateFlow()

    private val _deleteConfirmationState =
        MutableStateFlow<MatchDeleteConfirmationState>(MatchDeleteConfirmationState.None)
    val deleteConfirmationState: StateFlow<MatchDeleteConfirmationState> = _deleteConfirmationState.asStateFlow()

    init {
        loadMatches()
    }

    private fun loadMatches() {
        viewModelScope.launch {
            getAllMatchesUseCase.invoke().collect { allMatches ->
                _uiState.update { previousState ->
                    if (allMatches.isEmpty()) MatchListUiState.Empty else MatchListUiState.Success(matches = allMatches)
                }
            }
        }
    }

    fun updateMatch(match: Match) {
        viewModelScope.launch {
            updateMatchUseCase.invoke(match)
        }
    }

    fun resumeMatch(matchId: Long) {
        viewModelScope.launch {
            resumeMatchUseCase.invoke(matchId, System.currentTimeMillis())
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
}

sealed class MatchListUiState {
    data object Loading : MatchListUiState()
    data object Empty : MatchListUiState()
    data class Success(val matches: List<Match>) : MatchListUiState()
}

sealed class MatchDeleteConfirmationState {
    data object None : MatchDeleteConfirmationState()
    data class Requested(val match: Match) : MatchDeleteConfirmationState()
}
