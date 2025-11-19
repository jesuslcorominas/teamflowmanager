package com.jesuslcorominas.teamflowmanager.ui.settings

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.importData(it.toString())
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

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(TFMSpacing.spacing04),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Export button
            Button(
                onClick = { viewModel.exportData() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.padding(start = 8.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.export_data),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = stringResource(R.string.export_data_description),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(TFMSpacing.spacing03))

            // Import button
            OutlinedButton(
                onClick = { importLauncher.launch("*/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FileUpload,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.padding(start = 8.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.import_data),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = stringResource(R.string.import_data_description),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
