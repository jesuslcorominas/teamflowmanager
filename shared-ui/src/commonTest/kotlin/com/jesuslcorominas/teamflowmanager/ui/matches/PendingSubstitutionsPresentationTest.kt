package com.jesuslcorominas.teamflowmanager.ui.matches

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.domain.model.PeriodType
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionDiscardReason
import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionMode
import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionPair
import com.jesuslcorominas.teamflowmanager.viewmodel.DiscardedSubstitutionItem
import com.jesuslcorominas.teamflowmanager.viewmodel.PendingSubstitutionItem
import com.jesuslcorominas.teamflowmanager.viewmodel.PlayerTimeItem
import com.jesuslcorominas.teamflowmanager.viewmodel.SubstitutionExecutionResult
import com.jesuslcorominas.teamflowmanager.viewmodel.SubstitutionExecutionTrigger
import teamflowmanager.shared_ui.generated.resources.Res
import teamflowmanager.shared_ui.generated.resources.substitution_discard_reason_already_in_batch
import teamflowmanager.shared_ui.generated.resources.substitution_discard_reason_in_already_playing
import teamflowmanager.shared_ui.generated.resources.substitution_discard_reason_in_not_in_match
import teamflowmanager.shared_ui.generated.resources.substitution_discard_reason_out_not_playing
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PendingSubstitutionsPresentationTest {
    private fun player(id: String) =
        Player(
            id = id,
            firstName = "First$id",
            lastName = "Last$id",
            number = 1,
            positions = listOf(Position.Midfielder),
            teamId = "team-1",
            isCaptain = false,
        )

    private fun match(status: MatchStatus) =
        Match(
            id = "match-1",
            teamName = "Team",
            opponent = "Opponent",
            location = "Home",
            periodType = PeriodType.HALF_TIME,
            status = status,
        )

    private fun pending(index: Int) =
        PendingSubstitutionItem(
            pair = SubstitutionPair(playerOutId = "out-$index", playerInId = "in-$index"),
            playerOut = player("out-$index"),
            playerIn = player("in-$index"),
        )

    private fun playerTime(
        isRunning: Boolean,
        isPaused: Boolean,
    ) = PlayerTimeItem(
        player = player("p1"),
        timeMillis = 0L,
        isRunning = isRunning,
        isPaused = isPaused,
    )

    private fun result(
        trigger: SubstitutionExecutionTrigger,
        discardedCount: Int = 0,
    ) = SubstitutionExecutionResult(
        trigger = trigger,
        applied = listOf(pending(1)),
        discarded =
            (1..discardedCount).map {
                DiscardedSubstitutionItem(
                    substitution = pending(it + 1),
                    reason = SubstitutionDiscardReason.PLAYER_OUT_NOT_PLAYING,
                )
            },
    )

    // ---------- pendingCardsToShow ----------

    @Test
    fun `live mode paints no pending cards even when the queue is full`() {
        val items = listOf(pending(1), pending(2), pending(3))

        assertEquals(
            emptyList(),
            pendingCardsToShow(SubstitutionMode.LIVE, readOnly = false, items = items),
        )
    }

    @Test
    fun `scheduled mode paints every queued card`() {
        val items = listOf(pending(1), pending(2), pending(3))

        assertEquals(
            items,
            pendingCardsToShow(SubstitutionMode.SCHEDULED, readOnly = false, items = items),
        )
    }

    @Test
    fun `read only never paints pending cards even in scheduled mode`() {
        val items = listOf(pending(1), pending(2))

        assertEquals(
            emptyList(),
            pendingCardsToShow(SubstitutionMode.SCHEDULED, readOnly = true, items = items),
        )
    }

    // ---------- isOnPitchForDisplay ----------

    @Test
    fun `live mode in progress shows the running player on the pitch`() {
        assertTrue(
            isOnPitchForDisplay(
                SubstitutionMode.LIVE,
                match(MatchStatus.IN_PROGRESS),
                playerTime(isRunning = true, isPaused = false),
            ),
        )
    }

    @Test
    fun `live mode paused shows nobody on the pitch, exactly as before scheduled substitutions`() {
        assertFalse(
            isOnPitchForDisplay(
                SubstitutionMode.LIVE,
                match(MatchStatus.PAUSED),
                playerTime(isRunning = false, isPaused = true),
            ),
        )
    }

    @Test
    fun `scheduled mode paused shows the paused player on the pitch`() {
        assertTrue(
            isOnPitchForDisplay(
                SubstitutionMode.SCHEDULED,
                match(MatchStatus.PAUSED),
                playerTime(isRunning = false, isPaused = true),
            ),
        )
    }

    @Test
    fun `scheduled mode paused still leaves a benched player off the pitch`() {
        assertFalse(
            isOnPitchForDisplay(
                SubstitutionMode.SCHEDULED,
                match(MatchStatus.PAUSED),
                playerTime(isRunning = false, isPaused = false),
            ),
        )
    }

    @Test
    fun `a match that has not started shows nobody on the pitch in either mode`() {
        val notStarted = match(MatchStatus.SCHEDULED)
        val item = playerTime(isRunning = true, isPaused = true)

        assertFalse(isOnPitchForDisplay(SubstitutionMode.LIVE, notStarted, item))
        assertFalse(isOnPitchForDisplay(SubstitutionMode.SCHEDULED, notStarted, item))
    }

    // ---------- canSelectPlayerForSubstitution ----------

    @Test
    fun `read only never allows selecting a player`() {
        assertFalse(
            canSelectPlayerForSubstitution(
                SubstitutionMode.SCHEDULED,
                match(MatchStatus.IN_PROGRESS),
                readOnly = true,
            ),
        )
    }

    @Test
    fun `live mode does not allow selecting a player while paused`() {
        assertFalse(
            canSelectPlayerForSubstitution(
                SubstitutionMode.LIVE,
                match(MatchStatus.PAUSED),
                readOnly = false,
            ),
        )
    }

    @Test
    fun `scheduled mode allows selecting a player while paused so changes can be queued at the break`() {
        assertTrue(
            canSelectPlayerForSubstitution(
                SubstitutionMode.SCHEDULED,
                match(MatchStatus.PAUSED),
                readOnly = false,
            ),
        )
    }

    @Test
    fun `scheduled mode allows selecting a player during a timeout`() {
        assertTrue(
            canSelectPlayerForSubstitution(
                SubstitutionMode.SCHEDULED,
                match(MatchStatus.TIMEOUT),
                readOnly = false,
            ),
        )
    }

    @Test
    fun `a match that has not started does not allow selecting a player in either mode`() {
        val notStarted = match(MatchStatus.SCHEDULED)

        assertFalse(canSelectPlayerForSubstitution(SubstitutionMode.LIVE, notStarted, readOnly = false))
        assertFalse(canSelectPlayerForSubstitution(SubstitutionMode.SCHEDULED, notStarted, readOnly = false))
    }

    // ---------- canExecutePendingSubstitutions ----------

    @Test
    fun `the queue can only be run while the match is in progress`() {
        assertTrue(canExecutePendingSubstitutions(match(MatchStatus.IN_PROGRESS)))
    }

    @Test
    fun `the queue cannot be run while the match is stopped, because every pair would be discarded`() {
        assertFalse(canExecutePendingSubstitutions(match(MatchStatus.PAUSED)))
        assertFalse(canExecutePendingSubstitutions(match(MatchStatus.TIMEOUT)))
        assertFalse(canExecutePendingSubstitutions(match(MatchStatus.SCHEDULED)))
        assertFalse(canExecutePendingSubstitutions(match(MatchStatus.FINISHED)))
    }

    // ---------- presentationFor ----------

    @Test
    fun `a clean manual run is reported with a snackbar, since the coach is watching`() {
        assertEquals(
            SubstitutionResultPresentation.SNACKBAR,
            presentationFor(result(SubstitutionExecutionTrigger.MANUAL)),
        )
    }

    @Test
    fun `a manual run with discards needs a dialog, to show a reason per pair`() {
        assertEquals(
            SubstitutionResultPresentation.DIALOG,
            presentationFor(result(SubstitutionExecutionTrigger.MANUAL, discardedCount = 1)),
        )
    }

    @Test
    fun `a clean resume run still needs a dialog, because nobody was watching`() {
        assertEquals(
            SubstitutionResultPresentation.DIALOG,
            presentationFor(result(SubstitutionExecutionTrigger.RESUME)),
        )
    }

    @Test
    fun `a resume run with discards needs a dialog`() {
        assertEquals(
            SubstitutionResultPresentation.DIALOG,
            presentationFor(result(SubstitutionExecutionTrigger.RESUME, discardedCount = 2)),
        )
    }

    // ---------- discardReasonRes ----------

    @Test
    fun `player out not playing maps to its own message`() {
        assertEquals(
            Res.string.substitution_discard_reason_out_not_playing,
            discardReasonRes(SubstitutionDiscardReason.PLAYER_OUT_NOT_PLAYING),
        )
    }

    @Test
    fun `player in already playing maps to its own message`() {
        assertEquals(
            Res.string.substitution_discard_reason_in_already_playing,
            discardReasonRes(SubstitutionDiscardReason.PLAYER_IN_ALREADY_PLAYING),
        )
    }

    @Test
    fun `player in not in match maps to its own message`() {
        assertEquals(
            Res.string.substitution_discard_reason_in_not_in_match,
            discardReasonRes(SubstitutionDiscardReason.PLAYER_IN_NOT_IN_MATCH),
        )
    }

    @Test
    fun `player already substituted in batch maps to its own message`() {
        assertEquals(
            Res.string.substitution_discard_reason_already_in_batch,
            discardReasonRes(SubstitutionDiscardReason.PLAYER_ALREADY_SUBSTITUTED_IN_BATCH),
        )
    }

    @Test
    fun `every discard reason has a distinct message, so none reads as another`() {
        val messages = SubstitutionDiscardReason.entries.map { discardReasonRes(it) }

        assertEquals(SubstitutionDiscardReason.entries.size, messages.toSet().size)
    }
}
