package com.jesuslcorominas.teamflowmanager.usecase.repository

import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTime
import kotlinx.coroutines.flow.Flow

interface PlayerTimeRepository {
    fun getPlayerTime(playerId: String): Flow<PlayerTime?>

    fun getPlayerTimesByMatch(matchId: String): Flow<List<PlayerTime>>

    suspend fun startTimer(
        matchId: String,
        playerId: String,
        currentTimeMillis: Long,
    )

    suspend fun pauseTimer(
        playerId: String,
        currentTimeMillis: Long,
    )

    suspend fun pauseTimerForMatchPause(
        playerId: String,
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
        matchId: String,
        playerIds: List<String>,
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
        matchId: String,
        playerIds: List<String>,
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
        matchId: String,
        playerIds: List<String>,
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
        matchId: String,
        playerIds: List<String>,
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
        matchId: String,
        playerIds: List<String>,
        currentTimeMillis: Long,
        operationId: String,
    )

    suspend fun resetAllPlayerTimes()
}
