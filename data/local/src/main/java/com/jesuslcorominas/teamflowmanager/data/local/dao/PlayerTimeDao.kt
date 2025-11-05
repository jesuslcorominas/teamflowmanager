package com.jesuslcorominas.teamflowmanager.data.local.dao

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.jesuslcorominas.teamflowmanager.data.local.database.TeamFlowManagerDatabase
import com.jesuslcorominas.teamflowmanager.data.local.entity.PlayerTimeEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class PlayerTimeDao(
    private val database: TeamFlowManagerDatabase
) {
    fun getPlayerTime(playerId: Long): Flow<PlayerTimeEntity?> =
        database.playerTimeQueries
            .getPlayerTimeByPlayerId(playerId)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { playerTime ->
                playerTime?.let {
                    PlayerTimeEntity(
                        playerId = it.playerId,
                        elapsedTimeMillis = it.elapsedTimeMillis,
                        isRunning = it.isRunning != 0L,
                        lastStartTimeMillis = it.lastStartTimeMillis,
                        status = it.status
                    )
                }
            }

    fun getAllPlayerTimes(): Flow<List<PlayerTimeEntity>> =
        database.playerTimeQueries
            .getAllPlayerTimes()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { playerTimes ->
                playerTimes.map {
                    PlayerTimeEntity(
                        playerId = it.playerId,
                        elapsedTimeMillis = it.elapsedTimeMillis,
                        isRunning = it.isRunning != 0L,
                        lastStartTimeMillis = it.lastStartTimeMillis,
                        status = it.status
                    )
                }
            }

    suspend fun upsert(playerTime: PlayerTimeEntity) {
        database.playerTimeQueries.upsertPlayerTime(
            playerId = playerTime.playerId,
            elapsedTimeMillis = playerTime.elapsedTimeMillis,
            isRunning = if (playerTime.isRunning) 1L else 0L,
            lastStartTimeMillis = playerTime.lastStartTimeMillis,
            status = playerTime.status
        )
    }

    suspend fun deleteAll() {
        database.playerTimeQueries.deleteAllPlayerTimes()
    }
}
