@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
@file:Suppress("ktlint:standard:no-wildcard-imports", "ktlint:standard:import-ordering")

package com.jesuslcorominas.teamflowmanager.di

import com.jesuslcorominas.teamflowmanager.domain.model.ExportData
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerExportStats
import com.jesuslcorominas.teamflowmanager.domain.utils.PdfExporter
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGContextAddLineToPoint
import platform.CoreGraphics.CGContextFillRect
import platform.CoreGraphics.CGContextMoveToPoint
import platform.CoreGraphics.CGContextRef
import platform.CoreGraphics.CGContextSetFillColorWithColor
import platform.CoreGraphics.CGContextSetLineWidth
import platform.CoreGraphics.CGContextSetStrokeColorWithColor
import platform.CoreGraphics.CGContextStrokePath
import platform.CoreGraphics.CGPointMake
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSFileManager
import platform.Foundation.NSMutableData
import platform.Foundation.NSString
import platform.Foundation.NSTemporaryDirectory
import platform.posix.time
import platform.UIKit.* // UIKit categories on NSString (drawAtPoint, sizeWithAttributes) and UIGraphics PDF API

/**
 * iOS counterpart of the Android `PdfExporterImpl`: renders the team statistics (player table,
 * top scorers and match results) into an A4 PDF with UIKit, saves it in the temporary directory
 * and returns its path, which the Analysis screen hands to the share sheet.
 */
internal class IosPdfExporterImpl : PdfExporter {
    companion object {
        private const val PAGE_W = 595.0
        private const val PAGE_H = 842.0
        private const val MARGIN = 40.0
        private const val LINE_H = 18.0
        private const val TITLE_SIZE = 18.0
        private const val SECTION_SIZE = 13.0
        private const val BODY_SIZE = 10.0
        private const val ROW_H = 22.0
        private const val HEADER_ROW_H = 24.0
        private const val MAX_TOP_SCORERS = 10
        private const val MAX_NAME_LENGTH = 30

        /** Seconds between the Unix epoch (1970) and NSDate's reference date (2001). */
        private const val REFERENCE_DATE_OFFSET_SECS = 978307200.0

        /** Column widths as a fraction of the table width: Jugador, Conv, Jug, T.Total, T.Medio, Goles. */
        private val COLUMN_RATIOS = listOf(0.35, 0.10, 0.10, 0.20, 0.15, 0.10)
    }

    override fun exportToPdf(
        exportData: ExportData,
        teamName: String,
    ): String? =
        try {
            val data = NSMutableData()
            UIGraphicsBeginPDFContextToData(data, CGRectMake(0.0, 0.0, PAGE_W, PAGE_H), null)

            // ── Page 1: header + player statistics ───────────────────────
            UIGraphicsBeginPDFPage()
            var y = MARGIN
            y = drawCentered("Estadísticas del Equipo", y, UIFont.boldSystemFontOfSize(TITLE_SIZE))
            y = drawCentered(teamName, y + 4.0, UIFont.systemFontOfSize(SECTION_SIZE))
            val today = formatDate(time(null).toLong() * 1000L, "dd/MM/yyyy")
            y = drawCentered("Fecha: $today", y + 4.0, UIFont.systemFontOfSize(BODY_SIZE))
            y += LINE_H * 1.5

            y = drawSectionTitle("Estadísticas de Jugadores", y)
            y += LINE_H * 0.5

            if (exportData.playerStats.isEmpty()) {
                y = drawBody("No hay datos de jugadores disponibles", y)
            } else {
                y = drawPlayerTableHeader(y)
                exportData.playerStats.forEachIndexed { index, stat ->
                    if (y + ROW_H > PAGE_H - MARGIN) {
                        UIGraphicsBeginPDFPage()
                        y = drawPlayerTableHeader(MARGIN)
                    }
                    y = drawPlayerRow(stat, y, index)
                }
            }

            // ── Page 2: top scorers + match results ──────────────────────
            UIGraphicsBeginPDFPage()
            y = MARGIN
            y = drawSectionTitle("Goleadores", y)
            y += LINE_H * 0.5

            if (exportData.topScorers.isEmpty()) {
                y = drawBody("No hay goles registrados", y)
            } else {
                exportData.topScorers.take(MAX_TOP_SCORERS).forEachIndexed { index, scorer ->
                    y = drawBody("${index + 1}. ${scorer.player.displayName} - ${scorer.totalGoals} gol(es)", y)
                }
            }
            y += LINE_H

            if (y + LINE_H * 3 > PAGE_H - MARGIN) {
                UIGraphicsBeginPDFPage()
                y = MARGIN
            }
            y = drawSectionTitle("Resultados de Partidos", y)
            y += LINE_H * 0.5

            if (exportData.matchResults.isEmpty()) {
                drawBody("No hay partidos finalizados", y)
            } else {
                exportData.matchResults.forEach { result ->
                    if (y + LINE_H > PAGE_H - MARGIN) {
                        UIGraphicsBeginPDFPage()
                        y = MARGIN
                    }
                    val date = formatDate(result.date, "dd/MM/yyyy")
                    val score = "${result.teamGoals} - ${result.opponentGoals}"
                    y = drawBody("$date | ${result.opponent} ($score) - ${result.location}", y)
                }
            }

            UIGraphicsEndPDFContext()

            val filePath = NSTemporaryDirectory() + "estadisticas_${time(null)}.pdf"
            val saved = NSFileManager.defaultManager().createFileAtPath(filePath, contents = data, attributes = null)
            if (saved) filePath else null
        } catch (_: Exception) {
            null
        }

    // ── Drawing helpers ───────────────────────────────────────────────────

    private fun drawCentered(
        text: String,
        y: Double,
        font: UIFont,
    ): Double {
        drawText(text, (PAGE_W - measureWidth(text, font)) / 2, y, font)
        return y + font.lineHeight
    }

    private fun drawBody(
        text: String,
        y: Double,
    ): Double {
        drawText(text, MARGIN, y, UIFont.systemFontOfSize(BODY_SIZE))
        return y + LINE_H
    }

    private fun drawSectionTitle(
        text: String,
        y: Double,
    ): Double {
        UIGraphicsGetCurrentContext()?.let { ctx ->
            CGContextSetFillColorWithColor(ctx, UIColor(red = 0.0, green = 0.2, blue = 0.4, alpha = 1.0).CGColor)
            CGContextFillRect(ctx, CGRectMake(MARGIN, y - 2.0, PAGE_W - MARGIN * 2, SECTION_SIZE + 6.0))
        }
        drawText(text, MARGIN + 4.0, y, UIFont.boldSystemFontOfSize(SECTION_SIZE), UIColor.whiteColor)
        return y + SECTION_SIZE + 8.0
    }

    private fun drawPlayerTableHeader(y: Double): Double {
        val tableW = PAGE_W - MARGIN * 2
        UIGraphicsGetCurrentContext()?.let { ctx ->
            CGContextSetFillColorWithColor(ctx, UIColor(red = 0.86, green = 0.86, blue = 0.86, alpha = 1.0).CGColor)
            CGContextFillRect(ctx, CGRectMake(MARGIN, y, tableW, HEADER_ROW_H))
            drawTableBorders(ctx, y, HEADER_ROW_H, tableW)
        }
        val font = UIFont.boldSystemFontOfSize(BODY_SIZE)
        val textY = y + (HEADER_ROW_H - font.lineHeight) / 2
        val headers = listOf("JUGADOR", "CONV", "JUG", "T.TOTAL", "T.MEDIO", "GOLES")
        columnXPositions(tableW).zip(headers).forEach { (x, h) -> drawText(h, x + 5.0, textY, font) }
        return y + HEADER_ROW_H
    }

    private fun drawPlayerRow(
        stat: PlayerExportStats,
        y: Double,
        index: Int,
    ): Double {
        val tableW = PAGE_W - MARGIN * 2
        UIGraphicsGetCurrentContext()?.let { ctx ->
            val bg =
                if (index % 2 == 0) {
                    UIColor.whiteColor
                } else {
                    UIColor(red = 0.96, green = 0.96, blue = 0.96, alpha = 1.0)
                }
            CGContextSetFillColorWithColor(ctx, bg.CGColor)
            CGContextFillRect(ctx, CGRectMake(MARGIN, y, tableW, ROW_H))
            drawTableBorders(ctx, y, ROW_H, tableW)
        }
        val font = UIFont.systemFontOfSize(BODY_SIZE)
        val textY = y + (ROW_H - font.lineHeight) / 2
        val name = stat.player.displayName
        val values =
            listOf(
                if (name.length > MAX_NAME_LENGTH) name.take(MAX_NAME_LENGTH) + "…" else name,
                "${stat.matchesCalledUp}",
                "${stat.matchesPlayed}",
                formatDuration(stat.totalTimeMinutes),
                formatDuration(stat.averageTimePerMatch),
                "${stat.goalsScored}",
            )
        columnXPositions(tableW).zip(values).forEach { (x, v) -> drawText(v, x + 5.0, textY, font) }
        return y + ROW_H
    }

    private fun columnXPositions(tableW: Double): List<Double> = COLUMN_RATIOS.runningFold(MARGIN) { x, ratio -> x + ratio * tableW }.dropLast(1)

    private fun drawTableBorders(
        ctx: CGContextRef,
        y: Double,
        h: Double,
        tableW: Double,
    ) {
        CGContextSetStrokeColorWithColor(ctx, UIColor.blackColor.CGColor)
        CGContextSetLineWidth(ctx, 0.5)
        val right = MARGIN + tableW
        line(ctx, MARGIN, y, right, y)
        line(ctx, MARGIN, y + h, right, y + h)
        line(ctx, MARGIN, y, MARGIN, y + h)
        line(ctx, right, y, right, y + h)
        columnXPositions(tableW).drop(1).forEach { x -> line(ctx, x, y, x, y + h) }
        CGContextStrokePath(ctx)
    }

    private fun line(
        ctx: CGContextRef,
        x1: Double,
        y1: Double,
        x2: Double,
        y2: Double,
    ) {
        CGContextMoveToPoint(ctx, x1, y1)
        CGContextAddLineToPoint(ctx, x2, y2)
    }

    private fun drawText(
        text: String,
        x: Double,
        y: Double,
        font: UIFont,
        color: UIColor = UIColor.blackColor,
    ) {
        val attrs =
            mapOf<Any?, Any>(
                NSFontAttributeName to font,
                NSForegroundColorAttributeName to color,
            )
        (text as NSString).drawAtPoint(CGPointMake(x, y), withAttributes = attrs)
    }

    private fun measureWidth(
        text: String,
        font: UIFont,
    ): Double {
        val attrs = mapOf<Any?, Any>(NSFontAttributeName to font)
        return (text as NSString).sizeWithAttributes(attrs).useContents { width }
    }

    // ── Format helpers ────────────────────────────────────────────────────

    /** Same output as Android's `PdfExporterImpl.formatTime`: "1h 05m 03s", "5m 03s" or "42s". */
    private fun formatDuration(totalMinutes: Double): String {
        val totalSeconds = (totalMinutes * 60).toInt()
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        val mm = minutes.toString().padStart(2, '0')
        val ss = seconds.toString().padStart(2, '0')
        return when {
            hours > 0 -> "${hours}h ${mm}m ${ss}s"
            minutes > 0 -> "${minutes}m ${ss}s"
            else -> "${seconds}s"
        }
    }

    private fun formatDate(
        epochMillis: Long,
        pattern: String,
    ): String {
        val date = NSDate(timeIntervalSinceReferenceDate = epochMillis / 1000.0 - REFERENCE_DATE_OFFSET_SECS)
        val formatter = NSDateFormatter()
        formatter.dateFormat = pattern
        return formatter.stringFromDate(date)
    }
}
