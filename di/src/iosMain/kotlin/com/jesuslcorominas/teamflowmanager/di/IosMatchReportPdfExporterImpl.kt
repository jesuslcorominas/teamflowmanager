@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.jesuslcorominas.teamflowmanager.di

import com.jesuslcorominas.teamflowmanager.domain.model.MatchReportData
import com.jesuslcorominas.teamflowmanager.domain.model.TimelineEvent
import com.jesuslcorominas.teamflowmanager.domain.utils.MatchReportPdfExporter
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGContextAddLineToPoint
import platform.CoreGraphics.CGContextFillRect
import platform.CoreGraphics.CGContextMoveToPoint
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
// UIKit.* wildcard pulls in UIKit categories on NSString/NSAttributedString
// (drawAtPoint, drawInRect, sizeWithAttributes, etc.)
import platform.UIKit.*

internal class IosMatchReportPdfExporterImpl : MatchReportPdfExporter {

    companion object {
        private const val PAGE_W = 595.0
        private const val PAGE_H = 842.0
        private const val MARGIN = 40.0
        private const val LINE_H = 18.0
        private const val TITLE_SIZE = 18.0
        private const val SECTION_SIZE = 13.0
        private const val BODY_SIZE = 10.0
        private const val SMALL_SIZE = 8.5
        private const val ROW_H = 20.0
        private const val HEADER_ROW_H = 22.0
    }

    override fun exportMatchReportToPdf(matchReportData: MatchReportData): String? {
        return try {
            val data = NSMutableData()
            UIGraphicsBeginPDFContextToData(data, CGRectMake(0.0, 0.0, PAGE_W, PAGE_H), null)

            // ── Page 1: Match info + Player table ─────────────────────────
            UIGraphicsBeginPDFPage()
            var y = MARGIN

            y = drawTitle("Informe de Partido", y)
            y += LINE_H

            val match = matchReportData.match
            val dateStr = match.dateTime?.let { formatDate(it) } ?: "-"

            y = drawSectionTitle("Información del Partido", y)
            y += LINE_H * 0.5
            y = drawLabelValue("Rival", match.opponent, y)
            y = drawLabelValue("Fecha", dateStr, y)
            y = drawLabelValue("Lugar", match.location, y)
            y = drawLabelValue("Resultado", "${match.goals} - ${match.opponentGoals}", y)
            y += LINE_H

            y = drawSectionTitle("Jugadores", y)
            y += LINE_H * 0.5
            y = drawPlayerTableHeader(y)

            matchReportData.playerReports.forEachIndexed { index, report ->
                if (y + ROW_H > PAGE_H - MARGIN) {
                    UIGraphicsBeginPDFPage()
                    y = MARGIN
                    y = drawPlayerTableHeader(y)
                }
                y = drawPlayerRow(report, y, index)
            }

            // ── Page 2: Timeline ──────────────────────────────────────────
            if (matchReportData.timelineEvents.isNotEmpty()) {
                UIGraphicsBeginPDFPage()
                y = MARGIN
                y = drawSectionTitle("Línea de Tiempo", y)
                y += LINE_H * 0.5

                matchReportData.timelineEvents.forEach { event ->
                    if (y + LINE_H * 2 > PAGE_H - MARGIN) {
                        UIGraphicsBeginPDFPage()
                        y = MARGIN
                    }
                    y = drawTimelineRow(event, y)
                }
            }

            UIGraphicsEndPDFContext()

            // Save to temp dir
            val opponentClean = match.opponent.replace(" ", "_").replace("/", "-")
            val ts = time(null)
            val fileName = "partido_${opponentClean}_$ts.pdf"
            val filePath = NSTemporaryDirectory() + fileName
            NSFileManager.defaultManager().createFileAtPath(filePath, contents = data, attributes = null)
            filePath
        } catch (_: Exception) {
            null
        }
    }

    // ── Drawing helpers ───────────────────────────────────────────────────

    private fun drawTitle(text: String, y: Double): Double {
        val font = UIFont.boldSystemFontOfSize(TITLE_SIZE)
        val width = measureWidth(text, font)
        drawText(text, (PAGE_W - width) / 2, y, font)
        return y + TITLE_SIZE + LINE_H * 0.3
    }

    private fun drawSectionTitle(text: String, y: Double): Double {
        val ctx = UIGraphicsGetCurrentContext()
        if (ctx != null) {
            CGContextSetFillColorWithColor(ctx, UIColor(red = 0.0, green = 0.2, blue = 0.4, alpha = 1.0).CGColor)
            CGContextFillRect(ctx, CGRectMake(MARGIN, y - 2.0, PAGE_W - MARGIN * 2, SECTION_SIZE + 6.0))
        }
        drawText(text, MARGIN + 4.0, y, UIFont.boldSystemFontOfSize(SECTION_SIZE), UIColor.whiteColor)
        return y + SECTION_SIZE + 8.0
    }

    private fun drawLabelValue(label: String, value: String, y: Double): Double {
        drawText("$label:", MARGIN, y, UIFont.boldSystemFontOfSize(BODY_SIZE))
        drawText(value, MARGIN + 110.0, y, UIFont.systemFontOfSize(BODY_SIZE))
        return y + LINE_H
    }

    private fun drawPlayerTableHeader(y: Double): Double {
        val ctx = UIGraphicsGetCurrentContext()
        val tableW = PAGE_W - MARGIN * 2
        if (ctx != null) {
            CGContextSetFillColorWithColor(ctx, UIColor(red = 0.85, green = 0.85, blue = 0.85, alpha = 1.0).CGColor)
            CGContextFillRect(ctx, CGRectMake(MARGIN, y, tableW, HEADER_ROW_H))
            drawTableBorders(ctx, y, HEADER_ROW_H, tableW)
        }
        val font = UIFont.boldSystemFontOfSize(SMALL_SIZE)
        val textY = y + (HEADER_ROW_H - SMALL_SIZE) / 2
        val headers = listOf("#", "Jugador", "PO", "C", "T", "Tiempo", "Goles")
        columnXPositions(tableW).zip(headers).forEach { (x, h) ->
            drawText(h, x + 3.0, textY, font)
        }
        return y + HEADER_ROW_H
    }

    private fun drawPlayerRow(
        report: com.jesuslcorominas.teamflowmanager.domain.model.PlayerMatchReport,
        y: Double,
        index: Int,
    ): Double {
        val ctx = UIGraphicsGetCurrentContext()
        val tableW = PAGE_W - MARGIN * 2
        val textY = y + (ROW_H - SMALL_SIZE) / 2
        if (ctx != null) {
            val bg = if (index % 2 == 0) UIColor.whiteColor else UIColor(red = 0.96, green = 0.96, blue = 0.96, alpha = 1.0)
            CGContextSetFillColorWithColor(ctx, bg.CGColor)
            CGContextFillRect(ctx, CGRectMake(MARGIN, y, tableW, ROW_H))
            drawTableBorders(ctx, y, ROW_H, tableW)
        }
        val font = UIFont.systemFontOfSize(SMALL_SIZE)
        val name = "${report.player.firstName} ${report.player.lastName}"
        val values = listOf(
            "${report.number}",
            if (name.length > 22) name.take(22) + "…" else name,
            if (report.isGoalkeeper) "X" else "-",
            if (report.isCaptain) "X" else "-",
            if (report.isStarter) "X" else "-",
            formatTime(report.totalPlayTimeMillis),
            if (report.goals.isNotEmpty()) "${report.goals.size}" else "-",
        )
        columnXPositions(tableW).zip(values).forEach { (x, v) ->
            drawText(v, x + 3.0, textY, font)
        }
        return y + ROW_H
    }

    private fun drawTimelineRow(event: TimelineEvent, y: Double): Double {
        drawText(formatTimeMinutes(event.matchElapsedTimeMillis), MARGIN, y, UIFont.boldSystemFontOfSize(BODY_SIZE), UIColor.darkGrayColor)
        drawText(timelineEventText(event), MARGIN + 40.0, y, UIFont.systemFontOfSize(BODY_SIZE))
        val ctx = UIGraphicsGetCurrentContext()
        if (ctx != null) {
            CGContextSetStrokeColorWithColor(ctx, UIColor(red = 0.9, green = 0.9, blue = 0.9, alpha = 1.0).CGColor)
            CGContextSetLineWidth(ctx, 0.5)
            CGContextMoveToPoint(ctx, MARGIN, y + LINE_H + 2.0)
            CGContextAddLineToPoint(ctx, PAGE_W - MARGIN, y + LINE_H + 2.0)
            CGContextStrokePath(ctx)
        }
        return y + LINE_H + 6.0
    }

    // ── Table helpers ─────────────────────────────────────────────────────

    /** Returns the X start of each column: #, Jugador, PO, C, T, Tiempo, Goles */
    private fun columnXPositions(tableW: Double): List<Double> {
        val widths = listOf(0.07, 0.35, 0.07, 0.07, 0.07, 0.22, 0.15).map { it * tableW }
        val positions = mutableListOf<Double>()
        var x = MARGIN
        widths.forEach { w -> positions.add(x); x += w }
        return positions
    }

    private fun drawTableBorders(ctx: Any, y: Double, h: Double, tableW: Double) {
        @Suppress("UNCHECKED_CAST")
        val cgCtx = ctx as platform.CoreGraphics.CGContextRef
        CGContextSetStrokeColorWithColor(cgCtx, UIColor(red = 0.7, green = 0.7, blue = 0.7, alpha = 1.0).CGColor)
        CGContextSetLineWidth(cgCtx, 0.5)
        CGContextMoveToPoint(cgCtx, MARGIN, y); CGContextAddLineToPoint(cgCtx, MARGIN + tableW, y)
        CGContextMoveToPoint(cgCtx, MARGIN, y + h); CGContextAddLineToPoint(cgCtx, MARGIN + tableW, y + h)
        CGContextMoveToPoint(cgCtx, MARGIN, y); CGContextAddLineToPoint(cgCtx, MARGIN, y + h)
        CGContextMoveToPoint(cgCtx, MARGIN + tableW, y); CGContextAddLineToPoint(cgCtx, MARGIN + tableW, y + h)
        var x = MARGIN
        listOf(0.07, 0.35, 0.07, 0.07, 0.07, 0.22).forEach { ratio ->
            x += ratio * tableW
            CGContextMoveToPoint(cgCtx, x, y); CGContextAddLineToPoint(cgCtx, x, y + h)
        }
        CGContextStrokePath(cgCtx)
    }

    // ── Text helpers ──────────────────────────────────────────────────────

    private fun drawText(
        text: String,
        x: Double,
        y: Double,
        font: UIFont,
        color: UIColor = UIColor.blackColor,
    ) {
        val attrs = mapOf<Any?, Any>(
            NSFontAttributeName to font,
            NSForegroundColorAttributeName to color,
        )
        // Use NSString UIKit category: drawAtPoint:withAttributes: (2-param version)
        (text as NSString).drawAtPoint(CGPointMake(x, y), withAttributes = attrs)
    }

    private fun measureWidth(text: String, font: UIFont): Double {
        val attrs = mapOf<Any?, Any>(NSFontAttributeName to font)
        return (text as NSString).sizeWithAttributes(attrs).useContents { width }
    }

    // ── Format helpers ────────────────────────────────────────────────────

    private fun formatTime(millis: Long): String {
        val totalSec = (millis / 1000).toInt()
        return "${totalSec / 60}:${(totalSec % 60).toString().padStart(2, '0')}"
    }

    private fun formatTimeMinutes(millis: Long): String = "${millis / 60000}'"

    private fun formatDate(millis: Long): String {
        // NSDate constructor takes timeIntervalSinceReferenceDate (Jan 1 2001).
        // Offset from Unix epoch (Jan 1 1970) = 978307200 seconds.
        val secsSinceRef = millis / 1000.0 - 978307200.0
        val date = NSDate(timeIntervalSinceReferenceDate = secsSinceRef)
        val fmt = NSDateFormatter()
        fmt.dateFormat = "dd/MM/yyyy HH:mm"
        return fmt.stringFromDate(date)
    }

    private fun timelineEventText(event: TimelineEvent): String = when (event) {
        is TimelineEvent.StartingLineup -> {
            val names = event.players.take(5).joinToString(", ") { "${it.number}. ${it.firstName}" }
            val extra = if (event.players.size > 5) " (+${event.players.size - 5})" else ""
            "Alineación inicial: $names$extra"
        }
        is TimelineEvent.GoalScored -> {
            val score = "(${event.teamScore}-${event.opponentScore})"
            if (event.isOpponentGoal) "Gol rival $score"
            else {
                val scorer = event.scorer?.let { "${it.firstName} ${it.lastName}" } ?: "Gol en propia"
                "Gol: $scorer $score"
            }
        }
        is TimelineEvent.Substitution ->
            "Cambio: ${event.playerIn.firstName} ↑  ${event.playerOut.firstName} ↓"
        is TimelineEvent.Timeout -> "Tiempo muerto"
        is TimelineEvent.PeriodBreak -> "Descanso (periodo ${event.periodNumber})"
    }
}
