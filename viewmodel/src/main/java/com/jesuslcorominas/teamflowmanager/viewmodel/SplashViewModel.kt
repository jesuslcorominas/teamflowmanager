package com.jesuslcorominas.teamflowmanager.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetCurrentUserUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetTeamUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetUserClubMembershipUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SynchronizeTimeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SplashViewModel(
    private val getTeam: GetTeamUseCase,
    private val getCurrentUser: GetCurrentUserUseCase,
    private val synchronizeTimeUseCase: SynchronizeTimeUseCase,
    private val getUserClubMembership: GetUserClubMembershipUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    sealed interface UiState {
        data object Loading : UiState

        data object NotAuthenticated : UiState

        data object LocalDataNeedsAuth : UiState

        data object NoClub : UiState

        data object NoTeam : UiState

        data object TeamExists : UiState
    }

    init {
        performStartupTasks()
    }

    private fun performStartupTasks() {
        viewModelScope.launch {
            // Synchronize time with server on app startup
            try {
                synchronizeTimeUseCase()
                Log.d(TAG, "Time synchronized successfully on splash")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to synchronize time on splash", e)
                // Continue anyway - time sync will be attempted again when starting matches
            }

            // Continue with authentication checks
            checkAuthAndLoadTeam()
        }
    }

    private suspend fun checkAuthAndLoadTeam() {
        val user = getCurrentUser().first()
        if (user == null) {
            _uiState.value = UiState.NotAuthenticated
        } else {
            checkClubMembership()
        }
    }

    private suspend fun checkClubMembership() {
        val clubMember = getUserClubMembership().first()
        if (clubMember == null) {
            _uiState.value = UiState.NoClub
        } else {
            loadTeam()
        }
    }

    private suspend fun loadTeam() {
        getTeam().collect { team ->
            if (team == null) {
                _uiState.value = UiState.NoTeam
            } else {
                _uiState.value = UiState.TeamExists
            }
        }
    }

    companion object {
        private const val TAG = "SplashViewModel"
    }
}
