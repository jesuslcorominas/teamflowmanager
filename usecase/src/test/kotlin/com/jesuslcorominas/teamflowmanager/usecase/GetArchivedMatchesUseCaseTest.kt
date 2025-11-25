package com.jesuslcorominas.teamflowmanager.usecase

import app.cash.turbine.test
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetArchivedMatchesUseCaseTest {
    private lateinit var matchRepository: MatchRepository
    private lateinit var getArchivedMatchesUseCase: GetArchivedMatchesUseCase

    @Before
    fun setup() {
        matchRepository = mockk()
        getArchivedMatchesUseCase = GetArchivedMatchesUseCaseImpl(matchRepository)
    }

    @Test
    fun `invoke should return archived matches from repository`() =
        runTest {
            // Given
            val archivedMatches =
                listOf(
                    Match(
                        id = 1L,
                        opponent = "Team A",
                        status = MatchStatus.FINISHED,
                        archived = true,
                        teamName = "Team A"
                    ),
                    Match(
                        id = 2L,
                        opponent = "Team B",
                        status = MatchStatus.FINISHED,
                        archived = true,
                        teamName = "Team A"
                    ),
                )
            every { matchRepository.getArchivedMatches() } returns flowOf(archivedMatches)

            // When
            val result = getArchivedMatchesUseCase()

            // Then
            result.test {
                assertEquals(archivedMatches, awaitItem())
                awaitComplete()
            }
        }

    @Test
    fun `invoke should return empty list when no archived matches`() =
        runTest {
            // Given
            every { matchRepository.getArchivedMatches() } returns flowOf(emptyList())

            // When
            val result = getArchivedMatchesUseCase()

            // Then
            result.test {
                assertEquals(emptyList<Match>(), awaitItem())
                awaitComplete()
            }
        }
}
