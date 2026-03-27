package com.jesuslcorominas.teamflowmanager.usecase.repository

import com.jesuslcorominas.teamflowmanager.domain.model.FcmTokenEntry

interface FcmTokenRepository {
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
}
