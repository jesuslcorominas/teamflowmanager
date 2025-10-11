package com.jesuslcorominas.teamflowmanager.ui.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.ui.components.TeamInfoDialog
import com.jesuslcorominas.teamflowmanager.ui.players.PlayersScreen
import com.jesuslcorominas.teamflowmanager.ui.team.TeamScreen
import com.jesuslcorominas.teamflowmanager.viewmodel.TeamUiState
import com.jesuslcorominas.teamflowmanager.viewmodel.TeamViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: TeamViewModel = koinViewModel()) {
    var teamName by remember { mutableStateOf<String?>(null) }
    var showTeamInfo by remember { mutableStateOf(false) }
    var showEditTeam by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()

    val currentTeam =
        when (val state = uiState) {
            is TeamUiState.TeamExists -> state.team
            else -> null
        }

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
                    actions = {
                        IconButton(onClick = { showTeamInfo = true }) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = stringResource(R.string.team_info_description),
                            )
                        }
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

        if (showTeamInfo && currentTeam != null) {
            TeamInfoDialog(
                team = currentTeam,
                onDismiss = { showTeamInfo = false },
                onEdit = {
                    showTeamInfo = false
                    showEditTeam = true
                },
            )
        }

        if (showEditTeam && currentTeam != null) {
            com.jesuslcorominas.teamflowmanager.ui.components.EditTeamDialog(
                team = currentTeam,
                onDismiss = { showEditTeam = false },
                onSave = { team ->
                    viewModel.updateTeam(team)
                    showEditTeam = false
                },
            )
        }
    }
}
