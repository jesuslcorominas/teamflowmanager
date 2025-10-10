package com.jesuslcorominas.teamflowmanager.ui.team

import TFMSpacing
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.ui.components.AppTextField
import com.jesuslcorominas.teamflowmanager.viewmodel.TeamUiState
import com.jesuslcorominas.teamflowmanager.viewmodel.TeamViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun TeamScreen(
    viewModel: TeamViewModel = koinViewModel(),
    onNavigateToPlayers: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    when (uiState) {
        is TeamUiState.Loading -> LoadingState()
        is TeamUiState.NoTeam ->
            CreateTeamForm(
                onSave = { name, coachName, delegateName ->
                    viewModel.createTeam(name, coachName, delegateName)
                    onNavigateToPlayers(name)
                },
            )

        is TeamUiState.TeamExists -> {
            val team = (uiState as TeamUiState.TeamExists).team
            onNavigateToPlayers(team.name)
        }
    }
}

@Composable
private fun LoadingState() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun CreateTeamForm(onSave: (String, String, String) -> Unit) {
    var teamName by remember { mutableStateOf("") }
    var coachName by remember { mutableStateOf("") }
    var delegateName by remember { mutableStateOf("") }
    var teamNameError by remember { mutableStateOf(false) }
    var coachNameError by remember { mutableStateOf(false) }
    var delegateNameError by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(TFMSpacing.spacing04),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.create_team_title),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = TFMSpacing.spacing04),
            )

            AppTextField(
                value = teamName,
                onValueChange = {
                    teamName = it
                    teamNameError = false
                },
                label = stringResource(R.string.team_name),
                isError = teamNameError,
                errorMessage = if (teamNameError) stringResource(R.string.team_name_required) else null,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = TFMSpacing.spacing03),
            )

            AppTextField(
                value = coachName,
                onValueChange = {
                    coachName = it
                    coachNameError = false
                },
                label = stringResource(R.string.coach_name),
                isError = coachNameError,
                errorMessage = if (coachNameError) stringResource(R.string.coach_name_required) else null,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = TFMSpacing.spacing03),
            )

            AppTextField(
                value = delegateName,
                onValueChange = {
                    delegateName = it
                    delegateNameError = false
                },
                label = stringResource(R.string.delegate_name),
                isError = delegateNameError,
                errorMessage = if (delegateNameError) stringResource(R.string.delegate_name_required) else null,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = TFMSpacing.spacing04),
            )

            Button(
                onClick = {
                    teamNameError = teamName.isBlank()
                    coachNameError = coachName.isBlank()
                    delegateNameError = delegateName.isBlank()

                    if (!teamNameError && !coachNameError && !delegateNameError) {
                        onSave(teamName, coachName, delegateName)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(R.string.save))
            }
        }
    }
}
