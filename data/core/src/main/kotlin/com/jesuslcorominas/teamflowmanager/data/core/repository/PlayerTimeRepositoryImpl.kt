package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerTimeLocalDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTime
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeStatus
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

internal class PlayerTimeRepositoryImpl(
    private val localDataSource: PlayerTimeLocalDataSource,
) : PlayerTimeRepository {
    override fun getPlayerTime(playerId: Long): Flow<PlayerTime?> = localDataSource.getPlayerTime(playerId)

    override fun getAllPlayerTimes(): Flow<List<PlayerTime>> = localDataSource.getAllPlayerTimes()

    override suspend fun startTimer(
        playerId: Long,
        currentTimeMillis: Long,
    ) {
        val currentPlayerTime = localDataSource.getPlayerTime(playerId).first()
        val playerTime =
            if (currentPlayerTime != null) {
                currentPlayerTime.copy(
                    isRunning = true,
                    lastStartTimeMillis = currentTimeMillis,
                    status = PlayerTimeStatus.JUGANDO,
                )
            } else {
                PlayerTime(
                    playerId = playerId,
                    elapsedTimeMillis = 0L,
                    isRunning = true,
                    lastStartTimeMillis = currentTimeMillis,
                    status = PlayerTimeStatus.JUGANDO,
                )
            }
        localDataSource.upsertPlayerTime(playerTime)
    }

    override suspend fun pauseTimer(
        playerId: Long,
        currentTimeMillis: Long,
    ) {
        val currentPlayerTime = localDataSource.getPlayerTime(playerId).first()
        if (currentPlayerTime != null && currentPlayerTime.isRunning) {
            val lastStartTime = currentPlayerTime.lastStartTimeMillis ?: currentTimeMillis
            val additionalTime = currentTimeMillis - lastStartTime
            val updatedPlayerTime =
                currentPlayerTime.copy(
                    elapsedTimeMillis = currentPlayerTime.elapsedTimeMillis + additionalTime,
                    isRunning = false,
                    lastStartTimeMillis = lastStartTime,
                    status = PlayerTimeStatus.DESCANSO,
                )
            localDataSource.upsertPlayerTime(updatedPlayerTime)
        }
    }

    override suspend fun resetAllPlayerTimes() {
        localDataSource.deleteAllPlayerTimes()
    }
}
