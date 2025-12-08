package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.GoalDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.Goal
import com.jesuslcorominas.teamflowmanager.usecase.repository.GoalRepository
import kotlinx.coroutines.flow.Flow

internal class GoalRepositoryImpl(
    private val goalDataSource: GoalDataSource,
    private val goalLocalDataSource: GoalDataSource,
) : GoalRepository {
    override fun getMatchGoals(matchId: Long): Flow<List<Goal>> =
        goalDataSource.getMatchGoals(matchId)

    override fun getAllTeamGoals(): Flow<List<Goal>> =
        goalDataSource.getAllTeamGoals()

    override suspend fun insertGoal(goal: Goal): Long =
        goalDataSource.insertGoal(goal)

    override suspend fun getAllLocalGoalsDirect(): List<Goal> =
        goalLocalDataSource.getAllGoalsDirect()

    override suspend fun clearLocalGoalData() {
        goalLocalDataSource.clearLocalData()
    }
}
