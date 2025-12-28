package com.jesuslcorominas.teamflowmanager.ui.club

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.domain.analytics.ScreenName
import com.jesuslcorominas.teamflowmanager.ui.analytics.TrackScreenView
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import com.jesuslcorominas.teamflowmanager.viewmodel.JoinClubUiState
import com.jesuslcorominas.teamflowmanager.viewmodel.JoinClubViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun JoinClubScreen(
    onNavigateBack: () -> Unit,
    viewModel: JoinClubViewModel = koinViewModel(),
) {
    TrackScreenView(screenName = ScreenName.SETTINGS, screenClass = "JoinClubScreen")

    val uiState by viewModel.uiState.collectAsState()
    val invitationCode by viewModel.invitationCode.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Handle success - navigate back
    LaunchedEffect(uiState) {
        if (uiState is JoinClubUiState.Success) {
            // Give time for success dialog to show before navigating back
            kotlinx.coroutines.delay(2000)
            onNavigateBack()
        }
    }

    // Success dialog
    if (uiState is JoinClubUiState.Success) {
        val successState = uiState as JoinClubUiState.Success
        AlertDialog(
            onDismissRequest = { onNavigateBack() },
            icon = {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(text = "¡Te has unido al club!")
            },
            text = {
                Column {
                    Text(text = "Bienvenido a ${successState.clubName}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Tu equipo '${successState.teamName}' ha sido vinculado al club.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Rol asignado: ${successState.role}")
                }
            },
            confirmButton = {
                TextButton(onClick = { onNavigateBack() }) {
                    Text("Aceptar")
                }
            }
        )
    }

    // Error dialog
    if (uiState is JoinClubUiState.Error) {
        val errorState = uiState as JoinClubUiState.Error
        AlertDialog(
            onDismissRequest = { viewModel.dismissError() },
            icon = {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(text = "Error")
            },
            text = {
                Text(text = errorState.message)
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissError() }) {
                    Text("Aceptar")
                }
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(TFMSpacing.medium)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Unirse a un Club",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(TFMSpacing.medium))

                Text(
                    text = "Introduce el código de invitación proporcionado por el administrador del club",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(TFMSpacing.large))

                OutlinedTextField(
                    value = invitationCode,
                    onValueChange = { viewModel.onInvitationCodeChange(it) },
                    label = { Text("Código de invitación") },
                    placeholder = { Text("INVITE123") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState !is JoinClubUiState.Loading,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            if (invitationCode.isNotBlank()) {
                                viewModel.joinClub(onSuccess = {})
                            }
                        }
                    )
                )

                Spacer(modifier = Modifier.height(TFMSpacing.large))

                Button(
                    onClick = {
                        keyboardController?.hide()
                        viewModel.joinClub(onSuccess = {})
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState !is JoinClubUiState.Loading && invitationCode.isNotBlank()
                ) {
                    if (uiState is JoinClubUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .height(24.dp)
                                .padding(end = 8.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Text(
                        text = if (uiState is JoinClubUiState.Loading) "Uniéndose..." else "Unirse al club"
                    )
                }

                Spacer(modifier = Modifier.height(TFMSpacing.medium))

                Text(
                    text = "Tu equipo huérfano existente será automáticamente vinculado al club",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
