package com.jesuslcorominas.teamflowmanager.domain.model

data class PresidentNotification(
    val id: String,
    val type: NotificationType,
    val title: String,
    val body: String,
    val userData: Map<String, String>,
    val createdAt: Long,
    val read: Boolean,
)
