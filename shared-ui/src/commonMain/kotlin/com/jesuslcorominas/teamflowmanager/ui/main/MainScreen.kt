package com.jesuslcorominas.teamflowmanager.ui.main

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.ui.components.topbar.AppTopBar
import com.jesuslcorominas.teamflowmanager.ui.navigation.BottomNavigationBar
import com.jesuslcorominas.teamflowmanager.ui.navigation.Route
import com.jesuslcorominas.teamflowmanager.viewmodel.MainViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import teamflowmanager.shared_ui.generated.resources.Res
import teamflowmanager.shared_ui.generated.resources.add_match_title
import teamflowmanager.shared_ui.generated.resources.add_player_title
import teamflowmanager.shared_ui.generated.resources.analysis_title
import teamflowmanager.shared_ui.generated.resources.archived_matches
import teamflowmanager.shared_ui.generated.resources.create_team_title
import teamflowmanager.shared_ui.generated.resources.edit_team_title
import teamflowmanager.shared_ui.generated.resources.matches_title
import teamflowmanager.shared_ui.generated.resources.players_title
import teamflowmanager.shared_ui.generated.resources.search_match_placeholder
import teamflowmanager.shared_ui.generated.resources.settings_title
import teamflowmanager.shared_ui.generated.resources.team_list_title
import teamflowmanager.shared_ui.generated.resources.team_title

/**
 * Shared MainScreen shell: Scaffold with AppTopBar + BottomNavigationBar + FAB.
 *
 * Navigation is handled by the caller via callbacks. The content slot receives
 * the scaffold [PaddingValues] so the caller can apply them to their NavHost or
 * any other content composable.
 *
 * @param currentRoute the current destination route string (e.g. "matches")
 * @param teamMode optional Team route mode (create/view/edit) for resolving UI config
 * @param dynamicTitle optional title override (e.g. for match name set at runtime)
 * @param onBackNavigate called when the back navigation icon is tapped
 * @param onSettingsNavigate called when the settings icon is tapped
 * @param onFabClick called when the FAB is tapped
 * @param onBottomNavNavigate called with the destination string when a bottom nav tab is tapped
 * @param content slot composable that receives the scaffold padding values
 */
@Composable
fun MainScreen(
    currentRoute: String?,
    teamMode: String? = null,
    dynamicTitle: String? = null,
    viewModel: MainViewModel = koinViewModel(),
    onBackNavigate: () -> Unit = {},
    onSettingsNavigate: () -> Unit = {},
    onFabClick: () -> Unit = {},
    onBottomNavNavigate: (String) -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    val isPresident by viewModel.isPresident.collectAsState()
    val searchState = rememberSearchState()

    val route = Route.fromValue(currentRoute)
    val arguments: Map<String, Any?>? = teamMode?.let { mapOf(Route.Team.ARG_MODE to it) }
    val uiConfig = route?.uiConfig(arguments)

    val routeTitle = route?.toTitle(teamMode)
    val title = dynamicTitle ?: routeTitle ?: ""

    val searchPlaceholder = if (route is Route.Matches) {
        stringResource(Res.string.search_match_placeholder)
    } else {
        ""
    }

    CompositionLocalProvider(LocalSearchState provides searchState) {
        Scaffold(
            topBar = {
                AppTopBar(
                    modifier = Modifier.padding(top = 16.dp),
                    uiConfig = uiConfig,
                    title = title,
                    searchPlaceholder = searchPlaceholder,
                    onBack = onBackNavigate,
                    onSettings = onSettingsNavigate,
                )
            },
            bottomBar = {
                if (uiConfig?.showBottomBar == true) {
                    BottomNavigationBar(
                        currentRoute = currentRoute,
                        isPresident = isPresident,
                        onNavigate = onBottomNavNavigate,
                    )
                }
            },
            floatingActionButton = {
                if (route != null && uiConfig?.showFab == true) {
                    MainFloatingActionButton(route = route, onFabClick = onFabClick)
                }
            },
        ) { paddingValues ->
            content(paddingValues)
        }
    }
}

@Composable
private fun MainFloatingActionButton(route: Route, onFabClick: () -> Unit) {
    FloatingActionButton(onClick = onFabClick) {
        Icon(
            imageVector = if (route is Route.Team) Icons.Default.Edit else Icons.Default.Add,
            contentDescription = route.toFabContentDescription()
        )
    }
}

@Composable
private fun Route.toFabContentDescription(): String? = when (this) {
    Route.Players -> stringResource(Res.string.add_player_title)
    Route.Team -> stringResource(Res.string.edit_team_title)
    Route.TeamList -> stringResource(Res.string.create_team_title)
    Route.Matches -> stringResource(Res.string.add_match_title)
    else -> null
}

@Composable
internal fun Route.toTitle(teamMode: String? = null): String? = when (this) {
    Route.Players -> stringResource(Res.string.players_title)
    Route.Team -> when (teamMode) {
        Route.Team.MODE_EDIT -> stringResource(Res.string.edit_team_title)
        else -> stringResource(Res.string.team_title)
    }
    Route.TeamList -> stringResource(Res.string.team_list_title)
    Route.Matches -> stringResource(Res.string.matches_title)
    Route.ArchivedMatches -> stringResource(Res.string.archived_matches)
    Route.Analysis -> stringResource(Res.string.analysis_title)
    Route.Settings -> stringResource(Res.string.settings_title)
    Route.Match -> null
    else -> null
}
