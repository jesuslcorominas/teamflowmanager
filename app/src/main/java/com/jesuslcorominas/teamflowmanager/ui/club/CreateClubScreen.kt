package com.jesuslcorominas.teamflowmanager.ui.club

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.analytics.ScreenName
import com.jesuslcorominas.teamflowmanager.ui.TeamFlowManagerIcon
import com.jesuslcorominas.teamflowmanager.ui.analytics.TrackScreenView
import com.jesuslcorominas.teamflowmanager.ui.components.form.AppTextField
import com.jesuslcorominas.teamflowmanager.viewmodel.ClubNameError
import com.jesuslcorominas.teamflowmanager.viewmodel.CreateClubViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun CreateClubScreen(
    onClubCreated: () -> Unit,
    viewModel: CreateClubViewModel = koinViewModel()
) {
    TrackScreenView(screenName = ScreenName.CREATE_CLUB, screenClass = "CreateClubScreen")

    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val clubName by viewModel.clubName.collectAsState()
    val clubNameError by viewModel.clubNameError.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Handle success
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is CreateClubViewModel.UiState.Success -> {
                showSuccessDialog = true
                delay(2000) // Auto-redirect after 2 seconds
                viewModel.resetState()
                onClubCreated()
            }
            is CreateClubViewModel.UiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetState()
            }
            else -> { /* No action needed */ }
        }
    }

    // Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { /* Prevent dismissal, will auto-redirect */ },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = stringResource(id = R.string.create_club_success_title),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(id = R.string.create_club_success_message),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(id = R.string.create_club_redirecting),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        viewModel.resetState()
                        onClubCreated()
                    }
                ) {
                    Text(stringResource(id = R.string.create_club_continue))
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            TeamFlowManagerIcon()

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(id = R.string.create_club_title),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = R.string.create_club_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(48.dp))

            AppTextField(
                value = clubName,
                onValueChange = { viewModel.onClubNameChanged(it) },
                label = { Text(stringResource(id = R.string.club_name_label)) },
                isError = clubNameError != null,
                supportingText = clubNameError?.let { errorResId ->
                    {
                        Text(
                            text = stringResource(
                                id = when (errorResId) {
                                    ClubNameError.EMPTY_NAME -> R.string.club_name_error_empty
                                    ClubNameError.NAME_TOO_SHORT -> R.string.club_name_error_too_short
                                    ClubNameError.NAME_TOO_LONG -> R.string.club_name_error_too_long
                                }
                            )
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                    capitalization = KeyboardCapitalization.Words
                ),
                readOnly = uiState is CreateClubViewModel.UiState.Loading
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.createClub() },
                enabled = uiState !is CreateClubViewModel.UiState.Loading && clubName.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.small
            ) {
                if (uiState is CreateClubViewModel.UiState.Loading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.height(24.dp)
                    )
                } else {
                    Text(
                        text = stringResource(id = R.string.create_club_button),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
