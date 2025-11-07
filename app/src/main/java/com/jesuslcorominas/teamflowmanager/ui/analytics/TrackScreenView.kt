package com.jesuslcorominas.teamflowmanager.ui.analytics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker
import org.koin.compose.koinInject

/**
 * Composable to track screen views automatically.
 * Should be called once per screen, typically at the top level of the screen composable.
 *
 * @param screenName Name of the screen being viewed
 * @param screenClass Optional class name of the screen
 * @param analyticsTracker Analytics tracker instance (injected by Koin)
 *
 * Example usage:
 * ```
 * @Composable
 * fun MyScreen() {
 *     TrackScreenView(screenName = "My Screen", screenClass = "MyScreen")
 *     // Rest of screen content
 * }
 * ```
 */
@Composable
fun TrackScreenView(
    screenName: String,
    screenClass: String? = null,
    analyticsTracker: AnalyticsTracker = koinInject(),
) {
    LaunchedEffect(screenName) {
        analyticsTracker.logScreenView(screenName, screenClass)
    }
}
