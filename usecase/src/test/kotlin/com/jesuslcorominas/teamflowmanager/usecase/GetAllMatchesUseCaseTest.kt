package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.PeriodType
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

class GetAllMatchesUseCaseTest {
    private lateinit var matchRepository: MatchRepository
    private lateinit var getAllMatchesUseCase: GetAllMatchesUseCase

    @Before
    fun setup() {
        matchRepository = mockk(relaxed = true)
        getAllMatchesUseCase = GetAllMatchesUseCaseImpl(matchRepository)
    }

    @Test
    fun `invoke should return all matches from repository`() =
        runTest {
            // Given
            val matches =
                listOf(
                    Match(
                        id = 1L,
                        teamId = 1L,
                        opponent = "Rival FC",
                        location = "Stadium 1",
                        dateTime = System.currentTimeMillis(),
                        periodType = PeriodType.HALF_TIME,
                        captainId = 1L,
                        teamName = "Team A"
                    ),
                    Match(
                        id = 2L,
                        teamId = 1L,
                        opponent = "Team B",
                        location = "Stadium 2",
                        dateTime = System.currentTimeMillis(),
                        periodType = PeriodType.HALF_TIME,
                        captainId = 1L,
                        teamName = "Team A"
                    ),
                )
            every { matchRepository.getAllMatches() } returns flowOf(matches)

            // When
            val result = getAllMatchesUseCase.invoke().toList()

            // Then
            assertEquals(1, result.size)
            assertEquals(matches, result[0])
            verify { matchRepository.getAllMatches() }
        }
}
