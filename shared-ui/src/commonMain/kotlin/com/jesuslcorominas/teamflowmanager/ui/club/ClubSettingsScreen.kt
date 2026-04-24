package com.jesuslcorominas.teamflowmanager.ui.club

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.domain.analytics.ScreenName
import com.jesuslcorominas.teamflowmanager.ui.analytics.TrackScreenView
import com.jesuslcorominas.teamflowmanager.ui.components.AppBackHandler
import com.jesuslcorominas.teamflowmanager.ui.components.Loading
import com.jesuslcorominas.teamflowmanager.ui.main.LocalContentBottomPadding
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import com.jesuslcorominas.teamflowmanager.viewmodel.ClubSettingsViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import teamflowmanager.shared_ui.generated.resources.Res
import teamflowmanager.shared_ui.generated.resources.cancel
import teamflowmanager.shared_ui.generated.resources.club_name_label
import teamflowmanager.shared_ui.generated.resources.club_settings_edit_title
import teamflowmanager.shared_ui.generated.resources.club_settings_saved
import teamflowmanager.shared_ui.generated.resources.copy_invitation_code
import teamflowmanager.shared_ui.generated.resources.discard
import teamflowmanager.shared_ui.generated.resources.discard_message
import teamflowmanager.shared_ui.generated.resources.home_ground_label
import teamflowmanager.shared_ui.generated.resources.home_ground_placeholder
import teamflowmanager.shared_ui.generated.resources.invitation_code_copied
import teamflowmanager.shared_ui.generated.resources.invitation_code_label
import teamflowmanager.shared_ui.generated.resources.regenerate_code_confirm_message
import teamflowmanager.shared_ui.generated.resources.regenerate_code_confirm_title
import teamflowmanager.shared_ui.generated.resources.regenerate_invitation_code
import teamflowmanager.shared_ui.generated.resources.save
import teamflowmanager.shared_ui.generated.resources.share_invitation_code
import teamflowmanager.shared_ui.generated.resources.unsaved_changes_title

private val FabHeight = 56.dp
private val FabBarGap = 16.dp

@Composable
fun ClubSettingsScreen(
    viewModel: ClubSettingsViewModel = koinViewModel(),
    onShareCode: (String) -> Unit = {},
) {
    TrackScreenView(screenName = ScreenName.CLUB_SETTINGS, screenClass = "ClubSettingsScreen")

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboard = LocalClipboardManager.current
    val contentBottomPadding = LocalContentBottomPadding.current

    val savedMessage = stringResource(Res.string.club_settings_saved)
    val copiedMessage = stringResource(Res.string.invitation_code_copied)
    var codeCopied by remember { mutableStateOf(false) }

    AppBackHandler(enabled = uiState.isEditing && !uiState.showExitDialog) {
        viewModel.onCancelEdit()
    }

    LaunchedEffect(uiState.saved) {
        if (uiState.saved) {
            snackbarHostState.showSnackbar(savedMessage)
            viewModel.resetSavedState()
        }
    }

    LaunchedEffect(codeCopied) {
        if (codeCopied) {
            codeCopied = false
            snackbarHostState.showSnackbar(copiedMessage)
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it) }
    }

    if (uiState.showExitDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissExitDialog,
            title = { Text(stringResource(Res.string.unsaved_changes_title)) },
            text = { Text(stringResource(Res.string.discard_message)) },
            confirmButton = {
                TextButton(onClick = viewModel::onConfirmExit) {
                    Text(
                        text = stringResource(Res.string.discard),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDismissExitDialog) {
                    Text(stringResource(Res.string.cancel))
                }
            },
        )
    }

    val showFab = !uiState.loading && !uiState.isEditing
    val detailBottomPadding =
        if (showFab) {
            contentBottomPadding + FabBarGap + FabHeight + FabBarGap
        } else {
            contentBottomPadding
        }

    val onCopyCode = {
        clipboard.setText(AnnotatedString(uiState.invitationCode))
        codeCopied = true
    }
    val onRegenerate = viewModel::onRegenerateCode

    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            when {
                uiState.loading -> Loading()
                uiState.isEditing ->
                    ClubEditForm(
                        uiState = uiState,
                        onNameChange = viewModel::onNameChange,
                        onHomeGroundChange = viewModel::onHomeGroundChange,
                        onSave = viewModel::onSave,
                        contentBottomPadding = contentBottomPadding,
                        onCopyCode = onCopyCode,
                        onShareCode = { onShareCode(uiState.invitationCode) },
                        onRegenerateCode = onRegenerate,
                    )

                else ->
                    ClubDetailContent(
                        uiState = uiState,
                        contentBottomPadding = detailBottomPadding,
                        onCopyCode = onCopyCode,
                        onShareCode = { onShareCode(uiState.invitationCode) },
                        onRegenerateCode = onRegenerate,
                    )
            }
        }

        if (showFab) {
            FloatingActionButton(
                onClick = viewModel::onEnterEdit,
                modifier =
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = TFMSpacing.spacing04, bottom = contentBottomPadding + FabBarGap),
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(Res.string.club_settings_edit_title),
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = contentBottomPadding),
        )
    }
}

@Composable
private fun ClubDetailContent(
    uiState: ClubSettingsViewModel.UiState,
    contentBottomPadding: Dp,
    onCopyCode: () -> Unit,
    onShareCode: () -> Unit,
    onRegenerateCode: () -> Unit,
) {
    var showRegenerateDialog by remember { mutableStateOf(false) }

    if (showRegenerateDialog) {
        RegenerateConfirmDialog(
            onConfirm = {
                showRegenerateDialog = false
                onRegenerateCode()
            },
            onDismiss = { showRegenerateDialog = false },
        )
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(bottom = contentBottomPadding)
                .padding(TFMSpacing.spacing04),
    ) {
        InfoRow(
            label = stringResource(Res.string.club_name_label),
            value = uiState.name,
        )

        InfoRow(
            label = stringResource(Res.string.home_ground_label),
            value = uiState.homeGround.ifEmpty { "—" },
        )

        InvitationCodeSection(
            code = uiState.invitationCode,
            regenerating = uiState.regenerating,
            onCopy = onCopyCode,
            onShare = onShareCode,
            onRegenerate = { showRegenerateDialog = true },
        )
    }
}

@Composable
private fun ClubEditForm(
    uiState: ClubSettingsViewModel.UiState,
    onNameChange: (String) -> Unit,
    onHomeGroundChange: (String) -> Unit,
    onSave: () -> Unit,
    contentBottomPadding: Dp,
    onCopyCode: () -> Unit,
    onShareCode: () -> Unit,
    onRegenerateCode: () -> Unit,
) {
    var showRegenerateDialog by remember { mutableStateOf(false) }

    if (showRegenerateDialog) {
        RegenerateConfirmDialog(
            onConfirm = {
                showRegenerateDialog = false
                onRegenerateCode()
            },
            onDismiss = { showRegenerateDialog = false },
        )
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(bottom = contentBottomPadding)
                .padding(TFMSpacing.spacing04)
                .verticalScroll(rememberScrollState()),
    ) {
        OutlinedTextField(
            value = uiState.name,
            onValueChange = onNameChange,
            label = { Text(stringResource(Res.string.club_name_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !uiState.saving,
            keyboardOptions =
                KeyboardOptions(
                    imeAction = ImeAction.Next,
                    capitalization = KeyboardCapitalization.Words,
                ),
        )

        Spacer(modifier = Modifier.height(TFMSpacing.spacing04))

        OutlinedTextField(
            value = uiState.homeGround,
            onValueChange = onHomeGroundChange,
            label = { Text(stringResource(Res.string.home_ground_label)) },
            placeholder = { Text(stringResource(Res.string.home_ground_placeholder)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !uiState.saving,
            keyboardOptions =
                KeyboardOptions(
                    imeAction = ImeAction.Done,
                    capitalization = KeyboardCapitalization.Words,
                ),
        )

        Spacer(modifier = Modifier.height(TFMSpacing.spacing04))

        InvitationCodeSection(
            code = uiState.invitationCode,
            regenerating = uiState.regenerating,
            onCopy = onCopyCode,
            onShare = onShareCode,
            onRegenerate = { showRegenerateDialog = true },
        )

        Spacer(modifier = Modifier.height(TFMSpacing.spacing07))

        Button(
            onClick = onSave,
            enabled = !uiState.saving && uiState.name.isNotBlank(),
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

@Composable
private fun InvitationCodeSection(
    code: String,
    regenerating: Boolean,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onRegenerate: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = TFMSpacing.spacing03, horizontal = TFMSpacing.spacing02),
    ) {
        Text(
            text = stringResource(Res.string.invitation_code_label),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.padding(top = TFMSpacing.spacing01),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = code,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Medium,
            )
            IconButton(onClick = onShare) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = stringResource(Res.string.share_invitation_code),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onCopy) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = stringResource(Res.string.copy_invitation_code),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onRegenerate, enabled = !regenerating) {
                if (regenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(Res.string.regenerate_invitation_code),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = TFMSpacing.spacing03, horizontal = TFMSpacing.spacing02),
        verticalArrangement = Arrangement.spacedBy(TFMSpacing.spacing01),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun RegenerateConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.regenerate_code_confirm_title)) },
        text = { Text(stringResource(Res.string.regenerate_code_confirm_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(Res.string.regenerate_invitation_code))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.cancel))
            }
        },
    )
}
