package com.jesuslcorominas.teamflowmanager.data.core.datasource

import com.jesuslcorominas.teamflowmanager.domain.model.FcmTokenEntry

interface FcmTokenDataSource {
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
