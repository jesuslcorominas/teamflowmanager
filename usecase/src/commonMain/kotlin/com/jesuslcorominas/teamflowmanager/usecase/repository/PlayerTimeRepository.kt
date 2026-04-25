package com.jesuslcorominas.teamflowmanager.usecase.repository

import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTime
import kotlinx.coroutines.flow.Flow

interface PlayerTimeRepository {
    fun getPlayerTime(playerId: Long): Flow<PlayerTime?>

    fun getPlayerTimesByMatch(
        matchId: Long,
        teamId: String? = null,
    ): Flow<List<PlayerTime>>

    suspend fun startTimer(
        matchId: Long,
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
     * @param matchId The ID of the current match — used to scope records to this match
     * @param playerIds List of player IDs to start timers for
     * @param currentTimeMillis The current time in milliseconds
     */
    suspend fun startTimersBatch(
        matchId: Long,
        playerIds: List<Long>,
        currentTimeMillis: Long,
    )

    /**
     * Pause timers for multiple players at once using batch write for match pause.
     * All timers pause with the same timestamp and are marked as PAUSED.
     * @param matchId The ID of the current match — used to scope records to this match
     * @param playerIds List of player IDs to pause timers for
     * @param currentTimeMillis The current time in milliseconds
     */
    suspend fun pauseTimersBatch(
        matchId: Long,
        playerIds: List<Long>,
        currentTimeMillis: Long,
    )

    /**
     * Start timers for multiple players with an operation ID for atomic operations
     * @param matchId The ID of the current match — used to scope records to this match
     * @param playerIds List of player IDs to start timers for
     * @param currentTimeMillis The current time in milliseconds
     * @param operationId The operation ID to track atomic operations
     */
    suspend fun startTimersBatchWithOperationId(
        matchId: Long,
        playerIds: List<Long>,
        currentTimeMillis: Long,
        operationId: String,
    )

    /**
     * Pause timers for multiple players with an operation ID for atomic operations
     * @param matchId The ID of the current match — used to scope records to this match
     * @param playerIds List of player IDs to pause timers for
     * @param currentTimeMillis The current time in milliseconds
     * @param operationId The operation ID to track atomic operations
     */
    suspend fun pauseTimersBatchWithOperationId(
        matchId: Long,
        playerIds: List<Long>,
        currentTimeMillis: Long,
        operationId: String,
    )

    /**
     * Stop timers for players being substituted out with an operation ID for atomic operations.
     * Sets player status to ON_BENCH (not PAUSED) so they won't restart when match resumes.
     * @param matchId The ID of the current match — used to scope records to this match
     * @param playerIds List of player IDs being substituted out
     * @param currentTimeMillis The current time in milliseconds
     * @param operationId The operation ID to track atomic operations
     */
    suspend fun substituteOutPlayersBatchWithOperationId(
        matchId: Long,
        playerIds: List<Long>,
        currentTimeMillis: Long,
        operationId: String,
    )

    suspend fun resetAllPlayerTimes()
}
