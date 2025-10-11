package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CreateMatchUseCaseTest {
    private lateinit var matchRepository: MatchRepository
    private lateinit var createMatchUseCase: CreateMatchUseCase

    @Before
    fun setup() {
        matchRepository = mockk(relaxed = true)
        createMatchUseCase = CreateMatchUseCaseImpl(matchRepository)
    }

    @Test
    fun `invoke should create match in repository and return id`() =
        runTest {
            // Given
            val match =
                Match(
                    id = 0L,
                    teamId = 1L,
                    opponent = "Rival FC",
                    location = "Stadium",
                    date = System.currentTimeMillis(),
                    startingLineupIds = listOf(1L, 2L, 3L),
                    substituteIds = listOf(4L, 5L),
                )
            val expectedId = 123L
            coEvery { matchRepository.createMatch(match) } returns expectedId

            // When
            val result = createMatchUseCase.invoke(match)

            // Then
            assertEquals(expectedId, result)
            coVerify { matchRepository.createMatch(match) }
        }
}
