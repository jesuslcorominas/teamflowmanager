package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.FcmTokenProviderDataSource
import com.jesuslcorominas.teamflowmanager.usecase.repository.FcmTokenProviderRepository

class FcmTokenProviderRepositoryImpl(
    private val fcmTokenProviderDataSource: FcmTokenProviderDataSource,
) : FcmTokenProviderRepository {
    override suspend fun getToken(): String = fcmTokenProviderDataSource.getToken()
}
