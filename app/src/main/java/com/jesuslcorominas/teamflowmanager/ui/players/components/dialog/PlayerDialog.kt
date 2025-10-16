package com.jesuslcorominas.teamflowmanager.ui.players.components.dialog

import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.domain.model.Position.Companion.getAllPositions
import com.jesuslcorominas.teamflowmanager.ui.components.AppDialog
import com.jesuslcorominas.teamflowmanager.ui.components.AppTextField
import com.jesuslcorominas.teamflowmanager.ui.util.toLocalizedString

@Composable
fun PlayerDialog(
    player: Player? = null,
    onDismiss: () -> Unit,
    onSave: (Player) -> Unit
) {
    var formState by remember { mutableStateOf(player.toFormState()) }

    val validateAndSave = {
        formState = formState.copy(
            errors = FormErrors(
                firstName = formState.firstName.isBlank(),
                lastName = formState.lastName.isBlank(),
                number = formState.number.isBlank()
            ),
        )

        if (!formState.errors.hasErrors) {
            onSave(formState.toPlayer())
        }
    }

    val focusManager = LocalFocusManager.current

    AppDialog(
        title = stringResource(player?.let { R.string.edit_player_title }
            ?: R.string.add_player_title),
        onConfirm = validateAndSave,
        isConfirmEnabled = !formState.errors.hasErrors,
        confirmText = stringResource(R.string.save),
        onDismiss = onDismiss,
        dismissText = stringResource(R.string.cancel)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(TFMSpacing.spacing02)
        ) {
            item {
                AppTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = formState.firstName,
                    onValueChange = {
                        formState = formState.copy(
                            firstName = it,
                            errors = formState.errors.copy(firstName = false),
                        )
                    },
                    label = { Text(stringResource(R.string.first_name)) },
                    isError = formState.errors.firstName,
                    supportingText = if (formState.errors.firstName) {
                        { Text(stringResource(R.string.first_name_required)) }
                    } else {
                        null
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = {
                        focusManager.moveFocus(FocusDirection.Down)
                    }),
                )
            }

            item {
                AppTextField(
                    value = formState.lastName,
                    onValueChange = {
                        formState = formState.copy(
                            lastName = it,
                            errors = formState.errors.copy(lastName = false),
                        )
                    },
                    label = { Text(stringResource(R.string.last_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = formState.errors.lastName,
                    supportingText = if (formState.errors.lastName) {
                        { Text(stringResource(R.string.last_name_required)) }
                    } else {
                        null
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = {
                        focusManager.moveFocus(FocusDirection.Down)
                    }),
                )
            }

            item {
                AppTextField(
                    value = formState.number,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() }) {
                            formState = formState.copy(
                                number = newValue,
                                errors = formState.errors.copy(number = false),
                            )
                        }
                    },
                    label = { Text(stringResource(R.string.number)) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = formState.errors.number,
                    supportingText = if (formState.errors.number) {
                        { Text(stringResource(R.string.number_required)) }
                    } else {
                        null
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onNext = { focusManager.clearFocus() }),
                )
            }

            item {
                Text(
                    text = stringResource(R.string.positions),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(getAllPositions()) { position ->
                PositionCheckbox(
                    position = position,
                    isSelected = formState.selectedPositions.contains(position),
                    onCheckedChange = { isChecked ->
                        formState = formState.copy(
                            selectedPositions = if (isChecked) {
                                formState.selectedPositions + position
                            } else {
                                formState.selectedPositions - position
                            }
                        )
                    }
                )
            }

            item {
                Text(
                    text = stringResource(R.string.is_captain),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { formState = formState.copy(isCaptain = !formState.isCaptain) }
                        .padding(vertical = TFMSpacing.spacing01),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(TFMSpacing.spacing01)
                ) {
                    Checkbox(
                        modifier = Modifier
                            .size(TFMSpacing.spacing06)
                            .scale(.9F),
                        checked = formState.isCaptain,
                        onCheckedChange = { formState = formState.copy(isCaptain = it) }
                    )
                    Text(
                        text = stringResource(R.string.team_captain),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun PositionCheckbox(
    position: Position,
    isSelected: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isSelected) }
            .padding(vertical = TFMSpacing.spacing01),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(TFMSpacing.spacing01)
    ) {
        Checkbox(
            modifier = Modifier
                .size(TFMSpacing.spacing06)
                .scale(.9F),
            checked = isSelected,
            onCheckedChange = onCheckedChange
        )
        Text(
            text = position.toLocalizedString(context),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

private data class PlayerFormState(
    val id: Long = 0,
    val firstName: String = "",
    val lastName: String = "",
    val number: String = "",
    val selectedPositions: List<Position> = emptyList(),
    val isCaptain: Boolean = false,
    val errors: FormErrors = FormErrors()
)

private fun Player?.toFormState(): PlayerFormState = PlayerFormState(
    id = this?.id ?: 0,
    firstName = this?.firstName ?: "",
    lastName = this?.lastName ?: "",
    number = this?.number?.toString() ?: "",
    selectedPositions = this?.positions ?: listOf(),
    isCaptain = this?.isCaptain ?: false
)

private fun PlayerFormState.toPlayer(): Player = Player(
    id = id,
    firstName = firstName,
    lastName = lastName,
    number = number.toInt(),
    positions = selectedPositions,
    isCaptain = isCaptain,
)

private data class FormErrors(
    val firstName: Boolean = false,
    val lastName: Boolean = false,
    val number: Boolean = false
) {
    val hasErrors: Boolean = firstName || lastName || number
}

@Preview
@Composable
private fun EditPlayerDialogPreview() {
    MaterialTheme {
        PlayerDialog(
            player = Player(
                id = 1,
                firstName = "John",
                lastName = "Doe",
                number = 10,
                positions = listOf(Position.Forward)
            ),
            onDismiss = {},
            onSave = {}
        )
    }
}
