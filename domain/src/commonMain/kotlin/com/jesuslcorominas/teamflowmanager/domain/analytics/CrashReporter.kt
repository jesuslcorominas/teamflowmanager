package com.jesuslcorominas.teamflowmanager.domain.analytics

/**
 * Crash reporter interface for logging errors and exceptions.
 * This interface is platform-agnostic and can be implemented with different crash reporting providers.
 * KMM-ready: Can be implemented as expect/actual for different platforms.
 */
interface CrashReporter {
    /**
     * Record a non-fatal exception.
     *
     * @param throwable The exception to record
     */
    fun recordException(throwable: Throwable)

    /**
     * Log a message to be included in crash reports.
     *
     * @param message Message to log
     */
    fun log(message: String)

    /**
     * Set a custom key-value pair to be included in crash reports.
     *
     * @param key Key for the custom attribute
     * @param value Value for the custom attribute
     */
    fun setCustomKey(
        key: String,
        value: String,
    )

    /**
     * Set a custom key-value pair to be included in crash reports.
     *
     * @param key Key for the custom attribute
     * @param value Value for the custom attribute
     */
    fun setCustomKey(
        key: String,
        value: Int,
    )

    /**
     * Set a custom key-value pair to be included in crash reports.
     *
     * @param key Key for the custom attribute
     * @param value Value for the custom attribute
     */
    fun setCustomKey(
        key: String,
        value: Boolean,
    )
}
