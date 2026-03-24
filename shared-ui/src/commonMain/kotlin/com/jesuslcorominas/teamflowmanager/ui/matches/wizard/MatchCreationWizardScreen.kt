package com.jesuslcorominas.teamflowmanager.ui.matches.wizard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.jesuslcorominas.teamflowmanager.domain.analytics.ScreenName
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.ui.analytics.TrackScreenView
import com.jesuslcorominas.teamflowmanager.ui.components.AppBackHandler
import com.jesuslcorominas.teamflowmanager.ui.components.Loading
import com.jesuslcorominas.teamflowmanager.ui.components.dialog.AppAlertDialog
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import com.jesuslcorominas.teamflowmanager.viewmodel.MatchCreationWizardUiState
import com.jesuslcorominas.teamflowmanager.viewmodel.MatchCreationWizardViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.WizardStep
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import teamflowmanager.shared_ui.generated.resources.Res
import teamflowmanager.shared_ui.generated.resources.cancel
import teamflowmanager.shared_ui.generated.resources.discard
import teamflowmanager.shared_ui.generated.resources.discard_message
import teamflowmanager.shared_ui.generated.resources.make_default_captain_message
import teamflowmanager.shared_ui.generated.resources.make_default_captain_title
import teamflowmanager.shared_ui.generated.resources.no
import teamflowmanager.shared_ui.generated.resources.unsaved_changes_title
import teamflowmanager.shared_ui.generated.resources.yes

@Composable
fun MatchCreationWizardScreen(
    matchId: Long,
    onNavigateBack: () -> Unit,
    wizardViewModel: MatchCreationWizardViewModel = koinViewModel(parameters = { parametersOf(matchId) }),
) {
    TrackScreenView(screenName = ScreenName.MATCH_WIZARD, screenClass = "MatchCreationWizardScreen")

    // On iOS, koinViewModel caches instances in the root ViewModelStore (no NavBackStackEntry
    // lifecycle). Reset the wizard state each time the screen enters composition so that
    // navigating to "new match" after editing one does not show stale data.
    LaunchedEffect(Unit) {
        wizardViewModel.resetForMatchId(matchId)
    }

    val uiState by wizardViewModel.uiState.collectAsState()
    val currentStep by wizardViewModel.currentStep.collectAsState()
    val showExitDialog by wizardViewModel.showExitDialog.collectAsState()
    val scope = rememberCoroutineScope()

    var showDefaultCaptainDialog by remember { mutableStateOf(false) }
    var captainForDialog by remember { mutableStateOf<Player?>(null) }

    AppBackHandler(enabled = !showExitDialog) {
        wizardViewModel.requestBack(onNavigateBack)
    }

    Scaffold { paddingValues ->
        when (val state = uiState) {
            is MatchCreationWizardUiState.Loading -> Loading()
            is MatchCreationWizardUiState.Saving -> Loading()
            is MatchCreationWizardUiState.Ready -> {
                Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
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
                                onNext = { wizardViewModel.goToNextStep() },
                                onCancel = { wizardViewModel.requestBack(onNavigateBack) },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(TFMSpacing.spacing04),
                            )
                        }

                        WizardStep.SQUAD_CALLUP -> {
                            SquadCallUpStep(
                                players = state.players,
                                selectedPlayerIds = wizardViewModel.getSquadCallUpIds(),
                                minPlayers = wizardViewModel.getTeamTypePlayerCount(),
                                onSelectionChanged = { playerIds ->
                                    wizardViewModel.setSquadCallUp(playerIds)
                                },
                                onNext = {
                                    wizardViewModel.goToNextStep()
                                    scope.launch { wizardViewModel.loadDefaultCaptainIfExists() }
                                },
                                onPrevious = { wizardViewModel.goToPreviousStep() },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(TFMSpacing.spacing04),
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
                                onPrevious = { wizardViewModel.goToPreviousStep() },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(TFMSpacing.spacing04),
                            )
                        }

                        WizardStep.STARTING_LINEUP -> {
                            val squadPlayers = state.players.filter { it.id in wizardViewModel.getSquadCallUpIds() }
                            StartingLineupStep(
                                players = squadPlayers,
                                selectedPlayerIds = wizardViewModel.getStartingLineupIds(),
                                captainId = wizardViewModel.getCaptainId(),
                                hasGoalkeepersInSquad = wizardViewModel.hasGoalkeepersInSquad(),
                                requiredPlayers = wizardViewModel.getTeamTypePlayerCount(),
                                onSelectionChanged = { playerIds ->
                                    wizardViewModel.setStartingLineup(playerIds)
                                },
                                onCreate = {
                                    if (wizardViewModel.isEditMode()) {
                                        wizardViewModel.updateMatch { onNavigateBack() }
                                    } else {
                                        val match = wizardViewModel.buildMatch()
                                        wizardViewModel.createMatch(match) { onNavigateBack() }
                                    }
                                },
                                onPrevious = { wizardViewModel.goToPreviousStep() },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(TFMSpacing.spacing04),
                            )
                        }
                    }
                }
            }
        }
    }

    // Default captain dialog
    if (showDefaultCaptainDialog && captainForDialog != null) {
        AppAlertDialog(
            title = stringResource(Res.string.make_default_captain_title),
            message = stringResource(
                Res.string.make_default_captain_message,
                "${captainForDialog!!.firstName} ${captainForDialog!!.lastName}",
            ),
            confirmText = stringResource(Res.string.yes),
            dismissText = stringResource(Res.string.no),
            onConfirm = {
                captainForDialog?.let { wizardViewModel.setDefaultCaptain(it.id) }
                showDefaultCaptainDialog = false
                wizardViewModel.goToNextStep()
            },
            onDismiss = {
                showDefaultCaptainDialog = false
                wizardViewModel.goToNextStep()
            },
        )
    }

    // Unsaved changes dialog
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
