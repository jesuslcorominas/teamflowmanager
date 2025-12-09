package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerTimeDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTime
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeStatus
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

internal class PlayerTimeRepositoryImpl(
    private val playerTimeDataSource: PlayerTimeDataSource,
    private val playerTimeLocalDataSource: PlayerTimeDataSource,
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
        }

        // Batch upsert all player times at once
        playerTimeDataSource.batchUpsertPlayerTimes(playerTimesToUpsert)
    }

    override suspend fun resetAllPlayerTimes() {
        playerTimeDataSource.deleteAllPlayerTimes()
    }

    override suspend fun getAllLocalPlayerTimesDirect(): List<PlayerTime> =
        playerTimeLocalDataSource.getAllPlayerTimesDirect()

    override suspend fun clearLocalPlayerTimeData() {
        playerTimeLocalDataSource.clearLocalData()
    }
}
