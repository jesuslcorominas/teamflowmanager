package com.jesuslcorominas.teamflowmanager.data.remote.api.model

import kotlinx.serialization.Serializable

@Serializable
internal data class SendNotificationRequest(
    val token: String,
    val title: String? = null,
    val body: String? = null,
    val type: String? = null,
    val params: Map<String, String>? = null,
)
