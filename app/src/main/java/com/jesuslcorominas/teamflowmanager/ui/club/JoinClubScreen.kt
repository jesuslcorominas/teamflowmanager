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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.R
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
    TrackScreenView(screenName = ScreenName.TEAM, screenClass = "JoinClubScreen")

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
                Text(text = stringResource(R.string.join_club_success_title))
            },
            text = {
                Column {
                    Text(text = stringResource(R.string.join_club_success_message, successState.clubName))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = stringResource(R.string.join_club_team_linked, successState.teamName))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = stringResource(R.string.join_club_role_assigned, successState.role))
                }
            },
            confirmButton = {
                TextButton(onClick = { onNavigateBack() }) {
                    Text(stringResource(R.string.accept))
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
                Text(text = stringResource(R.string.error_title))
            },
            text = {
                Text(text = errorState.message)
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissError() }) {
                    Text(stringResource(R.string.accept))
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
                .padding(TFMSpacing.spacing04)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.join_club_title),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(TFMSpacing.spacing04))

                Text(
                    text = stringResource(R.string.join_club_description),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(TFMSpacing.spacing06))

                OutlinedTextField(
                    value = invitationCode,
                    onValueChange = { viewModel.onInvitationCodeChange(it) },
                    label = { Text(stringResource(R.string.invitation_code)) },
                    placeholder = { Text(stringResource(R.string.invitation_code_placeholder)) },
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

                Spacer(modifier = Modifier.height(TFMSpacing.spacing06))

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
                        text = if (uiState is JoinClubUiState.Loading) {
                            stringResource(R.string.joining_club)
                        } else {
                            stringResource(R.string.join_club_button)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(TFMSpacing.spacing04))

                Text(
                    text = stringResource(R.string.orphan_team_info),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
