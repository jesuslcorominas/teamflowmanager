package com.jesuslcorominas.teamflowmanager.ui.navigation

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.jesuslcorominas.teamflowmanager.ui.matches.ArchivedMatchesScreen
import com.jesuslcorominas.teamflowmanager.ui.matches.MatchDetailScreen
import com.jesuslcorominas.teamflowmanager.ui.matches.MatchListScreen
import com.jesuslcorominas.teamflowmanager.ui.matches.MatchScreen
import com.jesuslcorominas.teamflowmanager.ui.matches.wizard.MatchCreationWizardScreen
import com.jesuslcorominas.teamflowmanager.ui.players.PlayersScreen
import com.jesuslcorominas.teamflowmanager.ui.splash.SplashScreen
import com.jesuslcorominas.teamflowmanager.ui.team.TeamScreen
import com.jesuslcorominas.teamflowmanager.ui.teamdetail.TeamDetailScreen

@Composable
fun Navigation(
    modifier: Modifier = Modifier,
    navController: NavHostController,
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Route.Splash.createRoute(),
    ) {
        composable(Route.Splash.createRoute()) {
            SplashScreen(
                onNavigateToCreateTeam = {
                    navController.navigate(Route.CreateTeam.createRoute()) {
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

        composable(Route.CreateTeam.createRoute()) {
            TeamScreen(
                onNavigateToMatches = { _ ->
                    navController.navigate(Route.Matches.createRoute()) {
                        popUpTo(Route.CreateTeam.createRoute()) { inclusive = true }
                    }
                },
            )
        }

        composable(Route.Players.createRoute()) {
            PlayersScreen()
        }

        composable(Route.TeamDetail.createRoute()) {
            TeamDetailScreen()
        }

        composable(Route.Matches.createRoute()) {
            MatchListScreen(
                onNavigateToEditMatch = { matchId ->
                    navController.navigate(Route.MatchDetail.createRoute(matchId))
                },
                onNavigateToMatch = { match ->
                    navController.navigate(Route.Match.createRoute(match.id, match.teamName, match.opponent))
                },
                onNavigateToArchivedMatches = {
                    navController.navigate(Route.ArchivedMatches.createRoute())
                },
            )
        }

        composable(Route.ArchivedMatches.createRoute()) {
            ArchivedMatchesScreen(
                onNavigateToMatchSummary = { matchId, team, opponent ->
                    navController.navigate(Route.Match.createRoute(matchId, team, opponent))
                },
            )
        }

        composable(Route.CreateMatch.createRoute()) {
            MatchCreationWizardScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
            )
        }

        composable(
            route = Route.Match.FULL_ROUTE,
            arguments = listOf(
                navArgument(Route.Match.ARG_MATCH_ID) {
                    type = NavType.LongType
                },
                navArgument(Route.Match.ARG_TEAM) {
                    type = NavType.StringType
                },
                navArgument(Route.Match.ARG_OPPONENT) {
                    type = NavType.StringType
                },
            )
        ) {
            MatchScreen()
        }

        composable(
            route = "${Route.MatchDetail.createRoute()}/{matchId}",
            arguments = listOf(
                navArgument(Route.MatchDetail.ARG_MATCH_ID) {
                    type = NavType.LongType
                },
            ),
        ) { backStackEntry ->
            val matchId = backStackEntry.arguments?.getLong(Route.MatchDetail.ARG_MATCH_ID)
            MatchDetailScreen(
                matchId = matchId,
                onNavigateBack = {
                    navController.popBackStack()
                },
            )
        }
    }

    val activity = LocalContext.current as? Activity
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    BackHandler {
        when (currentRoute) {
            Route.CreateTeam.createRoute() -> activity?.finish()

            Route.TeamDetail.createRoute(),
            Route.Players.createRoute() -> navController.navigate(Route.Matches.createRoute()) {
                popUpTo(navController.graph.startDestinationId) { inclusive = false }
                launchSingleTop = true
                restoreState = true
            }

            Route.Matches.createRoute() -> activity?.finish()
            else -> navController.popBackStack()
        }
    }
}
