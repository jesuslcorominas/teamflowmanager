package com.jesuslcorominas.teamflowmanager.ui.team

import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.ui.components.AppTextField
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMAppTheme
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
                onSave = { team ->
                    viewModel.createTeam(team)
                    onNavigateToPlayers(team.name)
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
private fun CreateTeamForm(onSave: (Team) -> Unit) {

    val focusManager = LocalFocusManager.current
    var formState by remember { mutableStateOf(TeamFormState()) }
    val validateAndSave = {
        formState = formState.copy(
            errors = FormErrors(
                name = formState.name.isBlank(),
                coachName = formState.coachName.isBlank(),
                delegateName = formState.delegateName.isBlank()
            ),
        )

        if (!formState.errors.hasErrors) {
            onSave(formState.toTeam())
        }
    }


    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(TFMSpacing.spacing04),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                modifier = Modifier.size(TFMSpacing.spacing18),
                painter = painterResource(id = R.drawable.ic_launcher),
                contentDescription = stringResource(R.string.app_name),
                tint = Color.Unspecified
            )

            Text(
                modifier = Modifier.padding(
                    bottom = TFMSpacing.spacing04
                ),
                text = stringResource(R.string.create_team_title),
                style = MaterialTheme.typography.headlineMedium,
            )

            AppTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = TFMSpacing.spacing03),
                value = formState.name,
                onValueChange = {
                    formState = formState.copy(
                        name = it,
                        errors = formState.errors.copy(name = false)
                    )
                },
                label = { Text(stringResource(R.string.team_name)) },
                isError = formState.errors.name,
                supportingText = if (formState.errors.name) {
                    { Text(stringResource(R.string.team_name_required)) }
                } else {
                    null
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = {
                    focusManager.moveFocus(FocusDirection.Down)
                }),
            )

            AppTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = TFMSpacing.spacing03),
                value = formState.coachName,
                onValueChange = {
                    formState = formState.copy(
                        coachName = it,
                        errors = formState.errors.copy(coachName = false)
                    )
                },
                label = { Text(stringResource(R.string.coach_name)) },
                isError = formState.errors.coachName,
                supportingText = if (formState.errors.coachName) {
                    { Text(stringResource(R.string.first_name_required)) }
                } else {
                    null
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = {
                    focusManager.moveFocus(FocusDirection.Down)
                }),
            )

            AppTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = TFMSpacing.spacing04),
                value = formState.delegateName,
                onValueChange = {
                    formState = formState.copy(
                        delegateName = it,
                        errors = formState.errors.copy(delegateName = false)
                    )
                },
                label = { Text(stringResource(R.string.delegate_name)) },
                isError = formState.errors.delegateName,
                supportingText = if (formState.errors.delegateName) {
                    { Text(stringResource(R.string.delegate_name_required)) }
                } else {
                    null
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onNext = { focusManager.clearFocus() }),
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { validateAndSave() },
            ) {
                Text(text = stringResource(R.string.save))
            }
        }
    }
}

private data class TeamFormState(
    val id: Long = 0,
    val name: String = "",
    val coachName: String = "",
    val delegateName: String = "",
    val errors: FormErrors = FormErrors()
)

private fun Team?.toFormState(): TeamFormState = TeamFormState(
    id = this?.id ?: 0,
    name = this?.name ?: "",
    coachName = this?.coachName ?: "",
    delegateName = this?.delegateName ?: ""
)

private fun TeamFormState.toTeam(): Team = Team(
    id = id,
    name = name,
    coachName = coachName,
    delegateName = delegateName
)

data class FormErrors(
    val name: Boolean = false,
    val coachName: Boolean = false,
    val delegateName: Boolean = false
) {
    val hasErrors: Boolean = name || coachName || delegateName
}

@Preview
@Composable
private fun EditPlayerDialogPreview() {
    TFMAppTheme {
        CreateTeamForm(
            onSave = {}
        )
    }
}
