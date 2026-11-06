package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.PeriodType
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetMatchByIdUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchOperationRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import io.mockk.coVerify
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
    private lateinit var matchOperationRepository: MatchOperationRepository
    private lateinit var getMatchByIdUseCase: GetMatchByIdUseCase

    @Before
    fun setup() {
        matchRepository = mockk(relaxed = true)
        matchOperationRepository = mockk(relaxed = true)
        getMatchByIdUseCase = GetMatchByIdUseCaseImpl(matchRepository, matchOperationRepository)
    }

    @Test
    fun `invoke should return match from repository by id`() =
        runTest {
            // Given
            val matchId = "1"
            val match =
                Match(
                    id = matchId,
                    teamId = "1",
                    opponent = "Rival FC",
                    location = "Stadium",
                    teamName = "Team B",
                    periodType = PeriodType.HALF_TIME,
                    captainId = "1",
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
            val matchId = "999"
            every { matchRepository.getMatchById(matchId) } returns flowOf(null)

            // When
            val result = getMatchByIdUseCase.invoke(matchId).toList()

            // Then
            assertEquals(1, result.size)
            assertEquals(null, result[0])
            verify { matchRepository.getMatchById(matchId) }
        }

    // -----------------------------------------------------------------------------------------
    // Regression test — #416: stale operation sweep triggered when match is opened
    // -----------------------------------------------------------------------------------------

    /**
     * #416 — Verifies that opening a match triggers [MatchOperationRepository.markStaleOperationsAsFailed]
     * with a 2-minute threshold (120 000 ms) so that operations orphaned by crashed sessions are
     * swept before the UI renders.
     *
     * This test fails until T005 refactors [GetMatchByIdUseCaseImpl] to accept
     * [MatchOperationRepository] and call [markStaleOperationsAsFailed] on each invocation.
     * The 2-minute threshold covers both sides: operations created 1 min ago are NOT swept;
     * operations created 3 min ago are swept — the actual age filtering is performed by the
     * datasource, which receives [olderThanMillis] = 120 000.
     */
    @Test
    fun `#416 when match is opened, markStaleOperationsAsFailed is called with 2-minute threshold`() =
        runTest {
            // Given
            val matchId = "1"
            val twoMinutesMillis = 2 * 60 * 1000L
            val match =
                Match(
                    id = matchId,
                    teamId = "1",
                    opponent = "Rival FC",
                    location = "Stadium",
                    teamName = "Team B",
                    periodType = PeriodType.HALF_TIME,
                    captainId = "1",
                )
            every { matchRepository.getMatchById(matchId) } returns flowOf(match)

            // When
            getMatchByIdUseCase.invoke(matchId).toList()

            // Then — stale operations are swept with the 2-minute threshold
            coVerify(exactly = 1) {
                matchOperationRepository.markStaleOperationsAsFailed(matchId, twoMinutesMillis)
            }
        }
}
