package com.jesuslcorominas.teamflowmanager.data.core.datasource

import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTime
import kotlinx.coroutines.flow.Flow

interface PlayerTimeLocalDataSource {
    fun getPlayerTime(playerId: Long): Flow<PlayerTime?>

    fun getAllPlayerTimes(): Flow<List<PlayerTime>>

    suspend fun upsertPlayerTime(playerTime: PlayerTime)

    suspend fun deleteAllPlayerTimes()
}
