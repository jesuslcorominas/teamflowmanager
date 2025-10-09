package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerLocalDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PlayerRepositoryImplTest {

    private lateinit var localDataSource: PlayerLocalDataSource
    private lateinit var repository: PlayerRepositoryImpl

    @Before
    fun setup() {
        localDataSource = mockk()
        repository = PlayerRepositoryImpl(localDataSource)
    }

    @Test
    fun `getAllPlayers should return players from local data source`() = runTest {
        // Given
        val players = listOf(
            Player(1, "John", "Doe", listOf("Forward")),
            Player(2, "Jane", "Smith", listOf("Midfielder"))
        )
        every { localDataSource.getAllPlayers() } returns flowOf(players)

        // When
        val result = repository.getAllPlayers().first()

        // Then
        assertEquals(players, result)
        verify { localDataSource.getAllPlayers() }
    }

    @Test
    fun `getAllPlayers should return empty list when no players exist`() = runTest {
        // Given
        every { localDataSource.getAllPlayers() } returns flowOf(emptyList())

        // When
        val result = repository.getAllPlayers().first()

        // Then
        assertEquals(emptyList<Player>(), result)
        verify { localDataSource.getAllPlayers() }
    }
}
