package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.usecase.GenerateTeamInvitationUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetTeamsByClubUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetUserClubMembershipUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class TeamListViewModel(
    private val getTeamsByClub: GetTeamsByClubUseCase,
    private val getUserClubMembership: GetUserClubMembershipUseCase,
    private val generateTeamInvitation: GenerateTeamInvitationUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _shareEvent = MutableStateFlow<ShareEvent?>(null)
    val shareEvent: StateFlow<ShareEvent?> = _shareEvent.asStateFlow()

    sealed interface UiState {
        data object Loading : UiState
        data class Success(val teams: List<Team>, val clubName: String) : UiState
        data object Error : UiState
        data object NoClubMembership : UiState
    }

    data class ShareEvent(val invitationLink: String, val teamName: String)

    init {
        loadTeams()
    }

    private fun loadTeams() {
        viewModelScope.launch {
            try {
                // Get user's club membership
                val clubMember = getUserClubMembership().first()
                val clubFirestoreId = clubMember?.clubFirestoreId

                if (clubMember == null || clubFirestoreId == null) {
                    _uiState.value = UiState.NoClubMembership
                    return@launch
                }

                // Load teams for the club
                getTeamsByClub(clubFirestoreId).collect { teams ->
                    _uiState.value = UiState.Success(teams, clubMember.name)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error
            }
        }
    }

    fun shareTeam(team: Team) {
        viewModelScope.launch {
            try {
                val teamFirestoreId = team.coachId ?: return@launch
                val invitationLink = generateTeamInvitation(teamFirestoreId, team.name)
                _shareEvent.value = ShareEvent(invitationLink, team.name)
            } catch (e: Exception) {
                // Handle error silently or show a message
            }
        }
    }

    fun onShareEventConsumed() {
        _shareEvent.value = null
    }
}
