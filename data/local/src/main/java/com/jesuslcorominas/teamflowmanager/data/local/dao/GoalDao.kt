package com.jesuslcorominas.teamflowmanager.data.local.dao

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.jesuslcorominas.teamflowmanager.data.local.database.TeamFlowManagerDatabase
import com.jesuslcorominas.teamflowmanager.data.local.entity.GoalEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class GoalDao(
    private val database: TeamFlowManagerDatabase
) {
    fun getMatchGoals(matchId: Long): Flow<List<GoalEntity>> =
        database.goalQueries
            .getMatchGoals(matchId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { goals ->
                goals.map {
                    GoalEntity(
                        id = it.id,
                        matchId = it.matchId,
                        scorerId = it.scorerId,
                        goalTimeMillis = it.goalTimeMillis,
                        matchElapsedTimeMillis = it.matchElapsedTimeMillis,
                        isOpponentGoal = it.isOpponentGoal
                    )
                }
            }

    fun getAllTeamGoals(): Flow<List<GoalEntity>> =
        database.goalQueries
            .getAllTeamGoals()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { goals ->
                goals.map {
                    GoalEntity(
                        id = it.id,
                        matchId = it.matchId,
                        scorerId = it.scorerId,
                        goalTimeMillis = it.goalTimeMillis,
                        matchElapsedTimeMillis = it.matchElapsedTimeMillis,
                        isOpponentGoal = it.isOpponentGoal
                    )
                }
            }

    suspend fun insert(goal: GoalEntity): Long {
        database.goalQueries.insertGoal(
            matchId = goal.matchId,
            scorerId = goal.scorerId,
            goalTimeMillis = goal.goalTimeMillis,
            matchElapsedTimeMillis = goal.matchElapsedTimeMillis,
            isOpponentGoal = if (goal.isOpponentGoal) 1L else 0L
        )
        return database.goalQueries.lastInsertRowId().executeAsOne()
    }
}
