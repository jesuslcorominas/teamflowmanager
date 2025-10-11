package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerTimeLocalDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTime
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

class PlayerTimeRepositoryImplTest {
    private lateinit var localDataSource: PlayerTimeLocalDataSource
    private lateinit var repository: PlayerTimeRepositoryImpl

    @Before
    fun setup() {
        localDataSource = mockk(relaxed = true)
        repository = PlayerTimeRepositoryImpl(localDataSource)
    }

    @Test
    fun `getPlayerTime should return player time from local data source`() =
        runTest {
            // Given
            val playerId = 1L
            val playerTime = PlayerTime(playerId = playerId, elapsedTimeMillis = 5000L, isRunning = true)
            every { localDataSource.getPlayerTime(playerId) } returns flowOf(playerTime)

            // When
            val result = repository.getPlayerTime(playerId).first()

            // Then
            assertEquals(playerTime, result)
        }

    @Test
    fun `getAllPlayerTimes should return all player times from local data source`() =
        runTest {
            // Given
            val playerTimes =
                listOf(
                    PlayerTime(playerId = 1L, elapsedTimeMillis = 5000L, isRunning = true),
                    PlayerTime(playerId = 2L, elapsedTimeMillis = 3000L, isRunning = false),
                )
            every { localDataSource.getAllPlayerTimes() } returns flowOf(playerTimes)

            // When
            val result = repository.getAllPlayerTimes().first()

            // Then
            assertEquals(playerTimes, result)
        }

    @Test
    fun `startTimer should create new player time when none exists`() =
        runTest {
            // Given
            val playerId = 1L
            val currentTime = 1000L
            every { localDataSource.getPlayerTime(playerId) } returns flowOf(null)
            coEvery { localDataSource.upsertPlayerTime(any()) } returns Unit

            // When
            repository.startTimer(playerId, currentTime)

            // Then
            coVerify {
                localDataSource.upsertPlayerTime(
                    match {
                        it.playerId == playerId &&
                            it.elapsedTimeMillis == 0L &&
                            it.isRunning &&
                            it.lastStartTimeMillis == currentTime
                    },
                )
            }
        }

    @Test
    fun `startTimer should update existing player time to running state`() =
        runTest {
            // Given
            val playerId = 1L
            val currentTime = 2000L
            val existingPlayerTime = PlayerTime(playerId = playerId, elapsedTimeMillis = 5000L, isRunning = false)
            every { localDataSource.getPlayerTime(playerId) } returns flowOf(existingPlayerTime)
            coEvery { localDataSource.upsertPlayerTime(any()) } returns Unit

            // When
            repository.startTimer(playerId, currentTime)

            // Then
            coVerify {
                localDataSource.upsertPlayerTime(
                    match {
                        it.playerId == playerId &&
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
            val playerId = 1L
            val startTime = 1000L
            val pauseTime = 3000L
            val existingPlayerTime =
                PlayerTime(
                    playerId = playerId,
                    elapsedTimeMillis = 2000L,
                    isRunning = true,
                    lastStartTimeMillis = startTime,
                )
            every { localDataSource.getPlayerTime(playerId) } returns flowOf(existingPlayerTime)
            coEvery { localDataSource.upsertPlayerTime(any()) } returns Unit

            // When
            repository.pauseTimer(playerId, pauseTime)

            // Then
            coVerify {
                localDataSource.upsertPlayerTime(
                    match {
                        it.playerId == playerId &&
                            it.elapsedTimeMillis == 4000L &&
                            !it.isRunning &&
                            it.lastStartTimeMillis == null
                    },
                )
            }
        }

    @Test
    fun `pauseTimer should do nothing when player time is not running`() =
        runTest {
            // Given
            val playerId = 1L
            val pauseTime = 3000L
            val existingPlayerTime = PlayerTime(playerId = playerId, elapsedTimeMillis = 2000L, isRunning = false)
            every { localDataSource.getPlayerTime(playerId) } returns flowOf(existingPlayerTime)

            // When
            repository.pauseTimer(playerId, pauseTime)

            // Then
            coVerify(exactly = 0) { localDataSource.upsertPlayerTime(any()) }
        }

    @Test
    fun `pauseTimer should do nothing when no player time exists`() =
        runTest {
            // Given
            val playerId = 1L
            val pauseTime = 3000L
            every { localDataSource.getPlayerTime(playerId) } returns flowOf(null)

            // When
            repository.pauseTimer(playerId, pauseTime)

            // Then
            coVerify(exactly = 0) { localDataSource.upsertPlayerTime(any()) }
        }

    @Test
    fun `startTimer should be able to restart timer after pause`() =
        runTest {
            // Given
            val playerId = 1L
            val firstStartTime = 1000L
            val pauseTime = 3000L
            val secondStartTime = 5000L

            // First start
            every { localDataSource.getPlayerTime(playerId) } returns flowOf(null)
            coEvery { localDataSource.upsertPlayerTime(any()) } returns Unit
            repository.startTimer(playerId, firstStartTime)

            // Pause
            val runningPlayerTime =
                PlayerTime(
                    playerId = playerId,
                    elapsedTimeMillis = 0L,
                    isRunning = true,
                    lastStartTimeMillis = firstStartTime,
                )
            every { localDataSource.getPlayerTime(playerId) } returns flowOf(runningPlayerTime)
            repository.pauseTimer(playerId, pauseTime)

            // Second start
            val pausedPlayerTime = PlayerTime(playerId = playerId, elapsedTimeMillis = 2000L, isRunning = false)
            every { localDataSource.getPlayerTime(playerId) } returns flowOf(pausedPlayerTime)

            // When
            repository.startTimer(playerId, secondStartTime)

            // Then
            coVerify {
                localDataSource.upsertPlayerTime(
                    match {
                        it.playerId == playerId &&
                            it.elapsedTimeMillis == 2000L &&
                            it.isRunning &&
                            it.lastStartTimeMillis == secondStartTime
                    },
                )
            }
        }

    @Test
    fun `resetAllPlayerTimes should delete all player times from local data source`() =
        runTest {
            // Given
            coEvery { localDataSource.deleteAllPlayerTimes() } returns Unit

            // When
            repository.resetAllPlayerTimes()

            // Then
            coVerify { localDataSource.deleteAllPlayerTimes() }
        }
}
