package com.jesuslcorominas.teamflowmanager.data.remote.firestore

import com.jesuslcorominas.teamflowmanager.domain.model.Goal
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class GoalFirestoreModel(
    @Transient val id: String = "",
    val teamId: String = "",
    val matchId: String = "",
    val scorerId: String? = null,
    val goalTimeMillis: Long = 0L,
    val matchElapsedTimeMillis: Long = 0L,
    val opponentGoal: Boolean = false,
    val ownGoal: Boolean = false,
)

fun GoalFirestoreModel.toDomain(): Goal =
    Goal(
        id = id,
        matchId = matchId,
        scorerId = scorerId,
        goalTimeMillis = goalTimeMillis,
        matchElapsedTimeMillis = matchElapsedTimeMillis,
        isOpponentGoal = opponentGoal,
        isOwnGoal = ownGoal,
    )

fun Goal.toFirestoreModel(): GoalFirestoreModel =
    GoalFirestoreModel(
        id = id,
        teamId = "",
        matchId = matchId,
        scorerId = scorerId,
        goalTimeMillis = goalTimeMillis,
        matchElapsedTimeMillis = matchElapsedTimeMillis,
        opponentGoal = isOpponentGoal,
        ownGoal = isOwnGoal,
    )
