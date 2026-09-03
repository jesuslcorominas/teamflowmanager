package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.NotificationEventType
import com.jesuslcorominas.teamflowmanager.domain.model.User
import com.jesuslcorominas.teamflowmanager.domain.usecase.UpdateTeamNotificationPreferenceUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.AuthRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.NotificationPreferencesRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class UpdateTeamNotificationPreferenceUseCaseTest {
    private lateinit var authRepository: AuthRepository
    private lateinit var notificationPreferencesRepository: NotificationPreferencesRepository
    private lateinit var useCase: UpdateTeamNotificationPreferenceUseCase

    private val userId = "user-uid"
    private val clubId = "club-fs-1"
    private val teamRemoteId = "team-fs-1"

    @Before
    fun setup() {
        authRepository = mockk()
        notificationPreferencesRepository = mockk(relaxed = true)
        useCase = UpdateTeamNotificationPreferenceUseCaseImpl(authRepository, notificationPreferencesRepository)
        every { authRepository.getCurrentUser() } returns flowOf(
            User(id = userId, email = "test@test.com", displayName = "Test", photoUrl = null),
        )
    }

    @Test
    fun `delegates to repository with userId`() =
        runTest {
            useCase(clubId, teamRemoteId, NotificationEventType.GOALS, true)

            coVerify {
                notificationPreferencesRepository.updateTeamPreference(userId, clubId, teamRemoteId, NotificationEventType.GOALS, true)
            }
        }

    @Test
    fun `does nothing when user is not logged in`() =
        runTest {
            every { authRepository.getCurrentUser() } returns flowOf(null)

            useCase(clubId, teamRemoteId, NotificationEventType.MATCH_EVENTS, false)

            coVerify(exactly = 0) {
                notificationPreferencesRepository.updateTeamPreference(any(), any(), any(), any(), any())
            }
        }

    @Test
    fun `delegates with correct type for MATCH_EVENTS`() =
        runTest {
            useCase(clubId, teamRemoteId, NotificationEventType.MATCH_EVENTS, false)

            coVerify {
                notificationPreferencesRepository.updateTeamPreference(userId, clubId, teamRemoteId, NotificationEventType.MATCH_EVENTS, false)
            }
        }
}