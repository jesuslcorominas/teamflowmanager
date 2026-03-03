package com.jesuslcorominas.teamflowmanager.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.jesuslcorominas.teamflowmanager.IosDestination
import com.jesuslcorominas.teamflowmanager.IosNavController
import com.jesuslcorominas.teamflowmanager.ui.club.ClubMembersScreen
import com.jesuslcorominas.teamflowmanager.ui.club.ClubSelectionScreen
import com.jesuslcorominas.teamflowmanager.ui.club.CreateClubScreen
import com.jesuslcorominas.teamflowmanager.ui.club.JoinClubScreen
import com.jesuslcorominas.teamflowmanager.ui.invitation.AcceptTeamInvitationScreen
import com.jesuslcorominas.teamflowmanager.ui.login.LoginScreen
import com.jesuslcorominas.teamflowmanager.ui.main.MainScreen
import com.jesuslcorominas.teamflowmanager.ui.matches.ArchivedMatchesScreen
import com.jesuslcorominas.teamflowmanager.ui.matches.MatchListScreen
import com.jesuslcorominas.teamflowmanager.ui.matches.MatchScreen
import com.jesuslcorominas.teamflowmanager.ui.navigation.Route
import com.jesuslcorominas.teamflowmanager.ui.players.PlayersScreen
import com.jesuslcorominas.teamflowmanager.ui.players.wizard.PlayerWizardScreen
import com.jesuslcorominas.teamflowmanager.ui.settings.SettingsScreen
import com.jesuslcorominas.teamflowmanager.ui.splash.SplashScreen
import com.jesuslcorominas.teamflowmanager.ui.team.TeamListScreen
import com.jesuslcorominas.teamflowmanager.ui.team.TeamScreen

@Composable
fun App(onSignInWithGoogle: suspend () -> String = { throw NotImplementedError("KMP-17") }) {
    val navController = remember { IosNavController() }
    var matchTitle: String? by remember { mutableStateOf(null) }

    when (val dest = navController.current) {

        // ── Full-screen routes (no MainScreen shell) ──────────────────────

        is IosDestination.Splash -> SplashScreen(
            onNavigateToLogin = { navController.navigateClearing(IosDestination.Login) },
            onNavigateToMatches = { navController.navigateClearing(IosDestination.Matches) },
        )

        is IosDestination.Login -> LoginScreen(
            onSignInWithGoogle = onSignInWithGoogle,
            onLoginSuccess = { navController.navigateClearing(IosDestination.Matches) },
        )

        is IosDestination.ClubSelection -> ClubSelectionScreen(
            onCreateClub = { navController.navigate(IosDestination.CreateClub) },
            onJoinClub = { navController.navigate(IosDestination.JoinClub) },
        )

        is IosDestination.CreateClub -> CreateClubScreen(
            onClubCreated = {
                navController.navigateClearing(IosDestination.Team(Route.Team.MODE_CREATE))
            },
        )

        is IosDestination.JoinClub -> JoinClubScreen(
            onClubJoined = { navController.navigateClearing(IosDestination.Splash) },
        )

        is IosDestination.PlayerWizard -> PlayerWizardScreen(
            playerId = dest.playerId,
            onNavigateBack = { navController.popBackStack() },
        )

        is IosDestination.AcceptTeamInvitation -> AcceptTeamInvitationScreen(
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
                            navController.navigate(IosDestination.PlayerWizard(0L))
                        else -> { /* Matches FAB: KMP-26 MatchCreationWizard not yet available */ }
                    }
                },
                onBottomNavNavigate = { route -> navController.navigateToBottomNav(route) },
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                ) {
                    when (dest) {
                        is IosDestination.Matches -> MatchListScreen(
                            onNavigateToMatch = { match ->
                                navController.navigate(IosDestination.Match(match.id))
                            },
                            onNavigateToEditMatch = {
                                // KMP-26: MatchCreationWizardScreen not yet available
                            },
                            onNavigateToArchivedMatches = {
                                navController.navigate(IosDestination.ArchivedMatches)
                            },
                        )

                        is IosDestination.Match -> MatchScreen(
                            matchId = dest.matchId,
                            onTitleChange = { matchTitle = it },
                            onExportReady = { /* iOS share sheet: implement in KMP-29 */ },
                        )

                        is IosDestination.ArchivedMatches -> ArchivedMatchesScreen(
                            onNavigateToMatchSummary = { match ->
                                navController.navigate(IosDestination.Match(match.id))
                            },
                        )

                        is IosDestination.Settings -> SettingsScreen(
                            onSignOut = { navController.navigateClearing(IosDestination.Login) },
                        )

                        is IosDestination.TeamList -> TeamListScreen(
                            onTeamClick = {
                                navController.navigate(IosDestination.Team(Route.Team.MODE_VIEW))
                            },
                            onShareTeam = { _, _ -> /* iOS share sheet: implement in KMP-29 */ },
                        )

                        is IosDestination.Team -> TeamScreen(
                            mode = dest.mode,
                            onNavigateToMatches = { navController.navigateToMatches() },
                            onNavigateBackRequest = { navController.popBackStack() },
                            onNavigateToTeamList = {
                                navController.navigateClearing(IosDestination.TeamList)
                            },
                        )

                        is IosDestination.ClubMembers -> ClubMembersScreen()

                        is IosDestination.Players -> PlayersScreen(
                            onNavigateToCreatePlayer = {
                                navController.navigate(IosDestination.PlayerWizard(0L))
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

private fun IosDestination.toRouteString(): String = when (this) {
    is IosDestination.Matches -> Route.Matches.createRoute()
    is IosDestination.Match -> Route.Match.createRoute(matchId)
    is IosDestination.ArchivedMatches -> Route.ArchivedMatches.createRoute()
    is IosDestination.Settings -> Route.Settings.createRoute()
    is IosDestination.Team -> Route.Team.createRoute(mode)
    is IosDestination.TeamList -> Route.TeamList.createRoute()
    is IosDestination.ClubMembers -> Route.ClubMembers.createRoute()
    is IosDestination.Players -> Route.Players.createRoute()
    else -> ""
}

private fun IosNavController.navigateToBottomNav(route: String) {
    val dest: IosDestination = when (Route.fromValue(route)) {
        Route.Matches -> IosDestination.Matches
        Route.Team -> IosDestination.Team(Route.Team.MODE_VIEW)
        Route.TeamList -> IosDestination.TeamList
        Route.ClubMembers -> IosDestination.ClubMembers
        Route.Players -> IosDestination.Players
        Route.Analysis -> return // KMP-28: AnalysisScreen not yet available
        else -> return
    }
    navigateClearing(dest)
}
