package com.jesuslcorominas.teamflowmanager.data.core.datasource

interface FcmTokenDataSource {
    suspend fun saveToken(userId: String, token: String, platform: String)
    suspend fun deleteToken(userId: String, token: String)
}
