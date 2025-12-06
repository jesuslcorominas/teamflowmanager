package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsEvent
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsParam
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker
import com.jesuslcorominas.teamflowmanager.domain.analytics.CrashReporter
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.PeriodType
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.domain.model.SkeletonMatch
import com.jesuslcorominas.teamflowmanager.domain.navigation.Route
import com.jesuslcorominas.teamflowmanager.usecase.CreateMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetCaptainPlayerUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetDefaultCaptainUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetMatchByIdUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetPlayersUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetPreviousCaptainsUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetTeamUseCase
import com.jesuslcorominas.teamflowmanager.usecase.SaveDefaultCaptainUseCase
import com.jesuslcorominas.teamflowmanager.usecase.UpdateMatchUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class MatchCreationWizardViewModel(
    private val getPlayersUseCase: GetPlayersUseCase,
    private val getPreviousCaptainsUseCase: GetPreviousCaptainsUseCase,
    private val getDefaultCaptainUseCase: GetDefaultCaptainUseCase,
    private val saveDefaultCaptainUseCase: SaveDefaultCaptainUseCase,
    private val getCaptainPlayerUseCase: GetCaptainPlayerUseCase,
    private val getTeamUseCase: GetTeamUseCase,
    private val createMatch: CreateMatchUseCase,
    private val getMatchByIdUseCase: GetMatchByIdUseCase,
    private val updateMatchUseCase: UpdateMatchUseCase,
    private val analyticsTracker: AnalyticsTracker,
    private val crashReporter: CrashReporter,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow<MatchCreationWizardUiState>(MatchCreationWizardUiState.Loading)
    val uiState: StateFlow<MatchCreationWizardUiState> = _uiState.asStateFlow()

    private val _currentStep = MutableStateFlow(WizardStep.GENERAL_DATA)
    val currentStep: StateFlow<WizardStep> = _currentStep.asStateFlow()

    private val _showExitDialog = MutableStateFlow(false)
    val showExitDialog: StateFlow<Boolean> = _showExitDialog.asStateFlow()

    // Match data being built
    private var matchId: Long? = null
    private var opponent: String = ""
    private var location: String = ""
    private var date: Long? = null
    private var time: Long? = null
    private var numberOfPeriods: Int = 2
    private var squadCallUpIds: Set<Long> = emptySet()
    private var captainId: Long = 0L
    private var startingLineupIds: Set<Long> = emptySet()

    // Track original values for unsaved changes detection
    private var originalOpponent: String = ""
    private var originalLocation: String = ""
    private var originalDate: Long? = null
    private var originalTime: Long? = null
    private var originalNumberOfPeriods: Int = 2
    private var originalSquadCallUpIds: Set<Long> = emptySet()
    private var originalCaptainId: Long = 0L
    private var originalStartingLineupIds: Set<Long> = emptySet()

    // Flag to track if match data is loaded for edit mode
    private var matchDataLoaded = false
    private var playersLoaded = false
    private var pendingMatchIdForEdit: Long? = null

    init {
        // Check if we have a matchId in savedStateHandle for edit mode
        val matchIdFromState = savedStateHandle.get<Long>(Route.CreateMatch.ARG_MATCH_ID)
        if (matchIdFromState != null && matchIdFromState != 0L) {
            pendingMatchIdForEdit = matchIdFromState
            isEditMode = true
            this.matchId = matchIdFromState
        }
        
        loadPlayers()
        loadTeam()
        
        // Load match data if in edit mode
        if (pendingMatchIdForEdit != null) {
            loadMatchForEdit(pendingMatchIdForEdit!!)
        }
    }
    
    private fun loadTeam() {
        viewModelScope.launch {
            val team = getTeamUseCase.invoke().first()
            teamTypeValue = team?.teamType?.players ?: 5
        }
    }

    private fun loadMatchForEdit(matchId: Long) {
        viewModelScope.launch {
            val match = getMatchByIdUseCase.invoke(matchId).firstOrNull()
            if (match != null) {
                initializeFromMatch(match)
            }
            matchDataLoaded = true
            checkAndSetReady()
        }
    }

    private fun initializeFromMatch(match: Match) {
        opponent = match.opponent
        location = match.location
        date = match.dateTime?.let { it - (it % MILLIS_PER_DAY) }
        time = match.dateTime?.let { it % MILLIS_PER_DAY }
        numberOfPeriods = match.periodType.numberOfPeriods
        squadCallUpIds = match.squadCallUpIds.toSet()
        captainId = match.captainId
        startingLineupIds = match.startingLineupIds.toSet()

        // Store original values for unsaved changes detection
        originalOpponent = opponent
        originalLocation = location
        originalDate = date
        originalTime = time
        originalNumberOfPeriods = numberOfPeriods
        originalSquadCallUpIds = squadCallUpIds
        originalCaptainId = captainId
        originalStartingLineupIds = startingLineupIds
    }

    private fun loadPlayers() {
        viewModelScope.launch {
            allPlayers = getPlayersUseCase.invoke().first()
            playersLoaded = true
            checkAndSetReady()
        }
    }
    
    /**
     * Check if all required data is loaded and set the Ready state.
     * In edit mode, we need both players AND match data.
     * In create mode, we only need players.
     */
    private fun checkAndSetReady() {
        if (!playersLoaded) return
        
        // In edit mode, wait for match data too
        if (isEditMode && !matchDataLoaded) return
        
        _uiState.value = MatchCreationWizardUiState.Ready(allPlayers)
    }

    fun setGeneralData(opponent: String, location: String, date: Long?, time: Long?, numberOfPeriods: Int) {
        this.opponent = opponent
        this.location = location
        this.date = date
        this.time = time
        this.numberOfPeriods = numberOfPeriods
    }

    fun setSquadCallUp(playerIds: Set<Long>) {
        this.squadCallUpIds = playerIds
        
        // If captain is not in the squad anymore, clear captain selection
        if (captainId != 0L && captainId !in playerIds) {
            captainId = 0L
        }
        
        // Remove players from starting lineup if they're not in the squad anymore
        startingLineupIds = startingLineupIds.filter { it in playerIds }.toSet()
    }

    fun setCaptain(playerId: Long) {
        this.captainId = playerId
    }

    fun setStartingLineup(playerIds: Set<Long>) {
        this.startingLineupIds = playerIds
    }

    fun getOpponent() = opponent
    fun getLocation() = location
    fun getDate() = date
    fun getTime() = time
    fun getNumberOfPeriods() = numberOfPeriods
    fun getSquadCallUpIds() = squadCallUpIds
    fun getCaptainId() = captainId
    fun getStartingLineupIds() = startingLineupIds
    fun getTeamTypePlayerCount() = teamTypeValue

    fun goToNextStep() {
        viewModelScope.launch {
            _currentStep.value = when (_currentStep.value) {
                WizardStep.GENERAL_DATA -> WizardStep.SQUAD_CALLUP
                WizardStep.SQUAD_CALLUP -> {
                    // Check if there's a fixed captain
                    val fixedCaptain = getCaptainPlayerUseCase.invoke()
                    if (fixedCaptain != null && fixedCaptain.id in squadCallUpIds) {
                        // Auto-select the fixed captain and skip to starting lineup
                        captainId = fixedCaptain.id
                        WizardStep.STARTING_LINEUP
                    } else {
                        WizardStep.CAPTAIN
                    }
                }
                WizardStep.CAPTAIN -> WizardStep.STARTING_LINEUP
                WizardStep.STARTING_LINEUP -> WizardStep.STARTING_LINEUP // Final step
            }
        }
    }

    fun goToPreviousStep() {
        viewModelScope.launch {
            _currentStep.value = when (_currentStep.value) {
                WizardStep.GENERAL_DATA -> WizardStep.GENERAL_DATA // First step
                WizardStep.SQUAD_CALLUP -> WizardStep.GENERAL_DATA
                WizardStep.CAPTAIN -> WizardStep.SQUAD_CALLUP
                WizardStep.STARTING_LINEUP -> {
                    // Check if we have a fixed captain
                    val fixedCaptain = getCaptainPlayerUseCase.invoke()
                    if (fixedCaptain != null && captainId == fixedCaptain.id) {
                        // We skipped captain selection, go back to squad callup
                        WizardStep.SQUAD_CALLUP
                    } else {
                        WizardStep.CAPTAIN
                    }
                }
            }
        }
    }

    fun hasGoalkeepersInSquad(): Boolean {
        val squadPlayers = allPlayers.filter { it.id in squadCallUpIds }
        return squadPlayers.any { player ->
            player.positions.any { it == Position.Goalkeeper }
        }
    }

    suspend fun checkIfShouldAskForDefaultCaptain(): Pair<Boolean, Player?> {
        // Get default captain
        val defaultCaptainId = getDefaultCaptainUseCase.invoke()
        if (defaultCaptainId != null) {
            // Already has a default captain, don't ask
            return Pair(false, null)
        }

        // Check if this captain was in the last 2 matches
        val previousCaptains = getPreviousCaptainsUseCase.invoke(2)
        val sameCaptainCount = previousCaptains.count { it == captainId }

        if (sameCaptainCount >= 2) {
            val captain = allPlayers.find { it.id == captainId }
            return Pair(true, captain)
        }

        return Pair(false, null)
    }

    fun setDefaultCaptain(playerId: Long) {
        saveDefaultCaptainUseCase.invoke(playerId)
    }

    suspend fun loadDefaultCaptainIfExists() {
        val defaultCaptainId = getDefaultCaptainUseCase.invoke()
        if (defaultCaptainId != null && defaultCaptainId in squadCallUpIds) {
            captainId = defaultCaptainId
        }
    }

    fun buildMatch(): SkeletonMatch = SkeletonMatch(
        opponent = opponent,
        location = location,
        dateTime = date?.plus(time ?: 0L),
        numberOfPeriods = numberOfPeriods,
        captainId = captainId,
        squadCallUpIds = squadCallUpIds.toList(),
        startingLineupIds = startingLineupIds.toList(),
    )

    fun createMatch(skeletonMatch: SkeletonMatch, onComplete: () -> Unit = {}) {
        _uiState.value = MatchCreationWizardUiState.Saving
        viewModelScope.launch {
            try {
                crashReporter.log("Creating match via wizard: ${skeletonMatch.opponent}")
                createMatch.invoke(skeletonMatch)
                
                analyticsTracker.logEvent(
                    AnalyticsEvent.MATCH_CREATED,
                    mapOf(
                        AnalyticsParam.WIZARD_TYPE to "match_wizard",
                        AnalyticsParam.MATCH_TYPE to "scheduled",
                    ),
                )
                
                analyticsTracker.logEvent(
                    AnalyticsEvent.WIZARD_STEP_COMPLETED,
                    mapOf(
                        AnalyticsParam.WIZARD_TYPE to "match_wizard",
                        AnalyticsParam.STEP_NUMBER to "final",
                    ),
                )
                
                // Call onComplete callback after successful creation
                onComplete()
            } catch (e: Exception) {
                crashReporter.recordException(e)
                crashReporter.log("Error creating match in wizard: ${e.message}")
                // On error, restore the Ready state and navigate back
                _uiState.value = MatchCreationWizardUiState.Ready(allPlayers)
                onComplete()
            }
        }
    }

    fun updateMatch(onComplete: () -> Unit = {}) {
        _uiState.value = MatchCreationWizardUiState.Saving
        viewModelScope.launch {
            try {
                matchId?.let { id ->
                    crashReporter.log("Updating match via wizard: $id")
                    val existingMatch = getMatchByIdUseCase.invoke(id).firstOrNull()
                    existingMatch?.let { match ->
                        val updatedMatch = match.copy(
                            opponent = opponent,
                            location = location,
                            dateTime = date?.plus(time ?: 0L),
                            periodType = PeriodType.fromNumberOfPeriods(numberOfPeriods),
                            squadCallUpIds = squadCallUpIds.toList(),
                            captainId = captainId,
                            startingLineupIds = startingLineupIds.toList(),
                        )
                        updateMatchUseCase.invoke(updatedMatch)
                        
                        analyticsTracker.logEvent(
                            AnalyticsEvent.MATCH_UPDATED,
                            mapOf(
                                AnalyticsParam.MATCH_ID to id.toString(),
                                AnalyticsParam.WIZARD_TYPE to "match_wizard",
                            ),
                        )
                    }
                }
                // Call onComplete callback after successful update
                onComplete()
            } catch (e: Exception) {
                crashReporter.recordException(e)
                crashReporter.log("Error updating match in wizard: ${e.message}")
                // On error, restore the Ready state and navigate back
                _uiState.value = MatchCreationWizardUiState.Ready(allPlayers)
                onComplete()
            }
        }
    }

    fun hasUnsavedChanges(): Boolean {
        // In edit mode, check if any field has been modified from original
        if (isEditMode) {
            return opponent != originalOpponent ||
                location != originalLocation ||
                date != originalDate ||
                time != originalTime ||
                numberOfPeriods != originalNumberOfPeriods ||
                squadCallUpIds != originalSquadCallUpIds ||
                captainId != originalCaptainId ||
                startingLineupIds != originalStartingLineupIds
        }
        
        // In create mode, check if user has entered any data
        return opponent.isNotEmpty() ||
            location.isNotEmpty() ||
            date != null ||
            time != null ||
            squadCallUpIds.isNotEmpty() ||
            captainId != 0L ||
            startingLineupIds.isNotEmpty()
    }

    fun requestBack(onNavigateBack: () -> Unit) {
        if (hasUnsavedChanges()) {
            _showExitDialog.value = true
        } else {
            onNavigateBack()
        }
    }

    fun dismissExitDialog() {
        _showExitDialog.value = false
    }

    fun discardChanges(onNavigateBack: () -> Unit) {
        _showExitDialog.value = false
        onNavigateBack()
    }

    fun isEditMode() = isEditMode

    companion object {
        private const val MILLIS_PER_DAY = 24 * 60 * 60 * 1000L
    }
}

sealed class MatchCreationWizardUiState {
    data object Loading : MatchCreationWizardUiState()
    data object Saving : MatchCreationWizardUiState()
    data class Ready(val players: List<Player>) : MatchCreationWizardUiState()
}

enum class WizardStep {
    GENERAL_DATA,
    SQUAD_CALLUP,
    CAPTAIN,
    STARTING_LINEUP
}
