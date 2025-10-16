package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SetCurrentMatchUseCaseTest {
    private lateinit var matchRepository: MatchRepository
    private lateinit var getMatchByIdUseCase: GetMatchByIdUseCase
    private lateinit var useCase: SetCurrentMatchUseCase

    @Before
    fun setUp() {
        matchRepository = mockk(relaxed = true)
        getMatchByIdUseCase = mockk()
        useCase = SetCurrentMatchUseCaseImpl(matchRepository, getMatchByIdUseCase)
    }

    @Test
    fun `invoke should set match status to IN_PROGRESS and not running`() = runTest {
        // Given
        val matchId = 1L
        val match = Match(
            id = matchId,
            status = MatchStatus.SCHEDULED,
            isRunning = false,
        )
        val expectedMatch = match.copy(
            status = MatchStatus.IN_PROGRESS,
            isRunning = false,
        )

        coEvery { getMatchByIdUseCase(matchId) } returns flowOf(match)

        // When
        useCase(matchId)

        // Then
        coVerify { matchRepository.updateMatch(expectedMatch) }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invoke should throw exception when match not found`() = runTest {
        // Given
        val matchId = 1L
        coEvery { getMatchByIdUseCase(matchId) } returns flowOf(null)

        // When
        useCase(matchId)

        // Then - exception is thrown
    }
}
