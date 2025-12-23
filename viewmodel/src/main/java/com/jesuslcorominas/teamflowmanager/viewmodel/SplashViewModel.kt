package com.jesuslcorominas.teamflowmanager.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetCurrentUserUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetTeamUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetUserClubMembershipUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SynchronizeTimeUseCase
import kotlinx.coroutines.Job
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
    
    private var startupJob: Job? = null

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

    fun refresh() {
        Log.d(TAG, "Refresh called - cancelling previous job and restarting")
        startupJob?.cancel()
        _uiState.value = UiState.Loading
        performStartupTasks()
    }

    private fun performStartupTasks() {
        startupJob = viewModelScope.launch {
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
        Log.d(TAG, "Checking club membership...")
        val clubMember = getUserClubMembership().first()
        Log.d(TAG, "Club membership result: ${if (clubMember == null) "NO CLUB MEMBERSHIP" else "HAS CLUB MEMBERSHIP (clubId: ${clubMember.clubId}, role: ${clubMember.role})"}")
        if (clubMember == null) {
            Log.d(TAG, "Setting state to NoClub")
            _uiState.value = UiState.NoClub
        } else {
            Log.d(TAG, "User has club membership, checking team...")
            loadTeam()
        }
    }

    private suspend fun loadTeam() {
        Log.d(TAG, "Loading team...")
        val team = getTeam().first()
        Log.d(TAG, "Team result: ${if (team == null) "NO TEAM" else "HAS TEAM (id: ${team.id}, name: ${team.name})"}")
        if (team == null) {
            Log.d(TAG, "Setting state to NoTeam")
            _uiState.value = UiState.NoTeam
        } else {
            Log.d(TAG, "Setting state to TeamExists")
            _uiState.value = UiState.TeamExists
        }
    }

    companion object {
        private const val TAG = "SplashViewModel"
    }
}
