package com.jesuslcorominas.teamflowmanager.data.remote.firestore

import com.google.firebase.firestore.DocumentId

data class PresidentNotificationFirestoreModel(
    @DocumentId override val id: String = "",
    override val type: String = "",
    override val title: String = "",
    override val body: String = "",
    override val userData: Map<String, String> = emptyMap(),
    override val createdAt: Long = 0L,
    override val read: Boolean = false,
) : PresidentNotificationFields {
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
