package com.jesuslcorominas.teamflowmanager.ui.players.wizard

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.analytics.ScreenName
import com.jesuslcorominas.teamflowmanager.ui.analytics.TrackScreenView
import com.jesuslcorominas.teamflowmanager.ui.components.Loading
import com.jesuslcorominas.teamflowmanager.ui.components.dialog.AppAlertDialog
import com.jesuslcorominas.teamflowmanager.ui.players.components.dialog.CaptainConfirmationDialog
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import com.jesuslcorominas.teamflowmanager.viewmodel.CaptainConfirmationState
import com.jesuslcorominas.teamflowmanager.viewmodel.PlayerWizardStep
import com.jesuslcorominas.teamflowmanager.viewmodel.PlayerWizardUiState
import com.jesuslcorominas.teamflowmanager.viewmodel.PlayerWizardViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerWizardScreen(
    playerId: Long,
    onNavigateBack: () -> Unit,
    wizardViewModel: PlayerWizardViewModel = koinViewModel(parameters = { parametersOf(playerId) }),
) {
    TrackScreenView(screenName = ScreenName.PLAYER_WIZARD, screenClass = "PlayerWizardScreen")

    val uiState by wizardViewModel.uiState.collectAsState()
    val currentStep by wizardViewModel.currentStep.collectAsState()
    val captainConfirmationState by wizardViewModel.captainConfirmationState.collectAsState()
    val showExitDialog by wizardViewModel.showExitDialog.collectAsState()

    BackHandler {
        wizardViewModel.requestBack(onNavigateBack)
    }

    when (uiState) {
        is PlayerWizardUiState.Loading -> Loading()
        is PlayerWizardUiState.Error -> {
            LaunchedEffect(Unit) {
                onNavigateBack()
            }
        }
        is PlayerWizardUiState.Ready -> {
            val stepTitle =
                stringResource(
                    when (currentStep) {
                        PlayerWizardStep.PLAYER_DATA -> R.string.player_data_step_title
                        PlayerWizardStep.POSITIONS -> R.string.player_positions_step_title
                    },
                )

            Column(modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
                CenterAlignedTopAppBar(
                    modifier = Modifier.padding(top = 16.dp),
                    title = {
                        Text(
                            text = stepTitle,
                            maxLines = 1,
                            style = MaterialTheme.typography.titleLarge,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                )

                AnimatedContent(
                    targetState = currentStep,
                    modifier = Modifier.weight(1f),
                    transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
                    label = "wizard_step",
                ) { step ->
                    when (step) {
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
                                modifier =
                                    Modifier
                                        .fillMaxSize()
                                        .padding(TFMSpacing.spacing04),
                            )
                        }
                        PlayerWizardStep.POSITIONS -> {
                            PlayerPositionsStep(
                                initialPositions = wizardViewModel.getSelectedPositions(),
                                onPositionsChanged = { positions ->
                                    wizardViewModel.setPositions(positions)
                                },
                                onSave = { wizardViewModel.savePlayer(onSuccess = onNavigateBack) },
                                onPrevious = { wizardViewModel.goToPreviousStep() },
                                modifier =
                                    Modifier
                                        .fillMaxSize()
                                        .padding(TFMSpacing.spacing04),
                            )
                        }
                    }
                }
            }

            when (val state = captainConfirmationState) {
                is CaptainConfirmationState.ConfirmReplace -> {
                    CaptainConfirmationDialog(
                        state = state,
                        onConfirm = { wizardViewModel.confirmCaptainChange(onSuccess = onNavigateBack) },
                        onDismiss = { wizardViewModel.cancelCaptainChange() },
                    )
                }
                is CaptainConfirmationState.ConfirmReplaceWithMatches -> {
                    CaptainConfirmationDialog(
                        state = state,
                        onConfirm = { keepInMatches ->
                            wizardViewModel.confirmCaptainChange(keepInMatches, onSuccess = onNavigateBack)
                        },
                        onDismiss = { wizardViewModel.cancelCaptainChange() },
                    )
                }
                is CaptainConfirmationState.ConfirmRemove -> {
                    CaptainConfirmationDialog(
                        state = state,
                        onConfirm = { wizardViewModel.confirmCaptainChange(onSuccess = onNavigateBack) },
                        onDismiss = { wizardViewModel.cancelCaptainChange() },
                    )
                }
                is CaptainConfirmationState.ConfirmRemoveWithMatches -> {
                    CaptainConfirmationDialog(
                        state = state,
                        onConfirm = { keepInMatches ->
                            wizardViewModel.confirmCaptainChange(keepInMatches, onSuccess = onNavigateBack)
                        },
                        onDismiss = { wizardViewModel.cancelCaptainChange() },
                    )
                }
                CaptainConfirmationState.None -> {}
            }
        }
    }

    if (showExitDialog) {
        AppAlertDialog(
            title = stringResource(R.string.unsaved_changes_title),
            message = stringResource(R.string.discard_message),
            confirmText = stringResource(R.string.discard),
            dismissText = stringResource(R.string.cancel),
            onConfirm = { wizardViewModel.discardChanges(onNavigateBack) },
            onDismiss = { wizardViewModel.dismissExitDialog() },
        )
    }
}
