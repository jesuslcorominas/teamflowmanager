package com.jesuslcorominas.teamflowmanager.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.jesuslcorominas.teamflowmanager.ui.matches.wizard.MatchCreationWizardScreen as MatchCreationWizardContent
import com.jesuslcorominas.teamflowmanager.ui.navigation.BackHandlerController

data class MatchCreationWizardScreen(val matchId: Long) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val backHandlerController = remember { BackHandlerController() }

        MatchCreationWizardContent(
            onNavigateBack = {
                navigator.pop()
            },
            currentBackHandler = backHandlerController
        )
    }
}
