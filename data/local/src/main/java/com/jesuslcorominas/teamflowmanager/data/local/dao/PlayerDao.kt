package com.jesuslcorominas.teamflowmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.jesuslcorominas.teamflowmanager.data.local.entity.PlayerEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Player operations
 */
@Dao
interface PlayerDao {
    @Query("SELECT * FROM players")
    fun getAllPlayers(): Flow<List<PlayerEntity>>

    @Insert
    suspend fun insertPlayer(player: PlayerEntity)
}
