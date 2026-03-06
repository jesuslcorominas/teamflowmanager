package com.jesuslcorominas.teamflowmanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.jesuslcorominas.teamflowmanager.ui.theme.BackgroundContrast

/**
 * Android actual: gradient approximation of frosted glass since Android
 * backdrop blur requires API 31+ and RenderEffect.
 */
@Composable
actual fun BlurredBox(modifier: Modifier) {
    Box(
        modifier = modifier.background(
            Brush.verticalGradient(
                0.00f to Color.Transparent,
                0.40f to BackgroundContrast.copy(alpha = 0.40f),
                1.00f to BackgroundContrast.copy(alpha = 0.90f),
            )
        )
    )
}
