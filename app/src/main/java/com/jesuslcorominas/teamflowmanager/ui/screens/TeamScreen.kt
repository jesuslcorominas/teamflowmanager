package com.jesuslcorominas.teamflowmanager.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.jesuslcorominas.teamflowmanager.domain.navigation.Route
import com.jesuslcorominas.teamflowmanager.ui.navigation.BackHandlerController
import com.jesuslcorominas.teamflowmanager.ui.team.TeamScreen as TeamContent

data class TeamScreen(val mode: String) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val backHandlerController = remember { BackHandlerController() }

        TeamContent(
            onNavigateToMatches = { _ ->
                navigator.replaceAll(MainTabScreen())
            },
            onNavigateBackRequest = {
                navigator.pop()
            },
            currentBackHandler = if (mode == Route.Team.MODE_EDIT) backHandlerController else null
        )
    }
}
