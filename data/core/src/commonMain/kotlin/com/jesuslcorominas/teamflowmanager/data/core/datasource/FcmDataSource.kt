package com.jesuslcorominas.teamflowmanager.data.core.datasource

import com.jesuslcorominas.teamflowmanager.domain.model.FcmTokenEntry

interface FcmDataSource {
    suspend fun saveToken(
        userId: String,
        token: String,
        platform: String,
        topic: String?,
    )

    suspend fun deleteToken(
        userId: String,
        token: String,
    )

    suspend fun getTokenEntry(
        userId: String,
        token: String,
    ): FcmTokenEntry?

    suspend fun findTokensForOtherUsers(
        token: String,
        currentUserId: String,
    ): List<FcmTokenEntry>

    /**
     * Retrieves all FCM tokens registered for the given user.
     *
     * @param userId The Firebase user ID
     * @return List of token strings registered for the user
     */
    suspend fun getTokensByUserId(userId: String): List<String>

    suspend fun sendNotification(
        token: String,
        title: String,
        body: String,
    )
}
