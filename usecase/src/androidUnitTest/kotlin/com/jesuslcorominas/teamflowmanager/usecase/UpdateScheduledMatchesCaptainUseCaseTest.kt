package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.domain.model.PeriodType
import com.jesuslcorominas.teamflowmanager.domain.usecase.UpdateScheduledMatchesCaptainUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class UpdateScheduledMatchesCaptainUseCaseTest {
    private lateinit var matchRepository: MatchRepository
    private lateinit var useCase: UpdateScheduledMatchesCaptainUseCase

    @Before
    fun setup() {
        matchRepository = mockk(relaxed = true)
        useCase = UpdateScheduledMatchesCaptainUseCaseImpl(matchRepository)
    }

    @Test
    fun `invoke should update captain in all scheduled matches`() = runTest {
        // Given
        val captainId = "42"
        val scheduledMatches = listOf(
            Match(
                id = "1",
                opponent = "Team A",
                location = "Stadium",
                squadCallUpIds = listOf("1", "2", "3"),
                captainId = "1",
                status = MatchStatus.SCHEDULED,
                teamName = "Team B",
                periodType = PeriodType.HALF_TIME,
            ),
            Match(
                id = "2",
                opponent = "Team B",
                location = "Stadium",
                squadCallUpIds = listOf("1", "2", "3"),
                captainId = "1",
                status = MatchStatus.SCHEDULED,
                teamName = "Team B",
                periodType = PeriodType.HALF_TIME,
            )
        )
        coEvery { matchRepository.getScheduledMatches() } returns scheduledMatches

        // When
        useCase.invoke(captainId)

        // Then
        coVerify { matchRepository.updateMatchCaptain("1", captainId) }
        coVerify { matchRepository.updateMatchCaptain("2", captainId) }
    }

    @Test
    fun `invoke should clear captain in all scheduled matches when null`() = runTest {
        // Given
        val scheduledMatches = listOf(
            Match(
                id = "1",
                opponent = "Team A",
                location = "Stadium",
                squadCallUpIds = listOf("1", "2", "3"),
                captainId = "42",
                status = MatchStatus.SCHEDULED,
                teamName = "Team B",
                periodType = PeriodType.HALF_TIME,
            )
        )
        coEvery { matchRepository.getScheduledMatches() } returns scheduledMatches

        // When
        useCase.invoke(null)

        // Then
        coVerify { matchRepository.updateMatchCaptain("1", null) }
    }

    @Test
    fun `invoke should do nothing when no scheduled matches exist`() = runTest {
        // Given
        coEvery { matchRepository.getScheduledMatches() } returns emptyList()

        // When
        useCase.invoke("42")

        // Then
        coVerify(exactly = 0) { matchRepository.updateMatchCaptain(any(), any()) }
    }
}
