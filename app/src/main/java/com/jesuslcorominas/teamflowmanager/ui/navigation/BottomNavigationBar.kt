package com.jesuslcorominas.teamflowmanager.ui.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

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
            val selected = currentRoute == route.path
            NavigationBarItem(
                icon = {
                    route.icon?.let { iconRes ->
                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = route.label?.let { stringResource(it) },
                        )
                    }
                },
                label = {
                    route.label?.let { labelRes ->
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
