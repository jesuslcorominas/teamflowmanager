package com.jesuslcorominas.teamflowmanager.ui.splash

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.jesuslcorominas.teamflowmanager.ui.components.Loading
import com.jesuslcorominas.teamflowmanager.viewmodel.SplashViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.SplashViewModel.UiState
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SplashScreen(
    viewModel: SplashViewModel = koinViewModel(),
    onNavigateToLogin: () -> Unit,
    onNavigateToMatches: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        when (uiState) {
            is UiState.NotAuthenticated,
            is UiState.LocalDataNeedsAuth,
            -> onNavigateToLogin()

            is UiState.TeamExists,
            is UiState.NoTeam,
            is UiState.NoClub,
            is UiState.ClubPresident,
            -> onNavigateToMatches()

            is UiState.Loading -> {
                // wait
            }
        }
    }

    Loading()
}
