package com.jesuslcorominas.teamflowmanager.ui.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.jesuslcorominas.teamflowmanager.viewmodel.TeamUiState
import com.jesuslcorominas.teamflowmanager.viewmodel.TeamViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun SplashScreen(
    viewModel: TeamViewModel = koinViewModel(),
    onNavigateToCreateTeam: () -> Unit,
    onNavigateToPlayers: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        when (uiState) {
            is TeamUiState.NoTeam -> onNavigateToCreateTeam()
            is TeamUiState.TeamExists -> onNavigateToPlayers()
            is TeamUiState.Loading -> {
                // Wait for loading to finish
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    }
}
