package com.jesuslcorominas.teamflowmanager.analytics

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.jesuslcorominas.teamflowmanager.domain.analytics.CrashReporter

/**
 * Firebase implementation of CrashReporter.
 * Wraps Firebase Crashlytics for crash and error reporting.
 */
class FirebaseCrashReporter(
    private val crashlytics: FirebaseCrashlytics,
) : CrashReporter {
    override fun recordException(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }

    override fun log(message: String) {
        crashlytics.log(message)
    }

    override fun setCustomKey(key: String, value: String) {
        crashlytics.setCustomKey(key, value)
    }

    override fun setCustomKey(key: String, value: Int) {
        crashlytics.setCustomKey(key, value)
    }

    override fun setCustomKey(key: String, value: Boolean) {
        crashlytics.setCustomKey(key, value)
    }
}
