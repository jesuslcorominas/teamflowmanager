package com.jesuslcorominas.teamflowmanager.data.core.datasource

import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTime
import kotlinx.coroutines.flow.Flow

interface PlayerTimeDataSource {
    fun getPlayerTime(playerId: Long): Flow<PlayerTime?>

    fun getAllPlayerTimes(): Flow<List<PlayerTime>>

    suspend fun upsertPlayerTime(playerTime: PlayerTime)

    suspend fun deleteAllPlayerTimes()

    /**
     * Get all player times directly (not as a Flow) for migration purposes.
     * @return List of all player times
     */
    suspend fun getAllPlayerTimesDirect(): List<PlayerTime>

    /**
     * Clear all player time data from local storage.
     * Only applicable for local data sources.
     */
    suspend fun clearLocalData()
}
