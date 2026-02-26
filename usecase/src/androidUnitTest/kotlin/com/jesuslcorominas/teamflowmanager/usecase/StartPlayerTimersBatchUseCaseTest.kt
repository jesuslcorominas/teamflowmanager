package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.StartPlayerTimersBatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class StartPlayerTimersBatchUseCaseTest {
    private lateinit var playerTimeRepository: PlayerTimeRepository
    private lateinit var useCase: StartPlayerTimersBatchUseCase

    @Before
    fun setup() {
        playerTimeRepository = mockk(relaxed = true)
        useCase = StartPlayerTimersBatchUseCaseImpl(playerTimeRepository)
    }

    @Test
    fun `invoke should call startTimersBatch on repository`() = runTest {
        val playerIds = listOf(1L, 2L, 3L)
        val currentTimeMillis = 7000L

        useCase.invoke(playerIds, currentTimeMillis)

        coVerify { playerTimeRepository.startTimersBatch(playerIds, currentTimeMillis) }
    }
}
