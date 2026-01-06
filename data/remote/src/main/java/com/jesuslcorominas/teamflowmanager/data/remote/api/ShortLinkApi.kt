package com.jesuslcorominas.teamflowmanager.data.remote.api

import com.jesuslcorominas.teamflowmanager.data.remote.apimodel.CreateShortLinkRequest
import com.jesuslcorominas.teamflowmanager.data.remote.apimodel.CreateShortLinkResponse
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.POST

/**
 * API interface for generating short links using Firebase Cloud Functions.
 *
 * This API replaces deprecated Firebase Dynamic Links with a custom solution
 * that uses Firebase Hosting + Cloud Functions to create short, clickable URLs.
 */
internal interface ShortLinkApi {
    /**
     * Creates a short link for team invitation.
     *
     * @param request The request containing team details
     * @return Response with the generated short link URL
     */
    @POST("api/createShortLink")
    suspend fun createShortLink(@Body request: CreateShortLinkRequest): CreateShortLinkResponse
}
