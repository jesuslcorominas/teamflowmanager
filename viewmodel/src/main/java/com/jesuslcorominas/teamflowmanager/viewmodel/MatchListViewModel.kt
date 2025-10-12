package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.usecase.CreateMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.DeleteMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetAllMatchesUseCase
import com.jesuslcorominas.teamflowmanager.usecase.UpdateMatchUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MatchListViewModel(
    private val getAllMatchesUseCase: GetAllMatchesUseCase,
    private val deleteMatchUseCase: DeleteMatchUseCase,
    private val createMatchUseCase: CreateMatchUseCase,
    private val updateMatchUseCase: UpdateMatchUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<MatchListUiState>(MatchListUiState.Loading)
    val uiState: StateFlow<MatchListUiState> = _uiState.asStateFlow()

    private val _deleteConfirmationState = MutableStateFlow<MatchDeleteConfirmationState>(MatchDeleteConfirmationState.None)
    val deleteConfirmationState: StateFlow<MatchDeleteConfirmationState> = _deleteConfirmationState.asStateFlow()

    init {
        loadMatches()
    }

    private fun loadMatches() {
        viewModelScope.launch {
            getAllMatchesUseCase.invoke().collect { matches ->
                _uiState.value =
                    if (matches.isEmpty()) {
                        MatchListUiState.Empty
                    } else {
                        MatchListUiState.Success(matches)
                    }
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
