package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.model.ActiveViewRole
import com.jesuslcorominas.teamflowmanager.domain.model.ClubRole
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetUserClubMembershipUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.HasNotificationPermissionBeenRequestedUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.ObserveActiveViewRoleUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SetNotificationPermissionRequestedUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class MainViewModel(
    private val hasNotificationPermissionBeenRequestedUseCase: HasNotificationPermissionBeenRequestedUseCase,
    private val setNotificationPermissionRequestedUseCase: SetNotificationPermissionRequestedUseCase,
    private val getUserClubMembership: GetUserClubMembershipUseCase,
    private val observeActiveViewRole: ObserveActiveViewRoleUseCase,
) : ViewModel() {
    /**
     * Whether the app shell shows the president navigation.
     *
     * Combining both sources is what makes a role switch take effect immediately: reading the role
     * once inside the membership collector left this stale, because switching role writes to local
     * storage and the membership flow never re-emits for it.
     */
    val isPresident: StateFlow<Boolean> =
        combine(
            getUserClubMembership(),
            observeActiveViewRole(),
        ) { clubMember, activeRole ->
            clubMember?.hasRole(ClubRole.PRESIDENT) == true && activeRole != ActiveViewRole.Coach
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun hasNotificationPermissionBeenRequested(): Boolean = hasNotificationPermissionBeenRequestedUseCase()

    fun setNotificationPermissionRequested(requested: Boolean) {
        setNotificationPermissionRequestedUseCase(requested)
    }
}
