package com.jesuslcorominas.teamflowmanager.ui.club

import android.content.Intent
import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.ui.components.Loading
import com.jesuslcorominas.teamflowmanager.ui.main.LocalContentBottomPadding
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import com.jesuslcorominas.teamflowmanager.viewmodel.ClubSettingsViewModel
import org.koin.androidx.compose.koinViewModel

private val FabHeight = 56.dp
private val FabBarGap = 16.dp
private val FabContentGap = 16.dp

@Composable
fun ClubSettingsScreen(viewModel: ClubSettingsViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val contentBottomPadding = LocalContentBottomPadding.current

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it) }
    }

    BackHandler(enabled = uiState.isEditing && !uiState.showExitDialog) {
        viewModel.onCancelEdit()
    }

    if (uiState.showExitDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissExitDialog,
            title = { Text(stringResource(R.string.unsaved_changes_title)) },
            text = { Text(stringResource(R.string.discard_message)) },
            confirmButton = {
                TextButton(onClick = viewModel::onConfirmExit) {
                    Text(stringResource(R.string.discard))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDismissExitDialog) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    val showFab = !uiState.loading && !uiState.isEditing
    val detailBottomPadding = if (showFab) {
        contentBottomPadding + FabBarGap + FabHeight + FabContentGap
    } else {
        contentBottomPadding
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            when {
                uiState.loading -> Loading()
                uiState.isEditing -> {
                    ClubEditForm(
                        uiState = uiState,
                        onNameChange = viewModel::onNameChange,
                        onHomeGroundChange = viewModel::onHomeGroundChange,
                        onSave = viewModel::onSave,
                        contentBottomPadding = contentBottomPadding,
                        onCopyCode = {
                            clipboard.setText(AnnotatedString(uiState.invitationCode))
                        },
                        onShareCode = {
                            context.startActivity(
                                Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, uiState.invitationCode)
                                },
                            )
                        },
                        onRegenerateCode = viewModel::onRegenerateCode,
                    )
                }
                else -> {
                    ClubDetailContent(
                        uiState = uiState,
                        contentBottomPadding = detailBottomPadding,
                        onCopyCode = {
                            clipboard.setText(AnnotatedString(uiState.invitationCode))
                        },
                        onShareCode = {
                            context.startActivity(
                                Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, uiState.invitationCode)
                                },
                            )
                        },
                        onRegenerateCode = viewModel::onRegenerateCode,
                    )
                }
            }
        }

        if (showFab) {
            FloatingActionButton(
                onClick = viewModel::onEnterEdit,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = contentBottomPadding + FabBarGap),
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.club_settings_edit_title),
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
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
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = contentBottomPadding)
            .padding(TFMSpacing.spacing04),
    ) {
        InfoRow(
            label = stringResource(R.string.club_name_label),
            value = uiState.name,
        )

        InfoRow(
            label = stringResource(R.string.home_ground_label),
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
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = contentBottomPadding)
            .padding(TFMSpacing.spacing04)
            .verticalScroll(rememberScrollState()),
    ) {
        OutlinedTextField(
            value = uiState.name,
            onValueChange = onNameChange,
            label = { Text(stringResource(R.string.club_name_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !uiState.saving,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                capitalization = KeyboardCapitalization.Words,
            ),
        )

        Spacer(modifier = Modifier.height(TFMSpacing.spacing04))

        OutlinedTextField(
            value = uiState.homeGround,
            onValueChange = onHomeGroundChange,
            label = { Text(stringResource(R.string.home_ground_label)) },
            placeholder = { Text(stringResource(R.string.home_ground_placeholder)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !uiState.saving,
            keyboardOptions = KeyboardOptions(
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
            modifier = Modifier
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
                    text = stringResource(R.string.save),
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = TFMSpacing.spacing03, horizontal = TFMSpacing.spacing02),
    ) {
        Text(
            text = stringResource(R.string.invitation_code_label),
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
                    contentDescription = stringResource(R.string.share_invitation_code),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onCopy) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = stringResource(R.string.copy_invitation_code),
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
                        contentDescription = stringResource(R.string.regenerate_invitation_code),
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = TFMSpacing.spacing03, horizontal = TFMSpacing.spacing02),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = TFMSpacing.spacing01),
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
        title = { Text(stringResource(R.string.regenerate_code_confirm_title)) },
        text = { Text(stringResource(R.string.regenerate_code_confirm_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.regenerate_invitation_code))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}
