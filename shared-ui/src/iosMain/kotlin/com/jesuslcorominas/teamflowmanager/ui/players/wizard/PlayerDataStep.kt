package com.jesuslcorominas.teamflowmanager.ui.players.wizard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.ui.components.form.AppTextField
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import org.jetbrains.compose.resources.stringResource
import teamflowmanager.shared_ui.generated.resources.Res
import teamflowmanager.shared_ui.generated.resources.cancel
import teamflowmanager.shared_ui.generated.resources.first_name
import teamflowmanager.shared_ui.generated.resources.first_name_required
import teamflowmanager.shared_ui.generated.resources.is_captain
import teamflowmanager.shared_ui.generated.resources.last_name
import teamflowmanager.shared_ui.generated.resources.last_name_required
import teamflowmanager.shared_ui.generated.resources.next
import teamflowmanager.shared_ui.generated.resources.number
import teamflowmanager.shared_ui.generated.resources.number_required
import teamflowmanager.shared_ui.generated.resources.player_data_step_title
import teamflowmanager.shared_ui.generated.resources.tap_to_add_photo

@Composable
actual fun PlayerDataStep(
    initialFirstName: String,
    initialLastName: String,
    initialNumber: String,
    initialIsCaptain: Boolean,
    initialImageUri: String?,
    onDataChanged: (String, String, String, Boolean, String?) -> Unit,
    onNext: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier,
) {
    val focusManager = LocalFocusManager.current

    var firstName by remember(initialFirstName) { mutableStateOf(initialFirstName) }
    var lastName by remember(initialLastName) { mutableStateOf(initialLastName) }
    var number by remember(initialNumber) { mutableStateOf(initialNumber) }
    var isCaptain by remember(initialIsCaptain) { mutableStateOf(initialIsCaptain) }

    var firstNameError by remember { mutableStateOf<String?>(null) }
    var lastNameError by remember { mutableStateOf<String?>(null) }
    var numberError by remember { mutableStateOf<String?>(null) }

    val firstNameRequired = stringResource(Res.string.first_name_required)
    val lastNameRequired = stringResource(Res.string.last_name_required)
    val numberRequired = stringResource(Res.string.number_required)

    val validateAndNext = {
        firstName = firstName.trim()
        lastName = lastName.trim()
        number = number.trim()

        firstNameError = if (firstName.isBlank()) firstNameRequired else null
        lastNameError = if (lastName.isBlank()) lastNameRequired else null
        numberError = if (number.isBlank()) numberRequired else null

        if (firstNameError == null && lastNameError == null && numberError == null) {
            onDataChanged(firstName, lastName, number, isCaptain, initialImageUri)
            onNext()
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(TFMSpacing.spacing03),
    ) {
        Text(
            text = stringResource(Res.string.player_data_step_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(TFMSpacing.spacing02))

        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(TFMSpacing.spacing01))
            Text(
                text = stringResource(Res.string.tap_to_add_photo),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.height(TFMSpacing.spacing02))

        AppTextField(
            modifier = Modifier.fillMaxWidth(),
            value = firstName,
            onValueChange = { firstName = it; firstNameError = null },
            label = { Text(stringResource(Res.string.first_name)) },
            isError = firstNameError != null,
            supportingText = if (firstNameError != null) { { Text(firstNameError!!) } } else null,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next, capitalization = KeyboardCapitalization.Words),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
        )

        AppTextField(
            modifier = Modifier.fillMaxWidth(),
            value = lastName,
            onValueChange = { lastName = it; lastNameError = null },
            label = { Text(stringResource(Res.string.last_name)) },
            isError = lastNameError != null,
            supportingText = if (lastNameError != null) { { Text(lastNameError!!) } } else null,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next, capitalization = KeyboardCapitalization.Words),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
        )

        AppTextField(
            modifier = Modifier.fillMaxWidth(),
            value = number,
            onValueChange = { newValue -> if (newValue.all { it.isDigit() }) { number = newValue; numberError = null } },
            label = { Text(stringResource(Res.string.number)) },
            isError = numberError != null,
            supportingText = if (numberError != null) { { Text(numberError!!) } } else null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = TFMSpacing.spacing01),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(TFMSpacing.spacing01),
        ) {
            Checkbox(checked = isCaptain, onCheckedChange = { isCaptain = it })
            Text(text = stringResource(Res.string.is_captain), style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            OutlinedButton(onClick = onCancel) { Text(stringResource(Res.string.cancel)) }
            Button(onClick = validateAndNext) { Text(stringResource(Res.string.next)) }
        }
    }
}
