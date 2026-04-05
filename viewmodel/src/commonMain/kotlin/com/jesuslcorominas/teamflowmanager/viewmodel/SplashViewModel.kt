package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.model.ClubRole
import com.jesuslcorominas.teamflowmanager.domain.model.User
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetCurrentUserUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetTeamUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetUserClubMembershipUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.IsNotificationPermissionGrantedUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SyncFcmTokenUseCase
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
    private val syncFcmTokenUseCase: SyncFcmTokenUseCase,
    private val isNotificationPermissionGranted: IsNotificationPermissionGrantedUseCase,
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
        startupJob =
            viewModelScope.launch {
                try {
                    synchronizeTimeUseCase()
                } catch (_: Exception) {
                    // Continue anyway - time sync will be attempted again when starting matches
                }
                checkAuthAndLoadTeam()
            }
    }

    private suspend fun checkAuthAndLoadTeam() {
        val user = getCurrentUser().first()
        if (user == null) {
            _uiState.value = UiState.NotAuthenticated
        } else {
            loadTeam(user)
        }
    }

    private suspend fun loadTeam(user: User) {
        val clubMember = getUserClubMembership().first()
        if (clubMember != null) {
            if (clubMember.hasRole(ClubRole.PRESIDENT)) {
                syncFcmTokenIfPermitted(user.id, clubMember.clubFirestoreId)
                _uiState.value = UiState.ClubPresident
                return
            }
        }

        val team = getTeam().first()

        if (team == null) {
            if (clubMember == null) {
                // No club membership and no team: covers expelled members and new users
                // who never completed onboarding. Send them to club selection so they
                // can join or create a club.
                _uiState.value = UiState.NoClub
            } else {
                // Member belongs to a club but has no team assigned yet — show waiting screen.
                _uiState.value = UiState.NoTeam
            }
        } else {
            val clubFirestoreId = team.clubFirestoreId
            if (clubFirestoreId != null) {
                syncFcmTokenIfPermitted(user.id, clubFirestoreId)
                _uiState.value = UiState.TeamExists
            } else {
                _uiState.value = UiState.NoClub
            }
        }
    }

    private fun syncFcmTokenIfPermitted(
        userId: String,
        clubFirestoreId: String?,
    ) {
        if (!isNotificationPermissionGranted()) return
        viewModelScope.launch {
            runCatching { syncFcmTokenUseCase(userId, "android", clubFirestoreId) }
        }
    }
}
