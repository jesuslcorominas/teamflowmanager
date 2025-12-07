package com.jesuslcorominas.teamflowmanager.usecase.repository

import com.jesuslcorominas.teamflowmanager.domain.model.Goal
import kotlinx.coroutines.flow.Flow

interface GoalRepository {
    fun getMatchGoals(matchId: Long): Flow<List<Goal>>

    fun getAllTeamGoals(): Flow<List<Goal>>

    suspend fun insertGoal(goal: Goal): Long

    /**
     * Get all local goals directly (not as a Flow) for migration purposes.
     * @return List of all goals
     */
    suspend fun getAllLocalGoalsDirect(): List<Goal>

    /**
     * Clear local goal data from Room database.
     * Used after successful migration to Firestore.
     */
    suspend fun clearLocalGoalData()
}
