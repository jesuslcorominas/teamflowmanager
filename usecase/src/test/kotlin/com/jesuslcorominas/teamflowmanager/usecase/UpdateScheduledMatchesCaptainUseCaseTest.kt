package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.PeriodType
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import com.jesuslcorominas.teamflowmanager.domain.utils.TransactionRunner

class UpdateScheduledMatchesCaptainUseCaseTest {
    private lateinit var matchRepository: MatchRepository
    private lateinit var useCase: UpdateScheduledMatchesCaptainUseCase
    private lateinit var transactionRunner: TransactionRunner

    @Before
    fun setup() {
        matchRepository = mockk(relaxed = true)
        transactionRunner = mockk(relaxed = true)
        useCase = UpdateScheduledMatchesCaptainUseCaseImpl(matchRepository, transactionRunner)
    }

    @Test
    fun `invoke should update captain in all scheduled matches`() = runTest {
        // Given
        val captainId = 42L
        val scheduledMatches = listOf(
            Match(
                id = 1L,
                opponent = "Team A",
                location = "Stadium A",
                periodType = PeriodType.HALF_TIME,
                squadCallUpIds = listOf(1L, 2L, 3L),
                captainId = 1L,
                status = MatchStatus.SCHEDULED,
                teamName = "Team B"
            ),
            Match(
                id = 2L,
                opponent = "Team B",
                location = "Stadium B",
                periodType = PeriodType.HALF_TIME,
                squadCallUpIds = listOf(1L, 2L, 3L),
                captainId = 1L,
                status = MatchStatus.SCHEDULED,
                teamName = "Team B"
            )
        )
        coEvery { matchRepository.getScheduledMatches() } returns scheduledMatches

        // When
        useCase.invoke(captainId)

        // Then
        coVerify { matchRepository.updateMatchCaptain(1L, captainId) }
        coVerify { matchRepository.updateMatchCaptain(2L, captainId) }
    }

    @Test
    fun `invoke should clear captain in all scheduled matches when null`() = runTest {
        // Given
        val scheduledMatches = listOf(
            Match(
                id = 1L,
                opponent = "Team A",
                location = "Stadium A",
                periodType = PeriodType.HALF_TIME,
                squadCallUpIds = listOf(1L, 2L, 3L),
                captainId = 42L,
                status = MatchStatus.SCHEDULED,
                teamName = "Team B"
            )
        )
        coEvery { matchRepository.getScheduledMatches() } returns scheduledMatches

        // When
        useCase.invoke(null)

        // Then
        coVerify { matchRepository.updateMatchCaptain(1L, null) }
    }

    @Test
    fun `invoke should do nothing when no scheduled matches exist`() = runTest {
        // Given
        coEvery { matchRepository.getScheduledMatches() } returns emptyList()

        // When
        useCase.invoke(42L)

        // Then
        coVerify(exactly = 0) { matchRepository.updateMatchCaptain(any(), any()) }
    }
}
