package com.jesuslcorominas.teamflowmanager.data.local.datasource

import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerLocalDataSource
import com.jesuslcorominas.teamflowmanager.data.local.entity.toDomain
import com.jesuslcorominas.teamflowmanager.data.local.entity.toEntity
import com.jesuslcorominas.teamflowmanager.data.local.sqldelight.PlayerDaoWrapper
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class PlayerLocalDataSourceImpl(
    private val playerDao: PlayerDaoWrapper,
) : PlayerLocalDataSource {
    override fun getAllPlayers(): Flow<List<Player>> =
        playerDao.getAllPlayers().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getCaptainPlayer(): Player? =
        playerDao.getCaptainPlayer()?.toDomain()

    override suspend fun setPlayerAsCaptain(playerId: Long) {
        playerDao.setPlayerAsCaptain(playerId)
    }

    override suspend fun removePlayerAsCaptain(playerId: Long) {
        playerDao.removePlayerAsCaptain(playerId)
    }

    override suspend fun insertPlayer(player: Player) {
        playerDao.insertPlayer(player.toEntity())
    }

    override suspend fun deletePlayer(playerId: Long) {
        playerDao.deletePlayer(playerId)
    }

    override suspend fun updatePlayer(player: Player) {
        playerDao.updatePlayer(player.toEntity())
    }
}
