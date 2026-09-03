package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.domain.model.PeriodType
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerSubstitution
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeHistory
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetMatchSummaryUseCase
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
            val matchId = "1"
            every { matchRepository.getMatchById(matchId) } returns flowOf(null)
            every { playerTimeHistoryRepository.getMatchPlayerTimeHistory(matchId) } returns flowOf(emptyList())
            every { playerSubstitutionRepository.getMatchSubstitutions(matchId) } returns flowOf(emptyList())
            every { playerRepository.getPlayersByTeam(any()) } returns flowOf(emptyList())

            // When
            val result = getMatchSummaryUseCase(matchId).first()

            // Then
            assertNull(result)
        }

    @Test
    fun `invoke should return match summary with player times sorted by time descending`() =
        runTest {
            // Given
            val matchId = "1"
            val match = Match(
                id = matchId,
                opponent = "Team A",
                location = "Stadium",
                status = MatchStatus.FINISHED,
                teamName = "Team B",
                periodType = PeriodType.HALF_TIME,
                captainId = "1",
            )
            val player1 = Player(id = "1", firstName = "John", lastName = "Doe", number = 10, positions = listOf(Position.Forward), teamId = "1", isCaptain = false)
            val player2 = Player(id = "2", firstName = "Jane", lastName = "Smith", number = 5, positions = listOf(Position.Defender), teamId = "1", isCaptain = false)
            val playerTimes = listOf(
                PlayerTimeHistory(id = "1", playerId = "1", matchId = matchId, elapsedTimeMillis = 1500000L, savedAtMillis = 0L),
                PlayerTimeHistory(id = "2", playerId = "2", matchId = matchId, elapsedTimeMillis = 2000000L, savedAtMillis = 0L),
            )

            every { matchRepository.getMatchById(matchId) } returns flowOf(match)
            every { playerTimeHistoryRepository.getMatchPlayerTimeHistory(matchId) } returns flowOf(playerTimes)
            every { playerSubstitutionRepository.getMatchSubstitutions(matchId) } returns flowOf(emptyList())
            every { playerRepository.getPlayersByTeam(any()) } returns flowOf(listOf(player1, player2))

            // When
            val result = getMatchSummaryUseCase(matchId).first()

            // Then
            assertEquals(matchId, result?.match?.id)
            assertEquals(2, result?.playerTimes?.size)
            // Sorted by elapsed time descending
            assertEquals("2", result?.playerTimes?.get(0)?.player?.id)
            assertEquals(2000000L, result?.playerTimes?.get(0)?.elapsedTimeMillis)
            assertEquals("1", result?.playerTimes?.get(1)?.player?.id)
            assertEquals(1500000L, result?.playerTimes?.get(1)?.elapsedTimeMillis)
        }

    @Test
    fun `invoke should return match summary with substitutions sorted by time ascending`() =
        runTest {
            // Given
            val matchId = "1"
            val match = Match(
                id = matchId,
                opponent = "Team A",
                location = "Stadium",
                status = MatchStatus.FINISHED,
                teamName = "Team B",
                periodType = PeriodType.HALF_TIME,
                captainId = "1",
            )
            val player1 = Player(id = "1", firstName = "John", lastName = "Doe", number = 10, positions = listOf(Position.Forward), teamId = "1", isCaptain = false)
            val player2 = Player(id = "2", firstName = "Jane", lastName = "Smith", number = 5, positions = listOf(Position.Defender), teamId = "1", isCaptain = false)
            val player3 = Player(id = "3", firstName = "Bob", lastName = "Johnson", number = 7, positions = listOf(Position.Midfielder), teamId = "1", isCaptain = false)
            val substitutions = listOf(
                PlayerSubstitution(id = "1", matchId = matchId, playerOutId = "1", playerInId = "2", substitutionTimeMillis = 0L, matchElapsedTimeMillis = 1500000L),
                PlayerSubstitution(id = "2", matchId = matchId, playerOutId = "2", playerInId = "3", substitutionTimeMillis = 0L, matchElapsedTimeMillis = 900000L),
            )

            every { matchRepository.getMatchById(matchId) } returns flowOf(match)
            every { playerTimeHistoryRepository.getMatchPlayerTimeHistory(matchId) } returns flowOf(emptyList())
            every { playerSubstitutionRepository.getMatchSubstitutions(matchId) } returns flowOf(substitutions)
            every { playerRepository.getPlayersByTeam(any()) } returns flowOf(listOf(player1, player2, player3))

            // When
            val result = getMatchSummaryUseCase(matchId).first()

            // Then
            assertEquals(matchId, result?.match?.id)
            assertEquals(2, result?.substitutions?.size)
            // Sorted by match elapsed time ascending
            assertEquals(900000L, result?.substitutions?.get(0)?.matchElapsedTimeMillis)
            assertEquals("2", result?.substitutions?.get(0)?.playerOut?.id)
            assertEquals("3", result?.substitutions?.get(0)?.playerIn?.id)
            assertEquals(1500000L, result?.substitutions?.get(1)?.matchElapsedTimeMillis)
            assertEquals("1", result?.substitutions?.get(1)?.playerOut?.id)
            assertEquals("2", result?.substitutions?.get(1)?.playerIn?.id)
        }

    @Test
    fun `givenPlayerTimeReferencingUnknownPlayer_whenInvoke_thenSkipsEntry`() = runTest {
        // Given — a playerTime referencing a player that is no longer in the team
        val matchId = "1"
        val match = Match(
            id = matchId, opponent = "Team A", location = "Stadium",
            status = MatchStatus.FINISHED, teamName = "Team B",
            periodType = PeriodType.HALF_TIME, captainId = "1",
        )
        val playerTimes = listOf(
            PlayerTimeHistory(playerId = "99", matchId = matchId, elapsedTimeMillis = 1000L, savedAtMillis = 0L),
        )

        every { matchRepository.getMatchById(matchId) } returns flowOf(match)
        every { playerTimeHistoryRepository.getMatchPlayerTimeHistory(matchId) } returns flowOf(playerTimes)
        every { playerSubstitutionRepository.getMatchSubstitutions(matchId) } returns flowOf(emptyList())
        every { playerRepository.getPlayersByTeam(any()) } returns flowOf(emptyList()) // player 99 not found

        // When — should not throw; entry is skipped
        val result = getMatchSummaryUseCase(matchId).first()
        assertEquals(0, result?.playerTimes?.size)
    }

    @Test
    fun `givenSubstitutionReferencingUnknownPlayerOut_whenInvoke_thenSkipsEntry`() = runTest {
        // Given — a substitution whose playerOut is no longer in the team
        val matchId = "1"
        val player1 = Player(id = "1", firstName = "John", lastName = "Doe", number = 10, positions = listOf(Position.Forward), teamId = "1", isCaptain = false)
        val match = Match(
            id = matchId, opponent = "Team A", location = "Stadium",
            status = MatchStatus.FINISHED, teamName = "Team B",
            periodType = PeriodType.HALF_TIME, captainId = "1",
        )
        val substitutions = listOf(
            PlayerSubstitution(id = "1", matchId = matchId, playerOutId = "99", playerInId = "1", substitutionTimeMillis = 0L, matchElapsedTimeMillis = 1000L),
        )

        every { matchRepository.getMatchById(matchId) } returns flowOf(match)
        every { playerTimeHistoryRepository.getMatchPlayerTimeHistory(matchId) } returns flowOf(emptyList())
        every { playerSubstitutionRepository.getMatchSubstitutions(matchId) } returns flowOf(substitutions)
        every { playerRepository.getPlayersByTeam(any()) } returns flowOf(listOf(player1)) // player 99 absent

        // When — should not throw; entry is skipped
        val result = getMatchSummaryUseCase(matchId).first()
        assertEquals(0, result?.substitutions?.size)
    }

    @Test
    fun `givenSubstitutionReferencingUnknownPlayerIn_whenInvoke_thenSkipsEntry`() = runTest {
        // Given — a substitution whose playerIn is no longer in the team
        val matchId = "1"
        val player1 = Player(id = "1", firstName = "John", lastName = "Doe", number = 10, positions = listOf(Position.Forward), teamId = "1", isCaptain = false)
        val match = Match(
            id = matchId, opponent = "Team A", location = "Stadium",
            status = MatchStatus.FINISHED, teamName = "Team B",
            periodType = PeriodType.HALF_TIME, captainId = "1",
        )
        val substitutions = listOf(
            PlayerSubstitution(id = "1", matchId = matchId, playerOutId = "1", playerInId = "99", substitutionTimeMillis = 0L, matchElapsedTimeMillis = 1000L),
        )

        every { matchRepository.getMatchById(matchId) } returns flowOf(match)
        every { playerTimeHistoryRepository.getMatchPlayerTimeHistory(matchId) } returns flowOf(emptyList())
        every { playerSubstitutionRepository.getMatchSubstitutions(matchId) } returns flowOf(substitutions)
        every { playerRepository.getPlayersByTeam(any()) } returns flowOf(listOf(player1)) // player 99 absent

        // When — should not throw; entry is skipped
        val result = getMatchSummaryUseCase(matchId).first()
        assertEquals(0, result?.substitutions?.size)
    }

    @Test
    fun `invoke should return match summary with empty lists when no player times or substitutions`() =
        runTest {
            // Given
            val matchId = "1"
            val match = Match(
                id = matchId,
                opponent = "Team A",
                location = "Stadium",
                status = MatchStatus.FINISHED,
                teamName = "Team B",
                periodType = PeriodType.HALF_TIME,
                captainId = "1",
            )

            every { matchRepository.getMatchById(matchId) } returns flowOf(match)
            every { playerTimeHistoryRepository.getMatchPlayerTimeHistory(matchId) } returns flowOf(emptyList())
            every { playerSubstitutionRepository.getMatchSubstitutions(matchId) } returns flowOf(emptyList())
            every { playerRepository.getPlayersByTeam(any()) } returns flowOf(emptyList())

            // When
            val result = getMatchSummaryUseCase(matchId).first()

            // Then
            assertEquals(matchId, result?.match?.id)
            assertEquals(0, result?.playerTimes?.size)
            assertEquals(0, result?.substitutions?.size)
        }
}
