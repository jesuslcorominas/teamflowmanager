package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerLocalDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerRepository
import kotlinx.coroutines.flow.Flow

internal class PlayerRepositoryImpl(
    private val localDataSource: PlayerLocalDataSource,
) : PlayerRepository {
    override fun getAllPlayers(): Flow<List<Player>> = localDataSource.getAllPlayers()

    override suspend fun getCaptainPlayer(): Player? = localDataSource.getCaptainPlayer()

    override suspend fun addPlayer(player: Player) {
        localDataSource.insertPlayer(player)
    }

    override suspend fun deletePlayer(playerId: Long) {
        localDataSource.deletePlayer(playerId)
    }

    override suspend fun updatePlayer(player: Player) {
        localDataSource.updatePlayer(player)
    }

    override suspend fun setPlayerAsCaptain(playerId: Long) {
        localDataSource.setPlayerAsCaptain(playerId)
    }

    override suspend fun removePlayerAsCaptain(playerId: Long) {
        localDataSource.removePlayerAsCaptain(playerId)
    }
}
