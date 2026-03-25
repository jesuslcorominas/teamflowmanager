package com.jesuslcorominas.teamflowmanager.data.remote.api.model

import kotlinx.serialization.Serializable

/**
 * Response containing the generated short link from Cloud Functions.
 *
 * This model is internal to the data-remote module and should not be exposed
 * outside of it.
 */
@Serializable
internal data class CreateShortLinkResponse(
    val shortLink: String,
    val linkId: String? = null,
)
