package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.FcmSendNotificationDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.FcmTokenDataSource
import com.jesuslcorominas.teamflowmanager.usecase.repository.CoachAssignmentNotificationRepository

private const val NOTIFICATION_TITLE = "Has sido asignado como entrenador"

class CoachAssignmentNotificationRepositoryImpl(
    private val fcmTokenDataSource: FcmTokenDataSource,
    private val fcmSendNotificationDataSource: FcmSendNotificationDataSource,
) : CoachAssignmentNotificationRepository {
    override suspend fun notifyCoachAssigned(
        coachUserId: String,
        teamName: String,
    ) {
        val tokens = fcmTokenDataSource.getTokensByUserId(coachUserId)
        val body = "Has sido asignado como entrenador del equipo $teamName"
        tokens.forEach { token ->
            fcmSendNotificationDataSource.sendNotification(
                token = token,
                title = NOTIFICATION_TITLE,
                body = body,
            )
        }
    }
}
