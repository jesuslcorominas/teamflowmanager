package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.usecase.JoinClubWithInvitationCodeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class JoinClubViewModel(
    private val joinClubWithInvitationCodeUseCase: JoinClubWithInvitationCodeUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<JoinClubUiState>(JoinClubUiState.Initial)
    val uiState: StateFlow<JoinClubUiState> = _uiState.asStateFlow()

    private val _invitationCode = MutableStateFlow("")
    val invitationCode: StateFlow<String> = _invitationCode.asStateFlow()

    fun onInvitationCodeChange(code: String) {
        _invitationCode.value = code.trim().uppercase()
        // Reset error state when user types
        if (_uiState.value is JoinClubUiState.Error) {
            _uiState.value = JoinClubUiState.Initial
        }
    }

    fun joinClub(onSuccess: (Team) -> Unit) {
        val code = _invitationCode.value
        if (code.isBlank()) {
            _uiState.value = JoinClubUiState.Error("El código de invitación no puede estar vacío")
            return
        }

        _uiState.value = JoinClubUiState.Loading

        viewModelScope.launch {
            val result = joinClubWithInvitationCodeUseCase(code)
            result.fold(
                onSuccess = { joinResult ->
                    _uiState.update {
                        JoinClubUiState.Success(
                            clubName = joinResult.clubName,
                            teamName = joinResult.team.name,
                            role = joinResult.clubMember.role
                        )
                    }
                    onSuccess(joinResult.team)
                },
                onFailure = { error ->
                    val errorMessage = when (error) {
                        is JoinClubWithInvitationCodeUseCase.ClubNotFoundException ->
                            "No se encontró ningún club con ese código de invitación"
                        is JoinClubWithInvitationCodeUseCase.NoOrphanTeamsException ->
                            "No tienes equipos disponibles para vincular. Crea un equipo primero."
                        is JoinClubWithInvitationCodeUseCase.TeamUpdateException ->
                            "Error al vincular el equipo al club: ${error.message}"
                        is JoinClubWithInvitationCodeUseCase.ClubMemberUpdateException ->
                            "El equipo se vinculó pero hubo un error al actualizar tu membresía: ${error.message}"
                        else ->
                            "Error al unirse al club: ${error.message ?: "Error desconocido"}"
                    }
                    _uiState.value = JoinClubUiState.Error(errorMessage)
                }
            )
        }
    }

    fun dismissError() {
        _uiState.value = JoinClubUiState.Initial
    }
}

sealed interface JoinClubUiState {
    data object Initial : JoinClubUiState
    data object Loading : JoinClubUiState
    data class Success(
        val clubName: String,
        val teamName: String,
        val role: String
    ) : JoinClubUiState
    data class Error(val message: String) : JoinClubUiState
}
