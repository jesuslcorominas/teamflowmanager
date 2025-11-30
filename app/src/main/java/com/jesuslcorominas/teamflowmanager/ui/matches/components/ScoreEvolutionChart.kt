package com.jesuslcorominas.teamflowmanager.ui.matches.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.model.ScorePoint
import com.jesuslcorominas.teamflowmanager.ui.theme.ChartOpponentColor
import com.jesuslcorominas.teamflowmanager.ui.theme.ChartTeamColor
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMAppTheme
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.DotProperties
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.IndicatorCount
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.Line
import ir.ehsannarmani.compose_charts.models.StrokeStyle
import kotlin.math.max

@Composable
fun ScoreEvolutionChart(
    scoreEvolution: List<ScorePoint>,
    modifier: Modifier = Modifier,
) {
    if (scoreEvolution.isEmpty()) return

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(TFMSpacing.spacing04),
        ) {
            // Title
            Text(
                text = stringResource(R.string.score_evolution_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = TFMSpacing.spacing03),
            )

            // Legend
            ChartLegend()

            // Chart
            val maxScore = remember(scoreEvolution) {
                max(
                    scoreEvolution.maxOfOrNull { it.teamScore } ?: 0,
                    scoreEvolution.maxOfOrNull { it.opponentScore } ?: 0
                ).coerceAtLeast(1)
            }

            val teamScoreValues = remember(scoreEvolution) {
                scoreEvolution.map { it.teamScore.toDouble() }
            }

            val opponentScoreValues = remember(scoreEvolution) {
                scoreEvolution.map { it.opponentScore.toDouble() }
            }

            LineChart(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(top = TFMSpacing.spacing03),
                data = remember(teamScoreValues, opponentScoreValues) {
                    listOf(
                        createScoreLine("Team", teamScoreValues, ChartTeamColor),
                        createScoreLine("Opponent", opponentScoreValues, ChartOpponentColor),
                    )
                },
                animationMode = AnimationMode.Together(delayBuilder = { it * 200L }),
                gridProperties = GridProperties(
                    enabled = true,
                    xAxisProperties = GridProperties.AxisProperties(
                        enabled = false,
                    ),
                    yAxisProperties = GridProperties.AxisProperties(
                        enabled = true,
                        lineCount = maxScore + 1,
                    ),
                ),
                indicatorProperties = HorizontalIndicatorProperties(
                    enabled = true,
                    count = IndicatorCount.CountBased(maxScore + 1),
                ),
                labelHelperProperties = LabelHelperProperties(
                    enabled = false,
                ),
                minValue = 0.0,
                maxValue = maxScore.toDouble(),
            )
        }
    }
}

@Composable
private fun ChartLegend() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Team legend
        LegendItem(
            color = ChartTeamColor,
            label = stringResource(R.string.score_my_team),
        )

        // Spacer between legends
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(horizontal = TFMSpacing.spacing04))

        // Opponent legend
        LegendItem(
            color = ChartOpponentColor,
            label = stringResource(R.string.score_opponent),
        )
    }
}

@Composable
private fun LegendItem(
    color: androidx.compose.ui.graphics.Color,
    label: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(TFMSpacing.spacing02),
    ) {
        Surface(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape),
            color = color,
            content = {},
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Creates a Line configuration for the score evolution chart.
 */
private fun createScoreLine(
    label: String,
    values: List<Double>,
    color: androidx.compose.ui.graphics.Color,
): Line {
    return Line(
        label = label,
        values = values,
        color = androidx.compose.ui.graphics.SolidColor(color),
        firstGradientFillColor = color.copy(alpha = 0.3f),
        secondGradientFillColor = color.copy(alpha = 0.0f),
        strokeAnimationSpec = androidx.compose.animation.core.tween(1000),
        gradientAnimationDelay = 500,
        drawStyle = DrawStyle.Stroke(
            width = 3.dp,
            strokeStyle = StrokeStyle.Normal,
        ),
        dotProperties = DotProperties(
            enabled = true,
            color = androidx.compose.ui.graphics.SolidColor(color),
            radius = 4.dp,
            strokeWidth = 2.dp,
            strokeColor = androidx.compose.ui.graphics.SolidColor(color),
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun ScoreEvolutionChartPreview() {
    val scoreEvolution = listOf(
        ScorePoint(timeMillis = 0L, teamScore = 0, opponentScore = 0),
        ScorePoint(timeMillis = 300000L, teamScore = 1, opponentScore = 0),
        ScorePoint(timeMillis = 600000L, teamScore = 2, opponentScore = 0),
        ScorePoint(timeMillis = 900000L, teamScore = 2, opponentScore = 1),
        ScorePoint(timeMillis = 2700000L, teamScore = 3, opponentScore = 1),
        ScorePoint(timeMillis = 3000000L, teamScore = 3, opponentScore = 1),
    )

    TFMAppTheme {
        ScoreEvolutionChart(scoreEvolution = scoreEvolution)
    }
}
