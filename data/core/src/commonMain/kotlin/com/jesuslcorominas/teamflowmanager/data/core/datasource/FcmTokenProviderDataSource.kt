package com.jesuslcorominas.teamflowmanager.data.core.datasource

interface FcmTokenProviderDataSource {
    suspend fun getToken(): String
}
