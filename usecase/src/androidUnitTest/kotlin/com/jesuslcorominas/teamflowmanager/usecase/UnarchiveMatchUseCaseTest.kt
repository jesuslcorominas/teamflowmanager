package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.usecase.UnarchiveMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class UnarchiveMatchUseCaseTest {
    private lateinit var matchRepository: MatchRepository
    private lateinit var unarchiveMatchUseCase: UnarchiveMatchUseCase

    @Before
    fun setup() {
        matchRepository = mockk(relaxed = true)
        unarchiveMatchUseCase = UnarchiveMatchUseCaseImpl(matchRepository)
    }

    @Test
    fun `invoke should call unarchiveMatch on repository`() =
        runTest {
            // Given
            val matchId = 1L

            // When
            unarchiveMatchUseCase(matchId)

            // Then
            coVerify(exactly = 1) { matchRepository.unarchiveMatch(matchId) }
        }
}
