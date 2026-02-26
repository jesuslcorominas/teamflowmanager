package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchPeriod
import com.jesuslcorominas.teamflowmanager.domain.model.PeriodType
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetScheduledMatchesUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetScheduledMatchesUseCaseTest {
    private lateinit var matchRepository: MatchRepository
    private lateinit var useCase: GetScheduledMatchesUseCase

    @Before
    fun setup() {
        matchRepository = mockk()
        useCase = GetScheduledMatchesUseCaseImpl(matchRepository)
    }

    @Test
    fun `invoke should return empty list when no scheduled matches`() = runTest {
        coEvery { matchRepository.getScheduledMatches() } returns emptyList()

        val result = useCase.invoke()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `invoke should return scheduled matches from repository`() = runTest {
        val match1 = Match(
            id = 1L,
            teamName = "Team A",
            opponent = "Opponent 1",
            location = "Stadium",
            periodType = PeriodType.HALF_TIME,
            captainId = 1L,
            periods = listOf(MatchPeriod(periodNumber = 1, periodDuration = 1500000L)),
        )
        val match2 = Match(
            id = 2L,
            teamName = "Team A",
            opponent = "Opponent 2",
            location = "Arena",
            periodType = PeriodType.HALF_TIME,
            captainId = 1L,
            periods = listOf(MatchPeriod(periodNumber = 1, periodDuration = 1500000L)),
        )
        coEvery { matchRepository.getScheduledMatches() } returns listOf(match1, match2)

        val result = useCase.invoke()

        assertEquals(2, result.size)
        assertEquals(1L, result[0].id)
        assertEquals(2L, result[1].id)
    }
}
