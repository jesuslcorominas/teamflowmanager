package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.model.ClubMember
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetClubMembersUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetUserClubMembershipUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ClubMembersViewModel(
    private val getClubMembers: GetClubMembersUseCase,
    private val getUserClubMembership: GetUserClubMembershipUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    sealed interface UiState {
        data object Loading : UiState
        data class Success(val members: List<ClubMember>) : UiState
        data object Error : UiState
        data object NoClubMembership : UiState
    }

    init {
        loadMembers()
    }

    private fun loadMembers() {
        viewModelScope.launch {
            try {
                // Get user's club membership
                val clubMember = getUserClubMembership().first()
                val clubFirestoreId = clubMember?.clubFirestoreId

                if (clubMember == null || clubFirestoreId == null) {
                    _uiState.value = UiState.NoClubMembership
                    return@launch
                }

                // Load members for the club
                getClubMembers(clubFirestoreId).collect { members ->
                    _uiState.value = UiState.Success(members)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error
            }
        }
    }
}
