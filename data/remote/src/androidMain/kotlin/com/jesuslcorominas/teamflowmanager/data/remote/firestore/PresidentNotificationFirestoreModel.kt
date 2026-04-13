package com.jesuslcorominas.teamflowmanager.data.remote.firestore

import com.google.firebase.firestore.DocumentId
import com.jesuslcorominas.teamflowmanager.domain.model.NotificationType
import com.jesuslcorominas.teamflowmanager.domain.model.PresidentNotification

data class PresidentNotificationFirestoreModel(
    @DocumentId val id: String = "",
    val type: String = "",
    val title: String = "",
    val body: String = "",
    val userData: Map<String, String> = emptyMap(),
    val createdAt: Long = 0L,
    val read: Boolean = false,
) {
    // No-arg constructor required by Firestore
    constructor() : this(
        id = "",
        type = "",
        title = "",
        body = "",
        userData = emptyMap(),
        createdAt = 0L,
        read = false,
    )
}

fun PresidentNotificationFirestoreModel.toDomain(): PresidentNotification =
    PresidentNotification(
        id = id,
        type = NotificationType.entries.firstOrNull { it.key == type } ?: NotificationType.USER_WAITING_FOR_ASSIGNMENT,
        title = title,
        body = body,
        userData = userData,
        createdAt = createdAt,
        read = read,
    )
