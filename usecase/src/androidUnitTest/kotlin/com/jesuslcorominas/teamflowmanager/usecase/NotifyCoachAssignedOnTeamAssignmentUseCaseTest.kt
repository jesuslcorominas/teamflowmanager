package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.NotifyCoachAssignedOnTeamAssignmentUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.CoachAssignmentNotificationRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class NotifyCoachAssignedOnTeamAssignmentUseCaseTest {
    private lateinit var coachAssignmentNotificationRepository: CoachAssignmentNotificationRepository
    private lateinit var useCase: NotifyCoachAssignedOnTeamAssignmentUseCase

    @Before
    fun setup() {
        coachAssignmentNotificationRepository = mockk(relaxed = true)
        useCase = NotifyCoachAssignedOnTeamAssignmentUseCaseImpl(coachAssignmentNotificationRepository)
    }

    @Test
    fun `givenDifferentAssignorAndCoach_whenInvoke_thenNotificationIsSent`() = runTest {
        // Given
        val coachUserId = "coach1"
        val assignedByUserId = "president1"
        val teamName = "Team Alpha"

        // When
        useCase.invoke(
            coachUserId = coachUserId,
            assignedByUserId = assignedByUserId,
            teamName = teamName,
        )

        // Then — notification must be triggered
        coVerify(exactly = 1) {
            coachAssignmentNotificationRepository.notifyCoachAssigned(
                coachUserId = coachUserId,
                teamName = teamName,
            )
        }
    }

    @Test
    fun `givenSelfAssignment_whenInvoke_thenNotificationIsNotSent`() = runTest {
        // Given — president assigns themselves as coach
        val userId = "president1"
        val teamName = "Team Alpha"

        // When
        useCase.invoke(
            coachUserId = userId,
            assignedByUserId = userId,
            teamName = teamName,
        )

        // Then — no notification must be sent
        coVerify(exactly = 0) {
            coachAssignmentNotificationRepository.notifyCoachAssigned(any(), any())
        }
    }
}