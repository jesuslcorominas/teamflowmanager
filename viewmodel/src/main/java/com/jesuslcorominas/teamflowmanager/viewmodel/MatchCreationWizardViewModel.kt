package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.usecase.GetDefaultCaptainUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetPlayersUseCase
import com.jesuslcorominas.teamflowmanager.usecase.GetPreviousCaptainsUseCase
import com.jesuslcorominas.teamflowmanager.usecase.SaveDefaultCaptainUseCase
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
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<MatchCreationWizardUiState>(MatchCreationWizardUiState.Loading)
    val uiState: StateFlow<MatchCreationWizardUiState> = _uiState.asStateFlow()
    
    private val _currentStep = MutableStateFlow(WizardStep.GENERAL_DATA)
    val currentStep: StateFlow<WizardStep> = _currentStep.asStateFlow()
    
    // Match data being built
    private var opponent: String = ""
    private var location: String = ""
    private var date: Long? = null
    private var squadCallUpIds: Set<Long> = emptySet()
    private var captainId: Long? = null
    private var startingLineupIds: Set<Long> = emptySet()
    
    private var allPlayers: List<Player> = emptyList()
    
    init {
        loadPlayers()
    }
    
    private fun loadPlayers() {
        viewModelScope.launch {
            allPlayers = getPlayersUseCase.invoke().first()
            _uiState.value = MatchCreationWizardUiState.Ready(allPlayers)
        }
    }
    
    fun setGeneralData(opponent: String, location: String, date: Long?) {
        this.opponent = opponent
        this.location = location
        this.date = date
    }
    
    fun setSquadCallUp(playerIds: Set<Long>) {
        this.squadCallUpIds = playerIds
    }
    
    fun setCaptain(playerId: Long?) {
        this.captainId = playerId
    }
    
    fun setStartingLineup(playerIds: Set<Long>) {
        this.startingLineupIds = playerIds
    }
    
    fun getOpponent() = opponent
    fun getLocation() = location
    fun getDate() = date
    fun getSquadCallUpIds() = squadCallUpIds
    fun getCaptainId() = captainId
    fun getStartingLineupIds() = startingLineupIds
    
    fun goToNextStep() {
        _currentStep.value = when (_currentStep.value) {
            WizardStep.GENERAL_DATA -> WizardStep.SQUAD_CALLUP
            WizardStep.SQUAD_CALLUP -> WizardStep.CAPTAIN
            WizardStep.CAPTAIN -> WizardStep.STARTING_LINEUP
            WizardStep.STARTING_LINEUP -> WizardStep.STARTING_LINEUP // Final step
        }
    }
    
    fun goToPreviousStep() {
        _currentStep.value = when (_currentStep.value) {
            WizardStep.GENERAL_DATA -> WizardStep.GENERAL_DATA // First step
            WizardStep.SQUAD_CALLUP -> WizardStep.GENERAL_DATA
            WizardStep.CAPTAIN -> WizardStep.SQUAD_CALLUP
            WizardStep.STARTING_LINEUP -> WizardStep.CAPTAIN
        }
    }
    
    fun hasGoalkeepersInSquad(): Boolean {
        val squadPlayers = allPlayers.filter { it.id in squadCallUpIds }
        return squadPlayers.any { player ->
            player.positions.any { it is Position.Goalkeeper }
        }
    }
    
    fun hasGoalkeeperInStartingLineup(): Boolean {
        val startingPlayers = allPlayers.filter { it.id in startingLineupIds }
        return startingPlayers.any { player ->
            player.positions.any { it is Position.Goalkeeper }
        }
    }
    
    suspend fun checkIfShouldAskForDefaultCaptain(): Pair<Boolean, Player?> {
        if (captainId == null) return Pair(false, null)
        
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
    
    fun buildMatch(): Match {
        val allSelectedIds = startingLineupIds + squadCallUpIds
        val substituteIds = allSelectedIds - startingLineupIds
        
        return Match(
            id = 0L,
            teamId = 1L,
            opponent = opponent,
            location = location,
            date = date ?: System.currentTimeMillis(),
            squadCallUpIds = squadCallUpIds.toList(),
            captainId = captainId,
            startingLineupIds = startingLineupIds.toList(),
            substituteIds = substituteIds.toList(),
        )
    }
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
