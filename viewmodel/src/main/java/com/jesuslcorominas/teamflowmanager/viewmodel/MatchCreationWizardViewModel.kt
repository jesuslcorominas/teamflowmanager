package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.PeriodType
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.domain.model.SkeletonMatch
import com.jesuslcorominas.teamflowmanager.usecase.CreateMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetCaptainPlayerUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetDefaultCaptainUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetMatchByIdUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetPlayersUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetPreviousCaptainsUseCase
import com.jesuslcorominas.teamflowmanager.usecase.SaveDefaultCaptainUseCase
import com.jesuslcorominas.teamflowmanager.usecase.UpdateMatchUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MatchCreationWizardViewModel(
    private val getPlayersUseCase: GetPlayersUseCase,
    private val getPreviousCaptainsUseCase: GetPreviousCaptainsUseCase,
    private val getDefaultCaptainUseCase: GetDefaultCaptainUseCase,
    private val saveDefaultCaptainUseCase: SaveDefaultCaptainUseCase,
    private val getCaptainPlayerUseCase: GetCaptainPlayerUseCase,
    private val createMatch: CreateMatchUseCase,
    private val getMatchByIdUseCase: GetMatchByIdUseCase,
    private val updateMatchUseCase: UpdateMatchUseCase
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

    private var allPlayers: List<Player> = emptyList()
    private var isEditMode = false

    init {
        loadPlayers()
    }

    fun loadMatchForEdit(matchId: Long) {
        viewModelScope.launch {
            this@MatchCreationWizardViewModel.matchId = matchId
            isEditMode = true
            val match = getMatchByIdUseCase.invoke(matchId).first()
            if (match != null) {
                initializeFromMatch(match)
            }
        }
    }

    private fun initializeFromMatch(match: Match) {
        opponent = match.opponent
        location = match.location
        date = match.dateTime?.let { it - (it % (24 * 60 * 60 * 1000)) }
        time = match.dateTime?.let { it % (24 * 60 * 60 * 1000) }
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
            _uiState.value = MatchCreationWizardUiState.Ready(allPlayers)
        }
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

    fun createMatch(skeletonMatch: SkeletonMatch) {
        viewModelScope.launch {
            createMatch.invoke(skeletonMatch)
        }
    }

    fun updateMatch() {
        viewModelScope.launch {
            matchId?.let { id ->
                val existingMatch = getMatchByIdUseCase.invoke(id).first()
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
                }
            }
        }
    }

    fun hasUnsavedChanges(): Boolean {
        if (!isEditMode) return false
        return opponent != originalOpponent ||
            location != originalLocation ||
            date != originalDate ||
            time != originalTime ||
            numberOfPeriods != originalNumberOfPeriods ||
            squadCallUpIds != originalSquadCallUpIds ||
            captainId != originalCaptainId ||
            startingLineupIds != originalStartingLineupIds
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

}

sealed class MatchCreationWizardUiState {
    data object Loading : MatchCreationWizardUiState()
    data class Ready(val players: List<Player>) : MatchCreationWizardUiState()
}

enum class WizardStep {
    GENERAL_DATA,
    SQUAD_CALLUP,
    CAPTAIN,
    STARTING_LINEUP
}
