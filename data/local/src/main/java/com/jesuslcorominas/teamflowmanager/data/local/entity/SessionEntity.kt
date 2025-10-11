package com.jesuslcorominas.teamflowmanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jesuslcorominas.teamflowmanager.domain.model.Session

@Entity(tableName = "session")
data class SessionEntity(
    @PrimaryKey
    val id: Long = 1L,
    val elapsedTimeMillis: Long = 0L,
    val isRunning: Boolean = false,
    val lastStartTimeMillis: Long? = null,
)

fun SessionEntity.toDomain(): Session =
    Session(
        id = id,
        elapsedTimeMillis = elapsedTimeMillis,
        isRunning = isRunning,
        lastStartTimeMillis = lastStartTimeMillis,
    )

fun Session.toEntity(): SessionEntity =
    SessionEntity(
        id = id,
        elapsedTimeMillis = elapsedTimeMillis,
        isRunning = isRunning,
        lastStartTimeMillis = lastStartTimeMillis,
    )
