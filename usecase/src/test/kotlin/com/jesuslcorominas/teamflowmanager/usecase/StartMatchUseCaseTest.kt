package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class StartMatchUseCaseTest {
    private lateinit var matchRepository: MatchRepository
    private lateinit var getMatchByIdUseCase: GetMatchByIdUseCase
    private lateinit var startPlayerTimerUseCase: StartPlayerTimerUseCase
    private lateinit var startMatchUseCase: StartMatchUseCase

    @Before
    fun setup() {
        matchRepository = mockk(relaxed = true)
        getMatchByIdUseCase = mockk(relaxed = true)
        startPlayerTimerUseCase = mockk(relaxed = true)
        startMatchUseCase =
            StartMatchUseCaseImpl(
                matchRepository,
                getMatchByIdUseCase,
                startPlayerTimerUseCase,
            )
    }

    @Test
    fun `invoke should update match and start timers for starting lineup players`() =
        runTest {
            // Given
            val matchId = 1L
            val currentTime = 1000L
            val startingLineupIds = listOf(1L, 2L, 3L)
            val match =
                Match(
                    id = matchId,
                    startingLineupIds = startingLineupIds,
                    isRunning = false,
                    lastStartTimeMillis = null,
                )

            coEvery { getMatchByIdUseCase(matchId) } returns flowOf(match)

            // When
            startMatchUseCase.invoke(matchId, currentTime)

            // Then
            val expectedMatch =
                match.copy(
                    isRunning = true,
                    lastStartTimeMillis = currentTime,
                )
            coVerify { matchRepository.updateMatch(expectedMatch) }
            coVerify { startPlayerTimerUseCase(1L, currentTime) }
            coVerify { startPlayerTimerUseCase(2L, currentTime) }
            coVerify { startPlayerTimerUseCase(3L, currentTime) }
        }

    @Test
    fun `invoke should start timers for empty starting lineup without errors`() =
        runTest {
            // Given
            val matchId = 1L
            val currentTime = 1000L
            val match =
                Match(
                    id = matchId,
                    startingLineupIds = emptyList(),
                    isRunning = false,
                    lastStartTimeMillis = null,
                )

            coEvery { getMatchByIdUseCase(matchId) } returns flowOf(match)

            // When
            startMatchUseCase.invoke(matchId, currentTime)

            // Then
            val expectedMatch =
                match.copy(
                    isRunning = true,
                    lastStartTimeMillis = currentTime,
                )
            coVerify { matchRepository.updateMatch(expectedMatch) }
            coVerify(exactly = 0) { startPlayerTimerUseCase(any(), any()) }
        }

    @Test
    fun `invoke should throw exception when match not found`() = runTest {
        // Given
        val matchId = 999L
        val currentTime = 1000L

        coEvery { getMatchByIdUseCase(matchId) } returns flowOf(null)

        // When
        val result = runCatching {
            startMatchUseCase.invoke(matchId, currentTime)
        }

        // Then
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }
}
