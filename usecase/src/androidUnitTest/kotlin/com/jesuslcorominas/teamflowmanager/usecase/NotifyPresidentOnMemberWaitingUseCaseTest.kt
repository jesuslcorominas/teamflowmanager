package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.NotificationPayload
import com.jesuslcorominas.teamflowmanager.domain.model.NotificationType
import com.jesuslcorominas.teamflowmanager.domain.model.PresidentNotification
import com.jesuslcorominas.teamflowmanager.domain.usecase.NotifyPresidentOnMemberWaitingUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.FcmNotificationRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PresidentNotificationRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

class NotifyPresidentOnMemberWaitingUseCaseTest {
    private lateinit var presidentNotificationRepository: PresidentNotificationRepository
    private lateinit var fcmNotificationRepository: FcmNotificationRepository
    private lateinit var useCase: NotifyPresidentOnMemberWaitingUseCase

    @Before
    fun setup() {
        presidentNotificationRepository = mockk(relaxed = true)
        fcmNotificationRepository = mockk(relaxed = true)
        useCase = NotifyPresidentOnMemberWaitingUseCaseImpl(presidentNotificationRepository, fcmNotificationRepository)
    }

    @Test
    fun `invoke creates a Firestore notification with correct data`() =
        runTest {
            // Given
            val clubId = "club-fs-1"
            val presidentUserId = "president-uid"
            val userName = "John Doe"
            val userEmail = "john@example.com"
            val notificationSlot = slot<PresidentNotification>()

            // When
            useCase(clubId, presidentUserId, userName, userEmail)

            // Then
            coVerify {
                presidentNotificationRepository.createNotification(
                    clubId = clubId,
                    notification = capture(notificationSlot),
                )
            }
            val notification = notificationSlot.captured
            assertEquals(NotificationType.USER_WAITING_FOR_ASSIGNMENT, notification.type)
            assertEquals(userName, notification.title)
            assertEquals(userEmail, notification.body)
            assertEquals(userName, notification.userData[NotifyPresidentOnMemberWaitingUseCaseImpl.KEY_USER_NAME])
            assertEquals(userEmail, notification.userData[NotifyPresidentOnMemberWaitingUseCaseImpl.KEY_USER_EMAIL])
            assertFalse(notification.read)
        }

    @Test
    fun `invoke sends FCM push notification to president`() =
        runTest {
            // Given
            val clubId = "club-fs-1"
            val presidentUserId = "president-uid"
            val userName = "Jane Smith"
            val userEmail = "jane@example.com"

            // When
            useCase(clubId, presidentUserId, userName, userEmail)

            // Then
            coVerify {
                fcmNotificationRepository.sendNotificationToUser(
                    userId = presidentUserId,
                    payload = NotificationPayload.Typed.UserWaitingForAssignment(userName = userName, userEmail = userEmail),
                )
            }
        }

    @Test
    fun `invoke creates Firestore notification even when FCM throws`() =
        runTest {
            // Given
            val clubId = "club-fs-1"
            val presidentUserId = "president-uid"
            val userName = "Bob"
            val userEmail = "bob@example.com"
            coEvery { fcmNotificationRepository.sendNotificationToUser(any(), any()) } throws RuntimeException("FCM down")

            // When — must not throw
            useCase(clubId, presidentUserId, userName, userEmail)

            // Then — Firestore notification is still created
            coVerify { presidentNotificationRepository.createNotification(clubId = clubId, notification = any()) }
        }
}