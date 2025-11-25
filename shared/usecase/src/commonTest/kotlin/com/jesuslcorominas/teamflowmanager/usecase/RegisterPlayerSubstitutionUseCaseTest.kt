package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerSubstitution
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTime
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeStatus
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerSubstitutionRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RegisterPlayerSubstitutionUseCaseTest {
    private lateinit var matchRepository: MatchRepository
    private lateinit var playerTimeRepository: PlayerTimeRepository
    private lateinit var playerSubstitutionRepository: PlayerSubstitutionRepository
    private lateinit var getAllPlayerTimesUseCase: GetAllPlayerTimesUseCase
    private lateinit var registerPlayerSubstitutionUseCase: RegisterPlayerSubstitutionUseCase

    @Before
    fun setup() {
        matchRepository = mockk(relaxed = true)
        playerTimeRepository = mockk(relaxed = true)
        playerSubstitutionRepository = mockk(relaxed = true)
        getAllPlayerTimesUseCase = mockk(relaxed = true)
        registerPlayerSubstitutionUseCase =
            RegisterPlayerSubstitutionUseCaseImpl(
                matchRepository,
                playerTimeRepository,
                playerSubstitutionRepository,
                getAllPlayerTimesUseCase,
            )
    }

    @Test
    fun `invoke should stop player out timer, start player in timer, and record substitution`() =
        runTest {
            // Given
            val matchId = 1L
            val playerOutId = 2L
            val playerInId = 3L
            val currentTimeMillis = System.currentTimeMillis()
            val match =
                Match(
                    id = matchId,
                    teamId = 1L,
                    elapsedTimeMillis = 900000L,
                    isRunning = true,
                    lastStartTimeMillis = currentTimeMillis - 60000L,
                    teamName = "Team B"
                )
            coEvery { matchRepository.getMatch() } returns flowOf(match)

            // Mock player times - playerOut is running
            val playerTimes = listOf(
                PlayerTime(playerId = playerOutId, elapsedTimeMillis = 300000L, isRunning = true, lastStartTimeMillis = currentTimeMillis - 60000L, status = PlayerTimeStatus.PLAYING),
                PlayerTime(playerId = playerInId, elapsedTimeMillis = 0L, isRunning = false, lastStartTimeMillis = null),
            )
            coEvery { getAllPlayerTimesUseCase() } returns flowOf(playerTimes)

            val substitutionSlot = slot<PlayerSubstitution>()
            coEvery { playerSubstitutionRepository.insertSubstitution(capture(substitutionSlot)) } returns 1L

            // When
            registerPlayerSubstitutionUseCase(matchId, playerOutId, playerInId, currentTimeMillis)

            // Then
            coVerify { playerTimeRepository.pauseTimer(playerOutId, currentTimeMillis) }
            coVerify { playerTimeRepository.startTimer(playerInId, currentTimeMillis) }
            coVerify { playerSubstitutionRepository.insertSubstitution(any()) }

            val substitution = substitutionSlot.captured
            assertEquals(matchId, substitution.matchId)
            assertEquals(playerOutId, substitution.playerOutId)
            assertEquals(playerInId, substitution.playerInId)
            assertEquals(currentTimeMillis, substitution.substitutionTimeMillis)
            assertEquals(960000L, substitution.matchElapsedTimeMillis)
        }

    @Test
    fun `invoke should calculate correct match elapsed time when match is running`() =
        runTest {
            // Given
            val matchId = 1L
            val playerOutId = 2L
            val playerInId = 3L
            val currentTimeMillis = System.currentTimeMillis()
            val lastStartTimeMillis = currentTimeMillis - 120000L
            val match =
                Match(
                    id = matchId,
                    teamId = 1L,
                    elapsedTimeMillis = 300000L,
                    isRunning = true,
                    lastStartTimeMillis = lastStartTimeMillis,
                    teamName = "Team B"
                )
            coEvery { matchRepository.getMatch() } returns flowOf(match)

            // Mock player times - playerOut is running
            val playerTimes = listOf(
                PlayerTime(playerId = playerOutId, elapsedTimeMillis = 300000L, isRunning = true, lastStartTimeMillis = lastStartTimeMillis, status = PlayerTimeStatus.PLAYING),
            )
            coEvery { getAllPlayerTimesUseCase() } returns flowOf(playerTimes)

            val substitutionSlot = slot<PlayerSubstitution>()
            coEvery { playerSubstitutionRepository.insertSubstitution(capture(substitutionSlot)) } returns 1L

            // When
            registerPlayerSubstitutionUseCase(matchId, playerOutId, playerInId, currentTimeMillis)

            // Then
            val substitution = substitutionSlot.captured
            assertEquals(420000L, substitution.matchElapsedTimeMillis)
        }

    @Test
    fun `invoke should use elapsed time when match is paused`() =
        runTest {
            // Given
            val matchId = 1L
            val playerOutId = 2L
            val playerInId = 3L
            val currentTimeMillis = System.currentTimeMillis()
            val match =
                Match(
                    id = matchId,
                    teamId = 1L,
                    elapsedTimeMillis = 600000L,
                    isRunning = false,
                    lastStartTimeMillis = null,
                    teamName = "Team B"
                )
            coEvery { matchRepository.getMatch() } returns flowOf(match)

            // Mock player times - playerOut is running
            val playerTimes = listOf(
                PlayerTime(playerId = playerOutId, elapsedTimeMillis = 300000L, isRunning = true, lastStartTimeMillis = null, status = PlayerTimeStatus.PLAYING),
            )
            coEvery { getAllPlayerTimesUseCase() } returns flowOf(playerTimes)

            val substitutionSlot = slot<PlayerSubstitution>()
            coEvery { playerSubstitutionRepository.insertSubstitution(capture(substitutionSlot)) } returns 1L

            // When
            registerPlayerSubstitutionUseCase(matchId, playerOutId, playerInId, currentTimeMillis)

            // Then
            val substitution = substitutionSlot.captured
            assertEquals(600000L, substitution.matchElapsedTimeMillis)
        }

    @Test
    fun `invoke should not substitute if player out is not running`() =
        runTest {
            // Given
            val matchId = 1L
            val playerOutId = 2L
            val playerInId = 3L
            val currentTimeMillis = System.currentTimeMillis()
            val match =
                Match(
                    id = matchId,
                    teamId = 1L,
                    elapsedTimeMillis = 600000L,
                    isRunning = true,
                    lastStartTimeMillis = currentTimeMillis - 60000L,
                    teamName = "Team B"
                )
            coEvery { matchRepository.getMatch() } returns flowOf(match)

            // Mock player times - playerOut is NOT running (on bench)
            val playerTimes = listOf(
                PlayerTime(playerId = playerOutId, elapsedTimeMillis = 300000L, isRunning = false, lastStartTimeMillis = null),
                PlayerTime(playerId = playerInId, elapsedTimeMillis = 0L, isRunning = false, lastStartTimeMillis = null),
            )
            coEvery { getAllPlayerTimesUseCase() } returns flowOf(playerTimes)

            // When
            registerPlayerSubstitutionUseCase(matchId, playerOutId, playerInId, currentTimeMillis)

            // Then - no timer operations should be called
            coVerify(exactly = 0) { playerTimeRepository.pauseTimer(any(), any()) }
            coVerify(exactly = 0) { playerTimeRepository.startTimer(any(), any()) }
            coVerify(exactly = 0) { playerSubstitutionRepository.insertSubstitution(any()) }
        }
}
