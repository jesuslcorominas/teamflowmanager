package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.model.ClubRole
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.usecase.GenerateTeamInvitationUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetTeamsByClubUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetUserClubMembershipUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SelfAssignAsCoachUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class TeamListViewModel(
    private val getTeamsByClub: GetTeamsByClubUseCase,
    private val getUserClubMembership: GetUserClubMembershipUseCase,
    private val generateTeamInvitation: GenerateTeamInvitationUseCase,
    private val selfAssignAsCoach: SelfAssignAsCoachUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _shareEvent = MutableStateFlow<ShareEvent?>(null)
    val shareEvent: StateFlow<ShareEvent?> = _shareEvent.asStateFlow()

    private val _sharingTeamId = MutableStateFlow<String?>(null)
    val sharingTeamId: StateFlow<String?> = _sharingTeamId.asStateFlow()

    private val _assigningCoachToTeamId = MutableStateFlow<String?>(null)
    val assigningCoachToTeamId: StateFlow<String?> = _assigningCoachToTeamId.asStateFlow()

    private val _currentUserRole = MutableStateFlow<String?>(null)
    val currentUserRole: StateFlow<String?> = _currentUserRole.asStateFlow()

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

                // Store user's role
                _currentUserRole.value = clubMember.role

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
        // Prevent multiple concurrent share operations for the same team
        if (_sharingTeamId.value == team.firestoreId) {
            return
        }

        viewModelScope.launch {
            val teamFirestoreId = team.firestoreId ?: return@launch

            _sharingTeamId.value = teamFirestoreId

            try {
                val invitationLink = generateTeamInvitation(teamFirestoreId, team.name)
                _shareEvent.value = ShareEvent(invitationLink, team.name)
            } catch (e: Exception) {
                // TODO: Show error to user
            } finally {
                _sharingTeamId.value = null
            }
        }
    }

    fun onShareEventConsumed() {
        _shareEvent.value = null
    }

    fun selfAssignAsCoachToTeam(team: Team) {
        // Prevent multiple concurrent operations for the same team
        if (_assigningCoachToTeamId.value == team.firestoreId) {
            return
        }

        viewModelScope.launch {
            val teamFirestoreId = team.firestoreId ?: return@launch

            _assigningCoachToTeamId.value = teamFirestoreId

            try {
                selfAssignAsCoach(teamFirestoreId)
                // The UI will automatically update through the teams flow
            } catch (e: Exception) {
                // TODO: Show error to user
            } finally {
                _assigningCoachToTeamId.value = null
            }
        }
    }
}
