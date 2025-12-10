package com.jesuslcorominas.teamflowmanager.data.local.datasource

import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerTimeDataSource
import com.jesuslcorominas.teamflowmanager.data.local.dao.PlayerTimeDao
import com.jesuslcorominas.teamflowmanager.data.local.entity.toDomain
import com.jesuslcorominas.teamflowmanager.data.local.entity.toEntity
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class PlayerTimeLocalDataSourceImpl(
    private val playerTimeDao: PlayerTimeDao,
) : PlayerTimeDataSource {
    override fun getPlayerTime(playerId: Long): Flow<PlayerTime?> =
        playerTimeDao
            .getPlayerTime(playerId)
            .map { it?.toDomain() }

    override fun getAllPlayerTimes(): Flow<List<PlayerTime>> =
        playerTimeDao
            .getAllPlayerTimes()
            .map { list -> list.map { it.toDomain() } }

    override suspend fun upsertPlayerTime(playerTime: PlayerTime) {
        playerTimeDao.upsert(playerTime.toEntity())
    }

    override suspend fun batchUpsertPlayerTimes(playerTimes: List<PlayerTime>) {
        // For local data source, just upsert each one individually
        // Room handles this efficiently
        playerTimes.forEach { playerTime ->
            playerTimeDao.upsert(playerTime.toEntity())
        }
    }

    override suspend fun deleteAllPlayerTimes() {
        playerTimeDao.deleteAll()
    }

    override suspend fun getAllPlayerTimesDirect(): List<PlayerTime> =
        playerTimeDao.getAllPlayerTimesDirect().map { it.toDomain() }

    override suspend fun clearLocalData() {
        playerTimeDao.deleteAll()
    }
}
