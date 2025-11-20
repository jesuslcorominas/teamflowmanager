package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.domain.model.PeriodType
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class GetActiveMatchUseCaseTest {
    private lateinit var matchRepository: MatchRepository
    private lateinit var getActiveMatchUseCase: GetActiveMatchUseCase

    @Before
    fun setup() {
        matchRepository = mockk(relaxed = true)
        getActiveMatchUseCase = GetActiveMatchUseCaseImpl(matchRepository)
    }

    @Test
    fun `invoke should return match with IN_PROGRESS status`() =
        runTest {
            // Given
            val activeMatch =
                Match(
                    id = 1L,
                    teamId = 1L,
                    teamName = "Team A",
                    opponent = "Team B",
                    location = "Stadium",
                    periodType = PeriodType.HALF_TIME,
                    captainId = 1L,
                    status = MatchStatus.IN_PROGRESS,
                )
            val scheduledMatch =
                Match(
                    id = 2L,
                    teamId = 1L,
                    teamName = "Team A",
                    opponent = "Team C",
                    location = "Stadium",
                    periodType = PeriodType.HALF_TIME,
                    captainId = 1L,
                    status = MatchStatus.SCHEDULED,
                )
            every { matchRepository.getAllMatches() } returns flowOf(listOf(activeMatch, scheduledMatch))

            // When
            val result = getActiveMatchUseCase.invoke().first()

            // Then
            assertEquals(activeMatch, result)
            verify { matchRepository.getAllMatches() }
        }

    @Test
    fun `invoke should return match with PAUSED status`() =
        runTest {
            // Given
            val pausedMatch =
                Match(
                    id = 1L,
                    teamId = 1L,
                    teamName = "Team A",
                    opponent = "Team B",
                    location = "Stadium",
                    periodType = PeriodType.HALF_TIME,
                    captainId = 1L,
                    status = MatchStatus.PAUSED,
                )
            every { matchRepository.getAllMatches() } returns flowOf(listOf(pausedMatch))

            // When
            val result = getActiveMatchUseCase.invoke().first()

            // Then
            assertEquals(pausedMatch, result)
            verify { matchRepository.getAllMatches() }
        }

    @Test
    fun `invoke should return match with TIMEOUT status`() =
        runTest {
            // Given
            val timeoutMatch =
                Match(
                    id = 1L,
                    teamId = 1L,
                    teamName = "Team A",
                    opponent = "Team B",
                    location = "Stadium",
                    periodType = PeriodType.HALF_TIME,
                    captainId = 1L,
                    status = MatchStatus.TIMEOUT,
                )
            every { matchRepository.getAllMatches() } returns flowOf(listOf(timeoutMatch))

            // When
            val result = getActiveMatchUseCase.invoke().first()

            // Then
            assertEquals(timeoutMatch, result)
            verify { matchRepository.getAllMatches() }
        }

    @Test
    fun `invoke should return null when no active matches exist`() =
        runTest {
            // Given
            val finishedMatch =
                Match(
                    id = 1L,
                    teamId = 1L,
                    teamName = "Team A",
                    opponent = "Team B",
                    location = "Stadium",
                    periodType = PeriodType.HALF_TIME,
                    captainId = 1L,
                    status = MatchStatus.FINISHED,
                )
            val scheduledMatch =
                Match(
                    id = 2L,
                    teamId = 1L,
                    teamName = "Team A",
                    opponent = "Team C",
                    location = "Stadium",
                    periodType = PeriodType.HALF_TIME,
                    captainId = 1L,
                    status = MatchStatus.SCHEDULED,
                )
            every { matchRepository.getAllMatches() } returns flowOf(listOf(finishedMatch, scheduledMatch))

            // When
            val result = getActiveMatchUseCase.invoke().first()

            // Then
            assertNull(result)
            verify { matchRepository.getAllMatches() }
        }

    @Test
    fun `invoke should return null when no matches exist`() =
        runTest {
            // Given
            every { matchRepository.getAllMatches() } returns flowOf(emptyList())

            // When
            val result = getActiveMatchUseCase.invoke().first()

            // Then
            assertNull(result)
            verify { matchRepository.getAllMatches() }
        }

    @Test
    fun `invoke should return first active match when multiple active matches exist`() =
        runTest {
            // Given
            val firstActiveMatch =
                Match(
                    id = 1L,
                    teamId = 1L,
                    teamName = "Team A",
                    opponent = "Team B",
                    location = "Stadium",
                    periodType = PeriodType.HALF_TIME,
                    captainId = 1L,
                    status = MatchStatus.IN_PROGRESS,
                )
            val secondActiveMatch =
                Match(
                    id = 2L,
                    teamId = 1L,
                    teamName = "Team A",
                    opponent = "Team C",
                    location = "Stadium",
                    periodType = PeriodType.HALF_TIME,
                    captainId = 1L,
                    status = MatchStatus.PAUSED,
                )
            every { matchRepository.getAllMatches() } returns flowOf(listOf(firstActiveMatch, secondActiveMatch))

            // When
            val result = getActiveMatchUseCase.invoke().first()

            // Then
            assertEquals(firstActiveMatch, result)
            verify { matchRepository.getAllMatches() }
        }
}
