package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.usecase.GetCurrentUserUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetTeamUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SplashViewModel(
    private val getTeam: GetTeamUseCase,
    private val getCurrentUser: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    sealed interface UiState {
        data object Loading : UiState

        data object NotAuthenticated : UiState

        data object NoTeam : UiState

        data object TeamExists : UiState
    }

    init {
        checkAuthAndLoadTeam()
    }

    private fun checkAuthAndLoadTeam() {
        viewModelScope.launch {
            val user = getCurrentUser().first()
            if (user == null) {
                _uiState.value = UiState.NotAuthenticated
            } else {
                loadTeam()
            }
        }
    }

    private fun loadTeam() {
        viewModelScope.launch {
            getTeam().collect { team ->
                if (team == null) {
                    _uiState.value = UiState.NoTeam
                } else {
                    _uiState.value = UiState.TeamExists
                }
            }
        }
    }
}
