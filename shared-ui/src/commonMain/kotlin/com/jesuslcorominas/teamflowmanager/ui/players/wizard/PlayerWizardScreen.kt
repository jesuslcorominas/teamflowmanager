package com.jesuslcorominas.teamflowmanager.ui.players.wizard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.jesuslcorominas.teamflowmanager.domain.analytics.ScreenName
import com.jesuslcorominas.teamflowmanager.ui.analytics.TrackScreenView
import com.jesuslcorominas.teamflowmanager.ui.components.AppBackHandler
import com.jesuslcorominas.teamflowmanager.ui.components.Loading
import com.jesuslcorominas.teamflowmanager.ui.components.dialog.AppAlertDialog
import com.jesuslcorominas.teamflowmanager.ui.players.components.dialog.CaptainConfirmationDialog
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import com.jesuslcorominas.teamflowmanager.viewmodel.CaptainConfirmationState
import com.jesuslcorominas.teamflowmanager.viewmodel.PlayerWizardStep
import com.jesuslcorominas.teamflowmanager.viewmodel.PlayerWizardUiState
import com.jesuslcorominas.teamflowmanager.viewmodel.PlayerWizardViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import teamflowmanager.shared_ui.generated.resources.Res
import teamflowmanager.shared_ui.generated.resources.cancel
import teamflowmanager.shared_ui.generated.resources.discard
import teamflowmanager.shared_ui.generated.resources.discard_message
import teamflowmanager.shared_ui.generated.resources.unsaved_changes_title

@Composable
fun PlayerWizardScreen(
    playerId: Long,
    onNavigateBack: () -> Unit,
    wizardViewModel: PlayerWizardViewModel = koinViewModel(key = playerId.toString(), parameters = { parametersOf(playerId) }),
) {
    TrackScreenView(screenName = ScreenName.PLAYER_WIZARD, screenClass = "PlayerWizardScreen")

    val uiState by wizardViewModel.uiState.collectAsState()
    val currentStep by wizardViewModel.currentStep.collectAsState()
    val captainConfirmationState by wizardViewModel.captainConfirmationState.collectAsState()
    val showExitDialog by wizardViewModel.showExitDialog.collectAsState()

    AppBackHandler {
        wizardViewModel.requestBack(onNavigateBack)
    }

    when (uiState) {
        is PlayerWizardUiState.Loading -> Loading()
        is PlayerWizardUiState.Error -> {
            LaunchedEffect(Unit) { onNavigateBack() }
        }
        is PlayerWizardUiState.Ready -> {
            Column(modifier = Modifier.fillMaxSize()) {
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
                            onNext = { wizardViewModel.goToNextStep() },
                            onCancel = { wizardViewModel.requestBack(onNavigateBack) },
                            modifier = Modifier
                                .weight(1f)
                                .padding(TFMSpacing.spacing04),
                        )
                    }
                    PlayerWizardStep.POSITIONS -> {
                        PlayerPositionsStep(
                            initialPositions = wizardViewModel.getSelectedPositions(),
                            onPositionsChanged = { positions -> wizardViewModel.setPositions(positions) },
                            onSave = { wizardViewModel.savePlayer(onSuccess = onNavigateBack) },
                            onPrevious = { wizardViewModel.goToPreviousStep() },
                            modifier = Modifier
                                .weight(1f)
                                .padding(TFMSpacing.spacing04),
                        )
                    }
                }
            }

            when (val state = captainConfirmationState) {
                is CaptainConfirmationState.ConfirmReplace -> CaptainConfirmationDialog(
                    state = state,
                    onConfirm = { wizardViewModel.confirmCaptainChange(onSuccess = onNavigateBack) },
                    onDismiss = { wizardViewModel.cancelCaptainChange() },
                )
                is CaptainConfirmationState.ConfirmReplaceWithMatches -> CaptainConfirmationDialog(
                    state = state,
                    onConfirm = { keepInMatches -> wizardViewModel.confirmCaptainChange(keepInMatches, onSuccess = onNavigateBack) },
                    onDismiss = { wizardViewModel.cancelCaptainChange() },
                )
                is CaptainConfirmationState.ConfirmRemove -> CaptainConfirmationDialog(
                    state = state,
                    onConfirm = { wizardViewModel.confirmCaptainChange(onSuccess = onNavigateBack) },
                    onDismiss = { wizardViewModel.cancelCaptainChange() },
                )
                is CaptainConfirmationState.ConfirmRemoveWithMatches -> CaptainConfirmationDialog(
                    state = state,
                    onConfirm = { keepInMatches -> wizardViewModel.confirmCaptainChange(keepInMatches, onSuccess = onNavigateBack) },
                    onDismiss = { wizardViewModel.cancelCaptainChange() },
                )
                is CaptainConfirmationState.None -> {}
            }
        }
    }

    if (showExitDialog) {
        AppAlertDialog(
            title = stringResource(Res.string.unsaved_changes_title),
            message = stringResource(Res.string.discard_message),
            confirmText = stringResource(Res.string.discard),
            dismissText = stringResource(Res.string.cancel),
            onConfirm = { wizardViewModel.discardChanges(onNavigateBack) },
            onDismiss = { wizardViewModel.dismissExitDialog() },
        )
    }
}
