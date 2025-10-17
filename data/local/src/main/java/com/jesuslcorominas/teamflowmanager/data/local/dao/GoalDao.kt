package com.jesuslcorominas.teamflowmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.jesuslcorominas.teamflowmanager.data.local.entity.GoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query("SELECT * FROM goal WHERE matchId = :matchId ORDER BY goalTimeMillis ASC")
    fun getMatchGoals(matchId: Long): Flow<List<GoalEntity>>

    @Insert
    suspend fun insert(goal: GoalEntity): Long
}
