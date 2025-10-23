package com.jesuslcorominas.teamflowmanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus

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
    val captainId: Long? = null,
    val startingLineupIds: String = "",
    val elapsedTimeMillis: Long = 0L,
    val lastStartTimeMillis: Long? = null,
    val status: String = MatchStatus.SCHEDULED.name,
    val archived: Boolean = false,
    val currentPeriod: Int = 1,
    val pauseCount: Int = 0,
    val goals: Int = 0,
    val opponentGoals: Int = 0,
)

fun MatchEntity.toDomain(): Match =
    Match(
        id = id,
        teamId = teamId,
        teamName = teamName,
        opponent = opponent,
        location = location,
        dateTime = dateTime,
        numberOfPeriods = numberOfPeriods,
        squadCallUpIds = squadCallUpIds.split(",").mapNotNull { it.toLongOrNull() },
        captainId = captainId,
        startingLineupIds = startingLineupIds.split(",").mapNotNull { it.toLongOrNull() },
        elapsedTimeMillis = elapsedTimeMillis,
        lastStartTimeMillis = lastStartTimeMillis,
        status = try {
            MatchStatus.valueOf(status)
        } catch (e: Exception) {
            MatchStatus.SCHEDULED
        },
        archived = archived,
        currentPeriod = currentPeriod,
        pauseCount = pauseCount,
        goals = goals,
        opponentGoals = opponentGoals,
    )

fun Match.toEntity(): MatchEntity =
    MatchEntity(
        id = id,
        teamId = teamId,
        teamName = teamName,
        opponent = opponent,
        location = location,
        dateTime = dateTime,
        numberOfPeriods = numberOfPeriods,
        squadCallUpIds = squadCallUpIds.joinToString(","),
        captainId = captainId,
        startingLineupIds = startingLineupIds.joinToString(","),
        elapsedTimeMillis = elapsedTimeMillis,
        lastStartTimeMillis = lastStartTimeMillis,
        status = status.name,
        archived = archived,
        currentPeriod = currentPeriod,
        pauseCount = pauseCount,
        goals = goals,
        opponentGoals = opponentGoals,
    )
