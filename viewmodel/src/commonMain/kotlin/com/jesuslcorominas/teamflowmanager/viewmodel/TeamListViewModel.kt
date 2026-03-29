package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.model.ClubMember
import com.jesuslcorominas.teamflowmanager.domain.model.ClubRole
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.usecase.AssignCoachToTeamUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GenerateTeamInvitationUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetClubMembersUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetTeamsByClubUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetUserClubMembershipUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SelfAssignAsCoachUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class TeamListViewModel(
    private val getTeamsByClub: GetTeamsByClubUseCase,
    private val getUserClubMembership: GetUserClubMembershipUseCase,
    private val generateTeamInvitation: GenerateTeamInvitationUseCase,
    private val selfAssignAsCoach: SelfAssignAsCoachUseCase,
    private val assignCoachToTeam: AssignCoachToTeamUseCase,
    private val getClubMembers: GetClubMembersUseCase,
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

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _coachFilter = MutableStateFlow(CoachFilter.ALL)
    val coachFilter: StateFlow<CoachFilter> = _coachFilter.asStateFlow()

    private val _assignCoachDialogTeam = MutableStateFlow<Team?>(null)
    val assignCoachDialogTeam: StateFlow<Team?> = _assignCoachDialogTeam.asStateFlow()

    private val _clubMembers = MutableStateFlow<List<ClubMember>>(emptyList())
    val clubMembers: StateFlow<List<ClubMember>> = _clubMembers.asStateFlow()

    private val _assignCoachError = MutableStateFlow<String?>(null)
    val assignCoachError: StateFlow<String?> = _assignCoachError.asStateFlow()

    enum class CoachFilter { ALL, WITH_COACH, WITHOUT_COACH }

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
                val clubMember = getUserClubMembership().first()
                val clubFirestoreId = clubMember?.clubFirestoreId

                if (clubMember == null || clubFirestoreId == null) {
                    _uiState.value = UiState.NoClubMembership
                    return@launch
                }

                _currentUserRole.value =
                    if (clubMember.hasRole(ClubRole.PRESIDENT)) {
                        ClubRole.PRESIDENT.roleName
                    } else {
                        clubMember.roles.firstOrNull() ?: ""
                    }

                // Load club members in background for the assign coach dialog
                launch {
                    getClubMembers(clubFirestoreId).collect { members ->
                        _clubMembers.value = members
                    }
                }

                // Load teams for the club, applying search and filter reactively
                combine(
                    getTeamsByClub(clubFirestoreId),
                    _searchQuery,
                    _coachFilter,
                ) { teams, query, coachFilter ->
                    val filtered =
                        teams
                            .filter { query.isBlank() || it.name.contains(query, ignoreCase = true) }
                            .filter {
                                when (coachFilter) {
                                    CoachFilter.ALL -> true
                                    CoachFilter.WITH_COACH -> it.coachId != null
                                    CoachFilter.WITHOUT_COACH -> it.coachId == null
                                }
                            }
                    UiState.Success(filtered, clubMember.name)
                }.collect { state ->
                    _uiState.value = state
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error
            }
        }
    }

    fun shareTeam(team: Team) {
        if (_sharingTeamId.value == team.firestoreId) return

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

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onCoachFilterChanged(filter: CoachFilter) {
        _coachFilter.value = filter
    }

    fun requestAssignCoach(team: Team) {
        _assignCoachError.value = null
        _assignCoachDialogTeam.value = team
    }

    fun dismissAssignCoachDialog() {
        _assignCoachDialogTeam.value = null
        _assignCoachError.value = null
    }

    fun assignCoachByMember(member: ClubMember) {
        val team = _assignCoachDialogTeam.value ?: return
        val teamId = team.firestoreId ?: return

        viewModelScope.launch {
            _assigningCoachToTeamId.value = teamId
            try {
                assignCoachToTeam(teamId, member.userId)
                _assignCoachDialogTeam.value = null
                _assignCoachError.value = null
            } catch (e: Exception) {
                _assignCoachError.value = e.message
            } finally {
                _assigningCoachToTeamId.value = null
            }
        }
    }

    fun assignCoachByEmail(email: String) {
        val team = _assignCoachDialogTeam.value ?: return
        val teamId = team.firestoreId ?: return
        val member =
            _clubMembers.value.firstOrNull {
                it.email.equals(email.trim(), ignoreCase = true)
            }
        if (member == null) {
            _assignCoachError.value = "NO_MEMBER"
            return
        }
        viewModelScope.launch {
            _assigningCoachToTeamId.value = teamId
            try {
                assignCoachToTeam(teamId, member.userId)
                _assignCoachDialogTeam.value = null
                _assignCoachError.value = null
            } catch (e: Exception) {
                _assignCoachError.value = e.message
            } finally {
                _assigningCoachToTeamId.value = null
            }
        }
    }

    fun clearAssignCoachError() {
        _assignCoachError.value = null
    }

    fun onShareEventConsumed() {
        _shareEvent.value = null
    }

    fun selfAssignAsCoachToTeam(team: Team) {
        if (_assigningCoachToTeamId.value == team.firestoreId) return

        viewModelScope.launch {
            val teamFirestoreId = team.firestoreId ?: return@launch
            _assigningCoachToTeamId.value = teamFirestoreId
            try {
                selfAssignAsCoach(teamFirestoreId)
            } catch (e: Exception) {
                // TODO: Show error to user
            } finally {
                _assigningCoachToTeamId.value = null
            }
        }
    }
}