package com.jesuslcorominas.teamflowmanager.data.local.sqldelight

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.jesuslcorominas.teamflowmanager.data.local.database.TeamFlowManagerDatabase
import com.jesuslcorominas.teamflowmanager.data.local.entity.PlayerEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class PlayerDaoWrapper(
    private val database: TeamFlowManagerDatabase
) {
    fun getAllPlayers(): Flow<List<PlayerEntity>> =
        database.playerQueries
            .getAllPlayers()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { players ->
                players.map { player ->
                    PlayerEntity(
                        id = player.id,
                        firstName = player.firstName,
                        lastName = player.lastName,
                        number = player.number.toInt(),
                        positions = player.positions,
                        teamId = player.teamId,
                        isCaptain = player.isCaptain
                    )
                }
            }

    suspend fun getCaptainPlayer(): PlayerEntity? {
        val player = database.playerQueries.getCaptainPlayer().executeAsOneOrNull()
        return player?.let {
            PlayerEntity(
                id = it.id,
                firstName = it.firstName,
                lastName = it.lastName,
                number = it.number.toInt(),
                positions = it.positions,
                teamId = it.teamId,
                isCaptain = it.isCaptain
            )
        }
    }

    suspend fun clearAllCaptains() {
        database.playerQueries.clearAllCaptains()
    }

    suspend fun getPlayerById(playerId: Long): PlayerEntity? {
        val player = database.playerQueries.getPlayerById(playerId).executeAsOneOrNull()
        return player?.let {
            PlayerEntity(
                id = it.id,
                firstName = it.firstName,
                lastName = it.lastName,
                number = it.number.toInt(),
                positions = it.positions,
                teamId = it.teamId,
                isCaptain = it.isCaptain
            )
        }
    }

    suspend fun insertPlayer(player: PlayerEntity) {
        database.playerQueries.insertPlayer(
            firstName = player.firstName,
            lastName = player.lastName,
            number = player.number.toLong(),
            positions = player.positions,
            teamId = player.teamId,
            isCaptain = if (player.isCaptain) 1L else 0L
        )
    }

    suspend fun updatePlayer(player: PlayerEntity) {
        database.playerQueries.updatePlayer(
            firstName = player.firstName,
            lastName = player.lastName,
            number = player.number.toLong(),
            positions = player.positions,
            teamId = player.teamId,
            isCaptain = if (player.isCaptain) 1L else 0L,
            id = player.id
        )
    }

    suspend fun deletePlayer(playerId: Long) {
        database.playerQueries.deletePlayer(playerId)
    }

    suspend fun setPlayerAsCaptain(playerId: Long) {
        database.transaction {
            clearAllCaptains()
            val player = getPlayerById(playerId)
            if (player != null) {
                updatePlayer(player.copy(isCaptain = true))
            }
        }
    }

    suspend fun removePlayerAsCaptain(playerId: Long) {
        val player = getPlayerById(playerId)
        if (player != null) {
            updatePlayer(player.copy(isCaptain = false))
        }
    }
}
