package com.jesuslcorominas.teamflowmanager.ui

import androidx.compose.runtime.Composable

/**
 * Displays the TeamFlow Manager app icon.
 *
 * - Android: renders the branded vector drawable (ic_launcher).
 * - iOS: renders a styled "TFM" text placeholder.
 *   TODO (KMP-19+): export icon as CMP SVG/PNG resource to unify both platforms.
 */
@Composable
expect fun TeamFlowManagerIcon()
