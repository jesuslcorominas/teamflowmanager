package com.jesuslcorominas.teamflowmanager.data.remote.api

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.POST
import kotlinx.serialization.Serializable

/**
 * API interface for generating short links using Firebase Cloud Functions.
 *
 * This API replaces deprecated Firebase Dynamic Links with a custom solution
 * that uses Firebase Hosting + Cloud Functions to create short, clickable URLs.
 */
interface ShortLinkApi {
    /**
     * Creates a short link for team invitation.
     *
     * @param request The request containing team details
     * @return Response with the generated short link URL
     */
    @POST("api/createShortLink")
    suspend fun createShortLink(@Body request: CreateShortLinkRequest): CreateShortLinkResponse
}

/**
 * Request body for creating a short link.
 */
@Serializable
data class CreateShortLinkRequest(
    val teamId: String,
    val teamName: String
)

/**
 * Response containing the generated short link.
 */
@Serializable
data class CreateShortLinkResponse(
    val shortLink: String,
    val linkId: String? = null
)
