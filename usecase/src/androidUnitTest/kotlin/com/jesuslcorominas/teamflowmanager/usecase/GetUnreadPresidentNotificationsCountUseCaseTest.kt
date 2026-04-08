package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.GetUnreadPresidentNotificationsCountUseCase
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

class GetUnreadPresidentNotificationsCountUseCaseTest {
    private lateinit var repository: PresidentNotificationRepository
    private lateinit var useCase: GetUnreadPresidentNotificationsCountUseCase

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
        useCase = GetUnreadPresidentNotificationsCountUseCaseImpl(repository)
    }

    @Test
    fun `invoke delegates to repository getUnreadCount`() =
        runTest {
            // Given
            val clubId = "club-fs-1"
            every { repository.getUnreadCount(clubId) } returns flowOf(0)

            // When
            useCase(clubId).first()

            // Then
            verify { repository.getUnreadCount(clubId) }
        }

    @Test
    fun `invoke returns count from repository`() =
        runTest {
            // Given
            val clubId = "club-fs-1"
            every { repository.getUnreadCount(clubId) } returns flowOf(5)

            // When
            val result = useCase(clubId).first()

            // Then
            assertEquals(5, result)
        }

    @Test
    fun `invoke returns zero when no unread notifications`() =
        runTest {
            // Given
            val clubId = "club-fs-1"
            every { repository.getUnreadCount(clubId) } returns flowOf(0)

            // When
            val result = useCase(clubId).first()

            // Then
            assertEquals(0, result)
        }
}