package com.jesuslcorominas.teamflowmanager.domain.utils

/**
 * Interface for providing time values synchronized with server time.
 * This helps avoid time discrepancies between different devices.
 */
interface TimeProvider {
    /**
     * Get the current time in milliseconds, adjusted for server time offset.
     * This should be used instead of System.currentTimeMillis() for match timing.
     */
    fun getCurrentTime(): Long

    /**
     * Synchronize with server time to calculate offset.
     * Should be called periodically to maintain accuracy.
     */
    suspend fun synchronize()

    /**
     * Get the current server time offset in milliseconds.
     * Positive values mean server is ahead of device.
     */
    fun getOffset(): Long
}
