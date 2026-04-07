package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.NotificationPayload
import com.jesuslcorominas.teamflowmanager.domain.model.NotificationType
import com.jesuslcorominas.teamflowmanager.domain.usecase.NotifyCoachAssignedOnTeamAssignmentUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.FcmNotificationRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class NotifyCoachAssignedOnTeamAssignmentUseCaseTest {
    private lateinit var fcmNotificationRepository: FcmNotificationRepository
    private lateinit var useCase: NotifyCoachAssignedOnTeamAssignmentUseCase

    @Before
    fun setup() {
        fcmNotificationRepository = mockk(relaxed = true)
        useCase = NotifyCoachAssignedOnTeamAssignmentUseCaseImpl(fcmNotificationRepository)
    }

    @Test
    fun `givenDifferentAssignorAndCoach_whenInvoke_thenTypedPayloadIsSent`() = runTest {
        useCase.invoke(
            coachUserId = "coach1",
            assignedByUserId = "president1",
            teamName = "Team Alpha",
        )

        coVerify(exactly = 1) {
            fcmNotificationRepository.sendNotificationToUser(
                userId = "coach1",
                payload = NotificationPayload.Typed.AssignedAsCoach(teamName = "Team Alpha"),
            )
        }
    }

    @Test
    fun `givenSelfAssignment_whenInvoke_thenNotificationIsNotSent`() = runTest {
        useCase.invoke(
            coachUserId = "president1",
            assignedByUserId = "president1",
            teamName = "Team Alpha",
        )

        coVerify(exactly = 0) { fcmNotificationRepository.sendNotificationToUser(any(), any()) }
    }

    @Test
    fun `givenDifferentAssignorAndCoach_whenInvoke_thenPayloadTypeIsAssignedAsCoach`() = runTest {
        useCase.invoke(
            coachUserId = "coach1",
            assignedByUserId = "president1",
            teamName = "Atlético Junior",
        )

        coVerify {
            fcmNotificationRepository.sendNotificationToUser(
                userId = "coach1",
                payload = match { it is NotificationPayload.Typed && it.type == NotificationType.ASSIGNED_AS_COACH },
            )
        }
    }

    @Test
    fun `givenDifferentAssignorAndCoach_whenInvoke_thenPayloadParamsContainTeamName`() = runTest {
        useCase.invoke(
            coachUserId = "coach1",
            assignedByUserId = "president1",
            teamName = "Atlético Junior",
        )

        coVerify {
            fcmNotificationRepository.sendNotificationToUser(
                userId = "coach1",
                payload = match { it is NotificationPayload.Typed && it.params["teamName"] == "Atlético Junior" },
            )
        }
    }
}
