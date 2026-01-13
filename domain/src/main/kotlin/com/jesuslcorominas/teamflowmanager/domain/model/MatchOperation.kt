package com.jesuslcorominas.teamflowmanager.domain.model

data class MatchOperation(
    val id: String = "",
    val matchId: Long,
    val type: MatchOperationType,
    val status: MatchOperationStatus = MatchOperationStatus.IN_PROGRESS,
    val createdAt: Long = System.currentTimeMillis()
)
