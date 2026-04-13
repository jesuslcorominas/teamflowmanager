package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetTeamUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SignOutUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PendingTeamAssignmentViewModel(
    private val getTeam: GetTeamUseCase,
    private val signOut: SignOutUseCase,
) : ViewModel() {
    sealed interface UiState {
        data object Waiting : UiState

        data object TeamAssigned : UiState

        data object SignedOut : UiState
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Waiting)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getTeam().collect { team ->
                if (team != null) {
                    _uiState.value = UiState.TeamAssigned
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            runCatching { signOut.invoke() }
            _uiState.value = UiState.SignedOut
        }
    }
}
