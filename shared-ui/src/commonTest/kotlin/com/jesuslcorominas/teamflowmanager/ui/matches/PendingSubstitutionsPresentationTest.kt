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
    fun `every queued card is painted`() {
        val items = listOf(pending(1), pending(2), pending(3))

        assertEquals(items, pendingCardsToShow(readOnly = false, items = items))
    }

    @Test
    fun `an empty queue paints nothing, which is what live mode always looks like`() {
        assertEquals(emptyList(), pendingCardsToShow(readOnly = false, items = emptyList()))
    }

    @Test
    fun `a queue that outlived a switch to live is still painted, so it can be deleted`() {
        // Nothing can be queued in live mode, so this only happens to a queue built in scheduled
        // mode that survived the switch. Hiding it would leave cards applying themselves at the
        // next break with the coach never having seen them.
        val items = listOf(pending(1), pending(2))

        assertEquals(items, pendingCardsToShow(readOnly = false, items = items))
    }

    @Test
    fun `read only never paints pending cards`() {
        val items = listOf(pending(1), pending(2))

        assertEquals(emptyList(), pendingCardsToShow(readOnly = true, items = items))
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

    // ---------- shouldShowPendingSubstitutionsSection ----------

    @Test
    fun `scheduled mode paints the section with an empty queue, so its buttons exist to be disabled`() {
        assertTrue(
            shouldShowPendingSubstitutionsSection(
                readOnly = false,
                mode = SubstitutionMode.SCHEDULED,
                items = emptyList(),
            ),
        )
    }

    @Test
    fun `live mode with an empty queue paints nothing, rather than spend a row on a count of zero`() {
        assertFalse(
            shouldShowPendingSubstitutionsSection(
                readOnly = false,
                mode = SubstitutionMode.LIVE,
                items = emptyList(),
            ),
        )
    }

    @Test
    fun `a queue that outlived a switch to live still gets its section, so it can be deleted`() {
        assertTrue(
            shouldShowPendingSubstitutionsSection(
                readOnly = false,
                mode = SubstitutionMode.LIVE,
                items = listOf(pending(1)),
            ),
        )
    }

    @Test
    fun `read only never paints the section, queued or not`() {
        assertFalse(
            shouldShowPendingSubstitutionsSection(
                readOnly = true,
                mode = SubstitutionMode.SCHEDULED,
                items = listOf(pending(1)),
            ),
        )
        assertFalse(
            shouldShowPendingSubstitutionsSection(
                readOnly = true,
                mode = SubstitutionMode.SCHEDULED,
                items = emptyList(),
            ),
        )
    }

    // ---------- canExecuteAllPendingSubstitutions / canClearAllPendingSubstitutions ----------

    @Test
    fun `substitute all needs both a queue and a running match`() {
        assertTrue(
            canExecuteAllPendingSubstitutions(match(MatchStatus.IN_PROGRESS), listOf(pending(1))),
        )
    }

    @Test
    fun `substitute all is off with an empty queue even while the match is running`() {
        assertFalse(canExecuteAllPendingSubstitutions(match(MatchStatus.IN_PROGRESS), emptyList()))
    }

    @Test
    fun `substitute all is off while paused even with a queue, as it was before`() {
        assertFalse(canExecuteAllPendingSubstitutions(match(MatchStatus.PAUSED), listOf(pending(1))))
    }

    @Test
    fun `delete all is off with an empty queue`() {
        assertFalse(canClearAllPendingSubstitutions(emptyList()))
    }

    @Test
    fun `delete all stays on while the match is paused, because changing one's mind is not blocked`() {
        // The two reasons are distinct on purpose: the pause blocks applying changes, never
        // discarding them. Half time is exactly when a coach rethinks the queue.
        assertTrue(canClearAllPendingSubstitutions(listOf(pending(1))))
    }

    // ---------- shouldShowPausedHint ----------

    @Test
    fun `the pause is explained when it is holding a real queue back`() {
        assertTrue(shouldShowPausedHint(match(MatchStatus.PAUSED), listOf(pending(1))))
    }

    @Test
    fun `nothing is explained while the match is running`() {
        assertFalse(shouldShowPausedHint(match(MatchStatus.IN_PROGRESS), listOf(pending(1))))
    }

    @Test
    fun `an empty queue during a pause explains nothing, because the pause is not what is off`() {
        // The button is disabled here for lack of anything to run. Printing the pause message would
        // name the wrong cause and send the coach looking for a pause to undo.
        assertFalse(shouldShowPausedHint(match(MatchStatus.PAUSED), emptyList()))
        assertFalse(shouldShowPausedHint(match(MatchStatus.TIMEOUT), emptyList()))
    }

    // ---------- canExpandPendingSubstitutions ----------

    @Test
    fun `an empty queue cannot be opened, so the row is not a switch`() {
        assertFalse(canExpandPendingSubstitutions(emptyList()))
    }

    @Test
    fun `a queue with something in it can be opened`() {
        assertTrue(canExpandPendingSubstitutions(listOf(pending(1))))
    }

    // ---------- shouldPulseCounter ----------

    @Test
    fun `the counter reacts when a change is added`() {
        assertTrue(
            shouldPulseCounter(
                previousPairs = listOf(pending(1).pair),
                currentPairs = listOf(pending(1).pair, pending(2).pair),
            ),
        )
    }

    @Test
    fun `the first change queued reacts, which is when it matters most`() {
        assertTrue(shouldPulseCounter(previousPairs = emptyList(), currentPairs = listOf(pending(1).pair)))
    }

    @Test
    fun `replacing a change reacts even though the count does not move`() {
        // The case the count-based version got wrong: scheduling over a player who already had a
        // change displaces the old pair, so the total stays put and the coach was told nothing.
        assertTrue(
            shouldPulseCounter(
                previousPairs = listOf(pending(1).pair, pending(2).pair),
                currentPairs = listOf(pending(1).pair, pending(3).pair),
            ),
        )
    }

    @Test
    fun `deleting a change does not react, since the animation means stored`() {
        assertFalse(
            shouldPulseCounter(
                previousPairs = listOf(pending(1).pair, pending(2).pair),
                currentPairs = listOf(pending(1).pair),
            ),
        )
        assertFalse(shouldPulseCounter(previousPairs = listOf(pending(1).pair), currentPairs = emptyList()))
    }

    @Test
    fun `an unchanged queue does not react, so a repaint is not mistaken for a change`() {
        val pairs = listOf(pending(1).pair, pending(2).pair)
        assertFalse(shouldPulseCounter(previousPairs = pairs, currentPairs = pairs))
    }

    @Test
    fun `the same pairs in a different order do not react`() {
        // Sorting is not news. Nothing was stored, so nothing should say it was.
        assertFalse(
            shouldPulseCounter(
                previousPairs = listOf(pending(1).pair, pending(2).pair),
                currentPairs = listOf(pending(2).pair, pending(1).pair),
            ),
        )
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

    // ---------- discardReasonSubject ----------

    @Test
    fun `a reason about the player going off names the player going off`() {
        assertEquals(
            DiscardReasonSubject.PLAYER_OUT,
            discardReasonSubject(SubstitutionDiscardReason.PLAYER_OUT_NOT_PLAYING),
        )
    }

    @Test
    fun `a reason about the player coming on names the player coming on, not the one going off`() {
        assertEquals(
            DiscardReasonSubject.PLAYER_IN,
            discardReasonSubject(SubstitutionDiscardReason.PLAYER_IN_ALREADY_PLAYING),
        )
        assertEquals(
            DiscardReasonSubject.PLAYER_IN,
            discardReasonSubject(SubstitutionDiscardReason.PLAYER_IN_NOT_IN_MATCH),
        )
    }

    @Test
    fun `a reason that can be about either player names neither, instead of inventing one`() {
        assertEquals(
            DiscardReasonSubject.EITHER,
            discardReasonSubject(SubstitutionDiscardReason.PLAYER_ALREADY_SUBSTITUTED_IN_BATCH),
        )
    }

    // ---------- distinctness ----------

    @Test
    fun `every discard reason has a distinct message, so none reads as another`() {
        val messages = SubstitutionDiscardReason.entries.map { discardReasonRes(it) }

        assertEquals(SubstitutionDiscardReason.entries.size, messages.toSet().size)
    }
}
