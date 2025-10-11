package com.jesuslcorominas.teamflowmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.jesuslcorominas.teamflowmanager.data.local.entity.PlayerTimeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerTimeDao {
    @Query("SELECT * FROM player_time WHERE playerId = :playerId")
    fun getPlayerTime(playerId: Long): Flow<PlayerTimeEntity?>

    @Query("SELECT * FROM player_time")
    fun getAllPlayerTimes(): Flow<List<PlayerTimeEntity>>

    @Upsert
    suspend fun upsert(playerTime: PlayerTimeEntity)

    @Query("DELETE FROM player_time")
    suspend fun deleteAll()
}
