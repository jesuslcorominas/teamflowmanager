package com.jesuslcorominas.teamflowmanager.usecase.repository

import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeHistory
import kotlinx.coroutines.flow.Flow

interface PlayerTimeHistoryRepository {
    fun getPlayerTimeHistory(playerId: Long): Flow<List<PlayerTimeHistory>>

    fun getMatchPlayerTimeHistory(matchId: Long): Flow<List<PlayerTimeHistory>>

    fun getAllPlayerTimeHistory(): Flow<List<PlayerTimeHistory>>

    suspend fun insertPlayerTimeHistory(playerTimeHistory: PlayerTimeHistory): Long

    /**
     * Get all local player time history directly (not as a Flow) for migration purposes.
     * @return List of all player time history records
     */
    suspend fun getAllLocalPlayerTimeHistoryDirect(): List<PlayerTimeHistory>

    /**
     * Clear local player time history data from Room database.
     * Used after successful migration to Firestore.
     */
    suspend fun clearLocalPlayerTimeHistoryData()
}
