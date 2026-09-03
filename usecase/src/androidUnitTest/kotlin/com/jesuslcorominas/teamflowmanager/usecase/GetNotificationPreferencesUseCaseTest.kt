package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.User
import com.jesuslcorominas.teamflowmanager.domain.model.UserNotificationPreferences
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetNotificationPreferencesUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.AuthRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.NotificationPreferencesRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetNotificationPreferencesUseCaseTest {
    private lateinit var authRepository: AuthRepository
    private lateinit var notificationPreferencesRepository: NotificationPreferencesRepository
    private lateinit var useCase: GetNotificationPreferencesUseCase

    @Before
    fun setup() {
        authRepository = mockk(relaxed = true)
        notificationPreferencesRepository = mockk(relaxed = true)
        useCase = GetNotificationPreferencesUseCaseImpl(authRepository, notificationPreferencesRepository)
    }

    @Test
    fun `invoke returns preferences for current user`() =
        runTest {
            // Given
            val userId = "user-123"
            val clubId = "club-fs-1"
            val user = User(id = userId, displayName = "Test User", email = "test@test.com", photoUrl = null)
            val preferences = UserNotificationPreferences(userId = userId, globalMatchEvents = true, globalGoals = false)

            every { authRepository.getCurrentUser() } returns flowOf(user)
            every { notificationPreferencesRepository.getPreferences(userId, clubId) } returns flowOf(preferences)

            // When
            val result = useCase(clubId).first()

            // Then
            assertEquals(preferences, result)
            verify { notificationPreferencesRepository.getPreferences(userId, clubId) }
        }

    @Test
    fun `invoke returns nothing when no user logged in`() =
        runTest {
            // Given
            val clubId = "club-fs-1"
            every { authRepository.getCurrentUser() } returns flowOf(null)

            // When
            val result = useCase(clubId).toList()

            // Then
            assertTrue(result.isEmpty())
        }
}