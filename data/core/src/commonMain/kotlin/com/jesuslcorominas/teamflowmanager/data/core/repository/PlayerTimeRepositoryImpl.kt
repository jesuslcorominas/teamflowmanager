package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerTimeDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTime
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeStatus
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

internal class PlayerTimeRepositoryImpl(
    private val playerTimeDataSource: PlayerTimeDataSource
) : PlayerTimeRepository {
    override fun getPlayerTime(playerId: Long): Flow<PlayerTime?> = playerTimeDataSource.getPlayerTime(playerId)

    override fun getAllPlayerTimes(): Flow<List<PlayerTime>> = playerTimeDataSource.getAllPlayerTimes()

    override suspend fun startTimer(
        playerId: Long,
        currentTimeMillis: Long,
    ) {
        val currentPlayerTime = playerTimeDataSource.getPlayerTime(playerId).first()
        val playerTime =
            if (currentPlayerTime != null) {
                currentPlayerTime.copy(
                    isRunning = true,
                    lastStartTimeMillis = currentTimeMillis,
                    status = PlayerTimeStatus.PLAYING,
                )
            } else {
                PlayerTime(
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
        playerIds: List<Long>,
        currentTimeMillis: Long,
    ) {
        if (playerIds.isEmpty()) return

        // Get current player times for all players
        val allCurrentTimes = playerTimeDataSource.getAllPlayerTimes().first()
        val currentTimesMap = allCurrentTimes.associateBy { it.playerId }

        // Create player times for batch upsert
        val playerTimesToUpsert = playerIds.map { playerId ->
            val currentPlayerTime = currentTimesMap[playerId]
            currentPlayerTime?.copy(
                isRunning = true,
                lastStartTimeMillis = currentTimeMillis,
                status = PlayerTimeStatus.PLAYING,
            )
                ?: PlayerTime(
                    playerId = playerId,
                    elapsedTimeMillis = 0L,
                    isRunning = true,
                    lastStartTimeMillis = currentTimeMillis,
                    status = PlayerTimeStatus.PLAYING,
                )
        }

        // Batch upsert all player times at once
        playerTimeDataSource.batchUpsertPlayerTimes(playerTimesToUpsert)
    }

    override suspend fun pauseTimersBatch(
        playerIds: List<Long>,
        currentTimeMillis: Long,
    ) {
        if (playerIds.isEmpty()) return

        // Get current player times for all players
        val allCurrentTimes = playerTimeDataSource.getAllPlayerTimes().first()
        val currentTimesMap = allCurrentTimes.associateBy { it.playerId }

        // Create updated player times for batch upsert
        val playerTimesToUpsert = playerIds.mapNotNull { playerId ->
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
                null // Skip players that aren't running
            }
        }

        // Batch upsert all player times at once
        if (playerTimesToUpsert.isNotEmpty()) {
            playerTimeDataSource.batchUpsertPlayerTimes(playerTimesToUpsert)
        }
    }

    override suspend fun resetAllPlayerTimes() {
        playerTimeDataSource.deleteAllPlayerTimes()
    }

    override suspend fun startTimersBatchWithOperationId(
        playerIds: List<Long>,
        currentTimeMillis: Long,
        operationId: String,
    ) {
        if (playerIds.isEmpty()) return

        // Get current player times for all players
        val allCurrentTimes = playerTimeDataSource.getAllPlayerTimes().first()
        val currentTimesMap = allCurrentTimes.associateBy { it.playerId }

        // Create player times for batch upsert with operation ID
        val playerTimesToUpsert = playerIds.map { playerId ->
            val currentPlayerTime = currentTimesMap[playerId]
            currentPlayerTime?.copy(
                isRunning = true,
                lastStartTimeMillis = currentTimeMillis,
                status = PlayerTimeStatus.PLAYING,
                lastOperationId = operationId,
            )
                ?: PlayerTime(
                    playerId = playerId,
                    elapsedTimeMillis = 0L,
                    isRunning = true,
                    lastStartTimeMillis = currentTimeMillis,
                    status = PlayerTimeStatus.PLAYING,
                    lastOperationId = operationId,
                )
        }

        // Batch upsert all player times at once
        playerTimeDataSource.batchUpsertPlayerTimes(playerTimesToUpsert)
    }

    override suspend fun pauseTimersBatchWithOperationId(
        playerIds: List<Long>,
        currentTimeMillis: Long,
        operationId: String,
    ) {
        if (playerIds.isEmpty()) return

        // Get current player times for all players
        val allCurrentTimes = playerTimeDataSource.getAllPlayerTimes().first()
        val currentTimesMap = allCurrentTimes.associateBy { it.playerId }

        // Create updated player times for batch upsert with operation ID
        // Only pause player timers that are currently running - players on the bench don't need to be paused
        val playerTimesToUpsert = playerIds.mapNotNull { playerId ->
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
                null // Skip players that aren't running - they don't need to be paused
            }
        }

        // Batch upsert all player times at once
        if (playerTimesToUpsert.isNotEmpty()) {
            playerTimeDataSource.batchUpsertPlayerTimes(playerTimesToUpsert)
        }
    }

    override suspend fun substituteOutPlayersBatchWithOperationId(
        playerIds: List<Long>,
        currentTimeMillis: Long,
        operationId: String,
    ) {
        if (playerIds.isEmpty()) return

        // Get current player times for all players
        val allCurrentTimes = playerTimeDataSource.getAllPlayerTimes().first()
        val currentTimesMap = allCurrentTimes.associateBy { it.playerId }

        // Create updated player times for batch upsert with operation ID
        // Mark substituted-out players as ON_BENCH so they won't restart when match resumes
        val playerTimesToUpsert = playerIds.mapNotNull { playerId ->
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
                null // Skip players that aren't running
            }
        }

        // Batch upsert all player times at once
        if (playerTimesToUpsert.isNotEmpty()) {
            playerTimeDataSource.batchUpsertPlayerTimes(playerTimesToUpsert)
        }
    }
}
