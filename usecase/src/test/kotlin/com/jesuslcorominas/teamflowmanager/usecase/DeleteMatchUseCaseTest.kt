package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DeleteMatchUseCaseTest {
    private lateinit var matchRepository: MatchRepository
    private lateinit var deleteMatchUseCase: DeleteMatchUseCase

    @Before
    fun setup() {
        matchRepository = mockk(relaxed = true)
        deleteMatchUseCase = DeleteMatchUseCaseImpl(matchRepository)
    }

    @Test
    fun `invoke should delete match from repository`() =
        runTest {
            // Given
            val matchId = 1L

            // When
            deleteMatchUseCase.invoke(matchId)

            // Then
            coVerify { matchRepository.deleteMatch(matchId) }
        }
}
