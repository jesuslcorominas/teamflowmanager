package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.DeletePresidentNotificationUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PresidentNotificationRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DeletePresidentNotificationUseCaseTest {
    private lateinit var repository: PresidentNotificationRepository
    private lateinit var useCase: DeletePresidentNotificationUseCase

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
        useCase = DeletePresidentNotificationUseCaseImpl(repository)
    }

    @Test
    fun `invoke calls repository deleteNotification with correct args`() =
        runTest {
            // Given
            val clubId = "club-fs-1"
            val notificationId = "notif-1"

            // When
            useCase(clubId, notificationId)

            // Then
            coVerify { repository.deleteNotification(clubId, notificationId) }
        }

    @Test
    fun `invoke calls repository deleteNotification with different args`() =
        runTest {
            // Given
            val clubId = "club-fs-2"
            val notificationId = "notif-to-delete"

            // When
            useCase(clubId, notificationId)

            // Then
            coVerify { repository.deleteNotification(clubId, notificationId) }
        }
}