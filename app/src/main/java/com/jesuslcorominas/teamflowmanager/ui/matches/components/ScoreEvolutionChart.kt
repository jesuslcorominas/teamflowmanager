package com.jesuslcorominas.teamflowmanager.ui.matches.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.model.ScorePoint
import com.jesuslcorominas.teamflowmanager.ui.theme.ChartOpponentColor
import com.jesuslcorominas.teamflowmanager.ui.theme.ChartTeamColor
import com.jesuslcorominas.teamflowmanager.ui.theme.ContentHigh
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMAppTheme
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import kotlin.math.max

@Composable
fun ScoreEvolutionChart(
    scoreEvolution: List<ScorePoint>,
    teamName: String,
    opponentName: String,
    modifier: Modifier = Modifier,
) {
    if (scoreEvolution.isEmpty()) return

    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier =
                Modifier
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

            // Legend with actual team names
            ChartLegend(
                teamName = teamName,
                opponentName = opponentName,
            )

            Spacer(modifier = Modifier.height(TFMSpacing.spacing03))

            // Custom Step Chart with time-proportional X-axis
            val maxScore =
                remember(scoreEvolution) {
                    max(
                        scoreEvolution.maxOfOrNull { it.teamScore } ?: 0,
                        scoreEvolution.maxOfOrNull { it.opponentScore } ?: 0,
                    ).coerceAtLeast(1)
                }

            val maxTime =
                remember(scoreEvolution) {
                    scoreEvolution.maxOfOrNull { it.timeMillis } ?: 1L
                }

            val density = LocalDensity.current
            val textSize = with(density) { 10.sp.toPx() }

            Canvas(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(200.dp),
            ) {
                val chartPadding = 40.dp.toPx()
                val chartWidth = size.width - chartPadding * 2
                val chartHeight = size.height - chartPadding
                val labelColor =
                    android.graphics.Color.argb(
                        (ContentHigh.alpha * 255).toInt(),
                        (ContentHigh.red * 255).toInt(),
                        (ContentHigh.green * 255).toInt(),
                        (ContentHigh.blue * 255).toInt(),
                    )

                // Draw Y-axis labels (integers only)
                for (i in 0..maxScore) {
                    val y = chartHeight - (i.toFloat() / maxScore * chartHeight) + chartPadding / 2
                    drawContext.canvas.nativeCanvas.drawText(
                        i.toString(),
                        chartPadding / 2 - 10,
                        y + textSize / 3,
                        android.graphics.Paint().apply {
                            color = labelColor
                            this.textSize = textSize
                            textAlign = android.graphics.Paint.Align.CENTER
                        },
                    )
                    // Draw horizontal grid line
                    drawLine(
                        color = ContentHigh.copy(alpha = 0.3f),
                        start = Offset(chartPadding, y),
                        end = Offset(size.width - chartPadding / 2, y),
                        strokeWidth = 1f,
                    )
                }

                // Draw team score line (step-wise)
                drawStepLine(
                    scoreEvolution = scoreEvolution,
                    maxTime = maxTime,
                    maxScore = maxScore,
                    chartPadding = chartPadding,
                    chartWidth = chartWidth,
                    chartHeight = chartHeight,
                    color = ChartTeamColor,
                    isTeamScore = true,
                )

                // Draw opponent score line (step-wise)
                drawStepLine(
                    scoreEvolution = scoreEvolution,
                    maxTime = maxTime,
                    maxScore = maxScore,
                    chartPadding = chartPadding,
                    chartWidth = chartWidth,
                    chartHeight = chartHeight,
                    color = ChartOpponentColor,
                    isTeamScore = false,
                )

                // Draw dots at score change points
                scoreEvolution.forEach { point ->
                    val x = chartPadding + (point.timeMillis.toFloat() / maxTime * chartWidth)
                    val teamY = chartHeight - (point.teamScore.toFloat() / maxScore * chartHeight) + chartPadding / 2
                    val opponentY = chartHeight - (point.opponentScore.toFloat() / maxScore * chartHeight) + chartPadding / 2

                    // Team dot
                    drawCircle(
                        color = ChartTeamColor,
                        radius = 5.dp.toPx(),
                        center = Offset(x, teamY),
                    )
                    // Opponent dot
                    drawCircle(
                        color = ChartOpponentColor,
                        radius = 5.dp.toPx(),
                        center = Offset(x, opponentY),
                    )
                }

                // Draw X-axis time labels
                val timeLabels = listOf(0L, maxTime / 2, maxTime)
                timeLabels.forEach { time ->
                    val x = chartPadding + (time.toFloat() / maxTime * chartWidth)
                    val minutes = (time / 60000).toInt()
                    drawContext.canvas.nativeCanvas.drawText(
                        "$minutes'",
                        x,
                        size.height - 5,
                        android.graphics.Paint().apply {
                            color = labelColor
                            this.textSize = textSize
                            textAlign = android.graphics.Paint.Align.CENTER
                        },
                    )
                }
            }
        }
    }
}

/**
 * Draws a step-wise line for score evolution.
 * The line goes horizontal until the time of score change, then vertical to the new score.
 */
private fun DrawScope.drawStepLine(
    scoreEvolution: List<ScorePoint>,
    maxTime: Long,
    maxScore: Int,
    chartPadding: Float,
    chartWidth: Float,
    chartHeight: Float,
    color: Color,
    isTeamScore: Boolean,
) {
    if (scoreEvolution.size < 2) return

    val path = Path()
    var isFirst = true

    for (i in 0 until scoreEvolution.size) {
        val point = scoreEvolution[i]
        val score = if (isTeamScore) point.teamScore else point.opponentScore
        val x = chartPadding + (point.timeMillis.toFloat() / maxTime * chartWidth)
        val y = chartHeight - (score.toFloat() / maxScore * chartHeight) + chartPadding / 2

        if (isFirst) {
            path.moveTo(x, y)
            isFirst = false
        } else {
            val prevPoint = scoreEvolution[i - 1]
            val prevScore = if (isTeamScore) prevPoint.teamScore else prevPoint.opponentScore
            val prevY = chartHeight - (prevScore.toFloat() / maxScore * chartHeight) + chartPadding / 2

            // Draw horizontal line first (keep same Y as previous point)
            path.lineTo(x, prevY)
            // Then draw vertical line to new score
            path.lineTo(x, y)
        }
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(width = 3.dp.toPx()),
    )
}

@Composable
private fun ChartLegend(
    teamName: String,
    opponentName: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Team legend
        LegendItem(
            color = ChartTeamColor,
            label = teamName,
        )

        // Spacer between legends
        Spacer(modifier = Modifier.padding(horizontal = TFMSpacing.spacing04))

        // Opponent legend
        LegendItem(
            color = ChartOpponentColor,
            label = opponentName,
        )
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(TFMSpacing.spacing02),
    ) {
        Surface(
            modifier =
                Modifier
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

@Preview(showBackground = true)
@Composable
private fun ScoreEvolutionChartPreview() {
    val scoreEvolution =
        listOf(
            ScorePoint(timeMillis = 0L, teamScore = 0, opponentScore = 0, isOpponentGoal = false),
            ScorePoint(timeMillis = 300000L, teamScore = 1, opponentScore = 0, isOpponentGoal = false),
            ScorePoint(timeMillis = 420000L, teamScore = 2, opponentScore = 0, isOpponentGoal = false),
            ScorePoint(timeMillis = 900000L, teamScore = 2, opponentScore = 1, isOpponentGoal = true),
            ScorePoint(timeMillis = 2700000L, teamScore = 3, opponentScore = 1, isOpponentGoal = false),
            ScorePoint(timeMillis = 3000000L, teamScore = 3, opponentScore = 1, isOpponentGoal = false),
        )

    TFMAppTheme {
        ScoreEvolutionChart(
            scoreEvolution = scoreEvolution,
            teamName = "Loyola D",
            opponentName = "EFRO",
        )
    }
}
