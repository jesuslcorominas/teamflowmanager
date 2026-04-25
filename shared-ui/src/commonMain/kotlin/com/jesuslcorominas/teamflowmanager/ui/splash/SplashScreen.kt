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
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SplashScreen(
    viewModel: SplashViewModel = koinViewModel(),
    onNavigateToLogin: () -> Unit,
    onNavigateToClubSelection: () -> Unit,
    onNavigateToAwaitTeam: () -> Unit,
    onNavigateToTeamList: () -> Unit,
    onNavigateToMatches: () -> Unit,
) {
    TrackScreenView(screenName = ScreenName.SPLASH, screenClass = "SplashScreen")

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        when (uiState) {
            is UiState.NotAuthenticated -> onNavigateToLogin()
            is UiState.LocalDataNeedsAuth -> onNavigateToLogin()
            is UiState.NoClub -> onNavigateToClubSelection()
            is UiState.NoTeam -> onNavigateToAwaitTeam()
            is UiState.ClubPresident -> onNavigateToTeamList()
            is UiState.TeamExists -> onNavigateToMatches()
            is UiState.Loading -> {
                // Wait for loading to finish
            }
        }
    }

    Loading()
}
