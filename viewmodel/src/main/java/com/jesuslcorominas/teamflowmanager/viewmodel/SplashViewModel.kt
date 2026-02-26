package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.model.ClubRole
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
        startupJob?.cancel()
        _uiState.value = UiState.Loading
        performStartupTasks()
    }

    private fun performStartupTasks() {
        startupJob = viewModelScope.launch {
            // Synchronize time with server on app startup
            try {
                synchronizeTimeUseCase()
            } catch (_: Exception) {
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
            loadTeam()
        }
    }

    private suspend fun loadTeam() {
        // First, check if user is a President - Presidents should always see team list
        val clubMember = getUserClubMembership().first()
        if (clubMember != null) {
            if (clubMember.hasRole(ClubRole.PRESIDENT)) {
                _uiState.value = UiState.ClubPresident
                return
            }
        }

        // For non-Presidents, check if they have their own team
        val team = getTeam().first()

        if (team == null) {
            if (clubMember != null) {
                _uiState.value = UiState.NoClub
            } else {
                _uiState.value = UiState.NoClub
            }
        } else {
            // Check if team has a club
            val hasClub = team.clubId != null || team.clubFirestoreId != null

            if (hasClub) {
                _uiState.value = UiState.TeamExists
            } else {
                _uiState.value = UiState.NoClub
            }
        }
    }
}
