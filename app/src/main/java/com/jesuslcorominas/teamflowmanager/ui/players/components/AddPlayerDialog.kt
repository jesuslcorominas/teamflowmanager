package com.jesuslcorominas.teamflowmanager.ui.players.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.model.Player

@Composable
fun AddPlayerDialog(
    onDismiss: () -> Unit,
    onSave: (Player) -> Unit,
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    var firstNameError by remember { mutableStateOf(false) }
    var lastNameError by remember { mutableStateOf(false) }
    var numberError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                val focusManager = LocalFocusManager.current

                Text(
                    text = stringResource(R.string.add_player),
                    style = MaterialTheme.typography.headlineSmall,
                )

                OutlinedTextField(
                    value = firstName,
                    onValueChange = {
                        firstName = it
                        firstNameError = false
                    },
                    label = { Text(stringResource(R.string.first_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = firstNameError,
                    supportingText =
                        if (firstNameError) {
                            { Text(stringResource(R.string.first_name_required)) }
                        } else {
                            null
                        },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions =
                        KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) },
                        ),
                )

                OutlinedTextField(
                    value = lastName,
                    onValueChange = {
                        lastName = it
                        lastNameError = false
                    },
                    label = { Text(stringResource(R.string.last_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = lastNameError,
                    supportingText =
                        if (lastNameError) {
                            { Text(stringResource(R.string.last_name_required)) }
                        } else {
                            null
                        },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions =
                        KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) },
                        ),
                )

                OutlinedTextField(
                    value = number,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() }) {
                            number = newValue
                            numberError = false
                        }
                    },
                    label = { Text(stringResource(R.string.number)) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = numberError,
                    supportingText =
                        if (numberError) {
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
                            onNext = { focusManager.clearFocus() }, // TODO call viewmodel to save player
                        ),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.padding(end = 8.dp),
                    ) {
                        Text(stringResource(R.string.cancel))
                    }

                    Button(
                        onClick = {
                            // TODO move to viewmodel?
                            var hasError = false

                            if (firstName.isBlank()) {
                                firstNameError = true
                                hasError = true
                            }

                            if (lastName.isBlank()) {
                                lastNameError = true
                                hasError = true
                            }

                            if (!hasError) {
                                onSave(
                                    Player(
                                        firstName = firstName,
                                        lastName = lastName,
                                        number = number.toInt(),
                                        positions = emptyList(),
                                    ),
                                )
                            }
                        },
                    ) {
                        Text(stringResource(R.string.save))
                    }
                }
            }
        }
    }
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
