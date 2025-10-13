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
    val opponent: String? = null,
    val location: String? = null,
    val date: Long? = null,
    val startingLineupIds: String = "", // Comma-separated IDs
    val substituteIds: String = "", // Comma-separated IDs
    val elapsedTimeMillis: Long = 0L,
    val isRunning: Boolean = false,
    val lastStartTimeMillis: Long? = null,
    val status: String = MatchStatus.SCHEDULED.name,
)

fun MatchEntity.toDomain(): Match =
    Match(
        id = id,
        teamId = teamId,
        opponent = opponent,
        location = location,
        date = date,
        startingLineupIds = startingLineupIds.split(",").mapNotNull { it.toLongOrNull() },
        substituteIds = substituteIds.split(",").mapNotNull { it.toLongOrNull() },
        elapsedTimeMillis = elapsedTimeMillis,
        isRunning = isRunning,
        lastStartTimeMillis = lastStartTimeMillis,
        status = try { MatchStatus.valueOf(status) } catch (e: Exception) { MatchStatus.SCHEDULED },
    )

fun Match.toEntity(): MatchEntity =
    MatchEntity(
        id = id,
        teamId = teamId,
        opponent = opponent,
        location = location,
        date = date,
        startingLineupIds = startingLineupIds.joinToString(","),
        substituteIds = substituteIds.joinToString(","),
        elapsedTimeMillis = elapsedTimeMillis,
        isRunning = isRunning,
        lastStartTimeMillis = lastStartTimeMillis,
        status = status.name,
    )
