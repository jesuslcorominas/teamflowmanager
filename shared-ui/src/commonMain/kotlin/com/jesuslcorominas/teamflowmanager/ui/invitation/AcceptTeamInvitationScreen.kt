package com.jesuslcorominas.teamflowmanager.ui.invitation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.jesuslcorominas.teamflowmanager.viewmodel.AcceptTeamInvitationState
import com.jesuslcorominas.teamflowmanager.viewmodel.AcceptTeamInvitationViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import teamflowmanager.shared_ui.generated.resources.Res
import teamflowmanager.shared_ui.generated.resources.accepting_team_invitation
import teamflowmanager.shared_ui.generated.resources.go_to_team
import teamflowmanager.shared_ui.generated.resources.go_to_teams
import teamflowmanager.shared_ui.generated.resources.redirecting_to_login
import teamflowmanager.shared_ui.generated.resources.retry
import teamflowmanager.shared_ui.generated.resources.team_invitation_accepted
import teamflowmanager.shared_ui.generated.resources.team_invitation_error
import teamflowmanager.shared_ui.generated.resources.team_invitation_success_message

@Composable
fun AcceptTeamInvitationScreen(
    teamId: String?,
    onNavigateToLogin: (String) -> Unit,
    onNavigateToTeam: () -> Unit,
    onNavigateToTeams: () -> Unit,
    viewModel: AcceptTeamInvitationViewModel =
        koinViewModel(key = teamId.orEmpty(), parameters = { parametersOf(teamId) }),
) {
    TrackScreenView(screenName = ScreenName.ACCEPT_TEAM_INVITATION, screenClass = "AcceptTeamInvitationScreen")

    val state by viewModel.state.collectAsState()

    LaunchedEffect(state) {
        if (state is AcceptTeamInvitationState.NotAuthenticated) {
            val id = (state as AcceptTeamInvitationState.NotAuthenticated).teamId
            onNavigateToLogin(id)
        }
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        when (val currentState = state) {
            is AcceptTeamInvitationState.Loading -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = stringResource(Res.string.accepting_team_invitation),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            is AcceptTeamInvitationState.Success -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = stringResource(Res.string.team_invitation_accepted),
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = stringResource(Res.string.team_invitation_success_message, currentState.team.name),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                    )
                    Button(
                        onClick = onNavigateToTeam,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(Res.string.go_to_team))
                    }
                }
            }

            is AcceptTeamInvitationState.Error -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error,
                    )
                    Text(
                        text = stringResource(Res.string.team_invitation_error),
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = currentState.message,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        OutlinedButton(
                            onClick = onNavigateToTeams,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(stringResource(Res.string.go_to_teams))
                        }
                        Button(
                            onClick = { viewModel.retry() },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(stringResource(Res.string.retry))
                        }
                    }
                }
            }

            is AcceptTeamInvitationState.NotAuthenticated -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = stringResource(Res.string.redirecting_to_login),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
