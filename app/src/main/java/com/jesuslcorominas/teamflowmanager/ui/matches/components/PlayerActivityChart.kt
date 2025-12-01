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
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerActivityInterval
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.domain.model.ScorePoint
import com.jesuslcorominas.teamflowmanager.ui.theme.ChartOpponentColor
import com.jesuslcorominas.teamflowmanager.ui.theme.ChartTeamColor
import com.jesuslcorominas.teamflowmanager.ui.theme.ContentHigh
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMAppTheme
import com.jesuslcorominas.teamflowmanager.ui.theme.TFMSpacing
import kotlin.math.max

// Chart dimension constants
private val BASE_CHART_HEIGHT = 200.dp
private val PLAYER_ROW_HEIGHT = 20.dp

/**
 * Predefined colors for player activity lines.
 */
private val playerColors = listOf(
    Color(0xFF2196F3), // Blue
    Color(0xFF4CAF50), // Green
    Color(0xFFFF9800), // Orange
    Color(0xFF9C27B0), // Purple
    Color(0xFFE91E63), // Pink
    Color(0xFF00BCD4), // Cyan
    Color(0xFFFFEB3B), // Yellow
    Color(0xFF795548), // Brown
    Color(0xFF607D8B), // Blue Gray
    Color(0xFFFF5722), // Deep Orange
    Color(0xFF3F51B5), // Indigo
    Color(0xFF009688), // Teal
    Color(0xFFCDDC39), // Lime
    Color(0xFF673AB7), // Deep Purple
    Color(0xFF8BC34A), // Light Green
)

/**
 * Converts a Compose Color to a native Android color int.
 */
private fun Color.toNativeColor(): Int {
    return android.graphics.Color.argb(
        (alpha * 255).toInt(),
        (red * 255).toInt(),
        (green * 255).toInt(),
        (blue * 255).toInt()
    )
}

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

    // Get unique players from activity intervals
    val uniquePlayers = remember(playerActivity) {
        playerActivity.map { it.player }.distinctBy { it.id }.sortedBy { it.number }
    }

    // Track which lines are visible
    val visibleLines = remember {
        mutableStateMapOf<String, Boolean>().apply {
            put("teamScore", true)
            put("opponentScore", true)
            uniquePlayers.forEach { player ->
                put("player_${player.id}", true)
            }
        }
    }

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
                text = stringResource(R.string.player_activity_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = TFMSpacing.spacing03),
            )

            // Toggle controls for score lines
            Text(
                text = stringResource(R.string.score_lines_label),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = TFMSpacing.spacing02),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TFMSpacing.spacing04),
            ) {
                ToggleLegendItem(
                    color = ChartTeamColor,
                    label = teamName,
                    isChecked = visibleLines["teamScore"] ?: true,
                    onCheckedChange = { visibleLines["teamScore"] = it }
                )
                ToggleLegendItem(
                    color = ChartOpponentColor,
                    label = opponentName,
                    isChecked = visibleLines["opponentScore"] ?: true,
                    onCheckedChange = { visibleLines["opponentScore"] = it }
                )
            }

            Spacer(modifier = Modifier.height(TFMSpacing.spacing03))

            // Toggle controls for player activity lines
            if (uniquePlayers.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.player_activity_lines_label),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = TFMSpacing.spacing02),
                )

                FlowRow(
                    modifier = Modifier
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
                            isChecked = visibleLines["player_${player.id}"] ?: true,
                            onCheckedChange = { visibleLines["player_${player.id}"] = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(TFMSpacing.spacing03))
            }

            // Combined Chart
            val maxScore = remember(scoreEvolution) {
                max(
                    scoreEvolution.maxOfOrNull { it.teamScore } ?: 0,
                    scoreEvolution.maxOfOrNull { it.opponentScore } ?: 0
                ).coerceAtLeast(1)
            }

            val maxTime = remember(scoreEvolution, playerActivity) {
                val scoreMaxTime = scoreEvolution.maxOfOrNull { it.timeMillis } ?: 0L
                val activityMaxTime = playerActivity.maxOfOrNull { it.endTimeMillis } ?: 0L
                max(scoreMaxTime, activityMaxTime).coerceAtLeast(1L)
            }

            val density = LocalDensity.current
            val textSize = with(density) { 10.sp.toPx() }

            // Calculate chart height based on number of players
            val chartHeight = BASE_CHART_HEIGHT + (PLAYER_ROW_HEIGHT * uniquePlayers.size)

            val playersLabel = stringResource(R.string.players_section)

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(chartHeight)
            ) {
                val leftPadding = 60.dp.toPx() // Extra space for player numbers on the left
                val rightPadding = 30.dp.toPx()
                val verticalPadding = 40.dp.toPx()
                val chartWidth = size.width - leftPadding - rightPadding
                val scoreChartHeight = size.height * 0.5f - verticalPadding / 2
                val playerChartTop = size.height * 0.5f + verticalPadding / 2
                val playerChartHeight = size.height * 0.5f - verticalPadding

                val labelColor = ContentHigh.toNativeColor()

                // Draw score Y-axis labels (integers only) - top half
                for (i in 0..maxScore) {
                    val y = scoreChartHeight - (i.toFloat() / maxScore * scoreChartHeight) + verticalPadding / 2
                    drawContext.canvas.nativeCanvas.drawText(
                        i.toString(),
                        leftPadding / 2,
                        y + textSize / 3,
                        android.graphics.Paint().apply {
                            color = labelColor
                            this.textSize = textSize
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                    // Draw horizontal grid line
                    drawLine(
                        color = ContentHigh.copy(alpha = 0.2f),
                        start = Offset(leftPadding, y),
                        end = Offset(size.width - rightPadding, y),
                        strokeWidth = 1f
                    )
                }

                // Draw team score line (step-wise) if visible
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
                        isTeamScore = true
                    )
                }

                // Draw opponent score line if visible
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
                        isTeamScore = false
                    )
                }

                // Draw dots at score change points
                scoreEvolution.forEach { point ->
                    val x = leftPadding + (point.timeMillis.toFloat() / maxTime * chartWidth)

                    if (!point.isOpponentGoal && visibleLines["teamScore"] == true) {
                        val teamY = scoreChartHeight - (point.teamScore.toFloat() / maxScore * scoreChartHeight) + verticalPadding / 2
                        drawCircle(
                            color = ChartTeamColor,
                            radius = 4.dp.toPx(),
                            center = Offset(x, teamY)
                        )
                    }

                    if (point.isOpponentGoal && visibleLines["opponentScore"] == true) {
                        val opponentY = scoreChartHeight - (point.opponentScore.toFloat() / maxScore * scoreChartHeight) + verticalPadding / 2
                        drawCircle(
                            color = ChartOpponentColor,
                            radius = 4.dp.toPx(),
                            center = Offset(x, opponentY)
                        )
                    }
                }

                // Draw separator line between charts
                drawLine(
                    color = ContentHigh.copy(alpha = 0.5f),
                    start = Offset(leftPadding, size.height * 0.5f),
                    end = Offset(size.width - rightPadding, size.height * 0.5f),
                    strokeWidth = 2f
                )

                // Draw "Players" label
                drawContext.canvas.nativeCanvas.drawText(
                    playersLabel,
                    10f,
                    playerChartTop,
                    android.graphics.Paint().apply {
                        color = labelColor
                        this.textSize = textSize
                        textAlign = android.graphics.Paint.Align.LEFT
                    }
                )

                // Draw player activity bars (horizontal bars showing when each player was active)
                val rowHeight = if (uniquePlayers.isNotEmpty()) playerChartHeight / uniquePlayers.size else 0f

                uniquePlayers.forEachIndexed { index, player ->
                    if (visibleLines["player_${player.id}"] != true) return@forEachIndexed

                    val color = playerColors[index % playerColors.size]
                    val rowY = playerChartTop + index * rowHeight + rowHeight / 2

                    // Draw player name label (jersey number)
                    drawContext.canvas.nativeCanvas.drawText(
                        "${player.number}",
                        leftPadding - 30.dp.toPx(),
                        rowY + textSize / 3,
                        android.graphics.Paint().apply {
                            this.color = labelColor
                            this.textSize = textSize
                            textAlign = android.graphics.Paint.Align.RIGHT
                        }
                    )

                    // Draw activity intervals for this player
                    playerActivity
                        .filter { it.player.id == player.id }
                        .forEach { interval ->
                            val startX = leftPadding + (interval.startTimeMillis.toFloat() / maxTime * chartWidth)
                            val endX = leftPadding + (interval.endTimeMillis.toFloat() / maxTime * chartWidth)

                            // Draw horizontal bar
                            drawLine(
                                color = color,
                                start = Offset(startX, rowY),
                                end = Offset(endX, rowY),
                                strokeWidth = 8.dp.toPx()
                            )

                            // Draw start and end dots
                            drawCircle(
                                color = color,
                                radius = 4.dp.toPx(),
                                center = Offset(startX, rowY)
                            )
                            drawCircle(
                                color = color,
                                radius = 4.dp.toPx(),
                                center = Offset(endX, rowY)
                            )
                        }
                }

                // Draw X-axis time labels
                val timeLabels = listOf(0L, maxTime / 2, maxTime)
                timeLabels.forEach { time ->
                    val x = leftPadding + (time.toFloat() / maxTime * chartWidth)
                    val minutes = (time / 60000).toInt()
                    drawContext.canvas.nativeCanvas.drawText(
                        "${minutes}'",
                        x,
                        size.height - 5,
                        android.graphics.Paint().apply {
                            color = labelColor
                            this.textSize = textSize
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )

                    // Draw vertical grid line across both charts
                    drawLine(
                        color = ContentHigh.copy(alpha = 0.1f),
                        start = Offset(x, verticalPadding / 2),
                        end = Offset(x, size.height - verticalPadding / 2),
                        strokeWidth = 1f
                    )
                }
            }
        }
    }
}

/**
 * Draws a step-wise line for score evolution.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawScoreLine(
    scoreEvolution: List<ScorePoint>,
    maxTime: Long,
    maxScore: Int,
    leftPadding: Float,
    chartWidth: Float,
    chartHeight: Float,
    verticalPadding: Float,
    color: Color,
    isTeamScore: Boolean
) {
    if (scoreEvolution.size < 2) return

    val path = androidx.compose.ui.graphics.Path()
    var isFirst = true

    for (i in 0 until scoreEvolution.size) {
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

            // Draw horizontal line first (keep same Y as previous point)
            path.lineTo(x, prevY)
            // Then draw vertical line to new score
            path.lineTo(x, y)
        }
    }

    drawPath(
        path = path,
        color = color,
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
    )
}

@Composable
private fun ToggleLegendItem(
    color: Color,
    label: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.clickable { onCheckedChange(!isChecked) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(TFMSpacing.spacing01),
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.size(20.dp),
        )
        Surface(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape),
            color = if (isChecked) color else color.copy(alpha = 0.3f),
            content = {},
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (isChecked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PlayerActivityChartPreview() {
    val player1 = Player(
        id = 1L,
        firstName = "John",
        lastName = "Doe",
        number = 10,
        positions = listOf(Position.Forward),
        teamId = 1L,
        isCaptain = true,
    )
    val player2 = Player(
        id = 2L,
        firstName = "Jane",
        lastName = "Smith",
        number = 7,
        positions = listOf(Position.Midfielder),
        teamId = 1L,
        isCaptain = false,
    )
    val player3 = Player(
        id = 3L,
        firstName = "Mike",
        lastName = "Johnson",
        number = 9,
        positions = listOf(Position.Forward),
        teamId = 1L,
        isCaptain = false,
    )

    val scoreEvolution = listOf(
        ScorePoint(timeMillis = 0L, teamScore = 0, opponentScore = 0, isOpponentGoal = false),
        ScorePoint(timeMillis = 300000L, teamScore = 1, opponentScore = 0, isOpponentGoal = false),
        ScorePoint(timeMillis = 420000L, teamScore = 2, opponentScore = 0, isOpponentGoal = false),
        ScorePoint(timeMillis = 900000L, teamScore = 2, opponentScore = 1, isOpponentGoal = true),
        ScorePoint(timeMillis = 2700000L, teamScore = 3, opponentScore = 1, isOpponentGoal = false),
        ScorePoint(timeMillis = 3000000L, teamScore = 3, opponentScore = 1, isOpponentGoal = false),
    )

    val playerActivity = listOf(
        PlayerActivityInterval(player = player1, startTimeMillis = 0L, endTimeMillis = 1500000L),
        PlayerActivityInterval(player = player2, startTimeMillis = 0L, endTimeMillis = 3000000L),
        PlayerActivityInterval(player = player3, startTimeMillis = 1500000L, endTimeMillis = 3000000L),
    )

    TFMAppTheme {
        PlayerActivityChart(
            scoreEvolution = scoreEvolution,
            playerActivity = playerActivity,
            teamName = "Loyola D",
            opponentName = "EFRO",
        )
    }
}
