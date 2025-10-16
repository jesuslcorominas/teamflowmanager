package com.jesuslcorominas.teamflowmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.jesuslcorominas.teamflowmanager.data.local.entity.PlayerEntity
import kotlinx.coroutines.flow.Flow

package com.jesuslcorominas.teamflowmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.jesuslcorominas.teamflowmanager.data.local.entity.PlayerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
    @Query("SELECT * FROM players ORDER BY number ASC")
    fun getAllPlayers(): Flow<List<PlayerEntity>>

    @Query("SELECT * FROM players WHERE isCaptain = 1 LIMIT 1")
    suspend fun getCaptainPlayer(): PlayerEntity?

    @Query("UPDATE players SET isCaptain = 0")
    suspend fun clearAllCaptains()

    @Query("SELECT * FROM players WHERE id = :playerId LIMIT 1")
    suspend fun getPlayerById(playerId: Long): PlayerEntity?

    @Insert
    suspend fun insertPlayer(player: PlayerEntity)

    @Update
    suspend fun updatePlayer(player: PlayerEntity)

    @Query("DELETE FROM players WHERE id = :playerId")
    suspend fun deletePlayer(playerId: Long)

    @Transaction
    suspend fun setPlayerAsCaptain(playerId: Long) {
        clearAllCaptains()
        val player = getPlayerById(playerId)
        if (player != null) {
            updatePlayer(player.copy(isCaptain = true))
        }
    }

    @Transaction
    suspend fun removePlayerAsCaptain(playerId: Long) {
        val player = getPlayerById(playerId)
        if (player != null) {
            updatePlayer(player.copy(isCaptain = false))
        }
    }

}
