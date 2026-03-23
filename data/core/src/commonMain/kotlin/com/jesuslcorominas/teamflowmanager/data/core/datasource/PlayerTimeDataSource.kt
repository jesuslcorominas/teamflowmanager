package com.jesuslcorominas.teamflowmanager.data.core.datasource

import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTime
import kotlinx.coroutines.flow.Flow

interface PlayerTimeDataSource {
    fun getPlayerTime(playerId: Long): Flow<PlayerTime?>

    fun getPlayerTimesByMatch(matchId: Long): Flow<List<PlayerTime>>

    suspend fun upsertPlayerTime(playerTime: PlayerTime)

    /**
     * Batch upsert multiple player times at once.
     * Uses Firestore batch write for atomic operation.
     * @param playerTimes List of player times to upsert
     */
    suspend fun batchUpsertPlayerTimes(playerTimes: List<PlayerTime>)

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
