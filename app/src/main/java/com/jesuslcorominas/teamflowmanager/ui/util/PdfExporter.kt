package com.jesuslcorominas.teamflowmanager.ui.util

import android.content.Context
import android.graphics.Canvas
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
        yPosition = drawTitle(canvas, "Estadísticas del Equipo", yPosition)
        yPosition = drawText(canvas, teamName, yPosition, SECTION_SIZE, Paint.Align.CENTER)
        yPosition += LINE_HEIGHT
        
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        yPosition = drawText(canvas, "Fecha: ${dateFormat.format(Date())}", yPosition, BODY_SIZE, Paint.Align.CENTER)
        yPosition += LINE_HEIGHT * 2
        
        // Player Statistics Section
        yPosition = drawSectionTitle(canvas, "Estadísticas de Jugadores", yPosition)
        yPosition += LINE_HEIGHT
        
        if (exportData.playerStats.isEmpty()) {
            yPosition = drawText(canvas, "No hay datos de jugadores disponibles", yPosition, BODY_SIZE)
            yPosition += LINE_HEIGHT
        } else {
            // Headers
            yPosition = drawPlayerStatsHeader(canvas, yPosition)
            yPosition += LINE_HEIGHT / 2
            
            exportData.playerStats.forEach { stat ->
                if (yPosition > PAGE_HEIGHT - MARGIN * 2) {
                    document.finishPage(page)
                    currentPage++
                    pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, currentPage).create()
                    page = document.startPage(pageInfo)
                    canvas = page.canvas
                    yPosition = MARGIN
                    yPosition = drawPlayerStatsHeader(canvas, yPosition)
                    yPosition += LINE_HEIGHT / 2
                }
                
                yPosition = drawPlayerStats(canvas, stat, yPosition)
                yPosition += LINE_HEIGHT
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
        yPosition = drawSectionTitle(canvas, "Goleadores", yPosition)
        yPosition += LINE_HEIGHT
        
        if (exportData.topScorers.isEmpty()) {
            yPosition = drawText(canvas, "No hay goles registrados", yPosition, BODY_SIZE)
            yPosition += LINE_HEIGHT * 2
        } else {
            exportData.topScorers.take(10).forEachIndexed { index, scorer ->
                val playerName = "${scorer.player.firstName} ${scorer.player.lastName}"
                val text = "${index + 1}. $playerName - ${scorer.totalGoals} gol(es)"
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
        
        yPosition = drawSectionTitle(canvas, "Resultados de Partidos", yPosition)
        yPosition += LINE_HEIGHT
        
        if (exportData.matchResults.isEmpty()) {
            yPosition = drawText(canvas, "No hay partidos finalizados", yPosition, BODY_SIZE)
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
        val paint = Paint().apply {
            textSize = BODY_SIZE
            isFakeBoldText = true
        }
        
        canvas.drawText("Jugador", MARGIN, yPosition, paint)
        canvas.drawText("Conv", PAGE_WIDTH * 0.4f, yPosition, paint)
        canvas.drawText("Jug", PAGE_WIDTH * 0.5f, yPosition, paint)
        canvas.drawText("T.Tot", PAGE_WIDTH * 0.6f, yPosition, paint)
        canvas.drawText("T.Med", PAGE_WIDTH * 0.72f, yPosition, paint)
        canvas.drawText("Goles", PAGE_WIDTH * 0.85f, yPosition, paint)
        
        return yPosition
    }
    
    private fun drawPlayerStats(canvas: Canvas, stat: com.jesuslcorominas.teamflowmanager.domain.model.PlayerExportStats, yPosition: Float): Float {
        val paint = Paint().apply {
            textSize = BODY_SIZE
        }
        
        val playerName = "${stat.player.firstName} ${stat.player.lastName}"
        canvas.drawText(playerName, MARGIN, yPosition, paint)
        canvas.drawText("${stat.matchesCalledUp}", PAGE_WIDTH * 0.4f, yPosition, paint)
        canvas.drawText("${stat.matchesPlayed}", PAGE_WIDTH * 0.5f, yPosition, paint)
        canvas.drawText("${String.format("%.1f", stat.totalTimeMinutes)}m", PAGE_WIDTH * 0.6f, yPosition, paint)
        canvas.drawText("${String.format("%.1f", stat.averageTimePerMatch)}m", PAGE_WIDTH * 0.72f, yPosition, paint)
        canvas.drawText("${stat.goalsScored}", PAGE_WIDTH * 0.85f, yPosition, paint)
        
        return yPosition
    }
}
