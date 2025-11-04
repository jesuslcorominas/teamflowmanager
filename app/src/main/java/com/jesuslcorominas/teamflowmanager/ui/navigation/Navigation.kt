package com.jesuslcorominas.teamflowmanager.ui.navigation

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.jesuslcorominas.teamflowmanager.domain.navigation.Route
import com.jesuslcorominas.teamflowmanager.ui.main.search.LocalSearchState
import com.jesuslcorominas.teamflowmanager.ui.matches.ArchivedMatchesScreen
import com.jesuslcorominas.teamflowmanager.ui.matches.MatchListScreen
import com.jesuslcorominas.teamflowmanager.ui.matches.MatchScreen
import com.jesuslcorominas.teamflowmanager.ui.matches.wizard.MatchCreationWizardScreen
import com.jesuslcorominas.teamflowmanager.ui.players.PlayersScreen
import com.jesuslcorominas.teamflowmanager.ui.players.wizard.PlayerWizardScreen
import com.jesuslcorominas.teamflowmanager.ui.splash.SplashScreen
import com.jesuslcorominas.teamflowmanager.ui.team.TeamScreen
import com.jesuslcorominas.teamflowmanager.ui.analysis.AnalysisScreen

@Composable
fun Navigation(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    currentBackHandler: BackHandlerController
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Route.Splash.createRoute(),
    ) {
        composable(Route.Splash.createRoute()) {
            SplashScreen(
                onNavigateToCreateTeam = {
                    navController.navigate(Route.Team.createRoute(Route.Team.MODE_CREATE)) {
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

        composable(
            route = Route.Team.FULL_ROUTE,
            arguments = listOf(
                navArgument(Route.Team.ARG_MODE) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString(Route.Team.ARG_MODE) ?: ""

            TeamScreen(
                onNavigateToMatches = { _ ->
                    navController.navigate(Route.Matches.createRoute()) {
                        popUpTo(Route.Team.createRoute(Route.Team.MODE_CREATE)) { inclusive = true }
                    }
                },
                onNavigateBackRequest = { navController.popBackStack() },
                currentBackHandler = if (mode == Route.Team.MODE_EDIT) currentBackHandler else null,
            )
        }

        composable(Route.Players.createRoute()) {
            PlayersScreen(
                onNavigateToCreatePlayer = {
                    navController.navigate(Route.PlayerWizard.createRoute())
                },
                onNavigateToEditPlayer = { playerId ->
                    navController.navigate("${Route.PlayerWizard.createRoute()}?playerId=$playerId")
                }
            )
        }

        composable(
            route = Route.PlayerWizard.FULL_ROUTE,
            arguments = listOf(
                navArgument(Route.PlayerWizard.ARG_PLAYER_ID) {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) { backStackEntry ->
            val playerId = backStackEntry.arguments?.getLong(Route.PlayerWizard.ARG_PLAYER_ID)
            PlayerWizardScreen(
                playerId = if (playerId == 0L) null else playerId,
                onNavigateBack = { navController.popBackStack() }
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
                    navController.navigate(Route.Match.createRoute(match.id, match.teamName, match.opponent))
                },
                onNavigateToArchivedMatches = {
                    navController.navigate(Route.ArchivedMatches.createRoute())
                },
            )
        }

        composable(Route.ArchivedMatches.createRoute()) {
            ArchivedMatchesScreen(
                onNavigateToMatchSummary = { match ->
                    navController.navigate(Route.Match.createRoute(match.id, match.teamName, match.opponent))
                },
            )
        }

        composable(
            route = Route.CreateMatch.FULL_ROUTE,
            arguments = listOf(
                navArgument(Route.CreateMatch.ARG_MATCH_ID) {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) {
            MatchCreationWizardScreen(
                onNavigateBack = { navController.popBackStack() },
                currentBackHandler = currentBackHandler
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
    }

    val activity = LocalContext.current as? Activity

    val backStackEntry by navController.currentBackStackEntryAsState()

    val route = Route.fromValue(backStackEntry?.destination?.route)

    val searchState = LocalSearchState.current

    BackHandler {
        when (route) {
            Route.Matches -> if (searchState.isActive) {
                searchState.clear()
                searchState.isActive = false
            } else {
                activity?.finish()
            }

            Route.Team -> {
                val mode = backStackEntry?.arguments?.getString(Route.Team.ARG_MODE)

                when (mode) {
                    Route.Team.MODE_CREATE -> activity?.finish()
                    Route.Team.MODE_VIEW -> navController.navigateToMatches()
                }
            }
            Route.Players -> navController.navigateToMatches()
            Route.Analysis -> navController.navigateToMatches()

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
