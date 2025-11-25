package com.jesuslcorominas.teamflowmanager.ui.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.jesuslcorominas.teamflowmanager.R
import com.jesuslcorominas.teamflowmanager.domain.model.MatchReportData
import com.jesuslcorominas.teamflowmanager.domain.utils.MatchReportPdfExporter
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    }

    private fun formatTime(millis: Long): String {
        val totalSeconds = (millis / 1000).toInt()
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    override fun exportMatchReportToPdf(matchReportData: MatchReportData): String? {
        val document = PdfDocument()
        var currentPage = 1
        var yPosition = MARGIN

        // Page 1: Match Report
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
