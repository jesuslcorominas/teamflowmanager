package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetClubByIdUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetUserClubMembershipUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.RegenerateInvitationCodeUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.UpdateClubUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ClubSettingsViewModel(
    private val getUserClubMembership: GetUserClubMembershipUseCase,
    private val getClubById: GetClubByIdUseCase,
    private val updateClubUseCase: UpdateClubUseCase,
    private val regenerateInvitationCodeUseCase: RegenerateInvitationCodeUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var clubId: String? = null

    // Original values to restore on cancel
    private var savedName: String = ""
    private var savedHomeGround: String = ""

    data class UiState(
        val name: String = "",
        val homeGround: String = "",
        val invitationCode: String = "",
        val loading: Boolean = true,
        val isEditing: Boolean = false,
        val saving: Boolean = false,
        val saved: Boolean = false,
        val regenerating: Boolean = false,
        val showExitDialog: Boolean = false,
        val error: String? = null,
    )

    init {
        loadClub()
    }

    private fun loadClub() {
        viewModelScope.launch {
            try {
                val member = getUserClubMembership().first()
                val remoteId = member?.clubRemoteId
                if (remoteId == null) {
                    _uiState.value =
                        _uiState.value.copy(
                            loading = false,
                            error = "No club membership found",
                        )
                    return@launch
                }
                clubId = remoteId
                val club = getClubById(remoteId)
                if (club == null) {
                    _uiState.value =
                        _uiState.value.copy(
                            loading = false,
                            error = "Club not found",
                        )
                    return@launch
                }
                savedName = club.name
                savedHomeGround = club.homeGround ?: ""
                _uiState.value =
                    _uiState.value.copy(
                        name = club.name,
                        homeGround = club.homeGround ?: "",
                        invitationCode = club.invitationCode,
                        loading = false,
                    )
            } catch (e: Exception) {
                _uiState.value =
                    _uiState.value.copy(
                        loading = false,
                        error = e.message,
                    )
            }
        }
    }

    fun onEnterEdit() {
        _uiState.value = _uiState.value.copy(isEditing = true)
    }

    fun onCancelEdit() {
        val hasChanges =
            _uiState.value.name != savedName ||
                _uiState.value.homeGround != savedHomeGround
        if (hasChanges) {
            _uiState.value = _uiState.value.copy(showExitDialog = true)
        } else {
            _uiState.value = _uiState.value.copy(isEditing = false)
        }
    }

    fun onConfirmExit() {
        _uiState.value =
            _uiState.value.copy(
                name = savedName,
                homeGround = savedHomeGround,
                isEditing = false,
                showExitDialog = false,
                error = null,
            )
    }

    fun onDismissExitDialog() {
        _uiState.value = _uiState.value.copy(showExitDialog = false)
    }

    fun onNameChange(value: String) {
        _uiState.value = _uiState.value.copy(name = value, error = null)
    }

    fun onHomeGroundChange(value: String) {
        _uiState.value = _uiState.value.copy(homeGround = value, error = null)
    }

    fun onSave() {
        val id = clubId ?: return
        val name = _uiState.value.name.trim()
        if (name.isBlank()) return

        val homeGround = _uiState.value.homeGround.trim().takeIf { it.isNotEmpty() }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(saving = true, error = null)
            try {
                val updated = updateClubUseCase(id, name, homeGround)
                savedName = updated.name
                savedHomeGround = updated.homeGround ?: ""
                _uiState.value =
                    _uiState.value.copy(
                        name = updated.name,
                        homeGround = updated.homeGround ?: "",
                        saving = false,
                        saved = true,
                        isEditing = false,
                    )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(saving = false, error = e.message)
            }
        }
    }

    fun resetSavedState() {
        _uiState.value = _uiState.value.copy(saved = false)
    }

    fun onRegenerateCode() {
        val id = clubId ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(regenerating = true, error = null)
            try {
                val newCode = regenerateInvitationCodeUseCase(id)
                _uiState.value =
                    _uiState.value.copy(
                        invitationCode = newCode,
                        regenerating = false,
                    )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(regenerating = false, error = e.message)
            }
        }
    }
}
