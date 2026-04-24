package com.jesuslcorominas.teamflowmanager.ui.club

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.domain.analytics.ScreenName
import com.jesuslcorominas.teamflowmanager.ui.analytics.TrackScreenView
import com.jesuslcorominas.teamflowmanager.viewmodel.ClubSettingsViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import teamflowmanager.shared_ui.generated.resources.Res
import teamflowmanager.shared_ui.generated.resources.club_name_label
import teamflowmanager.shared_ui.generated.resources.club_settings_saved
import teamflowmanager.shared_ui.generated.resources.club_settings_title
import teamflowmanager.shared_ui.generated.resources.copy_invitation_code
import teamflowmanager.shared_ui.generated.resources.home_ground_label
import teamflowmanager.shared_ui.generated.resources.home_ground_placeholder
import teamflowmanager.shared_ui.generated.resources.invitation_code_copied
import teamflowmanager.shared_ui.generated.resources.invitation_code_label
import teamflowmanager.shared_ui.generated.resources.save

@Composable
fun ClubSettingsScreen(viewModel: ClubSettingsViewModel = koinViewModel()) {
    TrackScreenView(screenName = ScreenName.CLUB_SETTINGS, screenClass = "ClubSettingsScreen")

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboard = LocalClipboardManager.current

    val savedMessage = stringResource(Res.string.club_settings_saved)
    val copiedMessage = stringResource(Res.string.invitation_code_copied)

    LaunchedEffect(uiState.saved) {
        if (uiState.saved) {
            snackbarHostState.showSnackbar(savedMessage)
            viewModel.resetSavedState()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 24.dp)
                    .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = stringResource(Res.string.club_settings_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                label = { Text(stringResource(Res.string.club_name_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !uiState.loading && !uiState.saving,
                keyboardOptions =
                    KeyboardOptions(
                        imeAction = ImeAction.Next,
                        capitalization = KeyboardCapitalization.Words,
                    ),
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.homeGround,
                onValueChange = viewModel::onHomeGroundChange,
                label = { Text(stringResource(Res.string.home_ground_label)) },
                placeholder = { Text(stringResource(Res.string.home_ground_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !uiState.loading && !uiState.saving,
                keyboardOptions =
                    KeyboardOptions(
                        imeAction = ImeAction.Done,
                        capitalization = KeyboardCapitalization.Words,
                    ),
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.invitationCode,
                onValueChange = {},
                label = { Text(stringResource(Res.string.invitation_code_label)) },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                singleLine = true,
                trailingIcon = {
                    androidx.compose.material3.TextButton(
                        onClick = {
                            clipboard.setText(AnnotatedString(uiState.invitationCode))
                        },
                    ) {
                        Text(stringResource(Res.string.copy_invitation_code))
                    }
                },
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = viewModel::onSave,
                enabled = !uiState.loading && !uiState.saving && uiState.name.isNotBlank(),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                shape = MaterialTheme.shapes.small,
            ) {
                if (uiState.saving) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.height(24.dp),
                    )
                } else {
                    Text(
                        text = stringResource(Res.string.save),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}
