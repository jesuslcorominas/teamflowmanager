package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTime
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetPlayerTimeUseCaseTest {
    private lateinit var playerTimeRepository: PlayerTimeRepository
    private lateinit var getPlayerTimeUseCase: GetPlayerTimeUseCase

    @Before
    fun setup() {
        playerTimeRepository = mockk(relaxed = true)
        getPlayerTimeUseCase = GetPlayerTimeUseCaseImpl(playerTimeRepository)
    }

    @Test
    fun `invoke should return player time from repository`() =
        runTest {
            // Given
            val playerId = 1L
            val playerTime = PlayerTime(playerId = playerId, elapsedTimeMillis = 5000L, isRunning = true)
            every { playerTimeRepository.getPlayerTime(playerId) } returns flowOf(playerTime)

            // When
            val result = getPlayerTimeUseCase.invoke(playerId).first()

            // Then
            assertEquals(playerTime, result)
        }

    @Test
    fun `invoke should return null when no player time exists`() =
        runTest {
            // Given
            val playerId = 1L
            every { playerTimeRepository.getPlayerTime(playerId) } returns flowOf(null)

            // When
            val result = getPlayerTimeUseCase.invoke(playerId).first()

            // Then
            assertEquals(null, result)
        }
}
