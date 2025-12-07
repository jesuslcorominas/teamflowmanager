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

    @Query("SELECT * FROM match WHERE id = :matchId LIMIT 1")
    fun getMatchById(matchId: Long): Flow<MatchEntity?>

    @Query("SELECT * FROM match WHERE archived = 0 ORDER BY dateTime DESC")
    fun getAllMatches(): Flow<List<MatchEntity>>

    @Query("SELECT * FROM match ORDER BY dateTime DESC")
    suspend fun getAllMatchesDirect(): List<MatchEntity>

    @Query("SELECT * FROM match WHERE archived = 1 ORDER BY dateTime DESC")
    fun getArchivedMatches(): Flow<List<MatchEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertMatch(match: MatchEntity): Long

    @Update
    suspend fun updateMatch(match: MatchEntity)

    @Query("DELETE FROM match WHERE id = :matchId")
    suspend fun deleteMatch(matchId: Long)

    @Query("DELETE FROM match")
    suspend fun deleteAllMatches()

    @Query("SELECT * FROM match WHERE status = 'SCHEDULED' AND archived = 0")
    suspend fun getScheduledMatches(): List<MatchEntity>

    @Query("UPDATE match SET captainId = :captainId WHERE id = :matchId")
    suspend fun updateMatchCaptain(matchId: Long, captainId: Long?)
}
