package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsEvent
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsParam
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.domain.navigation.Route
import com.jesuslcorominas.teamflowmanager.usecase.CreateTeamUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetCaptainPlayerUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetPlayersUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetTeamUseCase
import com.jesuslcorominas.teamflowmanager.usecase.HasScheduledMatchesUseCase
import com.jesuslcorominas.teamflowmanager.usecase.UpdateTeamUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TeamViewModel(
    private val getTeam: GetTeamUseCase,
    private val getPlayers: GetPlayersUseCase,
    private val createTeam: CreateTeamUseCase,
    private val updateTeam: UpdateTeamUseCase,
    private val getCaptainPlayer: GetCaptainPlayerUseCase,
    private val hasScheduledMatches: HasScheduledMatchesUseCase,
    private val playerRepository: PlayerRepository, // TODO extract to usecase
    private val analyticsTracker: AnalyticsTracker,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _uiState = MutableStateFlow<TeamUiState>(TeamUiState.Loading)
    val uiState: StateFlow<TeamUiState> = _uiState.asStateFlow()

    private val _showExitDialog = MutableStateFlow(false)
    val showExitDialog: StateFlow<Boolean> = _showExitDialog

    private val _showTeamTypeChangeError = MutableStateFlow(false)
    val showTeamTypeChangeError: StateFlow<Boolean> = _showTeamTypeChangeError

    private val _showSaveError = MutableStateFlow(false)
    val showSaveError: StateFlow<Boolean> = _showSaveError

    private var originalTeam: Team? = null

    val isEditMode: Boolean = (savedStateHandle[Route.Team.ARG_MODE] as? String) == Route.Team.MODE_EDIT

    init {
        loadTeam()
    }

    private fun loadTeam() {
        viewModelScope.launch {
            combine(
                getTeam(), getPlayers()
            ) { team, players ->
                team to players
            }.collect { (team, players) ->
                if (originalTeam == null) {
                    originalTeam = team
                }
                _uiState.update { if (team == null) TeamUiState.NoTeam else TeamUiState.Success(team, players) }
            }
        }
    }

    fun showTeamTypeChangeError() {
        _showTeamTypeChangeError.value = true
    }

    fun dismissTeamTypeChangeError() {
        _showTeamTypeChangeError.value = false
    }

    fun dismissSaveError() {
        _showSaveError.value = false
    }

    fun createTeam(team: Team, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                createTeam.invoke(team)

                // Track team creation event
                analyticsTracker.logEvent(
                    AnalyticsEvent.TEAM_CREATED,
                    mapOf(
                        AnalyticsParam.TEAM_NAME to team.name,
                    ),
                )
                
                onSuccess()
            } catch (e: Exception) {
                _showSaveError.value = true
            }
        }
    }

    fun updateTeam(team: Team, captainId: Long?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val original = originalTeam
                val teamTypeChanged = original != null && original.teamType != team.teamType

                // Only check for scheduled matches if team type changed
                if (teamTypeChanged) {
                    val hasScheduled = hasScheduledMatches.invoke()
                    if (hasScheduled) {
                        showTeamTypeChangeError()
                        return@launch
                    }
                }

                // Handle captain changes
                val captain = getCaptainPlayer.invoke()
                val currentCaptainId = captain?.id
                val captainChanged = currentCaptainId != captainId

                if (captainChanged) {
                    if (captain != null && captainId == null) {
                        // Remove current captain
                        playerRepository.removePlayerAsCaptain(captain.id) // TODO extract to usecase
                    } else if (captainId != null && (captain == null || captain.id != captainId)) {
                        // Set new captain
                        playerRepository.setPlayerAsCaptain(captainId) // TODO extract to usecase
                    }
                }

                updateTeam.invoke(team)

                // Track team update event
                analyticsTracker.logEvent(
                    AnalyticsEvent.TEAM_UPDATED,
                    mapOf(
                        AnalyticsParam.TEAM_ID to team.id.toString(),
                    ),
                )
                
                // Only navigate back on success
                onSuccess()
            } catch (e: Exception) {
                _showSaveError.value = true
            }
        }
    }

    fun requestBack(onNavigateBack: () -> Unit) {
        if (isEditMode) {
            // TODO check if there are unsaved changes
            _showExitDialog.value = true
        } else {
            onNavigateBack()
        }
    }

    fun discardChanges(onNavigateBack: () -> Unit) {
        _showExitDialog.value = false
        onNavigateBack()
    }

    fun dismissExitDialog() {
        _showExitDialog.value = false
    }
}

sealed interface TeamUiState {
    data object Loading : TeamUiState

    data object NoTeam : TeamUiState

    data class Success(val team: Team, val players: List<Player>) : TeamUiState
}
