package com.jesuslcorominas.teamflowmanager.ui.screens

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import com.jesuslcorominas.teamflowmanager.ui.matches.MatchScreen as MatchContent

data class MatchDetailScreen(
    val matchId: Long,
    val teamName: String,
    val opponent: String
) : Screen {
    @Composable
    override fun Content() {
        MatchContent()
    }
}
