package com.jesuslcorominas.teamflowmanager.usecase

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
    fun `givenDifferentAssignorAndCoach_whenInvoke_thenNotificationIsSent`() = runTest {
        useCase.invoke(
            coachUserId = "coach1",
            assignedByUserId = "president1",
            teamName = "Team Alpha",
        )

        coVerify(exactly = 1) {
            fcmNotificationRepository.sendNotificationToUser(
                userId = "coach1",
                title = any(),
                body = any(),
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

        coVerify(exactly = 0) { fcmNotificationRepository.sendNotificationToUser(any(), any(), any()) }
    }

    @Test
    fun `givenDifferentAssignorAndCoach_whenInvoke_thenNotificationBodyContainsTeamName`() = runTest {
        useCase.invoke(
            coachUserId = "coach1",
            assignedByUserId = "president1",
            teamName = "Atlético Junior",
        )

        coVerify {
            fcmNotificationRepository.sendNotificationToUser(
                userId = any(),
                title = any(),
                body = match { it.contains("Atlético Junior") },
            )
        }
    }
}