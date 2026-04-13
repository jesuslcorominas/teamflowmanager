package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.MarkPresidentNotificationAsReadUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PresidentNotificationRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class MarkPresidentNotificationAsReadUseCaseTest {
    private lateinit var repository: PresidentNotificationRepository
    private lateinit var useCase: MarkPresidentNotificationAsReadUseCase

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
        useCase = MarkPresidentNotificationAsReadUseCaseImpl(repository)
    }

    @Test
    fun `invoke calls repository markAsRead with correct args`() =
        runTest {
            // Given
            val clubId = "club-fs-1"
            val notificationId = "notif-1"

            // When
            useCase(clubId, notificationId)

            // Then
            coVerify { repository.markAsRead(clubId, notificationId) }
        }

    @Test
    fun `invoke calls repository markAsRead with different args`() =
        runTest {
            // Given
            val clubId = "club-fs-2"
            val notificationId = "notif-abc"

            // When
            useCase(clubId, notificationId)

            // Then
            coVerify { repository.markAsRead(clubId, notificationId) }
        }
}