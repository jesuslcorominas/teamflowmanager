package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.model.ClubMember
import com.jesuslcorominas.teamflowmanager.domain.model.ClubRole
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetClubMembersUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetUserClubMembershipUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.RemoveClubMemberUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ClubMembersViewModel(
    private val getClubMembers: GetClubMembersUseCase,
    private val getUserClubMembership: GetUserClubMembershipUseCase,
    private val removeClubMember: RemoveClubMemberUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    sealed interface UiState {
        data object Loading : UiState

        data class Success(
            val members: List<ClubMember>,
            val currentUserId: String,
            val currentUserIsPresident: Boolean,
            val clubFirestoreId: String,
        ) : UiState

        data object Error : UiState

        data object NoClubMembership : UiState
    }

    init {
        loadMembers()
    }

    fun expelMember(
        userId: String,
        clubId: String,
    ) {
        viewModelScope.launch {
            try {
                removeClubMember(userId, clubId)
            } catch (e: Exception) {
                _uiState.value = UiState.Error
            }
        }
    }

    private fun loadMembers() {
        viewModelScope.launch {
            try {
                // Get user's club membership
                val clubMember = getUserClubMembership().first()
                val clubId = clubMember?.clubFirestoreId

                if (clubMember == null || clubId == null) {
                    _uiState.value = UiState.NoClubMembership
                    return@launch
                }

                val currentUserId = clubMember.userId
                val isPresident = clubMember.hasRole(ClubRole.PRESIDENT)

                // Load members for the club
                getClubMembers(clubId).collect { members ->
                    _uiState.value =
                        UiState.Success(
                            members = members,
                            currentUserId = currentUserId,
                            currentUserIsPresident = isPresident,
                            clubFirestoreId = clubId,
                        )
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error
            }
        }
    }
}
