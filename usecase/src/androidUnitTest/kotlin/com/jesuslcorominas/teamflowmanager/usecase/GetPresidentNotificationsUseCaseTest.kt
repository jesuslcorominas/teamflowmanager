package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.NotificationType
import com.jesuslcorominas.teamflowmanager.domain.model.PresidentNotification
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetPresidentNotificationsUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PresidentNotificationRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetPresidentNotificationsUseCaseTest {
    private lateinit var repository: PresidentNotificationRepository
    private lateinit var useCase: GetPresidentNotificationsUseCase

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
        useCase = GetPresidentNotificationsUseCaseImpl(repository)
    }

    @Test
    fun `invoke delegates to repository getNotifications`() =
        runTest {
            // Given
            val clubId = "club-fs-1"
            every { repository.getNotifications(clubId) } returns flowOf(emptyList())

            // When
            useCase(clubId).first()

            // Then
            verify { repository.getNotifications(clubId) }
        }

    @Test
    fun `invoke returns flow with notifications from repository`() =
        runTest {
            // Given
            val clubId = "club-fs-1"
            val notifications =
                listOf(
                    PresidentNotification(
                        id = "notif1",
                        type = NotificationType.USER_WAITING_FOR_ASSIGNMENT,
                        title = "New member",
                        body = "User is waiting",
                        userData = emptyMap(),
                        createdAt = 1000L,
                        read = false,
                    ),
                    PresidentNotification(
                        id = "notif2",
                        type = NotificationType.ASSIGNED_AS_COACH,
                        title = "Assigned as coach",
                        body = "You were assigned",
                        userData = emptyMap(),
                        createdAt = 2000L,
                        read = true,
                    ),
                )
            every { repository.getNotifications(clubId) } returns flowOf(notifications)

            // When
            val result = useCase(clubId).first()

            // Then
            assertEquals(notifications, result)
        }

    @Test
    fun `invoke returns empty list when repository emits empty list`() =
        runTest {
            // Given
            val clubId = "club-fs-1"
            every { repository.getNotifications(clubId) } returns flowOf(emptyList())

            // When
            val result = useCase(clubId).first()

            // Then
            assertEquals(emptyList<PresidentNotification>(), result)
        }
}