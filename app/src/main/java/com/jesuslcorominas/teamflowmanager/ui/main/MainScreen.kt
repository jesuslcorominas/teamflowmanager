package com.jesuslcorominas.teamflowmanager.ui.main

import androidx.annotation.StringRes
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.ui.navigation.BackHandlerController
import com.jesuslcorominas.teamflowmanager.ui.navigation.BottomNavigationBar
import com.jesuslcorominas.teamflowmanager.ui.navigation.Navigation
import com.jesuslcorominas.teamflowmanager.domain.navigation.Route

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val backHandlerController = remember { BackHandlerController() }

    val route = Route.fromValue(currentRoute)

    val arguments = backStackEntry
        ?.arguments
        ?.keySet()
        ?.associateWith { key -> backStackEntry?.arguments?.get(key) }

    val uiConfig = route?.uiConfig(arguments)

    val title = route?.toTitle(backStackEntry)

    Scaffold(
        topBar = {
            if (uiConfig?.showTopBar == true) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = title ?: "",
                            maxLines = 1,
                            style = MaterialTheme.typography.titleLarge,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    navigationIcon = {
                        if (uiConfig.canGoBack) {
                            IconButton(
                                onClick = {
                                    backHandlerController.onBackRequested?.invoke()
                                        ?: navController.popBackStack()
                                }
                            ) {
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
            currentBackHandler = backHandlerController,
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
    Route.Matches -> Route.CreateMatch.createRoute()
    else -> null
}

@Composable
private fun Route.toTitle(backStackEntry: NavBackStackEntry?): String? = when (this) {
    Route.Players -> stringResource(R.string.players_title)
    Route.Team -> stringResource((this as Route.Team).toTitleRes(backStackEntry))
    Route.Matches -> stringResource(R.string.matches_title)
    Route.ArchivedMatches -> stringResource(R.string.archived_matches)
    Route.Match ->
        "${backStackEntry?.arguments?.getString(Route.Match.ARG_TEAM)} - ${
            backStackEntry?.arguments?.getString(Route.Match.ARG_OPPONENT)
        }"

    else -> null
}

@StringRes
private fun Route.Team.toTitleRes(backStackEntry: NavBackStackEntry?): Int {
    val mode = backStackEntry?.arguments?.getString(Route.Team.ARG_MODE)

    return if (mode == Route.Team.MODE_EDIT) {
        R.string.edit_team_title
    } else {
        R.string.team_title
    }
}
