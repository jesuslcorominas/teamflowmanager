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
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.ui.components.bottomnav.BottomNavItem
import com.jesuslcorominas.teamflowmanager.ui.theme.BackgroundContrast
import com.jesuslcorominas.teamflowmanager.ui.theme.ContentContrast
import com.jesuslcorominas.teamflowmanager.ui.theme.Primary
import com.jesuslcorominas.teamflowmanager.ui.theme.PrimaryLight

@Composable
fun BottomNavigationBar(
    navController: NavController,
    isPresident: Boolean = false,
) {
    val items = if (isPresident) {
        listOf(
            Route.TeamList,
            Route.ClubMembers,
        )
    } else {
        listOf(
            Route.Matches,
            Route.Players,
            Route.Analysis,
            Route.Team,
        )
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Surface(
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        tonalElevation = 4.dp,
        shadowElevation = 8.dp,
        color = BackgroundContrast
    ) {
        NavigationBar(containerColor = Color.Transparent) {
            items.forEach { route ->
                val icon: ImageVector? = route.toIcon()
                val labelRes: Int? = route.toStringRes()

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
                        if (labelRes != null && icon != null) {
                            BottomNavItem(
                                iconVector = icon,
                                labelResId = labelRes,
                                isSelected = selected
                            )
                        }
                    },
//                    icon = {
//                        icon?.let {
//                            Icon(
//                                imageVector = icon,
//                                contentDescription = labelRes?.let { stringResource(it) },
//                            )
//                        }
//                    },
//                    label = {
//                        labelRes?.let { labelRes ->
//                            Text(text = stringResource(labelRes))
//                        }
//                    },
                    selected = selected,
                    onClick = {
                        if (!selected) {
                            val destination = when (route) {
                                is Route.Team -> route.createRoute(Route.Team.MODE_VIEW)
                                else -> route.createRoute()
                            }

                            navController.navigate(destination) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                // on the back stack as users select items
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
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
        }
    }
}

private fun Route.toIcon(): ImageVector? =
    when (this) {
        Route.Players -> Icons.Default.Group
        Route.Team -> Icons.Default.Groups
        Route.TeamList -> Icons.Default.Groups
        Route.ClubMembers -> Icons.Default.People
        Route.Matches, Route.ArchivedMatches -> Icons.Default.SportsSoccer
        Route.Analysis -> Icons.Default.BarChart
        else -> null
    }

private fun Route.toStringRes(): Int? = when (this) {
    Route.Players -> R.string.nav_players
    Route.Team -> R.string.nav_team
    Route.TeamList -> R.string.nav_teams
    Route.ClubMembers -> R.string.nav_staff
    Route.Matches, Route.ArchivedMatches -> R.string.nav_matches
    Route.Analysis -> R.string.nav_analysis
    else -> null
}
