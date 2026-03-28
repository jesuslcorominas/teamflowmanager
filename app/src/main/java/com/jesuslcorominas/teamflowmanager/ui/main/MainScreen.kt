package com.jesuslcorominas.teamflowmanager.ui.main

import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.ui.components.topbar.AppTopBar
import com.jesuslcorominas.teamflowmanager.ui.main.search.LocalSearchState
import com.jesuslcorominas.teamflowmanager.ui.main.search.rememberSearchState
import com.jesuslcorominas.teamflowmanager.ui.navigation.BackHandlerController
import com.jesuslcorominas.teamflowmanager.ui.navigation.BottomNavigationBar
import com.jesuslcorominas.teamflowmanager.ui.navigation.Navigation
import com.jesuslcorominas.teamflowmanager.ui.navigation.PendingNavigation
import com.jesuslcorominas.teamflowmanager.ui.navigation.Route
import com.jesuslcorominas.teamflowmanager.viewmodel.MainViewModel
import org.koin.androidx.compose.koinViewModel

private val FabHeight = 56.dp
private val FabBarGap = 16.dp
private val FabContentGap = 16.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    pendingNavigation: PendingNavigation? = null,
    viewModel: MainViewModel = koinViewModel(),
) {
    val navController = rememberNavController()
    val isPresident by viewModel.isPresident.collectAsState()

    LaunchedEffect(pendingNavigation) {
        when (val nav = pendingNavigation) {
            is PendingNavigation.DeepLink -> {
                navController.handleDeepLink(nav.intent)
            }

            is PendingNavigation.Match -> {
                val route =
                    Route.Match.createRoute(
                        nav.matchId,
                    )
                navController.navigate(route) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = false }
                    launchSingleTop = true
                }
            }

            else -> {
                // No pending navigation
            }
        }
    }

    MainScaffold(navController = navController, isPresident = isPresident)
}

@Composable
private fun MainScaffold(
    navController: NavHostController,
    isPresident: Boolean,
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val backHandlerController = remember { BackHandlerController() }
    val searchState = rememberSearchState()

    val route = Route.fromValue(backStackEntry?.destination?.route)

    val arguments =
        backStackEntry
            ?.arguments
            ?.keySet()
            ?.associateWith { key -> backStackEntry?.arguments?.get(key) }

    val uiConfig = route?.uiConfig(arguments)

    var dynamicTitle by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(route) {
        dynamicTitle = null
    }

    val routeTitle = route?.toTitleRes(backStackEntry)?.let { stringResource(it) }

    val title = dynamicTitle ?: routeTitle ?: ""

    CompositionLocalProvider(
        LocalSearchState provides searchState,
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets(0),
            topBar = {
                AppTopBar(
                    modifier = Modifier.padding(top = 16.dp),
                    uiConfig = uiConfig,
                    title = title,
                    backHandlerController = backHandlerController,
                    searchPlaceholder =
                        if (route is Route.Matches) {
                            stringResource(
                                R.string.search_match_placeholder,
                            )
                        } else {
                            ""
                        },
                    navController = navController,
                )
            },
            bottomBar = {
                if (uiConfig?.showBottomBar == true) {
                    BottomNavigationBar(navController = navController, isPresident = isPresident)
                }
            },
            floatingActionButton = {
                if (route != null && uiConfig?.showFab == true) {
                    RouteFloatingActionButton(route, navController)
                }
            },
        ) { paddingValues ->
            val contentBottomPadding: Dp =
                if (uiConfig?.showFab == true) {
                    paddingValues.calculateBottomPadding() + FabBarGap + FabHeight + FabContentGap
                } else {
                    paddingValues.calculateBottomPadding()
                }
            CompositionLocalProvider(
                LocalContentBottomPadding provides contentBottomPadding,
            ) {
                Navigation(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            // Only apply top padding. The bottomBar is drawn on top of the content
                            // by the Scaffold, so applying bottom padding here creates an empty white
                            // gap. The FAB and bar clearance is exposed via LocalContentBottomPadding.
                            .padding(top = paddingValues.calculateTopPadding()),
                    navController = navController,
                    currentBackHandler = backHandlerController,
                    onTitleChange = { dynamicTitle = it },
                )
            }
        }
    }
}

@Composable
private fun RouteFloatingActionButton(
    route: Route,
    navController: NavHostController,
) {
    FloatingActionButton(
        onClick = { route.toDestination()?.let { navController.navigate(it) } },
    ) {
        Icon(
            imageVector = route.toFABIcon(),
            contentDescription = route.toFABContentDescriptionRes()?.let { stringResource(it) } ?: "",
        )
    }
}

// region Route extensions

private fun Route.toFABIcon() =
    when (this) {
        Route.Team -> Icons.Default.Edit
        else -> Icons.Default.Add
    }

private fun Route.toFABContentDescriptionRes(): Int? =
    when (this) {
        Route.Players -> R.string.add_player_title
        Route.Team -> R.string.edit_team_title
        Route.TeamList -> R.string.create_team_title
        Route.Matches -> R.string.add_match_title
        else -> null
    }

private fun Route.toDestination() =
    when (this) {
        Route.Team -> Route.Team.createRoute(Route.Team.MODE_EDIT)
        Route.TeamList -> Route.Team.createRoute(Route.Team.MODE_CREATE)
        Route.Matches -> Route.CreateMatch.createRoute(Route.CreateMatch.DEFAULT_MATCH_ID)
        Route.Players -> Route.PlayerWizard.createRoute(0L)
        else -> null
    }

fun Route.toTitleRes(backStackEntry: NavBackStackEntry?): Int? =
    when (this) {
        Route.Players -> R.string.players_title

        Route.Team -> {
            val mode = backStackEntry?.arguments?.getString(Route.Team.ARG_MODE)
            if (mode == Route.Team.MODE_EDIT) {
                R.string.edit_team_title
            } else {
                R.string.team_title
            }
        }

        Route.TeamList -> R.string.team_list_title
        Route.Matches -> R.string.matches_title
        Route.ArchivedMatches -> R.string.archived_matches
        Route.Analysis -> R.string.analysis_title
        Route.Settings -> R.string.settings_title
        Route.ClubSettings -> R.string.club_settings_title

        Route.Match -> null

        else -> null
    }
// endregion
