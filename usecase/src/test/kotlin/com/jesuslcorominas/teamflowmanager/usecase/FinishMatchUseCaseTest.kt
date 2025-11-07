package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.PeriodType
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTime
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeHistoryRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class FinishMatchUseCaseTest {
    private lateinit var matchRepository: MatchRepository
    private lateinit var playerTimeRepository: PlayerTimeRepository
    private lateinit var playerTimeHistoryRepository: PlayerTimeHistoryRepository
    private lateinit var finishMatchUseCase: FinishMatchUseCase

    @Before
    fun setup() {
        matchRepository = mockk(relaxed = true)
        playerTimeRepository = mockk(relaxed = true)
        playerTimeHistoryRepository = mockk(relaxed = true)
        finishMatchUseCase = FinishMatchUseCaseImpl(
            matchRepository = matchRepository,
            playerTimeRepository = playerTimeRepository,
            playerTimeHistoryRepository = playerTimeHistoryRepository,
        )
    }

    @Test
    fun `invoke should do nothing when no match exists`() =
        runTest {
            // Given
            every { matchRepository.getMatch() } returns flowOf(null)

            // When
            finishMatchUseCase.invoke()

            // Then
            coVerify(exactly = 0) { matchRepository.updateMatch(any()) }
            coVerify(exactly = 0) { playerTimeRepository.getAllPlayerTimes() }
            coVerify(exactly = 0) { playerTimeHistoryRepository.insertPlayerTimeHistory(any()) }
            coVerify(exactly = 0) { playerTimeRepository.resetAllPlayerTimes() }
        }

    @Test
    fun `invoke should save player times to history and reset`() =
        runTest {
            // Given
            val matchId = 1L
            val match = Match(id = matchId, elapsedTimeMillis = 5000L, isRunning = false, teamName = "Team A")
            val playerTimes =
                listOf(
                    PlayerTime(playerId = 1L, elapsedTimeMillis = 3000L, isRunning = false),
                    PlayerTime(playerId = 2L, elapsedTimeMillis = 2000L, isRunning = false),
                )
            every { matchRepository.getMatch() } returns flowOf(match)
            every { playerTimeRepository.getAllPlayerTimes() } returns flowOf(playerTimes)
            coEvery { playerTimeHistoryRepository.insertPlayerTimeHistory(any()) } returns 1L
            coEvery { playerTimeRepository.resetAllPlayerTimes() } returns Unit

            // When
            finishMatchUseCase.invoke()

            // Then
            coVerify {
                matchRepository.updateMatch(
                    match.copy(
                        isRunning = false,
                        elapsedTimeMillis = 5000L,
                        lastStartTimeMillis = null,
                        status = MatchStatus.FINISHED,
                    ),
                )
            }
            coVerify {
                playerTimeHistoryRepository.insertPlayerTimeHistory(
                    match {
                        it.playerId == 1L &&
                            it.matchId == matchId &&
                            it.elapsedTimeMillis == 3000L
                    },
                )
            }
            coVerify {
                playerTimeHistoryRepository.insertPlayerTimeHistory(
                    match {
                        it.playerId == 2L &&
                            it.matchId == matchId &&
                            it.elapsedTimeMillis == 2000L
                    },
                )
            }
            coVerify { playerTimeRepository.resetAllPlayerTimes() }
        }

    @Test
    fun `invoke should calculate final elapsed time for running players`() =
        runTest {
            // Given
            val matchId = 1L
            val startTime = 1000L
            val match = Match(
                id = matchId,
                elapsedTimeMillis = 5000L,
                isRunning = true,
                lastStartTimeMillis = startTime,
                teamName = "Team A"
            )
            val playerTimes =
                listOf(
                    PlayerTime(
                        playerId = 1L,
                        elapsedTimeMillis = 3000L,
                        isRunning = true,
                        lastStartTimeMillis = startTime,
                    ),
                )
            every { matchRepository.getMatch() } returns flowOf(match)
            every { playerTimeRepository.getAllPlayerTimes() } returns flowOf(playerTimes)
            coEvery { playerTimeHistoryRepository.insertPlayerTimeHistory(any()) } returns 1L
            coEvery { playerTimeRepository.resetAllPlayerTimes() } returns Unit

            // When
            finishMatchUseCase.invoke()

            // Then
            coVerify {
                matchRepository.updateMatch(
                    match {
                        it.id == matchId &&
                            it.isRunning == false &&
                            it.elapsedTimeMillis > 5000L && // Should be 5000L + current time - startTime
                            it.lastStartTimeMillis == null &&
                            it.status == MatchStatus.FINISHED
                    },
                )
            }
            coVerify {
                playerTimeHistoryRepository.insertPlayerTimeHistory(
                    match {
                        it.playerId == 1L &&
                            it.matchId == matchId &&
                            it.elapsedTimeMillis > 3000L // Should be 3000L + current time - startTime
                    },
                )
            }
            coVerify { playerTimeRepository.resetAllPlayerTimes() }
        }

    @Test
    fun `invoke should not save player times with zero elapsed time`() =
        runTest {
            // Given
            val matchId = 1L
            val match = Match(id = matchId, elapsedTimeMillis = 5000L, isRunning = false, teamName = "Team A")
            val playerTimes =
                listOf(
                    PlayerTime(playerId = 1L, elapsedTimeMillis = 0L, isRunning = false),
                    PlayerTime(playerId = 2L, elapsedTimeMillis = 2000L, isRunning = false),
                )
            every { matchRepository.getMatch() } returns flowOf(match)
            every { playerTimeRepository.getAllPlayerTimes() } returns flowOf(playerTimes)
            coEvery { playerTimeHistoryRepository.insertPlayerTimeHistory(any()) } returns 1L
            coEvery { playerTimeRepository.resetAllPlayerTimes() } returns Unit

            // When
            finishMatchUseCase.invoke()

            // Then
            coVerify(exactly = 0) {
                playerTimeHistoryRepository.insertPlayerTimeHistory(
                    match {
                        it.playerId == 1L
                    },
                )
            }
            coVerify(exactly = 1) {
                playerTimeHistoryRepository.insertPlayerTimeHistory(
                    match {
                        it.playerId == 2L
                    },
                )
            }
            coVerify { playerTimeRepository.resetAllPlayerTimes() }
        }

    @Test
    fun `invoke should save empty list when no player times exist`() =
        runTest {
            // Given
            val matchId = 1L
            val match = Match(id = matchId, elapsedTimeMillis = 5000L, isRunning = false, teamName = "Team A")
            every { matchRepository.getMatch() } returns flowOf(match)
            every { playerTimeRepository.getAllPlayerTimes() } returns flowOf(emptyList())
            coEvery { playerTimeRepository.resetAllPlayerTimes() } returns Unit

            // When
            finishMatchUseCase.invoke()

            // Then
            coVerify(exactly = 0) { playerTimeHistoryRepository.insertPlayerTimeHistory(any()) }
            coVerify { playerTimeRepository.resetAllPlayerTimes() }
        }
}
