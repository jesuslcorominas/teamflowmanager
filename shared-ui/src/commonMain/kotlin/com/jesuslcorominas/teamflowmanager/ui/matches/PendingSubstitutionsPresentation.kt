package com.jesuslcorominas.teamflowmanager.ui.matches

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionDiscardReason
import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionMode
import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionPair
import com.jesuslcorominas.teamflowmanager.viewmodel.PendingSubstitutionItem
import com.jesuslcorominas.teamflowmanager.viewmodel.PlayerTimeItem
import com.jesuslcorominas.teamflowmanager.viewmodel.SubstitutionExecutionResult
import com.jesuslcorominas.teamflowmanager.viewmodel.SubstitutionExecutionTrigger
import org.jetbrains.compose.resources.StringResource
import teamflowmanager.shared_ui.generated.resources.Res
import teamflowmanager.shared_ui.generated.resources.substitution_discard_reason_already_in_batch
import teamflowmanager.shared_ui.generated.resources.substitution_discard_reason_in_already_playing
import teamflowmanager.shared_ui.generated.resources.substitution_discard_reason_in_not_in_match
import teamflowmanager.shared_ui.generated.resources.substitution_discard_reason_out_not_playing

// Every decision the scheduled-substitution UI makes, kept out of the composables so it can be
// tested: shared-ui has no Compose UI test framework, and the module already proves its screen
// logic this way — see aggregateScorers and calculateFinishedPeriodElapsedTime.

/**
 * The cards to paint: whatever is queued, unless the screen is read-only.
 *
 * Deliberately not gated on [SubstitutionMode.LIVE]. The ViewModel exposes the queue in both modes
 * on purpose — switching to live is a device-wide setting, not an instruction to throw away what
 * was already scheduled for this match — and hiding it here would contradict that contract.
 *
 * In ordinary live use this changes nothing: nothing can be queued in live mode, so the list is
 * empty and the section never appears. It only matters when a queue outlives a switch to live,
 * and there the coach seeing their cards and being able to delete them beats the cards staying
 * invisible and applying themselves at the next break.
 */
internal fun pendingCardsToShow(
    readOnly: Boolean,
    items: List<PendingSubstitutionItem>,
): List<PendingSubstitutionItem> = if (readOnly) emptyList() else items

/**
 * Whether the player is shown as being on the pitch.
 *
 * Mirrors `MatchSubstitutionCoordinator.isOnPitch(mode)` on purpose: whoever is painted as on the
 * pitch must be exactly whoever the ViewModel will accept as a player going off. Inventing a second
 * predicate here would let the coach tap someone whose substitution is then discarded.
 *
 * In [SubstitutionMode.LIVE] this reduces to the expression the screen used before scheduled
 * substitutions existed.
 */
internal fun isOnPitchForDisplay(
    mode: SubstitutionMode,
    match: Match,
    item: PlayerTimeItem,
): Boolean =
    when {
        mode == SubstitutionMode.SCHEDULED && match.isStarted -> item.isRunning || item.isPaused
        match.isInProgress -> item.isRunning
        else -> false
    }

/**
 * Whether tapping a player starts the two-step selection.
 *
 * Scheduled mode allows it during a break — queueing changes at half time is the point of the
 * feature — and `isStarted` covers a timeout too, consistent with the ViewModel, which never looks
 * at the match status. In [SubstitutionMode.LIVE] it stays `isInProgress`, as before.
 */
internal fun canSelectPlayerForSubstitution(
    mode: SubstitutionMode,
    match: Match,
    readOnly: Boolean,
): Boolean =
    when {
        readOnly -> false
        mode == SubstitutionMode.SCHEDULED -> match.isStarted
        else -> match.isInProgress
    }

/**
 * Whether running the queue can achieve anything right now.
 *
 * While the match is paused every player on the pitch is PAUSED, so no pair can prosper: the batch
 * would be discarded whole on PLAYER_OUT_NOT_PLAYING. The cards survive and apply themselves on
 * resume, so nothing is lost — but offering the button there is offering a dead end, which is why
 * it is disabled with a line saying why rather than left enabled behind a warning.
 *
 * UI only. The ViewModel is unchanged: were the batch to run while paused it would still behave
 * exactly as its tests fix it.
 */
internal fun canExecutePendingSubstitutions(match: Match): Boolean = match.isInProgress

/**
 * Whether the section is painted at all — header included, even with nothing queued.
 *
 * The header has to survive an empty queue, because "Substitute all" and "Delete all" are required
 * to show up disabled when there is nothing to act on, and a button that is not drawn cannot be
 * disabled. The count in the title carries the rest of the message: `(0)` says the queue is empty
 * without a line of prose saying so.
 *
 * Not in live mode with an empty queue, though. There is nothing to schedule there, so a permanent
 * "Scheduled changes (0)" would cost the squad list a row and buy nothing — the exact opposite of
 * what this section is being reshaped for. A queue that outlived a switch to live still shows, for
 * the reason [pendingCardsToShow] gives: it has to be reachable to be deleted.
 */
internal fun shouldShowPendingSubstitutionsSection(
    readOnly: Boolean,
    mode: SubstitutionMode,
    items: List<PendingSubstitutionItem>,
): Boolean = !readOnly && (mode == SubstitutionMode.SCHEDULED || items.isNotEmpty())

/**
 * Whether "Substitute all" does anything: there has to be something queued, and the match has to be
 * able to take it.
 *
 * Two independent reasons to be off, deliberately kept as two. [canExecutePendingSubstitutions] is
 * unchanged and still owns the pause; this only adds the emptiness on top of it.
 */
internal fun canExecuteAllPendingSubstitutions(
    match: Match,
    items: List<PendingSubstitutionItem>,
): Boolean = items.isNotEmpty() && canExecutePendingSubstitutions(match)

/**
 * Whether "Delete all" does anything: only that there is something to delete.
 *
 * Pointedly not gated on the pause. Throwing the queue away at half time is a perfectly ordinary
 * thing for a coach to do — the pause blocks *applying* changes, not changing one's mind about
 * them.
 */
internal fun canClearAllPendingSubstitutions(items: List<PendingSubstitutionItem>): Boolean = items.isNotEmpty()

/**
 * Whether to print the line explaining the pause.
 *
 * Only when the pause is actually blocking something. With an empty queue the button is off because
 * there is nothing to run, not because the match is stopped, and saying "while the match is paused
 * these changes cannot be applied yet" under an empty list would name the wrong cause — the coach
 * would go looking for a pause to undo and find no changes waiting on the other side of it.
 */
internal fun shouldShowPausedHint(
    match: Match,
    items: List<PendingSubstitutionItem>,
): Boolean = items.isNotEmpty() && !canExecutePendingSubstitutions(match)

/**
 * Whether the section can be opened at all.
 *
 * With nothing queued there is nothing behind the header, and a row that answers a tap by doing
 * visibly nothing reads as broken — the coach presses it, no list appears, and they press again.
 * So with an empty queue the row is not a switch: no chevron, no ripple, no tap.
 *
 * It also decides what the section looks like, not just what it does. A queue emptied while the
 * section was open must fall back to its closed form rather than sit there open around nothing.
 */
internal fun canExpandPendingSubstitutions(items: List<PendingSubstitutionItem>): Boolean = items.isNotEmpty()

/**
 * Whether the counter should play its animation: did the queue just gain a pair it did not have?
 *
 * Deliberately about the pairs and not about how many there are. Scheduling a change for a player
 * who already had one does not grow the queue — the new pair displaces the old — and keying this on
 * the count left that case with no acknowledgement at all: the coach tapped two players, the number
 * stayed at 3, and nothing on screen said the app had heard them. The animation is the receipt, so
 * it has to fire whenever something was stored, not whenever the total moved.
 *
 * Still nothing on the way down. Deleting a change removes pairs and adds none, so this is false,
 * and a queue that is merely repainted contains no pair it did not contain a moment ago.
 */
internal fun shouldPulseCounter(
    previousPairs: List<SubstitutionPair>,
    currentPairs: List<SubstitutionPair>,
): Boolean = currentPairs.any { it !in previousPairs }

/** How the outcome of a batch reaches the coach. */
internal enum class SubstitutionResultPresentation {
    /** Transient: they pressed the button and are watching the list change in front of them. */
    SNACKBAR,

    /** Dismissible: it has to be read, and cannot be allowed to scroll away unseen. */
    DIALOG,
}

/**
 * A clean manual run is the only case the coach has already witnessed, so it is the only one that
 * gets a snackbar.
 *
 * Anything discarded needs a dialog: there is a reason per pair to read, with names, and the cards
 * are still there to be fixed. A [SubstitutionExecutionTrigger.RESUME] run always gets one — it
 * happened with nobody watching, and its discarded cards are deleted from the store before the
 * screen ever sees this, so the dialog is the only record that will remain of the team changing.
 */
internal fun presentationFor(result: SubstitutionExecutionResult): SubstitutionResultPresentation =
    if (result.trigger == SubstitutionExecutionTrigger.RESUME || result.discarded.isNotEmpty()) {
        SubstitutionResultPresentation.DIALOG
    } else {
        SubstitutionResultPresentation.SNACKBAR
    }

/**
 * Which of the two players a discard reason is actually about.
 *
 * The reasons are not all about the same side, and a message that names the wrong player is worse
 * than one full of jargon: jargon confuses, this misleads. PLAYER_ALREADY_SUBSTITUTED_IN_BATCH is
 * the honest exception — the domain raises it when *either* side was already used by an earlier
 * pair, so naming one of them would be inventing detail the app does not have.
 */
internal enum class DiscardReasonSubject {
    PLAYER_OUT,
    PLAYER_IN,
    EITHER,
}

internal fun discardReasonSubject(reason: SubstitutionDiscardReason): DiscardReasonSubject =
    when (reason) {
        SubstitutionDiscardReason.PLAYER_OUT_NOT_PLAYING -> DiscardReasonSubject.PLAYER_OUT
        SubstitutionDiscardReason.PLAYER_IN_ALREADY_PLAYING -> DiscardReasonSubject.PLAYER_IN
        SubstitutionDiscardReason.PLAYER_IN_NOT_IN_MATCH -> DiscardReasonSubject.PLAYER_IN
        SubstitutionDiscardReason.PLAYER_ALREADY_SUBSTITUTED_IN_BATCH -> DiscardReasonSubject.EITHER
    }

/**
 * Why a pair was not applied, in a coach's words: no ids, no "batch", no "player time".
 *
 * Every message names its subject — see [discardReasonSubject] for which player that is.
 */
internal fun discardReasonRes(reason: SubstitutionDiscardReason): StringResource =
    when (reason) {
        SubstitutionDiscardReason.PLAYER_OUT_NOT_PLAYING -> Res.string.substitution_discard_reason_out_not_playing
        SubstitutionDiscardReason.PLAYER_IN_ALREADY_PLAYING -> Res.string.substitution_discard_reason_in_already_playing
        SubstitutionDiscardReason.PLAYER_IN_NOT_IN_MATCH -> Res.string.substitution_discard_reason_in_not_in_match
        SubstitutionDiscardReason.PLAYER_ALREADY_SUBSTITUTED_IN_BATCH ->
            Res.string.substitution_discard_reason_already_in_batch
    }
