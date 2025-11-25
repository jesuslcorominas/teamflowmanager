package com.jesuslcorominas.teamflowmanager.data.core.datasource

import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeHistory
import kotlinx.coroutines.flow.Flow

interface PlayerTimeHistoryLocalDataSource {
    fun getPlayerTimeHistory(playerId: Long): Flow<List<PlayerTimeHistory>>

    fun getMatchPlayerTimeHistory(matchId: Long): Flow<List<PlayerTimeHistory>>

    fun getAllPlayerTimeHistory(): Flow<List<PlayerTimeHistory>>

    suspend fun insertPlayerTimeHistory(playerTimeHistory: PlayerTimeHistory): Long
}
