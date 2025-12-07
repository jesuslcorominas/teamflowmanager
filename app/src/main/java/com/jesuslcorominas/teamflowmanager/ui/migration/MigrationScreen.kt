package com.jesuslcorominas.teamflowmanager.ui.migration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.analytics.ScreenName
import com.jesuslcorominas.teamflowmanager.ui.TeamFlowManagerIcon
import com.jesuslcorominas.teamflowmanager.ui.analytics.TrackScreenView
import com.jesuslcorominas.teamflowmanager.viewmodel.MigrationViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.MigrationViewModel.UiState
import org.koin.androidx.compose.koinViewModel

@Composable
fun MigrationScreen(
    viewModel: MigrationViewModel = koinViewModel(),
    onMigrationComplete: () -> Unit
) {
    TrackScreenView(screenName = ScreenName.MIGRATION, screenClass = "MigrationScreen")

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        when (uiState) {
            is UiState.Success -> {
                onMigrationComplete()
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar((uiState as UiState.Error).message)
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                TeamFlowManagerIcon()

                Spacer(modifier = Modifier.height(32.dp))

                when (uiState) {
                    is UiState.Migrating -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(64.dp),
                            strokeWidth = 4.dp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = stringResource(id = R.string.migration_in_progress_title),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = stringResource(id = R.string.migration_in_progress_message),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    is UiState.Error -> {
                        Text(
                            text = stringResource(id = R.string.migration_error_title),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.error
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = (uiState as UiState.Error).message,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(onClick = onMigrationComplete) {
                            Text(text = stringResource(id = R.string.continue_button))
                        }
                    }

                    is UiState.Success -> {
                        // Navigation handled by LaunchedEffect
                    }
                }
            }
        }
    }
}
