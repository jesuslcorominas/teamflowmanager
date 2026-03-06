package com.jesuslcorominas.teamflowmanager.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.ui.components.topbar.AppTopBar
import com.jesuslcorominas.teamflowmanager.ui.navigation.BottomNavigationBar
import com.jesuslcorominas.teamflowmanager.ui.navigation.Route
import com.jesuslcorominas.teamflowmanager.ui.theme.BackgroundContrast
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
 * Shared MainScreen shell for iOS.
 *
 * Layout strategy:
 * - Content (list) has NO bottom padding → extends all the way to the screen bottom.
 * - A semi-transparent frosted-glass gradient covers the bar zone so list items
 *   entering that area are progressively obscured without a hard clip.
 * - The pill-shaped BottomNavigationBar floats as a Box overlay; its surroundings
 *   (between pill and device edges) are part of the frosted overlay, not opaque.
 * - The FAB is placed 8 dp above the bar top so it is clearly separated.
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

    // Height of the floating bar overlay (including navigationBarsPadding inset).
    var bottomNavHeightPx by remember { mutableStateOf(0) }

    CompositionLocalProvider(LocalSearchState provides searchState) {
        Box(modifier = Modifier.fillMaxSize()) {
            val bottomNavHeightDp: Dp = with(LocalDensity.current) { bottomNavHeightPx.toDp() }

            // The Scaffold provides top padding (status bar + topBar height) to the content.
            // Bottom padding is intentionally 0: the list reaches the screen bottom edge.
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
                contentWindowInsets = WindowInsets(0),
            ) { paddingValues ->
                content(
                    PaddingValues(
                        start = 0.dp,
                        top = paddingValues.calculateTopPadding(),
                        end = 0.dp,
                        bottom = 0.dp, // list goes to screen bottom; frosted overlay handles UX
                    )
                )
            }

            // Frosted-glass overlay — semi-transparent dark gradient that covers the bar zone.
            // The gradient is NOT fully opaque so the bar "floats" over the content background
            // rather than cutting it with a solid wall. The pill Surface on top adds the fully
            // opaque bar element.
            if (uiConfig?.showBottomBar == true && bottomNavHeightDp > 0.dp) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(bottomNavHeightDp + 24.dp) // 24 dp above bar for smooth fade-in
                        .background(
                            Brush.verticalGradient(
                                0.00f to Color.Transparent,
                                0.35f to BackgroundContrast.copy(alpha = 0.30f),
                                0.70f to BackgroundContrast.copy(alpha = 0.72f),
                                1.00f to BackgroundContrast.copy(alpha = 0.90f),
                            )
                        )
                )
            }

            // Floating pill navigation bar.
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

            // FAB: positioned 8 dp above the bar top. bottomNavHeightDp already includes the
            // nav-bar inset (onSizeChanged fires after navigationBarsPadding is applied), so no
            // extra navigationBarsPadding is needed here.
            if (route != null && uiConfig?.showFab == true) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 24.dp, bottom = bottomNavHeightDp + 8.dp),
                ) {
                    MainFloatingActionButton(route = route, onFabClick = onFabClick)
                }
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
