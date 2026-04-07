package com.jesuslcorominas.teamflowmanager.data.remote.api.model

import kotlinx.serialization.Serializable

/**
 * Request body for sending a push notification via Cloud Functions.
 */
@Serializable
internal data class SendNotificationRequest(
    val token: String,
    val title: String,
    val body: String,
)
