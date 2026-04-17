package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.ClubMember
import com.jesuslcorominas.teamflowmanager.domain.model.ClubRole
import com.jesuslcorominas.teamflowmanager.domain.model.NotificationPayload
import com.jesuslcorominas.teamflowmanager.domain.model.UserNotificationPreferences
import com.jesuslcorominas.teamflowmanager.domain.usecase.MatchEventNotification
import com.jesuslcorominas.teamflowmanager.domain.usecase.NotifyPresidentMatchEventUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.ClubMemberRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.FcmNotificationRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.NotificationPreferencesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class NotifyPresidentMatchEventUseCaseTest {
    private lateinit var clubMemberRepository: ClubMemberRepository
    private lateinit var notificationPreferencesRepository: NotificationPreferencesRepository
    private lateinit var fcmNotificationRepository: FcmNotificationRepository
    private lateinit var useCase: NotifyPresidentMatchEventUseCase

    private val presidentUserId = "president-uid"
    private val clubRemoteId = "club-fs-1"
    private val teamRemoteId = "team-fs-1"

    private val presidentMember = ClubMember(
        id = 1L,
        userId = presidentUserId,
        name = "President User",
        email = "president@test.com",
        clubId = 1L,
        roles = listOf(ClubRole.PRESIDENT.roleName),
        remoteId = "member-1",
        clubRemoteId = clubRemoteId,
    )

    @Before
    fun setup() {
        clubMemberRepository = mockk(relaxed = true)
        notificationPreferencesRepository = mockk(relaxed = true)
        fcmNotificationRepository = mockk(relaxed = true)
        useCase = NotifyPresidentMatchEventUseCaseImpl(
            clubMemberRepository,
            notificationPreferencesRepository,
            fcmNotificationRepository,
        )
    }

    @Test
    fun `sends notification to president when subscribed`() =
        runTest {
            // Given
            every { clubMemberRepository.getClubMembers(clubRemoteId) } returns flowOf(listOf(presidentMember))
            every {
                notificationPreferencesRepository.getPreferences(presidentUserId, clubRemoteId)
            } returns flowOf(UserNotificationPreferences(userId = presidentUserId, globalMatchEvents = true, globalGoals = true))

            val event = MatchEventNotification.Start("Team A", "Team B")

            // When
            useCase(event, teamRemoteId, clubRemoteId)

            // Then
            coVerify {
                fcmNotificationRepository.sendNotificationToUser(
                    userId = presidentUserId,
                    payload = NotificationPayload.Typed.MatchStart("Team A", "Team B"),
                )
            }
        }

    @Test
    fun `does not send notification when match events preference is OFF`() =
        runTest {
            // Given
            every { clubMemberRepository.getClubMembers(clubRemoteId) } returns flowOf(listOf(presidentMember))
            every {
                notificationPreferencesRepository.getPreferences(presidentUserId, clubRemoteId)
            } returns flowOf(UserNotificationPreferences(userId = presidentUserId, globalMatchEvents = false, globalGoals = true))

            val event = MatchEventNotification.Start("Team A", "Team B")

            // When
            useCase(event, teamRemoteId, clubRemoteId)

            // Then
            coVerify(exactly = 0) {
                fcmNotificationRepository.sendNotificationToUser(any(), any())
            }
        }

    @Test
    fun `does not throw when FCM fails`() =
        runTest {
            // Given
            every { clubMemberRepository.getClubMembers(clubRemoteId) } returns flowOf(listOf(presidentMember))
            every {
                notificationPreferencesRepository.getPreferences(presidentUserId, clubRemoteId)
            } returns flowOf(UserNotificationPreferences(userId = presidentUserId))
            coEvery {
                fcmNotificationRepository.sendNotificationToUser(any(), any())
            } throws RuntimeException("FCM error")

            val event = MatchEventNotification.Goal("Team A", 1, 0, 15)

            // When — must not throw
            useCase(event, teamRemoteId, clubRemoteId)
        }
}