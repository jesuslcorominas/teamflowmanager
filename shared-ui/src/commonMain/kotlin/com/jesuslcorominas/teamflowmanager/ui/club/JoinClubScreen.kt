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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.domain.analytics.ScreenName
import com.jesuslcorominas.teamflowmanager.ui.analytics.TrackScreenView
import com.jesuslcorominas.teamflowmanager.ui.components.form.AppTextField
import com.jesuslcorominas.teamflowmanager.viewmodel.InvitationCodeError
import com.jesuslcorominas.teamflowmanager.viewmodel.JoinClubViewModel
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import teamflowmanager.shared_ui.generated.resources.Res
import teamflowmanager.shared_ui.generated.resources.ic_launcher
import teamflowmanager.shared_ui.generated.resources.invitation_code_error_empty
import teamflowmanager.shared_ui.generated.resources.invitation_code_error_invalid_format
import teamflowmanager.shared_ui.generated.resources.invitation_code_error_too_long
import teamflowmanager.shared_ui.generated.resources.invitation_code_error_too_short
import teamflowmanager.shared_ui.generated.resources.invitation_code_label
import teamflowmanager.shared_ui.generated.resources.join_club_button
import teamflowmanager.shared_ui.generated.resources.join_club_continue
import teamflowmanager.shared_ui.generated.resources.join_club_redirecting
import teamflowmanager.shared_ui.generated.resources.join_club_subtitle
import teamflowmanager.shared_ui.generated.resources.join_club_success_message
import teamflowmanager.shared_ui.generated.resources.join_club_success_role
import teamflowmanager.shared_ui.generated.resources.join_club_success_team_linked
import teamflowmanager.shared_ui.generated.resources.join_club_success_title
import teamflowmanager.shared_ui.generated.resources.join_club_title

@Composable
fun JoinClubScreen(
    onClubJoined: () -> Unit,
    viewModel: JoinClubViewModel = koinViewModel(),
) {
    TrackScreenView(screenName = ScreenName.JOIN_CLUB, screenClass = "JoinClubScreen")

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is JoinClubViewModel.UiState.Success -> {
                delay(5000)
                viewModel.resetState()
                onClubJoined()
            }
            is JoinClubViewModel.UiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        if (uiState is JoinClubViewModel.UiState.Success) {
            val successState = uiState as JoinClubViewModel.UiState.Success
            ClubJoinedSuccessfullyContent(
                paddingValues = paddingValues,
                clubName = successState.result.club.name,
                hasOrphanTeam = successState.result.orphanTeam != null,
                role = successState.result.clubMember.roles.joinToString(", "),
            ) {
                viewModel.resetState()
                onClubJoined()
            }
        } else {
            JoinClubForm(paddingValues = paddingValues, viewModel = viewModel)
        }
    }
}

@Composable
private fun ClubJoinedSuccessfullyContent(
    paddingValues: PaddingValues,
    clubName: String,
    hasOrphanTeam: Boolean,
    role: String,
    onContinueClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(72.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(Res.string.join_club_success_title),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(Res.string.join_club_success_message, clubName),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(Res.string.join_club_success_role, role),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
        )

        if (hasOrphanTeam) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(Res.string.join_club_success_team_linked),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(Res.string.join_club_redirecting),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onContinueClick,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            shape = MaterialTheme.shapes.small,
        ) {
            Text(
                text = stringResource(Res.string.join_club_continue),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun JoinClubForm(
    paddingValues: PaddingValues,
    viewModel: JoinClubViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    val invitationCode by viewModel.invitationCode.collectAsState()
    val invitationCodeError by viewModel.invitationCodeError.collectAsState()

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            modifier = Modifier.size(144.dp),
            painter = painterResource(Res.drawable.ic_launcher),
            contentDescription = null,
            tint = Color.Unspecified,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(Res.string.join_club_title),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(Res.string.join_club_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(48.dp))

        AppTextField(
            value = invitationCode,
            onValueChange = { viewModel.onInvitationCodeChanged(it) },
            label = { Text(stringResource(Res.string.invitation_code_label)) },
            isError = invitationCodeError != null,
            supportingText =
                invitationCodeError?.let { error ->
                    {
                        Text(
                            text =
                                stringResource(
                                    when (error) {
                                        InvitationCodeError.EMPTY_CODE -> Res.string.invitation_code_error_empty
                                        InvitationCodeError.CODE_TOO_SHORT -> Res.string.invitation_code_error_too_short
                                        InvitationCodeError.CODE_TOO_LONG -> Res.string.invitation_code_error_too_long
                                        InvitationCodeError.INVALID_FORMAT -> Res.string.invitation_code_error_invalid_format
                                    },
                                ),
                        )
                    }
                },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            readOnly = uiState is JoinClubViewModel.UiState.Loading,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.joinClub() },
            enabled = uiState !is JoinClubViewModel.UiState.Loading && invitationCode.isNotBlank(),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            shape = MaterialTheme.shapes.small,
        ) {
            if (uiState is JoinClubViewModel.UiState.Loading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.height(24.dp),
                )
            } else {
                Text(
                    text = stringResource(Res.string.join_club_button),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}
