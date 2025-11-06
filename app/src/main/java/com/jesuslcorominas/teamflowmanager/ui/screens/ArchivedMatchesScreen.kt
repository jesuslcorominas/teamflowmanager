package com.jesuslcorominas.teamflowmanager.ui.screens

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.jesuslcorominas.teamflowmanager.ui.matches.ArchivedMatchesScreen as ArchivedMatchesContent

class ArchivedMatchesScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        ArchivedMatchesContent(
            onNavigateToMatchSummary = { match ->
                navigator.push(MatchDetailScreen(match.id, match.teamName, match.opponent))
            }
        )
    }
}
