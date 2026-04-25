package com.jesuslcorominas.teamflowmanager.usecase.repository

import com.jesuslcorominas.teamflowmanager.domain.model.Goal
import kotlinx.coroutines.flow.Flow

interface GoalRepository {
    fun getMatchGoals(
        matchId: Long,
        teamId: String? = null,
    ): Flow<List<Goal>>

    fun getAllTeamGoals(): Flow<List<Goal>>

    suspend fun insertGoal(goal: Goal): Long
}
