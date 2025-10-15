package com.jesuslcorominas.teamflowmanager.ui.matches

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.ui.theme.BackgroundContrast
import com.jesuslcorominas.teamflowmanager.ui.theme.BebasNeueFontFamily
import com.jesuslcorominas.teamflowmanager.ui.theme.ContentContrast
import com.jesuslcorominas.teamflowmanager.ui.theme.ShirtOrange
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import com.jesuslcorominas.teamflowmanager.ui.theme.White
import com.jesuslcorominas.teamflowmanager.ui.util.formatTime
import com.jesuslcorominas.teamflowmanager.viewmodel.MatchSummaryUiState
import com.jesuslcorominas.teamflowmanager.viewmodel.MatchSummaryViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.PlayerTimeItem
import com.jesuslcorominas.teamflowmanager.viewmodel.SubstitutionItem
import org.koin.androidx.compose.koinViewModel

@Composable
fun MatchSummaryScreen(
    matchId: Long,
    onNavigateBack: () -> Unit,
    viewModel: MatchSummaryViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(matchId) {
        viewModel.loadMatchSummary(matchId)
    }

    when (val state = uiState) {
        is MatchSummaryUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        is MatchSummaryUiState.NotFound -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.match_not_found),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        is MatchSummaryUiState.Success -> {
            MatchSummaryContent(state = state)
        }
    }
}

@Composable
private fun MatchSummaryContent(
    state: MatchSummaryUiState.Success,
    modifier: Modifier = Modifier,
) {
    val halfTimeMillis = 25 * 60 * 1000L // 25 minutes in milliseconds
    val firstHalfMillis = minOf(state.matchTimeMillis, halfTimeMillis)
    val secondHalfMillis = maxOf(0L, state.matchTimeMillis - halfTimeMillis)
    val firstHalfStoppage = maxOf(0L, firstHalfMillis - halfTimeMillis)
    val secondHalfStoppage = maxOf(0L, secondHalfMillis - halfTimeMillis)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(TFMSpacing.spacing04),
        contentPadding = PaddingValues(bottom = TFMSpacing.spacing04),
        verticalArrangement = Arrangement.spacedBy(TFMSpacing.spacing03),
    ) {
        // Match header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
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
                    )
                    Spacer(modifier = Modifier.padding(TFMSpacing.spacing01))
                    Text(
                        text = state.opponent,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = state.location,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(modifier = Modifier.padding(TFMSpacing.spacing02))
                    
                    // Total time
                    Text(
                        text = stringResource(R.string.total_time_label),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = formatTime(state.matchTimeMillis),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    
                    Spacer(modifier = Modifier.padding(TFMSpacing.spacing02))
                    
                    // Half times
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(R.string.first_half_label),
                                style = MaterialTheme.typography.bodySmall,
                            )
                            Text(
                                text = if (firstHalfStoppage > 0) {
                                    stringResource(
                                        R.string.stoppage_time_format,
                                        formatTime(halfTimeMillis),
                                        formatTime(firstHalfStoppage)
                                    )
                                } else {
                                    formatTime(firstHalfMillis)
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        if (secondHalfMillis > 0) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = stringResource(R.string.second_half_label),
                                    style = MaterialTheme.typography.bodySmall,
                                )
                                Text(
                                    text = if (secondHalfStoppage > 0) {
                                        stringResource(
                                            R.string.stoppage_time_format,
                                            formatTime(halfTimeMillis),
                                            formatTime(secondHalfStoppage)
                                        )
                                    } else {
                                        formatTime(secondHalfMillis)
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
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
private fun JerseyBadge(
    number: Int,
    modifier: Modifier = Modifier,
    size: Int = 56,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(BackgroundContrast)
            .size(size.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                modifier = Modifier.padding(top = TFMSpacing.spacing02),
                text = number.toString(),
                fontFamily = BebasNeueFontFamily,
                color = ContentContrast,
                style = if (size >= 56) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.titleLarge,
            )
        }

        Column {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(TFMSpacing.spacing02)
                    .background(ShirtOrange)
            )
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(TFMSpacing.spacing01)
                    .background(White)
            )
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
            JerseyBadge(number = playerTimeItem.player.number)
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${playerTimeItem.player.firstName} ${playerTimeItem.player.lastName}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
                if (playerTimeItem.substitutionCount > 0) {
                    Text(
                        text = stringResource(R.string.substitution_count, playerTimeItem.substitutionCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text(
                text = formatTime(playerTimeItem.timeMillis),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun SubstitutionCard(substitution: SubstitutionItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(TFMSpacing.spacing04),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Players and arrows row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Player In (left side)
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            JerseyBadge(
                                number = substitution.playerIn.number,
                                size = 64,
                            )
                        }
                        
                        // Name above badge
                        Text(
                            text = "${substitution.playerIn.firstName} ${substitution.playerIn.lastName}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .fillMaxWidth(0.5f),
                        )
                    }

                    // Arrows in center
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = TFMSpacing.spacing02),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = stringResource(R.string.player_in),
                            tint = Color(0xFF4CAF50), // Green color
                            modifier = Modifier.size(32.dp),
                        )
                        Spacer(modifier = Modifier.height(TFMSpacing.spacing01))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.player_out),
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(32.dp),
                        )
                    }

                    // Player Out (right side)
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            JerseyBadge(
                                number = substitution.playerOut.number,
                                size = 64,
                            )
                        }
                        
                        // Name above badge
                        Text(
                            text = "${substitution.playerOut.firstName} ${substitution.playerOut.lastName}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .fillMaxWidth(0.5f),
                        )
                    }
                }

                // Time below centered
                Spacer(modifier = Modifier.height(TFMSpacing.spacing03))
                Text(
                    text = formatTime(substitution.matchElapsedTimeMillis),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
