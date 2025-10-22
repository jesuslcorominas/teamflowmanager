package com.jesuslcorominas.teamflowmanager.ui.splash

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.jesuslcorominas.teamflowmanager.ui.components.Loading
import com.jesuslcorominas.teamflowmanager.viewmodel.TeamUiState
import com.jesuslcorominas.teamflowmanager.viewmodel.TeamViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun SplashScreen(
    viewModel: TeamViewModel = koinViewModel(),
    onNavigateToCreateTeam: () -> Unit,
    onNavigateToMatches: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        when (uiState) {
            is TeamUiState.NoTeam -> onNavigateToCreateTeam()
            is TeamUiState.TeamExists -> onNavigateToMatches()
            is TeamUiState.Loading -> {
                // Wait for loading to finish
            }
        }
    }

    Loading()
}
