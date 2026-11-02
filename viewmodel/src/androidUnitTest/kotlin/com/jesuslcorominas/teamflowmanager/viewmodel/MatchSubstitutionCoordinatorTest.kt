package com.jesuslcorominas.teamflowmanager.viewmodel

import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker
import com.jesuslcorominas.teamflowmanager.domain.analytics.CrashReporter
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionBatchResult
import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionPair
import com.jesuslcorominas.teamflowmanager.domain.usecase.AddPendingSubstitutionUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.ClearPendingSubstitutionsUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.ObservePendingSubstitutionsUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.ObserveSubstitutionModeUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.RegisterPlayerSubstitutionUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.RemovePendingSubstitutionUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SetShouldShowInvalidSubstitutionAlertUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.ShouldShowInvalidSubstitutionAlertUseCase
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * What a batch run leaves behind — covered on the coordinator itself rather than through
 * [MatchViewModel], because the two properties under test are about this class alone: that a batch
 * which blows up still says so, and that it unschedules nothing when it does.
 */
class MatchSubstitutionCoordinatorTest {
    private lateinit var registerPlayerSubstitutionUseCase: RegisterPlayerSubstitutionUseCase
    private lateinit var observeSubstitutionModeUseCase: ObserveSubstitutionModeUseCase
    private lateinit var observePendingSubstitutionsUseCase: ObservePendingSubstitutionsUseCase
    private lateinit var addPendingSubstitutionUseCase: AddPendingSubstitutionUseCase
    private lateinit var removePendingSubstitutionUseCase: RemovePendingSubstitutionUseCase
    private lateinit var clearPendingSubstitutionsUseCase: ClearPendingSubstitutionsUseCase
    private lateinit var shouldShowInvalidSubstitutionAlertUseCase: ShouldShowInvalidSubstitutionAlertUseCase
    private lateinit var setShouldShowInvalidSubstitutionAlertUseCase: SetShouldShowInvalidSubstitutionAlertUseCase
    private lateinit var analyticsTracker: AnalyticsTracker
    private lateinit var crashReporter: CrashReporter
    private lateinit var coordinator: MatchSubstitutionCoordinator

    @Before
    fun setup() {
        registerPlayerSubstitutionUseCase = mockk(relaxed = true)
        observeSubstitutionModeUseCase = mockk(relaxed = true)
        observePendingSubstitutionsUseCase = mockk(relaxed = true)
        addPendingSubstitutionUseCase = mockk(relaxed = true)
        removePendingSubstitutionUseCase = mockk(relaxed = true)
        clearPendingSubstitutionsUseCase = mockk(relaxed = true)
        shouldShowInvalidSubstitutionAlertUseCase = mockk(relaxed = true)
        setShouldShowInvalidSubstitutionAlertUseCase = mockk(relaxed = true)
        analyticsTracker = mockk(relaxed = true)
        crashReporter = mockk(relaxed = true)
        coordinator =
            MatchSubstitutionCoordinator(
                matchId = MATCH_ID,
                registerPlayerSubstitutionUseCase = registerPlayerSubstitutionUseCase,
                observeSubstitutionModeUseCase = observeSubstitutionModeUseCase,
                observePendingSubstitutionsUseCase = observePendingSubstitutionsUseCase,
                addPendingSubstitutionUseCase = addPendingSubstitutionUseCase,
                removePendingSubstitutionUseCase = removePendingSubstitutionUseCase,
                clearPendingSubstitutionsUseCase = clearPendingSubstitutionsUseCase,
                shouldShowInvalidSubstitutionAlertUseCase = shouldShowInvalidSubstitutionAlertUseCase,
                setShouldShowInvalidSubstitutionAlertUseCase = setShouldShowInvalidSubstitutionAlertUseCase,
                analyticsTracker = analyticsTracker,
                crashReporter = crashReporter,
            )
    }

    @Test
    fun `a batch that throws publishes a failed result instead of staying silent`() =
        runTest {
            // Given
            coEvery { registerPlayerSubstitutionUseCase(any(), any(), any()) } throws
                IllegalStateException("no active match found")

            // When
            coordinator.run(
                pairs = listOf(PAIR_1_2),
                trigger = SubstitutionExecutionTrigger.MANUAL,
                squadPlayers = SQUAD,
                currentTimeMillis = 0L,
            )

            // Then — the batch used to fail in silence, which from the outside was
            // indistinguishable from the button not working
            val result = requireNotNull(coordinator.lastSubstitutionResult.value)
            assertTrue(result.failed)
            assertEquals(emptyList<PendingSubstitutionItem>(), result.applied)
            assertEquals(emptyList<DiscardedSubstitutionItem>(), result.discarded)
            assertEquals(SubstitutionExecutionTrigger.MANUAL, result.trigger)
        }

    @Test
    fun `a failure on a resume run keeps its trigger - nobody was watching that one`() =
        runTest {
            // Given
            coEvery { registerPlayerSubstitutionUseCase(any(), any(), any()) } throws
                IllegalStateException("no active match found")

            // When
            coordinator.run(
                pairs = listOf(PAIR_1_2),
                trigger = SubstitutionExecutionTrigger.RESUME,
                squadPlayers = SQUAD,
                currentTimeMillis = 0L,
            )

            // Then — the resume wording is the one the coach needs most here: nobody was
            // watching, so without it they would assume the queue had applied itself
            val result = requireNotNull(coordinator.lastSubstitutionResult.value)
            assertTrue(result.failed)
            assertEquals(SubstitutionExecutionTrigger.RESUME, result.trigger)
        }

    /**
     * The invariant the swallowed exception rests on: nothing was lost, so the coach can press
     * again. It holds only because the calls to `removePendingSubstitutionUseCase` sit *after* the
     * use case inside the `try`. Reorder them and this test goes red — which is the whole point of
     * it existing, since until now nothing watched that ordering.
     */
    @Test
    fun `a batch that throws unschedules nothing - which is what lets it be swallowed`() =
        runTest {
            // Given
            coEvery { registerPlayerSubstitutionUseCase(any(), any(), any()) } throws
                IllegalStateException("no active match found")

            // When
            coordinator.run(
                pairs = listOf(PAIR_1_2, PAIR_3_4),
                trigger = SubstitutionExecutionTrigger.RESUME,
                squadPlayers = SQUAD,
                currentTimeMillis = 0L,
            )

            // Then
            verify(exactly = 0) { removePendingSubstitutionUseCase(any(), any()) }
        }

    @Test
    fun `a batch that throws is reported and clears the spinner without bringing the app down`() =
        runTest {
            // Given
            val boom = IllegalStateException("no active match found")
            coEvery { registerPlayerSubstitutionUseCase(any(), any(), any()) } throws boom

            // When — no runCatching: reaching the assertions at all proves it was not rethrown,
            // which is deliberate. Rethrowing inside viewModelScope takes the app down.
            coordinator.run(
                pairs = listOf(PAIR_1_2),
                trigger = SubstitutionExecutionTrigger.MANUAL,
                squadPlayers = SQUAD,
                currentTimeMillis = 0L,
            )

            // Then
            verify { crashReporter.recordException(boom) }
            assertFalse(coordinator.isSubstitutionInProgress.value)
        }

    @Test
    fun `a batch that succeeds is not marked as failed`() =
        runTest {
            // Given
            coEvery { registerPlayerSubstitutionUseCase(any(), any(), any()) } returns
                SubstitutionBatchResult(applied = listOf(PAIR_1_2), discarded = emptyList())

            // When
            coordinator.run(
                pairs = listOf(PAIR_1_2),
                trigger = SubstitutionExecutionTrigger.MANUAL,
                squadPlayers = SQUAD,
                currentTimeMillis = 0L,
            )

            // Then
            val result = requireNotNull(coordinator.lastSubstitutionResult.value)
            assertFalse(result.failed)
            assertEquals(listOf(PAIR_1_2), result.applied.map { it.pair })
        }

    /**
     * The contract the screen relies on to survive a rotation: the result stays put until somebody
     * says they have shown it. If the coordinator dropped it on its own, a recreated screen would
     * have nothing left to paint.
     */
    @Test
    fun `a published result stays put until it is explicitly consumed`() =
        runTest {
            // Given
            coEvery { registerPlayerSubstitutionUseCase(any(), any(), any()) } returns
                SubstitutionBatchResult(applied = listOf(PAIR_1_2), discarded = emptyList())
            coordinator.run(
                pairs = listOf(PAIR_1_2),
                trigger = SubstitutionExecutionTrigger.RESUME,
                squadPlayers = SQUAD,
                currentTimeMillis = 0L,
            )
            assertNotNull(coordinator.lastSubstitutionResult.value)

            // When
            coordinator.consumeLastResult()

            // Then
            assertNull(coordinator.lastSubstitutionResult.value)
        }

    companion object {
        private const val MATCH_ID = "1"
        private val PAIR_1_2 = SubstitutionPair(playerOutId = "1", playerInId = "2")
        private val PAIR_3_4 = SubstitutionPair(playerOutId = "3", playerInId = "4")
        private val SQUAD = listOf(player("1"), player("2"), player("3"), player("4"))

        private fun player(id: String) =
            Player(
                id = id,
                firstName = "Player",
                lastName = id,
                number = id.toInt(),
                positions = listOf(Position.Midfielder),
                teamId = "team-1",
                isCaptain = false,
            )
    }
}
