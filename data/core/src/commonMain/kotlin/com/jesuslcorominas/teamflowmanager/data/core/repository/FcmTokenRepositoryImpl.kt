package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.FcmTokenDataSource
import com.jesuslcorominas.teamflowmanager.usecase.repository.FcmTokenRepository

class FcmTokenRepositoryImpl(
    private val fcmTokenDataSource: FcmTokenDataSource,
) : FcmTokenRepository {
    override suspend fun saveToken(userId: String, token: String, platform: String) =
        fcmTokenDataSource.saveToken(userId, token, platform)

    override suspend fun deleteToken(userId: String, token: String) =
        fcmTokenDataSource.deleteToken(userId, token)
}
