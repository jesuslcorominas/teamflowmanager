package com.jesuslcorominas.teamflowmanager.data.remote.firestore

import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperation
import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperationStatus
import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperationType
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class MatchOperationFirestoreModel(
    @Transient val id: String = "",
    val matchId: String = "",
    val teamId: String = "",
    val type: String = MatchOperationType.START.name,
    val status: String = MatchOperationStatus.IN_PROGRESS.name,
    val createdAt: Long = 0L,
)

fun MatchOperationFirestoreModel.toDomain(): MatchOperation =
    MatchOperation(
        id = id,
        matchId = matchId,
        teamId = teamId,
        type =
            try {
                MatchOperationType.valueOf(type)
            } catch (_: Exception) {
                MatchOperationType.START
            },
        status =
            try {
                MatchOperationStatus.valueOf(status)
            } catch (_: Exception) {
                MatchOperationStatus.IN_PROGRESS
            },
        createdAt = createdAt,
    )

fun MatchOperation.toFirestoreModel(): MatchOperationFirestoreModel =
    MatchOperationFirestoreModel(
        id = id,
        matchId = matchId,
        teamId = teamId,
        type = type.name,
        status = status.name,
        createdAt = createdAt,
    )
