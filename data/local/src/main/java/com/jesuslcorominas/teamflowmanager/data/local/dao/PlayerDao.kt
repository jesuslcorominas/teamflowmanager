package com.jesuslcorominas.teamflowmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.jesuslcorominas.teamflowmanager.data.local.entity.PlayerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
    @Query("SELECT * FROM players ORDER BY number ASC")
    fun getAllPlayers(): Flow<List<PlayerEntity>>

    @Insert
    suspend fun insertPlayer(player: PlayerEntity)

    @Update
    suspend fun updatePlayer(player: PlayerEntity)

    @Query("DELETE FROM players WHERE id = :playerId")
    suspend fun deletePlayer(playerId: Long)

}
