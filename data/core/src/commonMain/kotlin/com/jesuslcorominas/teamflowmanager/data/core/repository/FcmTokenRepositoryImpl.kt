package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.FcmTokenDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.FcmTokenEntry
import com.jesuslcorominas.teamflowmanager.usecase.repository.FcmTokenRepository

class FcmTokenRepositoryImpl(
    private val fcmTokenDataSource: FcmTokenDataSource,
) : FcmTokenRepository {
    override suspend fun saveToken(userId: String, token: String, platform: String, topic: String?) =
        fcmTokenDataSource.saveToken(userId, token, platform, topic)

    override suspend fun deleteToken(userId: String, token: String) =
        fcmTokenDataSource.deleteToken(userId, token)

    override suspend fun getTokenEntry(userId: String, token: String): FcmTokenEntry? =
        fcmTokenDataSource.getTokenEntry(userId, token)

    override suspend fun findTokensForOtherUsers(token: String, currentUserId: String): List<FcmTokenEntry> =
        fcmTokenDataSource.findTokensForOtherUsers(token, currentUserId)
}
