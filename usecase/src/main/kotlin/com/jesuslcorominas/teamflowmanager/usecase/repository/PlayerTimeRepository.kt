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

    /**
     * Pause timers for multiple players at once using batch write for match pause.
     * All timers pause with the same timestamp and are marked as PAUSED.
     * @param playerIds List of player IDs to pause timers for
     * @param currentTimeMillis The current time in milliseconds
     */
    suspend fun pauseTimersBatch(
        playerIds: List<Long>,
        currentTimeMillis: Long,
    )

    /**
     * Start timers for multiple players with an operation ID for atomic operations
     * @param playerIds List of player IDs to start timers for
     * @param currentTimeMillis The current time in milliseconds
     * @param operationId The operation ID to track atomic operations
     */
    suspend fun startTimersBatchWithOperationId(
        playerIds: List<Long>,
        currentTimeMillis: Long,
        operationId: String,
    )

    /**
     * Pause timers for multiple players with an operation ID for atomic operations
     * @param playerIds List of player IDs to pause timers for
     * @param currentTimeMillis The current time in milliseconds
     * @param operationId The operation ID to track atomic operations
     */
    suspend fun pauseTimersBatchWithOperationId(
        playerIds: List<Long>,
        currentTimeMillis: Long,
        operationId: String,
    )

    suspend fun resetAllPlayerTimes()
}
