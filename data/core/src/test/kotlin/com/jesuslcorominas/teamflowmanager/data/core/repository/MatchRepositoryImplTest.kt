package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.MatchDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchPeriod
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.domain.model.PeriodType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class MatchRepositoryImplTest {

    private lateinit var matchDataSource: MatchDataSource
    private lateinit var repository: MatchRepositoryImpl

    @Before
    fun setup() {
        matchDataSource = mockk(relaxed = true)
        repository = MatchRepositoryImpl(matchDataSource)
    }

    private fun createMatch(
        id: Long = 1L,
        status: MatchStatus = MatchStatus.SCHEDULED,
        pauseCount: Int = 0,
        timeoutStartTimeMillis: Long = 0L,
        archived: Boolean = false,
        periods: List<MatchPeriod> = listOf(
            MatchPeriod(periodNumber = 1),
            MatchPeriod(periodNumber = 2),
        ),
    ) = Match(
        id = id,
        teamName = "Team A",
        opponent = "Team B",
        location = "Home",
        periodType = PeriodType.HALF_TIME,
        captainId = 1L,
        status = status,
        pauseCount = pauseCount,
        timeoutStartTimeMillis = timeoutStartTimeMillis,
        archived = archived,
        periods = periods,
    )

    // --- getMatchById ---

    @Test
    fun `givenExistingMatch_whenGetMatchById_thenDelegatesToDataSource`() = runTest {
        val match = createMatch(id = 1L)
        every { matchDataSource.getMatchById(1L) } returns flowOf(match)

        val result = repository.getMatchById(1L).first()

        assertEquals(match, result)
    }

    @Test
    fun `givenNoMatch_whenGetMatchById_thenReturnsNull`() = runTest {
        every { matchDataSource.getMatchById(99L) } returns flowOf(null)

        val result = repository.getMatchById(99L).first()

        assertEquals(null, result)
    }

    // --- getAllMatches ---

    @Test
    fun `givenMultipleMatches_whenGetAllMatches_thenDelegatesToDataSource`() = runTest {
        val matches = listOf(createMatch(id = 1L), createMatch(id = 2L))
        every { matchDataSource.getAllMatches() } returns flowOf(matches)

        val result = repository.getAllMatches().first()

        assertEquals(matches, result)
    }

    @Test
    fun `givenNoMatches_whenGetAllMatches_thenReturnsEmptyList`() = runTest {
        every { matchDataSource.getAllMatches() } returns flowOf(emptyList())

        val result = repository.getAllMatches().first()

        assertEquals(emptyList<Match>(), result)
    }

    // --- getArchivedMatches ---

    @Test
    fun `givenArchivedMatches_whenGetArchivedMatches_thenDelegatesToDataSource`() = runTest {
        val matches = listOf(createMatch(id = 1L, archived = true))
        every { matchDataSource.getArchivedMatches() } returns flowOf(matches)

        val result = repository.getArchivedMatches().first()

        assertEquals(matches, result)
    }

    // --- getScheduledMatches ---

    @Test
    fun `givenScheduledMatches_whenGetScheduledMatches_thenDelegatesToDataSource`() = runTest {
        val matches = listOf(createMatch(id = 1L, status = MatchStatus.SCHEDULED))
        coEvery { matchDataSource.getScheduledMatches() } returns matches

        val result = repository.getScheduledMatches()

        assertEquals(matches, result)
    }

    // --- updateMatchCaptain ---

    @Test
    fun `givenMatchIdAndCaptainId_whenUpdateMatchCaptain_thenDelegatesToDataSource`() = runTest {
        repository.updateMatchCaptain(1L, 5L)

        coVerify { matchDataSource.updateMatchCaptain(1L, 5L) }
    }

    @Test
    fun `givenMatchIdAndNullCaptainId_whenUpdateMatchCaptain_thenDelegatesToDataSource`() = runTest {
        repository.updateMatchCaptain(1L, null)

        coVerify { matchDataSource.updateMatchCaptain(1L, null) }
    }

    // --- createMatch ---

    @Test
    fun `givenMatch_whenCreateMatch_thenReturnsInsertedId`() = runTest {
        val match = createMatch(id = 0L)
        coEvery { matchDataSource.insertMatch(match) } returns 42L

        val result = repository.createMatch(match)

        assertEquals(42L, result)
        coVerify { matchDataSource.insertMatch(match) }
    }

    // --- updateMatch ---

    @Test
    fun `givenMatch_whenUpdateMatch_thenDelegatesToDataSource`() = runTest {
        val match = createMatch()

        repository.updateMatch(match)

        coVerify { matchDataSource.updateMatch(match) }
    }

    // --- deleteMatch ---

    @Test
    fun `givenMatchId_whenDeleteMatch_thenDelegatesToDataSource`() = runTest {
        repository.deleteMatch(1L)

        coVerify { matchDataSource.deleteMatch(1L) }
    }

    // --- startTimer ---

    @Test
    fun `givenMatchDoesNotExist_whenStartTimer_thenDoesNothing`() = runTest {
        every { matchDataSource.getMatchById(1L) } returns flowOf(null)

        repository.startTimer(1L, 1000L)

        coVerify(exactly = 0) { matchDataSource.updateMatch(any()) }
    }

    @Test
    fun `givenMatchWithFirstPeriodUnstarted_whenStartTimer_thenSetsStatusInProgressAndStartsFirstPeriod`() = runTest {
        val currentTime = 2000L
        val match = createMatch(
            status = MatchStatus.SCHEDULED,
            periods = listOf(
                MatchPeriod(periodNumber = 1, startTimeMillis = 0L),
                MatchPeriod(periodNumber = 2, startTimeMillis = 0L),
            ),
        )
        every { matchDataSource.getMatchById(1L) } returns flowOf(match)

        repository.startTimer(1L, currentTime)

        coVerify {
            matchDataSource.updateMatch(
                match {
                    it.status == MatchStatus.IN_PROGRESS &&
                        it.periods[0].startTimeMillis == currentTime &&
                        it.periods[1].startTimeMillis == 0L
                },
            )
        }
    }

    @Test
    fun `givenMatchWithFirstPeriodAlreadyFinished_whenStartTimer_thenStartsSecondPeriod`() = runTest {
        val currentTime = 5000L
        val match = createMatch(
            status = MatchStatus.PAUSED,
            periods = listOf(
                MatchPeriod(periodNumber = 1, startTimeMillis = 1000L, endTimeMillis = 3000L),
                MatchPeriod(periodNumber = 2, startTimeMillis = 0L),
            ),
        )
        every { matchDataSource.getMatchById(1L) } returns flowOf(match)

        repository.startTimer(1L, currentTime)

        coVerify {
            matchDataSource.updateMatch(
                match {
                    it.status == MatchStatus.IN_PROGRESS &&
                        it.periods[0].startTimeMillis == 1000L &&
                        it.periods[1].startTimeMillis == currentTime
                },
            )
        }
    }

    // --- pauseTimer ---

    @Test
    fun `givenMatchDoesNotExist_whenPauseTimer_thenDoesNothing`() = runTest {
        every { matchDataSource.getMatchById(1L) } returns flowOf(null)

        repository.pauseTimer(1L, 3000L)

        coVerify(exactly = 0) { matchDataSource.updateMatch(any()) }
    }

    @Test
    fun `givenMatchNotInProgress_whenPauseTimer_thenDoesNothing`() = runTest {
        val match = createMatch(status = MatchStatus.PAUSED)
        every { matchDataSource.getMatchById(1L) } returns flowOf(match)

        repository.pauseTimer(1L, 3000L)

        coVerify(exactly = 0) { matchDataSource.updateMatch(any()) }
    }

    @Test
    fun `givenMatchInProgress_whenPauseTimer_thenEndsFirstUnfinishedPeriodAndIncrementsPauseCount`() = runTest {
        val pauseTime = 4000L
        val match = createMatch(
            status = MatchStatus.IN_PROGRESS,
            pauseCount = 0,
            periods = listOf(
                MatchPeriod(periodNumber = 1, startTimeMillis = 1000L, endTimeMillis = 0L),
                MatchPeriod(periodNumber = 2, startTimeMillis = 0L, endTimeMillis = 0L),
            ),
        )
        every { matchDataSource.getMatchById(1L) } returns flowOf(match)

        repository.pauseTimer(1L, pauseTime)

        coVerify {
            matchDataSource.updateMatch(
                match {
                    it.status == MatchStatus.PAUSED &&
                        it.pauseCount == 1 &&
                        it.periods[0].endTimeMillis == pauseTime &&
                        it.periods[1].endTimeMillis == 0L
                },
            )
        }
    }

    @Test
    fun `givenMatchInProgressWithFirstPeriodAlreadyFinished_whenPauseTimer_thenEndsSecondPeriodAndIncrementsPauseCount`() = runTest {
        val pauseTime = 7000L
        val match = createMatch(
            status = MatchStatus.IN_PROGRESS,
            pauseCount = 1,
            periods = listOf(
                MatchPeriod(periodNumber = 1, startTimeMillis = 1000L, endTimeMillis = 4000L),
                MatchPeriod(periodNumber = 2, startTimeMillis = 5000L, endTimeMillis = 0L),
            ),
        )
        every { matchDataSource.getMatchById(1L) } returns flowOf(match)

        repository.pauseTimer(1L, pauseTime)

        coVerify {
            matchDataSource.updateMatch(
                match {
                    it.status == MatchStatus.PAUSED &&
                        it.pauseCount == 2 &&
                        it.periods[0].endTimeMillis == 4000L &&
                        it.periods[1].endTimeMillis == pauseTime
                },
            )
        }
    }

    // --- startTimeout ---

    @Test
    fun `givenMatchDoesNotExist_whenStartTimeout_thenDoesNothing`() = runTest {
        every { matchDataSource.getMatchById(1L) } returns flowOf(null)

        repository.startTimeout(1L, 2000L)

        coVerify(exactly = 0) { matchDataSource.updateMatch(any()) }
    }

    @Test
    fun `givenMatchNotInProgress_whenStartTimeout_thenDoesNothing`() = runTest {
        val match = createMatch(status = MatchStatus.PAUSED)
        every { matchDataSource.getMatchById(1L) } returns flowOf(match)

        repository.startTimeout(1L, 2000L)

        coVerify(exactly = 0) { matchDataSource.updateMatch(any()) }
    }

    @Test
    fun `givenMatchInProgress_whenStartTimeout_thenSetsStatusTimeoutAndSavesStartTime`() = runTest {
        val timeoutStart = 3000L
        val match = createMatch(status = MatchStatus.IN_PROGRESS)
        every { matchDataSource.getMatchById(1L) } returns flowOf(match)

        repository.startTimeout(1L, timeoutStart)

        coVerify {
            matchDataSource.updateMatch(
                match {
                    it.status == MatchStatus.TIMEOUT &&
                        it.timeoutStartTimeMillis == timeoutStart
                },
            )
        }
    }

    // --- endTimeout ---

    @Test
    fun `givenMatchDoesNotExist_whenEndTimeout_thenDoesNothing`() = runTest {
        every { matchDataSource.getMatchById(1L) } returns flowOf(null)

        repository.endTimeout(1L, 5000L)

        coVerify(exactly = 0) { matchDataSource.updateMatch(any()) }
    }

    @Test
    fun `givenMatchNotInTimeout_whenEndTimeout_thenDoesNothing`() = runTest {
        val match = createMatch(status = MatchStatus.IN_PROGRESS)
        every { matchDataSource.getMatchById(1L) } returns flowOf(match)

        repository.endTimeout(1L, 5000L)

        coVerify(exactly = 0) { matchDataSource.updateMatch(any()) }
    }

    @Test
    fun `givenMatchInTimeoutWithActivePeriod_whenEndTimeout_thenAdjustsPeriodStartTimeAndResumes`() = runTest {
        val timeoutStartTime = 3000L
        val endTime = 5000L
        val timeoutDuration = endTime - timeoutStartTime // 2000L
        val periodOriginalStart = 1000L
        val match = createMatch(
            status = MatchStatus.TIMEOUT,
            timeoutStartTimeMillis = timeoutStartTime,
            periods = listOf(
                MatchPeriod(periodNumber = 1, startTimeMillis = periodOriginalStart, endTimeMillis = 0L),
                MatchPeriod(periodNumber = 2, startTimeMillis = 0L, endTimeMillis = 0L),
            ),
        )
        every { matchDataSource.getMatchById(1L) } returns flowOf(match)

        repository.endTimeout(1L, endTime)

        coVerify {
            matchDataSource.updateMatch(
                match {
                    it.status == MatchStatus.IN_PROGRESS &&
                        it.timeoutStartTimeMillis == 0L &&
                        it.periods[0].startTimeMillis == periodOriginalStart + timeoutDuration &&
                        it.periods[1].startTimeMillis == 0L
                },
            )
        }
    }

    @Test
    fun `givenMatchInTimeoutWithNoActivePeriod_whenEndTimeout_thenResumesWithoutAdjustingPeriods`() = runTest {
        val timeoutStartTime = 3000L
        val endTime = 5000L
        val match = createMatch(
            status = MatchStatus.TIMEOUT,
            timeoutStartTimeMillis = timeoutStartTime,
            periods = listOf(
                MatchPeriod(periodNumber = 1, startTimeMillis = 0L, endTimeMillis = 0L),
                MatchPeriod(periodNumber = 2, startTimeMillis = 0L, endTimeMillis = 0L),
            ),
        )
        every { matchDataSource.getMatchById(1L) } returns flowOf(match)

        repository.endTimeout(1L, endTime)

        coVerify {
            matchDataSource.updateMatch(
                match {
                    it.status == MatchStatus.IN_PROGRESS &&
                        it.timeoutStartTimeMillis == 0L &&
                        it.periods[0].startTimeMillis == 0L &&
                        it.periods[1].startTimeMillis == 0L
                },
            )
        }
    }

    @Test
    fun `givenMatchInTimeoutWithFirstPeriodFinishedAndSecondUnstarted_whenEndTimeout_thenResumesWithoutAdjustingPeriods`() = runTest {
        val timeoutStartTime = 5000L
        val endTime = 7000L
        val match = createMatch(
            status = MatchStatus.TIMEOUT,
            timeoutStartTimeMillis = timeoutStartTime,
            periods = listOf(
                MatchPeriod(periodNumber = 1, startTimeMillis = 1000L, endTimeMillis = 4000L),
                MatchPeriod(periodNumber = 2, startTimeMillis = 0L, endTimeMillis = 0L),
            ),
        )
        every { matchDataSource.getMatchById(1L) } returns flowOf(match)

        repository.endTimeout(1L, endTime)

        coVerify {
            matchDataSource.updateMatch(
                match {
                    it.status == MatchStatus.IN_PROGRESS &&
                        it.timeoutStartTimeMillis == 0L &&
                        it.periods[0].startTimeMillis == 1000L &&
                        it.periods[1].startTimeMillis == 0L
                },
            )
        }
    }

    // --- archiveMatch ---

    @Test
    fun `givenMatchDoesNotExist_whenArchiveMatch_thenDoesNothing`() = runTest {
        every { matchDataSource.getMatchById(1L) } returns flowOf(null)

        repository.archiveMatch(1L)

        coVerify(exactly = 0) { matchDataSource.updateMatch(any()) }
    }

    @Test
    fun `givenUnarchivedMatch_whenArchiveMatch_thenSetsArchivedToTrue`() = runTest {
        val match = createMatch(archived = false)
        every { matchDataSource.getMatchById(1L) } returns flowOf(match)

        repository.archiveMatch(1L)

        coVerify {
            matchDataSource.updateMatch(match { it.archived })
        }
    }

    // --- unarchiveMatch ---

    @Test
    fun `givenMatchDoesNotExist_whenUnarchiveMatch_thenDoesNothing`() = runTest {
        every { matchDataSource.getMatchById(1L) } returns flowOf(null)

        repository.unarchiveMatch(1L)

        coVerify(exactly = 0) { matchDataSource.updateMatch(any()) }
    }

    @Test
    fun `givenArchivedMatch_whenUnarchiveMatch_thenSetsArchivedToFalse`() = runTest {
        val match = createMatch(archived = true)
        every { matchDataSource.getMatchById(1L) } returns flowOf(match)

        repository.unarchiveMatch(1L)

        coVerify {
            matchDataSource.updateMatch(match { !it.archived })
        }
    }

    // --- updateMatchWithOperationId ---

    @Test
    fun `givenMatchAndOperationId_whenUpdateMatchWithOperationId_thenUpdatesMatchWithOperationId`() = runTest {
        val match = createMatch()
        val operationId = "op-123"

        repository.updateMatchWithOperationId(match, operationId)

        coVerify {
            matchDataSource.updateMatch(
                match {
                    it.id == match.id && it.lastCompletedOperationId == operationId
                },
            )
        }
    }
}
