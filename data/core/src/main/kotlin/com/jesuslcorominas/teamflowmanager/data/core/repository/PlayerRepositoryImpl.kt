package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerLocalDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerRepository
import kotlinx.coroutines.flow.Flow

internal class PlayerRepositoryImpl(
    private val localDataSource: PlayerLocalDataSource,
) : PlayerRepository {
    override fun getAllPlayers(): Flow<List<Player>> = localDataSource.getAllPlayers()

    override suspend fun addPlayer(player: Player) {
        localDataSource.insertPlayer(player)
    }

    override suspend fun deletePlayer(playerId: Long) {
        localDataSource.deletePlayer(playerId)
    }
}
