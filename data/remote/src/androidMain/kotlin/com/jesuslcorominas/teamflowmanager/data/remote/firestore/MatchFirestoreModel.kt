package com.jesuslcorominas.teamflowmanager.data.remote.firestore

import com.google.firebase.firestore.DocumentId
import com.jesuslcorominas.teamflowmanager.data.remote.util.toStableId
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchPeriod
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.domain.model.PeriodType

/**
 * Firestore model for Match document.
 * This model is used for serialization/deserialization with Firestore.
 * The `id` field is automatically populated by Firestore with the document ID.
 * The `teamId` field stores the Firestore document ID of the team, which is used by
 * security rules to validate that the authenticated user is the owner of the team.
 */
data class MatchFirestoreModel(
    @DocumentId
    val id: String = "",
    val teamId: String = "",
    val teamName: String = "",
    val opponent: String = "",
    val location: String = "",
    val dateTime: Long? = null,
    val numberOfPeriods: Int = 2,
    val squadCallUpIds: List<Long> = emptyList(),
    val captainId: Long = 0L,
    val startingLineupIds: List<Long> = emptyList(),
    val status: String = MatchStatus.SCHEDULED.name,
    val archived: Boolean = false,
    val pauseCount: Int = 0,
    val goals: Int = 0,
    val opponentGoals: Int = 0,
    val timeoutStartTimeMillis: Long = 0L,
    val periods: List<MatchPeriodFirestoreModel> = emptyList(),
    val lastCompletedOperationId: String? = null,
) {
    // No-arg constructor required by Firestore
    constructor() : this(
        id = "",
        teamId = "",
        teamName = "",
        opponent = "",
        location = "",
        dateTime = null,
        numberOfPeriods = 2,
        squadCallUpIds = emptyList(),
        captainId = 0L,
        startingLineupIds = emptyList(),
        status = MatchStatus.SCHEDULED.name,
        archived = false,
        pauseCount = 0,
        goals = 0,
        opponentGoals = 0,
        timeoutStartTimeMillis = 0L,
        periods = emptyList(),
        lastCompletedOperationId = null,
    )
}

/**
 * Firestore model for MatchPeriod.
 */
data class MatchPeriodFirestoreModel(
    val periodNumber: Int = 0,
    val periodDuration: Long = 0L,
    val startTimeMillis: Long = 0L,
    val endTimeMillis: Long = 0L,
) {
    // No-arg constructor required by Firestore
    constructor() : this(
        periodNumber = 0,
        periodDuration = 0L,
        startTimeMillis = 0L,
        endTimeMillis = 0L,
    )
}

fun MatchFirestoreModel.toDomain(): Match {
    val periodType = PeriodType.fromNumberOfPeriods(numberOfPeriods)
    return Match(
        id = id.toStableId(),
        teamId = teamId.toStableId(),
        teamName = teamName,
        opponent = opponent,
        location = location,
        dateTime = dateTime,
        periodType = periodType,
        squadCallUpIds = squadCallUpIds,
        captainId = captainId,
        startingLineupIds = startingLineupIds,
        status =
            try {
                MatchStatus.valueOf(status)
            } catch (_: Exception) {
                MatchStatus.SCHEDULED
            },
        archived = archived,
        pauseCount = pauseCount,
        goals = goals,
        opponentGoals = opponentGoals,
        timeoutStartTimeMillis = timeoutStartTimeMillis,
        periods =
            if (periods.isNotEmpty()) {
                periods.map { it.toDomain() }
            } else {
                // Create default periods if none exist
                (1..periodType.numberOfPeriods).map {
                    MatchPeriod(
                        periodNumber = it,
                        periodDuration = periodType.duration,
                    )
                }
            },
        lastCompletedOperationId = lastCompletedOperationId,
    )
}

fun Match.toFirestoreModel(): MatchFirestoreModel =
    MatchFirestoreModel(
        id = "", // Will be set when inserting/updating
        teamId = "", // Will be set by the data source
        teamName = teamName,
        opponent = opponent,
        location = location,
        dateTime = dateTime,
        numberOfPeriods = periodType.numberOfPeriods,
        squadCallUpIds = squadCallUpIds,
        captainId = captainId,
        startingLineupIds = startingLineupIds,
        status = status.name,
        archived = archived,
        pauseCount = pauseCount,
        goals = goals,
        opponentGoals = opponentGoals,
        timeoutStartTimeMillis = timeoutStartTimeMillis,
        periods = periods.map { it.toFirestoreModel() },
        lastCompletedOperationId = lastCompletedOperationId,
    )

fun MatchPeriodFirestoreModel.toDomain(): MatchPeriod =
    MatchPeriod(
        periodNumber = periodNumber,
        periodDuration = periodDuration,
        startTimeMillis = startTimeMillis,
        endTimeMillis = endTimeMillis,
    )

fun MatchPeriod.toFirestoreModel(): MatchPeriodFirestoreModel =
    MatchPeriodFirestoreModel(
        periodNumber = periodNumber,
        periodDuration = periodDuration,
        startTimeMillis = startTimeMillis,
        endTimeMillis = endTimeMillis,
    )
