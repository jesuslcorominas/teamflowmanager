package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker
import com.jesuslcorominas.teamflowmanager.domain.model.ActiveViewRole
import com.jesuslcorominas.teamflowmanager.domain.model.ClubRole
import com.jesuslcorominas.teamflowmanager.domain.model.User
import com.jesuslcorominas.teamflowmanager.domain.usecase.DeleteFcmTokenUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetActiveViewRoleUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetCurrentUserUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetTeamUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetUserClubMembershipUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SetActiveViewRoleUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SignOutUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val deleteFcmTokenUseCase: DeleteFcmTokenUseCase,
    private val analyticsTracker: AnalyticsTracker,
    private val getTeam: GetTeamUseCase,
    private val getUserClubMembership: GetUserClubMembershipUseCase,
    private val getActiveViewRole: GetActiveViewRoleUseCase,
    private val setActiveViewRole: SetActiveViewRoleUseCase,
) : ViewModel() {
    val currentUser: StateFlow<User?> =
        getCurrentUserUseCase()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _signOutComplete = MutableStateFlow(false)
    val signOutComplete: StateFlow<Boolean> = _signOutComplete.asStateFlow()

    private val _roleSelectorState = MutableStateFlow(RoleSelectorState())
    val roleSelectorState: StateFlow<RoleSelectorState> = _roleSelectorState.asStateFlow()

    data class RoleSelectorState(
        val showRoleSelector: Boolean = false,
        val isRoleSelectorEnabled: Boolean = false,
        val activeRole: ActiveViewRole = ActiveViewRole.President,
        val roleChangedEvent: Boolean = false,
    )

    init {
        loadRoleSelectorState()
    }

    private fun loadRoleSelectorState() {
        viewModelScope.launch {
            val clubMember = getUserClubMembership().first()
            val isPresident = clubMember != null && clubMember.hasRole(ClubRole.PRESIDENT)
            if (!isPresident) return@launch

            val team = getTeam().first()

            _roleSelectorState.value =
                RoleSelectorState(
                    showRoleSelector = true,
                    isRoleSelectorEnabled = team != null,
                    activeRole = getActiveViewRole(),
                )
        }
    }

    fun onRoleSelected(role: ActiveViewRole) {
        setActiveViewRole(role)
        _roleSelectorState.value =
            _roleSelectorState.value.copy(
                activeRole = role,
                roleChangedEvent = true,
            )
    }

    fun onRoleChangedEventConsumed() {
        _roleSelectorState.value = _roleSelectorState.value.copy(roleChangedEvent = false)
    }

    fun signOut() {
        viewModelScope.launch {
            val user = getCurrentUserUseCase().first()
            if (user != null) {
                runCatching { deleteFcmTokenUseCase(user.id) }
            }
            signOutUseCase()
            analyticsTracker.logEvent("logout", emptyMap())
            analyticsTracker.setUserId(null)
            _signOutComplete.value = true
        }
    }

    fun clearSignOutComplete() {
        _signOutComplete.value = false
    }
}
