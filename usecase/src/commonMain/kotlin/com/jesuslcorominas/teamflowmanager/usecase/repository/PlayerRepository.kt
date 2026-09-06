package com.jesuslcorominas.teamflowmanager.usecase.repository

import com.jesuslcorominas.teamflowmanager.domain.model.Player
import kotlinx.coroutines.flow.Flow

interface PlayerRepository {
    fun getAllPlayers(): Flow<List<Player>>

    fun getPlayersByTeam(teamId: String): Flow<List<Player>>

    suspend fun getPlayerById(playerId: String): Player?

    suspend fun getCaptainPlayer(): Player?

    suspend fun addPlayer(player: Player): String

    suspend fun deletePlayer(playerId: String)

    suspend fun updatePlayer(player: Player)

    suspend fun setPlayerAsCaptain(playerId: String)

    suspend fun removePlayerAsCaptain(playerId: String)
}
