package com.jesuslcorominas.teamflowmanager.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.jesuslcorominas.teamflowmanager.ui.matches.CurrentMatchScreen
import com.jesuslcorominas.teamflowmanager.ui.matches.MatchDetailScreen
import com.jesuslcorominas.teamflowmanager.ui.matches.MatchListScreen
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
        composable(Route.Splash.path) {
            SplashScreen(
                onNavigateToCreateTeam = {
                    navController.navigate(Route.CreateTeam.createRoute()) {
                        popUpTo(Route.Splash.path) { inclusive = true }
                    }
                },
                onNavigateToPlayers = {
                    navController.navigate(Route.Players.createRoute()) {
                        popUpTo(Route.Splash.path) { inclusive = true }
                    }
                },
            )
        }

        composable(Route.CreateTeam.path) {
            TeamScreen(
                onNavigateToPlayers = { _ ->
                    navController.navigate(Route.Players.createRoute()) {
                        popUpTo(Route.CreateTeam.path) { inclusive = true }
                    }
                },
            )
        }

        composable(Route.Players.path) {
            PlayersScreen()
        }

        composable(Route.TeamDetail.path) {
            TeamDetailScreen()
        }

        composable(Route.Matches.path) {
            MatchListScreen(
                onNavigateToAddMatch = {
                    navController.navigate(Route.MatchDetail.createRoute(null))
                },
                onNavigateToEditMatch = { matchId ->
                    navController.navigate(Route.MatchDetail.createRoute(matchId))
                },
                onNavigateToCurrentMatch = {
                    navController.navigate(Route.CurrentMatch.createRoute())
                },
            )
        }

        composable(Route.CurrentMatch.path) {
            CurrentMatchScreen()
        }

        composable(
            route = "${Route.MatchDetail.path}/{matchId}",
            arguments = listOf(
                navArgument("matchId") {
                    type = NavType.LongType
                },
            ),
        ) { backStackEntry ->
            val matchId = backStackEntry.arguments?.getLong("matchId")
            MatchDetailScreen(
                matchId = matchId,
                onNavigateBack = {
                    navController.popBackStack()
                },
            )
        }

        composable(Route.MatchDetail.path) {
            MatchDetailScreen(
                matchId = null,
                onNavigateBack = {
                    navController.popBackStack()
                },
            )
        }
    }
}
