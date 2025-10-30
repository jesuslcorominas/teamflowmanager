package com.jesuslcorominas.teamflowmanager.ui.analysis

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerGoalStats
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeStats
import com.jesuslcorominas.teamflowmanager.ui.components.EmptyContent
import com.jesuslcorominas.teamflowmanager.ui.components.Loading
import com.jesuslcorominas.teamflowmanager.ui.theme.Primary
import com.jesuslcorominas.teamflowmanager.ui.theme.PrimaryLight
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import com.jesuslcorominas.teamflowmanager.viewmodel.AnalysisTab
import com.jesuslcorominas.teamflowmanager.viewmodel.AnalysisUiState
import com.jesuslcorominas.teamflowmanager.viewmodel.AnalysisViewModel
import ir.ehsannarmani.compose_charts.RowChart
import ir.ehsannarmani.compose_charts.models.BarProperties
import ir.ehsannarmani.compose_charts.models.Bars
import ir.ehsannarmani.compose_charts.models.Bars.Data.Radius.Rectangle
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.IndicatorCount
import ir.ehsannarmani.compose_charts.models.VerticalIndicatorProperties
import org.koin.androidx.compose.koinViewModel

@Composable
fun AnalysisScreen(
    viewModel: AnalysisViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()

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
                        text = stringResource(R.string.analysis_times_tab),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            )
            Tab(
                selected = selectedTab == AnalysisTab.GOALS,
                onClick = { viewModel.selectTab(AnalysisTab.GOALS) },
                text = {
                    Text(
                        text = stringResource(R.string.analysis_goals_tab),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when (val state = uiState) {
                is AnalysisUiState.Loading -> Loading()
                is AnalysisUiState.Empty -> EmptyContent(
                    when (selectedTab) {
                        AnalysisTab.TIMES -> stringResource(R.string.analysis_no_data)
                        AnalysisTab.GOALS -> stringResource(R.string.analysis_no_goals_data)
                    }
                )

                is AnalysisUiState.Success -> {
                    when (selectedTab) {
                        AnalysisTab.TIMES -> {
                            if (state.playerTimeStats.isEmpty()) {
                                EmptyContent(stringResource(R.string.analysis_no_data))
                            } else {
                                PlayerTimeChart(playerStats = state.playerTimeStats)
                            }
                        }

                        AnalysisTab.GOALS -> {
                            if (state.playerGoalStats.isEmpty()) {
                                EmptyContent(stringResource(R.string.analysis_no_goals_data))
                            } else {
                                PlayerGoalChart(playerStats = state.playerGoalStats)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerTimeChart(playerStats: List<PlayerTimeStats>) {
    val label = stringResource(R.string.player_time_title)

    RowChart(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = TFMSpacing.spacing04, end = TFMSpacing.spacing04, top = TFMSpacing.spacing04),
        gridProperties = GridProperties(enabled = false),
        indicatorProperties = VerticalIndicatorProperties(
            indicators = listOf(
                playerStats.maxBy { it.totalTimeMinutes }.totalTimeMinutes,
                playerStats.maxBy { it.totalTimeMinutes }.totalTimeMinutes / 2,
                0.0,
            ),
            count = IndicatorCount.CountBased(count = 3),
        ),
        data = remember {
            playerStats.map {
                Bars(
                    label = "${it.player.firstName} ${it.player.lastName}",
                    values = listOf(
                        Bars.Data(
                            label = label,
                            value = it.totalTimeMinutes,
                            color = Brush.horizontalGradient(
                                colors = listOf(PrimaryLight, Primary)
                            )
                        ),
                    )
                )
            }
        },
        barProperties = BarProperties(
            thickness = TFMSpacing.spacing06,
            spacing = 0.dp,
            cornerRadius = Rectangle(topRight = TFMSpacing.spacing04, bottomRight = TFMSpacing.spacing04),
        ),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
    )
}

@Composable
private fun PlayerGoalChart(playerStats: List<PlayerGoalStats>) {
    val label = stringResource(R.string.analysis_goals_label)

    RowChart(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = TFMSpacing.spacing04, end = TFMSpacing.spacing04, top = TFMSpacing.spacing04),
        gridProperties = GridProperties(enabled = false),
        indicatorProperties = VerticalIndicatorProperties(
            indicators = listOf(
                playerStats.maxBy { it.totalGoals }.totalGoals.toDouble(),
                (playerStats.maxBy { it.totalGoals }.totalGoals.toDouble() / 2).let { if (it % 2 == 0.toDouble()) it else null },
                0.0,
            ).mapNotNull { it },
            count = IndicatorCount.CountBased((playerStats.maxBy { it.totalGoals }.totalGoals.toDouble() / 2).let { if (it % 2 == 0.toDouble()) 3 else 2 }),
        ),
        data = remember {
            playerStats.map {
                Bars(
                    label = "${it.player.firstName} ${it.player.lastName}",
                    values = listOf(
                        Bars.Data(
                            label = label,
                            value = it.totalGoals.toDouble(),
                            color = Brush.horizontalGradient(
                                colors = listOf(PrimaryLight, Primary)
                            )
                        ),
                    )
                )
            }
        },
        barProperties = BarProperties(
            thickness = TFMSpacing.spacing06,
            spacing = 0.dp,
            cornerRadius = Rectangle(topRight = TFMSpacing.spacing04, bottomRight = TFMSpacing.spacing04),
        ),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
    )
}
