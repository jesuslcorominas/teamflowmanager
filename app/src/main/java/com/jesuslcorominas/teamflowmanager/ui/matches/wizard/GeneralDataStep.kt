package com.jesuslcorominas.teamflowmanager.ui.matches.wizard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.ui.components.AppTextField
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing

@Composable
fun GeneralDataStep(
    initialOpponent: String,
    initialLocation: String,
    initialDate: Long?,
    onDataChanged: (String, String, Long?) -> Unit,
    onNext: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var opponent by remember { mutableStateOf(initialOpponent) }
    var location by remember { mutableStateOf(initialLocation) }
    var opponentError by remember { mutableStateOf<String?>(null) }
    var locationError by remember { mutableStateOf<String?>(null) }
    
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
        
        // TODO: Add date and time pickers in a future enhancement
        // For now, we'll use the current timestamp
        
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
                    
                    if (!hasError) {
                        onDataChanged(opponent, location, System.currentTimeMillis())
                        onNext()
                    }
                },
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.next))
            }
        }
    }
}
