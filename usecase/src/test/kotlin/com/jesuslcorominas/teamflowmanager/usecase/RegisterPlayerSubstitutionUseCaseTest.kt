package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperation
import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperationStatus
import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperationType
import com.jesuslcorominas.teamflowmanager.domain.model.MatchPeriod
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.domain.model.PeriodType
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerSubstitution
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTime
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeStatus
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchOperationRepository
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
    private lateinit var matchOperationRepository: MatchOperationRepository
    private lateinit var registerPlayerSubstitutionUseCase: RegisterPlayerSubstitutionUseCase

    @Before
    fun setup() {
        matchRepository = mockk(relaxed = true)
        playerTimeRepository = mockk(relaxed = true)
        playerSubstitutionRepository = mockk(relaxed = true)
        getAllPlayerTimesUseCase = mockk(relaxed = true)
        matchOperationRepository = mockk(relaxed = true)
        registerPlayerSubstitutionUseCase =
            RegisterPlayerSubstitutionUseCaseImpl(
                matchRepository,
                playerTimeRepository,
                playerSubstitutionRepository,
                getAllPlayerTimesUseCase,
                matchOperationRepository,
            )
    }

    @Test
    fun `invoke should use atomic operation pattern for substitution`() =
        runTest {
            // Given
            val matchId = 1L
            val playerOutId = 2L
            val playerInId = 3L
            val currentTimeMillis = System.currentTimeMillis()
            val periodStartTime = currentTimeMillis - 60000L
            val operationId = "op123"
            val match =
                Match(
                    id = matchId,
                    teamId = 1L,
                    teamName = "Team B",
                    opponent = "Team A",
                    location = "Stadium",
                    periodType = PeriodType.HALF_TIME,
                    captainId = 1L,
                    status = MatchStatus.IN_PROGRESS,
                    periods = listOf(
                        MatchPeriod(
                            periodNumber = 1,
                            periodDuration = 25 * 60 * 1000L,
                            startTimeMillis = periodStartTime,
                            endTimeMillis = 0L
                        )
                    )
                )
            coEvery { matchRepository.getMatchById(matchId) } returns flowOf(match)

            // Mock player times - playerOut is running
            val playerTimes = listOf(
                PlayerTime(playerId = playerOutId, elapsedTimeMillis = 300000L, isRunning = true, lastStartTimeMillis = currentTimeMillis - 60000L, status = PlayerTimeStatus.PLAYING),
                PlayerTime(playerId = playerInId, elapsedTimeMillis = 0L, isRunning = false, lastStartTimeMillis = null),
            )
            coEvery { getAllPlayerTimesUseCase() } returns flowOf(playerTimes)

            // Mock operation creation
            coEvery { matchOperationRepository.createOperation(any()) } returns operationId

            val substitutionSlot = slot<PlayerSubstitution>()
            coEvery { playerSubstitutionRepository.insertSubstitution(capture(substitutionSlot)) } returns 1L

            val operationSlot = slot<MatchOperation>()
            coEvery { matchOperationRepository.updateOperation(capture(operationSlot)) } returns Unit

            // When
            registerPlayerSubstitutionUseCase(matchId, playerOutId, playerInId, currentTimeMillis)

            // Then - verify atomic operation pattern
            // 1. Operation was created with IN_PROGRESS
            coVerify { matchOperationRepository.createOperation(
                match {
                    it.matchId == matchId &&
                    it.teamId == match.teamId &&
                    it.type == MatchOperationType.SUBSTITUTION &&
                    it.status == MatchOperationStatus.IN_PROGRESS
                }
            ) }

            // 2. Player timers updated with operation ID
            coVerify { playerTimeRepository.pauseTimersBatchWithOperationId(listOf(playerOutId), currentTimeMillis, operationId) }
            coVerify { playerTimeRepository.startTimersBatchWithOperationId(listOf(playerInId), currentTimeMillis, operationId) }

            // 3. Substitution recorded with operation ID
            coVerify { playerSubstitutionRepository.insertSubstitution(any()) }
            val substitution = substitutionSlot.captured
            assertEquals(matchId, substitution.matchId)
            assertEquals(playerOutId, substitution.playerOutId)
            assertEquals(playerInId, substitution.playerInId)
            assertEquals(operationId, substitution.operationId)

            // 4. Operation marked as COMPLETED
            val completedOperation = operationSlot.captured
            assertEquals(MatchOperationStatus.COMPLETED, completedOperation.status)

            // 5. Match updated with lastCompletedOperationId
            coVerify { matchRepository.updateMatchWithOperationId(
                match = match.copy(lastCompletedOperationId = operationId),
                operationId = operationId
            ) }
        }

    @Test
    fun `invoke should calculate correct match elapsed time when match is running`() =
        runTest {
            // Given
            val matchId = 1L
            val playerOutId = 2L
            val playerInId = 3L
            val currentTimeMillis = System.currentTimeMillis()
            val periodStartTime = currentTimeMillis - 120000L
            val operationId = "op456"
            val match =
                Match(
                    id = matchId,
                    teamId = 1L,
                    teamName = "Team B",
                    opponent = "Team A",
                    location = "Stadium",
                    periodType = PeriodType.HALF_TIME,
                    captainId = 1L,
                    status = MatchStatus.IN_PROGRESS,
                    periods = listOf(
                        MatchPeriod(
                            periodNumber = 1,
                            periodDuration = 25 * 60 * 1000L,
                            startTimeMillis = periodStartTime,
                            endTimeMillis = 0L
                        )
                    )
                )
            coEvery { matchRepository.getMatchById(matchId) } returns flowOf(match)

            // Mock player times - playerOut is running
            val playerTimes = listOf(
                PlayerTime(playerId = playerOutId, elapsedTimeMillis = 300000L, isRunning = true, lastStartTimeMillis = periodStartTime, status = PlayerTimeStatus.PLAYING),
            )
            coEvery { getAllPlayerTimesUseCase() } returns flowOf(playerTimes)

            coEvery { matchOperationRepository.createOperation(any()) } returns operationId

            val substitutionSlot = slot<PlayerSubstitution>()
            coEvery { playerSubstitutionRepository.insertSubstitution(capture(substitutionSlot)) } returns 1L

            // When
            registerPlayerSubstitutionUseCase(matchId, playerOutId, playerInId, currentTimeMillis)

            // Then - match elapsed time should be 120000L (2 minutes)
            val substitution = substitutionSlot.captured
            assertEquals(120000L, substitution.matchElapsedTimeMillis)
        }

    @Test
    fun `invoke should use elapsed time when match is paused`() =
        runTest {
            // Given
            val matchId = 1L
            val playerOutId = 2L
            val playerInId = 3L
            val currentTimeMillis = System.currentTimeMillis()
            val periodStartTime = currentTimeMillis - 600000L
            val periodEndTime = currentTimeMillis - 100000L
            val operationId = "op789"
            val match =
                Match(
                    id = matchId,
                    teamId = 1L,
                    teamName = "Team B",
                    opponent = "Team A",
                    location = "Stadium",
                    periodType = PeriodType.HALF_TIME,
                    captainId = 1L,
                    status = MatchStatus.PAUSED,
                    periods = listOf(
                        MatchPeriod(
                            periodNumber = 1,
                            periodDuration = 25 * 60 * 1000L,
                            startTimeMillis = periodStartTime,
                            endTimeMillis = periodEndTime
                        )
                    )
                )
            coEvery { matchRepository.getMatchById(matchId) } returns flowOf(match)

            // Mock player times - playerOut is running
            val playerTimes = listOf(
                PlayerTime(playerId = playerOutId, elapsedTimeMillis = 300000L, isRunning = true, lastStartTimeMillis = null, status = PlayerTimeStatus.PLAYING),
            )
            coEvery { getAllPlayerTimesUseCase() } returns flowOf(playerTimes)

            coEvery { matchOperationRepository.createOperation(any()) } returns operationId

            val substitutionSlot = slot<PlayerSubstitution>()
            coEvery { playerSubstitutionRepository.insertSubstitution(capture(substitutionSlot)) } returns 1L

            // When
            registerPlayerSubstitutionUseCase(matchId, playerOutId, playerInId, currentTimeMillis)

            // Then - elapsed time should be 500000L (period end - period start)
            val substitution = substitutionSlot.captured
            assertEquals(500000L, substitution.matchElapsedTimeMillis)
        }

    @Test
    fun `invoke should not substitute if player out is not running`() =
        runTest {
            // Given
            val matchId = 1L
            val playerOutId = 2L
            val playerInId = 3L
            val currentTimeMillis = System.currentTimeMillis()
            val periodStartTime = currentTimeMillis - 60000L
            val match =
                Match(
                    id = matchId,
                    teamId = 1L,
                    teamName = "Team B",
                    opponent = "Team A",
                    location = "Stadium",
                    periodType = PeriodType.HALF_TIME,
                    captainId = 1L,
                    status = MatchStatus.IN_PROGRESS,
                    periods = listOf(
                        MatchPeriod(
                            periodNumber = 1,
                            periodDuration = 25 * 60 * 1000L,
                            startTimeMillis = periodStartTime,
                            endTimeMillis = 0L
                        )
                    )
                )
            coEvery { matchRepository.getMatchById(matchId) } returns flowOf(match)

            // Mock player times - playerOut is NOT running (on bench)
            val playerTimes = listOf(
                PlayerTime(playerId = playerOutId, elapsedTimeMillis = 300000L, isRunning = false, lastStartTimeMillis = null),
                PlayerTime(playerId = playerInId, elapsedTimeMillis = 0L, isRunning = false, lastStartTimeMillis = null),
            )
            coEvery { getAllPlayerTimesUseCase() } returns flowOf(playerTimes)

            // When
            registerPlayerSubstitutionUseCase(matchId, playerOutId, playerInId, currentTimeMillis)

            // Then - no operations should be called
            coVerify(exactly = 0) { matchOperationRepository.createOperation(any()) }
            coVerify(exactly = 0) { playerTimeRepository.pauseTimersBatchWithOperationId(any(), any(), any()) }
            coVerify(exactly = 0) { playerTimeRepository.startTimersBatchWithOperationId(any(), any(), any()) }
            coVerify(exactly = 0) { playerSubstitutionRepository.insertSubstitution(any()) }
        }
}
