package com.jesuslcorominas.teamflowmanager.data.core.datasource

import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeHistory
import kotlinx.coroutines.flow.Flow

interface PlayerTimeHistoryDataSource {
    fun getPlayerTimeHistory(playerId: Long): Flow<List<PlayerTimeHistory>>

    fun getMatchPlayerTimeHistory(
        matchId: Long,
        teamId: String? = null,
    ): Flow<List<PlayerTimeHistory>>

    fun getAllPlayerTimeHistory(): Flow<List<PlayerTimeHistory>>

    suspend fun insertPlayerTimeHistory(playerTimeHistory: PlayerTimeHistory): Long

    /**
     * Get all player time history directly (not as a Flow) for migration purposes.
     * @return List of all player time history records
     */
    suspend fun getAllPlayerTimeHistoryDirect(): List<PlayerTimeHistory>

    /**
     * Clear all player time history data from local storage.
     * Only applicable for local data sources.
     */
    suspend fun clearLocalData()
}
