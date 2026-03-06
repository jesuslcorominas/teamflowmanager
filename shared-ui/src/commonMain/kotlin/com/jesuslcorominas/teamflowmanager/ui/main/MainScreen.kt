package com.jesuslcorominas.teamflowmanager.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
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
 * Shared MainScreen shell: Scaffold with AppTopBar + floating BottomNavigationBar + FAB.
 *
 * The bottom navigation bar is rendered as a Box overlay (not in Scaffold's bottomBar slot)
 * so the Scaffold surface doesn't paint an opaque background in the gap around the pill.
 * The bar height is measured via [onSizeChanged] and forwarded as bottom padding to both
 * the Scaffold content and the FAB, so lists scroll above the bar and the FAB stays visible.
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

    // Measured height of the floating bar overlay (px). Used to add equivalent bottom
    // padding to the Scaffold content and FAB so nothing hides behind the bar.
    var bottomNavHeightPx by remember { mutableStateOf(0) }

    CompositionLocalProvider(LocalSearchState provides searchState) {
        Box(modifier = Modifier.fillMaxSize()) {
            val bottomNavHeightDp: Dp = with(LocalDensity.current) { bottomNavHeightPx.toDp() }

            Scaffold(
                topBar = {
                    AppTopBar(
                        modifier = Modifier,
                        uiConfig = uiConfig,
                        title = title,
                        searchPlaceholder = searchPlaceholder,
                        onBack = onBackNavigate,
                        onSettings = onSettingsNavigate,
                    )
                },
                // bottomBar intentionally omitted — BottomNavigationBar is overlaid as a
                // Box child so no opaque Scaffold surface appears around the floating pill.
                contentWindowInsets = WindowInsets(0),
                floatingActionButton = {
                    if (route != null && uiConfig?.showFab == true) {
                        // Lift the FAB above the floating bar.
                        Box(modifier = Modifier.padding(bottom = bottomNavHeightDp)) {
                            MainFloatingActionButton(route = route, onFabClick = onFabClick)
                        }
                    }
                },
            ) { paddingValues ->
                // Forward scaffold padding but replace bottom with the measured bar height so
                // lists/content can scroll fully above the floating bar.
                content(
                    PaddingValues(
                        start = 0.dp,
                        top = paddingValues.calculateTopPadding(),
                        end = 0.dp,
                        bottom = paddingValues.calculateBottomPadding() + bottomNavHeightDp,
                    )
                )
            }

            if (uiConfig?.showBottomBar == true) {
                BottomNavigationBar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .onSizeChanged { bottomNavHeightPx = it.height },
                    currentRoute = currentRoute,
                    isPresident = isPresident,
                    onNavigate = onBottomNavNavigate,
                )
            }
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
