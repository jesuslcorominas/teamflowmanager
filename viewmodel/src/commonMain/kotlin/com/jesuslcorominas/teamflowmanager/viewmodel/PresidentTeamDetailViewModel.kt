package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.model.GlobalNotificationState
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.domain.model.NotificationEventType
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetMatchesByTeamUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetNotificationPreferencesUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetPlayersByTeamUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetTeamByIdUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetUserClubMembershipUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.UpdateTeamNotificationPreferenceUseCase
import com.jesuslcorominas.teamflowmanager.viewmodel.utils.TimeTicker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class PresidentTeamTab { SUMMARY, PLAYERS, MATCHES, STATS, NOTIFICATIONS }

data class PresidentTeamStats(
    val totalMatches: Int,
    val wins: Int,
    val draws: Int,
    val losses: Int,
    val goalsScored: Int,
    val goalsConceded: Int,
    val squadSize: Int,
)

sealed interface PresidentTeamDetailUiState {
    data object Loading : PresidentTeamDetailUiState

    data class Ready(
        val team: Team,
        val players: List<Player>,
        val matches: List<Match>,
        val stats: PresidentTeamStats,
    ) : PresidentTeamDetailUiState

    data object Error : PresidentTeamDetailUiState
}

class PresidentTeamDetailViewModel(
    private val teamId: String,
    private val getTeamById: GetTeamByIdUseCase,
    private val getPlayersByTeam: GetPlayersByTeamUseCase,
    private val getMatchesByTeam: GetMatchesByTeamUseCase,
    private val getNotificationPreferences: GetNotificationPreferencesUseCase,
    private val updateTeamNotificationPreference: UpdateTeamNotificationPreferenceUseCase,
    private val getUserClubMembership: GetUserClubMembershipUseCase,
    private val timeTicker: TimeTicker,
) : ViewModel() {
    private val _uiState = MutableStateFlow<PresidentTeamDetailUiState>(PresidentTeamDetailUiState.Loading)
    val uiState: StateFlow<PresidentTeamDetailUiState> = _uiState.asStateFlow()

    private val _selectedTab = MutableStateFlow(PresidentTeamTab.SUMMARY)
    val selectedTab: StateFlow<PresidentTeamTab> = _selectedTab.asStateFlow()

    private val _currentTime = MutableStateFlow(0L)
    val currentTime: StateFlow<Long> = _currentTime.asStateFlow()

    data class TeamNotificationPreferencesState(
        val matchEvents: Boolean = true,
        val goals: Boolean = true,
        val globalMatchEventsState: GlobalNotificationState = GlobalNotificationState.ALL_ON,
        val globalGoalsState: GlobalNotificationState = GlobalNotificationState.ALL_ON,
        val clubId: String = "",
    )

    private val _teamNotificationState = MutableStateFlow(TeamNotificationPreferencesState())
    val teamNotificationState: StateFlow<TeamNotificationPreferencesState> = _teamNotificationState.asStateFlow()

    init {
        load()
        viewModelScope.launch {
            timeTicker.timeFlow.collect { now ->
                _currentTime.value = now
            }
        }
    }

    fun selectTab(tab: PresidentTeamTab) {
        _selectedTab.value = tab
    }

    private fun load() {
        viewModelScope.launch {
            val team = getTeamById(teamId)
            if (team == null) {
                _uiState.value = PresidentTeamDetailUiState.Error
                return@launch
            }

            combine(
                getPlayersByTeam(teamId),
                getMatchesByTeam(teamId),
            ) { players, matches ->
                val finishedMatches = matches.filter { it.status == MatchStatus.FINISHED }
                val stats =
                    PresidentTeamStats(
                        totalMatches = finishedMatches.size,
                        wins = finishedMatches.count { it.goals > it.opponentGoals },
                        draws = finishedMatches.count { it.goals == it.opponentGoals },
                        losses = finishedMatches.count { it.goals < it.opponentGoals },
                        goalsScored = finishedMatches.sumOf { it.goals },
                        goalsConceded = finishedMatches.sumOf { it.opponentGoals },
                        squadSize = players.size,
                    )
                PresidentTeamDetailUiState.Ready(
                    team = team,
                    players = players,
                    matches = matches.filter { !it.archived }.sortedByDescending { it.dateTime },
                    stats = stats,
                )
            }.collect { state ->
                _uiState.value = state
            }
        }

        viewModelScope.launch {
            val clubMember = getUserClubMembership().first() ?: return@launch
            val clubRemoteId = clubMember.clubRemoteId ?: return@launch

            getNotificationPreferences(clubRemoteId).collect { prefs ->
                val teamPref = prefs.teamPreferences[teamId]
                _teamNotificationState.value =
                    TeamNotificationPreferencesState(
                        matchEvents = teamPref?.matchEvents ?: prefs.globalMatchEvents,
                        goals = teamPref?.goals ?: prefs.globalGoals,
                        globalMatchEventsState = prefs.globalStateFor(NotificationEventType.MATCH_EVENTS),
                        globalGoalsState = prefs.globalStateFor(NotificationEventType.GOALS),
                        clubId = clubRemoteId,
                    )
            }
        }
    }

    fun updateTeamMatchEvents(enabled: Boolean) {
        viewModelScope.launch {
            runCatching {
                val state = _teamNotificationState.value
                updateTeamNotificationPreference(state.clubId, teamId, NotificationEventType.MATCH_EVENTS, enabled)
            }
        }
    }

    fun updateTeamGoals(enabled: Boolean) {
        viewModelScope.launch {
            runCatching {
                val state = _teamNotificationState.value
                updateTeamNotificationPreference(state.clubId, teamId, NotificationEventType.GOALS, enabled)
            }
        }
    }
}
