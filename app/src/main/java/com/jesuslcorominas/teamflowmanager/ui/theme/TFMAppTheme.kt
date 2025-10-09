package com.jesuslcorominas.teamflowmanager.ui.theme

import LocalSpacing
import TFMSpacing
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun TFMAppTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalSpacing provides TFMSpacing,
    ) {
        MaterialTheme(
            colorScheme = LightColorScheme,
            typography = Typography(),
            shapes = Shapes(),
            content = content,
        )
    }
}
