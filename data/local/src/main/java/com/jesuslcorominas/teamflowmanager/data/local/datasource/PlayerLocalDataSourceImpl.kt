package com.jesuslcorominas.teamflowmanager.data.local.datasource

import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerLocalDataSource
import com.jesuslcorominas.teamflowmanager.data.local.dao.PlayerDao
import com.jesuslcorominas.teamflowmanager.data.local.entity.toDomain
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implementation of PlayerLocalDataSource using Room
 */
internal class PlayerLocalDataSourceImpl(
    private val playerDao: PlayerDao
) : PlayerLocalDataSource {
    override fun getAllPlayers(): Flow<List<Player>> {
        return playerDao.getAllPlayers().map { entities ->
            entities.map { it.toDomain() }
        }
    }
}
