package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.model.ClubMember
import com.jesuslcorominas.teamflowmanager.domain.model.ClubRole
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.usecase.AssignCoachToTeamUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.ClearTeamCoachUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.CreatePendingCoachAssignmentUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.DeletePendingCoachAssignmentUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GenerateTeamInvitationUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetClubMembersUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetMatchesByTeamUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetTeamsByClubUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetUserClubMembershipUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SelfAssignAsCoachUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TeamMatchInfo(
    val currentMatch: Match?,
    val nextMatch: Match?,
)

class TeamListViewModel(
    private val getTeamsByClub: GetTeamsByClubUseCase,
    private val getUserClubMembership: GetUserClubMembershipUseCase,
    private val generateTeamInvitation: GenerateTeamInvitationUseCase,
    private val selfAssignAsCoach: SelfAssignAsCoachUseCase,
    private val assignCoachToTeam: AssignCoachToTeamUseCase,
    private val clearTeamCoachUseCase: ClearTeamCoachUseCase,
    private val getClubMembers: GetClubMembersUseCase,
    private val getMatchesByTeam: GetMatchesByTeamUseCase,
    private val createPendingCoachAssignment: CreatePendingCoachAssignmentUseCase,
    private val deletePendingCoachAssignment: DeletePendingCoachAssignmentUseCase,
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

    // Raw (unfiltered) team list — backing store for both display and match status
    private val allTeamsCache = MutableStateFlow<List<Team>>(emptyList())

    // Set of userIds currently assigned as coach to any team — used to filter assignable members
    val assignedCoachIds: StateFlow<Set<String>> =
        allTeamsCache
            .map { teams -> teams.mapNotNull { it.coachId }.toSet() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    private val _matchStatusByTeam = MutableStateFlow<Map<String, TeamMatchInfo>>(emptyMap())
    val matchStatusByTeam: StateFlow<Map<String, TeamMatchInfo>> = _matchStatusByTeam.asStateFlow()

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
                val clubRemoteId = clubMember?.clubRemoteId

                if (clubMember == null || clubRemoteId == null) {
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
                    getClubMembers(clubRemoteId).collect { members ->
                        _clubMembers.value = members
                    }
                }

                // Collect raw teams into allTeamsCache
                launch {
                    getTeamsByClub(clubRemoteId).collect { teams ->
                        allTeamsCache.value = teams
                    }
                }

                // Load teams for the club, applying search and filter reactively
                combine(
                    allTeamsCache,
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

        // Reactively subscribe to match status for all teams
        viewModelScope.launch {
            allTeamsCache
                .flatMapLatest { teams ->
                    val teamsWithId = teams.filter { !it.remoteId.isNullOrBlank() }
                    if (teamsWithId.isEmpty()) return@flatMapLatest flowOf(emptyMap())
                    combine(
                        teamsWithId.map { team ->
                            getMatchesByTeam(team.remoteId!!)
                                .map { matches ->
                                    val current =
                                        matches.firstOrNull {
                                            it.status == MatchStatus.IN_PROGRESS ||
                                                it.status == MatchStatus.PAUSED
                                        }
                                    val next =
                                        matches
                                            .filter { it.status == MatchStatus.SCHEDULED }
                                            .minByOrNull { it.dateTime ?: Long.MAX_VALUE }
                                    team.remoteId!! to TeamMatchInfo(current, next)
                                }
                        },
                    ) { pairs -> pairs.toMap() }
                }
                .catch { emit(emptyMap()) }
                .collect { _matchStatusByTeam.value = it }
        }
    }

    fun shareTeam(team: Team) {
        if (_sharingTeamId.value == team.remoteId) return

        viewModelScope.launch {
            val teamFirestoreId = team.remoteId ?: return@launch
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
        val teamId = team.remoteId ?: return

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
        val teamId = team.remoteId ?: return
        val trimmedEmail = email.trim()
        val existingMember =
            _clubMembers.value.firstOrNull {
                it.email.equals(trimmedEmail, ignoreCase = true)
            }
        viewModelScope.launch {
            _assigningCoachToTeamId.value = teamId
            try {
                if (existingMember != null) {
                    // Member already in the club — assign directly
                    assignCoachToTeam(teamId, existingMember.userId)
                } else {
                    // External email — create pending assignment (#307)
                    createPendingCoachAssignment(teamId, trimmedEmail)
                }
                _assignCoachDialogTeam.value = null
                _assignCoachError.value = null
            } catch (e: Exception) {
                _assignCoachError.value = e.message
            } finally {
                _assigningCoachToTeamId.value = null
            }
        }
    }

    fun removeCoach(team: Team) {
        val teamId = team.remoteId ?: return
        viewModelScope.launch {
            try {
                clearTeamCoachUseCase(teamId)
            } catch (e: Exception) {
                // TODO: Show error to user
            }
        }
    }

    fun deletePendingAssignment(team: Team) {
        val teamId = team.remoteId ?: return
        viewModelScope.launch {
            try {
                deletePendingCoachAssignment(teamId)
            } catch (e: Exception) {
                // TODO: Show error to user
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
        if (_assigningCoachToTeamId.value == team.remoteId) return

        viewModelScope.launch {
            val teamFirestoreId = team.remoteId ?: return@launch
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
