package com.jesuslcorominas.teamflowmanager.ui.splash

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.jesuslcorominas.teamflowmanager.domain.analytics.ScreenName
import com.jesuslcorominas.teamflowmanager.ui.analytics.TrackScreenView
import com.jesuslcorominas.teamflowmanager.ui.components.Loading
import com.jesuslcorominas.teamflowmanager.viewmodel.SplashViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.SplashViewModel.UiState
import org.koin.androidx.compose.koinViewModel

@Composable
fun SplashScreen(
    viewModel: SplashViewModel = koinViewModel(),
    onNavigateToLogin: () -> Unit,
    onNavigateToCreateTeam: () -> Unit,
    onNavigateToMatches: () -> Unit,
) {
    TrackScreenView(screenName = ScreenName.SPLASH, screenClass = "SplashScreen")

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        when (uiState) {
            is UiState.NotAuthenticated -> onNavigateToLogin()
            is UiState.LocalDataNeedsAuth -> onNavigateToLogin()
            is UiState.NoTeam -> onNavigateToCreateTeam()
            is UiState.TeamExists -> onNavigateToMatches()
            is UiState.Loading -> {
                // Wait for loading to finish
            }
        }
    }

    Loading()
}
