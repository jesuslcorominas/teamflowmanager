package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.PeriodType
import com.jesuslcorominas.teamflowmanager.domain.usecase.UpdateMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class UpdateMatchUseCaseTest {
    private lateinit var matchRepository: MatchRepository
    private lateinit var updateMatchUseCase: UpdateMatchUseCase

    @Before
    fun setup() {
        matchRepository = mockk(relaxed = true)
        updateMatchUseCase = UpdateMatchUseCaseImpl(matchRepository)
    }

    @Test
    fun `invoke should update match in repository`() =
        runTest {
            // Given
            val match =
                Match(
                    id = 1L,
                    teamId = 1L,
                    opponent = "Updated Rival FC",
                    location = "New Stadium",
                    startingLineupIds = listOf(1L, 2L, 3L),
                    teamName = "Team B",
                    periodType = PeriodType.HALF_TIME,
                    captainId = 1L,
                )

            // When
            updateMatchUseCase.invoke(match)

            // Then
            coVerify { matchRepository.updateMatch(match) }
        }
}
