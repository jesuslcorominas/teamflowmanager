package com.jesuslcorominas.teamflowmanager.data.core.datasource

import com.jesuslcorominas.teamflowmanager.domain.model.Goal
import kotlinx.coroutines.flow.Flow

interface GoalDataSource {
    fun getMatchGoals(matchId: Long): Flow<List<Goal>>

    fun getAllTeamGoals(): Flow<List<Goal>>

    suspend fun insertGoal(goal: Goal): Long
}
