package com.jesuslcorominas.teamflowmanager.domain.analytics

/**
 * Analytics tracker interface for logging events and user properties.
 * This interface is platform-agnostic and can be implemented with different analytics providers.
 * KMM-ready: Can be implemented as expect/actual for different platforms.
 */
interface AnalyticsTracker {
    /**
     * Log a custom event with optional parameters.
     *
     * @param eventName Name of the event to log (e.g., "team_created", "match_started")
     * @param params Optional map of parameters to include with the event
     */
    fun logEvent(eventName: String, params: Map<String, Any> = emptyMap())

    /**
     * Log a screen view event.
     *
     * @param screenName Name of the screen being viewed
     * @param screenClass Optional class name of the screen
     */
    fun logScreenView(screenName: String, screenClass: String? = null)

    /**
     * Set a user ID for analytics.
     *
     * @param userId User identifier (should be anonymized)
     */
    fun setUserId(userId: String?)

    /**
     * Set a user property.
     *
     * @param key Property key
     * @param value Property value
     */
    fun setUserProperty(key: String, value: String?)
}
