package com.jesuslcorominas.teamflowmanager.domain.model

import com.jesuslcorominas.teamflowmanager.domain.utils.currentTimeMillis

data class MatchOperation(
    val id: String = "",
    val matchId: String,
    val teamId: String,
    val type: MatchOperationType,
    val status: MatchOperationStatus = MatchOperationStatus.IN_PROGRESS,
    val createdAt: Long = currentTimeMillis(),
)
