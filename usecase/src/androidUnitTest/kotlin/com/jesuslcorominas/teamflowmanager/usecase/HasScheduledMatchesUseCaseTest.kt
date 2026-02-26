package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchPeriod
import com.jesuslcorominas.teamflowmanager.domain.model.PeriodType
import com.jesuslcorominas.teamflowmanager.domain.usecase.HasScheduledMatchesUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HasScheduledMatchesUseCaseTest {
    private lateinit var matchRepository: MatchRepository
    private lateinit var useCase: HasScheduledMatchesUseCase

    @Before
    fun setup() {
        matchRepository = mockk()
        useCase = HasScheduledMatchesUseCaseImpl(matchRepository)
    }

    @Test
    fun `invoke should return false when no scheduled matches exist`() = runTest {
        coEvery { matchRepository.getScheduledMatches() } returns emptyList()

        val result = useCase.invoke()

        assertFalse(result)
    }

    @Test
    fun `invoke should return true when scheduled matches exist`() = runTest {
        val match = Match(
            id = 1L,
            teamName = "Team A",
            opponent = "Opponent",
            location = "Stadium",
            periodType = PeriodType.HALF_TIME,
            captainId = 1L,
            periods = listOf(MatchPeriod(periodNumber = 1, periodDuration = 1500000L)),
        )
        coEvery { matchRepository.getScheduledMatches() } returns listOf(match)

        val result = useCase.invoke()

        assertTrue(result)
    }
}
