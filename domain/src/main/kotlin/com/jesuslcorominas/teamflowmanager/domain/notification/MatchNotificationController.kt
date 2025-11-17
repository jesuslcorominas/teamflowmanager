package com.jesuslcorominas.teamflowmanager.domain.notification

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import kotlinx.coroutines.flow.Flow

/**
 * Controller interface for managing match notifications.
 * This interface is platform-agnostic and can be implemented with different notification systems.
 * KMM-ready: Can be implemented as expect/actual for different platforms.
 */
interface MatchNotificationController {
    /**
     * Get the currently active match (IN_PROGRESS, PAUSED, or TIMEOUT).
     * Returns a Flow that emits the active match or null if no match is active.
     */
    fun getActiveMatch(): Flow<Match?>

    /**
     * Pause the specified match.
     *
     * @param matchId ID of the match to pause
     * @param currentTimeMillis Current time in milliseconds
     */
    suspend fun pauseMatch(matchId: Long, currentTimeMillis: Long)

    /**
     * Resume the specified match.
     *
     * @param matchId ID of the match to resume
     * @param currentTimeMillis Current time in milliseconds
     */
    suspend fun resumeMatch(matchId: Long, currentTimeMillis: Long)

    /**
     * Start timeout for the specified match.
     *
     * @param matchId ID of the match
     * @param currentTimeMillis Current time in milliseconds
     */
    suspend fun startTimeout(matchId: Long, currentTimeMillis: Long)

    /**
     * End timeout for the specified match.
     *
     * @param matchId ID of the match
     * @param currentTimeMillis Current time in milliseconds
     */
    suspend fun endTimeout(matchId: Long, currentTimeMillis: Long)
}
