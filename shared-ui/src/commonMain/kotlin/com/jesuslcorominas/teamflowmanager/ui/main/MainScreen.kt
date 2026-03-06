package com.jesuslcorominas.teamflowmanager.ui.main

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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.ui.components.BlurredBox
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

private val FabHeight = 56.dp       // standard Material FAB size
private val FabBarGap = 16.dp       // gap between FAB bottom and bar top (matches Android Scaffold default)
private val FabContentGap = 16.dp   // gap between last list item and FAB bottom

/**
 * Shared MainScreen shell for iOS.
 *
 * Layout:
 * - Content has NO clip at the bottom → list reaches the screen bottom edge.
 * - A native UIVisualEffectView (via [BlurredBox]) covers the bar zone so the
 *   content beneath is truly blurred, not merely tinted.
 * - The pill-shaped BottomNavigationBar floats as an overlay; its surroundings
 *   are transparent, giving the "floating" feel.
 * - Content bottom padding = barHeight + FABBarGap + FabHeight + FabContentGap
 *   so the last list item can always be scrolled fully above the FAB.
 * - The FAB sits FabBarGap (16 dp) above the bar top, matching Android's behaviour.
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

    var bottomNavHeightPx by remember { mutableStateOf(0) }

    CompositionLocalProvider(LocalSearchState provides searchState) {
        Box(modifier = Modifier.fillMaxSize()) {
            val bottomNavHeightDp: Dp = with(LocalDensity.current) { bottomNavHeightPx.toDp() }

            // Content bottom padding: enough space so the last item can scroll
            // fully above the FAB (bar + gap + FAB height + gap above FAB).
            val contentBottomPadding = if (uiConfig?.showFab == true) {
                bottomNavHeightDp + FabBarGap + FabHeight + FabContentGap
            } else {
                bottomNavHeightDp
            }

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
                        bottom = contentBottomPadding,
                    )
                )
            }

            // Frosted-glass overlay covering the bar zone (bar height + 32 dp above for fade-in).
            // On iOS: real UIVisualEffectView blur. On Android: gradient approximation.
            if (uiConfig?.showBottomBar == true && bottomNavHeightDp > 0.dp) {
                BlurredBox(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(bottomNavHeightDp + 32.dp),
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

            // FAB: FabBarGap (16 dp) above the bar top, matching Android Scaffold behaviour.
            // bottomNavHeightDp already includes the nav-bar inset.
            if (route != null && uiConfig?.showFab == true) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 24.dp, bottom = bottomNavHeightDp + FabBarGap),
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
