package com.jesuslcorominas.teamflowmanager.ui.screens

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.navigation.Route
import com.jesuslcorominas.teamflowmanager.ui.components.bottomnav.BottomNavItem
import com.jesuslcorominas.teamflowmanager.ui.components.topbar.AppTopBar
import com.jesuslcorominas.teamflowmanager.ui.main.search.LocalSearchState
import com.jesuslcorominas.teamflowmanager.ui.main.search.rememberSearchState
import com.jesuslcorominas.teamflowmanager.ui.navigation.BackHandlerController
import com.jesuslcorominas.teamflowmanager.ui.theme.BackgroundContrast
import com.jesuslcorominas.teamflowmanager.ui.theme.ContentContrast
import com.jesuslcorominas.teamflowmanager.ui.theme.Primary
import com.jesuslcorominas.teamflowmanager.ui.theme.PrimaryLight
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing

class MainTabScreen : Screen {
    @Composable
    override fun Content() {
        val searchState = rememberSearchState()

        CompositionLocalProvider(LocalSearchState provides searchState) {
            TabNavigator(MatchesTab) {
                MainScaffold()
                VoyagerBackHandler()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScaffold() {
    val tabNavigator = LocalTabNavigator.current
    val navigator = LocalNavigator.currentOrThrow
    val searchState = LocalSearchState.current

    // Determine current route configuration
    val currentTab = tabNavigator.current
    val uiConfig = when (currentTab) {
        is MatchesTab -> Route.Matches.uiConfig(null)
        is PlayersTab -> Route.Players.uiConfig(null)
        is TeamTab -> {
            val mode = (currentTab as? TeamTab)?.mode ?: Route.Team.MODE_VIEW
            Route.Team.uiConfig(mapOf(Route.Team.ARG_MODE to mode))
        }
        is AnalysisTab -> Route.Analysis.uiConfig(null)
        else -> null
    }

    val title = when (currentTab) {
        is MatchesTab -> stringResource(R.string.matches_title)
        is PlayersTab -> stringResource(R.string.players_title)
        is TeamTab -> {
            val mode = (currentTab as? TeamTab)?.mode ?: Route.Team.MODE_VIEW
            stringResource(if (mode == Route.Team.MODE_EDIT) R.string.edit_team_title else R.string.team_title)
        }
        is AnalysisTab -> stringResource(R.string.analysis_title)
        else -> null
    }

    val backHandlerController = androidx.compose.runtime.remember { BackHandlerController() }

    Scaffold(
        topBar = {
            VoyagerAppTopBarWrapper(
                modifier = Modifier.padding(top = TFMSpacing.spacing04),
                uiConfig = uiConfig,
                title = title,
                backHandlerController = backHandlerController,
                searchPlaceholder = if (currentTab is MatchesTab) stringResource(R.string.search_match_placeholder) else ""
            )
        },
        bottomBar = {
            if (uiConfig?.showBottomBar == true) {
                BottomBar()
            }
        },
        floatingActionButton = {
            if (uiConfig?.showFab == true) {
                FloatingActionButton(
                    onClick = {
                        when (currentTab) {
                            is TeamTab -> tabNavigator.current = TeamTab(Route.Team.MODE_EDIT)
                            is MatchesTab -> navigator.push(MatchCreationWizardScreen(0L))
                            else -> {}
                        }
                    }
                ) {
                    val icon = when (currentTab) {
                        is TeamTab -> Icons.Default.Edit
                        else -> Icons.Default.Add
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null
                    )
                }
            }
        }
    ) { paddingValues ->
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            CurrentTab()
        }
    }
}

@Composable
private fun VoyagerAppTopBarWrapper(
    modifier: Modifier,
    uiConfig: Route.UiConfig?,
    title: String?,
    backHandlerController: BackHandlerController,
    searchPlaceholder: String
) {
    val navigator = LocalNavigator.currentOrThrow
    val tabNavigator = LocalTabNavigator.current

    // Handle back navigation
    androidx.activity.compose.BackHandler(enabled = uiConfig?.canGoBack == true) {
        backHandlerController.onBackRequested?.invoke() ?: run {
            if (tabNavigator.current is TeamTab) {
                tabNavigator.current = TeamTab(Route.Team.MODE_VIEW)
            } else {
                navigator.pop()
            }
        }
    }

    // Use the Voyager-compatible top bar
    if (uiConfig?.showTopBar == true) {
        com.jesuslcorominas.teamflowmanager.ui.components.topbar.VoyagerAppTopBar(
            modifier = modifier,
            uiConfig = uiConfig,
            title = title,
            searchPlaceholder = searchPlaceholder,
            onBackClick = {
                backHandlerController.onBackRequested?.invoke() ?: run {
                    if (tabNavigator.current is TeamTab) {
                        tabNavigator.current = TeamTab(Route.Team.MODE_VIEW)
                    } else {
                        navigator.pop()
                    }
                }
            }
        )
    }
}

@Composable
private fun BottomBar() {
    val tabNavigator = LocalTabNavigator.current

    Surface(
        shape = RoundedCornerShape(topStart = TFMSpacing.spacing06, topEnd = TFMSpacing.spacing06),
        tonalElevation = TFMSpacing.spacing01,
        shadowElevation = TFMSpacing.spacing02,
        color = BackgroundContrast
    ) {
        NavigationBar(containerColor = Color.Transparent) {
            TabNavigationItem(MatchesTab)
            TabNavigationItem(PlayersTab)
            TabNavigationItem(AnalysisTab)
            TabNavigationItem(TeamTab())
        }
    }
}

@Composable
private fun RowScope.TabNavigationItem(tab: Tab) {
    val tabNavigator = LocalTabNavigator.current
    val isSelected = when {
        tab is TeamTab && tabNavigator.current is TeamTab -> true
        tab is PlayersTab && tabNavigator.current is PlayersTab -> true
        tab is AnalysisTab && tabNavigator.current is AnalysisTab -> true
        tab is MatchesTab && tabNavigator.current is MatchesTab -> true
        else -> false
    }

    val icon = tab.options.icon
    val title = tab.options.title

    NavigationBarItem(
        alwaysShowLabel = false,
        icon = {
            if (icon != null) {
                BottomNavItem(
                    iconVector = icon,
                    labelResId = when (tab) {
                        is MatchesTab -> R.string.nav_matches
                        is PlayersTab -> R.string.nav_players
                        is AnalysisTab -> R.string.nav_analysis
                        is TeamTab -> R.string.nav_team
                        else -> 0
                    },
                    isSelected = isSelected
                )
            }
        },
        selected = isSelected,
        onClick = {
            if (!isSelected) {
                // Reset to first screen when clicking on same tab
                tabNavigator.current = when (tab) {
                    is TeamTab -> TeamTab(Route.Team.MODE_VIEW)
                    else -> tab
                }
            }
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = ContentContrast,
            unselectedIconColor = PrimaryLight,
            selectedTextColor = ContentContrast,
            unselectedTextColor = PrimaryLight,
            indicatorColor = Primary
        )
    )
}

// Tab definitions
object MatchesTab : Tab {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        com.jesuslcorominas.teamflowmanager.ui.matches.MatchListScreen(
            onNavigateToEditMatch = { matchId ->
                navigator.push(MatchCreationWizardScreen(matchId))
            },
            onNavigateToMatch = { match ->
                navigator.push(MatchDetailScreen(match.id, match.teamName, match.opponent))
            },
            onNavigateToArchivedMatches = {
                navigator.push(ArchivedMatchesScreen())
            }
        )
    }

    override val options: TabOptions
        @Composable
        get() = TabOptions(
            index = 0u,
            title = stringResource(R.string.nav_matches),
            icon = Icons.Default.SportsSoccer
        )
}

object PlayersTab : Tab {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        com.jesuslcorominas.teamflowmanager.ui.players.PlayersScreen(
            onNavigateToCreatePlayer = {
                navigator.push(PlayerWizardScreen(0L))
            },
            onNavigateToEditPlayer = { playerId ->
                navigator.push(PlayerWizardScreen(playerId))
            }
        )
    }

    override val options: TabOptions
        @Composable
        get() = TabOptions(
            index = 1u,
            title = stringResource(R.string.nav_players),
            icon = Icons.Default.Group
        )
}

object AnalysisTab : Tab {
    @Composable
    override fun Content() {
        com.jesuslcorominas.teamflowmanager.ui.analysis.AnalysisScreen()
    }

    override val options: TabOptions
        @Composable
        get() = TabOptions(
            index = 2u,
            title = stringResource(R.string.nav_analysis),
            icon = Icons.Default.BarChart
        )
}

data class TeamTab(val mode: String = Route.Team.MODE_VIEW) : Tab {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val tabNavigator = LocalTabNavigator.current
        val backHandlerController = androidx.compose.runtime.remember { BackHandlerController() }

        com.jesuslcorominas.teamflowmanager.ui.team.TeamScreen(
            onNavigateToMatches = { _ ->
                tabNavigator.current = MatchesTab
            },
            onNavigateBackRequest = {
                if (mode == Route.Team.MODE_EDIT) {
                    tabNavigator.current = TeamTab(Route.Team.MODE_VIEW)
                } else {
                    tabNavigator.current = MatchesTab
                }
            },
            currentBackHandler = if (mode == Route.Team.MODE_EDIT) backHandlerController else null
        )
    }

    override val options: TabOptions
        @Composable
        get() = TabOptions(
            index = 3u,
            title = stringResource(R.string.nav_team),
            icon = Icons.Default.Groups
        )
}
