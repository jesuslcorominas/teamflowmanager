package com.jesuslcorominas.teamflowmanager.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.jesuslcorominas.teamflowmanager.viewmodel.SplashViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * Root composable for the iOS Phase 2 MVP app.
 * Uses SplashViewModel to decide initial destination.
 */
@Composable
fun App() {
    val splashViewModel: SplashViewModel = koinViewModel()
    val splashState by splashViewModel.uiState.collectAsState()

    when (splashState) {
        is SplashViewModel.UiState.Loading -> SplashScreen()
        is SplashViewModel.UiState.NotAuthenticated,
        is SplashViewModel.UiState.LocalDataNeedsAuth,
        -> LoginScreen()
        is SplashViewModel.UiState.TeamExists,
        is SplashViewModel.UiState.NoTeam,
        is SplashViewModel.UiState.NoClub,
        is SplashViewModel.UiState.ClubPresident,
        -> MatchListScreen()
    }
}
