package com.jesuslcorominas.teamflowmanager.ui.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.ui.navigation.BottomNavigationBar
import com.jesuslcorominas.teamflowmanager.ui.navigation.Navigation
import com.jesuslcorominas.teamflowmanager.ui.navigation.Route

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val route = Route.fromValue(currentRoute)
    val uiConfig = route?.uiConfig(null)

    val title = route?.toTitle(backStackEntry)

    Scaffold(
        topBar = {
            if (uiConfig?.showTopBar == true) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = title ?: "",
                            style = MaterialTheme.typography.titleLarge,
                        )
                    },
                    navigationIcon = {
                        if (uiConfig.canGoBack) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.close),
                                )
                            }
                        }
                    },
                )
            }
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
        )
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

private fun Route.toFABIcon() = when (this) {
    Route.TeamDetail -> Icons.Default.Edit
    else -> Icons.Default.Add
}

private fun Route.toFABContentDescriptionRes(): Int? = when (this) {
    Route.Players -> R.string.add_player_title
    Route.TeamDetail -> R.string.edit_team_title
    Route.Matches -> R.string.add_match_title
    else -> null
}

private fun Route.toDestination() = when (this) {
    Route.Matches -> Route.CreateMatch.createRoute()
    else -> null
}

@Composable
private fun Route.toTitle(backStackEntry: NavBackStackEntry?): String? = when (this) {
    Route.Players -> stringResource(R.string.players_title)
    Route.TeamDetail -> stringResource(R.string.team_title)
    Route.Matches -> stringResource(R.string.matches_title)
    Route.ArchivedMatches -> stringResource(R.string.archived_matches)
    Route.Match ->
        "${backStackEntry?.arguments?.getString(Route.Match.ARG_TEAM)} - ${
            backStackEntry?.arguments?.getString(Route.Match.ARG_OPPONENT)
        }"

    else -> null
}
