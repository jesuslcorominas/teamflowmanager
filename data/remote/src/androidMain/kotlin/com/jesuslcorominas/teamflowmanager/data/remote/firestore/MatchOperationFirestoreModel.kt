package com.jesuslcorominas.teamflowmanager.data.remote.firestore

import com.google.firebase.firestore.DocumentId
import com.jesuslcorominas.teamflowmanager.data.remote.util.toStableId
import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperation
import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperationStatus
import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperationType

/**
 * Firestore model for MatchOperation document.
 * Operations are stored in matchOperations/{operationId} collection.
 */
data class MatchOperationFirestoreModel(
    @DocumentId
    val id: String = "",
    val matchId: Long = 0L,
    val teamId: String = "",
    val type: String = MatchOperationType.START.name,
    val status: String = MatchOperationStatus.IN_PROGRESS.name,
    val createdAt: Long = 0L,
) {
    // No-arg constructor required by Firestore
    constructor() : this(
        id = "",
        matchId = 0L,
        teamId = "",
        type = MatchOperationType.START.name,
        status = MatchOperationStatus.IN_PROGRESS.name,
        createdAt = 0L,
    )
}

fun MatchOperationFirestoreModel.toDomain(): MatchOperation =
    MatchOperation(
        id = id,
        matchId = matchId,
        teamId = teamId.toStableId(),
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
        teamId = "", // Will be set by the data source
        type = type.name,
        status = status.name,
        createdAt = createdAt,
    )
