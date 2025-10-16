package com.jesuslcorominas.teamflowmanager.ui.matches

import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.jesuslcorominas.teamflowmanager.viewmodel.PlayerSortOrder
import com.jesuslcorominas.teamflowmanager.viewmodel.SubstitutionItem
import org.koin.androidx.compose.koinViewModel

@Composable
fun CurrentMatchScreen(viewModel: MatchViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedPlayerOut by viewModel.selectedPlayerOut.collectAsState()
    val showInvalidSubstitutionAlert by viewModel.showInvalidSubstitutionAlert.collectAsState()
    val showStopConfirmation by viewModel.showStopConfirmation.collectAsState()
    val currentSortOrder by viewModel.currentSortOrder.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        when (val state = uiState) {
            is MatchUiState.Loading -> LoadingState()
            is MatchUiState.NoMatch -> NoMatchState()
            is MatchUiState.Success -> SuccessState(
                state = state,
                selectedPlayerOut = selectedPlayerOut,
                currentSortOrder = currentSortOrder,
                onSaveMatch = { viewModel.saveMatch() },
                onPauseMatch = { viewModel.pauseMatch() },
                onResumeMatch = { viewModel.resumeMatch() },
                onPlayerClick = { playerId ->
                    if (selectedPlayerOut == null) {
                        viewModel.selectPlayerOut(playerId)
                    } else if (selectedPlayerOut == playerId) {
                        viewModel.clearPlayerOutSelection()
                    } else {
                        viewModel.substitutePlayer(playerId)
                    }
                },
                onSortOrderChange = { viewModel.setSortOrder(it) },
            )
            is MatchUiState.Finished -> FinishedMatchState(state = state)
        }

        // Show alert if trying to select an inactive player
        if (showInvalidSubstitutionAlert) {
            InvalidSubstitutionAlertDialog(
                onDismiss = { dontShowAgain ->
                    viewModel.dismissInvalidSubstitutionAlert(dontShowAgain)
                }
            )
        }

        // Show confirmation dialog if stopping match early
        if (showStopConfirmation) {
            StopMatchEarlyConfirmationDialog(
                onConfirm = { viewModel.confirmStopMatch() },
                onDismiss = { viewModel.dismissStopConfirmation() }
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
    selectedPlayerOut: Long?,
    currentSortOrder: PlayerSortOrder,
    onSaveMatch: () -> Unit,
    onPauseMatch: () -> Unit,
    onResumeMatch: () -> Unit,
    onPlayerClick: (Long) -> Unit,
    onSortOrderChange: (PlayerSortOrder) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(TFMSpacing.spacing04),
    ) {
        if (!state.isMatchStarted) {
            // Pre-match state - show begin match button
            PreMatchView(
                state = state,
            )
        } else {
            // Match is ongoing
            OngoingMatchView(
                state = state,
                selectedPlayerOut = selectedPlayerOut,
                currentSortOrder = currentSortOrder,
                onSaveMatch = onSaveMatch,
                onPauseMatch = onPauseMatch,
                onResumeMatch = onResumeMatch,
                onPlayerClick = onPlayerClick,
                onSortOrderChange = onSortOrderChange,
            )
        }
    }
}

@Composable
private fun PreMatchView(
    state: MatchUiState.Success,
) {
    val viewModel: MatchViewModel = koinViewModel()
    
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(TFMSpacing.spacing04),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent,
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(TFMSpacing.spacing04),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.current_match_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(
                        R.string.period_label,
                        state.currentPeriod,
                        state.numberOfPeriods
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        Text(
            text = stringResource(R.string.player_times_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(TFMSpacing.spacing02),
        ) {
            items(state.playerTimes) { playerTimeItem ->
                PlayerTimeCard(
                    playerTimeItem = playerTimeItem,
                    isSelected = false,
                    onClick = { },
                )
            }
        }

        Button(
            onClick = { viewModel.beginMatch() },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
            )
            Spacer(modifier = Modifier.width(TFMSpacing.spacing02))
            Text(text = stringResource(R.string.begin_match))
        }
    }
}

@Composable
private fun OngoingMatchView(
    state: MatchUiState.Success,
    selectedPlayerOut: Long?,
    currentSortOrder: PlayerSortOrder,
    onSaveMatch: () -> Unit,
    onPauseMatch: () -> Unit,
    onResumeMatch: () -> Unit,
    onPlayerClick: (Long) -> Unit,
    onSortOrderChange: (PlayerSortOrder) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        MatchTimeCard(
            timeMillis = state.matchTimeMillis,
            isRunning = state.matchIsRunning,
            numberOfPeriods = state.numberOfPeriods,
            currentPeriod = state.currentPeriod,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = TFMSpacing.spacing03, bottom = TFMSpacing.spacing02),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.player_times_title),
                style = MaterialTheme.typography.titleMedium,
            )
            
            // Sort order dropdown
            SortOrderSelector(
                currentSortOrder = currentSortOrder,
                onSortOrderChange = onSortOrderChange,
            )
        }

        if (selectedPlayerOut != null) {
            Text(
                text = stringResource(R.string.select_player_in_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = TFMSpacing.spacing02),
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(TFMSpacing.spacing02),
        ) {
            items(state.playerTimes) { playerTimeItem ->
                PlayerTimeCard(
                    playerTimeItem = playerTimeItem,
                    isSelected = selectedPlayerOut == playerTimeItem.player.id,
                    onClick = { onPlayerClick(playerTimeItem.player.id) },
                )
            }
        }

        Spacer(modifier = Modifier.padding(TFMSpacing.spacing02))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(TFMSpacing.spacing03, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Play/Pause button
            IconButton(
                onClick = if (state.matchIsRunning) onPauseMatch else onResumeMatch,
                modifier = Modifier.size(64.dp),
                enabled = if (state.matchIsRunning) state.canPause else true,
            ) {
                Icon(
                    imageVector = if (state.matchIsRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (state.matchIsRunning) {
                        stringResource(R.string.pause_match_button)
                    } else {
                        stringResource(R.string.resume_match_button)
                    },
                    modifier = Modifier.size(48.dp),
                    tint = if (state.canPause || !state.matchIsRunning) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    },
                )
            }

            // Stop/Finish button
            IconButton(
                onClick = onSaveMatch,
                modifier = Modifier.size(64.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Stop,
                    contentDescription = stringResource(R.string.finish_match_button),
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun MatchTimeCard(
    timeMillis: Long,
    isRunning: Boolean,
    numberOfPeriods: Int,
    currentPeriod: Int,
) {
    // Calculate period duration
    val periodDurationMillis = if (numberOfPeriods == 2) {
        25 * 60 * 1000L // 25 minutes
    } else {
        (12 * 60 + 30) * 1000L // 12 minutes 30 seconds
    }
    
    // Use elapsed time directly for current period (not accumulating across periods)
    // Calculate remaining time (can be negative for stoppage time)
    val remainingTime = periodDurationMillis - timeMillis
    val isStoppageTime = remainingTime < 0
    val displayTime = if (isStoppageTime) -remainingTime else remainingTime
    
    // Determine period name
    val periodName = when {
        numberOfPeriods == 2 && currentPeriod == 1 -> stringResource(R.string.first_half)
        numberOfPeriods == 2 && currentPeriod == 2 -> stringResource(R.string.second_half)
        numberOfPeriods == 4 && currentPeriod == 1 -> stringResource(R.string.first_quarter)
        numberOfPeriods == 4 && currentPeriod == 2 -> stringResource(R.string.second_quarter)
        numberOfPeriods == 4 && currentPeriod == 3 -> stringResource(R.string.third_quarter)
        numberOfPeriods == 4 && currentPeriod == 4 -> stringResource(R.string.fourth_quarter)
        else -> stringResource(R.string.period_label, currentPeriod, numberOfPeriods)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 2.dp,
            color = MaterialTheme.colorScheme.primary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(TFMSpacing.spacing04),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = periodName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                if (isStoppageTime) {
                    Text(
                        text = formatTime(periodDurationMillis),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = " + ",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                    Text(
                        text = formatTime(displayTime),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error,
                    )
                } else {
                    Text(
                        text = formatTime(displayTime),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerTimeCard(
    playerTimeItem: PlayerTimeItem,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> MaterialTheme.colorScheme.primaryContainer
                playerTimeItem.isRunning -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surface
            },
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(TFMSpacing.spacing04),
            horizontalArrangement = Arrangement.spacedBy(TFMSpacing.spacing03),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Jersey number with shirt style
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(com.jesuslcorominas.teamflowmanager.ui.theme.BackgroundContrast)
                    .size(56.dp),
                contentAlignment = Alignment.TopCenter,
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        modifier = Modifier.padding(top = TFMSpacing.spacing02),
                        text = playerTimeItem.player.number.toString(),
                        fontFamily = com.jesuslcorominas.teamflowmanager.ui.theme.BebasNeueFontFamily,
                        color = com.jesuslcorominas.teamflowmanager.ui.theme.ContentContrast,
                        style = MaterialTheme.typography.headlineLarge,
                    )
                }

                Column {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(TFMSpacing.spacing02)
                            .background(com.jesuslcorominas.teamflowmanager.ui.theme.ShirtOrange)
                    )
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(TFMSpacing.spacing01)
                            .background(com.jesuslcorominas.teamflowmanager.ui.theme.White)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(TFMSpacing.spacing01),
                ) {
                    Text(
                        text = "${playerTimeItem.player.firstName} ${playerTimeItem.player.lastName}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                    )
                    
                    // Captain badge
                    if (playerTimeItem.isCaptain) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = stringResource(R.string.captain_badge),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
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
                matchId = 1,
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
            selectedPlayerOut = null,
            onSaveMatch = {},
            onPauseMatch = {},
            onResumeMatch = {},
            onPlayerClick = {},
        )
    }
}

@Composable
private fun FinishedMatchState(state: MatchUiState.Finished) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(TFMSpacing.spacing04),
        verticalArrangement = Arrangement.spacedBy(TFMSpacing.spacing03),
    ) {
        // Match header
        item {
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
                        text = stringResource(R.string.match_finished),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Spacer(modifier = Modifier.padding(TFMSpacing.spacing01))
                    Text(
                        text = state.opponent,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        text = state.location,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Spacer(modifier = Modifier.padding(TFMSpacing.spacing02))
                    Text(
                        text = stringResource(R.string.total_time_label),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        text = formatTime(state.matchTimeMillis),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }

        // Player times section
        item {
            Text(
                text = stringResource(R.string.player_times_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        items(state.playerTimes) { playerTimeItem ->
            PlayerTimeCard(
                playerTimeItem = playerTimeItem,
                isSelected = false,
                onClick = { /* Read-only, no click action */ },
            )
        }

        // Substitutions section
        if (state.substitutions.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.padding(TFMSpacing.spacing02))
                Text(
                    text = stringResource(R.string.substitutions_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            items(state.substitutions) { substitution ->
                SubstitutionCard(substitution = substitution)
            }
        }
    }
}

@Composable
private fun SubstitutionCard(substitution: SubstitutionItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(TFMSpacing.spacing04),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Time
            Text(
                text = formatTime(substitution.matchElapsedTimeMillis),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )

            Spacer(modifier = Modifier.width(TFMSpacing.spacing03))

            // Player Out
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowDownward,
                    contentDescription = stringResource(R.string.player_out),
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.width(TFMSpacing.spacing02))
                Column {
                    Text(
                        text = "${substitution.playerOut.firstName} ${substitution.playerOut.lastName}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = stringResource(
                            R.string.player_number_format,
                            substitution.playerOut.number
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.width(TFMSpacing.spacing02))

            // Player In
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowUpward,
                    contentDescription = stringResource(R.string.player_in),
                    tint = Color(0xFF4CAF50), // Green color
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.width(TFMSpacing.spacing02))
                Column {
                    Text(
                        text = "${substitution.playerIn.firstName} ${substitution.playerIn.lastName}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = stringResource(
                            R.string.player_number_format,
                            substitution.playerIn.number
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun InvalidSubstitutionAlertDialog(
    onDismiss: (dontShowAgain: Boolean) -> Unit,
) {
    var dontShowAgain by remember { mutableStateOf(false) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = { onDismiss(false) },
        title = {
            Text(
                stringResource(R.string.invalid_substitution_title),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column {
                Text(
                    stringResource(R.string.invalid_substitution_message),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.padding(TFMSpacing.spacing02))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = TFMSpacing.spacing02)
                ) {
                    androidx.compose.material3.Checkbox(
                        checked = dontShowAgain,
                        onCheckedChange = { dontShowAgain = it }
                    )
                    Spacer(modifier = Modifier.padding(TFMSpacing.spacing01))
                    Text(
                        stringResource(R.string.dont_show_again),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(
                onClick = { onDismiss(dontShowAgain) }
            ) {
                Text(stringResource(R.string.close))
            }
        },
        shape = MaterialTheme.shapes.medium,
    )
}

@Composable
private fun StopMatchEarlyConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(R.string.stop_match_early_title),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Text(
                stringResource(R.string.stop_match_early_message),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            androidx.compose.material3.TextButton(
                onClick = onConfirm
            ) {
                Text(stringResource(R.string.yes))
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(
                onClick = onDismiss
            ) {
                Text(stringResource(R.string.no))
            }
        },
        shape = MaterialTheme.shapes.medium,
    )
}

@Composable
private fun SortOrderSelector(
    currentSortOrder: PlayerSortOrder,
    onSortOrderChange: (PlayerSortOrder) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        androidx.compose.material3.TextButton(
            onClick = { expanded = true }
        ) {
            Text(
                text = when (currentSortOrder) {
                    PlayerSortOrder.BY_TIME_DESC -> stringResource(R.string.sort_by_time_desc)
                    PlayerSortOrder.BY_TIME_ASC -> stringResource(R.string.sort_by_time_asc)
                    PlayerSortOrder.BY_ACTIVE_FIRST -> stringResource(R.string.sort_by_active)
                },
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        androidx.compose.material3.DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            androidx.compose.material3.DropdownMenuItem(
                text = { Text(stringResource(R.string.sort_by_active)) },
                onClick = {
                    onSortOrderChange(PlayerSortOrder.BY_ACTIVE_FIRST)
                    expanded = false
                }
            )
            androidx.compose.material3.DropdownMenuItem(
                text = { Text(stringResource(R.string.sort_by_time_desc)) },
                onClick = {
                    onSortOrderChange(PlayerSortOrder.BY_TIME_DESC)
                    expanded = false
                }
            )
            androidx.compose.material3.DropdownMenuItem(
                text = { Text(stringResource(R.string.sort_by_time_asc)) },
                onClick = {
                    onSortOrderChange(PlayerSortOrder.BY_TIME_ASC)
                    expanded = false
                }
            )
        }
    }
}

