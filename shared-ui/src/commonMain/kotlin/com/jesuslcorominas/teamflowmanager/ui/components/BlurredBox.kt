package com.jesuslcorominas.teamflowmanager.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A composable that renders a blurred/frosted-glass overlay.
 *
 * On iOS: uses a native UIVisualEffectView so the content behind it is truly blurred.
 * On Android: falls back to a semi-transparent gradient approximation.
 */
@Composable
expect fun BlurredBox(modifier: Modifier = Modifier)
