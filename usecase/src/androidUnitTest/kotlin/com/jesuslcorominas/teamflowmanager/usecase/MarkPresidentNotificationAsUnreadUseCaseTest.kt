package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.MarkPresidentNotificationAsUnreadUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PresidentNotificationRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class MarkPresidentNotificationAsUnreadUseCaseTest {
    private lateinit var repository: PresidentNotificationRepository
    private lateinit var useCase: MarkPresidentNotificationAsUnreadUseCase

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
        useCase = MarkPresidentNotificationAsUnreadUseCaseImpl(repository)
    }

    @Test
    fun `invoke calls repository markAsUnread with correct args`() =
        runTest {
            // Given
            val clubId = "club-fs-1"
            val notificationId = "notif-1"

            // When
            useCase(clubId, notificationId)

            // Then
            coVerify { repository.markAsUnread(clubId, notificationId) }
        }

    @Test
    fun `invoke calls repository markAsUnread with different args`() =
        runTest {
            // Given
            val clubId = "club-fs-2"
            val notificationId = "notif-xyz"

            // When
            useCase(clubId, notificationId)

            // Then
            coVerify { repository.markAsUnread(clubId, notificationId) }
        }
}