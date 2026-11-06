package com.jesuslcorominas.teamflowmanager.domain.analytics

/**
 * Reported through [CrashReporter] every time an abandoned match is auto-finished.
 *
 * It exists as its own type so the crash reporting backend groups every recovery under a single
 * issue instead of scattering them among unrelated non-fatals. The whole diagnostic context travels
 * in [message] as well as in the custom keys, because the message survives even when a later report
 * overwrites those keys.
 */
class AbandonedMatchRecoveredException(message: String) : Exception(message)
