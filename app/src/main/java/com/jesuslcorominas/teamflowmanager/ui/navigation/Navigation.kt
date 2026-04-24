package com.jesuslcorominas.teamflowmanager.ui.navigation

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.ui.analysis.AnalysisScreen
import com.jesuslcorominas.teamflowmanager.ui.club.ClubMembersScreen
import com.jesuslcorominas.teamflowmanager.ui.club.ClubSelectionScreen
import com.jesuslcorominas.teamflowmanager.ui.club.ClubSettingsScreen
import com.jesuslcorominas.teamflowmanager.ui.club.CreateClubScreen
import com.jesuslcorominas.teamflowmanager.ui.club.JoinClubScreen
import com.jesuslcorominas.teamflowmanager.ui.club.PendingTeamAssignmentScreen
import com.jesuslcorominas.teamflowmanager.ui.club.PresidentNotificationsScreen
import com.jesuslcorominas.teamflowmanager.ui.club.PresidentTeamDetailScreen
import com.jesuslcorominas.teamflowmanager.ui.invitation.AcceptTeamInvitationScreen
import com.jesuslcorominas.teamflowmanager.ui.login.LoginScreen
import com.jesuslcorominas.teamflowmanager.ui.main.search.LocalSearchState
import com.jesuslcorominas.teamflowmanager.ui.matches.ArchivedMatchesScreen
import com.jesuslcorominas.teamflowmanager.ui.matches.MatchListScreen
import com.jesuslcorominas.teamflowmanager.ui.matches.MatchScreen
import com.jesuslcorominas.teamflowmanager.ui.matches.wizard.MatchCreationWizardScreen
import com.jesuslcorominas.teamflowmanager.ui.players.PlayersScreen
import com.jesuslcorominas.teamflowmanager.ui.players.wizard.PlayerWizardScreen
import com.jesuslcorominas.teamflowmanager.ui.settings.SettingsScreen
import com.jesuslcorominas.teamflowmanager.ui.splash.SplashScreen
import com.jesuslcorominas.teamflowmanager.ui.team.TeamListScreen
import com.jesuslcorominas.teamflowmanager.ui.team.TeamScreen

@Composable
fun Navigation(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    onTitleChange: (String?) -> Unit,
    onRoleChanged: () -> Unit,
    currentBackHandler: BackHandlerController,
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Route.Splash.createRoute(),
        // Default: entering screens fade in; exiting screens disappear instantly to avoid
        // layout-shift artifacts (topBar/bottomBar change in the same frame as navigation).
        enterTransition = { fadeIn(tween(220)) },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { fadeIn(tween(220)) },
        popExitTransition = { ExitTransition.None },
    ) {
        composable(Route.Splash.createRoute()) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Route.Login.createRoute()) {
                        popUpTo(Route.Splash.createRoute()) { inclusive = true }
                    }
                },
                onNavigateToClubSelection = {
                    navController.navigate(Route.ClubSelection.createRoute()) {
                        popUpTo(Route.Splash.createRoute()) { inclusive = true }
                    }
                },
                onNavigateToAwaitTeam = {
                    navController.navigate(Route.PendingTeamAssignment.createRoute()) {
                        popUpTo(Route.Splash.createRoute()) { inclusive = true }
                    }
                },
                onNavigateToTeamList = {
                    navController.navigate(Route.TeamList.createRoute()) {
                        popUpTo(Route.Splash.createRoute()) { inclusive = true }
                    }
                },
                onNavigateToMatches = {
                    navController.navigate(Route.Matches.createRoute()) {
                        popUpTo(Route.Splash.createRoute()) { inclusive = true }
                    }
                },
            )
        }

        composable(Route.Login.createRoute()) {
            val context = LocalContext.current
            LoginScreen(
                onSignInWithGoogle = { getGoogleIdToken(context) },
                onLoginSuccess = {
                    navController.navigate(Route.Splash.createRoute()) {
                        popUpTo(Route.Login.createRoute()) { inclusive = true }
                    }
                },
            )
        }

        composable(Route.ClubSelection.createRoute()) {
            ClubSelectionScreen(
                onCreateClub = {
                    navController.navigate(Route.CreateClub.createRoute())
                },
                onJoinClub = {
                    navController.navigate(Route.JoinClub.createRoute())
                },
            )
        }

        composable(Route.CreateClub.createRoute()) {
            CreateClubScreen(
                onClubCreated = {
                    navController.navigate(Route.Team.createRoute(Route.Team.MODE_CREATE)) {
                        popUpTo(Route.ClubSelection.createRoute()) { inclusive = true }
                    }
                },
            )
        }

        composable(Route.JoinClub.createRoute()) {
            JoinClubScreen(
                onClubJoined = {
                    navController.navigate(Route.Splash.createRoute()) {
                        popUpTo(Route.ClubSelection.createRoute()) { inclusive = true }
                    }
                },
            )
        }

        composable(
            route = Route.Team.FULL_ROUTE,
            arguments =
                listOf(
                    navArgument(Route.Team.ARG_MODE) {
                        type = NavType.StringType
                    },
                ),
        ) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString(Route.Team.ARG_MODE) ?: ""

            TeamScreen(
                mode = mode,
                onNavigateToMatches = { _ ->
                    navController.navigate(Route.Matches.createRoute()) {
                        popUpTo(Route.Team.createRoute(Route.Team.MODE_CREATE)) { inclusive = true }
                    }
                },
                onNavigateBackRequest = { navController.popBackStack() },
                onNavigateToTeamList = {
                    navController.navigate(Route.TeamList.createRoute()) {
                        popUpTo(Route.Team.createRoute(Route.Team.MODE_CREATE)) { inclusive = true }
                    }
                },
            )
        }

        composable(
            route = Route.TeamList.createRoute(),
            // Instant pop-enter: TeamList ↔ PresidentTeamDetail change the scaffold
            // (bottomBar/canGoBack), so restoring TeamList must be atomic too.
            popEnterTransition = { EnterTransition.None },
        ) {
            TeamListScreen(
                onTeamClick = { team ->
                    team.remoteId?.let { remoteId ->
                        navController.navigate(Route.PresidentTeamDetail.createRoute(remoteId))
                    }
                },
            )
        }

        composable(
            route = Route.PresidentTeamDetail.FULL_ROUTE,
            arguments =
                listOf(
                    navArgument(Route.PresidentTeamDetail.ARG_TEAM_ID) {
                        type = NavType.StringType
                    },
                ),
            // Instant transitions: PresidentTeamDetail changes canGoBack/bottomBar visibility,
            // so we switch atomically to avoid scaffold layout-shift artifacts.
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None },
        ) { backStackEntry ->
            val teamId =
                backStackEntry.arguments?.getString(Route.PresidentTeamDetail.ARG_TEAM_ID)
                    ?: return@composable
            PresidentTeamDetailScreen(
                teamId = teamId,
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(Route.ClubMembers.createRoute()) {
            ClubMembersScreen()
        }

        composable(Route.ClubSettings.createRoute()) {
            ClubSettingsScreen()
        }

        composable(Route.PresidentNotifications.createRoute()) {
            PresidentNotificationsScreen()
        }

        composable(Route.Players.createRoute()) {
            PlayersScreen(
                onNavigateToCreatePlayer = {
                    navController.navigate(Route.PlayerWizard.createRoute(0L))
                },
                onNavigateToEditPlayer = { playerId ->
                    navController.navigate(Route.PlayerWizard.createRoute(playerId))
                },
            )
        }

        composable(
            route = Route.PlayerWizard.FULL_ROUTE,
            arguments =
                listOf(
                    navArgument(Route.PlayerWizard.ARG_PLAYER_ID) {
                        type = NavType.LongType
                    },
                ),
            // Instant transitions: wizard changes topBar/bottomBar visibility, so we switch
            // atomically to avoid any in-between scaffold state being visible.
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None },
        ) { backStackEntry ->
            val playerId = backStackEntry.arguments?.getLong(Route.PlayerWizard.ARG_PLAYER_ID) ?: 0L
            PlayerWizardScreen(
                playerId = playerId,
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(Route.Analysis.createRoute()) {
            AnalysisScreen()
        }

        composable(Route.Matches.createRoute()) {
            MatchListScreen(
                onNavigateToEditMatch = { matchId ->
                    navController.navigate(Route.CreateMatch.createRoute(matchId))
                },
                onNavigateToMatch = { match ->
                    navController.navigate(Route.Match.createRoute(match.id))
                },
                onNavigateToArchivedMatches = {
                    navController.navigate(Route.ArchivedMatches.createRoute())
                },
            )
        }

        composable(Route.ArchivedMatches.createRoute()) {
            ArchivedMatchesScreen(
                onNavigateToMatchSummary = { match ->
                    navController.navigate(Route.Match.createRoute(match.id))
                },
            )
        }

        composable(
            route = Route.CreateMatch.FULL_ROUTE,
            arguments =
                listOf(
                    navArgument(Route.CreateMatch.ARG_MATCH_ID) {
                        type = NavType.LongType
                        defaultValue = 0L
                    },
                ),
            // Instant transitions: same reason as PlayerWizard.
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None },
        ) { backStackEntry ->
            val matchId = backStackEntry.arguments?.getLong(Route.CreateMatch.ARG_MATCH_ID) ?: 0L
            MatchCreationWizardScreen(
                matchId = matchId,
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Route.Match.FULL_ROUTE,
            arguments =
                listOf(
                    navArgument(Route.Match.ARG_MATCH_ID) { type = NavType.LongType },
                ),
            deepLinks =
                listOf(
                    navDeepLink {
                        uriPattern = "teamflowmanager://match/{${Route.Match.ARG_MATCH_ID}}"
                    },
                ),
        ) { backStackEntry ->
            val matchId =
                backStackEntry.arguments?.getLong(Route.Match.ARG_MATCH_ID)
                    ?: error("matchId required")
            MatchScreen(matchId = matchId, onTitleChange = onTitleChange)
        }

        composable(route = Route.Settings.createRoute()) {
            SettingsScreen(
                onSignOut = {
                    navController.navigate(Route.Login.createRoute()) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }

        composable(
            route = Route.PendingTeamAssignment.createRoute(),
            // Instant transitions: no topBar, so scaffold change must be atomic.
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None },
        ) {
            PendingTeamAssignmentScreen(
                onTeamAssigned = {
                    navController.navigate(Route.Matches.createRoute()) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onSignOut = {
                    navController.navigate(Route.Login.createRoute()) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }

        composable(
            route = Route.AcceptTeamInvitation.FULL_ROUTE,
            arguments =
                listOf(
                    navArgument(Route.AcceptTeamInvitation.ARG_TEAM_ID) {
                        type = NavType.StringType
                        nullable = false
                    },
                ),
            deepLinks =
                listOf(
                    // Custom scheme deep link - always works
                    navDeepLink {
                        uriPattern = "teamflowmanager://team/accept?teamId={${Route.AcceptTeamInvitation.ARG_TEAM_ID}}"
                    },
                    // HTTPS deep link - requires server configuration
                    navDeepLink {
                        uriPattern = "https://teamflowmanager.app/team/accept?teamId={${Route.AcceptTeamInvitation.ARG_TEAM_ID}}"
                    },
                ),
        ) { backStackEntry ->
            val teamId = backStackEntry.arguments?.getString(Route.AcceptTeamInvitation.ARG_TEAM_ID)
            AcceptTeamInvitationScreen(
                teamId = teamId,
                onNavigateToLogin = { tid ->
                    // TODO: Implement proper state saving mechanism
                    // Current limitation: teamId will be lost after login
                    // Consider using SharedPreferences to persist the teamId
                    // so it can be retrieved after login is complete
                    navController.navigate(Route.Login.createRoute()) {
                        popUpTo(Route.AcceptTeamInvitation.createRoute()) { inclusive = true }
                    }
                },
                onNavigateToTeam = {
                    navController.navigate(Route.Team.createRoute(Route.Team.MODE_VIEW)) {
                        popUpTo(Route.AcceptTeamInvitation.createRoute()) { inclusive = true }
                    }
                },
                onNavigateToTeams = {
                    navController.navigate(Route.TeamList.createRoute()) {
                        popUpTo(Route.AcceptTeamInvitation.createRoute()) { inclusive = true }
                    }
                },
            )
        }
    }

    val activity = LocalActivity.current

    val backStackEntry by navController.currentBackStackEntryAsState()

    val route = Route.fromValue(backStackEntry?.destination?.route)

    val searchState = LocalSearchState.current

    BackHandler {
        when (route) {
            Route.Login -> activity?.finish()
            Route.Migration -> activity?.finish()
            Route.ClubSelection -> activity?.finish()
            Route.TeamList -> activity?.finish()
            Route.PendingTeamAssignment -> activity?.finish()
            Route.CreateClub ->
                navController.navigate(Route.ClubSelection.createRoute()) {
                    popUpTo(Route.CreateClub.createRoute()) { inclusive = true }
                }
            Route.Matches ->
                if (searchState.isActive) {
                    searchState.clear()
                    searchState.isActive = false
                } else {
                    activity?.finish()
                }

            Route.Team -> {
                val mode = backStackEntry?.arguments?.getString(Route.Team.ARG_MODE)

                when (mode) {
                    Route.Team.MODE_CREATE -> {
                        // If TeamList is in the back stack (president flow), go back there
                        if (navController.previousBackStackEntry?.destination?.route == Route.TeamList.createRoute()) {
                            navController.popBackStack()
                        } else {
                            activity?.finish()
                        }
                    }
                    Route.Team.MODE_VIEW -> navController.navigateToMatches()
                }
            }

            Route.Players -> navController.navigateToMatches()
            Route.Analysis -> navController.navigateToMatches()
            Route.Settings -> {
                // If settings was opened from deep link (back stack is shallow), go to matches instead of back
                if (navController.previousBackStackEntry == null) {
                    navController.navigateToMatches()
                } else {
                    navController.popBackStack()
                }
            }

            else -> navController.popBackStack()
        }
    }
}

private fun NavHostController.navigateToMatches() {
    navigate(Route.Matches.createRoute()) {
        popUpTo(graph.startDestinationId) { inclusive = false }
        launchSingleTop = true
        restoreState = true
    }
}

private suspend fun getGoogleIdToken(context: Context): String {
    val credentialManager = CredentialManager.create(context)
    val googleIdOption =
        GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(context.getString(R.string.default_web_client_id))
            .setAutoSelectEnabled(true)
            .build()
    val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()
    return try {
        val result = credentialManager.getCredential(request = request, context = context)
        val credential = result.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            GoogleIdTokenCredential.createFrom(credential.data).idToken
        } else {
            throw IllegalStateException("Unexpected credential type")
        }
    } catch (e: GetCredentialException) {
        throw IllegalStateException("Google sign-in failed: ${e.message}", e)
    } catch (e: GoogleIdTokenParsingException) {
        throw IllegalStateException("Failed to parse Google ID token: ${e.message}", e)
    }
}
