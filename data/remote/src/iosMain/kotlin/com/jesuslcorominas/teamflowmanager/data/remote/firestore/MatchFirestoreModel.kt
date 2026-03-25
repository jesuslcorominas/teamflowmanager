package com.jesuslcorominas.teamflowmanager.data.remote.firestore

import com.jesuslcorominas.teamflowmanager.data.remote.util.toStableId
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchPeriod
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.domain.model.PeriodType
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * iOS Firestore model for Match — uses @Serializable (kotlinx.serialization) instead
 * of @DocumentId/@PropertyName (Android Firebase SDK only).
 * The document ID is injected externally from DocumentSnapshot.id.
 */
@Serializable
data class MatchFirestoreModel(
    @Transient val id: String = "",
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
)

@Serializable
data class MatchPeriodFirestoreModel(
    val periodNumber: Int = 0,
    val periodDuration: Long = 0L,
    val startTimeMillis: Long = 0L,
    val endTimeMillis: Long = 0L,
)

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

fun MatchPeriodFirestoreModel.toDomain(): MatchPeriod =
    MatchPeriod(
        periodNumber = periodNumber,
        periodDuration = periodDuration,
        startTimeMillis = startTimeMillis,
        endTimeMillis = endTimeMillis,
    )

fun Match.toFirestoreModel(): MatchFirestoreModel =
    MatchFirestoreModel(
        id = "", // set by data source from document ID
        teamId = "", // set by data source
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

fun MatchPeriod.toFirestoreModel(): MatchPeriodFirestoreModel =
    MatchPeriodFirestoreModel(
        periodNumber = periodNumber,
        periodDuration = periodDuration,
        startTimeMillis = startTimeMillis,
        endTimeMillis = endTimeMillis,
    )
