package com.jesuslcorominas.teamflowmanager.usecase.repository

import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeHistory
import kotlinx.coroutines.flow.Flow

interface PlayerTimeHistoryRepository {
    fun getPlayerTimeHistory(playerId: Long): Flow<List<PlayerTimeHistory>>

    fun getMatchPlayerTimeHistory(
        matchId: Long,
        teamId: String? = null,
    ): Flow<List<PlayerTimeHistory>>

    fun getAllPlayerTimeHistory(): Flow<List<PlayerTimeHistory>>

    suspend fun insertPlayerTimeHistory(playerTimeHistory: PlayerTimeHistory): Long
}
