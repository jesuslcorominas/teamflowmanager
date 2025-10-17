package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Goal
import com.jesuslcorominas.teamflowmanager.usecase.repository.GoalRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetGoalsForMatchUseCaseTest {
    private lateinit var goalRepository: GoalRepository
    private lateinit var getGoalsForMatchUseCase: GetGoalsForMatchUseCase

    @Before
    fun setup() {
        goalRepository = mockk(relaxed = true)
        getGoalsForMatchUseCase = GetGoalsForMatchUseCaseImpl(goalRepository)
    }

    @Test
    fun `invoke should return goals from repository`() =
        runTest {
            // Given
            val matchId = 1L
            val goals =
                listOf(
                    Goal(
                        id = 1L,
                        matchId = matchId,
                        scorerId = 2L,
                        goalTimeMillis = 1000L,
                        matchElapsedTimeMillis = 500000L,
                    ),
                    Goal(
                        id = 2L,
                        matchId = matchId,
                        scorerId = 3L,
                        goalTimeMillis = 2000L,
                        matchElapsedTimeMillis = 900000L,
                    ),
                )
            every { goalRepository.getMatchGoals(matchId) } returns flowOf(goals)

            // When
            val result = getGoalsForMatchUseCase(matchId).first()

            // Then
            verify { goalRepository.getMatchGoals(matchId) }
            assertEquals(2, result.size)
            assertEquals(goals, result)
        }

    @Test
    fun `invoke should return empty list when no goals exist`() =
        runTest {
            // Given
            val matchId = 1L
            every { goalRepository.getMatchGoals(matchId) } returns flowOf(emptyList())

            // When
            val result = getGoalsForMatchUseCase(matchId).first()

            // Then
            verify { goalRepository.getMatchGoals(matchId) }
            assertEquals(0, result.size)
        }
}
