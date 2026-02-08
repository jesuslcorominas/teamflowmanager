package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.MatchDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.*
import io.mockk.Called
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

    @Test
    fun `getMatchById should return match from data source`() =
        runTest {
            // Given
            val match = Match(id = 1L, teamName = "Team A", opponent = "Opponent", location = "Location", periodType = PeriodType.HALF_TIME, captainId = 1L)
            every { matchDataSource.getMatchById(1L) } returns flowOf(match)

            // When
            val result = repository.getMatchById(1L).first()

            // Then
            assertEquals(match, result)
        }

    @Test
    fun `startTimer should not do anything when match does not exist`() =
        runTest {
            // Given
            val currentTime = 1000L
            every { matchDataSource.getMatchById(1L) } returns flowOf(null)

            // When
            repository.startTimer(1L, currentTime)

            // Then
            coVerify(exactly = 0) { matchDataSource.updateMatch(any()) }
        }

    @Test
    fun `startTimer should update existing match to in progress and set start time of first not started period`() =
        runTest {
            // Given
            val currentTime = 2000L
            val existingMatch = Match(
                id = 1L,
                teamName = "Team A",
                opponent = "Opponent",
                location = "Location",
                periodType = PeriodType.HALF_TIME,
                captainId = 1L,
                periods = listOf(
                    MatchPeriod(1, 1000L, 0L, 0L),
                    MatchPeriod(2, 1000L, 0L, 0L)
                )
            )
            every { matchDataSource.getMatchById(1L) } returns flowOf(existingMatch)
            coEvery { matchDataSource.updateMatch(any()) } returns Unit

            // When
            repository.startTimer(existingMatch.id, currentTime)

            // Then
            coVerify {
                matchDataSource.updateMatch(
                    match {
                        it.id == 1L &&
                            it.status == MatchStatus.IN_PROGRESS &&
                            it.periods[0].startTimeMillis == currentTime
                    },
                )
            }
        }

    @Test
    fun `pauseTimer should update end time of first not finished period and set status to paused`() =
        runTest {
            // Given
            val startTime = 1000L
            val pauseTime = 3000L
            val existingMatch = Match(
                id = 1L,
                teamName = "Team A",
                opponent = "Opponent",
                location = "Location",
                periodType = PeriodType.HALF_TIME,
                captainId = 1L,
                status = MatchStatus.IN_PROGRESS,
                periods = listOf(
                    MatchPeriod(1, 1000L, startTime, 0L),
                    MatchPeriod(2, 1000L, 0L, 0L)
                )
            )
            every { matchDataSource.getMatchById(1L) } returns flowOf(existingMatch)
            coEvery { matchDataSource.updateMatch(any()) } returns Unit

            // When
            repository.pauseTimer(1L, pauseTime)

            // Then
            coVerify {
                matchDataSource.updateMatch(
                    match {
                        it.id == 1L &&
                            it.status == MatchStatus.PAUSED &&
                            it.periods[0].endTimeMillis == pauseTime &&
                            it.pauseCount == 1
                    },
                )
            }
        }

    @Test
    fun `pauseTimer should do nothing when match is not in progress`() =
        runTest {
            // Given
            val pauseTime = 3000L
            val existingMatch = Match(
                id = 1L,
                teamName = "Team A",
                opponent = "Opponent",
                location = "Location",
                periodType = PeriodType.HALF_TIME,
                captainId = 1L,
                status = MatchStatus.PAUSED
            )
            every { matchDataSource.getMatchById(1L) } returns flowOf(existingMatch)

            // When
            repository.pauseTimer(1L, pauseTime)

            // Then
            coVerify(exactly = 0) { matchDataSource.updateMatch(any()) }
        }

    @Test
    fun `pauseTimer should do nothing when no match exists`() =
        runTest {
            // Given
            val pauseTime = 3000L
            every { matchDataSource.getMatchById(1L) } returns flowOf(null)

            // When
            repository.pauseTimer(1L, pauseTime)

            // Then
            coVerify(exactly = 0) { matchDataSource.updateMatch(any()) }
        }
}
