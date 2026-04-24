package com.jesuslcorominas.teamflowmanager.ui.club

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.domain.analytics.ScreenName
import com.jesuslcorominas.teamflowmanager.ui.analytics.TrackScreenView
import com.jesuslcorominas.teamflowmanager.viewmodel.PendingTeamAssignmentViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import teamflowmanager.shared_ui.generated.resources.Res
import teamflowmanager.shared_ui.generated.resources.pending_team_message
import teamflowmanager.shared_ui.generated.resources.pending_team_sign_out
import teamflowmanager.shared_ui.generated.resources.pending_team_title

@Composable
fun PendingTeamAssignmentScreen(
    onTeamAssigned: () -> Unit,
    onSignOut: () -> Unit,
    viewModel: PendingTeamAssignmentViewModel = koinViewModel(),
) {
    TrackScreenView(screenName = ScreenName.PENDING_TEAM_ASSIGNMENT, screenClass = "PendingTeamAssignmentScreen")

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        when (uiState) {
            PendingTeamAssignmentViewModel.UiState.TeamAssigned -> onTeamAssigned()
            PendingTeamAssignmentViewModel.UiState.SignedOut -> onSignOut()
            PendingTeamAssignmentViewModel.UiState.Waiting -> Unit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(Res.string.pending_team_title),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(Res.string.pending_team_message),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = viewModel::signOut) {
            Text(stringResource(Res.string.pending_team_sign_out))
        }
    }
}
