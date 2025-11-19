package com.jesuslcorominas.teamflowmanager.ui.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.navigation.Route
import com.jesuslcorominas.teamflowmanager.ui.components.topbar.AppTopBar
import com.jesuslcorominas.teamflowmanager.ui.main.search.LocalSearchState
import com.jesuslcorominas.teamflowmanager.ui.main.search.rememberSearchState
import com.jesuslcorominas.teamflowmanager.ui.navigation.BackHandlerController
import com.jesuslcorominas.teamflowmanager.ui.navigation.BottomNavigationBar
import com.jesuslcorominas.teamflowmanager.ui.navigation.Navigation
import com.jesuslcorominas.teamflowmanager.viewmodel.MainViewModel
import kotlinx.coroutines.flow.firstOrNull
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    pendingMatchNavigation: MatchNavigation? = null,
    onNavigationHandled: () -> Unit = {},
    viewModel: MainViewModel = koinViewModel()
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val searchState = rememberSearchState()

    val currentRoute = backStackEntry?.destination?.route

    val backHandlerController = remember { BackHandlerController() }

    val route = Route.fromValue(currentRoute)

    val arguments = backStackEntry
        ?.arguments
        ?.keySet()
        ?.associateWith { key -> backStackEntry?.arguments?.get(key) }

    val uiConfig = route?.uiConfig(arguments)

    // Handle pending match navigation from notification
    LaunchedEffect(pendingMatchNavigation) {
        pendingMatchNavigation?.let { navigation ->
            // Get match details to build proper route
            viewModel.getMatchById(navigation.matchId).firstOrNull()?.let { match ->
                val matchRoute = Route.Match.createRoute(
                    navigation.matchId,
                    match.team,
                    match.opponent
                )
                // Navigate to match detail
                navController.navigate(matchRoute) {
                    // Keep Matches in back stack for proper back navigation
                    launchSingleTop = true
                }
                // Clear pending navigation after successful navigation
                onNavigationHandled()
            }
        }
    }

    val title = route?.toTitle(backStackEntry)

    CompositionLocalProvider(LocalSearchState provides searchState) {
        Scaffold(
            topBar = {
                AppTopBar(
                    modifier = Modifier.padding(top = 16.dp),
                    uiConfig = uiConfig,
                    title = title,
                    backHandlerController = backHandlerController,
                    searchPlaceholder = if (route is Route.Matches) stringResource(R.string.search_match_placeholder) else "",
                    navController = navController
                )
            },
            bottomBar = {
                if (uiConfig?.showBottomBar == true) {
                    BottomNavigationBar(navController = navController)
                }
            },
            floatingActionButton = {
                if (uiConfig?.showFab == true) {
                    RouteFloatingActionButton(route, navController)
                }
            },
        ) { paddingValues ->
            Navigation(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                navController = navController,
                currentBackHandler = backHandlerController,
            )
        }
    }
}

@Composable
private fun RouteFloatingActionButton(route: Route, navController: NavHostController) {
    FloatingActionButton(
        onClick = { route.toDestination()?.let { navController.navigate(it) } },
    ) {
        Icon(
            imageVector = route.toFABIcon(),
            contentDescription = route.toFABContentDescriptionRes()?.let { stringResource(it) } ?: ""
        )
    }
}

// region Route extensions

private fun Route.toFABIcon() = when (this) {
    Route.Team -> Icons.Default.Edit
    else -> Icons.Default.Add
}

private fun Route.toFABContentDescriptionRes(): Int? = when (this) {
    Route.Players -> R.string.add_player_title
    Route.Team -> R.string.edit_team_title
    Route.Matches -> R.string.add_match_title
    else -> null
}

private fun Route.toDestination() = when (this) {
    Route.Team -> Route.Team.createRoute(Route.Team.MODE_EDIT)
    Route.Matches -> Route.CreateMatch.createRoute(Route.CreateMatch.DEFAULT_MATCH_ID)
    else -> null
}

@Composable
private fun Route.toTitle(backStackEntry: NavBackStackEntry?): String? = when (this) {
    Route.Players -> stringResource(R.string.players_title)
    Route.Team -> backStackEntry?.arguments?.getString(Route.Team.ARG_MODE).let { mode ->
        stringResource(if (mode == Route.Team.MODE_EDIT) R.string.edit_team_title else R.string.team_title)
    }

    Route.Matches -> stringResource(R.string.matches_title)
    Route.ArchivedMatches -> stringResource(R.string.archived_matches)
    Route.Analysis -> stringResource(R.string.analysis_title)
    Route.Match ->
        "${backStackEntry?.arguments?.getString(Route.Match.ARG_TEAM)} - ${
            backStackEntry?.arguments?.getString(Route.Match.ARG_OPPONENT)
        }"

    else -> null
}
// endregion
