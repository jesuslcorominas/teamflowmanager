package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Match
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

class GetMatchUseCaseTest {
    private lateinit var matchRepository: MatchRepository
    private lateinit var getMatchUseCase: GetMatchUseCase

    @Before
    fun setup() {
        matchRepository = mockk(relaxed = true)
        getMatchUseCase = GetMatchUseCaseImpl(matchRepository)
    }

    @Test
    fun `invoke should return match from repository`() =
        runTest {
            // Given
            val match = Match(id = 1L, elapsedTimeMillis = 5000L, isRunning = true)
            every { matchRepository.getMatch() } returns flowOf(match)

            // When
            val result = getMatchUseCase.invoke().first()

            // Then
            assertEquals(match, result)
            verify { matchRepository.getMatch() }
        }

    @Test
    fun `invoke should return null when no match exists`() =
        runTest {
            // Given
            every { matchRepository.getMatch() } returns flowOf(null)

            // When
            val result = getMatchUseCase.invoke().first()

            // Then
            assertNull(result)
            verify { matchRepository.getMatch() }
        }
}
