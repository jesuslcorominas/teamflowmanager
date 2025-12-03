package com.jesuslcorominas.teamflowmanager.ui.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.model.MatchReportData
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerActivityInterval
import com.jesuslcorominas.teamflowmanager.domain.model.ScorePoint
import com.jesuslcorominas.teamflowmanager.domain.model.TimelineEvent
import com.jesuslcorominas.teamflowmanager.domain.utils.MatchReportPdfExporter
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

class MatchReportPdfExporterImpl(private val context: Context) : MatchReportPdfExporter {

    companion object {
        private const val PAGE_WIDTH = 595 // A4 width in points
        private const val PAGE_HEIGHT = 842 // A4 height in points
        private const val MARGIN = 40f
        private const val LINE_HEIGHT = 20f
        private const val TITLE_SIZE = 18f
        private const val SECTION_SIZE = 14f
        private const val BODY_SIZE = 10f
        private const val SMALL_SIZE = 8f
        private const val MIN_TABLE_ROW_HEIGHT = 20f
        private const val HEADER_ROW_HEIGHT = 24f
        private const val CHART_HEIGHT = 150f
        private const val PLAYER_ACTIVITY_ROW_HEIGHT = 16f
        private const val TIMELINE_ITEM_HEIGHT = 36f  // Increased from 24f for more spacing between items
        
        // Chart colors
        private val CHART_TEAM_COLOR = Color.rgb(76, 175, 80) // Green
        private val CHART_OPPONENT_COLOR = Color.rgb(244, 67, 54) // Red
        private val CHART_GRID_COLOR = Color.rgb(200, 200, 200)
        
        // Player activity colors
        private val PLAYER_COLORS = listOf(
            Color.rgb(33, 150, 243),  // Blue
            Color.rgb(76, 175, 80),   // Green
            Color.rgb(255, 152, 0),   // Orange
            Color.rgb(156, 39, 176),  // Purple
            Color.rgb(233, 30, 99),   // Pink
            Color.rgb(0, 188, 212),   // Cyan
            Color.rgb(255, 235, 59),  // Yellow
            Color.rgb(121, 85, 72),   // Brown
            Color.rgb(96, 125, 139),  // Blue Gray
            Color.rgb(255, 87, 34),   // Deep Orange
            Color.rgb(63, 81, 181),   // Indigo
            Color.rgb(0, 150, 136),   // Teal
            Color.rgb(205, 220, 57),  // Lime
            Color.rgb(103, 58, 183),  // Deep Purple
            Color.rgb(139, 195, 74),  // Light Green
        )
    }

    private fun formatTime(millis: Long): String {
        val totalSeconds = (millis / 1000).toInt()
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%d:%02d", minutes, seconds)
    }
    
    private fun formatTimeMinutes(millis: Long): String {
        val minutes = (millis / 60000).toInt()
        return "${minutes}'"
    }

    override fun exportMatchReportToPdf(matchReportData: MatchReportData): String? {
        val document = PdfDocument()
        var currentPage = 1
        var yPosition = MARGIN

        // ========== PAGE 1: Match Summary and Player Table ==========
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, currentPage).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        // Title
        yPosition = drawTitle(canvas, context.getString(R.string.match_report_title), yPosition)
        yPosition += LINE_HEIGHT

        // Match information
        val match = matchReportData.match
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val dateString = match.dateTime?.let { dateFormat.format(Date(it)) } ?: "-"

        yPosition = drawSectionTitle(canvas, context.getString(R.string.match_info_section), yPosition)
        yPosition += LINE_HEIGHT

        yPosition = drawLabelValue(canvas, context.getString(R.string.opponent), match.opponent, yPosition)
        yPosition = drawLabelValue(canvas, context.getString(R.string.date_label), dateString, yPosition)
        yPosition = drawLabelValue(canvas, context.getString(R.string.location), match.location, yPosition)
        yPosition = drawLabelValue(
            canvas,
            context.getString(R.string.result_label),
            context.getString(R.string.match_score, match.goals, match.opponentGoals),
            yPosition
        )
        yPosition += LINE_HEIGHT

        // Players section
        yPosition = drawSectionTitle(canvas, context.getString(R.string.players_section), yPosition)
        yPosition += LINE_HEIGHT

        // Player table header
        yPosition = drawPlayerTableHeader(canvas, yPosition)

        matchReportData.playerReports.forEachIndexed { index, playerReport ->
            if (yPosition > PAGE_HEIGHT - MARGIN * 2) {
                document.finishPage(page)
                currentPage++
                pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, currentPage).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                yPosition = MARGIN
                yPosition = drawPlayerTableHeader(canvas, yPosition)
            }

            yPosition = drawPlayerRow(canvas, playerReport, yPosition, index)
        }

        document.finishPage(page)

        // ========== PAGE 2: Charts (Score Evolution + Player Activity) ==========
        currentPage++
        pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, currentPage).create()
        page = document.startPage(pageInfo)
        canvas = page.canvas
        yPosition = MARGIN

        // Score Evolution Chart Section
        if (matchReportData.scoreEvolution.isNotEmpty()) {
            yPosition = drawSectionTitle(canvas, context.getString(R.string.score_evolution_title), yPosition)
            yPosition += LINE_HEIGHT / 2
            yPosition = drawScoreEvolutionChart(
                canvas, 
                matchReportData.scoreEvolution, 
                match.teamName, 
                match.opponent, 
                yPosition
            )
            yPosition += LINE_HEIGHT * 2
        }

        // Player Activity Chart Section
        if (matchReportData.playerActivity.isNotEmpty()) {
            yPosition = drawSectionTitle(canvas, context.getString(R.string.player_activity_title), yPosition)
            yPosition += LINE_HEIGHT / 2
            yPosition = drawPlayerActivityChart(
                canvas,
                matchReportData.playerActivity,
                matchReportData.scoreEvolution,
                yPosition
            )
        }

        document.finishPage(page)

        // ========== PAGE 3+: Timeline ==========
        if (matchReportData.timelineEvents.isNotEmpty()) {
            currentPage++
            pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, currentPage).create()
            page = document.startPage(pageInfo)
            canvas = page.canvas
            yPosition = MARGIN
            
            yPosition = drawSectionTitle(canvas, context.getString(R.string.timeline_tab), yPosition)
            yPosition += LINE_HEIGHT / 2
            
            matchReportData.timelineEvents.forEach { event ->
                // Check if we need a new page for timeline items
                if (yPosition + TIMELINE_ITEM_HEIGHT > PAGE_HEIGHT - MARGIN) {
                    document.finishPage(page)
                    currentPage++
                    pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, currentPage).create()
                    page = document.startPage(pageInfo)
                    canvas = page.canvas
                    yPosition = MARGIN
                }
                
                yPosition = drawTimelineEvent(canvas, event, yPosition)
            }

            document.finishPage(page)
        }

        // Save to file
        return try {
            val fileName = "partido_${match.opponent.replace(" ", "_")}_${System.currentTimeMillis()}.pdf"
            val file = File(context.cacheDir, fileName)
            val outputStream = FileOutputStream(file)
            document.writeTo(outputStream)
            outputStream.close()
            document.close()

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            uri.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            document.close()
            null
        }
    }

    private fun drawTitle(canvas: Canvas, text: String, yPosition: Float): Float {
        val paint = Paint().apply {
            textSize = TITLE_SIZE
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
        canvas.drawText(text, PAGE_WIDTH / 2f, yPosition, paint)
        return yPosition + LINE_HEIGHT * 1.5f
    }

    private fun drawSectionTitle(canvas: Canvas, text: String, yPosition: Float): Float {
        val paint = Paint().apply {
            textSize = SECTION_SIZE
            isFakeBoldText = true
        }
        canvas.drawText(text, MARGIN, yPosition, paint)
        return yPosition + LINE_HEIGHT
    }

    private fun drawLabelValue(
        canvas: Canvas,
        label: String,
        value: String,
        yPosition: Float
    ): Float {
        val paint = Paint().apply {
            textSize = BODY_SIZE
        }
        val boldPaint = Paint().apply {
            textSize = BODY_SIZE
            isFakeBoldText = true
        }

        canvas.drawText("$label:", MARGIN, yPosition, boldPaint)
        canvas.drawText(value, MARGIN + 120f, yPosition, paint)
        return yPosition + LINE_HEIGHT
    }

    private fun drawScoreEvolutionChart(
        canvas: Canvas,
        scoreEvolution: List<ScorePoint>,
        teamName: String,
        opponentName: String,
        yPosition: Float
    ): Float {
        // Early return if insufficient data points to draw a meaningful chart
        if (scoreEvolution.size < 2) {
            return yPosition
        }
        
        val chartLeft = MARGIN + 30f
        val chartRight = PAGE_WIDTH - MARGIN
        val chartTop = yPosition
        val chartBottom = yPosition + CHART_HEIGHT
        val chartWidth = chartRight - chartLeft
        val chartHeight = CHART_HEIGHT

        // Calculate max values
        val maxScore = max(
            scoreEvolution.maxOfOrNull { it.teamScore } ?: 0,
            scoreEvolution.maxOfOrNull { it.opponentScore } ?: 0
        ).coerceAtLeast(1)
        
        val maxTime = scoreEvolution.maxOfOrNull { it.timeMillis } ?: 1L

        // Draw chart background
        val backgroundPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawRect(chartLeft, chartTop, chartRight, chartBottom, backgroundPaint)

        // Draw grid lines
        val gridPaint = Paint().apply {
            color = CHART_GRID_COLOR
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        
        val labelPaint = Paint().apply {
            textSize = SMALL_SIZE
            color = Color.GRAY
            textAlign = Paint.Align.RIGHT
        }

        // Draw horizontal grid lines and Y-axis labels
        for (i in 0..maxScore) {
            val y = chartBottom - (i.toFloat() / maxScore * chartHeight)
            canvas.drawLine(chartLeft, y, chartRight, y, gridPaint)
            canvas.drawText(i.toString(), chartLeft - 5f, y + SMALL_SIZE / 3, labelPaint)
        }

        // Draw team score line (step-wise)
        drawStepLine(canvas, scoreEvolution, maxTime, maxScore, chartLeft, chartBottom, chartWidth, chartHeight, CHART_TEAM_COLOR, true)
        
        // Draw opponent score line (step-wise)
        drawStepLine(canvas, scoreEvolution, maxTime, maxScore, chartLeft, chartBottom, chartWidth, chartHeight, CHART_OPPONENT_COLOR, false)

        // Draw dots ONLY for the team that scored at each point
        val dotPaint = Paint().apply {
            style = Paint.Style.FILL
        }
        
        // Draw dots ONLY for goals (where score changed compared to previous point)
        scoreEvolution.forEachIndexed { index, point ->
            if (index == 0) return@forEachIndexed // Skip starting point (0-0, no goal scored)
            
            val prevPoint = scoreEvolution[index - 1]
            val teamScoreChanged = point.teamScore > prevPoint.teamScore
            val opponentScoreChanged = point.opponentScore > prevPoint.opponentScore
            
            // Only draw dot if a score actually changed (a goal was scored)
            if (!teamScoreChanged && !opponentScoreChanged) return@forEachIndexed
            
            val x = chartLeft + (point.timeMillis.toFloat() / maxTime * chartWidth)

            // Draw dot for the team that scored at this point
            if (opponentScoreChanged) {
                // Opponent scored - draw opponent dot
                val opponentY = chartBottom - (point.opponentScore.toFloat() / maxScore * chartHeight)
                dotPaint.color = CHART_OPPONENT_COLOR
                canvas.drawCircle(x, opponentY, 4f, dotPaint)
            } else if (teamScoreChanged) {
                // Team scored - draw team dot
                val teamY = chartBottom - (point.teamScore.toFloat() / maxScore * chartHeight)
                dotPaint.color = CHART_TEAM_COLOR
                canvas.drawCircle(x, teamY, 4f, dotPaint)
            }
        }

        // Draw X-axis time labels
        val timeLabelPaint = Paint().apply {
            textSize = SMALL_SIZE
            color = Color.GRAY
            textAlign = Paint.Align.CENTER
        }
        
        val timeLabels = listOf(0L, maxTime / 2, maxTime)
        timeLabels.forEach { time ->
            val x = chartLeft + (time.toFloat() / maxTime * chartWidth)
            canvas.drawText(formatTimeMinutes(time), x, chartBottom + SMALL_SIZE + 5f, timeLabelPaint)
        }

        // Draw legend
        val legendY = chartBottom + LINE_HEIGHT + 5f
        val legendPaint = Paint().apply {
            textSize = SMALL_SIZE
            color = Color.BLACK
        }
        val legendDotPaint = Paint().apply {
            style = Paint.Style.FILL
        }

        // Team legend
        val teamLegendX = MARGIN + 50f
        legendDotPaint.color = CHART_TEAM_COLOR
        canvas.drawCircle(teamLegendX, legendY - 3f, 5f, legendDotPaint)
        canvas.drawText(teamName, teamLegendX + 10f, legendY, legendPaint)

        // Opponent legend
        val opponentLegendX = PAGE_WIDTH / 2 + 50f
        legendDotPaint.color = CHART_OPPONENT_COLOR
        canvas.drawCircle(opponentLegendX, legendY - 3f, 5f, legendDotPaint)
        canvas.drawText(opponentName, opponentLegendX + 10f, legendY, legendPaint)

        return chartBottom + LINE_HEIGHT * 1.5f
    }

    private fun drawStepLine(
        canvas: Canvas,
        scoreEvolution: List<ScorePoint>,
        maxTime: Long,
        maxScore: Int,
        chartLeft: Float,
        chartBottom: Float,
        chartWidth: Float,
        chartHeight: Float,
        color: Int,
        isTeamScore: Boolean
    ) {
        if (scoreEvolution.size < 2) return

        val linePaint = Paint().apply {
            this.color = color
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }

        val path = Path()
        var isFirst = true

        for (i in scoreEvolution.indices) {
            val point = scoreEvolution[i]
            val score = if (isTeamScore) point.teamScore else point.opponentScore
            val x = chartLeft + (point.timeMillis.toFloat() / maxTime * chartWidth)
            val y = chartBottom - (score.toFloat() / maxScore * chartHeight)

            if (isFirst) {
                path.moveTo(x, y)
                isFirst = false
            } else {
                val prevPoint = scoreEvolution[i - 1]
                val prevScore = if (isTeamScore) prevPoint.teamScore else prevPoint.opponentScore
                val prevY = chartBottom - (prevScore.toFloat() / maxScore * chartHeight)

                // Draw horizontal line first (keep same Y as previous point)
                path.lineTo(x, prevY)
                // Then draw vertical line to new score
                path.lineTo(x, y)
            }
        }

        canvas.drawPath(path, linePaint)
    }

    private fun drawPlayerActivityChart(
        canvas: Canvas,
        playerActivity: List<PlayerActivityInterval>,
        scoreEvolution: List<ScorePoint>,
        yPosition: Float
    ): Float {
        if (playerActivity.isEmpty()) return yPosition
        
        // Get unique players from activity intervals
        val uniquePlayers = playerActivity.map { it.player }.distinctBy { it.id }.sortedBy { it.number }
        
        val chartLeft = MARGIN + 40f
        val chartRight = PAGE_WIDTH - MARGIN
        val chartWidth = chartRight - chartLeft
        val chartHeight = uniquePlayers.size * PLAYER_ACTIVITY_ROW_HEIGHT + 20f
        val chartBottom = yPosition + chartHeight
        
        // Calculate max time from both score evolution and player activity
        val scoreMaxTime = scoreEvolution.maxOfOrNull { it.timeMillis } ?: 0L
        val activityMaxTime = playerActivity.maxOfOrNull { it.endTimeMillis } ?: 0L
        val maxTime = max(scoreMaxTime, activityMaxTime).coerceAtLeast(1L)
        
        // Draw chart background
        val backgroundPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawRect(chartLeft, yPosition, chartRight, chartBottom, backgroundPaint)
        
        // Draw player activity bars
        val labelPaint = Paint().apply {
            textSize = SMALL_SIZE
            color = Color.BLACK
            textAlign = Paint.Align.RIGHT
        }
        
        uniquePlayers.forEachIndexed { index, player ->
            val rowY = yPosition + 10f + index * PLAYER_ACTIVITY_ROW_HEIGHT + PLAYER_ACTIVITY_ROW_HEIGHT / 2
            val playerColor = PLAYER_COLORS[index % PLAYER_COLORS.size]
            
            // Draw player number label
            canvas.drawText("${player.number}", chartLeft - 5f, rowY + SMALL_SIZE / 3, labelPaint)
            
            // Draw activity intervals for this player
            val barPaint = Paint().apply {
                color = playerColor
                style = Paint.Style.FILL
            }
            
            playerActivity
                .filter { it.player.id == player.id }
                .forEach { interval ->
                    val startX = chartLeft + (interval.startTimeMillis.toFloat() / maxTime * chartWidth)
                    val endX = chartLeft + (interval.endTimeMillis.toFloat() / maxTime * chartWidth)
                    
                    // Draw horizontal bar
                    canvas.drawRect(startX, rowY - 4f, endX, rowY + 4f, barPaint)
                    
                    // Draw start and end dots
                    canvas.drawCircle(startX, rowY, 4f, barPaint)
                    canvas.drawCircle(endX, rowY, 4f, barPaint)
                }
        }
        
        // Draw X-axis time labels
        val timeLabelPaint = Paint().apply {
            textSize = SMALL_SIZE
            color = Color.GRAY
            textAlign = Paint.Align.CENTER
        }
        
        val timeLabels = listOf(0L, maxTime / 2, maxTime)
        timeLabels.forEach { time ->
            val x = chartLeft + (time.toFloat() / maxTime * chartWidth)
            canvas.drawText(formatTimeMinutes(time), x, chartBottom + SMALL_SIZE + 5f, timeLabelPaint)
        }
        
        return chartBottom + LINE_HEIGHT
    }

    private fun drawTimelineEvent(canvas: Canvas, event: TimelineEvent, yPosition: Float): Float {
        val timeText = formatTimeMinutes(event.matchElapsedTimeMillis)
        val eventText = getTimelineEventText(event)
        val eventColor = getTimelineEventColor(event)

        // Draw time badge
        val timePaint = Paint().apply {
            textSize = SMALL_SIZE
            isFakeBoldText = true
            color = Color.GRAY
        }
        canvas.drawText(timeText, MARGIN, yPosition + TIMELINE_ITEM_HEIGHT / 2 + SMALL_SIZE / 2, timePaint)

        // Draw event icon with colored background circle
        val iconCenterX = MARGIN + 48f
        val iconCenterY = yPosition + TIMELINE_ITEM_HEIGHT / 2
        val iconRadius = 12f
        
        // Draw background circle with anti-aliasing
        val backgroundPaint = Paint().apply {
            color = eventColor
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(iconCenterX, iconCenterY, iconRadius, backgroundPaint)
        
        // Draw icon on top of background circle
        drawTimelineIcon(canvas, event, iconCenterX, iconCenterY, iconRadius)

        // Draw event text
        val eventPaint = Paint().apply {
            textSize = SMALL_SIZE
            color = Color.BLACK
        }
        canvas.drawText(eventText, MARGIN + 70f, yPosition + TIMELINE_ITEM_HEIGHT / 2 + SMALL_SIZE / 2, eventPaint)

        return yPosition + TIMELINE_ITEM_HEIGHT
    }

    private fun drawTimelineIcon(canvas: Canvas, event: TimelineEvent, centerX: Float, centerY: Float, radius: Float) {
        // Get the appropriate drawable resource ID for the event
        val drawableResId = when (event) {
            is TimelineEvent.StartingLineup -> R.drawable.ic_people
            is TimelineEvent.GoalScored -> R.drawable.ic_sports_soccer
            is TimelineEvent.Substitution -> R.drawable.ic_swap_horiz
            is TimelineEvent.Timeout -> R.drawable.ic_timer
            is TimelineEvent.PeriodBreak -> R.drawable.ic_pause
        }
        
        // Load the drawable and render it at high resolution using Bitmap for better quality
        val drawable = ContextCompat.getDrawable(context, drawableResId)
        drawable?.let {
            // Set the icon color to white
            it.colorFilter = PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
            
            // Render at higher resolution for better quality (4x scale)
            val scale = 4
            val bitmapSize = 24 * scale  // Original is 24dp, render at 96px
            
            // Create a high-resolution bitmap
            val bitmap = Bitmap.createBitmap(bitmapSize, bitmapSize, Bitmap.Config.ARGB_8888)
            val bitmapCanvas = Canvas(bitmap)
            
            // Draw the drawable to the bitmap at full size
            it.setBounds(0, 0, bitmapSize, bitmapSize)
            it.draw(bitmapCanvas)
            
            // Calculate destination size (75% of circle diameter)
            val destSize = (radius * 1.5f).toInt()
            val halfSize = destSize / 2
            
            // Create destination rect centered on the icon position
            val destRect = Rect(
                (centerX - halfSize).toInt(),
                (centerY - halfSize).toInt(),
                (centerX + halfSize).toInt(),
                (centerY + halfSize).toInt()
            )
            
            // Draw the bitmap scaled down with anti-aliasing for smooth edges
            val bitmapPaint = Paint().apply {
                isAntiAlias = true
                isFilterBitmap = true  // Enable bilinear filtering for smooth scaling
            }
            canvas.drawBitmap(bitmap, null, destRect, bitmapPaint)
            
            // Recycle the bitmap to free memory
            bitmap.recycle()
        }
    }

    private fun getTimelineEventText(event: TimelineEvent): String {
        return when (event) {
            is TimelineEvent.StartingLineup -> {
                val playerNames = event.players.take(5).joinToString(", ") { 
                    "${it.number}. ${it.firstName}" 
                }
                val suffix = if (event.players.size > 5) " (+${event.players.size - 5})" else ""
                "${context.getString(R.string.timeline_starting_lineup)}: $playerNames$suffix"
            }
            is TimelineEvent.GoalScored -> {
                val scoreText = "(${event.teamScore}-${event.opponentScore})"
                if (event.isOpponentGoal) {
                    "${context.getString(R.string.timeline_opponent_goal)} $scoreText"
                } else {
                    val scorerName = event.scorer?.let { "${it.firstName} ${it.lastName}" } 
                        ?: context.getString(R.string.own_goal_option)
                    "${context.getString(R.string.timeline_goal)}: $scorerName $scoreText"
                }
            }
            is TimelineEvent.Substitution -> {
                "${context.getString(R.string.timeline_substitution)}: ${event.playerIn.firstName} ↑ ${event.playerOut.firstName} ↓"
            }
            is TimelineEvent.Timeout -> context.getString(R.string.timeline_timeout)
            is TimelineEvent.PeriodBreak -> context.getString(R.string.timeline_halftime)
        }
    }

    private fun getTimelineEventColor(event: TimelineEvent): Int {
        return when (event) {
            is TimelineEvent.StartingLineup -> Color.rgb(0, 51, 102) // Primary - Dark blue #003366
            is TimelineEvent.GoalScored -> if (event.isOpponentGoal) 
                Color.rgb(150, 6, 21) // SubstitutionRed #960615
                else Color.rgb(76, 175, 80) // SubstitutionGreen #4CAF50
            is TimelineEvent.Substitution -> Color.rgb(0, 51, 102) // Primary - Dark blue #003366
            is TimelineEvent.Timeout -> Color.rgb(59, 193, 91) // AccentAffirmative (tertiary) #3BC15B
            is TimelineEvent.PeriodBreak -> Color.rgb(150, 6, 21) // AccentEmphasis (secondary) #960615
        }
    }

    private fun drawPlayerTableHeader(canvas: Canvas, yPosition: Float): Float {
        val tableLeft = MARGIN
        val tableRight = PAGE_WIDTH - MARGIN
        val tableWidth = tableRight - tableLeft

        // Column widths - removed substitutions column
        val colNumber = tableWidth * 0.10f // Dorsal
        val colName = tableWidth * 0.30f // Nombre
        val colGK = tableWidth * 0.10f // Portero
        val colCaptain = tableWidth * 0.10f // Capitán
        val colStarter = tableWidth * 0.10f // Titular
        val colTime = tableWidth * 0.15f // Tiempo
        val colGoals = tableWidth * 0.15f // Goles (total only)

        // Draw header background
        val backgroundPaint = Paint().apply {
            color = Color.rgb(220, 220, 220)
            style = Paint.Style.FILL
        }
        canvas.drawRect(
            tableLeft,
            yPosition - HEADER_ROW_HEIGHT + 5,
            tableRight,
            yPosition + 5,
            backgroundPaint
        )

        // Draw header border
        val borderPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        canvas.drawRect(
            tableLeft,
            yPosition - HEADER_ROW_HEIGHT + 5,
            tableRight,
            yPosition + 5,
            borderPaint
        )

        // Draw vertical lines for columns
        var xPos = tableLeft + colNumber
        canvas.drawLine(xPos, yPosition - HEADER_ROW_HEIGHT + 5, xPos, yPosition + 5, borderPaint)
        xPos += colName
        canvas.drawLine(xPos, yPosition - HEADER_ROW_HEIGHT + 5, xPos, yPosition + 5, borderPaint)
        xPos += colGK
        canvas.drawLine(xPos, yPosition - HEADER_ROW_HEIGHT + 5, xPos, yPosition + 5, borderPaint)
        xPos += colCaptain
        canvas.drawLine(xPos, yPosition - HEADER_ROW_HEIGHT + 5, xPos, yPosition + 5, borderPaint)
        xPos += colStarter
        canvas.drawLine(xPos, yPosition - HEADER_ROW_HEIGHT + 5, xPos, yPosition + 5, borderPaint)
        xPos += colTime
        canvas.drawLine(xPos, yPosition - HEADER_ROW_HEIGHT + 5, xPos, yPosition + 5, borderPaint)
        xPos += colGoals
        canvas.drawLine(xPos, yPosition - HEADER_ROW_HEIGHT + 5, xPos, yPosition + 5, borderPaint)

        // Draw header text
        val textPaint = Paint().apply {
            textSize = SMALL_SIZE
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }

        val textY = yPosition - (HEADER_ROW_HEIGHT / 2) + (SMALL_SIZE / 2)

        xPos = tableLeft
        canvas.drawText(context.getString(R.string.number_short), xPos + colNumber / 2, textY, textPaint)
        xPos += colNumber
        canvas.drawText(context.getString(R.string.player_name), xPos + colName / 2, textY, textPaint)
        xPos += colName
        canvas.drawText(context.getString(R.string.goalkeeper_short), xPos + colGK / 2, textY, textPaint)
        xPos += colGK
        canvas.drawText(context.getString(R.string.captain_short), xPos + colCaptain / 2, textY, textPaint)
        xPos += colCaptain
        canvas.drawText(context.getString(R.string.starter_short), xPos + colStarter / 2, textY, textPaint)
        xPos += colStarter
        canvas.drawText(context.getString(R.string.time_short), xPos + colTime / 2, textY, textPaint)
        xPos += colTime
        canvas.drawText(context.getString(R.string.goals_short), xPos + colGoals / 2, textY, textPaint)

        return yPosition
    }

    private fun drawPlayerRow(
        canvas: Canvas,
        playerReport: com.jesuslcorominas.teamflowmanager.domain.model.PlayerMatchReport,
        yPosition: Float,
        index: Int
    ): Float {
        val tableLeft = MARGIN
        val tableRight = PAGE_WIDTH - MARGIN
        val tableWidth = tableRight - tableLeft

        // Column widths (same as header) - removed substitutions column
        val colNumber = tableWidth * 0.10f
        val colName = tableWidth * 0.30f
        val colGK = tableWidth * 0.10f
        val colCaptain = tableWidth * 0.10f
        val colStarter = tableWidth * 0.10f
        val colTime = tableWidth * 0.15f
        val colGoals = tableWidth * 0.15f

        // Use fixed row height since we're not showing multi-line goals anymore
        val rowHeight = MIN_TABLE_ROW_HEIGHT

        // Draw alternating row background
        val backgroundPaint = Paint().apply {
            color = if (index % 2 == 0) Color.WHITE else Color.rgb(245, 245, 245)
            style = Paint.Style.FILL
        }
        canvas.drawRect(tableLeft, yPosition, tableRight, yPosition + rowHeight, backgroundPaint)

        // Draw row borders
        val borderPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        canvas.drawRect(tableLeft, yPosition, tableRight, yPosition + rowHeight, borderPaint)

        // Draw vertical lines for columns
        var xPos = tableLeft + colNumber
        canvas.drawLine(xPos, yPosition, xPos, yPosition + rowHeight, borderPaint)
        xPos += colName
        canvas.drawLine(xPos, yPosition, xPos, yPosition + rowHeight, borderPaint)
        xPos += colGK
        canvas.drawLine(xPos, yPosition, xPos, yPosition + rowHeight, borderPaint)
        xPos += colCaptain
        canvas.drawLine(xPos, yPosition, xPos, yPosition + rowHeight, borderPaint)
        xPos += colStarter
        canvas.drawLine(xPos, yPosition, xPos, yPosition + rowHeight, borderPaint)
        xPos += colTime
        canvas.drawLine(xPos, yPosition, xPos, yPosition + rowHeight, borderPaint)
        xPos += colGoals
        canvas.drawLine(xPos, yPosition, xPos, yPosition + rowHeight, borderPaint)

        // Draw cell text
        val textPaint = Paint().apply {
            textSize = SMALL_SIZE
            textAlign = Paint.Align.CENTER
        }

        val singleLineTextY = yPosition + (rowHeight / 2) + (SMALL_SIZE / 2)

        xPos = tableLeft
        canvas.drawText("${playerReport.number}", xPos + colNumber / 2, singleLineTextY, textPaint)
        xPos += colNumber

        textPaint.textAlign = Paint.Align.LEFT
        val playerName = "${playerReport.player.firstName} ${playerReport.player.lastName}"
        canvas.drawText(playerName, xPos + 5, singleLineTextY, textPaint)
        xPos += colName

        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText(if (playerReport.isGoalkeeper) "X" else "-", xPos + colGK / 2, singleLineTextY, textPaint)
        xPos += colGK
        canvas.drawText(if (playerReport.isCaptain) "X" else "-", xPos + colCaptain / 2, singleLineTextY, textPaint)
        xPos += colCaptain
        canvas.drawText(if (playerReport.isStarter) "X" else "-", xPos + colStarter / 2, singleLineTextY, textPaint)
        xPos += colStarter
        canvas.drawText(formatTime(playerReport.totalPlayTimeMillis), xPos + colTime / 2, singleLineTextY, textPaint)
        xPos += colTime

        // Goals column - show only total count
        val goalsCount = playerReport.goals.size
        canvas.drawText(if (goalsCount > 0) "$goalsCount" else "-", xPos + colGoals / 2, singleLineTextY, textPaint)

        return yPosition + rowHeight
    }
}
