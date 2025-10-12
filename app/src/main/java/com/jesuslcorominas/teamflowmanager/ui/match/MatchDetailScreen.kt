package com.jesuslcorominas.teamflowmanager.ui.match

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.viewmodel.match.MatchDetailViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun MatchDetailScreen(
    matchId: String,
    viewModel: MatchDetailViewModel = koinViewModel()
) {
    val match by viewModel.match.observeAsState()
    val error by viewModel.error.observeAsState()
    val isLoading by viewModel.isLoading.observeAsState(false)

    LaunchedEffect(matchId) {
        viewModel.loadMatch(matchId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Match Detail",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        error?.let { errorMessage ->
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(8.dp)
            )
        }

        match?.let { currentMatch ->
            MatchInfoCard(currentMatch)

            Spacer(modifier = Modifier.height(16.dp))

            MatchControls(
                match = currentMatch,
                onPause = { viewModel.pauseMatch(matchId) },
                onResume = { viewModel.resumeMatch(matchId) }
            )
        }
    }
}

@Composable
fun MatchInfoCard(match: Match) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Opponent: ${match.opponent}",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Status: ${match.status.name.replace('_', ' ')}",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "First Half: ${formatDuration(match.firstHalfDuration)}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Second Half: ${formatDuration(match.secondHalfDuration)}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun MatchControls(
    match: Match,
    onPause: () -> Unit,
    onResume: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (match.status) {
            MatchStatus.IN_PROGRESS -> {
                Button(
                    onClick = onPause,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Descanso")
                }
            }
            MatchStatus.PAUSED -> {
                Button(
                    onClick = onResume,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Resume")
                }
            }
            else -> {
                // No controls for NOT_STARTED or FINISHED
            }
        }
    }
}

fun formatDuration(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    return String.format("%02d:%02d", minutes, seconds)
}
