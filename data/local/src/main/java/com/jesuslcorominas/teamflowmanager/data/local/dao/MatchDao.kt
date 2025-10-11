package com.jesuslcorominas.teamflowmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jesuslcorominas.teamflowmanager.data.local.entity.MatchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchDao {
    @Query("SELECT * FROM match WHERE id = 1 LIMIT 1")
    fun getMatch(): Flow<MatchEntity?>

    @Query("SELECT * FROM match WHERE id = :matchId LIMIT 1")
    fun getMatchById(matchId: Long): Flow<MatchEntity?>

    @Query("SELECT * FROM match ORDER BY date DESC")
    fun getAllMatches(): Flow<List<MatchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMatch(match: MatchEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertMatch(match: MatchEntity): Long

    @Update
    suspend fun updateMatch(match: MatchEntity)

    @Query("DELETE FROM match WHERE id = :matchId")
    suspend fun deleteMatch(matchId: Long)
}
