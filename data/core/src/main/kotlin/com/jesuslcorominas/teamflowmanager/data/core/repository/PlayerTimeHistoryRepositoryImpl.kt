package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerTimeHistoryLocalDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeHistory
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeHistoryRepository
import kotlinx.coroutines.flow.Flow

internal class PlayerTimeHistoryRepositoryImpl(
    private val localDataSource: PlayerTimeHistoryLocalDataSource,
) : PlayerTimeHistoryRepository {
    override fun getPlayerTimeHistory(playerId: Long): Flow<List<PlayerTimeHistory>> =
        localDataSource.getPlayerTimeHistory(playerId)

    override fun getMatchPlayerTimeHistory(matchId: Long): Flow<List<PlayerTimeHistory>> =
        localDataSource.getMatchPlayerTimeHistory(matchId)

    override fun getAllPlayerTimeHistory(): Flow<List<PlayerTimeHistory>> =
        localDataSource.getAllPlayerTimeHistory()

    override suspend fun insertPlayerTimeHistory(playerTimeHistory: PlayerTimeHistory): Long =
        localDataSource.insertPlayerTimeHistory(playerTimeHistory)
}
