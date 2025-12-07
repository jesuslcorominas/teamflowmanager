package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerTimeHistoryDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeHistory
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeHistoryRepository
import kotlinx.coroutines.flow.Flow

internal class PlayerTimeHistoryRepositoryImpl(
    private val playerTimeHistoryDataSource: PlayerTimeHistoryDataSource,
    private val playerTimeHistoryLocalDataSource: PlayerTimeHistoryDataSource,
) : PlayerTimeHistoryRepository {
    override fun getPlayerTimeHistory(playerId: Long): Flow<List<PlayerTimeHistory>> =
        playerTimeHistoryDataSource.getPlayerTimeHistory(playerId)

    override fun getMatchPlayerTimeHistory(matchId: Long): Flow<List<PlayerTimeHistory>> =
        playerTimeHistoryDataSource.getMatchPlayerTimeHistory(matchId)

    override fun getAllPlayerTimeHistory(): Flow<List<PlayerTimeHistory>> =
        playerTimeHistoryDataSource.getAllPlayerTimeHistory()

    override suspend fun insertPlayerTimeHistory(playerTimeHistory: PlayerTimeHistory): Long =
        playerTimeHistoryDataSource.insertPlayerTimeHistory(playerTimeHistory)

    override suspend fun getAllLocalPlayerTimeHistoryDirect(): List<PlayerTimeHistory> =
        playerTimeHistoryLocalDataSource.getAllPlayerTimeHistoryDirect()

    override suspend fun clearLocalPlayerTimeHistoryData() {
        playerTimeHistoryLocalDataSource.clearLocalData()
    }
}
