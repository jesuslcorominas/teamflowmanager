package com.jesuslcorominas.teamflowmanager.ui.matches.wizard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.ui.components.AppAlertDialog
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import com.jesuslcorominas.teamflowmanager.viewmodel.MatchCreationWizardUiState
import com.jesuslcorominas.teamflowmanager.viewmodel.MatchCreationWizardViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.MatchListViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.WizardStep
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchCreationWizardScreen(
    onNavigateBack: () -> Unit,
    wizardViewModel: MatchCreationWizardViewModel = koinViewModel(),
    listViewModel: MatchListViewModel = koinViewModel(),
) {
    val uiState by wizardViewModel.uiState.collectAsState()
    val currentStep by wizardViewModel.currentStep.collectAsState()
    val scope = rememberCoroutineScope()
    
    var showDefaultCaptainDialog by remember { mutableStateOf(false) }
    var captainForDialog by remember { mutableStateOf<com.jesuslcorominas.teamflowmanager.domain.model.Player?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentStep) {
                            WizardStep.GENERAL_DATA -> stringResource(R.string.wizard_step_general_data)
                            WizardStep.SQUAD_CALLUP -> stringResource(R.string.wizard_step_squad_callup)
                            WizardStep.CAPTAIN -> stringResource(R.string.wizard_step_captain)
                            WizardStep.STARTING_LINEUP -> stringResource(R.string.wizard_step_lineup)
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.close),
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        when (val state = uiState) {
            is MatchCreationWizardUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                )
            }
            is MatchCreationWizardUiState.Ready -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    when (currentStep) {
                        WizardStep.GENERAL_DATA -> {
                            GeneralDataStep(
                                initialOpponent = wizardViewModel.getOpponent(),
                                initialLocation = wizardViewModel.getLocation(),
                                initialDate = wizardViewModel.getDate(),
                                onDataChanged = { opponent, location, date ->
                                    wizardViewModel.setGeneralData(opponent, location, date)
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
                                hasGoalkeepersInSquad = wizardViewModel.hasGoalkeepersInSquad(),
                                onSelectionChanged = { playerIds ->
                                    wizardViewModel.setStartingLineup(playerIds)
                                },
                                onCreate = {
                                    val match = wizardViewModel.buildMatch()
                                    listViewModel.createMatch(match)
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
}
