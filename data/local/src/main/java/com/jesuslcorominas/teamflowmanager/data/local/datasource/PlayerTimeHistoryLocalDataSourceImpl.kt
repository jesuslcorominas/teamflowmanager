package com.jesuslcorominas.teamflowmanager.data.local.datasource

import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerTimeHistoryDataSource
import com.jesuslcorominas.teamflowmanager.data.local.dao.PlayerTimeHistoryDao
import com.jesuslcorominas.teamflowmanager.data.local.entity.toEntity
import com.jesuslcorominas.teamflowmanager.data.local.entity.toDomain
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeHistory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class PlayerTimeHistoryLocalDataSourceImpl(
    private val playerTimeHistoryDao: PlayerTimeHistoryDao,
) : PlayerTimeHistoryDataSource {
    override fun getPlayerTimeHistory(playerId: Long): Flow<List<PlayerTimeHistory>> =
        playerTimeHistoryDao.getPlayerTimeHistory(playerId).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getMatchPlayerTimeHistory(matchId: Long): Flow<List<PlayerTimeHistory>> =
        playerTimeHistoryDao.getMatchPlayerTimeHistory(matchId).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getAllPlayerTimeHistory(): Flow<List<PlayerTimeHistory>> =
        playerTimeHistoryDao.getAllPlayerTimeHistory().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun insertPlayerTimeHistory(playerTimeHistory: PlayerTimeHistory): Long =
        playerTimeHistoryDao.insert(playerTimeHistory.toEntity())
}
