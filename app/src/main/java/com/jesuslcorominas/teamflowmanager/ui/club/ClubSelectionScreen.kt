package com.jesuslcorominas.teamflowmanager.ui.club

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.analytics.ScreenName
import com.jesuslcorominas.teamflowmanager.ui.TeamFlowManagerIcon
import com.jesuslcorominas.teamflowmanager.ui.analytics.TrackScreenView
import com.jesuslcorominas.teamflowmanager.viewmodel.ClubSelectionViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun ClubSelectionScreen(
    viewModel: ClubSelectionViewModel = koinViewModel(),
    onCreateClub: () -> Unit,
    onJoinClub: () -> Unit,
    onSignedOut: () -> Unit,
) {
    TrackScreenView(screenName = ScreenName.CLUB_SELECTION, screenClass = "ClubSelectionScreen")

    val signOutComplete by viewModel.signOutComplete.collectAsState()

    LaunchedEffect(signOutComplete) {
        if (signOutComplete) {
            viewModel.clearSignOutComplete()
            onSignedOut()
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        TeamFlowManagerIcon()

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(id = R.string.club_selection_title),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(id = R.string.club_selection_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onCreateClub,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            shape = MaterialTheme.shapes.small,
        ) {
            Text(
                text = stringResource(id = R.string.create_club),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onJoinClub,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            shape = MaterialTheme.shapes.small,
        ) {
            Text(
                text = stringResource(id = R.string.join_club),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = { viewModel.signOut() }) {
            Text(
                text = stringResource(id = R.string.sign_in_with_another_account),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
