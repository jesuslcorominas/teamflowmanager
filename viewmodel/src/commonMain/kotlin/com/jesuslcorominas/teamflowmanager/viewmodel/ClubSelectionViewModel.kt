package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.usecase.SignOutUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ClubSelectionViewModel(
    private val signOutUseCase: SignOutUseCase,
) : ViewModel() {
    private val _signOutComplete = MutableStateFlow(false)
    val signOutComplete: StateFlow<Boolean> = _signOutComplete.asStateFlow()

    fun signOut() {
        viewModelScope.launch {
            runCatching { signOutUseCase() }
            _signOutComplete.value = true
        }
    }

    fun clearSignOutComplete() {
        _signOutComplete.value = false
    }
}
