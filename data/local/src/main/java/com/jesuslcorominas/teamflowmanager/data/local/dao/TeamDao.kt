package com.jesuslcorominas.teamflowmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jesuslcorominas.teamflowmanager.data.local.entity.TeamEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TeamDao {
    @Query("SELECT * FROM team LIMIT 1")
    fun getTeam(): Flow<TeamEntity?>

    @Query("SELECT * FROM team LIMIT 1")
    suspend fun getTeamDirect(): TeamEntity?

    @Query("SELECT * FROM team WHERE coachId = :coachId LIMIT 1")
    fun getTeamByCoachId(coachId: String): Flow<TeamEntity?>

    @Query("SELECT EXISTS(SELECT 1 FROM team WHERE coachId IS NULL LIMIT 1)")
    suspend fun hasLocalTeamWithoutUserId(): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeam(team: TeamEntity)

    @Update
    suspend fun updateTeam(team: TeamEntity)

    @Query("DELETE FROM team WHERE id = :teamId")
    suspend fun deleteTeamById(teamId: Long)

    @Query("DELETE FROM team")
    suspend fun deleteAllTeams()
}
