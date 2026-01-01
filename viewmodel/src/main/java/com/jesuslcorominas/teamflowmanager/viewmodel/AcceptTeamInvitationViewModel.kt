package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.navigation.Route
import com.jesuslcorominas.teamflowmanager.domain.usecase.AcceptTeamInvitationUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetCurrentUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AcceptTeamInvitationViewModel(
    savedStateHandle: SavedStateHandle,
    private val acceptTeamInvitation: AcceptTeamInvitationUseCase,
    private val getCurrentUser: GetCurrentUserUseCase,
) : ViewModel() {

    private val teamId: String? = savedStateHandle[Route.AcceptTeamInvitation.ARG_TEAM_ID]

    private val _state = MutableStateFlow<AcceptTeamInvitationState>(AcceptTeamInvitationState.Loading)
    val state: StateFlow<AcceptTeamInvitationState> = _state.asStateFlow()

    init {
        processInvitation()
    }

    fun processInvitation() {
        viewModelScope.launch {
            try {
                _state.value = AcceptTeamInvitationState.Loading

                // Validate teamId
                if (teamId.isNullOrBlank()) {
                    _state.value = AcceptTeamInvitationState.Error("Invalid invitation link")
                    return@launch
                }

                // Check if user is authenticated
                val currentUser = getCurrentUser().first()
                if (currentUser == null) {
                    _state.value = AcceptTeamInvitationState.NotAuthenticated(teamId)
                    return@launch
                }

                // Accept the invitation
                val team = acceptTeamInvitation(teamId)
                _state.value = AcceptTeamInvitationState.Success(team)
            } catch (e: Exception) {
                _state.value = AcceptTeamInvitationState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun retry() {
        processInvitation()
    }
}

sealed class AcceptTeamInvitationState {
    data object Loading : AcceptTeamInvitationState()
    data class NotAuthenticated(val teamId: String) : AcceptTeamInvitationState()
    data class Success(val team: Team) : AcceptTeamInvitationState()
    data class Error(val message: String) : AcceptTeamInvitationState()
}
