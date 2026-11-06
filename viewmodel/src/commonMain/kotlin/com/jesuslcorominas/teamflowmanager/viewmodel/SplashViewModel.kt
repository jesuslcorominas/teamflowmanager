package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.model.ActiveViewRole
import com.jesuslcorominas.teamflowmanager.domain.model.ClubRole
import com.jesuslcorominas.teamflowmanager.domain.model.User
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetActiveViewRoleUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetCurrentUserUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetTeamUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetUserClubMembershipUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.IsNotificationPermissionGrantedUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.RecoverAbandonedMatchesUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SyncFcmTokenUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SynchronizeTimeUseCase
import com.jesuslcorominas.teamflowmanager.viewmodel.utils.AppCoroutineScope
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
    private val getActiveViewRole: GetActiveViewRoleUseCase,
    private val recoverAbandonedMatches: RecoverAbandonedMatchesUseCase,
    private val appScope: AppCoroutineScope,
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var startupJob: Job? = null
    private var abandonedMatchSweepLaunched = false

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
                sweepAbandonedMatches()
            }
    }

    /**
     * Finishes the matches a dead process left running. A cold start is the only moment one can be
     * observed — the app was not there to notice — so the splash is where the trigger belongs,
     * after the clock is synchronised and only once the user turns out to be authenticated.
     *
     * The splash decides *when*, not *for how long*. The sweep runs on [appScope], not on
     * `viewModelScope`, because this ViewModel is about to be destroyed: the state emission that
     * gets us here is the same one that wakes the splash up to navigate, and every route out pops
     * the splash off the back stack. On `viewModelScope` the sweep died at its first suspension —
     * the read of the matches — so it would barely ever get as far as finding one to recover, and
     * silently, since cancellation is not reported. Worse, if it did get further, the window it
     * could be cut in was the one between the finish and the emptying of the queue, which is
     * precisely the state the order of those two calls exists to prevent.
     *
     * Fire and forget: nothing in the splash waits on it, and a failure must never cost the user
     * their navigation. The guard keeps a [refresh] from sweeping twice.
     */
    private fun sweepAbandonedMatches() {
        if (abandonedMatchSweepLaunched) return
        val state = _uiState.value
        if (state == UiState.NotAuthenticated || state == UiState.LocalDataNeedsAuth) return
        abandonedMatchSweepLaunched = true
        appScope.launch {
            runCatching { recoverAbandonedMatches() }
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

        if (clubMember != null && clubMember.hasRole(ClubRole.PRESIDENT)) {
            val team = getTeam().first()
            if (team != null) {
                // President who is also assigned as coach to a team — respect the role preference
                when (getActiveViewRole()) {
                    ActiveViewRole.Coach -> {
                        val clubId = team.clubId
                        if (clubId != null) {
                            syncFcmTokenIfPermitted(user.id, clubId)
                            _uiState.value = UiState.TeamExists
                        } else {
                            _uiState.value = UiState.ClubPresident
                        }
                    }
                    ActiveViewRole.President -> {
                        syncFcmTokenIfPermitted(user.id, clubMember.clubId)
                        _uiState.value = UiState.ClubPresident
                    }
                }
            } else {
                // President with no team assigned as coach — always show president view
                syncFcmTokenIfPermitted(user.id, clubMember.clubId)
                _uiState.value = UiState.ClubPresident
            }
            return
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
            val clubId = team.clubId
            if (clubId != null) {
                syncFcmTokenIfPermitted(user.id, clubId)
                _uiState.value = UiState.TeamExists
            } else {
                _uiState.value = UiState.NoClub
            }
        }
    }

    private fun syncFcmTokenIfPermitted(
        userId: String,
        clubRemoteId: String?,
    ) {
        if (!isNotificationPermissionGranted()) return
        viewModelScope.launch {
            runCatching { syncFcmTokenUseCase(userId, "android", clubRemoteId) }
        }
    }
}
