package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.NotificationPayload
import com.jesuslcorominas.teamflowmanager.domain.model.NotificationType
import com.jesuslcorominas.teamflowmanager.domain.model.PresidentNotification
import com.jesuslcorominas.teamflowmanager.domain.usecase.NotifyPresidentOnMemberWaitingUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.FcmNotificationRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PresidentNotificationRepository

internal class NotifyPresidentOnMemberWaitingUseCaseImpl(
    private val presidentNotificationRepository: PresidentNotificationRepository,
    private val fcmNotificationRepository: FcmNotificationRepository,
) : NotifyPresidentOnMemberWaitingUseCase {
    override suspend fun invoke(
        clubId: String,
        presidentUserId: String,
        userName: String,
        userEmail: String,
    ) {
        val notification =
            PresidentNotification(
                id = "",
                type = NotificationType.USER_WAITING_FOR_ASSIGNMENT,
                title = userName,
                body = userEmail,
                userData =
                    mapOf(
                        KEY_USER_NAME to userName,
                        KEY_USER_EMAIL to userEmail,
                    ),
                createdAt = System.currentTimeMillis(),
                read = false,
            )

        presidentNotificationRepository.createNotification(
            clubId = clubId,
            notification = notification,
        )

        try {
            fcmNotificationRepository.sendNotificationToUser(
                userId = presidentUserId,
                payload =
                    NotificationPayload.Typed.UserWaitingForAssignment(
                        userName = userName,
                        userEmail = userEmail,
                    ),
            )
        } catch (e: Exception) {
            // FCM failure must not prevent Firestore notification from being saved
        }
    }

    companion object {
        const val KEY_USER_NAME = "userName"
        const val KEY_USER_EMAIL = "userEmail"
    }
}
