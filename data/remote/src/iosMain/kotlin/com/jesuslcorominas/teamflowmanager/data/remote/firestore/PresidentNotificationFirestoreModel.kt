package com.jesuslcorominas.teamflowmanager.data.remote.firestore

import kotlinx.serialization.Serializable

@Serializable
data class PresidentNotificationFirestoreModel(
    override val id: String = "",
    override val type: String = "",
    override val title: String = "",
    override val body: String = "",
    override val userData: Map<String, String> = emptyMap(),
    override val createdAt: Long = 0L,
    override val read: Boolean = false,
) : PresidentNotificationFields
