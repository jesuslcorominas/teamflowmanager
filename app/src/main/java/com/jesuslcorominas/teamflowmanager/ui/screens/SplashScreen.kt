package com.jesuslcorominas.teamflowmanager.ui.screens

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.jesuslcorominas.teamflowmanager.domain.navigation.Route
import com.jesuslcorominas.teamflowmanager.ui.splash.SplashScreen as SplashContent

class SplashScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        SplashContent(
            onNavigateToCreateTeam = {
                navigator.replaceAll(TeamScreen(Route.Team.MODE_CREATE))
            },
            onNavigateToMatches = {
                navigator.replaceAll(MainTabScreen())
            }
        )
    }
}
