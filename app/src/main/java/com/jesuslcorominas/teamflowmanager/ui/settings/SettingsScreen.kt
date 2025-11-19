package com.jesuslcorominas.teamflowmanager.ui.settings

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.analytics.ScreenName
import com.jesuslcorominas.teamflowmanager.ui.analytics.TrackScreenView
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import com.jesuslcorominas.teamflowmanager.viewmodel.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = koinViewModel(),
) {
    TrackScreenView(screenName = ScreenName.SETTINGS, screenClass = "SettingsScreen")

    val context = LocalContext.current
    val exportResult by viewModel.exportResult.collectAsState()
    val importResult by viewModel.importResult.collectAsState()
    var showImportDialog by remember { mutableStateOf(false) }
    var pendingImportUri by remember { mutableStateOf<String?>(null) }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            pendingImportUri = it.toString()
            showImportDialog = true
        }
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
                    type = "text/plain"
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
        val message = if (result.isSuccess) {
            stringResource(R.string.import_success)
        } else {
            stringResource(R.string.import_error, result.exceptionOrNull()?.message ?: "Unknown error")
        }
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        viewModel.clearImportResult()
    }

    // Import confirmation dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = {
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
                        pendingImportUri?.let { viewModel.importData(it) }
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
                        showImportDialog = false
                        pendingImportUri = null
                    }
                ) {
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
            Text(
                text = stringResource(R.string.settings_data_section),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = TFMSpacing.spacing02)
            )

            Spacer(modifier = Modifier.padding(vertical = TFMSpacing.spacing01))

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
