package com.jesuslcorominas.teamflowmanager.ui.matches.wizard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.ui.components.form.AppTextField
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralDataStep(
    initialOpponent: String,
    initialLocation: String,
    initialDate: Long?,
    initialTime: Long?,
    initialNumberOfPeriods: Int,
    onDataChanged: (String, String, Long?, Long?, Int) -> Unit,
    onNext: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    homeGround: String? = null,
) {
    val focusManager = LocalFocusManager.current

    var opponent by remember { mutableStateOf(initialOpponent) }
    var location by remember { mutableStateOf(initialLocation) }
    var selectedDateMillis by remember { mutableLongStateOf(initialDate ?: 0L) }
    var selectedTimeMillis by remember { mutableStateOf<Long?>(initialTime) }
    var numberOfPeriods by remember { mutableIntStateOf(initialNumberOfPeriods) }
    var opponentError by remember { mutableStateOf<String?>(null) }
    var locationError by remember { mutableStateOf<String?>(null) }
    var dateError by remember { mutableStateOf<String?>(null) }
    var timeError by remember { mutableStateOf<String?>(null) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    val formattedDate =
        remember(selectedDateMillis) {
            if (selectedDateMillis == 0L) "" else dateFormatter.format(Date(selectedDateMillis))
        }

    val formattedTime =
        remember(selectedTimeMillis) {
            selectedTimeMillis?.let {
                val hours = ((it / (60 * 60 * 1000)) % 24).toInt()
                val minutes = ((it / (60 * 1000)) % 60).toInt()
                String.format(Locale.getDefault(), "%02d:%02d", hours, minutes)
            } ?: ""
        }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(TFMSpacing.spacing03),
    ) {
        AppTextField(
            modifier = Modifier.fillMaxWidth(),
            value = opponent,
            onValueChange = {
                opponent = it
                opponentError = null
            },
            label = { Text(stringResource(R.string.opponent)) },
            isError = opponentError != null,
            supportingText =
                if (opponentError != null) {
                    { Text(opponentError!!) }
                } else {
                    null
                },
            keyboardOptions =
                KeyboardOptions(
                    imeAction = ImeAction.Next,
                    capitalization = KeyboardCapitalization.Words,
                ),
            keyboardActions =
                KeyboardActions(onNext = {
                    focusManager.moveFocus(FocusDirection.Down)
                }),
        )

        AppTextField(
            modifier = Modifier.fillMaxWidth(),
            value = location,
            onValueChange = {
                location = it
                locationError = null
            },
            label = { Text(stringResource(R.string.location)) },
            isError = locationError != null,
            supportingText =
                if (locationError != null) {
                    { Text(locationError!!) }
                } else {
                    null
                },
            keyboardOptions =
                KeyboardOptions(
                    imeAction = ImeAction.Done,
                    capitalization = KeyboardCapitalization.Words,
                ),
            keyboardActions = KeyboardActions(onNext = { focusManager.clearFocus() }),
        )

        if (homeGround != null) {
            SuggestionChip(
                onClick = {
                    location = homeGround
                    locationError = null
                    focusManager.clearFocus()
                },
                label = { Text(homeGround) },
            )
        }

        // Date Picker
        OutlinedTextField(
            value = formattedDate,
            onValueChange = {},
            label = { Text(stringResource(R.string.match_date)) },
            readOnly = true,
            isError = dateError != null,
            supportingText =
                if (dateError != null) {
                    { Text(dateError!!) }
                } else {
                    null
                },
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Default.DateRange, contentDescription = null)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            interactionSource =
                remember { MutableInteractionSource() }
                    .also { interactionSource ->
                        androidx.compose.runtime.LaunchedEffect(interactionSource) {
                            interactionSource.interactions.collect {
                                if (it is PressInteraction.Release) {
                                    showDatePicker = true
                                }
                            }
                        }
                    },
        )

        // Time Picker
        OutlinedTextField(
            value = formattedTime,
            onValueChange = {},
            label = { Text(stringResource(R.string.match_time)) },
            readOnly = true,
            isError = timeError != null,
            supportingText =
                if (timeError != null) {
                    { Text(timeError!!) }
                } else {
                    null
                },
            trailingIcon = {
                IconButton(onClick = { showTimePicker = true }) {
                    Icon(Icons.Default.AccessTime, contentDescription = null)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            interactionSource =
                remember { MutableInteractionSource() }
                    .also { interactionSource ->
                        androidx.compose.runtime.LaunchedEffect(interactionSource) {
                            interactionSource.interactions.collect {
                                if (it is PressInteraction.Release) {
                                    showTimePicker = true
                                }
                            }
                        }
                    },
        )

        // Number of Periods- Radio Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(R.string.number_of_periods),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // 2 Halves
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier =
                        Modifier
                            .weight(1f)
                            .clickable { numberOfPeriods = 2 },
                ) {
                    RadioButton(
                        selected = numberOfPeriods == 2,
                        onClick = {
                            numberOfPeriods = 2
                        },
                    )
                    Text(
                        text = stringResource(R.string.two_halves),
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }

                // 4 Quarters
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier =
                        Modifier
                            .weight(1f)
                            .clickable { numberOfPeriods = 4 },
                ) {
                    RadioButton(
                        selected = numberOfPeriods == 4,
                        onClick = { numberOfPeriods = 4 },
                    )
                    Text(
                        text = stringResource(R.string.four_quarters),
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(TFMSpacing.spacing02),
        ) {
            Button(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.cancel))
            }

            val opponentRequired = stringResource(R.string.opponent_required)
            val locationRequired = stringResource(R.string.location_required)
            val timeRequired = stringResource(R.string.match_time_required)
            val dateRequired = stringResource(R.string.match_date_required)

            Button(
                onClick = {
                    var hasError = false
                    if (opponent.isBlank()) {
                        opponentError = opponentRequired
                        hasError = true
                    }
                    if (location.isBlank()) {
                        locationError = locationRequired
                        hasError = true
                    }
                    if (selectedTimeMillis == null) {
                        timeError = timeRequired
                        hasError = true
                    }
                    if (selectedDateMillis == 0L) {
                        dateError = dateRequired
                        hasError = true
                    }

                    if (!hasError) {
                        onDataChanged(
                            opponent.trimEnd(),
                            location.trimEnd(),
                            selectedDateMillis,
                            selectedTimeMillis,
                            numberOfPeriods,
                        )
                        onNext()
                    }
                },
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.next))
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState =
            rememberDatePickerState(
                initialSelectedDateMillis = if (selectedDateMillis == 0L) System.currentTimeMillis() else selectedDateMillis,
            )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            selectedDateMillis = it
                            dateError = null
                        }
                        showDatePicker = false
                    },
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Time Picker Dialog
    if (showTimePicker) {
        val initialHour =
            selectedTimeMillis?.let {
                ((it / (60 * 60 * 1000)) % 24).toInt()
            } ?: 0
        val initialMinute =
            selectedTimeMillis?.let {
                ((it / (60 * 1000)) % 60).toInt()
            } ?: 0
        val timePickerState =
            rememberTimePickerState(
                initialHour = initialHour,
                initialMinute = initialMinute,
            )

        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text(stringResource(R.string.match_time)) },
            text = {
                TimePicker(
                    state = timePickerState,
                    modifier = Modifier.padding(16.dp),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Store only time of day as milliseconds from midnight
                        val timeInMillis =
                            (timePickerState.hour * 60 * 60 * 1000) +
                                (timePickerState.minute * 60 * 1000)
                        selectedTimeMillis = timeInMillis.toLong()
                        timeError = null
                        showTimePicker = false
                    },
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}
