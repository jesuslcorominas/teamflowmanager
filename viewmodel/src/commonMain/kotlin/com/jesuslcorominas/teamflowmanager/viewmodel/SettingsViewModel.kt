package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker
import com.jesuslcorominas.teamflowmanager.domain.model.User
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetCurrentUserUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SignOutUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val analyticsTracker: AnalyticsTracker,
) : ViewModel() {
    val currentUser: StateFlow<User?> =
        getCurrentUserUseCase()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _signOutComplete = MutableStateFlow(false)
    val signOutComplete: StateFlow<Boolean> = _signOutComplete.asStateFlow()

    fun signOut() {
        viewModelScope.launch {
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
