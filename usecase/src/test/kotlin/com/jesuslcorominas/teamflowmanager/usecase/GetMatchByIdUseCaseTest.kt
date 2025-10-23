package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetMatchByIdUseCaseTest {
    private lateinit var matchRepository: MatchRepository
    private lateinit var getMatchByIdUseCase: GetMatchByIdUseCase

    @Before
    fun setup() {
        matchRepository = mockk(relaxed = true)
        getMatchByIdUseCase = GetMatchByIdUseCaseImpl(matchRepository)
    }

    @Test
    fun `invoke should return match from repository by id`() =
        runTest {
            // Given
            val matchId = 1L
            val match =
                Match(
                    id = matchId,
                    teamId = 1L,
                    opponent = "Rival FC",
                    location = "Stadium",
                    date = System.currentTimeMillis(),
                    teamName = "Team B"
                )
            every { matchRepository.getMatchById(matchId) } returns flowOf(match)

            // When
            val result = getMatchByIdUseCase.invoke(matchId).toList()

            // Then
            assertEquals(1, result.size)
            assertEquals(match, result[0])
            verify { matchRepository.getMatchById(matchId) }
        }

    @Test
    fun `invoke should return null when match not found`() =
        runTest {
            // Given
            val matchId = 999L
            every { matchRepository.getMatchById(matchId) } returns flowOf(null)

            // When
            val result = getMatchByIdUseCase.invoke(matchId).toList()

            // Then
            assertEquals(1, result.size)
            assertEquals(null, result[0])
            verify { matchRepository.getMatchById(matchId) }
        }
}
