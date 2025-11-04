package com.jesuslcorominas.teamflowmanager.data.local.dao

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.jesuslcorominas.teamflowmanager.data.local.database.TeamFlowManagerDatabase
import com.jesuslcorominas.teamflowmanager.data.local.entity.PlayerTimeHistoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class PlayerTimeHistoryDao(
    private val database: TeamFlowManagerDatabase
) {
    fun getPlayerTimeHistory(playerId: Long): Flow<List<PlayerTimeHistoryEntity>> =
        database.playerTimeHistoryQueries
            .getPlayerTimeHistoryByPlayerId(playerId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { histories ->
                histories.map {
                    PlayerTimeHistoryEntity(
                        id = it.id,
                        playerId = it.playerId,
                        matchId = it.matchId,
                        elapsedTimeMillis = it.elapsedTimeMillis,
                        savedAtMillis = it.savedAtMillis
                    )
                }
            }

    fun getMatchPlayerTimeHistory(matchId: Long): Flow<List<PlayerTimeHistoryEntity>> =
        database.playerTimeHistoryQueries
            .getPlayerTimeHistoryByMatchId(matchId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { histories ->
                histories.map {
                    PlayerTimeHistoryEntity(
                        id = it.id,
                        playerId = it.playerId,
                        matchId = it.matchId,
                        elapsedTimeMillis = it.elapsedTimeMillis,
                        savedAtMillis = it.savedAtMillis
                    )
                }
            }

    fun getAllPlayerTimeHistory(): Flow<List<PlayerTimeHistoryEntity>> =
        database.playerTimeHistoryQueries
            .getAllPlayerTimeHistories()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { histories ->
                histories.map {
                    PlayerTimeHistoryEntity(
                        id = it.id,
                        playerId = it.playerId,
                        matchId = it.matchId,
                        elapsedTimeMillis = it.elapsedTimeMillis,
                        savedAtMillis = it.savedAtMillis
                    )
                }
            }

    suspend fun insert(playerTimeHistory: PlayerTimeHistoryEntity): Long {
        database.playerTimeHistoryQueries.insertPlayerTimeHistory(
            playerId = playerTimeHistory.playerId,
            matchId = playerTimeHistory.matchId,
            elapsedTimeMillis = playerTimeHistory.elapsedTimeMillis,
            savedAtMillis = playerTimeHistory.savedAtMillis
        )
        return database.playerTimeHistoryQueries.lastInsertRowId().executeAsOne()
    }
}
