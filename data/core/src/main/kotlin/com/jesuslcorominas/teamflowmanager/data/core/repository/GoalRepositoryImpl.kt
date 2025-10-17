package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.GoalLocalDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.Goal
import com.jesuslcorominas.teamflowmanager.usecase.repository.GoalRepository
import kotlinx.coroutines.flow.Flow

internal class GoalRepositoryImpl(
    private val goalLocalDataSource: GoalLocalDataSource,
) : GoalRepository {
    override fun getMatchGoals(matchId: Long): Flow<List<Goal>> =
        goalLocalDataSource.getMatchGoals(matchId)

    override suspend fun insertGoal(goal: Goal): Long =
        goalLocalDataSource.insertGoal(goal)
}
