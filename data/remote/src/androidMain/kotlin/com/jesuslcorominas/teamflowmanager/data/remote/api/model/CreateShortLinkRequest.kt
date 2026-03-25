package com.jesuslcorominas.teamflowmanager.data.remote.api.model

import kotlinx.serialization.Serializable

/**
 * Request body for creating a short link via Cloud Functions.
 *
 * This model is internal to the data-remote module and should not be exposed
 * outside of it.
 */
@Serializable
internal data class CreateShortLinkRequest(
    val teamId: String,
    val teamName: String,
)
