package com.jesuslcorominas.teamflowmanager.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.jesuslcorominas.teamflowmanager.R

@Composable
fun BottomNavigationBar(
    navController: NavController,
) {
    val items = listOf(
        Route.Players,
        Route.TeamDetail,
        Route.Matches,
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        items.forEach { route ->
            val icon: ImageVector? = route.toIcon()
            val labelRes: Int? = route.toStringRes()

            val selected = currentRoute == route.path
            NavigationBarItem(
                icon = {
                    icon?.let {
                        Icon(
                            imageVector = icon,
                            contentDescription = labelRes?.let { stringResource(it) },
                        )
                    }
                },
                label = {
                    labelRes?.let { labelRes ->
                        Text(text = stringResource(labelRes))
                    }
                },
                selected = selected,
                onClick = {
                    if (!selected) {
                        navController.navigate(route.createRoute()) {
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
            )
        }
    }
}

private fun Route.toIcon(): ImageVector? =
    when (this) {
        Route.Players -> Icons.Default.Group
        Route.TeamDetail -> Icons.Default.Groups
        Route.Matches -> Icons.Default.SportsSoccer
        else -> null
    }

private fun Route.toStringRes(): Int? = when (this) {
    Route.Players -> R.string.nav_players
    Route.TeamDetail -> R.string.nav_team
    Route.Matches -> R.string.nav_matches
    else -> null
}
