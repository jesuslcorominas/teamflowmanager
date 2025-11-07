package com.jesuslcorominas.teamflowmanager.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker

/**
 * Firebase implementation of AnalyticsTracker.
 * Wraps Firebase Analytics for event tracking.
 */
class FirebaseAnalyticsTracker(
    private val firebaseAnalytics: FirebaseAnalytics,
) : AnalyticsTracker {
    override fun logEvent(eventName: String, params: Map<String, Any>) {
        val bundle = Bundle().apply {
            params.forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Double -> putDouble(key, value)
                    is Boolean -> putBoolean(key, value)
                    else -> putString(key, value.toString())
                }
            }
        }
        firebaseAnalytics.logEvent(eventName, bundle)
    }

    override fun logScreenView(screenName: String, screenClass: String?) {
        val params = mutableMapOf<String, Any>(
            FirebaseAnalytics.Param.SCREEN_NAME to screenName,
        )
        screenClass?.let {
            params[FirebaseAnalytics.Param.SCREEN_CLASS] = it
        }
        logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, params)
    }

    override fun setUserId(userId: String?) {
        firebaseAnalytics.setUserId(userId)
    }

    override fun setUserProperty(key: String, value: String?) {
        firebaseAnalytics.setUserProperty(key, value)
    }
}
