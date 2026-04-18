package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Club
import com.jesuslcorominas.teamflowmanager.domain.model.NotificationPayload
import com.jesuslcorominas.teamflowmanager.domain.model.UserNotificationPreferences
import com.jesuslcorominas.teamflowmanager.domain.usecase.MatchEventNotification
import com.jesuslcorominas.teamflowmanager.domain.usecase.NotifyPresidentMatchEventUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.ClubRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.FcmNotificationRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.NotificationPreferencesRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PresidentNotificationRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class NotifyPresidentMatchEventUseCaseTest {
    private lateinit var clubRepository: ClubRepository
    private lateinit var notificationPreferencesRepository: NotificationPreferencesRepository
    private lateinit var fcmNotificationRepository: FcmNotificationRepository
    private lateinit var presidentNotificationRepository: PresidentNotificationRepository
    private lateinit var useCase: NotifyPresidentMatchEventUseCase

    private val presidentUserId = "president-uid"
    private val clubRemoteId = "club-fs-1"
    private val teamRemoteId = "team-fs-1"
    private val matchId = "match-id-1"

    private val club = Club(
        id = 1L,
        ownerId = presidentUserId,
        name = "Test Club",
        invitationCode = "ABC123",
        remoteId = clubRemoteId,
    )

    @Before
    fun setup() {
        clubRepository = mockk(relaxed = true)
        notificationPreferencesRepository = mockk(relaxed = true)
        fcmNotificationRepository = mockk(relaxed = true)
        presidentNotificationRepository = mockk(relaxed = true)
        coEvery { clubRepository.getClubById(clubRemoteId) } returns club
        useCase = NotifyPresidentMatchEventUseCaseImpl(
            clubRepository,
            notificationPreferencesRepository,
            fcmNotificationRepository,
            presidentNotificationRepository,
        )
    }

    @Test
    fun `sends notification to president when subscribed`() =
        runTest {
            // Given
            every {
                notificationPreferencesRepository.getPreferences(presidentUserId, clubRemoteId)
            } returns flowOf(UserNotificationPreferences(userId = presidentUserId, globalMatchEvents = true, globalGoals = true))

            val event = MatchEventNotification.Start("Team A", "Team B")

            // When
            useCase(event, matchId, teamRemoteId, clubRemoteId)

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
            every {
                notificationPreferencesRepository.getPreferences(presidentUserId, clubRemoteId)
            } returns flowOf(UserNotificationPreferences(userId = presidentUserId, globalMatchEvents = false, globalGoals = true))

            val event = MatchEventNotification.Start("Team A", "Team B")

            // When
            useCase(event, matchId, teamRemoteId, clubRemoteId)

            // Then
            coVerify(exactly = 0) {
                fcmNotificationRepository.sendNotificationToUser(any(), any())
            }
        }

    @Test
    fun `does not throw when FCM fails`() =
        runTest {
            // Given
            every {
                notificationPreferencesRepository.getPreferences(presidentUserId, clubRemoteId)
            } returns flowOf(UserNotificationPreferences(userId = presidentUserId))
            coEvery {
                fcmNotificationRepository.sendNotificationToUser(any(), any())
            } throws RuntimeException("FCM error")

            val event = MatchEventNotification.Goal("Team A", "Rival", 1, 0, "15", false)

            // When — must not throw
            useCase(event, matchId, teamRemoteId, clubRemoteId)
        }

    @Test
    fun `does not send notification when club not found`() =
        runTest {
            // Given
            coEvery { clubRepository.getClubById(clubRemoteId) } returns null

            val event = MatchEventNotification.Start("Team A", "Team B")

            // When
            useCase(event, matchId, teamRemoteId, clubRemoteId)

            // Then
            coVerify(exactly = 0) {
                fcmNotificationRepository.sendNotificationToUser(any(), any())
            }
        }

    @Test
    fun `sends goal notification to president when goals preference is enabled`() =
        runTest {
            // Given
            every {
                notificationPreferencesRepository.getPreferences(presidentUserId, clubRemoteId)
            } returns flowOf(UserNotificationPreferences(userId = presidentUserId, globalGoals = true))

            val event = MatchEventNotification.Goal("Team A", "Rival", 2, 1, "22", false)

            // When
            useCase(event, matchId, teamRemoteId, clubRemoteId)

            // Then
            coVerify {
                fcmNotificationRepository.sendNotificationToUser(
                    userId = presidentUserId,
                    payload = NotificationPayload.Typed.GoalScored(
                        teamName = "Team A",
                        opponentName = "Rival",
                        teamGoals = 2,
                        opponentGoals = 1,
                        minuteOfPlay = "22",
                        isOpponentGoal = false,
                    ),
                )
            }
        }

    @Test
    fun `does not send notification when goals preference is OFF`() =
        runTest {
            // Given
            every {
                notificationPreferencesRepository.getPreferences(presidentUserId, clubRemoteId)
            } returns flowOf(UserNotificationPreferences(userId = presidentUserId, globalMatchEvents = true, globalGoals = false))

            val event = MatchEventNotification.Goal("Team A", "Rival", 1, 0, null, false)

            // When
            useCase(event, matchId, teamRemoteId, clubRemoteId)

            // Then
            coVerify(exactly = 0) {
                fcmNotificationRepository.sendNotificationToUser(any(), any())
            }
        }

    @Test
    fun `sends match end notification when match events preference is enabled`() =
        runTest {
            // Given
            every {
                notificationPreferencesRepository.getPreferences(presidentUserId, clubRemoteId)
            } returns flowOf(UserNotificationPreferences(userId = presidentUserId, globalMatchEvents = true))

            val event = MatchEventNotification.End("Team A", "Rival", 3, 2)

            // When
            useCase(event, matchId, teamRemoteId, clubRemoteId)

            // Then
            coVerify {
                fcmNotificationRepository.sendNotificationToUser(
                    userId = presidentUserId,
                    payload = NotificationPayload.Typed.MatchEnd("Team A", "Rival", 3, 2),
                )
            }
        }

    @Test
    fun `does not send notification when team-level preference disables match events`() =
        runTest {
            // Given – global ON but team override is OFF
            every {
                notificationPreferencesRepository.getPreferences(presidentUserId, clubRemoteId)
            } returns flowOf(
                UserNotificationPreferences(
                    userId = presidentUserId,
                    globalMatchEvents = true,
                    teamPreferences = mapOf(
                        teamRemoteId to com.jesuslcorominas.teamflowmanager.domain.model.TeamNotificationPreferences(
                            teamRemoteId = teamRemoteId,
                            matchEvents = false,
                        ),
                    ),
                ),
            )

            val event = MatchEventNotification.Start("Team A", "Team B")

            // When
            useCase(event, matchId, teamRemoteId, clubRemoteId)

            // Then
            coVerify(exactly = 0) {
                fcmNotificationRepository.sendNotificationToUser(any(), any())
            }
        }

    @Test
    fun `sends notification via team override when global is OFF but team override is ON`() =
        runTest {
            // Given – global OFF but team override is ON
            every {
                notificationPreferencesRepository.getPreferences(presidentUserId, clubRemoteId)
            } returns flowOf(
                UserNotificationPreferences(
                    userId = presidentUserId,
                    globalGoals = false,
                    teamPreferences = mapOf(
                        teamRemoteId to com.jesuslcorominas.teamflowmanager.domain.model.TeamNotificationPreferences(
                            teamRemoteId = teamRemoteId,
                            goals = true,
                        ),
                    ),
                ),
            )

            val event = MatchEventNotification.Goal("Team A", "Rival", 1, 0, null, true)

            // When
            useCase(event, matchId, teamRemoteId, clubRemoteId)

            // Then
            coVerify {
                fcmNotificationRepository.sendNotificationToUser(presidentUserId, any())
            }
        }

    @Test
    fun `creates Firestore notification when sending match start`() =
        runTest {
            // Given
            every {
                notificationPreferencesRepository.getPreferences(presidentUserId, clubRemoteId)
            } returns flowOf(UserNotificationPreferences(userId = presidentUserId, globalMatchEvents = true))

            val event = MatchEventNotification.Start("Team A", "Team B")

            // When
            useCase(event, matchId, teamRemoteId, clubRemoteId)

            // Then
            coVerify {
                presidentNotificationRepository.createNotification(
                    clubId = clubRemoteId,
                    notification = match { it.type == com.jesuslcorominas.teamflowmanager.domain.model.NotificationType.MATCH_START },
                )
            }
        }

    @Test
    fun `does not throw when presidentNotificationRepository fails`() =
        runTest {
            // Given
            every {
                notificationPreferencesRepository.getPreferences(presidentUserId, clubRemoteId)
            } returns flowOf(UserNotificationPreferences(userId = presidentUserId, globalMatchEvents = true))
            coEvery {
                presidentNotificationRepository.createNotification(any(), any())
            } throws RuntimeException("Firestore error")

            val event = MatchEventNotification.Start("Team A", "Team B")

            // When — must not throw
            useCase(event, matchId, teamRemoteId, clubRemoteId)
        }
}