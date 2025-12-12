package com.jesuslcorominas.teamflowmanager.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsEvent
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsParam
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker
import com.jesuslcorominas.teamflowmanager.domain.analytics.CrashReporter
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.domain.navigation.Route
import com.jesuslcorominas.teamflowmanager.domain.usecase.AddPlayerUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetCaptainPlayerUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetPlayerByIdUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetScheduledMatchesUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.RemovePlayerAsCaptainUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SetPlayerAsCaptainUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.UpdatePlayerUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.UpdateScheduledMatchesCaptainUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlayerWizardViewModel(
    private val getPlayerByIdUseCase: GetPlayerByIdUseCase,
    private val addPlayerUseCase: AddPlayerUseCase,
    private val updatePlayerUseCase: UpdatePlayerUseCase,
    private val getCaptainPlayerUseCase: GetCaptainPlayerUseCase,
    private val updateScheduledMatchesCaptainUseCase: UpdateScheduledMatchesCaptainUseCase,
    private val setPlayerAsCaptainUseCase: SetPlayerAsCaptainUseCase,
    private val removePlayerAsCaptainUseCase: RemovePlayerAsCaptainUseCase,
    private val getScheduledMatchesUseCase: GetScheduledMatchesUseCase,
    private val analyticsTracker: AnalyticsTracker,
    private val crashReporter: CrashReporter,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow<PlayerWizardUiState>(PlayerWizardUiState.Loading)
    val uiState: StateFlow<PlayerWizardUiState> = _uiState.asStateFlow()

    private val _currentStep = MutableStateFlow(PlayerWizardStep.PLAYER_DATA)
    val currentStep: StateFlow<PlayerWizardStep> = _currentStep.asStateFlow()

    private val _captainConfirmationState = MutableStateFlow<CaptainConfirmationState>(CaptainConfirmationState.None)
    val captainConfirmationState: StateFlow<CaptainConfirmationState> = _captainConfirmationState.asStateFlow()

    private val _showExitDialog = MutableStateFlow(false)
    val showExitDialog: StateFlow<Boolean> = _showExitDialog.asStateFlow()

    // Player data being built/edited
    private var playerId: Long = 0L
    private var firstName: String = ""
    private var lastName: String = ""
    private var number: String = ""
    private var isCaptain: Boolean = false
    private var imageUri: String? = null
    private var selectedPositions: List<Position> = emptyList()

    // Original data for change detection
    private var originalFirstName: String = ""
    private var originalLastName: String = ""
    private var originalNumber: String = ""
    private var originalIsCaptain: Boolean = false
    private var originalImageUri: String? = null
    private var originalPositions: List<Position> = emptyList()

    init {
        val playerIdFromArgs: Long = savedStateHandle[Route.PlayerWizard.ARG_PLAYER_ID] ?: 0L

        if (playerIdFromArgs > 0L) {
            initializeForEdit(playerIdFromArgs)
        } else {
            initializeForCreate()
        }
    }

    private fun initializeForEdit(playerId: Long) {
        viewModelScope.launch {
            val player = getPlayerByIdUseCase.invoke(playerId)
            if (player != null) {
                this@PlayerWizardViewModel.playerId = player.id
                firstName = player.firstName
                lastName = player.lastName
                number = player.number.toString()
                isCaptain = player.isCaptain
                imageUri = player.imageUri
                selectedPositions = player.positions

                // Store original values
                originalFirstName = player.firstName
                originalLastName = player.lastName
                originalNumber = player.number.toString()
                originalIsCaptain = player.isCaptain
                originalImageUri = player.imageUri
                originalPositions = player.positions

                _uiState.value = PlayerWizardUiState.Ready
            } else {
                _uiState.value = PlayerWizardUiState.Error("Player not found")
            }
        }
    }

    private fun initializeForCreate() {
        playerId = 0L
        firstName = ""
        lastName = ""
        number = ""
        isCaptain = false
        imageUri = null
        selectedPositions = emptyList()

        // Store original values (empty for create)
        originalFirstName = ""
        originalLastName = ""
        originalNumber = ""
        originalIsCaptain = false
        originalImageUri = null
        originalPositions = emptyList()

        _uiState.value = PlayerWizardUiState.Ready
    }

    fun setPlayerData(
        firstName: String,
        lastName: String,
        number: String,
        isCaptain: Boolean,
        imageUri: String?
    ) {
        this.firstName = firstName
        this.lastName = lastName
        this.number = number
        this.isCaptain = isCaptain
        this.imageUri = imageUri
    }

    fun setPositions(positions: List<Position>) {
        this.selectedPositions = positions
    }

    fun getFirstName() = firstName
    fun getLastName() = lastName
    fun getNumber() = number
    fun getIsCaptain() = isCaptain
    fun getImageUri() = imageUri
    fun getSelectedPositions() = selectedPositions
    fun isEditMode() = playerId != 0L

    fun hasUnsavedChanges(): Boolean {
        return firstName != originalFirstName ||
                lastName != originalLastName ||
                number != originalNumber ||
                isCaptain != originalIsCaptain ||
                imageUri != originalImageUri ||
                selectedPositions != originalPositions
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

    fun goToNextStep() {
        _currentStep.value = when (_currentStep.value) {
            PlayerWizardStep.PLAYER_DATA -> PlayerWizardStep.POSITIONS
            PlayerWizardStep.POSITIONS -> PlayerWizardStep.POSITIONS // Stay on last step
        }
    }

    fun goToPreviousStep() {
        _currentStep.value = when (_currentStep.value) {
            PlayerWizardStep.PLAYER_DATA -> PlayerWizardStep.PLAYER_DATA // Stay on first step
            PlayerWizardStep.POSITIONS -> PlayerWizardStep.PLAYER_DATA
        }
    }

    fun savePlayer(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val player = Player(
                id = playerId,
                firstName = firstName,
                lastName = lastName,
                number = number.toInt(),
                positions = selectedPositions,
                teamId = 1, // TODO: Get team ID properly
                isCaptain = isCaptain,
                imageUri = imageUri
            )

            val currentCaptain = getCaptainPlayerUseCase.invoke()

            if (player.isCaptain && currentCaptain != null && currentCaptain.id != player.id) {
                // Show confirmation to replace captain
                // Check if there are scheduled matches
                val scheduledMatches = getScheduledMatchesUseCase()
                if (scheduledMatches.isNotEmpty()) {
                    _captainConfirmationState.value = CaptainConfirmationState.ConfirmReplaceWithMatches(
                        currentCaptain = currentCaptain,
                        newCaptain = player,
                        matchCount = scheduledMatches.size
                    )
                } else {
                    _captainConfirmationState.value = CaptainConfirmationState.ConfirmReplace(
                        currentCaptain = currentCaptain,
                        newCaptain = player
                    )
                }
            } else if (!player.isCaptain && currentCaptain != null && currentCaptain.id == player.id) {
                // Player is being updated to no longer be captain
                val scheduledMatches = getScheduledMatchesUseCase()
                if (scheduledMatches.isNotEmpty()) {
                    _captainConfirmationState.value = CaptainConfirmationState.ConfirmRemoveWithMatches(
                        player = player,
                        matchCount = scheduledMatches.size
                    )
                } else {
                    _captainConfirmationState.value = CaptainConfirmationState.ConfirmRemove(player)
                }
            } else {
                // No confirmation needed, save directly
                savePlayerDirectly(player, onSuccess)
            }
        }
    }

    fun confirmCaptainChange(keepInMatches: Boolean = false, onSuccess: () -> Unit) {
        viewModelScope.launch {
            when (val state = _captainConfirmationState.value) {
                is CaptainConfirmationState.ConfirmReplace -> {
                    savePlayerDirectly(state.newCaptain, onSuccess)
                }
                is CaptainConfirmationState.ConfirmReplaceWithMatches -> {
                    savePlayerDirectly(state.newCaptain, onSuccess)
                    if (keepInMatches) {
                        updateScheduledMatchesCaptainUseCase.invoke(state.newCaptain.id)
                    }
                }
                is CaptainConfirmationState.ConfirmRemove -> {
                    savePlayerDirectly(state.player, onSuccess)
                }
                is CaptainConfirmationState.ConfirmRemoveWithMatches -> {
                    savePlayerDirectly(state.player, onSuccess)
                    if (!keepInMatches) {
                        updateScheduledMatchesCaptainUseCase.invoke(null)
                    }
                }
                else -> {}
            }
            _captainConfirmationState.value = CaptainConfirmationState.None
        }
    }

    fun cancelCaptainChange() {
        _captainConfirmationState.value = CaptainConfirmationState.None
    }

    private fun savePlayerDirectly(player: Player, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val isNewPlayer = player.id == 0L

                crashReporter.log("Saving player via wizard: ${player.firstName} ${player.lastName}, isNew: $isNewPlayer")

                if (isNewPlayer) {
                    addPlayerUseCase.invoke(player)

                    analyticsTracker.logEvent(
                        AnalyticsEvent.PLAYER_CREATED,
                        mapOf(
                            AnalyticsParam.PLAYER_NAME to "${player.firstName} ${player.lastName}",
                            AnalyticsParam.PLAYER_NUMBER to player.number.toString(),
                            AnalyticsParam.WIZARD_TYPE to "player_wizard",
                        ),
                    )
                } else {
                    updatePlayerUseCase.invoke(player)

                    analyticsTracker.logEvent(
                        AnalyticsEvent.PLAYER_UPDATED,
                        mapOf(
                            AnalyticsParam.PLAYER_ID to player.id.toString(),
                            AnalyticsParam.WIZARD_TYPE to "player_wizard",
                        ),
                    )
                }

                // Update captain status
                if (player.isCaptain) {
                    setPlayerAsCaptainUseCase(player.id)
                } else {
                    val currentCaptain = getCaptainPlayerUseCase.invoke()
                    if (currentCaptain?.id == player.id) {
                        removePlayerAsCaptainUseCase(player.id)
                    }
                }

                analyticsTracker.logEvent(
                    AnalyticsEvent.WIZARD_STEP_COMPLETED,
                    mapOf(
                        AnalyticsParam.WIZARD_TYPE to "player_wizard",
                        AnalyticsParam.STEP_NUMBER to "final",
                    ),
                )

                onSuccess()
            } catch (e: Exception) {
                crashReporter.recordException(e)
                crashReporter.log("Error saving player in wizard: ${e.message}")
                throw e
            }
        }
    }
}

sealed class PlayerWizardUiState {
    data object Loading : PlayerWizardUiState()
    data object Ready : PlayerWizardUiState()
    data class Error(val message: String) : PlayerWizardUiState()
}

enum class PlayerWizardStep {
    PLAYER_DATA,
    POSITIONS
}
