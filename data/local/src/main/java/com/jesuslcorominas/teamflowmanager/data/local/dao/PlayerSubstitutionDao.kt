package com.jesuslcorominas.teamflowmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.jesuslcorominas.teamflowmanager.data.local.entity.PlayerSubstitutionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerSubstitutionDao {
    @Query("SELECT * FROM player_substitution WHERE matchId = :matchId ORDER BY substitutionTimeMillis ASC")
    fun getMatchSubstitutions(matchId: Long): Flow<List<PlayerSubstitutionEntity>>

    @Insert
    suspend fun insert(substitution: PlayerSubstitutionEntity): Long
}
