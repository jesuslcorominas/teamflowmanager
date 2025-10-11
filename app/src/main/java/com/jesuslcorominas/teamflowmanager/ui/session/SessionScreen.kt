package com.jesuslcorominas.teamflowmanager.ui.session

import TFMSpacing
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.ui.util.formatTime
import com.jesuslcorominas.teamflowmanager.viewmodel.MatchUiState
import com.jesuslcorominas.teamflowmanager.viewmodel.MatchViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.PlayerTimeItem
import org.koin.androidx.compose.koinViewModel

@Composable
fun SessionScreen(viewModel: MatchViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        when (val state = uiState) {
            is MatchUiState.Loading -> LoadingState()
            is MatchUiState.NoMatch -> NoMatchState()
            is MatchUiState.Success -> SuccessState(
                state = state,
                onSaveSession = { viewModel.saveSession() },
            )
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun NoMatchState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.no_match_message),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun SuccessState(
    state: MatchUiState.Success,
    onSaveSession: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(TFMSpacing.spacing04),
    ) {
        MatchTimeCard(
            timeMillis = state.matchTimeMillis,
            isRunning = state.matchIsRunning,
        )

        Text(
            text = stringResource(R.string.player_times_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(
                top = TFMSpacing.spacing05,
                bottom = TFMSpacing.spacing03,
            ),
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(TFMSpacing.spacing02),
        ) {
            items(state.playerTimes) { playerTimeItem ->
                PlayerTimeCard(playerTimeItem = playerTimeItem)
            }
        }

        Spacer(modifier = Modifier.padding(TFMSpacing.spacing02))

        Button(
            onClick = onSaveSession,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.save_session_button))
        }
    }
}

@Composable
private fun MatchTimeCard(
    timeMillis: Long,
    isRunning: Boolean,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(TFMSpacing.spacing04),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.match_time_label),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(TFMSpacing.spacing02),
            ) {
                Text(
                    text = formatTime(timeMillis),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                if (isRunning) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.error,
                                shape = MaterialTheme.shapes.small,
                            )
                            .padding(horizontal = TFMSpacing.spacing02, vertical = TFMSpacing.spacing01),
                    ) {
                        Text(
                            text = stringResource(R.string.running_indicator),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onError,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerTimeCard(playerTimeItem: PlayerTimeItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (playerTimeItem.isRunning) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(TFMSpacing.spacing04),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${playerTimeItem.player.firstName} ${playerTimeItem.player.lastName}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = stringResource(R.string.player_number_format, playerTimeItem.player.number),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(TFMSpacing.spacing02),
            ) {
                Text(
                    text = formatTime(playerTimeItem.timeMillis),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                if (playerTimeItem.isRunning) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.error,
                                shape = MaterialTheme.shapes.small,
                            )
                            .padding(horizontal = TFMSpacing.spacing02, vertical = TFMSpacing.spacing01),
                    ) {
                        Text(
                            text = stringResource(R.string.running_indicator),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onError,
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SuccessStatePreview() {
    MaterialTheme {
        SuccessState(
            state = MatchUiState.Success(
                matchTimeMillis = 900000L,
                matchIsRunning = true,
                playerTimes = listOf(
                    PlayerTimeItem(
                        player = Player(
                            id = 1,
                            firstName = "John",
                            lastName = "Doe",
                            number = 10,
                            positions = listOf(Position.Forward),
                            teamId = 1,
                        ),
                        timeMillis = 450000L,
                        isRunning = true,
                    ),
                    PlayerTimeItem(
                        player = Player(
                            id = 2,
                            firstName = "Jane",
                            lastName = "Smith",
                            number = 5,
                            positions = listOf(Position.Defender),
                            teamId = 1,
                        ),
                        timeMillis = 300000L,
                        isRunning = false,
                    ),
                ),
            ),
            onSaveSession = {},
        )
    }
}
