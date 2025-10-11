package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.MatchLocalDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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
    fun `getMatch should return match from local data source`() =
        runTest {
            // Given
            val match = Match(id = 1L, elapsedTimeMillis = 5000L, isRunning = true)
            every { localDataSource.getMatch() } returns flowOf(match)

            // When
            val result = repository.getMatch().first()

            // Then
            assertEquals(match, result)
        }

    @Test
    fun `startTimer should create new match when none exists`() =
        runTest {
            // Given
            val currentTime = 1000L
            every { localDataSource.getMatch() } returns flowOf(null)
            coEvery { localDataSource.upsertMatch(any()) } returns Unit

            // When
            repository.startTimer(currentTime)

            // Then
            coVerify {
                localDataSource.upsertMatch(
                    match {
                        it.id == 1L &&
                            it.elapsedTimeMillis == 0L &&
                            it.isRunning &&
                            it.lastStartTimeMillis == currentTime
                    },
                )
            }
        }

    @Test
    fun `startTimer should update existing match to running state`() =
        runTest {
            // Given
            val currentTime = 2000L
            val existingMatch = Match(id = 1L, elapsedTimeMillis = 5000L, isRunning = false)
            every { localDataSource.getMatch() } returns flowOf(existingMatch)
            coEvery { localDataSource.upsertMatch(any()) } returns Unit

            // When
            repository.startTimer(currentTime)

            // Then
            coVerify {
                localDataSource.upsertMatch(
                    match {
                        it.id == 1L &&
                            it.elapsedTimeMillis == 5000L &&
                            it.isRunning &&
                            it.lastStartTimeMillis == currentTime
                    },
                )
            }
        }

    @Test
    fun `pauseTimer should update elapsed time and set running to false`() =
        runTest {
            // Given
            val startTime = 1000L
            val pauseTime = 3000L
            val existingMatch = Match(id = 1L, elapsedTimeMillis = 2000L, isRunning = true, lastStartTimeMillis = startTime)
            every { localDataSource.getMatch() } returns flowOf(existingMatch)
            coEvery { localDataSource.upsertMatch(any()) } returns Unit

            // When
            repository.pauseTimer(pauseTime)

            // Then
            coVerify {
                localDataSource.upsertMatch(
                    match {
                        it.id == 1L &&
                            it.elapsedTimeMillis == 4000L &&
                            !it.isRunning &&
                            it.lastStartTimeMillis == null
                    },
                )
            }
        }

    @Test
    fun `pauseTimer should do nothing when match is not running`() =
        runTest {
            // Given
            val pauseTime = 3000L
            val existingMatch = Match(id = 1L, elapsedTimeMillis = 2000L, isRunning = false)
            every { localDataSource.getMatch() } returns flowOf(existingMatch)

            // When
            repository.pauseTimer(pauseTime)

            // Then
            coVerify(exactly = 0) { localDataSource.upsertMatch(any()) }
        }

    @Test
    fun `pauseTimer should do nothing when no match exists`() =
        runTest {
            // Given
            val pauseTime = 3000L
            every { localDataSource.getMatch() } returns flowOf(null)

            // When
            repository.pauseTimer(pauseTime)

            // Then
            coVerify(exactly = 0) { localDataSource.upsertMatch(any()) }
        }
}
