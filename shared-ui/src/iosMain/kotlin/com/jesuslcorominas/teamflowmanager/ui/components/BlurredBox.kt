package com.jesuslcorominas.teamflowmanager.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIBlurEffect
import platform.UIKit.UIBlurEffectStyle
import platform.UIKit.UIVisualEffectView

/**
 * iOS actual: a native UIVisualEffectView with a dark blur effect.
 * This truly blurs the Compose content rendered beneath it in the UIKit layer.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun BlurredBox(modifier: Modifier) {
    UIKitView(
        factory = {
            UIVisualEffectView(
                effect = UIBlurEffect.effectWithStyle(UIBlurEffectStyle.UIBlurEffectStyleDark)
            )
        },
        modifier = modifier,
    )
}
