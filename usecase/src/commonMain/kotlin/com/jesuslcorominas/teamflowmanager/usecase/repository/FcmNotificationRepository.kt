package com.jesuslcorominas.teamflowmanager.usecase.repository

interface FcmNotificationRepository {
    suspend fun sendNotificationToUser(userId: String, title: String, body: String)
}