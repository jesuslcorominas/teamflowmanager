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

    suspend fun resetAllPlayerTimes()
}
