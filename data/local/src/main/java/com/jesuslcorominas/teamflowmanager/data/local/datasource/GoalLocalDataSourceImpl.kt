package com.jesuslcorominas.teamflowmanager.data.local.datasource

import com.jesuslcorominas.teamflowmanager.data.core.datasource.GoalDataSource
import com.jesuslcorominas.teamflowmanager.data.local.dao.GoalDao
import com.jesuslcorominas.teamflowmanager.data.local.entity.toDomain
import com.jesuslcorominas.teamflowmanager.data.local.entity.toEntity
import com.jesuslcorominas.teamflowmanager.domain.model.Goal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class GoalLocalDataSourceImpl(
    private val goalDao: GoalDao,
) : GoalDataSource {
    override fun getMatchGoals(matchId: Long): Flow<List<Goal>> =
        goalDao.getMatchGoals(matchId).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getAllTeamGoals(): Flow<List<Goal>> =
        goalDao.getAllTeamGoals().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun insertGoal(goal: Goal): Long =
        goalDao.insert(goal.toEntity())
}
