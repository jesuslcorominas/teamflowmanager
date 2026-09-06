package com.jesuslcorominas.teamflowmanager.data.core.datasource

import com.jesuslcorominas.teamflowmanager.domain.model.Player
import kotlinx.coroutines.flow.Flow

interface PlayerDataSource {
    fun getAllPlayers(): Flow<List<Player>>

    fun getPlayersByTeam(teamId: String): Flow<List<Player>>

    suspend fun getPlayerById(playerId: String): Player?

    suspend fun getCaptainPlayer(): Player?

    suspend fun setPlayerAsCaptain(playerId: String)

    suspend fun removePlayerAsCaptain(playerId: String)

    suspend fun updatePlayer(player: Player)

    suspend fun insertPlayer(player: Player): String

    suspend fun deletePlayer(playerId: String)

    /**
     * Get all players directly (not as a Flow) for migration purposes.
     * @return List of all players
     */
    suspend fun getAllPlayersDirect(): List<Player>

    /**
     * Clear all player data from local storage.
     * Only applicable for local data sources.
     */
    suspend fun clearLocalData()
}
