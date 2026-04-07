package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.FcmDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.FcmTokenEntry
import com.jesuslcorominas.teamflowmanager.usecase.repository.FcmTokenRepository

class FcmTokenRepositoryImpl(
    private val fcmDataSource: FcmDataSource,
) : FcmTokenRepository {
    override suspend fun saveToken(
        userId: String,
        token: String,
        platform: String,
        topic: String?,
    ) = fcmDataSource.saveToken(userId, token, platform, topic)

    override suspend fun deleteToken(
        userId: String,
        token: String,
    ) = fcmDataSource.deleteToken(userId, token)

    override suspend fun getTokenEntry(
        userId: String,
        token: String,
    ): FcmTokenEntry? = fcmDataSource.getTokenEntry(userId, token)

    override suspend fun findTokensForOtherUsers(
        token: String,
        currentUserId: String,
    ): List<FcmTokenEntry> = fcmDataSource.findTokensForOtherUsers(token, currentUserId)
}
