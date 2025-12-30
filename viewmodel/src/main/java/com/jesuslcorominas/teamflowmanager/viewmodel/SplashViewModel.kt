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
    private val getUserClubMembership: GetUserClubMembershipUseCase,
    private val synchronizeTimeUseCase: SynchronizeTimeUseCase,
) : ViewModel() {

    companion object {
        private const val TAG = "SplashViewModel"
        private const val ROLE_PRESIDENT = "Presidente"
    }

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

        data object ClubPresident : UiState
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
            Log.d(TAG, "User authenticated, loading team...")
            loadTeam()
        }
    }

    private suspend fun loadTeam() {
        Log.d(TAG, "Loading team...")
        val team = getTeam().first()
        
        if (team == null) {
            Log.d(TAG, "NO TEAM found - checking club membership")
            // Check if user is a club member (particularly a President)
            val clubMember = getUserClubMembership().first()
            
            if (clubMember != null) {
                Log.d(TAG, "User is club member with role: ${clubMember.role}")
                // User is a club member without their own team
                // If they're a President, they can create teams for the club
                if (clubMember.role == ROLE_PRESIDENT) {
                    Log.d(TAG, "User is President - navigating to team creation")
                    _uiState.value = UiState.ClubPresident
                } else {
                    Log.d(TAG, "User is not President - navigating to club selection")
                    _uiState.value = UiState.NoClub
                }
            } else {
                Log.d(TAG, "User is not a club member - navigating to club selection")
                _uiState.value = UiState.NoClub
            }
        } else {
            Log.d(TAG, "TEAM found (id: ${team.id}, name: ${team.name}, clubId: ${team.clubId}, clubFirestoreId: ${team.clubFirestoreId})")
            
            // Check if team has a club
            val hasClub = team.clubId != null || team.clubFirestoreId != null
            
            if (hasClub) {
                Log.d(TAG, "Team HAS club - navigating to matches")
                _uiState.value = UiState.TeamExists
            } else {
                Log.d(TAG, "Team DOES NOT have club - navigating to club selection")
                _uiState.value = UiState.NoClub
            }
        }
    }
}
