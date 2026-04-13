package com.jesuslcorominas.teamflowmanager.data.remote.api

import com.jesuslcorominas.teamflowmanager.data.remote.api.model.SendNotificationRequest
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.Headers
import de.jensklingenberg.ktorfit.http.POST

/**
 * API interface for sending FCM push notifications via Firebase Cloud Functions.
 */
internal interface FcmNotificationApi {
    /**
     * Sends a push notification to a specific FCM token.
     *
     * @param request The request containing the token, title, and body
     */
    @POST("sendNotification")
    @Headers("Content-Type: application/json")
    suspend fun sendNotification(
        @Body request: SendNotificationRequest,
    )
}
