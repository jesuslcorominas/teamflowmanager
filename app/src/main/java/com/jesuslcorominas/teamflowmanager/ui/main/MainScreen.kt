package com.jesuslcorominas.teamflowmanager.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.ui.navigation.BottomNavigationBar
import com.jesuslcorominas.teamflowmanager.ui.navigation.Navigation
import com.jesuslcorominas.teamflowmanager.ui.navigation.Route
import com.jesuslcorominas.teamflowmanager.viewmodel.TeamUiState
import com.jesuslcorominas.teamflowmanager.viewmodel.TeamViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: TeamViewModel = koinViewModel()) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val uiState by viewModel.uiState.collectAsState()

    val route = Route.fromValue(currentRoute)
    val uiConfig = route?.uiConfig(null)

    val teamName = when (val state = uiState) {
        is TeamUiState.TeamExists -> state.team.name
        else -> null
    }

    // Handle back button for CreateTeam screen - should exit app
    if (currentRoute == Route.CreateTeam.createRoute()) {
        BackHandler {
            // Close the app by doing nothing - the system will handle it
        }
    }

    Scaffold(
        topBar = {
            if (uiConfig?.showTopBar == true && teamName != null) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = teamName,
                            style = MaterialTheme.typography.titleLarge,
                        )
                    },
                )
            }
        },
        bottomBar = {
            if (uiConfig?.showBottomBar == true) {
                BottomNavigationBar(navController = navController)
            }
        },
    ) { paddingValues ->
        Navigation(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            navController = navController,
        )
    }
}
