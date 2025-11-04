package com.jesuslcorominas.teamflowmanager.data.local.entity

import com.jesuslcorominas.teamflowmanager.domain.model.Goal

data class GoalEntity(
    val id: Long = 0L,
    val matchId: Long,
    val scorerId: Long?,
    val goalTimeMillis: Long,
    val matchElapsedTimeMillis: Long,
    val isOpponentGoal: Boolean = false,
)

fun GoalEntity.toDomain(): Goal =
    Goal(
        id = id,
        matchId = matchId,
        scorerId = scorerId,
        goalTimeMillis = goalTimeMillis,
        matchElapsedTimeMillis = matchElapsedTimeMillis,
        isOpponentGoal = isOpponentGoal,
    )

fun Goal.toEntity(): GoalEntity =
    GoalEntity(
        id = id,
        matchId = matchId,
        scorerId = scorerId,
        goalTimeMillis = goalTimeMillis,
        matchElapsedTimeMillis = matchElapsedTimeMillis,
        isOpponentGoal = isOpponentGoal,
    )
