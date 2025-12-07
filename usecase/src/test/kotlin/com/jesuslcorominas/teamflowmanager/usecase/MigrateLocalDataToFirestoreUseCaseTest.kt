package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.TeamRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MigrateLocalDataToFirestoreUseCaseTest {

    private lateinit var teamRepository: TeamRepository
    private lateinit var playerRepository: PlayerRepository
    private lateinit var matchRepository: MatchRepository
    private lateinit var useCase: MigrateLocalDataToFirestoreUseCase

    @Before
    fun setup() {
        teamRepository = mockk(relaxed = true)
        playerRepository = mockk(relaxed = true)
        matchRepository = mockk(relaxed = true)
        useCase = MigrateLocalDataToFirestoreUseCaseImpl(
            teamRepository,
            playerRepository,
            matchRepository
        )
    }

    @Test
    fun `when local team exists, migration should succeed`() = runTest {
        // Given
        val userId = "test-user-123"
        val localTeam = Team(
            id = 1L,
            name = "Test Team",
            category = "Benjamín",
            coachId = null // No coachId means local data
        )
        val localPlayers = listOf(
            Player(id = 1L, name = "Player 1", number = 1, position = null),
            Player(id = 2L, name = "Player 2", number = 2, position = null)
        )
        val localMatches = listOf<Match>()

        coEvery { teamRepository.getLocalTeamDirect() } returns localTeam
        coEvery { playerRepository.getAllLocalPlayersDirect() } returns localPlayers
        coEvery { matchRepository.getAllLocalMatchesDirect() } returns localMatches

        // When
        val result = useCase(userId)

        // Then
        assertTrue(result.isSuccess)
        
        // Verify team was created with userId
        coVerify {
            teamRepository.createTeam(
                match {
                    it.name == localTeam.name &&
                    it.category == localTeam.category &&
                    it.coachId == userId
                }
            )
        }

        // Verify players were migrated
        coVerify(exactly = localPlayers.size) { playerRepository.addPlayer(any()) }

        // Verify local data was cleared
        coVerify { teamRepository.clearLocalTeamData() }
        coVerify { playerRepository.clearLocalPlayerData() }
        coVerify { matchRepository.clearLocalMatchData() }
    }

    @Test
    fun `when no local team exists, migration should fail`() = runTest {
        // Given
        val userId = "test-user-123"
        coEvery { teamRepository.getLocalTeamDirect() } returns null

        // When
        val result = useCase(userId)

        // Then
        assertTrue(result.isFailure)
        
        // Verify no data was migrated
        coVerify(exactly = 0) { teamRepository.createTeam(any()) }
        coVerify(exactly = 0) { playerRepository.addPlayer(any()) }
        coVerify(exactly = 0) { teamRepository.clearLocalTeamData() }
    }

    @Test
    fun `when team creation fails, migration should fail and not clear data`() = runTest {
        // Given
        val userId = "test-user-123"
        val localTeam = Team(
            id = 1L,
            name = "Test Team",
            category = "Benjamín",
            coachId = null
        )

        coEvery { teamRepository.getLocalTeamDirect() } returns localTeam
        coEvery { teamRepository.createTeam(any()) } throws Exception("Firebase error")

        // When
        val result = useCase(userId)

        // Then
        assertTrue(result.isFailure)
        
        // Verify local data was not cleared since migration failed
        coVerify(exactly = 0) { teamRepository.clearLocalTeamData() }
        coVerify(exactly = 0) { playerRepository.clearLocalPlayerData() }
        coVerify(exactly = 0) { matchRepository.clearLocalMatchData() }
    }

    @Test
    fun `when players and matches exist, all should be migrated`() = runTest {
        // Given
        val userId = "test-user-123"
        val localTeam = Team(
            id = 1L,
            name = "Test Team",
            category = "Benjamín",
            coachId = null
        )
        val localPlayers = listOf(
            Player(id = 1L, name = "Player 1", number = 1, position = null),
            Player(id = 2L, name = "Player 2", number = 2, position = null),
            Player(id = 3L, name = "Player 3", number = 3, position = null)
        )
        val localMatches = listOf(
            mockk<Match>(relaxed = true),
            mockk<Match>(relaxed = true)
        )

        coEvery { teamRepository.getLocalTeamDirect() } returns localTeam
        coEvery { playerRepository.getAllLocalPlayersDirect() } returns localPlayers
        coEvery { matchRepository.getAllLocalMatchesDirect() } returns localMatches

        // When
        val result = useCase(userId)

        // Then
        assertTrue(result.isSuccess)
        
        // Verify all players were migrated
        coVerify(exactly = 3) { playerRepository.addPlayer(any()) }
        
        // Verify all matches were migrated
        coVerify(exactly = 2) { matchRepository.createMatch(any()) }

        // Verify all local data was cleared
        coVerify { teamRepository.clearLocalTeamData() }
        coVerify { playerRepository.clearLocalPlayerData() }
        coVerify { matchRepository.clearLocalMatchData() }
    }
}
