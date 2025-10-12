package com.jesuslcorominas.teamflowmanager.usecase.match

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.domain.repository.MatchRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Date

class PauseMatchUseCaseTest {

    private lateinit var pauseMatchUseCase: PauseMatchUseCase
    private lateinit var mockRepository: MockMatchRepository

    @Before
    fun setup() {
        mockRepository = MockMatchRepository()
        pauseMatchUseCase = PauseMatchUseCase(mockRepository)
    }

    @Test
    fun `should pause match successfully when match is in progress`() = runBlocking {
        // Given
        val matchId = "match1"
        val match = Match(
            id = matchId,
            teamId = "team1",
            opponent = "Team B",
            startTime = Date(),
            status = MatchStatus.IN_PROGRESS
        )
        mockRepository.addMatch(match)

        // When
        val result = pauseMatchUseCase(matchId)

        // Then
        assertTrue(result.isSuccess)
        val pausedMatch = result.getOrNull()
        assertNotNull(pausedMatch)
        assertEquals(MatchStatus.PAUSED, pausedMatch?.status)
    }

    @Test
    fun `should fail to pause match when match is not in progress`() = runBlocking {
        // Given
        val matchId = "match1"
        val match = Match(
            id = matchId,
            teamId = "team1",
            opponent = "Team B",
            startTime = null,
            status = MatchStatus.NOT_STARTED
        )
        mockRepository.addMatch(match)

        // When
        val result = pauseMatchUseCase(matchId)

        // Then
        assertTrue(result.isFailure)
    }

    @Test
    fun `should fail to pause match when match does not exist`() = runBlocking {
        // Given
        val matchId = "nonexistent"

        // When
        val result = pauseMatchUseCase(matchId)

        // Then
        assertTrue(result.isFailure)
    }

    private class MockMatchRepository : MatchRepository {
        private val matches = mutableMapOf<String, Match>()

        fun addMatch(match: Match) {
            matches[match.id] = match
        }

        override suspend fun getMatchById(matchId: String): Match? {
            return matches[matchId]
        }

        override suspend fun updateMatch(match: Match): Result<Unit> {
            matches[match.id] = match
            return Result.success(Unit)
        }

        override suspend fun pauseMatch(matchId: String): Result<Match> {
            val match = matches[matchId] ?: return Result.failure(Exception("Match not found"))
            
            if (match.status != MatchStatus.IN_PROGRESS) {
                return Result.failure(Exception("Match is not in progress"))
            }

            val pausedMatch = match.copy(status = MatchStatus.PAUSED)
            matches[matchId] = pausedMatch
            return Result.success(pausedMatch)
        }

        override suspend fun resumeMatch(matchId: String): Result<Match> {
            val match = matches[matchId] ?: return Result.failure(Exception("Match not found"))
            
            if (match.status != MatchStatus.PAUSED) {
                return Result.failure(Exception("Match is not paused"))
            }

            val resumedMatch = match.copy(status = MatchStatus.IN_PROGRESS)
            matches[matchId] = resumedMatch
            return Result.success(resumedMatch)
        }
    }
}
