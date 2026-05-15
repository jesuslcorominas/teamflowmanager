package com.jesuslcorominas.teamflowmanager.data.core.datasource

import com.jesuslcorominas.teamflowmanager.domain.model.Goal
import kotlinx.coroutines.flow.Flow

interface GoalDataSource {
    fun getMatchGoals(matchId: String): Flow<List<Goal>>

    fun getAllTeamGoals(): Flow<List<Goal>>

    suspend fun insertGoal(goal: Goal): String

    /**
     * Get all goals directly (not as a Flow) for migration purposes.
     * @return List of all goals
     */
    suspend fun getAllGoalsDirect(): List<Goal>

    /**
     * Clear all goal data from local storage.
     * Only applicable for local data sources.
     */
    suspend fun clearLocalData()
}
