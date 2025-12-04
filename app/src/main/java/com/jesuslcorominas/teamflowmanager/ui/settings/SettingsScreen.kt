package com.jesuslcorominas.teamflowmanager.ui.settings

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.analytics.ScreenName
import com.jesuslcorominas.teamflowmanager.ui.analytics.TrackScreenView
import com.jesuslcorominas.teamflowmanager.ui.navigation.isAndroidNavigation
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import com.jesuslcorominas.teamflowmanager.viewmodel.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = koinViewModel(),
    incomingFileUri: String? = null,
    onNavigateToMatches: () -> Unit = {},
    onSignOut: () -> Unit = {},
) {
    TrackScreenView(screenName = ScreenName.SETTINGS, screenClass = "SettingsScreen")

    val context = LocalContext.current
    val exportResult by viewModel.exportResult.collectAsState()
    val importResult by viewModel.importResult.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val signOutComplete by viewModel.signOutComplete.collectAsState()
    var showImportDialog by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    var pendingImportUri by remember { mutableStateOf<String?>(null) }

    // Handle sign out complete
    LaunchedEffect(signOutComplete) {
        if (signOutComplete) {
            viewModel.clearSignOutComplete()
            onSignOut()
        }
    }

    // Handle incoming file URI from deep link
    LaunchedEffect(incomingFileUri) {
        if (!incomingFileUri.isNullOrBlank() && !incomingFileUri.isAndroidNavigation()) {
            pendingImportUri = incomingFileUri
            showImportDialog = true
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            pendingImportUri = it.toString()
            showImportDialog = true
        }
    }

    // Determine import source based on how we got here
    val importSource = remember(incomingFileUri) {
        if (incomingFileUri != null) "deep_link" else "settings_screen"
    }

    // Show toast messages for results
    exportResult?.let { result ->
        if (result.isSuccess) {
            val fileUri = result.getOrNull()
            if (fileUri != null) {
                // Share the file
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, fileUri.toUri())
                    type = "application/octet-stream"
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.export_share_title)))
                Toast.makeText(context, context.getString(R.string.export_success), Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, context.getString(R.string.export_error, "Unknown error"), Toast.LENGTH_LONG).show()
            }
        } else {
            val message = stringResource(R.string.export_error, result.exceptionOrNull()?.message ?: "Unknown error")
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
        viewModel.clearExportResult()
    }

    importResult?.let { result ->
        if (result.isSuccess) {
            Toast.makeText(context, stringResource(R.string.import_success), Toast.LENGTH_LONG).show()
            viewModel.clearImportResult()
            // Navigate to main screen after successful import
            onNavigateToMatches()
        } else {
            val message = stringResource(R.string.import_error, result.exceptionOrNull()?.message ?: "Unknown error")
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            viewModel.clearImportResult()
        }
    }

    // Import confirmation dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = {
                // Track import cancellation
                viewModel.trackImportCancelled(importSource)
                showImportDialog = false
                pendingImportUri = null
            },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.CloudUpload,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(text = stringResource(R.string.import_confirmation_title))
            },
            text = {
                Text(text = stringResource(R.string.import_confirmation_message))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingImportUri?.let { viewModel.importData(it, importSource) }
                        showImportDialog = false
                        pendingImportUri = null
                    }
                ) {
                    Text(stringResource(R.string.import_confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        // Track import cancellation
                        viewModel.trackImportCancelled(importSource)
                        showImportDialog = false
                        pendingImportUri = null
                    }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Sign out confirmation dialog
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Logout,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(text = stringResource(R.string.sign_out_title))
            },
            text = {
                Text(text = stringResource(R.string.sign_out_message))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.signOut()
                        showSignOutDialog = false
                    }
                ) {
                    Text(stringResource(R.string.sign_out))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(TFMSpacing.spacing04)
        ) {
            // Account section
            Text(
                text = stringResource(R.string.settings_account_section),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = TFMSpacing.spacing02)
            )

            Spacer(modifier = Modifier.height(TFMSpacing.spacing02))

            // User info and sign out button
            currentUser?.let { user ->
                UserAccountItem(
                    user = user,
                    onClick = { showSignOutDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(TFMSpacing.spacing06))

            Text(
                text = stringResource(R.string.settings_data_section),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = TFMSpacing.spacing02)
            )

            Spacer(modifier = Modifier.height(TFMSpacing.spacing02))

            // Export data item
            SettingsItem(
                icon = Icons.Outlined.CloudUpload,
                title = stringResource(R.string.export_data),
                subtitle = stringResource(R.string.export_data_description),
                onClick = { viewModel.exportData() }
            )

            HorizontalDivider(modifier = Modifier.padding(start = 56.dp))

            // Import data item
            SettingsItem(
                icon = Icons.Outlined.CloudDownload,
                title = stringResource(R.string.import_data),
                subtitle = stringResource(R.string.import_data_description),
                onClick = { importLauncher.launch("*/*") }
            )
        }
    }
}

@Composable
private fun UserAccountItem(
    user: com.jesuslcorominas.teamflowmanager.domain.model.User,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(TFMSpacing.spacing02),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // User photo or placeholder
        if (user.photoUrl != null) {
            AsyncImage(
                model = user.photoUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.width(TFMSpacing.spacing04))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user.displayName ?: stringResource(R.string.user_name_unknown),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = user.email ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Outlined.Logout,
            contentDescription = stringResource(R.string.sign_out),
            tint = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(TFMSpacing.spacing02),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(TFMSpacing.spacing04))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
