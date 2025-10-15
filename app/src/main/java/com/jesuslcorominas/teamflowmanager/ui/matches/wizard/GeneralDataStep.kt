package com.jesuslcorominas.teamflowmanager.ui.matches.wizard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.ui.components.AppAlertDialog
import com.jesuslcorominas.teamflowmanager.ui.components.AppTextField
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralDataStep(
    initialOpponent: String,
    initialLocation: String,
    initialDate: Long?,
    onDataChanged: (String, String, Long?, Long?, Int, Int) -> Unit,
    onNext: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var opponent by remember { mutableStateOf(initialOpponent) }
    var location by remember { mutableStateOf(initialLocation) }
    var selectedDateMillis by remember { mutableLongStateOf(initialDate ?: System.currentTimeMillis()) }
    var selectedTimeMillis by remember { mutableLongStateOf(0L) }
    var numberOfPeriods by remember { mutableIntStateOf(2) }
    var periodDurationMinutes by remember { mutableIntStateOf(25) }
    
    var opponentError by remember { mutableStateOf<String?>(null) }
    var locationError by remember { mutableStateOf<String?>(null) }
    var dateError by remember { mutableStateOf<String?>(null) }
    var timeError by remember { mutableStateOf<String?>(null) }
    
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    
    val formattedDate = remember(selectedDateMillis) {
        dateFormatter.format(Date(selectedDateMillis))
    }
    
    val formattedTime = remember(selectedTimeMillis) {
        if (selectedTimeMillis > 0) {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = selectedTimeMillis
            String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
        } else {
            ""
        }
    }
    
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(TFMSpacing.spacing03)
    ) {
        Text(
            text = stringResource(R.string.wizard_step_general_data),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(TFMSpacing.spacing02))
        
        AppTextField(
            value = opponent,
            onValueChange = {
                opponent = it
                opponentError = null
            },
            label = { Text(stringResource(R.string.opponent)) },
            isError = opponentError != null,
            supportingText = if (opponentError != null) {
                { Text(opponentError!!) }
            } else null,
            modifier = Modifier.fillMaxWidth(),
        )
        
        AppTextField(
            value = location,
            onValueChange = {
                location = it
                locationError = null
            },
            label = { Text(stringResource(R.string.location)) },
            isError = locationError != null,
            supportingText = if (locationError != null) {
                { Text(locationError!!) }
            } else null,
            modifier = Modifier.fillMaxWidth(),
        )
        
        // Date Picker
        OutlinedTextField(
            value = formattedDate,
            onValueChange = {},
            label = { Text(stringResource(R.string.match_date)) },
            readOnly = true,
            isError = dateError != null,
            supportingText = if (dateError != null) {
                { Text(dateError!!) }
            } else null,
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Default.DateRange, contentDescription = null)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Time Picker
        OutlinedTextField(
            value = formattedTime,
            onValueChange = {},
            label = { Text(stringResource(R.string.match_time)) },
            readOnly = true,
            isError = timeError != null,
            supportingText = if (timeError != null) {
                { Text(timeError!!) }
            } else null,
            trailingIcon = {
                IconButton(onClick = { showTimePicker = true }) {
                    Icon(Icons.Default.DateRange, contentDescription = null)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Number of Periods
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.number_of_periods),
                style = MaterialTheme.typography.bodyLarge
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { 
                        if (numberOfPeriods > 1) {
                            numberOfPeriods--
                            // Adjust period duration based on number of periods
                            periodDurationMinutes = if (numberOfPeriods == 2) 25 else 12
                        }
                    }
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease")
                }
                Text(
                    text = numberOfPeriods.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(40.dp)
                )
                IconButton(
                    onClick = { 
                        numberOfPeriods++
                        // Adjust period duration based on number of periods
                        periodDurationMinutes = if (numberOfPeriods == 2) 25 else 12
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Increase")
                }
            }
        }
        
        // Period Duration
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.period_duration),
                style = MaterialTheme.typography.bodyLarge
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { if (periodDurationMinutes > 1) periodDurationMinutes-- }
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease")
                }
                Text(
                    text = periodDurationMinutes.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(40.dp)
                )
                IconButton(
                    onClick = { periodDurationMinutes++ }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Increase")
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
            
            Button(
                onClick = {
                    var hasError = false
                    if (opponent.isBlank()) {
                        opponentError = stringResource(R.string.opponent_required)
                        hasError = true
                    }
                    if (location.isBlank()) {
                        locationError = stringResource(R.string.location_required)
                        hasError = true
                    }
                    if (selectedTimeMillis == 0L) {
                        timeError = stringResource(R.string.match_time_required)
                        hasError = true
                    }
                    
                    if (!hasError) {
                        onDataChanged(opponent, location, selectedDateMillis, selectedTimeMillis, numberOfPeriods, periodDurationMinutes)
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
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
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
                    }
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    // Time Picker Dialog
    if (showTimePicker) {
        val calendar = Calendar.getInstance()
        if (selectedTimeMillis > 0) {
            calendar.timeInMillis = selectedTimeMillis
        }
        val timePickerState = rememberTimePickerState(
            initialHour = calendar.get(Calendar.HOUR_OF_DAY),
            initialMinute = calendar.get(Calendar.MINUTE)
        )
        
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text(stringResource(R.string.match_time)) },
            text = {
                TimePicker(
                    state = timePickerState,
                    modifier = Modifier.padding(16.dp)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val timeCalendar = Calendar.getInstance()
                        timeCalendar.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        timeCalendar.set(Calendar.MINUTE, timePickerState.minute)
                        timeCalendar.set(Calendar.SECOND, 0)
                        timeCalendar.set(Calendar.MILLISECOND, 0)
                        selectedTimeMillis = timeCalendar.timeInMillis
                        timeError = null
                        showTimePicker = false
                    }
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
