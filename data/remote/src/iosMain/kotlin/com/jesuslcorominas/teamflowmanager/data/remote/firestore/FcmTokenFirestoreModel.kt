package com.jesuslcorominas.teamflowmanager.data.remote.firestore

import kotlinx.serialization.Serializable

/**
 * Document of the `fcmTokens` collection. Same fields Android writes in
 * `AndroidFcmDataSourceImpl`, so both platforms share the collection.
 */
@Serializable
data class FcmTokenFirestoreModel(
    val userId: String = "",
    val token: String = "",
    val platform: String = "",
    val topic: String? = null,
    val updatedAt: Long = 0L,
)
