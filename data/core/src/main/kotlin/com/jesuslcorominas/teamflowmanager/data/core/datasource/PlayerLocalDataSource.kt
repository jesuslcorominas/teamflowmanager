package com.jesuslcorominas.teamflowmanager.data.core.datasource

import com.jesuslcorominas.teamflowmanager.domain.model.Player
import kotlinx.coroutines.flow.Flow

interface PlayerLocalDataSource {
    fun getAllPlayers(): Flow<List<Player>>

    suspend fun getPlayerById(playerId: Long): Player?

    suspend fun getCaptainPlayer(): Player?

    suspend fun setPlayerAsCaptain(playerId: Long)

    suspend fun removePlayerAsCaptain(playerId: Long)

    suspend fun updatePlayer(player: Player)

    suspend fun insertPlayer(player: Player)

    suspend fun deletePlayer(playerId: Long)
}
