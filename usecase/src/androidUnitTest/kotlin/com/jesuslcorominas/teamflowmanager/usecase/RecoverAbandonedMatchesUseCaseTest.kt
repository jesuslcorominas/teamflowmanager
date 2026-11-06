package com.jesuslcorominas.teamflowmanager.usecase

import com.jesuslcorominas.teamflowmanager.domain.analytics.AbandonedMatchRecoveredException
import com.jesuslcorominas.teamflowmanager.domain.analytics.CrashReporter
import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchPeriod
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.domain.model.PeriodType
import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionPair
import com.jesuslcorominas.teamflowmanager.domain.usecase.ClearPendingSubstitutionsUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.EndTimeoutUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.FinishMatchUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetAllMatchesUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.GetMatchByIdUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.ObservePendingSubstitutionsUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.RecoverAbandonedMatchesUseCase
import com.jesuslcorominas.teamflowmanager.domain.utils.TimeProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RecoverAbandonedMatchesUseCaseTest {
    private lateinit var getAllMatches: GetAllMatchesUseCase
    private lateinit var getMatchById: GetMatchByIdUseCase
    private lateinit var finishMatch: FinishMatchUseCase
    private lateinit var endTimeout: EndTimeoutUseCase
    private lateinit var observePendingSubstitutions: ObservePendingSubstitutionsUseCase
    private lateinit var clearPendingSubstitutions: ClearPendingSubstitutionsUseCase
    private lateinit var timeProvider: TimeProvider
    private lateinit var crashReporter: CrashReporter
    private lateinit var useCase: RecoverAbandonedMatchesUseCase

    @Before
    fun setup() {
        getAllMatches = mockk(relaxed = true)
        getMatchById = mockk(relaxed = true)
        finishMatch = mockk(relaxed = true)
        endTimeout = mockk(relaxed = true)
        observePendingSubstitutions = mockk(relaxed = true)
        clearPendingSubstitutions = mockk(relaxed = true)
        timeProvider = mockk(relaxed = true)
        crashReporter = mockk(relaxed = true)
        useCase =
            RecoverAbandonedMatchesUseCaseImpl(
                getAllMatches = getAllMatches,
                getMatchById = getMatchById,
                finishMatch = finishMatch,
                endTimeout = endTimeout,
                observePendingSubstitutions = observePendingSubstitutions,
                clearPendingSubstitutions = clearPendingSubstitutions,
                timeProvider = timeProvider,
                crashReporter = crashReporter,
            )
        every { getAllMatches() } returns flowOf(emptyList())
        // Por defecto el finish cumple: el partido se relee ya en FINISHED.
        every { getMatchById(any()) } returns flowOf(baseMatch(MatchStatus.FINISHED, emptyList()))
        every { observePendingSubstitutions(any()) } returns flowOf(emptyList())
        every { timeProvider.getCurrentTime() } returns T0 + SEVEN_HOURS
    }

    @Test
    fun `givenNoMatches_whenInvoke_thenNothingIsFinished`() =
        runTest {
            val recovered = useCase()

            assertEquals(0, recovered)
            coVerify(exactly = 0) { finishMatch(any(), any()) }
            verify(exactly = 0) { clearPendingSubstitutions(any()) }
        }

    /**
     * Regression test for #418. Fails without the fix — nothing finished an abandoned match — and
     * passes with it. Also pins the cap: finishing at the real current time would record seven
     * hours of play for every starter of a 25-minute match.
     */
    @Test
    fun `givenInProgressMatchInactiveForMoreThanThreshold_whenInvoke_thenFinishesWithTimeCappedToPeriodEnd`() =
        runTest {
            val match = inProgressMatch(periodStart = T0)
            every { getAllMatches() } returns flowOf(listOf(match))

            val recovered = useCase()

            assertEquals(1, recovered)
            coVerify(exactly = 1) { finishMatch(MATCH_ID, T0 + PERIOD_DURATION) }
        }

    @Test
    fun `givenPausedMatchAtHalfTime_whenInvoke_thenMatchIsLeftAlone`() =
        runTest {
            val match = pausedMatch(pausedAt = T0)
            every { getAllMatches() } returns flowOf(listOf(match))
            every { timeProvider.getCurrentTime() } returns T0 + TWO_MINUTES

            val recovered = useCase()

            assertEquals(0, recovered)
            coVerify(exactly = 0) { finishMatch(any(), any()) }
            verify(exactly = 0) { clearPendingSubstitutions(any()) }
        }

    @Test
    fun `givenPausedMatchJustUnderTheThreshold_whenInvoke_thenMatchIsLeftAlone`() =
        runTest {
            val match = pausedMatch(pausedAt = T0)
            every { getAllMatches() } returns flowOf(listOf(match))
            every { timeProvider.getCurrentTime() } returns T0 + SIX_HOURS - 1

            useCase()

            coVerify(exactly = 0) { finishMatch(any(), any()) }
        }

    @Test
    fun `givenPausedMatchInactiveForMoreThanThreshold_whenInvoke_thenFinishesAtThePauseInstant`() =
        runTest {
            val match = pausedMatch(pausedAt = T0)
            every { getAllMatches() } returns flowOf(listOf(match))

            useCase()

            coVerify(exactly = 1) { finishMatch(MATCH_ID, T0) }
        }

    @Test
    fun `givenTimeoutMatchInactiveForMoreThanThreshold_whenInvoke_thenEndsTheTimeoutBeforeFinishing`() =
        runTest {
            val periodStart = T0 - TEN_MINUTES
            val match =
                inProgressMatch(periodStart = periodStart).copy(
                    status = MatchStatus.TIMEOUT,
                    timeoutStartTimeMillis = T0,
                )
            every { getAllMatches() } returns flowOf(listOf(match))

            useCase()

            coVerifyOrder {
                endTimeout(MATCH_ID, T0)
                finishMatch(MATCH_ID, periodStart + PERIOD_DURATION)
            }
        }

    @Test
    fun `givenMatchesThatAreNotRunning_whenInvoke_thenNoneIsTouched`() =
        runTest {
            val scheduled = inProgressMatch(periodStart = T0).copy(id = "scheduled", status = MatchStatus.SCHEDULED)
            val finished = inProgressMatch(periodStart = T0).copy(id = "finished", status = MatchStatus.FINISHED)
            val archived = inProgressMatch(periodStart = T0).copy(id = "archived", archived = true)
            every { getAllMatches() } returns flowOf(listOf(scheduled, finished, archived))

            val recovered = useCase()

            assertEquals(0, recovered)
            coVerify(exactly = 0) { finishMatch(any(), any()) }
        }

    @Test
    fun `givenDeviceClockMovedBackwards_whenInvoke_thenMatchIsLeftAlone`() =
        runTest {
            val match = inProgressMatch(periodStart = T0)
            every { getAllMatches() } returns flowOf(listOf(match))
            every { timeProvider.getCurrentTime() } returns T0 - SEVEN_HOURS

            val recovered = useCase()

            assertEquals(0, recovered)
            coVerify(exactly = 0) { finishMatch(any(), any()) }
        }

    @Test
    fun `givenRunningMatchWithNoUsableTimestamp_whenInvoke_thenMatchIsLeftAlone`() =
        runTest {
            val match = inProgressMatch(periodStart = T0).copy(periods = emptyList(), dateTime = null)
            every { getAllMatches() } returns flowOf(listOf(match))

            val recovered = useCase()

            assertEquals(0, recovered)
            coVerify(exactly = 0) { finishMatch(any(), any()) }
        }

    /**
     * El partido califica por su fecha, pero sin períodos la cota no puede salir de ellos. El
     * instante de finish es la hora de convocatoria más la duración nominal completa del partido
     * — nunca `now`, que aquí serían siete horas de juego por titular.
     */
    @Test
    fun `givenRunningMatchWithOnlyADate_whenInvoke_thenFinishesAtTheLatestPlausibleEnd`() =
        runTest {
            val match = inProgressMatch(periodStart = T0).copy(periods = emptyList(), dateTime = T0)
            every { getAllMatches() } returns flowOf(listOf(match))

            val recovered = useCase()

            assertEquals(1, recovered)
            coVerify(exactly = 1) { finishMatch(MATCH_ID, T0 + 2 * PERIOD_DURATION) }
            coVerify(exactly = 0) { finishMatch(MATCH_ID, T0 + SEVEN_HOURS) }
        }

    @Test
    fun `givenAbandonedMatch_whenInvoke_thenThePendingQueueIsCleared`() =
        runTest {
            val match = inProgressMatch(periodStart = T0)
            every { getAllMatches() } returns flowOf(listOf(match))

            useCase()

            coVerifyOrder {
                finishMatch(MATCH_ID, any())
                clearPendingSubstitutions(MATCH_ID)
            }
        }

    @Test
    fun `givenAbandonedMatch_whenInvoke_thenTheRecoveryIsReportedWithDiagnosticContext`() =
        runTest {
            val abandoned = inProgressMatch(periodStart = T0)
            every { getAllMatches() } returns flowOf(listOf(abandoned))
            every { observePendingSubstitutions(MATCH_ID) } returns flowOf(listOf(mockk<SubstitutionPair>(), mockk()))

            useCase()

            verify { crashReporter.setCustomKey("abandoned_match_id", MATCH_ID) }
            verify { crashReporter.setCustomKey("abandoned_match_status", "IN_PROGRESS") }
            verify { crashReporter.setCustomKey("abandoned_inactivity_minutes", 7 * 60) }
            verify { crashReporter.setCustomKey("abandoned_had_pending_substitutions", true) }
            verify { crashReporter.setCustomKey("abandoned_pending_substitutions_count", 2) }
            verify { crashReporter.recordException(ofType(AbandonedMatchRecoveredException::class)) }
            verify { crashReporter.log(match(::mentionsTheWholeContext)) }
        }

    @Test
    fun `givenAbandonedMatchWithAnEmptyQueue_whenInvoke_thenItIsReportedAsHavingNoPendings`() =
        runTest {
            val match = inProgressMatch(periodStart = T0)
            every { getAllMatches() } returns flowOf(listOf(match))

            useCase()

            verify { crashReporter.setCustomKey("abandoned_had_pending_substitutions", false) }
            verify { crashReporter.setCustomKey("abandoned_pending_substitutions_count", 0) }
        }

    @Test
    fun `givenTheQueueCannotBeRead_whenInvoke_thenTheMatchIsStillRecovered`() =
        runTest {
            val match = inProgressMatch(periodStart = T0)
            every { getAllMatches() } returns flowOf(listOf(match))
            every { observePendingSubstitutions(MATCH_ID) } returns flow { throw RuntimeException("no store") }

            val recovered = useCase()

            assertEquals(1, recovered)
            coVerify(exactly = 1) { finishMatch(MATCH_ID, any()) }
            verify { crashReporter.setCustomKey("abandoned_pending_substitutions_count", 0) }
        }

    @Test
    fun `givenTheFinishFails_whenInvoke_thenTheQueueIsNotClearedAndTheFailureIsReported`() =
        runTest {
            val match = inProgressMatch(periodStart = T0)
            every { getAllMatches() } returns flowOf(listOf(match))
            coEvery { finishMatch(MATCH_ID, any()) } throws RuntimeException("boom")

            val recovered = useCase()

            assertEquals(0, recovered)
            verify(exactly = 0) { clearPendingSubstitutions(MATCH_ID) }
            verify { crashReporter.recordException(ofType(RuntimeException::class)) }
        }

    @Test
    fun `givenOneMatchFails_whenInvoke_thenTheOthersAreStillRecovered`() =
        runTest {
            val failing = inProgressMatch(periodStart = T0).copy(id = "failing")
            val healthy = inProgressMatch(periodStart = T0).copy(id = "healthy")
            every { getAllMatches() } returns flowOf(listOf(failing, healthy))
            coEvery { finishMatch("failing", any()) } throws RuntimeException("boom")

            val recovered = useCase()

            assertEquals(1, recovered)
            coVerify(exactly = 1) { finishMatch("healthy", T0 + PERIOD_DURATION) }
            verify(exactly = 1) { clearPendingSubstitutions("healthy") }
        }

    @Test
    fun `givenTheMatchesCannotBeRead_whenInvoke_thenNothingIsFinishedAndTheFailureIsReported`() =
        runTest {
            every { getAllMatches() } returns flow { throw RuntimeException("offline") }

            val recovered = useCase()

            assertEquals(0, recovered)
            coVerify(exactly = 0) { finishMatch(any(), any()) }
            verify { crashReporter.recordException(ofType(RuntimeException::class)) }
        }


    @Test
    fun `givenPausedMatchWithNoClosedPeriod_whenInvoke_thenFinishesAtTheLatestPlausibleEnd`() =
        runTest {
            val match = pausedMatch(pausedAt = T0).copy(periods = emptyList(), dateTime = T0)
            every { getAllMatches() } returns flowOf(listOf(match))

            val recovered = useCase()

            assertEquals(1, recovered)
            coVerify(exactly = 1) { finishMatch(MATCH_ID, T0 + 2 * PERIOD_DURATION) }
            coVerify(exactly = 0) { finishMatch(MATCH_ID, T0 + SEVEN_HOURS) }
        }

    @Test
    fun `givenInProgressMatchWhosePeriodsAreAllClosed_whenInvoke_thenFinishesAtTheLastClosedPeriod`() =
        runTest {
            val closed =
                MatchPeriod(
                    periodNumber = 1,
                    periodDuration = PERIOD_DURATION,
                    startTimeMillis = T0 - PERIOD_DURATION,
                    endTimeMillis = T0,
                )
            val match = inProgressMatch(periodStart = T0).copy(periods = listOf(closed), dateTime = T0)
            every { getAllMatches() } returns flowOf(listOf(match))

            val recovered = useCase()

            assertEquals(1, recovered)
            coVerify(exactly = 1) { finishMatch(MATCH_ID, T0) }
            coVerify(exactly = 0) { finishMatch(MATCH_ID, T0 + SEVEN_HOURS) }
        }

    @Test
    fun `givenTimeoutMatchWithNoTimeoutInstantAndNoDate_whenInvoke_thenMatchIsLeftAlone`() =
        runTest {
            val match =
                inProgressMatch(periodStart = T0).copy(
                    status = MatchStatus.TIMEOUT,
                    timeoutStartTimeMillis = 0L,
                    dateTime = null,
                )
            every { getAllMatches() } returns flowOf(listOf(match))

            val recovered = useCase()

            assertEquals(0, recovered)
            coVerify(exactly = 0) { endTimeout(any(), any()) }
            coVerify(exactly = 0) { finishMatch(any(), any()) }
        }

    @Test
    fun `givenRunningMatchWithAnEmptyDate_whenInvoke_thenMatchIsLeftAlone`() =
        runTest {
            val match = inProgressMatch(periodStart = T0).copy(periods = emptyList(), dateTime = 0L)
            every { getAllMatches() } returns flowOf(listOf(match))

            val recovered = useCase()

            assertEquals(0, recovered)
            coVerify(exactly = 0) { finishMatch(any(), any()) }
        }


    @Test
    fun `givenTheSweepIsCancelled_whenInvoke_thenNothingIsReportedAsAFailure`() =
        runTest {
            every { getAllMatches() } returns flow { throw CancellationException("splash left") }

            val thrown = runCatching { useCase() }.exceptionOrNull()

            assertTrue(thrown is CancellationException)
            verify(exactly = 0) { crashReporter.recordException(any()) }
        }

    @Test
    fun `givenAMatchRecoveryIsCancelled_whenInvoke_thenNothingIsReportedAsAFailure`() =
        runTest {
            val match = inProgressMatch(periodStart = T0)
            every { getAllMatches() } returns flowOf(listOf(match))
            coEvery { finishMatch(any(), any()) } throws CancellationException("splash left")

            val thrown = runCatching { useCase() }.exceptionOrNull()

            assertTrue(thrown is CancellationException)
            verify(exactly = 0) { crashReporter.recordException(any()) }
            verify(exactly = 0) { clearPendingSubstitutions(any()) }
        }


    /** P1 de la revisión: ausencia de excepción no es finish ejecutado. */
    @Test
    fun `givenTheFinishReturnsSilently_whenInvoke_thenTheQueueIsKeptAndNoRecoveryIsReported`() =
        runTest {
            val match = inProgressMatch(periodStart = T0)
            every { getAllMatches() } returns flowOf(listOf(match))
            // El finish no lanza, pero el partido sigue vivo: fue borrado, o ya no estaba en un
            // estado finalizable. Es lo que devuelve FinishMatchUseCase por sus dos return mudos.
            every { getMatchById(MATCH_ID) } returns flowOf(match)

            val recovered = useCase()

            assertEquals(0, recovered)
            verify(exactly = 0) { clearPendingSubstitutions(any()) }
            verify(exactly = 0) { crashReporter.recordException(ofType(AbandonedMatchRecoveredException::class)) }
        }

    @Test
    fun `givenTheMatchDisappearsDuringTheFinish_whenInvoke_thenTheQueueIsKept`() =
        runTest {
            val match = inProgressMatch(periodStart = T0)
            every { getAllMatches() } returns flowOf(listOf(match))
            every { getMatchById(MATCH_ID) } returns flowOf(null)

            val recovered = useCase()

            assertEquals(0, recovered)
            verify(exactly = 0) { clearPendingSubstitutions(any()) }
        }

    /** P1 de la revisión: sin ningún timestamp fiable no se inventa una fecha de finish. */
    @Test
    fun `givenAnUndatableAbandonedMatch_whenInvoke_thenItIsReportedInsteadOfFinished`() =
        runTest {
            val match = inProgressMatch(periodStart = T0).copy(periods = emptyList(), dateTime = 0L, teamId = "t")
            val undatable = match.copy(status = MatchStatus.TIMEOUT, timeoutStartTimeMillis = T0, periods = emptyList())
            every { getAllMatches() } returns flowOf(listOf(undatable))

            val recovered = useCase()

            assertEquals(0, recovered)
            coVerify(exactly = 0) { finishMatch(any(), any()) }
            verify(exactly = 0) { clearPendingSubstitutions(any()) }
            verify { crashReporter.recordException(ofType(AbandonedMatchRecoveredException::class)) }
        }

    /**
     * P2 de la revisión: un tiempo muerto pedido en el descuento arranca los cronómetros DESPUÉS
     * del final nominal del período, así que sin suelo el finish caería por detrás y restaría
     * tiempo de juego a todos los que estaban en el campo.
     */
    @Test
    fun `givenATimeoutAskedForInStoppageTime_whenInvoke_thenTheFinishIsNotBeforeTheTimers`() =
        runTest {
            val periodStart = T0
            val timeoutStart = T0 + PERIOD_DURATION + TWO_MINUTES // minuto 27 de un período de 25
            val match =
                inProgressMatch(periodStart = periodStart).copy(
                    status = MatchStatus.TIMEOUT,
                    timeoutStartTimeMillis = timeoutStart,
                )
            every { getAllMatches() } returns flowOf(listOf(match))
            every { timeProvider.getCurrentTime() } returns timeoutStart + SEVEN_HOURS

            useCase()

            coVerify(exactly = 1) { finishMatch(MATCH_ID, timeoutStart) }
        }

    /** P2 de la revisión: el `=` de `>=` no lo verificaba ningún test. */
    @Test
    fun `givenInactivityExactlyAtTheThreshold_whenInvoke_thenTheMatchIsRecovered`() =
        runTest {
            val match = pausedMatch(pausedAt = T0)
            every { getAllMatches() } returns flowOf(listOf(match))
            every { timeProvider.getCurrentTime() } returns T0 + SIX_HOURS

            val recovered = useCase()

            assertEquals(1, recovered)
            coVerify(exactly = 1) { finishMatch(MATCH_ID, T0) }
        }

    private fun mentionsTheWholeContext(message: String): Boolean =
        message.contains(MATCH_ID) &&
            message.contains("IN_PROGRESS") &&
            message.contains("inactiveForMinutes=${7 * 60}") &&
            message.contains("pendingSubstitutions=")

    private fun inProgressMatch(periodStart: Long) =
        baseMatch(
            status = MatchStatus.IN_PROGRESS,
            periods =
                listOf(
                    MatchPeriod(periodNumber = 1, periodDuration = PERIOD_DURATION, startTimeMillis = periodStart),
                ),
        )

    private fun pausedMatch(pausedAt: Long) =
        baseMatch(
            status = MatchStatus.PAUSED,
            periods =
                listOf(
                    MatchPeriod(
                        periodNumber = 1,
                        periodDuration = PERIOD_DURATION,
                        startTimeMillis = pausedAt - PERIOD_DURATION,
                        endTimeMillis = pausedAt,
                    ),
                ),
        )

    private fun baseMatch(
        status: MatchStatus,
        periods: List<MatchPeriod>,
    ) = Match(
        id = MATCH_ID,
        teamId = "team-1",
        teamName = "Infantil A",
        opponent = "Rival CF",
        location = "Campo municipal",
        dateTime = null,
        periodType = PeriodType.HALF_TIME,
        status = status,
        periods = periods,
    )

    private companion object {
        const val MATCH_ID = "match-1"
        const val T0 = 1_700_000_000_000L
        const val PERIOD_DURATION = 25 * 60 * 1000L
        const val TWO_MINUTES = 2 * 60 * 1000L
        const val TEN_MINUTES = 10 * 60 * 1000L
        const val SIX_HOURS = 6 * 60 * 60 * 1000L
        const val SEVEN_HOURS = 7 * 60 * 60 * 1000L
    }
}
