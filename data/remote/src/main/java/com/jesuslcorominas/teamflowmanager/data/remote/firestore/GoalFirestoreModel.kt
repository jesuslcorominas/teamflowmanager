package com.jesuslcorominas.teamflowmanager.data.remote.firestore

import com.google.firebase.firestore.DocumentId
import com.jesuslcorominas.teamflowmanager.data.remote.util.toStableId
import com.jesuslcorominas.teamflowmanager.domain.model.Goal

/**
 * Firestore model for Goal document.
 * This model is used for serialization/deserialization with Firestore.
 * The `id` field is automatically populated by Firestore with the document ID.
 * The `teamId` field stores the Firestore document ID of the team, which is used by
 * security rules to validate that the authenticated user is the owner of the team.
 */
data class GoalFirestoreModel(
    @DocumentId
    val id: String = "",
    val teamId: String = "",
    val matchId: Long = 0L,
    val scorerId: Long? = null,
    val goalTimeMillis: Long = 0L,
    val matchElapsedTimeMillis: Long = 0L,
    val isOpponentGoal: Boolean = false,
    val isOwnGoal: Boolean = false,
) {
    // No-arg constructor required by Firestore
    constructor() : this(
        id = "",
        teamId = "",
        matchId = 0L,
        scorerId = null,
        goalTimeMillis = 0L,
        matchElapsedTimeMillis = 0L,
        isOpponentGoal = false,
        isOwnGoal = false,
    )
}

fun GoalFirestoreModel.toDomain(): Goal =
    Goal(
        id = id.toStableId(),
        matchId = matchId,
        scorerId = scorerId,
        goalTimeMillis = goalTimeMillis,
        matchElapsedTimeMillis = matchElapsedTimeMillis,
        isOpponentGoal = isOpponentGoal,
        isOwnGoal = isOwnGoal,
    )

fun Goal.toFirestoreModel(): GoalFirestoreModel =
    GoalFirestoreModel(
        id = "", // Will be set when inserting
        teamId = "", // Will be set by the data source
        matchId = matchId,
        scorerId = scorerId,
        goalTimeMillis = goalTimeMillis,
        matchElapsedTimeMillis = matchElapsedTimeMillis,
        isOpponentGoal = isOpponentGoal,
        isOwnGoal = isOwnGoal,
    )
