package com.jesuslcorominas.teamflowmanager.ui.club

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.analytics.ScreenName
import com.jesuslcorominas.teamflowmanager.ui.TeamFlowManagerIcon
import com.jesuslcorominas.teamflowmanager.ui.analytics.TrackScreenView
import com.jesuslcorominas.teamflowmanager.ui.components.form.AppTextField
import com.jesuslcorominas.teamflowmanager.viewmodel.InvitationCodeError
import com.jesuslcorominas.teamflowmanager.viewmodel.JoinClubViewModel
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@Composable
fun JoinClubScreen(
    onClubJoined: () -> Unit,
    viewModel: JoinClubViewModel = koinViewModel()
) {
    TrackScreenView(screenName = ScreenName.JOIN_CLUB, screenClass = "JoinClubScreen")

    val uiState by viewModel.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Handle success - auto-redirect after delay
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is JoinClubViewModel.UiState.Success -> {
                delay(5000) // Auto-redirect after 5 seconds
                viewModel.resetState()
                onClubJoined()
            }

            is JoinClubViewModel.UiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetState()
            }

            else -> { /* No action needed */
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        // Show success state instead of form
        if (uiState is JoinClubViewModel.UiState.Success) {
            val result = (uiState as JoinClubViewModel.UiState.Success).result
            ClubJoinedSuccessfullyContent(
                paddingValues = paddingValues,
                clubName = result.club.name,
                hasOrphanTeam = result.orphanTeam != null,
                role = result.clubMember.role
            ) {
                viewModel.resetState()
                onClubJoined()
            }
        } else {
            // Show form
            JoinClubForm(
                paddingValues = paddingValues,
                viewModel = viewModel
            )
        }
    }
}

@Composable
private fun ClubJoinedSuccessfullyContent(
    paddingValues: PaddingValues,
    clubName: String,
    hasOrphanTeam: Boolean,
    role: String,
    onContinueClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(72.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(id = R.string.join_club_success_title),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(id = R.string.join_club_success_message, clubName),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(id = R.string.join_club_success_role, role),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )

        if (hasOrphanTeam) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = R.string.join_club_success_team_linked),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(id = R.string.join_club_redirecting),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onContinueClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = MaterialTheme.shapes.small
        ) {
            Text(
                text = stringResource(id = R.string.join_club_continue),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun JoinClubForm(paddingValues: PaddingValues, viewModel: JoinClubViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    val invitationCode by viewModel.invitationCode.collectAsState()
    val invitationCodeError by viewModel.invitationCodeError.collectAsState()

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
            text = stringResource(id = R.string.join_club_title),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(id = R.string.join_club_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        AppTextField(
            value = invitationCode,
            onValueChange = { viewModel.onInvitationCodeChanged(it) },
            label = { Text(stringResource(id = R.string.invitation_code_label)) },
            placeholder = { Text(stringResource(id = R.string.invitation_code_placeholder)) },
            isError = invitationCodeError != null,
            supportingText = invitationCodeError?.let { errorResId ->
                {
                    Text(
                        text = stringResource(
                            id = when (errorResId) {
                                InvitationCodeError.EMPTY_CODE -> R.string.invitation_code_error_empty
                                InvitationCodeError.CODE_TOO_SHORT -> R.string.invitation_code_error_too_short
                                InvitationCodeError.INVALID_FORMAT -> R.string.invitation_code_error_invalid_format
                            }
                        )
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                capitalization = KeyboardCapitalization.Characters
            ),
            readOnly = uiState is JoinClubViewModel.UiState.Loading
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.joinClub() },
            enabled = uiState !is JoinClubViewModel.UiState.Loading && invitationCode.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = MaterialTheme.shapes.small
        ) {
            if (uiState is JoinClubViewModel.UiState.Loading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.height(24.dp)
                )
            } else {
                Text(
                    text = stringResource(id = R.string.join_club_button),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
