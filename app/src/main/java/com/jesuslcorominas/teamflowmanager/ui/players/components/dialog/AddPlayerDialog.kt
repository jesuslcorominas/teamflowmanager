package com.jesuslcorominas.teamflowmanager.ui.players.components.dialog

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.ui.components.AppDialog
import com.jesuslcorominas.teamflowmanager.ui.components.AppTextField

@Composable
fun AddPlayerDialog(
    onDismiss: () -> Unit,
    onSave: (Player) -> Unit,
) {
    var formState by remember { mutableStateOf(AddPlayerFormState()) }

    val validateAndSave = {
        formState =
            formState.copy(
                errors =
                    FormErrors(
                        firstName = formState.firstName.isBlank(),
                        lastName = formState.lastName.isBlank(),
                        number = formState.number.isBlank(),
                    ),
            )

        if (!formState.errors.hasErrors) {
            onSave(formState.toPlayer())
        }
    }

    AppDialog(
        title = stringResource(R.string.add_player),
        onDismiss = onDismiss,
        onConfirm = validateAndSave,
        confirmText = stringResource(R.string.save),
        dismissText = stringResource(R.string.cancel),
    ) {
        val focusManager = LocalFocusManager.current

        AppTextField(
            modifier = Modifier.fillMaxWidth(),
            value = formState.firstName,
            onValueChange = {
                formState =
                    formState.copy(
                        firstName = it,
                        errors = formState.errors.copy(firstName = false),
                    )
            },
            label = { Text(stringResource(R.string.first_name)) },
            isError = formState.errors.firstName,
            supportingText =
                if (formState.errors.firstName) {
                    { Text(stringResource(R.string.first_name_required)) }
                } else {
                    null
                },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions =
                KeyboardActions(onNext = {
                    focusManager.moveFocus(FocusDirection.Down)
                }),
        )

        AppTextField(
            value = formState.lastName,
            onValueChange = {
                formState =
                    formState.copy(
                        lastName = it,
                        errors = formState.errors.copy(lastName = false),
                    )
            },
            label = { Text(stringResource(R.string.last_name)) },
            modifier = Modifier.fillMaxWidth(),
            isError = formState.errors.lastName,
            supportingText =
                if (formState.errors.lastName) {
                    { Text(stringResource(R.string.last_name_required)) }
                } else {
                    null
                },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions =
                KeyboardActions(onNext = {
                    focusManager.moveFocus(FocusDirection.Down)
                }),
        )

        AppTextField(
            value = formState.number,
            onValueChange = { newValue ->
                if (newValue.all { it.isDigit() }) {
                    formState =
                        formState.copy(
                            number = newValue,
                            errors = formState.errors.copy(number = false),
                        )
                }
            },
            label = { Text(stringResource(R.string.number)) },
            modifier = Modifier.fillMaxWidth(),
            isError = formState.errors.number,
            supportingText =
                if (formState.errors.number) {
                    { Text(stringResource(R.string.number_required)) }
                } else {
                    null
                },
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                ),
            keyboardActions =
                KeyboardActions(
                    onNext = {
                        focusManager.clearFocus()

                        validateAndSave()
                    },
                ),
        )
    }
}

private data class AddPlayerFormState(
    val firstName: String = "",
    val lastName: String = "",
    val number: String = "",
    val errors: FormErrors = FormErrors(),
)

private fun AddPlayerFormState.toPlayer(): Player =
    Player(
        firstName = firstName,
        lastName = lastName,
        number = number.toInt(),
        positions = emptyList(),
    )

data class FormErrors(
    val firstName: Boolean = false,
    val lastName: Boolean = false,
    val number: Boolean = false,
) {
    val hasErrors: Boolean = firstName || lastName || number
}

@Preview(showBackground = true)
@Composable
private fun AddPlayerDialogPreview() {
    MaterialTheme {
        AddPlayerDialog(
            onDismiss = {},
            onSave = {},
        )
    }
}
