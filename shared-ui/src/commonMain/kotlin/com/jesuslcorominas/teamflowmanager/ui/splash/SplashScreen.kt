package com.jesuslcorominas.teamflowmanager.ui.splash

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.jesuslcorominas.teamflowmanager.domain.analytics.ScreenName
import com.jesuslcorominas.teamflowmanager.ui.analytics.TrackScreenView
import com.jesuslcorominas.teamflowmanager.ui.components.Loading
import com.jesuslcorominas.teamflowmanager.viewmodel.SplashViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.SplashViewModel.UiState
import kotlinx.coroutines.flow.first
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

    // On iOS, koinViewModel() caches VMs in the root ViewModelStore. If SplashViewModel
    // has a stale non-Loading state from a previous session, the LaunchedEffect(uiState)
    // pattern would navigate with that stale state before refresh() resets it to Loading.
    // Fix: call refresh() first, then wait for the first settled (non-Loading) state.
    LaunchedEffect(Unit) {
        viewModel.refresh()
        viewModel.uiState
            .first { it !is UiState.Loading }
            .let { uiState ->
                when (uiState) {
                    is UiState.NotAuthenticated -> onNavigateToLogin()
                    is UiState.LocalDataNeedsAuth -> onNavigateToLogin()
                    is UiState.NoClub -> onNavigateToClubSelection()
                    is UiState.NoTeam -> onNavigateToAwaitTeam()
                    is UiState.ClubPresident -> onNavigateToTeamList()
                    is UiState.TeamExists -> onNavigateToMatches()
                    is UiState.Loading -> {} // unreachable
                }
            }
    }

    Loading()
}
