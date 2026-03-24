package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerTimeDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTime
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeStatus
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PlayerTimeRepositoryImplTest {
    private lateinit var playerTimeDataSource: PlayerTimeDataSource
    private lateinit var repository: PlayerTimeRepositoryImpl

    @Before
    fun setup() {
        playerTimeDataSource = mockk(relaxed = true)
        repository = PlayerTimeRepositoryImpl(playerTimeDataSource)
    }

    @Test
    fun `getPlayerTime should return player time from local data source`() =
        runTest {
            // Given
            val playerId = 1L
            val playerTime = PlayerTime(playerId = playerId, elapsedTimeMillis = 5000L, isRunning = true)
            every { playerTimeDataSource.getPlayerTime(playerId) } returns flowOf(playerTime)

            // When
            val result = repository.getPlayerTime(playerId).first()

            // Then
            assertEquals(playerTime, result)
        }

    @Test
    fun `getPlayerTimesByMatch should return player times from data source`() =
        runTest {
            // Given
            val playerTimes =
                listOf(
                    PlayerTime(playerId = 1L, elapsedTimeMillis = 5000L, isRunning = true),
                    PlayerTime(playerId = 2L, elapsedTimeMillis = 3000L, isRunning = false),
                )
            every { playerTimeDataSource.getPlayerTimesByMatch(MATCH_ID) } returns flowOf(playerTimes)

            // When
            val result = repository.getPlayerTimesByMatch(MATCH_ID).first()

            // Then
            assertEquals(playerTimes, result)
        }

    @Test
    fun `startTimer should create new player time when none exists`() =
        runTest {
            // Given
            val playerId = 1L
            val currentTime = 1000L
            every { playerTimeDataSource.getPlayerTime(playerId) } returns flowOf(null)
            coEvery { playerTimeDataSource.upsertPlayerTime(any()) } returns Unit

            // When
            repository.startTimer(MATCH_ID, playerId, currentTime)

            // Then
            coVerify {
                playerTimeDataSource.upsertPlayerTime(
                    match {
                        it.playerId == playerId &&
                            it.elapsedTimeMillis == 0L &&
                            it.isRunning &&
                            it.lastStartTimeMillis == currentTime
                    },
                )
            }
        }

    @Test
    fun `startTimer should update existing player time to running state`() =
        runTest {
            // Given
            val playerId = 1L
            val currentTime = 2000L
            val existingPlayerTime = PlayerTime(playerId = playerId, elapsedTimeMillis = 5000L, isRunning = false)
            every { playerTimeDataSource.getPlayerTime(playerId) } returns flowOf(existingPlayerTime)
            coEvery { playerTimeDataSource.upsertPlayerTime(any()) } returns Unit

            // When
            repository.startTimer(MATCH_ID, playerId, currentTime)

            // Then
            coVerify {
                playerTimeDataSource.upsertPlayerTime(
                    match {
                        it.playerId == playerId &&
                            it.elapsedTimeMillis == 5000L &&
                            it.isRunning &&
                            it.lastStartTimeMillis == currentTime
                    },
                )
            }
        }

    @Test
    fun `pauseTimer should update elapsed time and set running to false`() =
        runTest {
            // Given
            val playerId = 1L
            val startTime = 1000L
            val pauseTime = 3000L
            val existingPlayerTime =
                PlayerTime(
                    playerId = playerId,
                    elapsedTimeMillis = 2000L,
                    isRunning = true,
                    lastStartTimeMillis = startTime,
                )
            every { playerTimeDataSource.getPlayerTime(playerId) } returns flowOf(existingPlayerTime)
            coEvery { playerTimeDataSource.upsertPlayerTime(any()) } returns Unit

            // When
            repository.pauseTimer(playerId, pauseTime)

            // Then
            coVerify {
                playerTimeDataSource.upsertPlayerTime(
                    match {
                        it.playerId == playerId &&
                            it.elapsedTimeMillis == 4000L &&
                            !it.isRunning &&
                            it.lastStartTimeMillis == null
                    },
                )
            }
        }

    @Test
    fun `pauseTimer should do nothing when player time is not running`() =
        runTest {
            // Given
            val playerId = 1L
            val pauseTime = 3000L
            val existingPlayerTime = PlayerTime(playerId = playerId, elapsedTimeMillis = 2000L, isRunning = false)
            every { playerTimeDataSource.getPlayerTime(playerId) } returns flowOf(existingPlayerTime)

            // When
            repository.pauseTimer(playerId, pauseTime)

            // Then
            coVerify(exactly = 0) { playerTimeDataSource.upsertPlayerTime(any()) }
        }

    @Test
    fun `pauseTimer should do nothing when no player time exists`() =
        runTest {
            // Given
            val playerId = 1L
            val pauseTime = 3000L
            every { playerTimeDataSource.getPlayerTime(playerId) } returns flowOf(null)

            // When
            repository.pauseTimer(playerId, pauseTime)

            // Then
            coVerify(exactly = 0) { playerTimeDataSource.upsertPlayerTime(any()) }
        }

    @Test
    fun `startTimer should be able to restart timer after pause`() =
        runTest {
            // Given
            val playerId = 1L
            val firstStartTime = 1000L
            val pauseTime = 3000L
            val secondStartTime = 5000L

            // First start
            every { playerTimeDataSource.getPlayerTime(playerId) } returns flowOf(null)
            coEvery { playerTimeDataSource.upsertPlayerTime(any()) } returns Unit
            repository.startTimer(MATCH_ID, playerId, firstStartTime)

            // Pause
            val runningPlayerTime =
                PlayerTime(
                    playerId = playerId,
                    elapsedTimeMillis = 0L,
                    isRunning = true,
                    lastStartTimeMillis = firstStartTime,
                )
            every { playerTimeDataSource.getPlayerTime(playerId) } returns flowOf(runningPlayerTime)
            repository.pauseTimer(playerId, pauseTime)

            // Second start
            val pausedPlayerTime = PlayerTime(playerId = playerId, elapsedTimeMillis = 2000L, isRunning = false)
            every { playerTimeDataSource.getPlayerTime(playerId) } returns flowOf(pausedPlayerTime)

            // When
            repository.startTimer(MATCH_ID, playerId, secondStartTime)

            // Then
            coVerify {
                playerTimeDataSource.upsertPlayerTime(
                    match {
                        it.playerId == playerId &&
                            it.elapsedTimeMillis == 2000L &&
                            it.isRunning &&
                            it.lastStartTimeMillis == secondStartTime
                    },
                )
            }
        }

    @Test
    fun `resetAllPlayerTimes should delete all player times from local data source`() =
        runTest {
            // Given
            coEvery { playerTimeDataSource.deleteAllPlayerTimes() } returns Unit

            // When
            repository.resetAllPlayerTimes()

            // Then
            coVerify { playerTimeDataSource.deleteAllPlayerTimes() }
        }

    @Test
    fun `givenRunningPlayerTimeWithNullLastStartTime_whenPauseTimer_thenUsesCurrentTimeAsStartReference`() = runTest {
        val playerId = 1L
        val currentTime = 3000L
        val existingPlayerTime = PlayerTime(
            playerId = playerId,
            elapsedTimeMillis = 2000L,
            isRunning = true,
            lastStartTimeMillis = null,
        )
        every { playerTimeDataSource.getPlayerTime(playerId) } returns flowOf(existingPlayerTime)

        repository.pauseTimer(playerId, currentTime)

        coVerify {
            playerTimeDataSource.upsertPlayerTime(
                match {
                    it.playerId == playerId &&
                        it.elapsedTimeMillis == 2000L && // 2000 + (3000 - 3000) = 2000
                        !it.isRunning &&
                        it.lastStartTimeMillis == null &&
                        it.status == PlayerTimeStatus.ON_BENCH
                },
            )
        }
    }

    // --- pauseTimerForMatchPause ---

    @Test
    fun `givenRunningPlayerTime_whenPauseTimerForMatchPause_thenAccumulatesTimeAndSetsStatusPausedKeepingLastStartTime`() = runTest {
        val playerId = 1L
        val startTime = 1000L
        val pauseTime = 4000L
        val existingPlayerTime = PlayerTime(
            playerId = playerId,
            elapsedTimeMillis = 2000L,
            isRunning = true,
            lastStartTimeMillis = startTime,
        )
        every { playerTimeDataSource.getPlayerTime(playerId) } returns flowOf(existingPlayerTime)

        repository.pauseTimerForMatchPause(playerId, pauseTime)

        coVerify {
            playerTimeDataSource.upsertPlayerTime(
                match {
                    it.playerId == playerId &&
                        it.elapsedTimeMillis == 5000L &&
                        !it.isRunning &&
                        it.lastStartTimeMillis == startTime &&
                        it.status == PlayerTimeStatus.PAUSED
                },
            )
        }
    }

    @Test
    fun `givenRunningPlayerTimeWithNullLastStartTime_whenPauseTimerForMatchPause_thenUsesCurrentTimeAsStartReference`() = runTest {
        val playerId = 1L
        val currentTime = 4000L
        val existingPlayerTime = PlayerTime(
            playerId = playerId,
            elapsedTimeMillis = 1000L,
            isRunning = true,
            lastStartTimeMillis = null,
        )
        every { playerTimeDataSource.getPlayerTime(playerId) } returns flowOf(existingPlayerTime)

        repository.pauseTimerForMatchPause(playerId, currentTime)

        coVerify {
            playerTimeDataSource.upsertPlayerTime(
                match {
                    it.playerId == playerId &&
                        it.elapsedTimeMillis == 1000L && // 1000 + (4000 - 4000) = 1000
                        !it.isRunning &&
                        it.lastStartTimeMillis == currentTime &&
                        it.status == PlayerTimeStatus.PAUSED
                },
            )
        }
    }

    @Test
    fun `givenNotRunningPlayerTime_whenPauseTimerForMatchPause_thenDoesNothing`() = runTest {
        val playerId = 1L
        val existingPlayerTime = PlayerTime(playerId = playerId, elapsedTimeMillis = 2000L, isRunning = false)
        every { playerTimeDataSource.getPlayerTime(playerId) } returns flowOf(existingPlayerTime)

        repository.pauseTimerForMatchPause(playerId, 3000L)

        coVerify(exactly = 0) { playerTimeDataSource.upsertPlayerTime(any()) }
    }

    @Test
    fun `givenNullPlayerTime_whenPauseTimerForMatchPause_thenDoesNothing`() = runTest {
        every { playerTimeDataSource.getPlayerTime(1L) } returns flowOf(null)

        repository.pauseTimerForMatchPause(1L, 3000L)

        coVerify(exactly = 0) { playerTimeDataSource.upsertPlayerTime(any()) }
    }

    // --- startTimersBatch ---

    @Test
    fun `givenEmptyPlayerIdList_whenStartTimersBatch_thenDoesNothing`() = runTest {
        repository.startTimersBatch(MATCH_ID, emptyList(), 1000L)

        coVerify(exactly = 0) { playerTimeDataSource.getPlayerTimesByMatch(any()) }
        coVerify(exactly = 0) { playerTimeDataSource.batchUpsertPlayerTimes(any()) }
    }

    @Test
    fun `givenPlayerIdsWithNoExistingTimes_whenStartTimersBatch_thenCreatesNewPlayerTimesAsPlaying`() = runTest {
        val playerIds = listOf(1L, 2L)
        val currentTime = 5000L
        every { playerTimeDataSource.getPlayerTimesByMatch(any()) } returns flowOf(emptyList())

        repository.startTimersBatch(MATCH_ID, playerIds, currentTime)

        coVerify {
            playerTimeDataSource.batchUpsertPlayerTimes(
                match { list ->
                    list.size == 2 &&
                        list.all {
                            it.isRunning &&
                                it.lastStartTimeMillis == currentTime &&
                                it.elapsedTimeMillis == 0L &&
                                it.status == PlayerTimeStatus.PLAYING
                        }
                },
            )
        }
    }

    @Test
    fun `givenPlayerIdsWithExistingTimes_whenStartTimersBatch_thenUpdatesExistingTimesPreservingElapsed`() = runTest {
        val currentTime = 5000L
        val existingTimes = listOf(
            PlayerTime(playerId = 1L, elapsedTimeMillis = 2000L, isRunning = false),
            PlayerTime(playerId = 2L, elapsedTimeMillis = 3000L, isRunning = false),
        )
        every { playerTimeDataSource.getPlayerTimesByMatch(any()) } returns flowOf(existingTimes)

        repository.startTimersBatch(MATCH_ID, listOf(1L, 2L), currentTime)

        coVerify {
            playerTimeDataSource.batchUpsertPlayerTimes(
                match { list ->
                    list.size == 2 &&
                        list.all { it.isRunning && it.lastStartTimeMillis == currentTime } &&
                        list.find { it.playerId == 1L }?.elapsedTimeMillis == 2000L &&
                        list.find { it.playerId == 2L }?.elapsedTimeMillis == 3000L
                },
            )
        }
    }

    // --- pauseTimersBatch ---

    @Test
    fun `givenEmptyPlayerIdList_whenPauseTimersBatch_thenDoesNothing`() = runTest {
        repository.pauseTimersBatch(MATCH_ID, emptyList(), 1000L)

        coVerify(exactly = 0) { playerTimeDataSource.batchUpsertPlayerTimes(any()) }
    }

    @Test
    fun `givenMixedRunningAndNotRunningPlayers_whenPauseTimersBatch_thenPausesOnlyRunningPlayers`() = runTest {
        val currentTime = 6000L
        val allTimes = listOf(
            PlayerTime(playerId = 1L, elapsedTimeMillis = 0L, isRunning = true, lastStartTimeMillis = 1000L),
            PlayerTime(playerId = 2L, elapsedTimeMillis = 1000L, isRunning = false),
        )
        every { playerTimeDataSource.getPlayerTimesByMatch(any()) } returns flowOf(allTimes)

        repository.pauseTimersBatch(MATCH_ID, listOf(1L, 2L), currentTime)

        coVerify {
            playerTimeDataSource.batchUpsertPlayerTimes(
                match { list ->
                    list.size == 1 &&
                        list[0].playerId == 1L &&
                        list[0].elapsedTimeMillis == 5000L && // 0 + (6000 - 1000)
                        !list[0].isRunning &&
                        list[0].status == PlayerTimeStatus.PAUSED
                },
            )
        }
    }

    @Test
    fun `givenRunningPlayerWithNullLastStartTime_whenPauseTimersBatch_thenUsesCurrentTimeAsStartReference`() = runTest {
        val currentTime = 6000L
        val allTimes = listOf(
            PlayerTime(playerId = 1L, elapsedTimeMillis = 500L, isRunning = true, lastStartTimeMillis = null),
        )
        every { playerTimeDataSource.getPlayerTimesByMatch(any()) } returns flowOf(allTimes)

        repository.pauseTimersBatch(MATCH_ID, listOf(1L), currentTime)

        coVerify {
            playerTimeDataSource.batchUpsertPlayerTimes(
                match { list ->
                    list.size == 1 &&
                        list[0].elapsedTimeMillis == 500L && // 500 + (6000 - 6000) = 500
                        !list[0].isRunning &&
                        list[0].status == PlayerTimeStatus.PAUSED
                },
            )
        }
    }

    @Test
    fun `givenAllNonRunningPlayers_whenPauseTimersBatch_thenDoesNotCallBatchUpsert`() = runTest {
        val allTimes = listOf(
            PlayerTime(playerId = 1L, elapsedTimeMillis = 2000L, isRunning = false),
            PlayerTime(playerId = 2L, elapsedTimeMillis = 1000L, isRunning = false),
        )
        every { playerTimeDataSource.getPlayerTimesByMatch(any()) } returns flowOf(allTimes)

        repository.pauseTimersBatch(MATCH_ID, listOf(1L, 2L), 6000L)

        coVerify(exactly = 0) { playerTimeDataSource.batchUpsertPlayerTimes(any()) }
    }

    // --- startTimersBatchWithOperationId ---

    @Test
    fun `givenEmptyPlayerIdList_whenStartTimersBatchWithOperationId_thenDoesNothing`() = runTest {
        repository.startTimersBatchWithOperationId(MATCH_ID, emptyList(), 1000L, "op-1")

        coVerify(exactly = 0) { playerTimeDataSource.batchUpsertPlayerTimes(any()) }
    }

    @Test
    fun `givenNewPlayerIds_whenStartTimersBatchWithOperationId_thenCreatesTimesWithOperationId`() = runTest {
        val currentTime = 5000L
        val operationId = "op-start-1"
        every { playerTimeDataSource.getPlayerTimesByMatch(any()) } returns flowOf(emptyList())

        repository.startTimersBatchWithOperationId(MATCH_ID, listOf(1L), currentTime, operationId)

        coVerify {
            playerTimeDataSource.batchUpsertPlayerTimes(
                match { list ->
                    list.size == 1 &&
                        list[0].isRunning &&
                        list[0].lastStartTimeMillis == currentTime &&
                        list[0].lastOperationId == operationId
                },
            )
        }
    }

    @Test
    fun `givenExistingPlayerTime_whenStartTimersBatchWithOperationId_thenUpdatesWithOperationIdPreservingElapsed`() = runTest {
        val currentTime = 5000L
        val operationId = "op-start-2"
        val existingTime = PlayerTime(playerId = 1L, elapsedTimeMillis = 1000L, isRunning = false)
        every { playerTimeDataSource.getPlayerTimesByMatch(any()) } returns flowOf(listOf(existingTime))

        repository.startTimersBatchWithOperationId(MATCH_ID, listOf(1L), currentTime, operationId)

        coVerify {
            playerTimeDataSource.batchUpsertPlayerTimes(
                match { list ->
                    list.size == 1 &&
                        list[0].isRunning &&
                        list[0].elapsedTimeMillis == 1000L &&
                        list[0].lastOperationId == operationId
                },
            )
        }
    }

    // --- pauseTimersBatchWithOperationId ---

    @Test
    fun `givenEmptyPlayerIdList_whenPauseTimersBatchWithOperationId_thenDoesNothing`() = runTest {
        repository.pauseTimersBatchWithOperationId(MATCH_ID, emptyList(), 1000L, "op-1")

        coVerify(exactly = 0) { playerTimeDataSource.batchUpsertPlayerTimes(any()) }
    }

    @Test
    fun `givenRunningPlayers_whenPauseTimersBatchWithOperationId_thenPausesWithOperationId`() = runTest {
        val currentTime = 6000L
        val operationId = "op-pause-1"
        val allTimes = listOf(
            PlayerTime(playerId = 1L, elapsedTimeMillis = 0L, isRunning = true, lastStartTimeMillis = 1000L),
        )
        every { playerTimeDataSource.getPlayerTimesByMatch(any()) } returns flowOf(allTimes)

        repository.pauseTimersBatchWithOperationId(MATCH_ID, listOf(1L), currentTime, operationId)

        coVerify {
            playerTimeDataSource.batchUpsertPlayerTimes(
                match { list ->
                    list.size == 1 &&
                        list[0].playerId == 1L &&
                        list[0].elapsedTimeMillis == 5000L && // 0 + (6000 - 1000)
                        !list[0].isRunning &&
                        list[0].status == PlayerTimeStatus.PAUSED &&
                        list[0].lastOperationId == operationId
                },
            )
        }
    }

    @Test
    fun `givenRunningPlayerWithNullLastStartTime_whenPauseTimersBatchWithOperationId_thenUsesCurrentTimeAsStartReference`() = runTest {
        val currentTime = 6000L
        val operationId = "op-pause-null"
        val allTimes = listOf(
            PlayerTime(playerId = 1L, elapsedTimeMillis = 500L, isRunning = true, lastStartTimeMillis = null),
        )
        every { playerTimeDataSource.getPlayerTimesByMatch(any()) } returns flowOf(allTimes)

        repository.pauseTimersBatchWithOperationId(MATCH_ID, listOf(1L), currentTime, operationId)

        coVerify {
            playerTimeDataSource.batchUpsertPlayerTimes(
                match { list ->
                    list.size == 1 &&
                        list[0].elapsedTimeMillis == 500L && // 500 + (6000 - 6000) = 500
                        !list[0].isRunning &&
                        list[0].status == PlayerTimeStatus.PAUSED &&
                        list[0].lastOperationId == operationId
                },
            )
        }
    }

    @Test
    fun `givenNonRunningPlayers_whenPauseTimersBatchWithOperationId_thenDoesNotCallBatchUpsert`() = runTest {
        val allTimes = listOf(
            PlayerTime(playerId = 1L, elapsedTimeMillis = 2000L, isRunning = false),
        )
        every { playerTimeDataSource.getPlayerTimesByMatch(any()) } returns flowOf(allTimes)

        repository.pauseTimersBatchWithOperationId(MATCH_ID, listOf(1L), 6000L, "op-1")

        coVerify(exactly = 0) { playerTimeDataSource.batchUpsertPlayerTimes(any()) }
    }

    // --- substituteOutPlayersBatchWithOperationId ---

    @Test
    fun `givenEmptyPlayerIdList_whenSubstituteOutPlayersBatchWithOperationId_thenDoesNothing`() = runTest {
        repository.substituteOutPlayersBatchWithOperationId(MATCH_ID, emptyList(), 1000L, "op-1")

        coVerify(exactly = 0) { playerTimeDataSource.batchUpsertPlayerTimes(any()) }
    }

    @Test
    fun `givenRunningPlayers_whenSubstituteOutPlayersBatchWithOperationId_thenSetsOnBenchWithNullLastStartTime`() = runTest {
        val currentTime = 6000L
        val operationId = "op-sub-1"
        val allTimes = listOf(
            PlayerTime(playerId = 1L, elapsedTimeMillis = 0L, isRunning = true, lastStartTimeMillis = 1000L),
        )
        every { playerTimeDataSource.getPlayerTimesByMatch(any()) } returns flowOf(allTimes)

        repository.substituteOutPlayersBatchWithOperationId(MATCH_ID, listOf(1L), currentTime, operationId)

        coVerify {
            playerTimeDataSource.batchUpsertPlayerTimes(
                match { list ->
                    list.size == 1 &&
                        !list[0].isRunning &&
                        list[0].status == PlayerTimeStatus.ON_BENCH &&
                        list[0].lastStartTimeMillis == null &&
                        list[0].elapsedTimeMillis == 5000L && // 0 + (6000 - 1000)
                        list[0].lastOperationId == operationId
                },
            )
        }
    }

    @Test
    fun `givenRunningPlayerWithNullLastStartTime_whenSubstituteOutPlayersBatchWithOperationId_thenUsesCurrentTimeAsStartReference`() = runTest {
        val currentTime = 6000L
        val operationId = "op-sub-null"
        val allTimes = listOf(
            PlayerTime(playerId = 1L, elapsedTimeMillis = 500L, isRunning = true, lastStartTimeMillis = null),
        )
        every { playerTimeDataSource.getPlayerTimesByMatch(any()) } returns flowOf(allTimes)

        repository.substituteOutPlayersBatchWithOperationId(MATCH_ID, listOf(1L), currentTime, operationId)

        coVerify {
            playerTimeDataSource.batchUpsertPlayerTimes(
                match { list ->
                    list.size == 1 &&
                        list[0].elapsedTimeMillis == 500L && // 500 + (6000 - 6000) = 500
                        !list[0].isRunning &&
                        list[0].status == PlayerTimeStatus.ON_BENCH &&
                        list[0].lastStartTimeMillis == null &&
                        list[0].lastOperationId == operationId
                },
            )
        }
    }

    @Test
    fun `givenNonRunningPlayers_whenSubstituteOutPlayersBatchWithOperationId_thenDoesNotCallBatchUpsert`() = runTest {
        val allTimes = listOf(
            PlayerTime(playerId = 1L, elapsedTimeMillis = 2000L, isRunning = false),
        )
        every { playerTimeDataSource.getPlayerTimesByMatch(any()) } returns flowOf(allTimes)

        repository.substituteOutPlayersBatchWithOperationId(MATCH_ID, listOf(1L), 6000L, "op-1")

        coVerify(exactly = 0) { playerTimeDataSource.batchUpsertPlayerTimes(any()) }
    }

    companion object {
        private const val MATCH_ID = 1L
    }
}
