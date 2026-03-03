package com.jesuslcorominas.teamflowmanager.ui.navigation

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.ui.components.bottomnav.BottomNavItem
import com.jesuslcorominas.teamflowmanager.ui.theme.BackgroundContrast
import com.jesuslcorominas.teamflowmanager.ui.theme.ContentContrast
import com.jesuslcorominas.teamflowmanager.ui.theme.Primary
import com.jesuslcorominas.teamflowmanager.ui.theme.PrimaryLight
import org.jetbrains.compose.resources.stringResource
import teamflowmanager.shared_ui.generated.resources.Res
import teamflowmanager.shared_ui.generated.resources.nav_analysis
import teamflowmanager.shared_ui.generated.resources.nav_matches
import teamflowmanager.shared_ui.generated.resources.nav_players
import teamflowmanager.shared_ui.generated.resources.nav_staff
import teamflowmanager.shared_ui.generated.resources.nav_team
import teamflowmanager.shared_ui.generated.resources.nav_teams

@Composable
fun BottomNavigationBar(
    currentRoute: String?,
    isPresident: Boolean = false,
    onNavigate: (String) -> Unit,
) {
    val items: List<Route> = if (isPresident) {
        listOf(Route.TeamList, Route.ClubMembers)
    } else {
        listOf(Route.Matches, Route.Players, Route.Analysis, Route.Team)
    }

    Surface(
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        tonalElevation = 4.dp,
        shadowElevation = 8.dp,
        color = BackgroundContrast
    ) {
        NavigationBar(containerColor = Color.Transparent) {
            items.forEach { route ->
                val icon: ImageVector? = route.toIcon()
                val label: String? = route.toLabel()

                val selected = when {
                    route is Route.Players && Route.fromValue(currentRoute) is Route.Players -> true
                    route is Route.Team && Route.fromValue(currentRoute) is Route.Team -> true
                    route is Route.TeamList && Route.fromValue(currentRoute) is Route.TeamList -> true
                    route is Route.ClubMembers && Route.fromValue(currentRoute) is Route.ClubMembers -> true
                    route is Route.Analysis && Route.fromValue(currentRoute) is Route.Analysis -> true
                    route is Route.Matches && (Route.fromValue(currentRoute) is Route.Matches ||
                        Route.fromValue(currentRoute) is Route.ArchivedMatches) -> true
                    else -> false
                }

                NavigationBarItem(
                    alwaysShowLabel = false,
                    icon = {
                        if (label != null && icon != null) {
                            BottomNavItem(
                                iconVector = icon,
                                label = label,
                                isSelected = selected
                            )
                        }
                    },
                    selected = selected,
                    onClick = {
                        if (!selected) {
                            val destination = when (route) {
                                is Route.Team -> route.createRoute(Route.Team.MODE_VIEW)
                                else -> route.createRoute()
                            }
                            onNavigate(destination)
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
        }
    }
}

private fun Route.toIcon(): ImageVector? = when (this) {
    Route.Players -> Icons.Default.Group
    Route.Team -> Icons.Default.Groups
    Route.TeamList -> Icons.Default.Groups
    Route.ClubMembers -> Icons.Default.People
    Route.Matches, Route.ArchivedMatches -> Icons.Default.SportsSoccer
    Route.Analysis -> Icons.Default.BarChart
    else -> null
}

@Composable
private fun Route.toLabel(): String? = when (this) {
    Route.Players -> stringResource(Res.string.nav_players)
    Route.Team -> stringResource(Res.string.nav_team)
    Route.TeamList -> stringResource(Res.string.nav_teams)
    Route.ClubMembers -> stringResource(Res.string.nav_staff)
    Route.Matches, Route.ArchivedMatches -> stringResource(Res.string.nav_matches)
    Route.Analysis -> stringResource(Res.string.nav_analysis)
    else -> null
}
