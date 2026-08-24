package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerRepository
import kotlinx.coroutines.flow.Flow

internal class PlayerRepositoryImpl(
    private val playerDataSource: PlayerDataSource,
) : PlayerRepository {
    override fun getAllPlayers(): Flow<List<Player>> = playerDataSource.getAllPlayers()

    override fun getPlayersByTeam(teamId: String): Flow<List<Player>> = playerDataSource.getPlayersByTeam(teamId)

    override suspend fun getPlayerById(playerId: String): Player? = playerDataSource.getPlayerById(playerId)

    override suspend fun getCaptainPlayer(): Player? = playerDataSource.getCaptainPlayer()

    override suspend fun addPlayer(player: Player): String {
        return playerDataSource.insertPlayer(player)
    }

    override suspend fun deletePlayer(playerId: String) {
        playerDataSource.deletePlayer(playerId)
    }

    override suspend fun updatePlayer(player: Player) {
        playerDataSource.updatePlayer(player)
    }

    override suspend fun setPlayerAsCaptain(playerId: String) {
        playerDataSource.setPlayerAsCaptain(playerId)
    }

    override suspend fun removePlayerAsCaptain(playerId: String) {
        playerDataSource.removePlayerAsCaptain(playerId)
    }
}
