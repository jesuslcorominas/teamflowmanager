package com.jesuslcorominas.teamflowmanager.ui.matches.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerActivityInterval
import com.jesuslcorominas.teamflowmanager.domain.model.ScorePoint
import com.jesuslcorominas.teamflowmanager.ui.theme.ChartOpponentColor
import com.jesuslcorominas.teamflowmanager.ui.theme.ChartTeamColor
import com.jesuslcorominas.teamflowmanager.ui.theme.ContentHigh
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import org.jetbrains.compose.resources.stringResource
import teamflowmanager.shared_ui.generated.resources.Res
import teamflowmanager.shared_ui.generated.resources.player_activity_lines_label
import teamflowmanager.shared_ui.generated.resources.player_activity_title
import teamflowmanager.shared_ui.generated.resources.players_section
import teamflowmanager.shared_ui.generated.resources.score_lines_label
import kotlin.math.max

// Chart dimension constants
private val BASE_CHART_HEIGHT = 200.dp
private val PLAYER_ROW_HEIGHT = 20.dp

private val playerColors =
    listOf(
        Color(0xFF2196F3),
        Color(0xFF4CAF50),
        Color(0xFFFF9800),
        Color(0xFF9C27B0),
        Color(0xFFE91E63),
        Color(0xFF00BCD4),
        Color(0xFFFFEB3B),
        Color(0xFF795548),
        Color(0xFF607D8B),
        Color(0xFFFF5722),
        Color(0xFF3F51B5),
        Color(0xFF009688),
        Color(0xFFCDDC39),
        Color(0xFF673AB7),
        Color(0xFF8BC34A),
    )

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlayerActivityChart(
    scoreEvolution: List<ScorePoint>,
    playerActivity: List<PlayerActivityInterval>,
    teamName: String,
    opponentName: String,
    modifier: Modifier = Modifier,
) {
    if (scoreEvolution.isEmpty() && playerActivity.isEmpty()) return

    val uniquePlayers =
        remember(playerActivity) {
            playerActivity.map { it.player }.distinctBy { it.id }.sortedBy { it.number }
        }

    val visibleLines =
        remember {
            mutableStateMapOf<String, Boolean>().apply {
                put("teamScore", true)
                put("opponentScore", true)
                uniquePlayers.forEach { player ->
                    put("player_${player.id}", true)
                }
            }
        }

    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(color = ContentHigh, fontSize = 10.sp)

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
            Text(
                text = stringResource(Res.string.player_activity_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = TFMSpacing.spacing03),
            )

            Text(
                text = stringResource(Res.string.score_lines_label),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(bottom = TFMSpacing.spacing02),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TFMSpacing.spacing04),
            ) {
                ToggleLegendItem(
                    color = ChartTeamColor,
                    label = teamName,
                    isEnabled = false,
                    isChecked = visibleLines["teamScore"] ?: true,
                    onCheckedChange = { visibleLines["teamScore"] = it },
                )
                ToggleLegendItem(
                    color = ChartOpponentColor,
                    label = opponentName,
                    isEnabled = false,
                    isChecked = visibleLines["opponentScore"] ?: true,
                    onCheckedChange = { visibleLines["opponentScore"] = it },
                )
            }

            Spacer(modifier = Modifier.height(TFMSpacing.spacing03))

            if (uniquePlayers.isNotEmpty()) {
                Text(
                    text = stringResource(Res.string.player_activity_lines_label),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = TFMSpacing.spacing02),
                )

                FlowRow(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(TFMSpacing.spacing02),
                    verticalArrangement = Arrangement.spacedBy(TFMSpacing.spacing02),
                ) {
                    uniquePlayers.forEachIndexed { index, player ->
                        val color = playerColors[index % playerColors.size]
                        ToggleLegendItem(
                            color = color,
                            label = "${player.number}. ${player.firstName}",
                            isEnabled = true,
                            isChecked = visibleLines["player_${player.id}"] ?: true,
                            onCheckedChange = { visibleLines["player_${player.id}"] = it },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(TFMSpacing.spacing03))
            }

            val maxScore =
                remember(scoreEvolution) {
                    max(
                        scoreEvolution.maxOfOrNull { it.teamScore } ?: 0,
                        scoreEvolution.maxOfOrNull { it.opponentScore } ?: 0,
                    ).coerceAtLeast(1)
                }

            val maxTime =
                remember(scoreEvolution, playerActivity) {
                    val scoreMaxTime = scoreEvolution.maxOfOrNull { it.timeMillis } ?: 0L
                    val activityMaxTime = playerActivity.maxOfOrNull { it.endTimeMillis } ?: 0L
                    max(scoreMaxTime, activityMaxTime).coerceAtLeast(1L)
                }

            val chartHeight = BASE_CHART_HEIGHT + (PLAYER_ROW_HEIGHT * uniquePlayers.size)
            val playersLabel = stringResource(Res.string.players_section)

            Canvas(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(chartHeight),
            ) {
                val leftPadding = 60.dp.toPx()
                val rightPadding = 30.dp.toPx()
                val verticalPadding = 40.dp.toPx()
                val chartWidth = size.width - leftPadding - rightPadding
                val scoreChartHeight = size.height * 0.5f - verticalPadding / 2
                val playerChartTop = size.height * 0.5f + verticalPadding / 2
                val playerChartHeight = size.height * 0.5f - verticalPadding

                // Y-axis labels for score chart
                for (i in 0..maxScore) {
                    val y = scoreChartHeight - (i.toFloat() / maxScore * scoreChartHeight) + verticalPadding / 2
                    val measured = textMeasurer.measure(i.toString(), labelStyle)
                    drawText(
                        textLayoutResult = measured,
                        topLeft = Offset(leftPadding / 2 - measured.size.width / 2f, y - measured.size.height / 2f),
                    )
                    drawLine(
                        color = ContentHigh.copy(alpha = 0.2f),
                        start = Offset(leftPadding, y),
                        end = Offset(size.width - rightPadding, y),
                        strokeWidth = 1f,
                    )
                }

                if (visibleLines["teamScore"] == true) {
                    drawScoreLine(
                        scoreEvolution = scoreEvolution,
                        maxTime = maxTime,
                        maxScore = maxScore,
                        leftPadding = leftPadding,
                        chartWidth = chartWidth,
                        chartHeight = scoreChartHeight,
                        verticalPadding = verticalPadding,
                        color = ChartTeamColor,
                        isTeamScore = true,
                    )
                }

                if (visibleLines["opponentScore"] == true) {
                    drawScoreLine(
                        scoreEvolution = scoreEvolution,
                        maxTime = maxTime,
                        maxScore = maxScore,
                        leftPadding = leftPadding,
                        chartWidth = chartWidth,
                        chartHeight = scoreChartHeight,
                        verticalPadding = verticalPadding,
                        color = ChartOpponentColor,
                        isTeamScore = false,
                    )
                }

                scoreEvolution.forEach { point ->
                    val x = leftPadding + (point.timeMillis.toFloat() / maxTime * chartWidth)

                    if (!point.isOpponentGoal && visibleLines["teamScore"] == true) {
                        val teamY = scoreChartHeight - (point.teamScore.toFloat() / maxScore * scoreChartHeight) + verticalPadding / 2
                        drawCircle(
                            color = ChartTeamColor,
                            radius = 4.dp.toPx(),
                            center = Offset(x, teamY),
                        )
                    }

                    if (point.isOpponentGoal && visibleLines["opponentScore"] == true) {
                        val opponentY = scoreChartHeight - (point.opponentScore.toFloat() / maxScore * scoreChartHeight) + verticalPadding / 2
                        drawCircle(
                            color = ChartOpponentColor,
                            radius = 4.dp.toPx(),
                            center = Offset(x, opponentY),
                        )
                    }
                }

                drawLine(
                    color = ContentHigh.copy(alpha = 0.5f),
                    start = Offset(leftPadding, size.height * 0.5f),
                    end = Offset(size.width - rightPadding, size.height * 0.5f),
                    strokeWidth = 2f,
                )

                val playersLabelMeasured = textMeasurer.measure(playersLabel, labelStyle)
                drawText(
                    textLayoutResult = playersLabelMeasured,
                    topLeft = Offset(10f, playerChartTop - playersLabelMeasured.size.height / 2f),
                )

                val rowHeight = if (uniquePlayers.isNotEmpty()) playerChartHeight / uniquePlayers.size else 0f

                uniquePlayers.forEachIndexed { index, player ->
                    if (visibleLines["player_${player.id}"] != true) return@forEachIndexed

                    val color = playerColors[index % playerColors.size]
                    val rowY = playerChartTop + index * rowHeight + rowHeight / 2

                    val numberLabel = "${player.number}"
                    val numberMeasured = textMeasurer.measure(numberLabel, labelStyle)
                    drawText(
                        textLayoutResult = numberMeasured,
                        topLeft =
                            Offset(
                                leftPadding - 30.dp.toPx() - numberMeasured.size.width,
                                rowY - numberMeasured.size.height / 2f,
                            ),
                    )

                    playerActivity
                        .filter { it.player.id == player.id }
                        .forEach { interval ->
                            val startX = leftPadding + (interval.startTimeMillis.toFloat() / maxTime * chartWidth)
                            val endX = leftPadding + (interval.endTimeMillis.toFloat() / maxTime * chartWidth)

                            drawLine(
                                color = color,
                                start = Offset(startX, rowY),
                                end = Offset(endX, rowY),
                                strokeWidth = 8.dp.toPx(),
                            )
                            drawCircle(
                                color = color,
                                radius = 4.dp.toPx(),
                                center = Offset(startX, rowY),
                            )
                            drawCircle(
                                color = color,
                                radius = 4.dp.toPx(),
                                center = Offset(endX, rowY),
                            )
                        }
                }

                val timeLabels = listOf(0L, maxTime / 2, maxTime)
                timeLabels.forEach { time ->
                    val x = leftPadding + (time.toFloat() / maxTime * chartWidth)
                    val minutes = (time / 60000).toInt()
                    val timeLabelText = "$minutes'"
                    val timeMeasured = textMeasurer.measure(timeLabelText, labelStyle)
                    drawText(
                        textLayoutResult = timeMeasured,
                        topLeft = Offset(x - timeMeasured.size.width / 2f, size.height - 5 - timeMeasured.size.height),
                    )

                    drawLine(
                        color = ContentHigh.copy(alpha = 0.1f),
                        start = Offset(x, verticalPadding / 2),
                        end = Offset(x, size.height - verticalPadding / 2),
                        strokeWidth = 1f,
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawScoreLine(
    scoreEvolution: List<ScorePoint>,
    maxTime: Long,
    maxScore: Int,
    leftPadding: Float,
    chartWidth: Float,
    chartHeight: Float,
    verticalPadding: Float,
    color: Color,
    isTeamScore: Boolean,
) {
    if (scoreEvolution.size < 2) return

    val path = Path()
    var isFirst = true

    for (i in scoreEvolution.indices) {
        val point = scoreEvolution[i]
        val score = if (isTeamScore) point.teamScore else point.opponentScore
        val x = leftPadding + (point.timeMillis.toFloat() / maxTime * chartWidth)
        val y = chartHeight - (score.toFloat() / maxScore * chartHeight) + verticalPadding / 2

        if (isFirst) {
            path.moveTo(x, y)
            isFirst = false
        } else {
            val prevPoint = scoreEvolution[i - 1]
            val prevScore = if (isTeamScore) prevPoint.teamScore else prevPoint.opponentScore
            val prevY = chartHeight - (prevScore.toFloat() / maxScore * chartHeight) + verticalPadding / 2
            path.lineTo(x, prevY)
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
private fun ToggleLegendItem(
    color: Color,
    label: String,
    isEnabled: Boolean,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.clickable { onCheckedChange(!isChecked) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(TFMSpacing.spacing01),
    ) {
        if (isEnabled) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.size(20.dp),
            )
        }
        Surface(
            modifier =
                Modifier
                    .size(10.dp)
                    .clip(CircleShape),
            color = if (isChecked) color else color.copy(alpha = 0.3f),
            content = {},
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color =
                if (isChecked) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                },
        )
    }
}
