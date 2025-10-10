package com.jesuslcorominas.teamflowmanager.ui.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.jesuslcorominas.teamflowmanager.ui.players.PlayersScreen
import com.jesuslcorominas.teamflowmanager.ui.team.TeamScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    var teamName by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            if (teamName != null) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = teamName ?: "",
                            style = MaterialTheme.typography.titleLarge,
                        )
                    },
                )
            }
        },
    ) { paddingValues ->
        Surface(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            color = MaterialTheme.colorScheme.background,
        ) {
            if (teamName == null) {
                TeamScreen(
                    onNavigateToPlayers = { name ->
                        teamName = name
                    },
                )
            } else {
                PlayersScreen()
            }
        }
    }
}
