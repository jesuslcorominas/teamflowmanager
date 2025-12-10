package com.jesuslcorominas.teamflowmanager.usecase.repository

import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTime
import kotlinx.coroutines.flow.Flow

interface PlayerTimeRepository {
    fun getPlayerTime(playerId: Long): Flow<PlayerTime?>

    fun getAllPlayerTimes(): Flow<List<PlayerTime>>

    suspend fun startTimer(
        playerId: Long,
        currentTimeMillis: Long,
    )

    suspend fun pauseTimer(
        playerId: Long,
        currentTimeMillis: Long,
    )

    suspend fun pauseTimerForMatchPause(
        playerId: Long,
        currentTimeMillis: Long,
    )

    /**
     * Start timers for multiple players at once using batch write.
     * All timers start with the same timestamp for synchronization.
     * @param playerIds List of player IDs to start timers for
     * @param currentTimeMillis The current time in milliseconds
     */
    suspend fun startTimersBatch(
        playerIds: List<Long>,
        currentTimeMillis: Long,
    )

    suspend fun resetAllPlayerTimes()

    /**
     * Get all local player times directly (not as a Flow) for migration purposes.
     * @return List of all player times
     */
    suspend fun getAllLocalPlayerTimesDirect(): List<PlayerTime>

    /**
     * Clear local player time data from Room database.
     * Used after successful migration to Firestore.
     */
    suspend fun clearLocalPlayerTimeData()
}
