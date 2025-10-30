package com.jesuslcorominas.teamflowmanager.ui.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.jesuslcorominas.teamflowmanager.domain.model.ExportData
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PdfExporter(private val context: Context) {
    
    companion object {
        private const val PAGE_WIDTH = 595 // A4 width in points
        private const val PAGE_HEIGHT = 842 // A4 height in points
        private const val MARGIN = 40f
        private const val LINE_HEIGHT = 20f
        private const val TITLE_SIZE = 18f
        private const val SECTION_SIZE = 14f
        private const val BODY_SIZE = 10f
        private const val TABLE_ROW_HEIGHT = 24f
        private const val HEADER_ROW_HEIGHT = 30f
    }
    
    private fun formatTime(totalMinutes: Double): String {
        val totalSeconds = (totalMinutes * 60).toInt()
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        
        return when {
            hours > 0 -> String.format("%dh %02dm %02ds", hours, minutes, seconds)
            minutes > 0 -> String.format("%dm %02ds", minutes, seconds)
            else -> String.format("%ds", seconds)
        }
    }
    
    fun exportToPdf(exportData: ExportData, teamName: String): Uri? {
        val document = PdfDocument()
        var currentPage = 1
        var yPosition = MARGIN
        
        // Page 1: Player Statistics
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, currentPage).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas
        
        // Title
        yPosition = drawTitle(canvas, context.getString(com.jesuslcorominas.teamflowmanager.R.string.pdf_team_stats_title), yPosition)
        yPosition = drawText(canvas, teamName, yPosition, SECTION_SIZE, Paint.Align.CENTER)
        yPosition += LINE_HEIGHT
        
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dateString = context.getString(com.jesuslcorominas.teamflowmanager.R.string.pdf_date_label, dateFormat.format(Date()))
        yPosition = drawText(canvas, dateString, yPosition, BODY_SIZE, Paint.Align.CENTER)
        yPosition += LINE_HEIGHT * 2
        
        // Player Statistics Section
        yPosition = drawSectionTitle(canvas, context.getString(com.jesuslcorominas.teamflowmanager.R.string.pdf_player_stats_section), yPosition)
        yPosition += LINE_HEIGHT * 1.5f
        
        if (exportData.playerStats.isEmpty()) {
            yPosition = drawText(canvas, context.getString(com.jesuslcorominas.teamflowmanager.R.string.pdf_no_player_data), yPosition, BODY_SIZE)
            yPosition += LINE_HEIGHT
        } else {
            // Headers
            yPosition = drawPlayerStatsHeader(canvas, yPosition)
            yPosition += HEADER_ROW_HEIGHT
            
            exportData.playerStats.forEachIndexed { index, stat ->
                if (yPosition > PAGE_HEIGHT - MARGIN * 2) {
                    document.finishPage(page)
                    currentPage++
                    pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, currentPage).create()
                    page = document.startPage(pageInfo)
                    canvas = page.canvas
                    yPosition = MARGIN
                    yPosition = drawPlayerStatsHeader(canvas, yPosition)
                    yPosition += HEADER_ROW_HEIGHT
                }
                
                yPosition = drawPlayerStats(canvas, stat, yPosition, index)
                yPosition += TABLE_ROW_HEIGHT
            }
        }
        
        document.finishPage(page)
        
        // Page 2: Top Scorers and Match Results
        currentPage++
        pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, currentPage).create()
        page = document.startPage(pageInfo)
        canvas = page.canvas
        yPosition = MARGIN
        
        // Top Scorers Section
        yPosition = drawSectionTitle(canvas, context.getString(com.jesuslcorominas.teamflowmanager.R.string.pdf_scorers_section), yPosition)
        yPosition += LINE_HEIGHT
        
        if (exportData.topScorers.isEmpty()) {
            yPosition = drawText(canvas, context.getString(com.jesuslcorominas.teamflowmanager.R.string.pdf_no_goals), yPosition, BODY_SIZE)
            yPosition += LINE_HEIGHT * 2
        } else {
            exportData.topScorers.take(10).forEachIndexed { index, scorer ->
                val playerName = "${scorer.player.firstName} ${scorer.player.lastName}"
                val text = context.getString(com.jesuslcorominas.teamflowmanager.R.string.pdf_scorer_format, index + 1, playerName, scorer.totalGoals)
                yPosition = drawText(canvas, text, yPosition, BODY_SIZE)
                yPosition += LINE_HEIGHT
            }
            yPosition += LINE_HEIGHT
        }
        
        // Match Results Section
        if (yPosition > PAGE_HEIGHT - MARGIN * 3) {
            document.finishPage(page)
            currentPage++
            pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, currentPage).create()
            page = document.startPage(pageInfo)
            canvas = page.canvas
            yPosition = MARGIN
        }
        
        yPosition = drawSectionTitle(canvas, context.getString(com.jesuslcorominas.teamflowmanager.R.string.pdf_match_results_section), yPosition)
        yPosition += LINE_HEIGHT
        
        if (exportData.matchResults.isEmpty()) {
            yPosition = drawText(canvas, context.getString(com.jesuslcorominas.teamflowmanager.R.string.pdf_no_matches), yPosition, BODY_SIZE)
        } else {
            exportData.matchResults.forEach { result ->
                if (yPosition > PAGE_HEIGHT - MARGIN * 2) {
                    document.finishPage(page)
                    currentPage++
                    pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, currentPage).create()
                    page = document.startPage(pageInfo)
                    canvas = page.canvas
                    yPosition = MARGIN
                }
                
                val date = dateFormat.format(Date(result.date))
                val scoreText = "${result.teamGoals} - ${result.opponentGoals}"
                val text = "$date | ${result.opponent} ($scoreText) - ${result.location}"
                yPosition = drawText(canvas, text, yPosition, BODY_SIZE)
                yPosition += LINE_HEIGHT
            }
        }
        
        document.finishPage(page)
        
        // Save to file
        return try {
            val fileName = "estadisticas_${System.currentTimeMillis()}.pdf"
            val file = File(context.cacheDir, fileName)
            val outputStream = FileOutputStream(file)
            document.writeTo(outputStream)
            outputStream.close()
            document.close()
            
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
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
    
    private fun drawText(
        canvas: Canvas, 
        text: String, 
        yPosition: Float, 
        textSize: Float = BODY_SIZE,
        align: Paint.Align = Paint.Align.LEFT
    ): Float {
        val paint = Paint().apply {
            this.textSize = textSize
            this.textAlign = align
        }
        val x = when (align) {
            Paint.Align.CENTER -> PAGE_WIDTH / 2f
            Paint.Align.RIGHT -> PAGE_WIDTH - MARGIN
            else -> MARGIN
        }
        canvas.drawText(text, x, yPosition, paint)
        return yPosition
    }
    
    private fun drawPlayerStatsHeader(canvas: Canvas, yPosition: Float): Float {
        val tableLeft = MARGIN
        val tableRight = PAGE_WIDTH - MARGIN
        val tableWidth = tableRight - tableLeft
        
        // Column widths (percentages of table width)
        val col1Width = tableWidth * 0.35f  // Jugador
        val col2Width = tableWidth * 0.10f  // Conv
        val col3Width = tableWidth * 0.10f  // Jug
        val col4Width = tableWidth * 0.20f  // T.Tot
        val col5Width = tableWidth * 0.15f  // T.Med
        val col6Width = tableWidth * 0.10f  // Goles
        
        // Draw header background (light gray)
        val backgroundPaint = Paint().apply {
            color = Color.rgb(220, 220, 220)
            style = Paint.Style.FILL
        }
        canvas.drawRect(tableLeft, yPosition - HEADER_ROW_HEIGHT + 5, tableRight, yPosition + 5, backgroundPaint)
        
        // Draw header border
        val borderPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        canvas.drawRect(tableLeft, yPosition - HEADER_ROW_HEIGHT + 5, tableRight, yPosition + 5, borderPaint)
        
        // Draw vertical lines for columns
        var xPos = tableLeft + col1Width
        canvas.drawLine(xPos, yPosition - HEADER_ROW_HEIGHT + 5, xPos, yPosition + 5, borderPaint)
        xPos += col2Width
        canvas.drawLine(xPos, yPosition - HEADER_ROW_HEIGHT + 5, xPos, yPosition + 5, borderPaint)
        xPos += col3Width
        canvas.drawLine(xPos, yPosition - HEADER_ROW_HEIGHT + 5, xPos, yPosition + 5, borderPaint)
        xPos += col4Width
        canvas.drawLine(xPos, yPosition - HEADER_ROW_HEIGHT + 5, xPos, yPosition + 5, borderPaint)
        xPos += col5Width
        canvas.drawLine(xPos, yPosition - HEADER_ROW_HEIGHT + 5, xPos, yPosition + 5, borderPaint)
        
        // Draw header text (uppercase and bold)
        val textPaint = Paint().apply {
            textSize = BODY_SIZE
            isFakeBoldText = true
            textAlign = Paint.Align.LEFT
        }
        
        val textY = yPosition - (HEADER_ROW_HEIGHT / 2) + (BODY_SIZE / 2)
        
        canvas.drawText(context.getString(com.jesuslcorominas.teamflowmanager.R.string.pdf_header_player), tableLeft + 5, textY, textPaint)
        
        xPos = tableLeft + col1Width
        canvas.drawText(context.getString(com.jesuslcorominas.teamflowmanager.R.string.pdf_header_called_up), xPos + 5, textY, textPaint)
        
        xPos += col2Width
        canvas.drawText(context.getString(com.jesuslcorominas.teamflowmanager.R.string.pdf_header_played), xPos + 5, textY, textPaint)
        
        xPos += col3Width
        canvas.drawText(context.getString(com.jesuslcorominas.teamflowmanager.R.string.pdf_header_total_time), xPos + 5, textY, textPaint)
        
        xPos += col4Width
        canvas.drawText(context.getString(com.jesuslcorominas.teamflowmanager.R.string.pdf_header_avg_time), xPos + 5, textY, textPaint)
        
        xPos += col5Width
        canvas.drawText(context.getString(com.jesuslcorominas.teamflowmanager.R.string.pdf_header_goals), xPos + 5, textY, textPaint)
        
        return yPosition
    }
    
    private fun drawPlayerStats(canvas: Canvas, stat: com.jesuslcorominas.teamflowmanager.domain.model.PlayerExportStats, yPosition: Float, index: Int): Float {
        val tableLeft = MARGIN
        val tableRight = PAGE_WIDTH - MARGIN
        val tableWidth = tableRight - tableLeft
        
        // Column widths (same as header)
        val col1Width = tableWidth * 0.35f
        val col2Width = tableWidth * 0.10f
        val col3Width = tableWidth * 0.10f
        val col4Width = tableWidth * 0.20f
        val col5Width = tableWidth * 0.15f
        val col6Width = tableWidth * 0.10f
        
        // Draw alternating row background
        val backgroundPaint = Paint().apply {
            color = if (index % 2 == 0) Color.WHITE else Color.rgb(245, 245, 245)
            style = Paint.Style.FILL
        }
        canvas.drawRect(tableLeft, yPosition, tableRight, yPosition + TABLE_ROW_HEIGHT, backgroundPaint)
        
        // Draw row borders
        val borderPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        canvas.drawRect(tableLeft, yPosition, tableRight, yPosition + TABLE_ROW_HEIGHT, borderPaint)
        
        // Draw vertical lines for columns
        var xPos = tableLeft + col1Width
        canvas.drawLine(xPos, yPosition, xPos, yPosition + TABLE_ROW_HEIGHT, borderPaint)
        xPos += col2Width
        canvas.drawLine(xPos, yPosition, xPos, yPosition + TABLE_ROW_HEIGHT, borderPaint)
        xPos += col3Width
        canvas.drawLine(xPos, yPosition, xPos, yPosition + TABLE_ROW_HEIGHT, borderPaint)
        xPos += col4Width
        canvas.drawLine(xPos, yPosition, xPos, yPosition + TABLE_ROW_HEIGHT, borderPaint)
        xPos += col5Width
        canvas.drawLine(xPos, yPosition, xPos, yPosition + TABLE_ROW_HEIGHT, borderPaint)
        
        // Draw cell text
        val textPaint = Paint().apply {
            textSize = BODY_SIZE
            textAlign = Paint.Align.LEFT
        }
        
        val textY = yPosition + (TABLE_ROW_HEIGHT / 2) + (BODY_SIZE / 2)
        
        val playerName = "${stat.player.firstName} ${stat.player.lastName}"
        canvas.drawText(playerName, tableLeft + 5, textY, textPaint)
        
        xPos = tableLeft + col1Width
        canvas.drawText("${stat.matchesCalledUp}", xPos + 5, textY, textPaint)
        
        xPos += col2Width
        canvas.drawText("${stat.matchesPlayed}", xPos + 5, textY, textPaint)
        
        xPos += col3Width
        canvas.drawText(formatTime(stat.totalTimeMinutes), xPos + 5, textY, textPaint)
        
        xPos += col4Width
        canvas.drawText(formatTime(stat.averageTimePerMatch), xPos + 5, textY, textPaint)
        
        xPos += col5Width
        canvas.drawText("${stat.goalsScored}", xPos + 5, textY, textPaint)
        
        return yPosition
    }
}
