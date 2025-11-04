package com.jesuslcorominas.teamflowmanager.ui.players.wizard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.ui.components.Loading
import com.jesuslcorominas.teamflowmanager.ui.players.components.dialog.CaptainConfirmationDialog
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import com.jesuslcorominas.teamflowmanager.viewmodel.CaptainConfirmationState
import com.jesuslcorominas.teamflowmanager.viewmodel.PlayerWizardStep
import com.jesuslcorominas.teamflowmanager.viewmodel.PlayerWizardUiState
import com.jesuslcorominas.teamflowmanager.viewmodel.PlayerWizardViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun PlayerWizardScreen(
    playerId: Long?,
    onNavigateBack: () -> Unit,
    wizardViewModel: PlayerWizardViewModel = koinViewModel()
) {
    val uiState by wizardViewModel.uiState.collectAsState()
    val currentStep by wizardViewModel.currentStep.collectAsState()
    val captainConfirmationState by wizardViewModel.captainConfirmationState.collectAsState()

    LaunchedEffect(playerId) {
        if (playerId != null && playerId > 0) {
            wizardViewModel.initializeForEdit(playerId)
        } else {
            wizardViewModel.initializeForCreate()
        }
    }

    when (uiState) {
        is PlayerWizardUiState.Loading -> Loading()
        is PlayerWizardUiState.Error -> {
            // Show error and navigate back
            LaunchedEffect(Unit) {
                onNavigateBack()
            }
        }
        is PlayerWizardUiState.Ready -> {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                when (currentStep) {
                    PlayerWizardStep.PLAYER_DATA -> {
                        PlayerDataStep(
                            initialFirstName = wizardViewModel.getFirstName(),
                            initialLastName = wizardViewModel.getLastName(),
                            initialNumber = wizardViewModel.getNumber(),
                            initialIsCaptain = wizardViewModel.getIsCaptain(),
                            initialImageUri = wizardViewModel.getImageUri(),
                            onDataChanged = { firstName, lastName, number, isCaptain, imageUri ->
                                wizardViewModel.setPlayerData(firstName, lastName, number, isCaptain, imageUri)
                            },
                            onNext = {
                                wizardViewModel.goToNextStep()
                            },
                            onCancel = onNavigateBack,
                            modifier = Modifier
                                .weight(1f)
                                .padding(TFMSpacing.spacing04)
                        )
                    }
                    PlayerWizardStep.POSITIONS -> {
                        PlayerPositionsStep(
                            initialPositions = wizardViewModel.getSelectedPositions(),
                            onPositionsChanged = { positions ->
                                wizardViewModel.setPositions(positions)
                            },
                            onSave = {
                                wizardViewModel.savePlayer(onSuccess = onNavigateBack)
                            },
                            onPrevious = {
                                wizardViewModel.goToPreviousStep()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(TFMSpacing.spacing04)
                        )
                    }
                }
            }

            // Captain confirmation dialogs
            when (val state = captainConfirmationState) {
                is CaptainConfirmationState.ConfirmReplace -> {
                    CaptainConfirmationDialog(
                        state = state,
                        onConfirm = {
                            wizardViewModel.confirmCaptainChange(onSuccess = onNavigateBack)
                        },
                        onDismiss = {
                            wizardViewModel.cancelCaptainChange()
                        },
                    )
                }

                is CaptainConfirmationState.ConfirmRemove -> {
                    CaptainConfirmationDialog(
                        state = state,
                        onConfirm = {
                            wizardViewModel.confirmCaptainChange(onSuccess = onNavigateBack)
                        },
                        onDismiss = {
                            wizardViewModel.cancelCaptainChange()
                        },
                    )
                }

                is CaptainConfirmationState.ConfirmRemoveWithMatches -> {
                    CaptainConfirmationDialog(
                        state = state,
                        onConfirm = { keepInMatches ->
                            wizardViewModel.confirmCaptainChange(keepInMatches, onSuccess = onNavigateBack)
                        },
                        onDismiss = {
                            wizardViewModel.cancelCaptainChange()
                        },
                    )
                }

                CaptainConfirmationState.None -> {}
            }
        }
    }
}
