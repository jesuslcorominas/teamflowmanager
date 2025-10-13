package com.jesuslcorominas.teamflowmanager.ui.components

import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMAppTheme

@Composable
fun EditTeamDialog(
    team: Team,
    onDismiss: () -> Unit,
    onSave: (Team) -> Unit,
) {
    var formState by remember { mutableStateOf(team.toFormState()) }

    val validateAndSave = {
        formState =
            formState.copy(
                errors =
                    FormErrors(
                        name = formState.name.isBlank(),
                        coachName = formState.coachName.isBlank(),
                        delegateName = formState.delegateName.isBlank(),
                    ),
            )

        if (!formState.errors.hasErrors) {
            onSave(formState.toTeam())
        }
    }

    val focusManager = LocalFocusManager.current

    AppDialog(
        title = stringResource(R.string.edit_team_title),
        onConfirm = validateAndSave,
        isConfirmEnabled = !formState.errors.hasErrors,
        confirmText = stringResource(R.string.save),
        onDismiss = onDismiss,
        dismissText = stringResource(R.string.cancel),
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(TFMSpacing.spacing02),
        ) {
            AppTextField(
                modifier = Modifier.fillMaxWidth(),
                value = formState.name,
                onValueChange = {
                    formState =
                        formState.copy(
                            name = it,
                            errors = formState.errors.copy(name = false),
                        )
                },
                label = { Text(stringResource(R.string.team_name)) },
                isError = formState.errors.name,
                supportingText =
                    if (formState.errors.name) {
                        { Text(stringResource(R.string.team_name_required)) }
                    } else {
                        null
                    },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions =
                    KeyboardActions(
                        onNext = {
                            focusManager.moveFocus(FocusDirection.Down)
                        },
                    ),
            )

            AppTextField(
                modifier = Modifier.fillMaxWidth(),
                value = formState.coachName,
                onValueChange = {
                    formState =
                        formState.copy(
                            coachName = it,
                            errors = formState.errors.copy(coachName = false),
                        )
                },
                label = { Text(stringResource(R.string.coach_name)) },
                isError = formState.errors.coachName,
                supportingText =
                    if (formState.errors.coachName) {
                        { Text(stringResource(R.string.coach_name_required)) }
                    } else {
                        null
                    },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions =
                    KeyboardActions(
                        onNext = {
                            focusManager.moveFocus(FocusDirection.Down)
                        },
                    ),
            )

            AppTextField(
                modifier = Modifier.fillMaxWidth(),
                value = formState.delegateName,
                onValueChange = {
                    formState =
                        formState.copy(
                            delegateName = it,
                            errors = formState.errors.copy(delegateName = false),
                        )
                },
                label = { Text(stringResource(R.string.delegate_name)) },
                isError = formState.errors.delegateName,
                supportingText =
                    if (formState.errors.delegateName) {
                        { Text(stringResource(R.string.delegate_name_required)) }
                    } else {
                        null
                    },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onNext = { focusManager.clearFocus() }),
            )
        }
    }
}

private data class TeamFormState(
    val id: Long = 0,
    val name: String = "",
    val coachName: String = "",
    val delegateName: String = "",
    val errors: FormErrors = FormErrors(),
)

private fun Team.toFormState(): TeamFormState =
    TeamFormState(
        id = id,
        name = name,
        coachName = coachName,
        delegateName = delegateName,
    )

private fun TeamFormState.toTeam(): Team =
    Team(
        id = id,
        name = name,
        coachName = coachName,
        delegateName = delegateName,
    )

private data class FormErrors(
    val name: Boolean = false,
    val coachName: Boolean = false,
    val delegateName: Boolean = false,
) {
    val hasErrors: Boolean = name || coachName || delegateName
}

@Preview
@Composable
private fun EditTeamDialogPreview() {
    TFMAppTheme {
        EditTeamDialog(
            team =
                Team(
                    id = 1,
                    name = "Team Name Example",
                    coachName = "John Doe",
                    delegateName = "Jane Smith",
                ),
            onDismiss = {},
            onSave = {},
        )
    }
}
