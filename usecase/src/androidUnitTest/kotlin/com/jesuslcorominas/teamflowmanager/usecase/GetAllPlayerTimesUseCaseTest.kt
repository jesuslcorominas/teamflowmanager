package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTime
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetAllPlayerTimesUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetAllPlayerTimesUseCaseTest {
    private lateinit var playerTimeRepository: PlayerTimeRepository
    private lateinit var getAllPlayerTimesUseCase: GetAllPlayerTimesUseCase

    @Before
    fun setup() {
        playerTimeRepository = mockk(relaxed = true)
        getAllPlayerTimesUseCase = GetAllPlayerTimesUseCaseImpl(playerTimeRepository)
    }

    @Test
    fun `invoke should return all player times from repository`() =
        runTest {
            // Given
            val playerTimes =
                listOf(
                    PlayerTime(playerId = 1L, elapsedTimeMillis = 5000L, isRunning = true),
                    PlayerTime(playerId = 2L, elapsedTimeMillis = 3000L, isRunning = false),
                )
            every { playerTimeRepository.getPlayerTimesByMatch(any()) } returns flowOf(playerTimes)

            // When
            val result = getAllPlayerTimesUseCase.invoke(matchId = 1L).first()

            // Then
            assertEquals(playerTimes, result)
        }

    @Test
    fun `invoke should return empty list when no player times exist`() =
        runTest {
            // Given
            every { playerTimeRepository.getPlayerTimesByMatch(any()) } returns flowOf(emptyList())

            // When
            val result = getAllPlayerTimesUseCase.invoke(matchId = 1L).first()

            // Then
            assertEquals(emptyList<PlayerTime>(), result)
        }
}
