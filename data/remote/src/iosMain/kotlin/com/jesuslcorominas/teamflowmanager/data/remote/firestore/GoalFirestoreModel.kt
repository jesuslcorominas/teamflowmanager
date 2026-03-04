package com.jesuslcorominas.teamflowmanager.data.remote.firestore

import com.jesuslcorominas.teamflowmanager.data.remote.util.toStableId
import com.jesuslcorominas.teamflowmanager.domain.model.Goal
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * iOS Firestore model for Goal — uses @Serializable (kotlinx.serialization).
 * Note: Android's @PropertyName("opponentGoal") / @PropertyName("ownGoal") map to
 * the field names stored in Firestore. We mirror those names directly here.
 * The document ID is injected externally from DocumentSnapshot.id.
 */
@Serializable
data class GoalFirestoreModel(
    @Transient val id: String = "",
    val teamId: String = "",
    val matchId: Long = 0L,
    val matchDocId: String = "",
    val scorerId: Long? = null,
    val goalTimeMillis: Long = 0L,
    val matchElapsedTimeMillis: Long = 0L,
    val opponentGoal: Boolean = false,
    val ownGoal: Boolean = false,
)

fun GoalFirestoreModel.toDomain(): Goal =
    Goal(
        id = id.toStableId(),
        matchId = matchId,
        scorerId = scorerId,
        goalTimeMillis = goalTimeMillis,
        matchElapsedTimeMillis = matchElapsedTimeMillis,
        isOpponentGoal = opponentGoal,
        isOwnGoal = ownGoal,
    )

fun Goal.toFirestoreModel(): GoalFirestoreModel =
    GoalFirestoreModel(
        teamId = "",
        matchId = matchId,
        matchDocId = "",
        scorerId = scorerId,
        goalTimeMillis = goalTimeMillis,
        matchElapsedTimeMillis = matchElapsedTimeMillis,
        opponentGoal = isOpponentGoal,
        ownGoal = isOwnGoal,
    )
