package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerTimeDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTime
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeStatus
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

internal class PlayerTimeRepositoryImpl(
    private val playerTimeDataSource: PlayerTimeDataSource,
) : PlayerTimeRepository {
    override fun getPlayerTime(playerId: Long): Flow<PlayerTime?> = playerTimeDataSource.getPlayerTime(playerId)

    override fun getPlayerTimesByMatch(matchId: Long): Flow<List<PlayerTime>> = playerTimeDataSource.getPlayerTimesByMatch(matchId)

    override suspend fun startTimer(
        matchId: Long,
        playerId: Long,
        currentTimeMillis: Long,
    ) {
        val currentPlayerTime = playerTimeDataSource.getPlayerTime(playerId).first()
        val playerTime =
            if (currentPlayerTime != null) {
                currentPlayerTime.copy(
                    matchId = matchId,
                    isRunning = true,
                    lastStartTimeMillis = currentTimeMillis,
                    status = PlayerTimeStatus.PLAYING,
                )
            } else {
                PlayerTime(
                    matchId = matchId,
                    playerId = playerId,
                    elapsedTimeMillis = 0L,
                    isRunning = true,
                    lastStartTimeMillis = currentTimeMillis,
                    status = PlayerTimeStatus.PLAYING,
                )
            }
        playerTimeDataSource.upsertPlayerTime(playerTime)
    }

    override suspend fun pauseTimer(
        playerId: Long,
        currentTimeMillis: Long,
    ) {
        val currentPlayerTime = playerTimeDataSource.getPlayerTime(playerId).first()
        if (currentPlayerTime != null && currentPlayerTime.isRunning) {
            val lastStartTime = currentPlayerTime.lastStartTimeMillis ?: currentTimeMillis
            val additionalTime = currentTimeMillis - lastStartTime
            val updatedPlayerTime =
                currentPlayerTime.copy(
                    elapsedTimeMillis = currentPlayerTime.elapsedTimeMillis + additionalTime,
                    isRunning = false,
                    lastStartTimeMillis = null,
                    status = PlayerTimeStatus.ON_BENCH,
                )
            playerTimeDataSource.upsertPlayerTime(updatedPlayerTime)
        }
    }

    override suspend fun pauseTimerForMatchPause(
        playerId: Long,
        currentTimeMillis: Long,
    ) {
        val currentPlayerTime = playerTimeDataSource.getPlayerTime(playerId).first()
        if (currentPlayerTime != null && currentPlayerTime.isRunning) {
            val lastStartTime = currentPlayerTime.lastStartTimeMillis ?: currentTimeMillis
            val additionalTime = currentTimeMillis - lastStartTime
            val updatedPlayerTime =
                currentPlayerTime.copy(
                    elapsedTimeMillis = currentPlayerTime.elapsedTimeMillis + additionalTime,
                    isRunning = false,
                    lastStartTimeMillis = lastStartTime,
                    status = PlayerTimeStatus.PAUSED,
                )
            playerTimeDataSource.upsertPlayerTime(updatedPlayerTime)
        }
    }

    override suspend fun startTimersBatch(
        matchId: Long,
        playerIds: List<Long>,
        currentTimeMillis: Long,
    ) {
        if (playerIds.isEmpty()) return

        val allCurrentTimes = playerTimeDataSource.getPlayerTimesByMatch(matchId).first()
        val currentTimesMap = allCurrentTimes.associateBy { it.playerId }

        val playerTimesToUpsert =
            playerIds.map { playerId ->
                val currentPlayerTime = currentTimesMap[playerId]
                currentPlayerTime?.copy(
                    matchId = matchId,
                    isRunning = true,
                    lastStartTimeMillis = currentTimeMillis,
                    status = PlayerTimeStatus.PLAYING,
                )
                    ?: PlayerTime(
                        matchId = matchId,
                        playerId = playerId,
                        elapsedTimeMillis = 0L,
                        isRunning = true,
                        lastStartTimeMillis = currentTimeMillis,
                        status = PlayerTimeStatus.PLAYING,
                    )
            }

        playerTimeDataSource.batchUpsertPlayerTimes(playerTimesToUpsert)
    }

    override suspend fun pauseTimersBatch(
        matchId: Long,
        playerIds: List<Long>,
        currentTimeMillis: Long,
    ) {
        if (playerIds.isEmpty()) return

        val allCurrentTimes = playerTimeDataSource.getPlayerTimesByMatch(matchId).first()
        val currentTimesMap = allCurrentTimes.associateBy { it.playerId }

        val playerTimesToUpsert =
            playerIds.mapNotNull { playerId ->
                val currentPlayerTime = currentTimesMap[playerId]
                if (currentPlayerTime != null && currentPlayerTime.isRunning) {
                    val lastStartTime = currentPlayerTime.lastStartTimeMillis ?: currentTimeMillis
                    val additionalTime = currentTimeMillis - lastStartTime
                    currentPlayerTime.copy(
                        elapsedTimeMillis = currentPlayerTime.elapsedTimeMillis + additionalTime,
                        isRunning = false,
                        lastStartTimeMillis = lastStartTime,
                        status = PlayerTimeStatus.PAUSED,
                    )
                } else {
                    null
                }
            }

        if (playerTimesToUpsert.isNotEmpty()) {
            playerTimeDataSource.batchUpsertPlayerTimes(playerTimesToUpsert)
        }
    }

    override suspend fun resetAllPlayerTimes() {
        playerTimeDataSource.deleteAllPlayerTimes()
    }

    override suspend fun startTimersBatchWithOperationId(
        matchId: Long,
        playerIds: List<Long>,
        currentTimeMillis: Long,
        operationId: String,
    ) {
        if (playerIds.isEmpty()) return

        val allCurrentTimes = playerTimeDataSource.getPlayerTimesByMatch(matchId).first()
        val currentTimesMap = allCurrentTimes.associateBy { it.playerId }

        val playerTimesToUpsert =
            playerIds.map { playerId ->
                val currentPlayerTime = currentTimesMap[playerId]
                currentPlayerTime?.copy(
                    matchId = matchId,
                    // If the player was already running, accumulate the delta before resetting
                    // lastStartTimeMillis. Without this, re-stamping the timer for "other playing
                    // players" during a substitution would lose all time accumulated since the
                    // last start.
                    elapsedTimeMillis =
                        run {
                            val startTime = currentPlayerTime.lastStartTimeMillis
                            if (currentPlayerTime.isRunning && startTime != null) {
                                currentPlayerTime.elapsedTimeMillis + (currentTimeMillis - startTime)
                            } else {
                                currentPlayerTime.elapsedTimeMillis
                            }
                        },
                    isRunning = true,
                    lastStartTimeMillis = currentTimeMillis,
                    status = PlayerTimeStatus.PLAYING,
                    lastOperationId = operationId,
                )
                    ?: PlayerTime(
                        matchId = matchId,
                        playerId = playerId,
                        elapsedTimeMillis = 0L,
                        isRunning = true,
                        lastStartTimeMillis = currentTimeMillis,
                        status = PlayerTimeStatus.PLAYING,
                        lastOperationId = operationId,
                    )
            }

        playerTimeDataSource.batchUpsertPlayerTimes(playerTimesToUpsert)
    }

    override suspend fun pauseTimersBatchWithOperationId(
        matchId: Long,
        playerIds: List<Long>,
        currentTimeMillis: Long,
        operationId: String,
    ) {
        if (playerIds.isEmpty()) return

        val allCurrentTimes = playerTimeDataSource.getPlayerTimesByMatch(matchId).first()
        val currentTimesMap = allCurrentTimes.associateBy { it.playerId }

        // Only pause player timers that are currently running - players on the bench don't need to be paused
        val playerTimesToUpsert =
            playerIds.mapNotNull { playerId ->
                val currentPlayerTime = currentTimesMap[playerId]
                if (currentPlayerTime != null && currentPlayerTime.isRunning) {
                    val lastStartTime = currentPlayerTime.lastStartTimeMillis ?: currentTimeMillis
                    val additionalTime = currentTimeMillis - lastStartTime
                    currentPlayerTime.copy(
                        elapsedTimeMillis = currentPlayerTime.elapsedTimeMillis + additionalTime,
                        isRunning = false,
                        lastStartTimeMillis = lastStartTime,
                        status = PlayerTimeStatus.PAUSED,
                        lastOperationId = operationId,
                    )
                } else {
                    null
                }
            }

        if (playerTimesToUpsert.isNotEmpty()) {
            playerTimeDataSource.batchUpsertPlayerTimes(playerTimesToUpsert)
        }
    }

    override suspend fun substituteOutPlayersBatchWithOperationId(
        matchId: Long,
        playerIds: List<Long>,
        currentTimeMillis: Long,
        operationId: String,
    ) {
        if (playerIds.isEmpty()) return

        val allCurrentTimes = playerTimeDataSource.getPlayerTimesByMatch(matchId).first()
        val currentTimesMap = allCurrentTimes.associateBy { it.playerId }

        // Mark substituted-out players as ON_BENCH so they won't restart when match resumes
        val playerTimesToUpsert =
            playerIds.mapNotNull { playerId ->
                val currentPlayerTime = currentTimesMap[playerId]
                if (currentPlayerTime != null && currentPlayerTime.isRunning) {
                    val lastStartTime = currentPlayerTime.lastStartTimeMillis ?: currentTimeMillis
                    val additionalTime = currentTimeMillis - lastStartTime
                    currentPlayerTime.copy(
                        elapsedTimeMillis = currentPlayerTime.elapsedTimeMillis + additionalTime,
                        isRunning = false,
                        lastStartTimeMillis = null,
                        status = PlayerTimeStatus.ON_BENCH,
                        lastOperationId = operationId,
                    )
                } else {
                    null
                }
            }

        if (playerTimesToUpsert.isNotEmpty()) {
            playerTimeDataSource.batchUpsertPlayerTimes(playerTimesToUpsert)
        }
    }
}
