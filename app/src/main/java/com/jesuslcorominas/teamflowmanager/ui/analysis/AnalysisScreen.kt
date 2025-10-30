package com.jesuslcorominas.teamflowmanager.ui.analysis

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeStats
import com.jesuslcorominas.teamflowmanager.ui.components.EmptyContent
import com.jesuslcorominas.teamflowmanager.ui.components.Loading
import com.jesuslcorominas.teamflowmanager.ui.theme.Primary
import com.jesuslcorominas.teamflowmanager.ui.theme.PrimaryLight
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
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

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is AnalysisUiState.Loading -> Loading()
            is AnalysisUiState.Empty -> EmptyContent(stringResource(R.string.analysis_no_data))
            is AnalysisUiState.Success -> PlayerTimeChart(playerStats = state.playerStats)
        }
    }
}

@Composable
private fun PlayerTimeChart(playerStats: List<PlayerTimeStats>) {
    val label = stringResource(R.string.player_time_title)

    RowChart(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = TFMSpacing.spacing05),
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
            thickness = 24.dp,
            spacing = 0.dp,
            cornerRadius = Rectangle(topRight = TFMSpacing.spacing04, bottomRight = TFMSpacing.spacing04),
        ),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
    )
}
