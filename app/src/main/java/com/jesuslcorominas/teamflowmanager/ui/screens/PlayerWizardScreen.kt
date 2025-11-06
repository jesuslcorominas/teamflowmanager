package com.jesuslcorominas.teamflowmanager.ui.screens

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.jesuslcorominas.teamflowmanager.ui.players.wizard.PlayerWizardScreen as PlayerWizardContent

data class PlayerWizardScreen(val playerId: Long) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        PlayerWizardContent(
            onNavigateBack = {
                navigator.pop()
            }
        )
    }
}
