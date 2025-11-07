package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.MatchLocalDataSource
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
    private lateinit var localDataSource: MatchLocalDataSource
    private lateinit var repository: MatchRepositoryImpl

    @Before
    fun setup() {
        localDataSource = mockk(relaxed = true)
        repository = MatchRepositoryImpl(localDataSource)
    }

    @Test
    fun `getMatchById should return match from local data source`() =
        runTest {
            // Given
            val matchId = 1L
            val match = Match(
                id = matchId,
                teamName = "Team A",
                opponent = "Team B",
                location = "Stadium A",
                periodType = PeriodType.HALF_TIME,
                captainId = 1L
            )
            every { localDataSource.getMatchById(matchId) } returns flowOf(match)

            // When
            val result = repository.getMatchById(matchId).first()

            // Then
            assertEquals(match, result)
        }

    @Test
    fun `startTimer should not do anything when match does not exist`() =
        runTest {
            // Given
            val matchId = 1L
            val currentTime = 1000L
            every { localDataSource.getMatchById(matchId) } returns flowOf(null)

            // When
            repository.startTimer(matchId, currentTime)

            // Then
            coVerify(exactly = 0) { localDataSource.upsertMatch(any()) }
        }

    @Test
    fun `startTimer should start first period when no period has started`() =
        runTest {
            // Given
            val matchId = 1L
            val currentTime = 2000L
            val existingMatch = Match(
                id = matchId,
                teamName = "Team A",
                opponent = "Team B",
                location = "Stadium A",
                periodType = PeriodType.HALF_TIME,
                captainId = 1L,
                periods = listOf(
                    MatchPeriod(periodNumber = 1, periodDuration = PeriodType.HALF_TIME.duration),
                    MatchPeriod(periodNumber = 2, periodDuration = PeriodType.HALF_TIME.duration)
                )
            )
            every { localDataSource.getMatchById(matchId) } returns flowOf(existingMatch)
            coEvery { localDataSource.upsertMatch(any()) } returns Unit

            // When
            repository.startTimer(matchId, currentTime)

            // Then
            coVerify {
                localDataSource.upsertMatch(
                    match {
                        it.id == matchId &&
                            it.status == MatchStatus.IN_PROGRESS &&
                            it.periods[0].startTimeMillis == currentTime &&
                            it.periods[1].startTimeMillis == 0L
                    },
                )
            }
        }

    @Test
    fun `pauseTimer should end current period and update status to PAUSED`() =
        runTest {
            // Given
            val matchId = 1L
            val startTime = 1000L
            val pauseTime = 3000L
            val existingMatch = Match(
                id = matchId,
                teamName = "Team A",
                opponent = "Team B",
                location = "Stadium A",
                periodType = PeriodType.HALF_TIME,
                captainId = 1L,
                status = MatchStatus.IN_PROGRESS,
                pauseCount = 0,
                periods = listOf(
                    MatchPeriod(periodNumber = 1, periodDuration = PeriodType.HALF_TIME.duration, startTimeMillis = startTime),
                    MatchPeriod(periodNumber = 2, periodDuration = PeriodType.HALF_TIME.duration)
                )
            )
            every { localDataSource.getMatchById(matchId) } returns flowOf(existingMatch)
            coEvery { localDataSource.upsertMatch(any()) } returns Unit

            // When
            repository.pauseTimer(matchId, pauseTime)

            // Then
            coVerify {
                localDataSource.upsertMatch(
                    match {
                        it.id == matchId &&
                            it.status == MatchStatus.PAUSED &&
                            it.periods[0].endTimeMillis == pauseTime &&
                            it.pauseCount == 1
                    },
                )
            }
        }

    @Test
    fun `pauseTimer should do nothing when match is not IN_PROGRESS`() =
        runTest {
            // Given
            val matchId = 1L
            val pauseTime = 3000L
            val existingMatch = Match(
                id = matchId,
                teamName = "Team A",
                opponent = "Team B",
                location = "Stadium A",
                periodType = PeriodType.HALF_TIME,
                captainId = 1L,
                status = MatchStatus.SCHEDULED
            )
            every { localDataSource.getMatchById(matchId) } returns flowOf(existingMatch)

            // When
            repository.pauseTimer(matchId, pauseTime)

            // Then
            coVerify(exactly = 0) { localDataSource.upsertMatch(any()) }
        }

    @Test
    fun `pauseTimer should do nothing when no match exists`() =
        runTest {
            // Given
            val matchId = 1L
            val pauseTime = 3000L
            every { localDataSource.getMatchById(matchId) } returns flowOf(null)

            // When
            repository.pauseTimer(matchId, pauseTime)

            // Then
            coVerify(exactly = 0) { localDataSource.upsertMatch(any()) }
        }

    @Test
    fun `archiveMatch should set archived to true`() =
        runTest {
            // Given
            val matchId = 1L
            val match = Match(
                id = matchId,
                teamName = "Team A",
                opponent = "Team B",
                location = "Stadium A",
                periodType = PeriodType.HALF_TIME,
                captainId = 1L,
                archived = false
            )
            every { localDataSource.getMatchById(matchId) } returns flowOf(match)
            coEvery { localDataSource.updateMatch(any()) } returns Unit

            // When
            repository.archiveMatch(matchId)

            // Then
            coVerify {
                localDataSource.updateMatch(
                    match {
                        it.id == matchId && it.archived == true
                    }
                )
            }
        }

    @Test
    fun `unarchiveMatch should set archived to false`() =
        runTest {
            // Given
            val matchId = 1L
            val match = Match(
                id = matchId,
                teamName = "Team A",
                opponent = "Team B",
                location = "Stadium A",
                periodType = PeriodType.HALF_TIME,
                captainId = 1L,
                archived = true
            )
            every { localDataSource.getMatchById(matchId) } returns flowOf(match)
            coEvery { localDataSource.updateMatch(any()) } returns Unit

            // When
            repository.unarchiveMatch(matchId)

            // Then
            coVerify {
                localDataSource.updateMatch(
                    match {
                        it.id == matchId && it.archived == false
                    }
                )
            }
        }
}
