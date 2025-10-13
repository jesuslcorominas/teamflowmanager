package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.PlayerSubstitution
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerSubstitutionRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetMatchSubstitutionsUseCaseTest {
    private lateinit var playerSubstitutionRepository: PlayerSubstitutionRepository
    private lateinit var getMatchSubstitutionsUseCase: GetMatchSubstitutionsUseCase

    @Before
    fun setup() {
        playerSubstitutionRepository = mockk(relaxed = true)
        getMatchSubstitutionsUseCase = GetMatchSubstitutionsUseCaseImpl(playerSubstitutionRepository)
    }

    @Test
    fun `invoke should return substitutions for given match id`() =
        runTest {
            // Given
            val matchId = 1L
            val substitutions =
                listOf(
                    PlayerSubstitution(
                        id = 1L,
                        matchId = matchId,
                        playerOutId = 2L,
                        playerInId = 3L,
                        substitutionTimeMillis = System.currentTimeMillis(),
                        matchElapsedTimeMillis = 300000L,
                    ),
                    PlayerSubstitution(
                        id = 2L,
                        matchId = matchId,
                        playerOutId = 4L,
                        playerInId = 5L,
                        substitutionTimeMillis = System.currentTimeMillis(),
                        matchElapsedTimeMillis = 600000L,
                    ),
                )
            every { playerSubstitutionRepository.getMatchSubstitutions(matchId) } returns flowOf(substitutions)

            // When
            val result = getMatchSubstitutionsUseCase(matchId).first()

            // Then
            assertEquals(substitutions, result)
            verify { playerSubstitutionRepository.getMatchSubstitutions(matchId) }
        }

    @Test
    fun `invoke should return empty list when no substitutions exist`() =
        runTest {
            // Given
            val matchId = 1L
            every { playerSubstitutionRepository.getMatchSubstitutions(matchId) } returns flowOf(emptyList())

            // When
            val result = getMatchSubstitutionsUseCase(matchId).first()

            // Then
            assertEquals(emptyList<PlayerSubstitution>(), result)
            verify { playerSubstitutionRepository.getMatchSubstitutions(matchId) }
        }
}
