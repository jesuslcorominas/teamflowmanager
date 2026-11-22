package com.jesuslcorominas.teamflowmanager.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.jesuslcorominas.teamflowmanager.IosDestination
import com.jesuslcorominas.teamflowmanager.IosNavController
import com.jesuslcorominas.teamflowmanager.domain.model.ActiveViewRole
import com.jesuslcorominas.teamflowmanager.domain.model.AppleSignInCredential
import com.jesuslcorominas.teamflowmanager.ui.analysis.AnalysisScreen
import com.jesuslcorominas.teamflowmanager.ui.club.ClubMembersScreen
import com.jesuslcorominas.teamflowmanager.ui.club.ClubSelectionScreen
import com.jesuslcorominas.teamflowmanager.ui.club.ClubSettingsScreen
import com.jesuslcorominas.teamflowmanager.ui.club.CreateClubScreen
import com.jesuslcorominas.teamflowmanager.ui.club.JoinClubScreen
import com.jesuslcorominas.teamflowmanager.ui.club.PresidentNotificationsScreen
import com.jesuslcorominas.teamflowmanager.ui.club.PresidentTeamDetailScreen
import com.jesuslcorominas.teamflowmanager.ui.invitation.AcceptTeamInvitationScreen
import com.jesuslcorominas.teamflowmanager.ui.login.LoginScreen
import com.jesuslcorominas.teamflowmanager.ui.main.MainScreen
import com.jesuslcorominas.teamflowmanager.ui.matches.ArchivedMatchesScreen
import com.jesuslcorominas.teamflowmanager.ui.matches.MatchListScreen
import com.jesuslcorominas.teamflowmanager.ui.matches.MatchScreen
import com.jesuslcorominas.teamflowmanager.ui.matches.wizard.MatchCreationWizardScreen
import com.jesuslcorominas.teamflowmanager.ui.navigation.Route
import com.jesuslcorominas.teamflowmanager.ui.players.PlayersScreen
import com.jesuslcorominas.teamflowmanager.ui.players.wizard.PlayerWizardScreen
import com.jesuslcorominas.teamflowmanager.ui.settings.SettingsScreen
import com.jesuslcorominas.teamflowmanager.ui.splash.SplashScreen
import com.jesuslcorominas.teamflowmanager.ui.team.TeamListScreen
import com.jesuslcorominas.teamflowmanager.ui.team.TeamScreen
import com.jesuslcorominas.teamflowmanager.viewmodel.LoginViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.MainViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun IosNavHost(
    navController: IosNavController,
    onSignInWithGoogle: suspend () -> String,
    onShareFile: (String) -> Unit,
    onSignInWithApple: (suspend () -> AppleSignInCredential?)? = null,
    appleSignInButton: (@Composable (enabled: Boolean, onClick: () -> Unit) -> Unit)? = null,
    onRequestNotificationPermission: (onResult: (Boolean) -> Unit) -> Unit,
) {
    var matchTitle: String? by remember { mutableStateOf(null) }
    var presidentMatchTitle: String? by remember { mutableStateOf(null) }

    // Same as AndroidAppShell: switching role swaps the whole bottom bar, so the current
    // destination belongs to the role just left. Land on the first tab of the new role and
    // drop the old back stack.
    val mainViewModel: MainViewModel = koinViewModel()
    LaunchedEffect(Unit) {
        mainViewModel.roleSwitches.collect { role ->
            val landing =
                when (role) {
                    ActiveViewRole.President -> IosDestination.TeamList
                    ActiveViewRole.Coach -> IosDestination.Matches
                }
            navController.navigateClearing(landing)
        }
    }

    when (val dest = navController.current) {
        // ── Full-screen routes (no MainScreen shell) ──────────────────────

        is IosDestination.Splash ->
            SplashScreen(
                onNavigateToLogin = { navController.navigateClearing(IosDestination.Login) },
                onNavigateToClubSelection = { navController.navigateClearing(IosDestination.ClubSelection) },
                onNavigateToAwaitTeam = { navController.navigateClearing(IosDestination.ClubSelection) },
                onNavigateToTeamList = { navController.navigateClearing(IosDestination.TeamList) },
                onNavigateToMatches = { navController.navigateClearing(IosDestination.Matches) },
            )

        is IosDestination.Login -> {
            val loginViewModel: LoginViewModel = koinViewModel()
            LoginScreen(
                viewModel = loginViewModel,
                onSignInWithGoogle = onSignInWithGoogle,
                onSignInWithApple = onSignInWithApple,
                appleSignInButton = appleSignInButton,
                // Like Android, go back through the splash: it is the one that picks the landing
                // screen from the user's roles and the stored view, not Matches for everyone.
                onLoginSuccess = { navController.navigateClearing(IosDestination.Splash) },
                // Without notification permission the login waits for this answer before it
                // navigates, so it has to be wired: a null callback would leave it loading.
                onRequestNotificationPermission = {
                    onRequestNotificationPermission(loginViewModel::onNotificationPermissionResult)
                },
            )
        }

        is IosDestination.ClubSelection ->
            ClubSelectionScreen(
                onCreateClub = { navController.navigate(IosDestination.CreateClub) },
                onJoinClub = { navController.navigate(IosDestination.JoinClub) },
            )

        is IosDestination.CreateClub ->
            CreateClubScreen(
                onClubCreated = {
                    navController.navigateClearing(IosDestination.Team(Route.Team.MODE_CREATE))
                },
            )

        is IosDestination.JoinClub ->
            JoinClubScreen(
                onClubJoined = { navController.navigateClearing(IosDestination.Splash) },
            )

        is IosDestination.PlayerWizard ->
            IosWizardRoute(scopedViewModels = true) {
                PlayerWizardScreen(
                    playerId = dest.playerId,
                    onNavigateBack = { navController.popBackStack() },
                )
            }

        is IosDestination.MatchWizard ->
            IosWizardRoute(scopedViewModels = true) {
                MatchCreationWizardScreen(
                    matchId = dest.matchId,
                    onNavigateBack = { navController.popBackStack() },
                )
            }

        is IosDestination.PresidentTeamDetail ->
            PresidentTeamDetailScreen(
                teamId = dest.teamId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToMatch = { matchId ->
                    navController.navigate(IosDestination.PresidentMatchDetail(dest.teamId, matchId))
                },
            )

        is IosDestination.PresidentMatchDetail ->
            PresidentMatchDetailScaffold(
                title = presidentMatchTitle,
                onNavigateBack = { navController.popBackStack() },
            ) {
                MatchScreen(
                    matchId = dest.matchId,
                    readOnly = true,
                    onTitleChange = { presidentMatchTitle = it },
                )
            }

        is IosDestination.AcceptTeamInvitation ->
            AcceptTeamInvitationScreen(
                teamId = dest.teamId,
                onNavigateToLogin = { navController.navigateClearing(IosDestination.Login) },
                onNavigateToTeam = {
                    navController.navigateClearing(IosDestination.Team(Route.Team.MODE_VIEW))
                },
                onNavigateToTeams = { navController.navigateClearing(IosDestination.TeamList) },
            )

        // ── Routes wrapped in MainScreen shell ────────────────────────────

        else -> {
            val routeString = dest.toRouteString()
            val teamMode = if (dest is IosDestination.Team) dest.mode else null
            val dynamicTitle = if (dest is IosDestination.Match) matchTitle else null

            MainScreen(
                currentRoute = routeString,
                teamMode = teamMode,
                dynamicTitle = dynamicTitle,
                onBackNavigate = { navController.popBackStack() },
                onSettingsNavigate = { navController.navigate(IosDestination.Settings) },
                onFabClick = {
                    when (dest) {
                        is IosDestination.Team ->
                            navController.navigate(IosDestination.Team(Route.Team.MODE_EDIT))
                        is IosDestination.TeamList ->
                            navController.navigate(IosDestination.Team(Route.Team.MODE_CREATE))
                        is IosDestination.Players ->
                            navController.navigate(IosDestination.PlayerWizard(""))
                        is IosDestination.Matches ->
                            navController.navigate(IosDestination.MatchWizard(""))
                        else -> {}
                    }
                },
                onBottomNavNavigate = { route -> navController.navigateToBottomNav(route) },
            ) { paddingValues ->
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(top = paddingValues.calculateTopPadding()),
                ) {
                    when (dest) {
                        is IosDestination.Matches ->
                            MatchListScreen(
                                onNavigateToMatch = { match ->
                                    navController.navigate(IosDestination.Match(match.id))
                                },
                                onNavigateToEditMatch = { matchId ->
                                    navController.navigate(IosDestination.MatchWizard(matchId))
                                },
                                onNavigateToArchivedMatches = {
                                    navController.navigate(IosDestination.ArchivedMatches)
                                },
                            )

                        is IosDestination.Match ->
                            MatchScreen(
                                matchId = dest.matchId,
                                onTitleChange = { matchTitle = it },
                                onExportReady = { filePath -> onShareFile(filePath) },
                            )

                        is IosDestination.ArchivedMatches ->
                            ArchivedMatchesScreen(
                                onNavigateToMatchSummary = { match ->
                                    navController.navigate(IosDestination.Match(match.id))
                                },
                            )

                        is IosDestination.Settings ->
                            SettingsScreen(
                                onSignOut = { navController.navigateClearing(IosDestination.Login) },
                            )

                        is IosDestination.TeamList ->
                            TeamListScreen(
                                onTeamClick = { team ->
                                    if (team.id.isNotEmpty()) {
                                        navController.navigate(IosDestination.PresidentTeamDetail(team.id))
                                    } else {
                                        navController.navigate(IosDestination.Team(Route.Team.MODE_VIEW))
                                    }
                                },
                            )

                        is IosDestination.Team ->
                            TeamScreen(
                                mode = dest.mode,
                                onNavigateToMatches = { navController.navigateToMatches() },
                                onNavigateBackRequest = { navController.popBackStack() },
                                onNavigateToTeamList = {
                                    navController.navigateClearing(IosDestination.TeamList)
                                },
                            )

                        is IosDestination.Analysis ->
                            AnalysisScreen(
                                onShareFile = onShareFile,
                            )

                        is IosDestination.ClubMembers -> ClubMembersScreen()

                        is IosDestination.PresidentNotifications ->
                            PresidentNotificationsScreen()

                        is IosDestination.ClubSettings ->
                            ClubSettingsScreen()

                        is IosDestination.Players ->
                            PlayersScreen(
                                onNavigateToCreatePlayer = {
                                    navController.navigate(IosDestination.PlayerWizard(""))
                                },
                                onNavigateToEditPlayer = { playerId ->
                                    navController.navigate(IosDestination.PlayerWizard(playerId))
                                },
                            )

                        else -> Unit // safety fallback — unreachable in practice
                    }
                }
            }
        }
    }
}
