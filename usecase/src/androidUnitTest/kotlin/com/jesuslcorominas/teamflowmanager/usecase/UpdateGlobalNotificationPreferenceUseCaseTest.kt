package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.NotificationEventType
import com.jesuslcorominas.teamflowmanager.domain.model.User
import com.jesuslcorominas.teamflowmanager.domain.usecase.UpdateGlobalNotificationPreferenceUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.AuthRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.NotificationPreferencesRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class UpdateGlobalNotificationPreferenceUseCaseTest {
    private lateinit var authRepository: AuthRepository
    private lateinit var notificationPreferencesRepository: NotificationPreferencesRepository
    private lateinit var useCase: UpdateGlobalNotificationPreferenceUseCase

    @Before
    fun setup() {
        authRepository = mockk(relaxed = true)
        notificationPreferencesRepository = mockk(relaxed = true)
        useCase = UpdateGlobalNotificationPreferenceUseCaseImpl(authRepository, notificationPreferencesRepository)
    }

    @Test
    fun `invoke calls repository with correct user ID`() =
        runTest {
            // Given
            val userId = "user-123"
            val clubId = "club-fs-1"
            val user = User(id = userId, displayName = "Test User", email = "test@test.com", photoUrl = null)
            every { authRepository.getCurrentUser() } returns flowOf(user)

            // When
            useCase(clubId, NotificationEventType.MATCH_EVENTS, true)

            // Then
            coVerify {
                notificationPreferencesRepository.updateGlobalPreference(
                    userId = userId,
                    clubId = clubId,
                    type = NotificationEventType.MATCH_EVENTS,
                    enabled = true,
                )
            }
        }

    @Test
    fun `invoke no-ops when user is null`() =
        runTest {
            // Given
            every { authRepository.getCurrentUser() } returns flowOf(null)

            // When — must not throw
            useCase("club-1", NotificationEventType.GOALS, false)

            // Then — repository is never called
            coVerify(exactly = 0) {
                notificationPreferencesRepository.updateGlobalPreference(any(), any(), any(), any())
            }
        }
}