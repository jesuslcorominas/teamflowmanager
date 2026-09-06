package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.GoalDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.Goal
import com.jesuslcorominas.teamflowmanager.usecase.repository.GoalRepository
import kotlinx.coroutines.flow.Flow

internal class GoalRepositoryImpl(
    private val goalDataSource: GoalDataSource,
) : GoalRepository {
    override fun getMatchGoals(matchId: String): Flow<List<Goal>> = goalDataSource.getMatchGoals(matchId)

    override fun getAllTeamGoals(): Flow<List<Goal>> = goalDataSource.getAllTeamGoals()

    override suspend fun insertGoal(goal: Goal): String = goalDataSource.insertGoal(goal)
}
