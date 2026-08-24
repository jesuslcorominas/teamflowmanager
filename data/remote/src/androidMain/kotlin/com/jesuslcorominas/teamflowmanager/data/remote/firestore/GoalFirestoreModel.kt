package com.jesuslcorominas.teamflowmanager.data.remote.firestore

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import com.jesuslcorominas.teamflowmanager.domain.model.Goal

/**
 * Firestore model for Goal document.
 */
data class GoalFirestoreModel(
    @DocumentId
    val id: String = "",
    val teamId: String = "",
    val matchId: String = "",
    val scorerId: String? = null,
    val goalTimeMillis: Long = 0L,
    val matchElapsedTimeMillis: Long = 0L,
    @get:PropertyName("opponentGoal")
    @set:PropertyName("opponentGoal")
    var isOpponentGoal: Boolean = false,
    @get:PropertyName("ownGoal")
    @set:PropertyName("ownGoal")
    var isOwnGoal: Boolean = false,
) {
    // No-arg constructor required by Firestore
    constructor() : this(
        id = "",
        teamId = "",
        matchId = "",
        scorerId = null,
        goalTimeMillis = 0L,
        matchElapsedTimeMillis = 0L,
        isOpponentGoal = false,
        isOwnGoal = false,
    )
}

fun GoalFirestoreModel.toDomain(): Goal =
    Goal(
        id = id,
        matchId = matchId,
        scorerId = scorerId,
        goalTimeMillis = goalTimeMillis,
        matchElapsedTimeMillis = matchElapsedTimeMillis,
        isOpponentGoal = isOpponentGoal,
        isOwnGoal = isOwnGoal,
    )

fun Goal.toFirestoreModel(): GoalFirestoreModel =
    GoalFirestoreModel(
        id = id,
        teamId = "",
        matchId = matchId,
        scorerId = scorerId,
        goalTimeMillis = goalTimeMillis,
        matchElapsedTimeMillis = matchElapsedTimeMillis,
        isOpponentGoal = isOpponentGoal,
        isOwnGoal = isOwnGoal,
    )
