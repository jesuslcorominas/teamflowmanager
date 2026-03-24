package com.jesuslcorominas.teamflowmanager.ui.analytics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker
import org.koin.compose.koinInject

/**
 * Composable to track screen views automatically.
 * Uses the Koin-injected [AnalyticsTracker] (no-op on iOS for now; Firebase on Android).
 * Should be called once per screen, typically at the top of the composable.
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
