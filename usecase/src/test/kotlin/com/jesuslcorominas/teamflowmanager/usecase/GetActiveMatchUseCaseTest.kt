package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchPeriod
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.domain.model.PeriodType
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetActiveMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class GetActiveMatchUseCaseTest {
    private lateinit var matchRepository: MatchRepository
    private lateinit var useCase: GetActiveMatchUseCase

    @Before
    fun setup() {
        matchRepository = mockk()
        useCase = GetActiveMatchUseCaseImpl(matchRepository)
    }

    @Test
    fun `invoke should return null when no active matches`() = runTest {
        val matches = listOf(createMatch(1L, MatchStatus.SCHEDULED), createMatch(2L, MatchStatus.FINISHED))
        every { matchRepository.getAllMatches() } returns flowOf(matches)

        val result = useCase.invoke().first()

        assertNull(result)
    }

    @Test
    fun `invoke should return in-progress match`() = runTest {
        val activeMatch = createMatch(2L, MatchStatus.IN_PROGRESS)
        val matches = listOf(createMatch(1L, MatchStatus.SCHEDULED), activeMatch, createMatch(3L, MatchStatus.FINISHED))
        every { matchRepository.getAllMatches() } returns flowOf(matches)

        val result = useCase.invoke().first()

        assertEquals(2L, result?.id)
    }

    @Test
    fun `invoke should return paused match`() = runTest {
        val pausedMatch = createMatch(1L, MatchStatus.PAUSED)
        every { matchRepository.getAllMatches() } returns flowOf(listOf(pausedMatch))

        val result = useCase.invoke().first()

        assertEquals(1L, result?.id)
    }

    @Test
    fun `invoke should return timeout match`() = runTest {
        val timeoutMatch = createMatch(1L, MatchStatus.TIMEOUT)
        every { matchRepository.getAllMatches() } returns flowOf(listOf(timeoutMatch))

        val result = useCase.invoke().first()

        assertEquals(1L, result?.id)
    }

    @Test
    fun `givenEmptyMatchList_whenInvoke_thenReturnNull`() = runTest {
        every { matchRepository.getAllMatches() } returns flowOf(emptyList())

        val result = useCase.invoke().first()

        assertNull(result)
    }

    @Test
    fun `givenMultipleActiveMatches_whenInvoke_thenReturnFirstActiveMatch`() = runTest {
        val firstActive = createMatch(1L, MatchStatus.PAUSED)
        val secondActive = createMatch(2L, MatchStatus.IN_PROGRESS)
        every { matchRepository.getAllMatches() } returns flowOf(listOf(firstActive, secondActive))

        val result = useCase.invoke().first()

        assertEquals(1L, result?.id)
    }

    private fun createMatch(id: Long, status: MatchStatus) = Match(
        id = id,
        teamName = "Team A",
        opponent = "Opponent",
        location = "Stadium",
        periodType = PeriodType.HALF_TIME,
        captainId = 1L,
        status = status,
        periods = listOf(MatchPeriod(periodNumber = 1, periodDuration = 1500000L)),
    )
}
