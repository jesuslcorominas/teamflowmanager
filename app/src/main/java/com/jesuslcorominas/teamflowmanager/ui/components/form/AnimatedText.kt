package com.jesuslcorominas.teamflowmanager.ui.components.form

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.lerp

@Composable
fun AnimatedText(
    text: String,
    start: TextStyle = MaterialTheme.typography.bodyLarge,
    end: TextStyle = MaterialTheme.typography.displayMedium,
    fontWeight: FontWeight = FontWeight.Bold,
    color: Color = MaterialTheme.colorScheme.onSurface,
    expanded: Boolean,
) {
    val transition = updateTransition(targetState = expanded, label = "textStyleTransition")
    val fraction by transition.animateFloat(label = "fraction") { if (it) 1f else 0f }

    val animatedStyle = lerpTextStyle(start, end, fraction)

    Text(
        text = text,
        style = animatedStyle,
        fontWeight = fontWeight,
        color = color,
    )
}

@Composable
private fun lerpTextStyle(
    start: TextStyle,
    end: TextStyle,
    fraction: Float,
): TextStyle {
    return TextStyle(
        fontSize = lerp(start.fontSize, end.fontSize, fraction),
        lineHeight = lerp(start.lineHeight, end.lineHeight, fraction),
        letterSpacing = lerp(start.letterSpacing, end.letterSpacing, fraction),
        fontWeight = if (fraction < 0.5f) start.fontWeight else end.fontWeight,
        fontFamily = start.fontFamily ?: end.fontFamily,
    )
}
