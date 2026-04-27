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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.domain.analytics.ScreenName
import com.jesuslcorominas.teamflowmanager.ui.analytics.TrackScreenView
import com.jesuslcorominas.teamflowmanager.ui.components.form.AppTextField
import com.jesuslcorominas.teamflowmanager.viewmodel.ClubNameError
import com.jesuslcorominas.teamflowmanager.viewmodel.CreateClubViewModel
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import teamflowmanager.shared_ui.generated.resources.Res
import teamflowmanager.shared_ui.generated.resources.club_name_error_empty
import teamflowmanager.shared_ui.generated.resources.club_name_error_too_long
import teamflowmanager.shared_ui.generated.resources.club_name_error_too_short
import teamflowmanager.shared_ui.generated.resources.club_name_label
import teamflowmanager.shared_ui.generated.resources.create_club_button
import teamflowmanager.shared_ui.generated.resources.create_club_continue
import teamflowmanager.shared_ui.generated.resources.create_club_redirecting
import teamflowmanager.shared_ui.generated.resources.create_club_subtitle
import teamflowmanager.shared_ui.generated.resources.create_club_success_message
import teamflowmanager.shared_ui.generated.resources.create_club_success_title
import teamflowmanager.shared_ui.generated.resources.create_club_title
import teamflowmanager.shared_ui.generated.resources.ic_launcher

@Composable
fun CreateClubScreen(
    onClubCreated: () -> Unit,
    viewModel: CreateClubViewModel = koinViewModel(),
) {
    TrackScreenView(screenName = ScreenName.CREATE_CLUB, screenClass = "CreateClubScreen")

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is CreateClubViewModel.UiState.Success -> {
                delay(5000)
                viewModel.resetState()
                onClubCreated()
            }
            is CreateClubViewModel.UiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        if (uiState is CreateClubViewModel.UiState.Success) {
            ClubCreatedSuccessfullyContent(paddingValues = paddingValues) {
                viewModel.resetState()
                onClubCreated()
            }
        } else {
            CreateClubForm(paddingValues = paddingValues, viewModel = viewModel)
        }
    }
}

@Composable
private fun ClubCreatedSuccessfullyContent(
    paddingValues: PaddingValues,
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
            text = stringResource(Res.string.create_club_success_title),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(Res.string.create_club_success_message),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(Res.string.create_club_redirecting),
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
                text = stringResource(Res.string.create_club_continue),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun CreateClubForm(
    paddingValues: PaddingValues,
    viewModel: CreateClubViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    val clubName by viewModel.clubName.collectAsState()
    val clubNameError by viewModel.clubNameError.collectAsState()

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
            text = stringResource(Res.string.create_club_title),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(Res.string.create_club_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(48.dp))

        AppTextField(
            value = clubName,
            onValueChange = { viewModel.onClubNameChanged(it) },
            label = { Text(stringResource(Res.string.club_name_label)) },
            isError = clubNameError != null,
            supportingText =
                clubNameError?.let { error ->
                    {
                        Text(
                            text =
                                stringResource(
                                    when (error) {
                                        ClubNameError.EMPTY_NAME -> Res.string.club_name_error_empty
                                        ClubNameError.NAME_TOO_SHORT -> Res.string.club_name_error_too_short
                                        ClubNameError.NAME_TOO_LONG -> Res.string.club_name_error_too_long
                                    },
                                ),
                        )
                    }
                },
            keyboardOptions =
                KeyboardOptions(
                    imeAction = ImeAction.Done,
                    capitalization = KeyboardCapitalization.Words,
                ),
            readOnly = uiState is CreateClubViewModel.UiState.Loading,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.createClub() },
            enabled = uiState !is CreateClubViewModel.UiState.Loading && clubName.isNotBlank(),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            shape = MaterialTheme.shapes.small,
        ) {
            if (uiState is CreateClubViewModel.UiState.Loading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.height(24.dp),
                )
            } else {
                Text(
                    text = stringResource(Res.string.create_club_button),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}
