package com.jesuslcorominas.teamflowmanager.ui.matches

import com.jesuslcorominas.teamflowmanager.domain.model.Match
import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionDiscardReason
import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionMode
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
 * The cards to paint. Empty in [SubstitutionMode.LIVE] and in read-only, which is what keeps the
 * live layout byte-for-byte what it was: the section, its header and its dialogs all hang off this
 * being non-empty.
 */
internal fun pendingCardsToShow(
    mode: SubstitutionMode,
    readOnly: Boolean,
    items: List<PendingSubstitutionItem>,
): List<PendingSubstitutionItem> = if (mode == SubstitutionMode.SCHEDULED && !readOnly) items else emptyList()

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

/** Why a pair was not applied, in a coach's words: no ids, no "batch", no "player time". */
internal fun discardReasonRes(reason: SubstitutionDiscardReason): StringResource =
    when (reason) {
        SubstitutionDiscardReason.PLAYER_OUT_NOT_PLAYING -> Res.string.substitution_discard_reason_out_not_playing
        SubstitutionDiscardReason.PLAYER_IN_ALREADY_PLAYING -> Res.string.substitution_discard_reason_in_already_playing
        SubstitutionDiscardReason.PLAYER_IN_NOT_IN_MATCH -> Res.string.substitution_discard_reason_in_not_in_match
        SubstitutionDiscardReason.PLAYER_ALREADY_SUBSTITUTED_IN_BATCH ->
            Res.string.substitution_discard_reason_already_in_batch
    }
