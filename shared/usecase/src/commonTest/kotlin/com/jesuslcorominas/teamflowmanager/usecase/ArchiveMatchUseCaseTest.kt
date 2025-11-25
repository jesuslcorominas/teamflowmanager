package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ArchiveMatchUseCaseTest {
    private lateinit var matchRepository: MatchRepository
    private lateinit var archiveMatchUseCase: ArchiveMatchUseCase

    @Before
    fun setup() {
        matchRepository = mockk(relaxed = true)
        archiveMatchUseCase = ArchiveMatchUseCaseImpl(matchRepository)
    }

    @Test
    fun `invoke should call archiveMatch on repository`() =
        runTest {
            // Given
            val matchId = 1L

            // When
            archiveMatchUseCase(matchId)

            // Then
            coVerify(exactly = 1) { matchRepository.archiveMatch(matchId) }
        }
}
