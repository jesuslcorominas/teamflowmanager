package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerTimeHistoryLocalDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeHistory
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

class PlayerTimeHistoryRepositoryImplTest {
    private lateinit var localDataSource: PlayerTimeHistoryLocalDataSource
    private lateinit var repository: PlayerTimeHistoryRepositoryImpl

    @Before
    fun setup() {
        localDataSource = mockk(relaxed = true)
        repository = PlayerTimeHistoryRepositoryImpl(localDataSource)
    }

    @Test
    fun `getPlayerTimeHistory should return player time history from local data source`() =
        runTest {
            // Given
            val playerId = 1L
            val history =
                listOf(
                    PlayerTimeHistory(
                        id = 1L,
                        playerId = playerId,
                        matchId = 1L,
                        elapsedTimeMillis = 5000L,
                        savedAtMillis = 1000L,
                    ),
                    PlayerTimeHistory(
                        id = 2L,
                        playerId = playerId,
                        matchId = 2L,
                        elapsedTimeMillis = 3000L,
                        savedAtMillis = 2000L,
                    ),
                )
            every { localDataSource.getPlayerTimeHistory(playerId) } returns flowOf(history)

            // When
            val result = repository.getPlayerTimeHistory(playerId).first()

            // Then
            assertEquals(history, result)
        }

    @Test
    fun `getMatchPlayerTimeHistory should return match player time history from local data source`() =
        runTest {
            // Given
            val matchId = 1L
            val history =
                listOf(
                    PlayerTimeHistory(
                        id = 1L,
                        playerId = 1L,
                        matchId = matchId,
                        elapsedTimeMillis = 5000L,
                        savedAtMillis = 1000L,
                    ),
                    PlayerTimeHistory(
                        id = 2L,
                        playerId = 2L,
                        matchId = matchId,
                        elapsedTimeMillis = 3000L,
                        savedAtMillis = 1000L,
                    ),
                )
            every { localDataSource.getMatchPlayerTimeHistory(matchId) } returns flowOf(history)

            // When
            val result = repository.getMatchPlayerTimeHistory(matchId).first()

            // Then
            assertEquals(history, result)
        }

    @Test
    fun `getAllPlayerTimeHistory should return all player time history from local data source`() =
        runTest {
            // Given
            val history =
                listOf(
                    PlayerTimeHistory(
                        id = 1L,
                        playerId = 1L,
                        matchId = 1L,
                        elapsedTimeMillis = 5000L,
                        savedAtMillis = 1000L,
                    ),
                    PlayerTimeHistory(
                        id = 2L,
                        playerId = 2L,
                        matchId = 1L,
                        elapsedTimeMillis = 3000L,
                        savedAtMillis = 1000L,
                    ),
                )
            every { localDataSource.getAllPlayerTimeHistory() } returns flowOf(history)

            // When
            val result = repository.getAllPlayerTimeHistory().first()

            // Then
            assertEquals(history, result)
        }

    @Test
    fun `insertPlayerTimeHistory should insert player time history to local data source`() =
        runTest {
            // Given
            val history =
                PlayerTimeHistory(
                    id = 0L,
                    playerId = 1L,
                    matchId = 1L,
                    elapsedTimeMillis = 5000L,
                    savedAtMillis = 1000L,
                )
            coEvery { localDataSource.insertPlayerTimeHistory(history) } returns 1L

            // When
            val result = repository.insertPlayerTimeHistory(history)

            // Then
            assertEquals(1L, result)
            coVerify { localDataSource.insertPlayerTimeHistory(history) }
        }
}
