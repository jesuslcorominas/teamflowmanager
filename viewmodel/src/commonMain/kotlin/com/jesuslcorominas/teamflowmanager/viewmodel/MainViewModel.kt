package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.model.ActiveViewRole
import com.jesuslcorominas.teamflowmanager.domain.model.ClubRole
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetActiveViewRoleUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetUserClubMembershipUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.HasNotificationPermissionBeenRequestedUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SetNotificationPermissionRequestedUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainViewModel(
    private val hasNotificationPermissionBeenRequestedUseCase: HasNotificationPermissionBeenRequestedUseCase,
    private val setNotificationPermissionRequestedUseCase: SetNotificationPermissionRequestedUseCase,
    private val getUserClubMembership: GetUserClubMembershipUseCase,
    private val getActiveViewRole: GetActiveViewRoleUseCase,
) : ViewModel() {
    private val _isPresident = MutableStateFlow(false)
    val isPresident: StateFlow<Boolean> = _isPresident.asStateFlow()

    init {
        viewModelScope.launch {
            getUserClubMembership().collect { clubMember ->
                _isPresident.value =
                    clubMember?.hasRole(ClubRole.PRESIDENT) == true &&
                    getActiveViewRole() != ActiveViewRole.Coach
            }
        }
    }

    fun refreshIsPresident() {
        viewModelScope.launch {
            val clubMember = getUserClubMembership().first()
            _isPresident.value =
                clubMember?.hasRole(ClubRole.PRESIDENT) == true &&
                getActiveViewRole() != ActiveViewRole.Coach
        }
    }

    fun hasNotificationPermissionBeenRequested(): Boolean = hasNotificationPermissionBeenRequestedUseCase()

    fun setNotificationPermissionRequested(requested: Boolean) {
        setNotificationPermissionRequestedUseCase(requested)
    }
}
