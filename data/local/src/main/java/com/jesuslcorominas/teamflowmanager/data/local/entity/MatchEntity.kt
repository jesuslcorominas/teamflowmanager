package com.jesuslcorominas.teamflowmanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchPeriod
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.domain.model.PeriodType
import com.squareup.moshi.JsonClass

@Entity(tableName = "match")
data class MatchEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val teamId: Long = 1L,
    val teamName: String = "",
    val opponent: String = "",
    val location: String = "",
    val dateTime: Long? = null,
    val numberOfPeriods: Int = 2,
    val squadCallUpIds: String = "",
    val captainId: Long,
    val startingLineupIds: String = "",
    val elapsedTimeMillis: Long = 0L,
    val lastStartTimeMillis: Long? = null,
    val status: String = MatchStatus.SCHEDULED.name,
    val archived: Boolean = false,
    val currentPeriod: Int = 1,
    val pauseCount: Int = 0,
    val goals: Int = 0,
    val opponentGoals: Int = 0,
    val periods: List<MatchPeriodEntity>,
    val periodType: Int,
)

@JsonClass(generateAdapter = true)
data class MatchPeriodEntity(
    val periodNumber: Int,
    val periodDuration: Long = 0L,
    val startTimeMillis: Long = 0L,
    val endTimeMillis: Long = 0L,
)

fun MatchEntity.toDomain(): Match =
    Match(
        id = id,
        teamId = teamId,
        teamName = teamName,
        opponent = opponent,
        location = location,
        dateTime = dateTime,
        squadCallUpIds = squadCallUpIds.split(",").mapNotNull { it.toLongOrNull() },
        captainId = captainId,
        startingLineupIds = startingLineupIds.split(",").mapNotNull { it.toLongOrNull() },
        status = try {
            MatchStatus.valueOf(status)
        } catch (e: Exception) {
            MatchStatus.SCHEDULED
        },
        archived = archived,
        pauseCount = pauseCount,
        goals = goals,
        opponentGoals = opponentGoals,
        periods = periods.map { it.toDomain() },
        periodType = PeriodType.fromNumberOfPeriods(numberOfPeriods)
    )

fun Match.toEntity(): MatchEntity =
    MatchEntity(
        id = id,
        teamId = teamId,
        teamName = teamName,
        opponent = opponent,
        location = location,
        dateTime = dateTime,
        squadCallUpIds = squadCallUpIds.joinToString(","),
        captainId = captainId,
        startingLineupIds = startingLineupIds.joinToString(","),
        status = status.name,
        archived = archived,
        pauseCount = pauseCount,
        goals = goals,
        opponentGoals = opponentGoals,
        periods = periods.map { it.toEntity() },
        periodType = periodType.numberOfPeriods
    )

fun MatchPeriodEntity.toDomain(): MatchPeriod = MatchPeriod(
    periodNumber = periodNumber,
    periodDuration = periodDuration,
    startTimeMillis = startTimeMillis,
    endTimeMillis = endTimeMillis,
)

fun MatchPeriod.toEntity(): MatchPeriodEntity = MatchPeriodEntity(
    periodNumber = periodNumber,
    periodDuration = periodDuration,
    startTimeMillis = startTimeMillis,
    endTimeMillis = endTimeMillis,
)
