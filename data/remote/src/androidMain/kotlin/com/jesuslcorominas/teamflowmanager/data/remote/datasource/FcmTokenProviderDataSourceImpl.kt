package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import com.google.firebase.messaging.FirebaseMessaging
import com.jesuslcorominas.teamflowmanager.data.core.datasource.FcmTokenProviderDataSource
import kotlinx.coroutines.tasks.await

class FcmTokenProviderDataSourceImpl : FcmTokenProviderDataSource {
    override suspend fun getToken(): String =
        runCatching { FirebaseMessaging.getInstance().token.await() ?: "" }.getOrDefault("")
}
