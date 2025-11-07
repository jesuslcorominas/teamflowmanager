package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.PeriodType
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerSubstitution
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeHistory
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerSubstitutionRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeHistoryRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class GetMatchSummaryUseCaseTest {
    private lateinit var matchRepository: MatchRepository
    private lateinit var playerTimeHistoryRepository: PlayerTimeHistoryRepository
    private lateinit var playerSubstitutionRepository: PlayerSubstitutionRepository
    private lateinit var playerRepository: PlayerRepository
    private lateinit var getMatchSummaryUseCase: GetMatchSummaryUseCase

    @Before
    fun setup() {
        matchRepository = mockk()
        playerTimeHistoryRepository = mockk()
        playerSubstitutionRepository = mockk()
        playerRepository = mockk()
        getMatchSummaryUseCase = GetMatchSummaryUseCaseImpl(
            matchRepository = matchRepository,
            playerTimeHistoryRepository = playerTimeHistoryRepository,
            playerSubstitutionRepository = playerSubstitutionRepository,
            playerRepository = playerRepository,
        )
    }

    @Test
    fun `invoke should return null when match does not exist`() =
        runTest {
            // Given
            val matchId = 1L
            every { matchRepository.getMatchById(matchId) } returns flowOf(null)
            every { playerTimeHistoryRepository.getMatchPlayerTimeHistory(matchId) } returns flowOf(emptyList())
            every { playerSubstitutionRepository.getMatchSubstitutions(matchId) } returns flowOf(emptyList())
            every { playerRepository.getAllPlayers() } returns flowOf(emptyList())

            // When
            val result = getMatchSummaryUseCase(matchId).first()

            // Then
            assertNull(result)
        }

    @Test
    fun `invoke should return match summary with player times sorted by time descending`() =
        runTest {
            // Given
            val matchId = 1L
            val match = Match(
                id = matchId,
                opponent = "Team A",
                location = "Stadium",
                status = MatchStatus.FINISHED,
                elapsedTimeMillis = 3000000L,
                teamName = "Team B"
            )
            val player1 = Player(id = 1L, firstName = "John", lastName = "Doe", number = 10, positions = listOf(Position.Forward), teamId = 1L)
            val player2 = Player(id = 2L, firstName = "Jane", lastName = "Smith", number = 5, positions = listOf(Position.Defender), teamId = 1L)
            val playerTimes = listOf(
                PlayerTimeHistory(id = 1L, playerId = 1L, matchId = matchId, elapsedTimeMillis = 1500000L, savedAtMillis = 0L),
                PlayerTimeHistory(id = 2L, playerId = 2L, matchId = matchId, elapsedTimeMillis = 2000000L, savedAtMillis = 0L),
            )

            every { matchRepository.getMatchById(matchId) } returns flowOf(match)
            every { playerTimeHistoryRepository.getMatchPlayerTimeHistory(matchId) } returns flowOf(playerTimes)
            every { playerSubstitutionRepository.getMatchSubstitutions(matchId) } returns flowOf(emptyList())
            every { playerRepository.getAllPlayers() } returns flowOf(listOf(player1, player2))

            // When
            val result = getMatchSummaryUseCase(matchId).first()

            // Then
            assertEquals(matchId, result?.match?.id)
            assertEquals(2, result?.playerTimes?.size)
            // Sorted by elapsed time descending
            assertEquals(2L, result?.playerTimes?.get(0)?.player?.id)
            assertEquals(2000000L, result?.playerTimes?.get(0)?.elapsedTimeMillis)
            assertEquals(1L, result?.playerTimes?.get(1)?.player?.id)
            assertEquals(1500000L, result?.playerTimes?.get(1)?.elapsedTimeMillis)
        }

    @Test
    fun `invoke should return match summary with substitutions sorted by time ascending`() =
        runTest {
            // Given
            val matchId = 1L
            val match = Match(
                id = matchId,
                opponent = "Team A",
                location = "Stadium",
                status = MatchStatus.FINISHED,
                elapsedTimeMillis = 3000000L,
                teamName = "Team B"
            )
            val player1 = Player(id = 1L, firstName = "John", lastName = "Doe", number = 10, positions = listOf(Position.Forward), teamId = 1L)
            val player2 = Player(id = 2L, firstName = "Jane", lastName = "Smith", number = 5, positions = listOf(Position.Defender), teamId = 1L)
            val player3 = Player(id = 3L, firstName = "Bob", lastName = "Johnson", number = 7, positions = listOf(Position.Midfielder), teamId = 1L)
            val substitutions = listOf(
                PlayerSubstitution(id = 1L, matchId = matchId, playerOutId = 1L, playerInId = 2L, substitutionTimeMillis = 0L, matchElapsedTimeMillis = 1500000L),
                PlayerSubstitution(id = 2L, matchId = matchId, playerOutId = 2L, playerInId = 3L, substitutionTimeMillis = 0L, matchElapsedTimeMillis = 900000L),
            )

            every { matchRepository.getMatchById(matchId) } returns flowOf(match)
            every { playerTimeHistoryRepository.getMatchPlayerTimeHistory(matchId) } returns flowOf(emptyList())
            every { playerSubstitutionRepository.getMatchSubstitutions(matchId) } returns flowOf(substitutions)
            every { playerRepository.getAllPlayers() } returns flowOf(listOf(player1, player2, player3))

            // When
            val result = getMatchSummaryUseCase(matchId).first()

            // Then
            assertEquals(matchId, result?.match?.id)
            assertEquals(2, result?.substitutions?.size)
            // Sorted by match elapsed time ascending
            assertEquals(900000L, result?.substitutions?.get(0)?.matchElapsedTimeMillis)
            assertEquals(2L, result?.substitutions?.get(0)?.playerOut?.id)
            assertEquals(3L, result?.substitutions?.get(0)?.playerIn?.id)
            assertEquals(1500000L, result?.substitutions?.get(1)?.matchElapsedTimeMillis)
            assertEquals(1L, result?.substitutions?.get(1)?.playerOut?.id)
            assertEquals(2L, result?.substitutions?.get(1)?.playerIn?.id)
        }

    @Test
    fun `invoke should return match summary with empty lists when no player times or substitutions`() =
        runTest {
            // Given
            val matchId = 1L
            val match = Match(
                id = matchId,
                opponent = "Team A",
                location = "Stadium",
                status = MatchStatus.FINISHED,
                elapsedTimeMillis = 3000000L,
                teamName = "Team B"
            )

            every { matchRepository.getMatchById(matchId) } returns flowOf(match)
            every { playerTimeHistoryRepository.getMatchPlayerTimeHistory(matchId) } returns flowOf(emptyList())
            every { playerSubstitutionRepository.getMatchSubstitutions(matchId) } returns flowOf(emptyList())
            every { playerRepository.getAllPlayers() } returns flowOf(emptyList())

            // When
            val result = getMatchSummaryUseCase(matchId).first()

            // Then
            assertEquals(matchId, result?.match?.id)
            assertEquals(0, result?.playerTimes?.size)
            assertEquals(0, result?.substitutions?.size)
        }
}
