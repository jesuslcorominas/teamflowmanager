package com.jesuslcorominas.teamflowmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.jesuslcorominas.teamflowmanager.data.local.entity.PlayerTimeHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerTimeHistoryDao {
    @Query("SELECT * FROM player_time_history WHERE playerId = :playerId")
    fun getPlayerTimeHistory(playerId: Long): Flow<List<PlayerTimeHistoryEntity>>

    @Query("SELECT * FROM player_time_history WHERE matchId = :matchId")
    fun getMatchPlayerTimeHistory(matchId: Long): Flow<List<PlayerTimeHistoryEntity>>

    @Query("SELECT * FROM player_time_history")
    fun getAllPlayerTimeHistory(): Flow<List<PlayerTimeHistoryEntity>>

    @Query("SELECT * FROM player_time_history")
    suspend fun getAllPlayerTimeHistoryDirect(): List<PlayerTimeHistoryEntity>

    @Insert
    suspend fun insert(playerTimeHistory: PlayerTimeHistoryEntity): Long

    @Query("DELETE FROM player_time_history")
    suspend fun deleteAllPlayerTimeHistory()
}
