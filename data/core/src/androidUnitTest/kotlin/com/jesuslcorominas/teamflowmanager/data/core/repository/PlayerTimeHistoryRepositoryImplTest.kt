package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerTimeHistoryDataSource
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
    private lateinit var playerTimeHistoryDataSource: PlayerTimeHistoryDataSource
    private lateinit var repository: PlayerTimeHistoryRepositoryImpl

    @Before
    fun setup() {
        playerTimeHistoryDataSource = mockk(relaxed = true)
        repository = PlayerTimeHistoryRepositoryImpl(playerTimeHistoryDataSource)
    }

    @Test
    fun `getPlayerTimeHistory should return player time history from local data source`() =
        runTest {
            // Given
            val playerId = "1"
            val history =
                listOf(
                    PlayerTimeHistory(
                        id = "1",
                        playerId = playerId,
                        matchId = "1",
                        elapsedTimeMillis = 5000L,
                        savedAtMillis = 1000L,
                    ),
                    PlayerTimeHistory(
                        id = "2",
                        playerId = playerId,
                        matchId = "2",
                        elapsedTimeMillis = 3000L,
                        savedAtMillis = 2000L,
                    ),
                )
            every { playerTimeHistoryDataSource.getPlayerTimeHistory(playerId) } returns flowOf(history)

            // When
            val result = repository.getPlayerTimeHistory(playerId).first()

            // Then
            assertEquals(history, result)
        }

    @Test
    fun `getMatchPlayerTimeHistory should return match player time history from local data source`() =
        runTest {
            // Given
            val matchId = "1"
            val history =
                listOf(
                    PlayerTimeHistory(
                        id = "1",
                        playerId = "1",
                        matchId = matchId,
                        elapsedTimeMillis = 5000L,
                        savedAtMillis = 1000L,
                    ),
                    PlayerTimeHistory(
                        id = "2",
                        playerId = "2",
                        matchId = matchId,
                        elapsedTimeMillis = 3000L,
                        savedAtMillis = 1000L,
                    ),
                )
            every { playerTimeHistoryDataSource.getMatchPlayerTimeHistory(matchId) } returns flowOf(history)

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
                        id = "1",
                        playerId = "1",
                        matchId = "1",
                        elapsedTimeMillis = 5000L,
                        savedAtMillis = 1000L,
                    ),
                    PlayerTimeHistory(
                        id = "2",
                        playerId = "2",
                        matchId = "1",
                        elapsedTimeMillis = 3000L,
                        savedAtMillis = 1000L,
                    ),
                )
            every { playerTimeHistoryDataSource.getAllPlayerTimeHistory() } returns flowOf(history)

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
                    id = "",
                    playerId = "1",
                    matchId = "1",
                    elapsedTimeMillis = 5000L,
                    savedAtMillis = 1000L,
                )
            coEvery { playerTimeHistoryDataSource.insertPlayerTimeHistory(history) } returns "history-1"

            // When
            val result = repository.insertPlayerTimeHistory(history)

            // Then
            assertEquals("history-1", result)
            coVerify { playerTimeHistoryDataSource.insertPlayerTimeHistory(history) }
        }
}
