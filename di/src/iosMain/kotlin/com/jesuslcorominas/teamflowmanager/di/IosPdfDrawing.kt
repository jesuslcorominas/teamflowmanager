@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
@file:Suppress("ktlint:standard:no-wildcard-imports", "ktlint:standard:import-ordering")

package com.jesuslcorominas.teamflowmanager.di

import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGContextFillRect
import platform.CoreGraphics.CGContextSetFillColorWithColor
import platform.CoreGraphics.CGPointMake
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSString
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.timeIntervalSince1970
import platform.UIKit.* // UIKit categories on NSString (drawAtPoint, sizeWithAttributes) and UIGraphics API

/**
 * Drawing and file helpers shared by the iOS PDF exporters (match report and team statistics).
 * Every page is A4 with the same margin, and every section title uses the same band style.
 */
internal object IosPdfDrawing {
    const val PAGE_W = 595.0
    const val PAGE_H = 842.0
    const val MARGIN = 40.0
    const val SECTION_SIZE = 13.0

    /** Seconds between the Unix epoch (1970) and NSDate's reference date (2001). */
    private const val REFERENCE_DATE_OFFSET_SECS = 978307200.0

    fun drawText(
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
        // NSString UIKit category: drawAtPoint:withAttributes:
        (text as NSString).drawAtPoint(CGPointMake(x, y), withAttributes = attrs)
    }

    fun measureWidth(
        text: String,
        font: UIFont,
    ): Double {
        val attrs = mapOf<Any?, Any>(NSFontAttributeName to font)
        return (text as NSString).sizeWithAttributes(attrs).useContents { width }
    }

    /** Draws a full-width dark band with the title in white and returns the next y. */
    fun drawSectionTitle(
        text: String,
        y: Double,
    ): Double {
        val ctx = UIGraphicsGetCurrentContext()
        if (ctx != null) {
            CGContextSetFillColorWithColor(ctx, UIColor(red = 0.0, green = 0.2, blue = 0.4, alpha = 1.0).CGColor)
            CGContextFillRect(ctx, CGRectMake(MARGIN, y - 2.0, PAGE_W - MARGIN * 2, SECTION_SIZE + 6.0))
        }
        drawText(text, MARGIN + 4.0, y, UIFont.boldSystemFontOfSize(SECTION_SIZE), UIColor.whiteColor)
        return y + SECTION_SIZE + 8.0
    }

    fun formatDate(
        epochMillis: Long,
        pattern: String,
    ): String {
        // NSDate(timeIntervalSinceReferenceDate:) counts from 2001, not from the Unix epoch.
        val date = NSDate(timeIntervalSinceReferenceDate = epochMillis / 1000.0 - REFERENCE_DATE_OFFSET_SECS)
        val formatter = NSDateFormatter()
        formatter.dateFormat = pattern
        return formatter.stringFromDate(date)
    }

    /**
     * Path in the temporary directory for a new PDF. Uses millisecond resolution so two exports
     * in the same second do not end up writing the same file.
     */
    fun newTempPdfPath(baseName: String): String {
        val millis = (NSDate().timeIntervalSince1970 * 1000).toLong()
        return NSTemporaryDirectory() + "${baseName}_$millis.pdf"
    }
}
