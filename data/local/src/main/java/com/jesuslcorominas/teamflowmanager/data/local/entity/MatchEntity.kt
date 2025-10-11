package com.jesuslcorominas.teamflowmanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jesuslcorominas.teamflowmanager.domain.model.Match

@Entity(tableName = "match")
data class MatchEntity(
    @PrimaryKey
    val id: Long = 1L,
    val elapsedTimeMillis: Long = 0L,
    val isRunning: Boolean = false,
    val lastStartTimeMillis: Long? = null,
)

fun MatchEntity.toDomain(): Match =
    Match(
        id = id,
        elapsedTimeMillis = elapsedTimeMillis,
        isRunning = isRunning,
        lastStartTimeMillis = lastStartTimeMillis,
    )

fun Match.toEntity(): MatchEntity =
    MatchEntity(
        id = id,
        elapsedTimeMillis = elapsedTimeMillis,
        isRunning = isRunning,
        lastStartTimeMillis = lastStartTimeMillis,
    )
