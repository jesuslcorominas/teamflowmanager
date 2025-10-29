package com.jesuslcorominas.teamflowmanager.ui.analysis

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeStats
import com.jesuslcorominas.teamflowmanager.ui.components.EmptyContent
import com.jesuslcorominas.teamflowmanager.ui.components.Loading
import com.jesuslcorominas.teamflowmanager.ui.theme.Primary
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import com.jesuslcorominas.teamflowmanager.viewmodel.AnalysisUiState
import com.jesuslcorominas.teamflowmanager.viewmodel.AnalysisViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AnalysisScreen(
    viewModel: AnalysisViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is AnalysisUiState.Loading -> Loading()
            is AnalysisUiState.Empty -> EmptyContent(stringResource(R.string.analysis_no_data))
            is AnalysisUiState.Success -> AnalysisContent(playerStats = state.playerStats)
        }
    }
}

@Composable
private fun AnalysisContent(playerStats: List<PlayerTimeStats>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(TFMSpacing.spacing04)
    ) {
        Text(
            text = stringResource(R.string.analysis_times_tab),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = TFMSpacing.spacing04)
        )

        Text(
            text = stringResource(R.string.analysis_player_time_chart_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = TFMSpacing.spacing03)
        )

        PlayerTimeBarChart(playerStats = playerStats)
    }
}

@Composable
private fun PlayerTimeBarChart(playerStats: List<PlayerTimeStats>) {
    if (playerStats.isEmpty()) return

    val maxTime = playerStats.maxOfOrNull { it.totalTimeMillis } ?: 0L

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(TFMSpacing.spacing03)
    ) {
        playerStats.forEach { stats ->
            PlayerTimeBar(
                stats = stats,
                maxTime = maxTime
            )
        }
    }
}

@Composable
private fun PlayerTimeBar(
    stats: PlayerTimeStats,
    maxTime: Long,
) {
    val minutes = (stats.totalTimeMillis / 1000 / 60).toInt()
    val fillPercentage = if (maxTime > 0) stats.totalTimeMillis.toFloat() / maxTime.toFloat() else 0f

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${stats.player.firstName} ${stats.player.lastName}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "$minutes ${stringResource(R.string.analysis_minutes_label)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Primary
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = TFMSpacing.spacing01),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(32.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                ) {
                    val barWidth = size.width * fillPercentage
                    drawRoundRect(
                        color = Primary,
                        topLeft = Offset.Zero,
                        size = Size(barWidth, size.height),
                        cornerRadius = CornerRadius(8.dp.toPx())
                    )
                }
            }
            
            Text(
                text = stringResource(R.string.analysis_matches_played, stats.matchesPlayed),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(start = TFMSpacing.spacing02)
                    .width(80.dp),
                textAlign = TextAlign.End,
                fontSize = 11.sp
            )
        }
    }
}
