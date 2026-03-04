package com.jesuslcorominas.teamflowmanager.ui.analysis

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.domain.analytics.ScreenName
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerGoalStats
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeStats
import com.jesuslcorominas.teamflowmanager.ui.analytics.TrackScreenView
import com.jesuslcorominas.teamflowmanager.ui.components.EmptyContent
import com.jesuslcorominas.teamflowmanager.ui.components.Loading
import com.jesuslcorominas.teamflowmanager.ui.theme.Primary
import com.jesuslcorominas.teamflowmanager.ui.theme.PrimaryLight
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import com.jesuslcorominas.teamflowmanager.viewmodel.AnalysisTab
import com.jesuslcorominas.teamflowmanager.viewmodel.AnalysisUiState
import com.jesuslcorominas.teamflowmanager.viewmodel.AnalysisViewModel
import com.jesuslcorominas.teamflowmanager.viewmodel.ExportState
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import teamflowmanager.shared_ui.generated.resources.Res
import teamflowmanager.shared_ui.generated.resources.analysis_goals_label
import teamflowmanager.shared_ui.generated.resources.analysis_goals_tab
import teamflowmanager.shared_ui.generated.resources.analysis_no_data
import teamflowmanager.shared_ui.generated.resources.analysis_no_goals_data
import teamflowmanager.shared_ui.generated.resources.analysis_times_tab
import teamflowmanager.shared_ui.generated.resources.export_button_description
import teamflowmanager.shared_ui.generated.resources.player_time_title

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(
    onShareFile: (String) -> Unit = {},
    viewModel: AnalysisViewModel = koinViewModel(),
) {
    TrackScreenView(screenName = ScreenName.ANALYSIS, screenClass = "AnalysisScreen")

    val uiState by viewModel.uiState.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val exportState by viewModel.exportState.collectAsState()

    LaunchedEffect(exportState) {
        if (exportState is ExportState.Ready) {
            val state = exportState as ExportState.Ready
            onShareFile(state.uri)
            viewModel.exportCompleted()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            SecondaryTabRow(
                modifier = Modifier.fillMaxWidth(),
                selectedTabIndex = selectedTab.ordinal,
            ) {
                Tab(
                    selected = selectedTab == AnalysisTab.TIMES,
                    onClick = { viewModel.selectTab(AnalysisTab.TIMES) },
                    text = {
                        Text(
                            text = stringResource(Res.string.analysis_times_tab),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    },
                )
                Tab(
                    selected = selectedTab == AnalysisTab.GOALS,
                    onClick = { viewModel.selectTab(AnalysisTab.GOALS) },
                    text = {
                        Text(
                            text = stringResource(Res.string.analysis_goals_tab),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    },
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {
                    is AnalysisUiState.Loading -> Loading()
                    is AnalysisUiState.Empty -> EmptyContent(
                        when (selectedTab) {
                            AnalysisTab.TIMES -> stringResource(Res.string.analysis_no_data)
                            AnalysisTab.GOALS -> stringResource(Res.string.analysis_no_goals_data)
                        },
                    )

                    is AnalysisUiState.Success -> {
                        when (selectedTab) {
                            AnalysisTab.TIMES -> {
                                if (state.playerTimeStats.isEmpty()) {
                                    EmptyContent(stringResource(Res.string.analysis_no_data))
                                } else {
                                    PlayerTimeChart(
                                        playerStats = state.playerTimeStats,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(TFMSpacing.spacing04),
                                    )
                                }
                            }

                            AnalysisTab.GOALS -> {
                                if (state.playerGoalStats.isEmpty()) {
                                    EmptyContent(stringResource(Res.string.analysis_no_goals_data))
                                } else {
                                    PlayerGoalChart(
                                        playerStats = state.playerGoalStats,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(TFMSpacing.spacing04),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { viewModel.requestExport() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(TFMSpacing.spacing04),
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = stringResource(Res.string.export_button_description),
            )
        }
    }
}

@Composable
private fun PlayerTimeChart(
    playerStats: List<PlayerTimeStats>,
    modifier: Modifier = Modifier,
) {
    val label = stringResource(Res.string.player_time_title)
    val maxValue = playerStats.maxOfOrNull { it.totalTimeMinutes } ?: 1.0

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(TFMSpacing.spacing02),
    ) {
        items(playerStats) { stat ->
            HorizontalBar(
                label = "${stat.player.firstName} ${stat.player.lastName}",
                valueLabel = label,
                value = stat.totalTimeMinutes,
                maxValue = maxValue,
                valueText = "${stat.totalTimeMinutes.toInt()}'",
            )
        }
    }
}

@Composable
private fun PlayerGoalChart(
    playerStats: List<PlayerGoalStats>,
    modifier: Modifier = Modifier,
) {
    val label = stringResource(Res.string.analysis_goals_label)
    val maxValue = playerStats.maxOfOrNull { it.totalGoals.toDouble() } ?: 1.0

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(TFMSpacing.spacing02),
    ) {
        items(playerStats) { stat ->
            HorizontalBar(
                label = "${stat.player.firstName} ${stat.player.lastName}",
                valueLabel = label,
                value = stat.totalGoals.toDouble(),
                maxValue = maxValue,
                valueText = stat.totalGoals.toString(),
            )
        }
    }
}

@Composable
private fun HorizontalBar(
    label: String,
    valueLabel: String,
    value: Double,
    maxValue: Double,
    valueText: String,
) {
    val fraction = if (maxValue > 0) (value / maxValue).toFloat().coerceIn(0f, 1f) else 0f
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
    )
    val barGradient = Brush.horizontalGradient(colors = listOf(PrimaryLight, Primary))

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "$valueText $valueLabel",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(TFMSpacing.spacing06)
                .clip(RoundedCornerShape(topEnd = TFMSpacing.spacing04, bottomEnd = TFMSpacing.spacing04))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedFraction)
                    .fillMaxHeight()
                    .background(brush = barGradient),
            )
        }
    }
}
