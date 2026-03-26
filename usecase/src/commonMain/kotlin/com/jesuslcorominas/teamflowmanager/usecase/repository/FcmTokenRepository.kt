package com.jesuslcorominas.teamflowmanager.usecase.repository

interface FcmTokenRepository {
    suspend fun saveToken(userId: String, token: String, platform: String)
    suspend fun deleteToken(userId: String, token: String)
}
