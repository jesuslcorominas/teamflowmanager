package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.model.DiscardedSubstitution
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperationCommit
import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperationStatus
import com.jesuslcorominas.teamflowmanager.domain.model.MatchOperationType
import com.jesuslcorominas.teamflowmanager.domain.model.MatchPeriod
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.domain.model.PeriodType
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerSubstitution
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTime
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeStatus
import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionDiscardReason
import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionPair
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetAllPlayerTimesUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.RegisterPlayerSubstitutionUseCase
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchOperationRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.MatchRepository
import com.jesuslcorominas.teamflowmanager.usecase.repository.PlayerTimeRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RegisterPlayerSubstitutionUseCaseTest {
    private lateinit var matchRepository: MatchRepository
    private lateinit var playerTimeRepository: PlayerTimeRepository
    private lateinit var getAllPlayerTimesUseCase: GetAllPlayerTimesUseCase
    private lateinit var matchOperationRepository: MatchOperationRepository
    private lateinit var registerPlayerSubstitutionUseCase: RegisterPlayerSubstitutionUseCase

    @Before
    fun setup() {
        matchRepository = mockk(relaxed = true)
        playerTimeRepository = mockk(relaxed = true)
        getAllPlayerTimesUseCase = mockk(relaxed = true)
        matchOperationRepository = mockk(relaxed = true)
        registerPlayerSubstitutionUseCase =
            RegisterPlayerSubstitutionUseCaseImpl(
                matchRepository,
                playerTimeRepository,
                getAllPlayerTimesUseCase,
                matchOperationRepository,
            )
    }

    private fun buildMatch(
        matchId: String,
        status: MatchStatus = MatchStatus.IN_PROGRESS,
        startTimeMillis: Long,
        endTimeMillis: Long = 0L,
        squadCallUpIds: List<String> = emptyList(),
    ) = Match(
        id = matchId,
        teamId = "1",
        teamName = "Team B",
        opponent = "Team A",
        location = "Stadium",
        periodType = PeriodType.HALF_TIME,
        captainId = "1",
        status = status,
        squadCallUpIds = squadCallUpIds,
        periods =
            listOf(
                MatchPeriod(
                    periodNumber = 1,
                    periodDuration = 25 * 60 * 1000L,
                    startTimeMillis = startTimeMillis,
                    endTimeMillis = endTimeMillis,
                ),
            ),
    )

    @Test
    fun `invoke should use atomic operation pattern for substitution`() =
        runTest {
            // Given
            val matchId = "1"
            val playerOutId = "2"
            val playerInId = "3"
            val currentTimeMillis = System.currentTimeMillis()
            val periodStartTime = currentTimeMillis - 60000L
            val operationId = "op123"
            val match = buildMatch(matchId, startTimeMillis = periodStartTime, squadCallUpIds = listOf("2", "3"))
            coEvery { matchRepository.getMatchById(matchId) } returns flowOf(match)

            // Mock player times - playerOut is running
            val playerTimes =
                listOf(
                    PlayerTime(
                        playerId = playerOutId,
                        elapsedTimeMillis = 300000L,
                        isRunning = true,
                        lastStartTimeMillis = currentTimeMillis - 60000L,
                        status = PlayerTimeStatus.PLAYING,
                    ),
                    PlayerTime(playerId = playerInId, elapsedTimeMillis = 0L, isRunning = false, lastStartTimeMillis = null),
                )
            coEvery { getAllPlayerTimesUseCase(matchId) } returns flowOf(playerTimes)

            // Mock operation creation
            coEvery { matchOperationRepository.createOperation(any()) } returns operationId

            val commitSlot = slot<MatchOperationCommit>()
            coEvery { matchOperationRepository.commitOperation(capture(commitSlot)) } returns Unit

            // When
            registerPlayerSubstitutionUseCase(
                matchId,
                listOf(SubstitutionPair(playerOutId = playerOutId, playerInId = playerInId)),
                currentTimeMillis,
            )

            // Then - verify atomic operation pattern
            // 1. Operation was created with IN_PROGRESS
            coVerify {
                matchOperationRepository.createOperation(
                    match {
                        it.matchId == matchId &&
                            it.teamId == match.teamId &&
                            it.type == MatchOperationType.SUBSTITUTION &&
                            it.status == MatchOperationStatus.IN_PROGRESS
                    },
                )
            }

            // 2. Player timers prepared with operation ID
            coVerify {
                playerTimeRepository.prepareSubstituteOutPlayersBatch(
                    matchId,
                    listOf(playerOutId),
                    currentTimeMillis,
                    operationId,
                )
            }
            coVerify {
                playerTimeRepository.prepareStartTimersBatch(matchId, listOf(playerInId), currentTimeMillis, operationId)
            }

            // 3. Atomic commit called with operation and substitution
            coVerify { matchOperationRepository.commitOperation(any()) }
            val commit = commitSlot.captured
            val substitution = commit.substitutions.first()
            assertEquals(matchId, substitution.matchId)
            assertEquals(playerOutId, substitution.playerOutId)
            assertEquals(playerInId, substitution.playerInId)
            assertEquals(operationId, substitution.operationId)

            // 4. Operation embedded in commit with COMPLETED status
            assertEquals(MatchOperationStatus.COMPLETED, commit.operation.status)

            // 5. lastCompletedOperationId set via commit
            assertEquals(operationId, commit.lastCompletedOperationId)
        }

    @Test
    fun `invoke should calculate correct match elapsed time when match is running`() =
        runTest {
            // Given
            val matchId = "1"
            val playerOutId = "2"
            val playerInId = "3"
            val currentTimeMillis = System.currentTimeMillis()
            val periodStartTime = currentTimeMillis - 120000L
            val operationId = "op456"
            val match = buildMatch(matchId, startTimeMillis = periodStartTime, squadCallUpIds = listOf("2", "3"))
            coEvery { matchRepository.getMatchById(matchId) } returns flowOf(match)

            // Mock player times - playerOut is running. playerIn is called up but has never played,
            // so it has NO player time row: that is how production looks before a first entry
            val playerTimes =
                listOf(
                    PlayerTime(
                        playerId = playerOutId,
                        elapsedTimeMillis = 300000L,
                        isRunning = true,
                        lastStartTimeMillis = periodStartTime,
                        status = PlayerTimeStatus.PLAYING,
                    ),
                )
            coEvery { getAllPlayerTimesUseCase(matchId) } returns flowOf(playerTimes)

            coEvery { matchOperationRepository.createOperation(any()) } returns operationId

            val commitSlot = slot<MatchOperationCommit>()
            coEvery { matchOperationRepository.commitOperation(capture(commitSlot)) } returns Unit

            // When
            registerPlayerSubstitutionUseCase(
                matchId,
                listOf(SubstitutionPair(playerOutId = playerOutId, playerInId = playerInId)),
                currentTimeMillis,
            )

            // Then - match elapsed time should be 120000L (2 minutes)
            val substitution = commitSlot.captured.substitutions.first()
            assertEquals(120000L, substitution.matchElapsedTimeMillis)
        }

    @Test
    fun `invoke should use elapsed time when match is paused`() =
        runTest {
            // Given
            val matchId = "1"
            val playerOutId = "2"
            val playerInId = "3"
            val currentTimeMillis = System.currentTimeMillis()
            val periodStartTime = currentTimeMillis - 600000L
            val periodEndTime = currentTimeMillis - 100000L
            val operationId = "op789"
            val match =
                buildMatch(
                    matchId,
                    status = MatchStatus.PAUSED,
                    startTimeMillis = periodStartTime,
                    endTimeMillis = periodEndTime,
                    squadCallUpIds = listOf("2", "3"),
                )
            coEvery { matchRepository.getMatchById(matchId) } returns flowOf(match)

            // Mock player times - playerOut is running. playerIn is called up but has never played,
            // so it has NO player time row: that is how production looks before a first entry
            val playerTimes =
                listOf(
                    PlayerTime(
                        playerId = playerOutId,
                        elapsedTimeMillis = 300000L,
                        isRunning = true,
                        lastStartTimeMillis = null,
                        status = PlayerTimeStatus.PLAYING,
                    ),
                )
            coEvery { getAllPlayerTimesUseCase(matchId) } returns flowOf(playerTimes)

            coEvery { matchOperationRepository.createOperation(any()) } returns operationId

            val commitSlot = slot<MatchOperationCommit>()
            coEvery { matchOperationRepository.commitOperation(capture(commitSlot)) } returns Unit

            // When
            registerPlayerSubstitutionUseCase(
                matchId,
                listOf(SubstitutionPair(playerOutId = playerOutId, playerInId = playerInId)),
                currentTimeMillis,
            )

            // Then - elapsed time should be 500000L (period end - period start)
            val substitution = commitSlot.captured.substitutions.first()
            assertEquals(500000L, substitution.matchElapsedTimeMillis)
        }

    @Test
    fun `givenPlayerOutNotFoundInPlayerTimes_whenInvoke_thenDoNothing`() =
        runTest {
            // Given
            val matchId = "1"
            val playerOutId = "99" // not present in player times
            val playerInId = "3"
            val currentTimeMillis = System.currentTimeMillis()
            val match = buildMatch(matchId, startTimeMillis = currentTimeMillis - 60000L, squadCallUpIds = listOf("2", "3"))
            coEvery { matchRepository.getMatchById(matchId) } returns flowOf(match)
            coEvery { getAllPlayerTimesUseCase(matchId) } returns
                flowOf(
                    listOf(PlayerTime(playerId = "2", status = PlayerTimeStatus.PLAYING)), // player 99 is absent
                )

            // When
            registerPlayerSubstitutionUseCase(
                matchId,
                listOf(SubstitutionPair(playerOutId = playerOutId, playerInId = playerInId)),
                currentTimeMillis,
            )

            // Then - playerOut is not playing, so no operation is started
            coVerify(exactly = 0) { matchOperationRepository.createOperation(any()) }
            coVerify(exactly = 0) { playerTimeRepository.substituteOutPlayersBatchWithOperationId(any(), any(), any(), any()) }
        }

    @Test
    fun `givenOnlyPlayerOutIsPlaying_whenInvoke_thenDoNotCallStartTimersForOtherPlayers`() =
        runTest {
            // Given
            val matchId = "1"
            val playerOutId = "2"
            val playerInId = "3"
            val currentTimeMillis = System.currentTimeMillis()
            val operationId = "op789"
            val match = buildMatch(matchId, startTimeMillis = currentTimeMillis - 60000L, squadCallUpIds = listOf("2", "3"))
            coEvery { matchRepository.getMatchById(matchId) } returns flowOf(match)
            coEvery { getAllPlayerTimesUseCase(matchId) } returns
                flowOf(
                    // only playerOut is playing; playerIn is called up but has no row yet
                    listOf(PlayerTime(playerId = playerOutId, status = PlayerTimeStatus.PLAYING)),
                )
            coEvery { matchOperationRepository.createOperation(any()) } returns operationId

            // When
            registerPlayerSubstitutionUseCase(
                matchId,
                listOf(SubstitutionPair(playerOutId = playerOutId, playerInId = playerInId)),
                currentTimeMillis,
            )

            // Then - prepareStartTimersBatch called only once (for playerIn), NOT for other players
            coVerify {
                playerTimeRepository.prepareSubstituteOutPlayersBatch(
                    matchId,
                    listOf(playerOutId),
                    currentTimeMillis,
                    operationId,
                )
            }
            coVerify(exactly = 1) { playerTimeRepository.prepareStartTimersBatch(any(), any(), any(), any()) }
            coVerify {
                playerTimeRepository.prepareStartTimersBatch(matchId, listOf(playerInId), currentTimeMillis, operationId)
            }
        }

    @Test
    fun `invoke should not substitute if player out is not running`() =
        runTest {
            // Given
            val matchId = "1"
            val playerOutId = "2"
            val playerInId = "3"
            val currentTimeMillis = System.currentTimeMillis()
            val periodStartTime = currentTimeMillis - 60000L
            val match = buildMatch(matchId, startTimeMillis = periodStartTime, squadCallUpIds = listOf("2", "3"))
            coEvery { matchRepository.getMatchById(matchId) } returns flowOf(match)

            // Mock player times - playerOut is NOT running (on bench)
            val playerTimes =
                listOf(
                    PlayerTime(playerId = playerOutId, elapsedTimeMillis = 300000L, isRunning = false, lastStartTimeMillis = null),
                    PlayerTime(playerId = playerInId, elapsedTimeMillis = 0L, isRunning = false, lastStartTimeMillis = null),
                )
            coEvery { getAllPlayerTimesUseCase(matchId) } returns flowOf(playerTimes)

            // When
            registerPlayerSubstitutionUseCase(
                matchId,
                listOf(SubstitutionPair(playerOutId = playerOutId, playerInId = playerInId)),
                currentTimeMillis,
            )

            // Then - no operations should be called
            coVerify(exactly = 0) { matchOperationRepository.createOperation(any()) }
            coVerify(exactly = 0) { playerTimeRepository.substituteOutPlayersBatchWithOperationId(any(), any(), any(), any()) }
            coVerify(exactly = 0) { playerTimeRepository.startTimersBatchWithOperationId(any(), any(), any(), any()) }
        }

    @Test(expected = IllegalArgumentException::class)
    fun `givenMatchNotFound_whenInvoke_thenThrowsIllegalArgumentException`() =
        runTest {
            // Given
            val matchId = "99"
            coEvery { matchRepository.getMatchById(matchId) } returns flowOf(null)

            // When - requireNotNull throws because match is null
            registerPlayerSubstitutionUseCase(
                matchId,
                listOf(SubstitutionPair(playerOutId = "1", playerInId = "2")),
                System.currentTimeMillis(),
            )
        }

    @Test
    fun `givenMultiplePlayersPlaying_whenInvoke_thenUpdatesAllOtherPlayingPlayersOperationId`() =
        runTest {
            // Given: playerOut is playing, plus two other players also playing
            val matchId = "1"
            val playerOutId = "2"
            val playerInId = "3"
            val otherPlayerId1 = "4"
            val otherPlayerId2 = "5"
            val currentTimeMillis = System.currentTimeMillis()
            val operationId = "op-multi"
            val match = buildMatch(matchId, startTimeMillis = currentTimeMillis - 60000L, squadCallUpIds = listOf("2", "3", "4", "5"))
            coEvery { matchRepository.getMatchById(matchId) } returns flowOf(match)

            val playerTimes =
                listOf(
                    PlayerTime(playerId = playerOutId, status = PlayerTimeStatus.PLAYING),
                    PlayerTime(playerId = otherPlayerId1, status = PlayerTimeStatus.PLAYING),
                    PlayerTime(playerId = otherPlayerId2, status = PlayerTimeStatus.PLAYING),
                    PlayerTime(playerId = playerInId, status = PlayerTimeStatus.ON_BENCH),
                )
            coEvery { getAllPlayerTimesUseCase(matchId) } returns flowOf(playerTimes)
            coEvery { matchOperationRepository.createOperation(any()) } returns operationId

            // When
            registerPlayerSubstitutionUseCase(
                matchId,
                listOf(SubstitutionPair(playerOutId = playerOutId, playerInId = playerInId)),
                currentTimeMillis,
            )

            // Then - prepareStartTimersBatch called twice: once for playerIn, once for the other playing players
            coVerify {
                playerTimeRepository.prepareSubstituteOutPlayersBatch(
                    matchId,
                    listOf(playerOutId),
                    currentTimeMillis,
                    operationId,
                )
            }
            coVerify {
                playerTimeRepository.prepareStartTimersBatch(matchId, listOf(playerInId), currentTimeMillis, operationId)
            }
            coVerify {
                playerTimeRepository.prepareStartTimersBatch(
                    matchId,
                    listOf(otherPlayerId1, otherPlayerId2),
                    currentTimeMillis,
                    operationId,
                )
            }
        }

    @Test
    fun `givenEmptyList_whenInvoke_thenDoNothingAndDoNotReadMatch`() =
        runTest {
            // Given
            val matchId = "1"
            val currentTimeMillis = System.currentTimeMillis()

            // When
            val result = registerPlayerSubstitutionUseCase(matchId, emptyList(), currentTimeMillis)

            // Then - an empty batch applies nothing and discards nothing
            assertEquals(emptyList<SubstitutionPair>(), result.applied)
            assertEquals(emptyList<DiscardedSubstitution>(), result.discarded)

            // Then - exits before touching any repository: neither the match nor the times are read
            coVerify(exactly = 0) { matchOperationRepository.createOperation(any()) }
            coVerify(exactly = 0) { matchRepository.getMatchById(any()) }
            coVerify(exactly = 0) { getAllPlayerTimesUseCase(any()) }
        }

    @Test
    fun `givenThreeValidPairs_whenInvoke_thenSingleOperationAndThreeSubstitutions`() =
        runTest {
            // Given: three playing players leaving, three bench players coming in
            val matchId = "1"
            val currentTimeMillis = System.currentTimeMillis()
            val periodStartTime = currentTimeMillis - 120000L
            val operationId = "op-batch-3"
            val match = buildMatch(matchId, startTimeMillis = periodStartTime, squadCallUpIds = listOf("out1", "out2", "out3", "in1", "in2", "in3"))
            coEvery { matchRepository.getMatchById(matchId) } returns flowOf(match)

            val playerTimes =
                listOf(
                    PlayerTime(playerId = "out1", status = PlayerTimeStatus.PLAYING),
                    PlayerTime(playerId = "out2", status = PlayerTimeStatus.PLAYING),
                    PlayerTime(playerId = "out3", status = PlayerTimeStatus.PLAYING),
                    PlayerTime(playerId = "in1", status = PlayerTimeStatus.ON_BENCH),
                    PlayerTime(playerId = "in2", status = PlayerTimeStatus.ON_BENCH),
                    PlayerTime(playerId = "in3", status = PlayerTimeStatus.ON_BENCH),
                )
            coEvery { getAllPlayerTimesUseCase(matchId) } returns flowOf(playerTimes)
            coEvery { matchOperationRepository.createOperation(any()) } returns operationId

            val commitSlot = slot<MatchOperationCommit>()
            coEvery { matchOperationRepository.commitOperation(capture(commitSlot)) } returns Unit

            val pairs =
                listOf(
                    SubstitutionPair(playerOutId = "out1", playerInId = "in1"),
                    SubstitutionPair(playerOutId = "out2", playerInId = "in2"),
                    SubstitutionPair(playerOutId = "out3", playerInId = "in3"),
                )

            // When
            val result = registerPlayerSubstitutionUseCase(matchId, pairs, currentTimeMillis)

            // Then - the three pairs are reported as applied, in the requested order
            assertEquals(pairs, result.applied)
            assertEquals(emptyList<DiscardedSubstitution>(), result.discarded)

            // Then - a single atomic operation covers the whole batch
            coVerify(exactly = 1) { matchOperationRepository.createOperation(any()) }
            coVerify(exactly = 1) { matchOperationRepository.commitOperation(any()) }
            @Suppress("DEPRECATION")
            coVerify(exactly = 0) { matchRepository.updateMatchWithOperationId(any(), any()) }

            // Single batched call for the three leaving players
            coVerify(exactly = 1) { playerTimeRepository.prepareSubstituteOutPlayersBatch(any(), any(), any(), any()) }
            coVerify {
                playerTimeRepository.prepareSubstituteOutPlayersBatch(
                    matchId,
                    listOf("out1", "out2", "out3"),
                    currentTimeMillis,
                    operationId,
                )
            }

            // Single batched call for the three incoming players (nobody else stays on the pitch)
            coVerify(exactly = 1) { playerTimeRepository.prepareStartTimersBatch(any(), any(), any(), any()) }
            coVerify {
                playerTimeRepository.prepareStartTimersBatch(
                    matchId,
                    listOf("in1", "in2", "in3"),
                    currentTimeMillis,
                    operationId,
                )
            }

            // Three substitutions, all sharing the same operationId and elapsed time
            val substitutions = commitSlot.captured.substitutions
            assertEquals(3, substitutions.size)
            assertEquals(listOf("out1", "out2", "out3"), substitutions.map { it.playerOutId })
            assertEquals(listOf("in1", "in2", "in3"), substitutions.map { it.playerInId })
            assertTrue(substitutions.all { it.operationId == operationId })
            assertEquals(1, substitutions.map { it.matchElapsedTimeMillis }.distinct().size)
            assertEquals(120000L, substitutions.first().matchElapsedTimeMillis)
        }

    @Test
    fun `givenMixOfValidAndInvalidPairs_whenInvoke_thenInvalidIsDiscardedAndBatchContinues`() =
        runTest {
            // Given: out2 is ON_BENCH, so its pair is invalid but must not abort the batch
            val matchId = "1"
            val currentTimeMillis = System.currentTimeMillis()
            val operationId = "op-mixed"
            val match = buildMatch(matchId, startTimeMillis = currentTimeMillis - 60000L, squadCallUpIds = listOf("out1", "out2", "out3", "in1", "in2", "in3"))
            coEvery { matchRepository.getMatchById(matchId) } returns flowOf(match)

            val playerTimes =
                listOf(
                    PlayerTime(playerId = "out1", status = PlayerTimeStatus.PLAYING),
                    PlayerTime(playerId = "out2", status = PlayerTimeStatus.ON_BENCH),
                    PlayerTime(playerId = "out3", status = PlayerTimeStatus.PLAYING),
                    PlayerTime(playerId = "in1", status = PlayerTimeStatus.ON_BENCH),
                    PlayerTime(playerId = "in2", status = PlayerTimeStatus.ON_BENCH),
                    PlayerTime(playerId = "in3", status = PlayerTimeStatus.ON_BENCH),
                )
            coEvery { getAllPlayerTimesUseCase(matchId) } returns flowOf(playerTimes)
            coEvery { matchOperationRepository.createOperation(any()) } returns operationId

            val commitSlot = slot<MatchOperationCommit>()
            coEvery { matchOperationRepository.commitOperation(capture(commitSlot)) } returns Unit

            val pairs =
                listOf(
                    SubstitutionPair(playerOutId = "out1", playerInId = "in1"),
                    SubstitutionPair(playerOutId = "out2", playerInId = "in2"),
                    SubstitutionPair(playerOutId = "out3", playerInId = "in3"),
                )

            // When
            val result = registerPlayerSubstitutionUseCase(matchId, pairs, currentTimeMillis)

            // Then - the result tells which pair fell and why, keeping the requested order
            assertEquals(listOf(pairs[0], pairs[2]), result.applied)
            assertEquals(
                listOf(
                    DiscardedSubstitution(pairs[1], SubstitutionDiscardReason.PLAYER_OUT_NOT_PLAYING),
                ),
                result.discarded,
            )

            // Then - only the two valid pairs are applied, under a single operation
            coVerify(exactly = 1) { matchOperationRepository.createOperation(any()) }
            coVerify {
                playerTimeRepository.prepareSubstituteOutPlayersBatch(
                    matchId,
                    listOf("out1", "out3"),
                    currentTimeMillis,
                    operationId,
                )
            }
            coVerify {
                playerTimeRepository.prepareStartTimersBatch(
                    matchId,
                    listOf("in1", "in3"),
                    currentTimeMillis,
                    operationId,
                )
            }
            val substitutions = commitSlot.captured.substitutions
            assertEquals(2, substitutions.size)
            assertEquals(listOf("out1", "out3"), substitutions.map { it.playerOutId })
            assertEquals(listOf("in1", "in3"), substitutions.map { it.playerInId })
            assertTrue(substitutions.all { it.operationId == operationId })
        }

    @Test
    fun `givenAllPairsInvalid_whenInvoke_thenNoOperationIsCreated`() =
        runTest {
            // Given: no leaving player is PLAYING
            val matchId = "1"
            val currentTimeMillis = System.currentTimeMillis()
            val match = buildMatch(matchId, startTimeMillis = currentTimeMillis - 60000L, squadCallUpIds = listOf("out1", "out2", "in1", "in2"))
            coEvery { matchRepository.getMatchById(matchId) } returns flowOf(match)
            coEvery { getAllPlayerTimesUseCase(matchId) } returns
                flowOf(
                    listOf(
                        PlayerTime(playerId = "out1", status = PlayerTimeStatus.ON_BENCH),
                        PlayerTime(playerId = "out2", status = PlayerTimeStatus.ON_BENCH),
                    ),
                )

            val pairs =
                listOf(
                    SubstitutionPair(playerOutId = "out1", playerInId = "in1"),
                    SubstitutionPair(playerOutId = "out2", playerInId = "in2"),
                )

            // When
            val result = registerPlayerSubstitutionUseCase(matchId, pairs, currentTimeMillis)

            // Then - REASON 1: nothing applied, every pair reported as PLAYER_OUT_NOT_PLAYING
            assertEquals(emptyList<SubstitutionPair>(), result.applied)
            assertEquals(
                listOf(
                    DiscardedSubstitution(pairs[0], SubstitutionDiscardReason.PLAYER_OUT_NOT_PLAYING),
                    DiscardedSubstitution(pairs[1], SubstitutionDiscardReason.PLAYER_OUT_NOT_PLAYING),
                ),
                result.discarded,
            )

            coVerify(exactly = 0) { matchOperationRepository.createOperation(any()) }
            coVerify(exactly = 0) { playerTimeRepository.substituteOutPlayersBatchWithOperationId(any(), any(), any(), any()) }
            coVerify(exactly = 0) { playerTimeRepository.startTimersBatchWithOperationId(any(), any(), any(), any()) }
            coVerify(exactly = 0) { matchRepository.updateMatchWithOperationId(any(), any()) }
        }

    @Test
    fun `givenBatchAndOtherPlayersStillPlaying_whenInvoke_thenTheyGetTheNewOperationId`() =
        runTest {
            // Given: two pairs leaving while two other players remain on the pitch
            val matchId = "1"
            val currentTimeMillis = System.currentTimeMillis()
            val operationId = "op-others"
            val match = buildMatch(matchId, startTimeMillis = currentTimeMillis - 60000L, squadCallUpIds = listOf("out1", "out2", "in1", "in2", "stay1", "stay2"))
            coEvery { matchRepository.getMatchById(matchId) } returns flowOf(match)

            val playerTimes =
                listOf(
                    PlayerTime(playerId = "out1", status = PlayerTimeStatus.PLAYING),
                    PlayerTime(playerId = "stay1", status = PlayerTimeStatus.PLAYING),
                    PlayerTime(playerId = "out2", status = PlayerTimeStatus.PLAYING),
                    PlayerTime(playerId = "stay2", status = PlayerTimeStatus.PLAYING),
                    PlayerTime(playerId = "in1", status = PlayerTimeStatus.ON_BENCH),
                    PlayerTime(playerId = "in2", status = PlayerTimeStatus.ON_BENCH),
                )
            coEvery { getAllPlayerTimesUseCase(matchId) } returns flowOf(playerTimes)
            coEvery { matchOperationRepository.createOperation(any()) } returns operationId

            val pairs =
                listOf(
                    SubstitutionPair(playerOutId = "out1", playerInId = "in1"),
                    SubstitutionPair(playerOutId = "out2", playerInId = "in2"),
                )

            // When
            registerPlayerSubstitutionUseCase(matchId, pairs, currentTimeMillis)

            // Then - prepareStartTimersBatch called twice: incoming players and remaining players
            coVerify(exactly = 2) { playerTimeRepository.prepareStartTimersBatch(any(), any(), any(), any()) }
            coVerify {
                playerTimeRepository.prepareStartTimersBatch(
                    matchId,
                    listOf("in1", "in2"),
                    currentTimeMillis,
                    operationId,
                )
            }
            coVerify {
                playerTimeRepository.prepareStartTimersBatch(
                    matchId,
                    listOf("stay1", "stay2"),
                    currentTimeMillis,
                    operationId,
                )
            }
        }

    @Test
    fun `givenDuplicatedPlayerOut_whenInvoke_thenOnlyFirstPairIsApplied`() =
        runTest {
            // Given: two pairs sharing the same leaving player
            val matchId = "1"
            val currentTimeMillis = System.currentTimeMillis()
            val operationId = "op-dup"
            val match = buildMatch(matchId, startTimeMillis = currentTimeMillis - 60000L, squadCallUpIds = listOf("out1", "in1", "in2"))
            coEvery { matchRepository.getMatchById(matchId) } returns flowOf(match)

            val playerTimes =
                listOf(
                    PlayerTime(playerId = "out1", status = PlayerTimeStatus.PLAYING),
                    PlayerTime(playerId = "in1", status = PlayerTimeStatus.ON_BENCH),
                    PlayerTime(playerId = "in2", status = PlayerTimeStatus.ON_BENCH),
                )
            coEvery { getAllPlayerTimesUseCase(matchId) } returns flowOf(playerTimes)
            coEvery { matchOperationRepository.createOperation(any()) } returns operationId

            val commitSlot = slot<MatchOperationCommit>()
            coEvery { matchOperationRepository.commitOperation(capture(commitSlot)) } returns Unit

            val pairs =
                listOf(
                    SubstitutionPair(playerOutId = "out1", playerInId = "in1"),
                    SubstitutionPair(playerOutId = "out1", playerInId = "in2"),
                )

            // When
            val result = registerPlayerSubstitutionUseCase(matchId, pairs, currentTimeMillis)

            // Then - REASON 4 on the leaving side this time
            assertEquals(listOf(pairs[0]), result.applied)
            assertEquals(
                listOf(
                    DiscardedSubstitution(pairs[1], SubstitutionDiscardReason.PLAYER_ALREADY_SUBSTITUTED_IN_BATCH),
                ),
                result.discarded,
            )

            // Then - the leaving player is consumed by the first pair, so the second one is dropped
            val substitutions = commitSlot.captured.substitutions
            assertEquals(1, substitutions.size)
            assertEquals("out1", substitutions.first().playerOutId)
            assertEquals("in1", substitutions.first().playerInId)
            coVerify {
                playerTimeRepository.prepareSubstituteOutPlayersBatch(
                    matchId,
                    listOf("out1"),
                    currentTimeMillis,
                    operationId,
                )
            }
            coVerify(exactly = 1) { playerTimeRepository.prepareStartTimersBatch(any(), any(), any(), any()) }
            coVerify {
                playerTimeRepository.prepareStartTimersBatch(matchId, listOf("in1"), currentTimeMillis, operationId)
            }
        }

    @Test
    fun `givenDuplicatedPlayerIn_whenInvoke_thenSecondPairIsDiscardedEntirely`() =
        runTest {
            // Given: two pairs sharing the same incoming player
            val matchId = "1"
            val currentTimeMillis = System.currentTimeMillis()
            val operationId = "op-dup-in"
            val match = buildMatch(matchId, startTimeMillis = currentTimeMillis - 60000L, squadCallUpIds = listOf("out1", "out2", "in1"))
            coEvery { matchRepository.getMatchById(matchId) } returns flowOf(match)

            val playerTimes =
                listOf(
                    PlayerTime(playerId = "out1", status = PlayerTimeStatus.PLAYING),
                    PlayerTime(playerId = "out2", status = PlayerTimeStatus.PLAYING),
                    PlayerTime(playerId = "in1", status = PlayerTimeStatus.ON_BENCH),
                )
            coEvery { getAllPlayerTimesUseCase(matchId) } returns flowOf(playerTimes)
            coEvery { matchOperationRepository.createOperation(any()) } returns operationId

            val commitSlot = slot<MatchOperationCommit>()
            coEvery { matchOperationRepository.commitOperation(capture(commitSlot)) } returns Unit

            val pairs =
                listOf(
                    SubstitutionPair(playerOutId = "out1", playerInId = "in1"),
                    SubstitutionPair(playerOutId = "out2", playerInId = "in1"),
                )

            // When
            val result = registerPlayerSubstitutionUseCase(matchId, pairs, currentTimeMillis)

            // Then - REASON 4: in1 was consumed by the first pair, so the second one is reported as
            // an incompatible pair rather than silently half-applied
            assertEquals(listOf(pairs[0]), result.applied)
            assertEquals(
                listOf(
                    DiscardedSubstitution(pairs[1], SubstitutionDiscardReason.PLAYER_ALREADY_SUBSTITUTED_IN_BATCH),
                ),
                result.discarded,
            )

            // Then - the second pair is dropped whole: out2 must NOT be benched, or the team would
            // be left a player short with a single incoming player covering two exits
            val substitutions = commitSlot.captured.substitutions
            assertEquals(1, substitutions.size)
            assertEquals("out1", substitutions.first().playerOutId)
            assertEquals("in1", substitutions.first().playerInId)
            coVerify {
                playerTimeRepository.prepareSubstituteOutPlayersBatch(
                    matchId,
                    listOf("out1"),
                    currentTimeMillis,
                    operationId,
                )
            }
            // out2 stays on the pitch and only gets the operationId refresh
            coVerify {
                playerTimeRepository.prepareStartTimersBatch(matchId, listOf("out2"), currentTimeMillis, operationId)
            }
        }

    @Test
    fun `givenPlayerOutEqualsPlayerIn_whenInvoke_thenPairIsDiscarded`() =
        runTest {
            // Given: a self-substitution A -> A for a player currently on the pitch
            val matchId = "1"
            val currentTimeMillis = System.currentTimeMillis()
            val match = buildMatch(matchId, startTimeMillis = currentTimeMillis - 60000L, squadCallUpIds = listOf("out1"))
            coEvery { matchRepository.getMatchById(matchId) } returns flowOf(match)
            coEvery { getAllPlayerTimesUseCase(matchId) } returns
                flowOf(listOf(PlayerTime(playerId = "out1", status = PlayerTimeStatus.PLAYING)))

            val pairs = listOf(SubstitutionPair(playerOutId = "out1", playerInId = "out1"))

            // When
            val result = registerPlayerSubstitutionUseCase(matchId, pairs, currentTimeMillis)

            // Then - REASON 2: A is on the pitch, so A is a valid leaver but an invalid arrival
            assertEquals(emptyList<SubstitutionPair>(), result.applied)
            assertEquals(
                listOf(
                    DiscardedSubstitution(pairs[0], SubstitutionDiscardReason.PLAYER_IN_ALREADY_PLAYING),
                ),
                result.discarded,
            )

            // Then - no operation at all: benching and restarting the same player in one operation
            // would alter the time accounting and leave an A <-> A row in the history
            coVerify(exactly = 0) { matchOperationRepository.createOperation(any()) }
            coVerify(exactly = 0) { playerTimeRepository.substituteOutPlayersBatchWithOperationId(any(), any(), any(), any()) }
        }

    @Test
    fun `givenChainedPairs_whenInvoke_thenPairWhoseIncomingPlayerIsOnThePitchIsDiscarded`() =
        runTest {
            // Given: a chained batch A -> B, B -> C where B is currently on the pitch
            val matchId = "1"
            val currentTimeMillis = System.currentTimeMillis()
            val operationId = "op-chained"
            val match = buildMatch(matchId, startTimeMillis = currentTimeMillis - 60000L, squadCallUpIds = listOf("A", "B", "C"))
            coEvery { matchRepository.getMatchById(matchId) } returns flowOf(match)

            val playerTimes =
                listOf(
                    PlayerTime(playerId = "A", status = PlayerTimeStatus.PLAYING),
                    PlayerTime(playerId = "B", status = PlayerTimeStatus.PLAYING),
                    PlayerTime(playerId = "C", status = PlayerTimeStatus.ON_BENCH),
                )
            coEvery { getAllPlayerTimesUseCase(matchId) } returns flowOf(playerTimes)
            coEvery { matchOperationRepository.createOperation(any()) } returns operationId

            val commitSlot = slot<MatchOperationCommit>()
            coEvery { matchOperationRepository.commitOperation(capture(commitSlot)) } returns Unit

            val pairs =
                listOf(
                    SubstitutionPair(playerOutId = "A", playerInId = "B"),
                    SubstitutionPair(playerOutId = "B", playerInId = "C"),
                )

            // When
            val result = registerPlayerSubstitutionUseCase(matchId, pairs, currentTimeMillis)

            // Then - REASON 2 again, this time through a chain: only B -> C survives
            assertEquals(listOf(pairs[1]), result.applied)
            assertEquals(
                listOf(
                    DiscardedSubstitution(pairs[0], SubstitutionDiscardReason.PLAYER_IN_ALREADY_PLAYING),
                ),
                result.discarded,
            )

            // Then - A -> B is dropped because B is already playing. Applying it would bench B and
            // restart B's timer in the same operation, leaving B on the pitch while the history
            // claims B left, and putting two players on for a single exit
            val substitutions = commitSlot.captured.substitutions
            assertEquals(1, substitutions.size)
            assertEquals("B", substitutions.first().playerOutId)
            assertEquals("C", substitutions.first().playerInId)
            coVerify {
                playerTimeRepository.prepareSubstituteOutPlayersBatch(
                    matchId,
                    listOf("B"),
                    currentTimeMillis,
                    operationId,
                )
            }
            coVerify {
                playerTimeRepository.prepareStartTimersBatch(matchId, listOf("C"), currentTimeMillis, operationId)
            }
            // A never leaves the pitch, it only gets the operationId refresh
            coVerify {
                playerTimeRepository.prepareStartTimersBatch(matchId, listOf("A"), currentTimeMillis, operationId)
            }
        }

    @Test
    fun `givenUnknownPlayerIn_whenInvoke_thenPairIsDiscardedAndNoGhostPlayerIsCreated`() =
        runTest {
            // Given: an incoming player that has no PlayerTime for this match (not called up)
            val matchId = "1"
            val currentTimeMillis = System.currentTimeMillis()
            val operationId = "op-ghost"
            val match = buildMatch(matchId, startTimeMillis = currentTimeMillis - 60000L, squadCallUpIds = listOf("out1", "out2", "in1"))
            coEvery { matchRepository.getMatchById(matchId) } returns flowOf(match)

            val playerTimes =
                listOf(
                    PlayerTime(playerId = "out1", status = PlayerTimeStatus.PLAYING),
                    PlayerTime(playerId = "out2", status = PlayerTimeStatus.PLAYING),
                    PlayerTime(playerId = "in1", status = PlayerTimeStatus.ON_BENCH),
                )
            coEvery { getAllPlayerTimesUseCase(matchId) } returns flowOf(playerTimes)
            coEvery { matchOperationRepository.createOperation(any()) } returns operationId

            val commitSlot = slot<MatchOperationCommit>()
            coEvery { matchOperationRepository.commitOperation(capture(commitSlot)) } returns Unit

            val pairs =
                listOf(
                    SubstitutionPair(playerOutId = "out1", playerInId = "in1"),
                    SubstitutionPair(playerOutId = "out2", playerInId = "unknown"),
                )

            // When
            val result = registerPlayerSubstitutionUseCase(matchId, pairs, currentTimeMillis)

            // Then - REASON 3: the caller can tell the pending change points at someone not called up
            assertEquals(listOf(pairs[0]), result.applied)
            assertEquals(
                listOf(
                    DiscardedSubstitution(pairs[1], SubstitutionDiscardReason.PLAYER_IN_NOT_IN_MATCH),
                ),
                result.discarded,
            )

            // Then - the unknown id never reaches prepareStartTimersBatch, which would
            // upsert a brand new PlayerTime row in PLAYING for a player nobody called up
            val substitutions = commitSlot.captured.substitutions
            assertEquals(1, substitutions.size)
            assertEquals("in1", substitutions.first().playerInId)
            coVerify(exactly = 0) {
                playerTimeRepository.prepareStartTimersBatch(
                    any(),
                    match { it.contains("unknown") },
                    any(),
                    any(),
                )
            }
            coVerify {
                playerTimeRepository.prepareSubstituteOutPlayersBatch(
                    matchId,
                    listOf("out1"),
                    currentTimeMillis,
                    operationId,
                )
            }
        }

    @Test
    fun `givenCalledUpSubstituteWithoutPlayerTimeRow_whenInvoke_thenPairIsApplied`() =
        runTest {
            // Given: the production shape of a first substitution. PlayerTime rows are created
            // lazily by startTimersBatchWithOperationId, so a called-up substitute who has not
            // played yet has NO row - only the starters do. Validating the incoming player against
            // the player times instead of the squad call-up would silently drop this substitution,
            // which is the first one of every substitute in every match.
            val matchId = "1"
            val currentTimeMillis = System.currentTimeMillis()
            val operationId = "op-first-entry"
            val match =
                buildMatch(
                    matchId,
                    startTimeMillis = currentTimeMillis - 60000L,
                    squadCallUpIds = listOf("starter1", "starter2", "bench1"),
                )
            coEvery { matchRepository.getMatchById(matchId) } returns flowOf(match)

            // Only the starters have a row; bench1 has never played
            val playerTimes =
                listOf(
                    PlayerTime(playerId = "starter1", status = PlayerTimeStatus.PLAYING),
                    PlayerTime(playerId = "starter2", status = PlayerTimeStatus.PLAYING),
                )
            coEvery { getAllPlayerTimesUseCase(matchId) } returns flowOf(playerTimes)
            coEvery { matchOperationRepository.createOperation(any()) } returns operationId

            val commitSlot = slot<MatchOperationCommit>()
            coEvery { matchOperationRepository.commitOperation(capture(commitSlot)) } returns Unit

            val pairs = listOf(SubstitutionPair(playerOutId = "starter1", playerInId = "bench1"))

            // When
            val result = registerPlayerSubstitutionUseCase(matchId, pairs, currentTimeMillis)

            // Then - the substitution goes through
            assertEquals(pairs, result.applied)
            assertEquals(emptyList<DiscardedSubstitution>(), result.discarded)
            val substitutions = commitSlot.captured.substitutions
            assertEquals(1, substitutions.size)
            assertEquals("bench1", substitutions.first().playerInId)
            coVerify(exactly = 1) { matchOperationRepository.createOperation(any()) }
            coVerify {
                playerTimeRepository.prepareSubstituteOutPlayersBatch(
                    matchId,
                    listOf("starter1"),
                    currentTimeMillis,
                    operationId,
                )
            }
            coVerify {
                playerTimeRepository.prepareStartTimersBatch(matchId, listOf("bench1"), currentTimeMillis, operationId)
            }
        }

    @Test
    fun `givenPairBreakingSeveralRules_whenInvoke_thenReasonFollowsEnumDeclarationOrder`() =
        runTest {
            // Given: a pair that breaks three rules at once - its leaving player is on the bench,
            // its incoming player is on the pitch, and both repeat the first pair of the batch
            val matchId = "1"
            val currentTimeMillis = System.currentTimeMillis()
            val operationId = "op-precedence"
            val match = buildMatch(matchId, startTimeMillis = currentTimeMillis - 60000L, squadCallUpIds = listOf("out1", "in1"))
            coEvery { matchRepository.getMatchById(matchId) } returns flowOf(match)

            val playerTimes =
                listOf(
                    PlayerTime(playerId = "out1", status = PlayerTimeStatus.PLAYING),
                    PlayerTime(playerId = "in1", status = PlayerTimeStatus.ON_BENCH),
                )
            coEvery { getAllPlayerTimesUseCase(matchId) } returns flowOf(playerTimes)
            coEvery { matchOperationRepository.createOperation(any()) } returns operationId

            val pairs =
                listOf(
                    SubstitutionPair(playerOutId = "out1", playerInId = "in1"),
                    // in1 is now consumed AND out1 is consumed, but the very first rule that the
                    // second pair breaks is that its leaving player (in1) is not on the pitch
                    SubstitutionPair(playerOutId = "in1", playerInId = "out1"),
                )

            // When
            val result = registerPlayerSubstitutionUseCase(matchId, pairs, currentTimeMillis)

            // Then - the earliest reason in the enum wins, not the last rule evaluated
            assertEquals(listOf(pairs[0]), result.applied)
            assertEquals(
                listOf(
                    DiscardedSubstitution(pairs[1], SubstitutionDiscardReason.PLAYER_OUT_NOT_PLAYING),
                ),
                result.discarded,
            )
        }

    // -----------------------------------------------------------------------------------------
    // Regression tests — #423: atomic commit, #416: error handling
    // -----------------------------------------------------------------------------------------

    /**
     * #423 — Verifies that a substitution is persisted through a SINGLE commitOperation call that
     * includes all affected PlayerTimes and the lastCompletedOperationId together.
     *
     * Regression: currently the impl makes four separate writes
     * (substituteOutPlayersBatchWithOperationId, two startTimersBatchWithOperationId calls,
     * updateMatchWithOperationId) and never calls commitOperation. This test fails until T004
     * migrates the use case to the atomic pattern.
     */
    @Test
    fun `#423 when substitution is applied, single commitOperation call includes all PlayerTimes and lastCompletedOperationId`() =
        runTest {
            // Given
            val matchId = "1"
            val currentTimeMillis = System.currentTimeMillis()
            val operationId = "op-atomic-423"
            val match =
                buildMatch(
                    matchId,
                    startTimeMillis = currentTimeMillis - 60_000L,
                    squadCallUpIds = listOf("player-out", "player-in"),
                )
            coEvery { matchRepository.getMatchById(matchId) } returns flowOf(match)
            coEvery { matchOperationRepository.createOperation(any()) } returns operationId

            val playerOutTime =
                PlayerTime(playerId = "player-out", status = PlayerTimeStatus.ON_BENCH, isRunning = false, lastOperationId = operationId)
            val playerInTime =
                PlayerTime(playerId = "player-in", status = PlayerTimeStatus.PLAYING, isRunning = true, lastOperationId = operationId)

            coEvery { getAllPlayerTimesUseCase(matchId) } returns
                flowOf(
                    listOf(PlayerTime(playerId = "player-out", status = PlayerTimeStatus.PLAYING, isRunning = true)),
                )
            coEvery {
                playerTimeRepository.prepareSubstituteOutPlayersBatch(matchId, listOf("player-out"), currentTimeMillis, operationId)
            } returns listOf(playerOutTime)
            coEvery {
                playerTimeRepository.prepareStartTimersBatch(matchId, listOf("player-in"), currentTimeMillis, operationId)
            } returns listOf(playerInTime)
            // No other playing players — otherPlayingPlayers list is empty

            val commitSlot = slot<MatchOperationCommit>()
            coEvery { matchOperationRepository.commitOperation(capture(commitSlot)) } returns Unit

            // When
            registerPlayerSubstitutionUseCase(
                matchId,
                listOf(SubstitutionPair(playerOutId = "player-out", playerInId = "player-in")),
                currentTimeMillis,
            )

            // Then — single atomic write, no separate writes
            coVerify(exactly = 1) { matchOperationRepository.commitOperation(any()) }
            @Suppress("DEPRECATION")
            coVerify(exactly = 0) { matchRepository.updateMatchWithOperationId(any(), any()) }
            coVerify(exactly = 0) { matchRepository.updateLastCompletedOperationId(any(), any()) }

            // Verify commit content
            val commit = commitSlot.captured
            assertTrue("commit must include the outgoing player's time", commit.playerTimes.any { it.playerId == "player-out" })
            assertTrue("commit must include the incoming player's time", commit.playerTimes.any { it.playerId == "player-in" })
            assertEquals("lastCompletedOperationId must match the created operation id", operationId, commit.lastCompletedOperationId)
        }

    /**
     * #423 — The scenario actually observed on device: a batch applied while other players were
     * still on the pitch. Those players get their operationId re-stamped too, and all three groups
     * (outgoing, incoming and the rest of the pitch) must land in the SAME commit. If any of them
     * were committed separately, the UI filter would stop matching them and the pitch would empty
     * out, which is exactly the reported bug.
     */
    @Test
    fun `#423 commit includes outgoing, incoming and remaining pitch players in one single write`() =
        runTest {
            // Given
            val matchId = "1"
            val currentTimeMillis = System.currentTimeMillis()
            val operationId = "op-atomic-423-full"
            val match =
                buildMatch(
                    matchId,
                    startTimeMillis = currentTimeMillis - 60_000L,
                    squadCallUpIds = listOf("player-out", "player-in", "player-stays"),
                )
            coEvery { matchRepository.getMatchById(matchId) } returns flowOf(match)
            coEvery { matchOperationRepository.createOperation(any()) } returns operationId

            // "player-stays" is on the pitch and is NOT part of the substitution pair
            coEvery { getAllPlayerTimesUseCase(matchId) } returns
                flowOf(
                    listOf(
                        PlayerTime(playerId = "player-out", status = PlayerTimeStatus.PLAYING, isRunning = true),
                        PlayerTime(playerId = "player-stays", status = PlayerTimeStatus.PLAYING, isRunning = true),
                    ),
                )
            coEvery {
                playerTimeRepository.prepareSubstituteOutPlayersBatch(matchId, listOf("player-out"), currentTimeMillis, operationId)
            } returns
                listOf(
                    PlayerTime(playerId = "player-out", status = PlayerTimeStatus.ON_BENCH, isRunning = false, lastOperationId = operationId),
                )
            coEvery {
                playerTimeRepository.prepareStartTimersBatch(matchId, listOf("player-in"), currentTimeMillis, operationId)
            } returns
                listOf(
                    PlayerTime(playerId = "player-in", status = PlayerTimeStatus.PLAYING, isRunning = true, lastOperationId = operationId),
                )
            coEvery {
                playerTimeRepository.prepareStartTimersBatch(matchId, listOf("player-stays"), currentTimeMillis, operationId)
            } returns
                listOf(
                    PlayerTime(playerId = "player-stays", status = PlayerTimeStatus.PLAYING, isRunning = true, lastOperationId = operationId),
                )

            val commitSlot = slot<MatchOperationCommit>()
            coEvery { matchOperationRepository.commitOperation(capture(commitSlot)) } returns Unit

            // When
            registerPlayerSubstitutionUseCase(
                matchId,
                listOf(SubstitutionPair(playerOutId = "player-out", playerInId = "player-in")),
                currentTimeMillis,
            )

            // Then — exactly one write, carrying all three groups
            coVerify(exactly = 1) { matchOperationRepository.commitOperation(any()) }

            val commit = commitSlot.captured
            val committedIds = commit.playerTimes.map { it.playerId }.toSet()
            assertEquals(
                "a single commit must carry the outgoing, incoming and remaining pitch players",
                setOf("player-out", "player-in", "player-stays"),
                committedIds,
            )
            assertTrue(
                "every committed player time must carry the new operation id",
                commit.playerTimes.all { it.lastOperationId == operationId },
            )
            assertEquals(
                "the match must advance to the same operation id in that very commit",
                operationId,
                commit.lastCompletedOperationId,
            )
        }

    /**
     * #416 — Verifies that when the atomic write fails, the operation is marked FAILED and
     * lastCompletedOperationId is NOT advanced.
     *
     * Regression: currently there is no try/catch around the write path, so an exception
     * propagates without marking the operation FAILED. This test fails until T004 wraps
     * commitOperation in a try/catch that calls updateOperation(FAILED) on error.
     */
    @Test
    fun `#416 when commitOperation throws during substitution, operation is marked FAILED and lastCompletedOperationId is not advanced`() =
        runTest {
            // Given
            val matchId = "1"
            val currentTimeMillis = System.currentTimeMillis()
            val operationId = "op-fail-416"
            val match =
                buildMatch(
                    matchId,
                    startTimeMillis = currentTimeMillis - 60_000L,
                    squadCallUpIds = listOf("player-out", "player-in"),
                )
            coEvery { matchRepository.getMatchById(matchId) } returns flowOf(match)
            coEvery { matchOperationRepository.createOperation(any()) } returns operationId
            coEvery { getAllPlayerTimesUseCase(matchId) } returns
                flowOf(listOf(PlayerTime(playerId = "player-out", status = PlayerTimeStatus.PLAYING, isRunning = true)))
            coEvery {
                playerTimeRepository.prepareSubstituteOutPlayersBatch(matchId, listOf("player-out"), currentTimeMillis, operationId)
            } returns listOf(PlayerTime(playerId = "player-out", status = PlayerTimeStatus.ON_BENCH, isRunning = false))
            coEvery {
                playerTimeRepository.prepareStartTimersBatch(matchId, listOf("player-in"), currentTimeMillis, operationId)
            } returns listOf(PlayerTime(playerId = "player-in", status = PlayerTimeStatus.PLAYING, isRunning = true))

            // The atomic write fails
            coEvery { matchOperationRepository.commitOperation(any()) } throws RuntimeException("Firestore write failed")

            // When — exception is expected to propagate
            var caughtException: Throwable? = null
            try {
                registerPlayerSubstitutionUseCase(
                    matchId,
                    listOf(SubstitutionPair(playerOutId = "player-out", playerInId = "player-in")),
                    currentTimeMillis,
                )
            } catch (e: RuntimeException) {
                caughtException = e
            }

            // Then — operation is marked FAILED; lastCompletedOperationId is NOT advanced
            coVerify(exactly = 1) {
                matchOperationRepository.updateOperation(
                    match { it.id == operationId && it.status == MatchOperationStatus.FAILED },
                )
            }
            coVerify(exactly = 0) { matchRepository.updateLastCompletedOperationId(any(), any()) }
            @Suppress("DEPRECATION")
            coVerify(exactly = 0) { matchRepository.updateMatchWithOperationId(any(), any()) }
            assertTrue("exception must propagate to caller", caughtException != null)
        }
}
