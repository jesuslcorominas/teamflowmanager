package com.jesuslcorominas.teamflowmanager.usecase.repository

interface FcmTokenProviderRepository {
    suspend fun getToken(): String
}
