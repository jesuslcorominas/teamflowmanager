package com.jesuslcorominas.teamflowmanager.ui.matches.wizard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.ui.components.Loading
import com.jesuslcorominas.teamflowmanager.ui.components.dialog.AppAlertDialog
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import com.jesuslcorominas.teamflowmanager.viewmodel.MatchCreationWizardUiState
import com.jesuslcorominas.teamflowmanager.viewmodel.MatchCreationWizardViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.WizardStep
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun MatchCreationWizardScreen(
    onNavigateBack: () -> Unit,
    wizardViewModel: MatchCreationWizardViewModel = koinViewModel()
) {
    val uiState by wizardViewModel.uiState.collectAsState()
    val currentStep by wizardViewModel.currentStep.collectAsState()
    val scope = rememberCoroutineScope()

    var showDefaultCaptainDialog by remember { mutableStateOf(false) }
    var captainForDialog by remember { mutableStateOf<Player?>(null) }

    when (val state = uiState) {
        is MatchCreationWizardUiState.Loading -> Loading()
        is MatchCreationWizardUiState.Ready -> {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                    when (currentStep) {
                        WizardStep.GENERAL_DATA -> {
                            GeneralDataStep(
                                initialOpponent = wizardViewModel.getOpponent(),
                                initialLocation = wizardViewModel.getLocation(),
                                initialDate = wizardViewModel.getDate(),
                                initialTime = wizardViewModel.getTime(),
                                initialNumberOfPeriods = wizardViewModel.getNumberOfPeriods(),
                                onDataChanged = { opponent, location, date, time, numberOfPeriods ->
                                    wizardViewModel.setGeneralData(opponent, location, date, time, numberOfPeriods)
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
                        WizardStep.SQUAD_CALLUP -> {
                            SquadCallUpStep(
                                players = state.players,
                                selectedPlayerIds = wizardViewModel.getSquadCallUpIds(),
                                onSelectionChanged = { playerIds ->
                                    wizardViewModel.setSquadCallUp(playerIds)
                                },
                                onNext = {
                                    wizardViewModel.goToNextStep()
                                    // Load default captain if exists for the captain step
                                    scope.launch {
                                        wizardViewModel.loadDefaultCaptainIfExists()
                                    }
                                },
                                onPrevious = {
                                    wizardViewModel.goToPreviousStep()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(TFMSpacing.spacing04)
                            )
                        }
                        WizardStep.CAPTAIN -> {
                            val squadPlayers = state.players.filter { it.id in wizardViewModel.getSquadCallUpIds() }
                            CaptainSelectionStep(
                                players = squadPlayers,
                                selectedCaptainId = wizardViewModel.getCaptainId(),
                                onCaptainChanged = { captainId ->
                                    wizardViewModel.setCaptain(captainId)
                                },
                                onNext = {
                                    scope.launch {
                                        val (shouldAsk, player) = wizardViewModel.checkIfShouldAskForDefaultCaptain()
                                        if (shouldAsk && player != null) {
                                            captainForDialog = player
                                            showDefaultCaptainDialog = true
                                        } else {
                                            wizardViewModel.goToNextStep()
                                        }
                                    }
                                },
                                onPrevious = {
                                    wizardViewModel.goToPreviousStep()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(TFMSpacing.spacing04)
                            )
                        }
                        WizardStep.STARTING_LINEUP -> {
                            val squadPlayers = state.players.filter { it.id in wizardViewModel.getSquadCallUpIds() }
                            StartingLineupStep(
                                players = squadPlayers,
                                selectedPlayerIds = wizardViewModel.getStartingLineupIds(),
                                captainId = wizardViewModel.getCaptainId(),
                                hasGoalkeepersInSquad = wizardViewModel.hasGoalkeepersInSquad(),
                                onSelectionChanged = { playerIds ->
                                    wizardViewModel.setStartingLineup(playerIds)
                                },
                                onCreate = {
                                    val match = wizardViewModel.buildMatch()
                                    wizardViewModel.createMatch(match)
                                    onNavigateBack()
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
        }
    }

    // Default captain dialog
    if (showDefaultCaptainDialog && captainForDialog != null) {
        AppAlertDialog(
            title = stringResource(R.string.make_default_captain_title),
            message = stringResource(
                R.string.make_default_captain_message,
                "${captainForDialog!!.firstName} ${captainForDialog!!.lastName}"
            ),
            confirmText = stringResource(R.string.yes),
            dismissText = stringResource(R.string.no),
            onConfirm = {
                captainForDialog?.let { wizardViewModel.setDefaultCaptain(it.id) }
                showDefaultCaptainDialog = false
                wizardViewModel.goToNextStep()
            },
            onDismiss = {
                showDefaultCaptainDialog = false
                wizardViewModel.goToNextStep()
            }
        )
    }
}
