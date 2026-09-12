package com.jesuslcorominas.teamflowmanager.viewmodel

import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsEvent
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsParam
import com.jesuslcorominas.teamflowmanager.domain.analytics.AnalyticsTracker
import com.jesuslcorominas.teamflowmanager.domain.analytics.CrashReporter
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionBatchResult
import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionMode
import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionPair
import com.jesuslcorominas.teamflowmanager.domain.usecase.AddPendingSubstitutionUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.ClearPendingSubstitutionsUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.ObservePendingSubstitutionsUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.ObserveSubstitutionModeUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.RegisterPlayerSubstitutionUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.RemovePendingSubstitutionUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.SetShouldShowInvalidSubstitutionAlertUseCase
import com.jesuslcorominas.teamflowmanager.domain.usecase.ShouldShowInvalidSubstitutionAlertUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Everything about substituting a player: choosing who comes off, validating who goes on,
 * applying a change immediately in live mode, queueing it in scheduled mode, and running the
 * queue — by hand or on its own after a break.
 *
 * Live and scheduled live together on purpose. They share the "is this player on the pitch"
 * predicate, the validation of the incoming player and the selection of the outgoing one;
 * splitting them would mean duplicating those three or having one call the other.
 *
 * It owns no clock and no match state: whatever it needs of those is handed to it per call, so
 * there is no second copy to fall out of step. Ordering — that the queue only runs after the
 * match has been resumed — is not its business either; that stays with the ViewModel.
 */
internal class MatchSubstitutionCoordinator(
    private val matchId: String,
    private val registerPlayerSubstitutionUseCase: RegisterPlayerSubstitutionUseCase,
    private val observeSubstitutionModeUseCase: ObserveSubstitutionModeUseCase,
    private val observePendingSubstitutionsUseCase: ObservePendingSubstitutionsUseCase,
    private val addPendingSubstitutionUseCase: AddPendingSubstitutionUseCase,
    private val removePendingSubstitutionUseCase: RemovePendingSubstitutionUseCase,
    private val clearPendingSubstitutionsUseCase: ClearPendingSubstitutionsUseCase,
    private val shouldShowInvalidSubstitutionAlertUseCase: ShouldShowInvalidSubstitutionAlertUseCase,
    private val setShouldShowInvalidSubstitutionAlertUseCase: SetShouldShowInvalidSubstitutionAlertUseCase,
    private val analyticsTracker: AnalyticsTracker,
    private val crashReporter: CrashReporter,
) {
    private val _selectedPlayerOut = MutableStateFlow<String?>(null)
    val selectedPlayerOut: StateFlow<String?> = _selectedPlayerOut.asStateFlow()

    private val _showInvalidSubstitutionAlert = MutableStateFlow(false)
    val showInvalidSubstitutionAlert: StateFlow<Boolean> = _showInvalidSubstitutionAlert.asStateFlow()

    private val _isSubstitutionInProgress = MutableStateFlow(false)
    val isSubstitutionInProgress: StateFlow<Boolean> = _isSubstitutionInProgress.asStateFlow()

    private val _playerAlreadyScheduledAlert = MutableStateFlow<PlayerAlreadyScheduledAlert?>(null)
    val playerAlreadyScheduledAlert: StateFlow<PlayerAlreadyScheduledAlert?> = _playerAlreadyScheduledAlert.asStateFlow()

    /**
     * What the coach was in the middle of doing when the warning interrupted them, so confirming
     * can finish it. Held as data rather than a callback so a test can see what is pending.
     *
     * [playerInId] null means they were picking the player coming off and had got no further.
     */
    private var interruptedSelection: InterruptedSelection? = null

    private data class InterruptedSelection(
        val playerOutId: String,
        val playerInId: String?,
    )

    private val _lastSubstitutionResult = MutableStateFlow<SubstitutionExecutionResult?>(null)
    val lastSubstitutionResult: StateFlow<SubstitutionExecutionResult?> = _lastSubstitutionResult.asStateFlow()

    /** Flows, not StateFlows: the ViewModel owns the scope, so it does the `stateIn`. */
    fun observeMode(): Flow<SubstitutionMode> = observeSubstitutionModeUseCase()

    fun pendingItems(squadPlayers: Flow<List<Player>>): Flow<List<PendingSubstitutionItem>> =
        combine(observePendingSubstitutionsUseCase(matchId), squadPlayers) { pairs, players ->
            // A pair neither of whose players is in the call-up cannot be painted; dropping it
            // beats showing a blank card. Corrupt data, not a normal state.
            pairs.mapNotNull { it.toPendingItem(players) }
        }

    /**
     * Picks the player coming off, warning first if they already have a change waiting.
     *
     * The warning used to come at the end, once both players were picked, and by then it could not
     * say which of the two it was about — the coach saw a name they had just tapped and reasonably
     * read the problem as being with that one. Asked the moment a player is chosen, there is only
     * one player it can be about.
     */
    fun selectPlayerOut(
        playerId: String,
        playerTimes: List<PlayerTimeItem>,
        mode: SubstitutionMode,
        pendingPairs: List<SubstitutionPair>,
    ) {
        val player = playerTimes.find { it.player.id == playerId }
        if (player?.isOnPitch(mode) == true) {
            if (mode == SubstitutionMode.SCHEDULED && pendingPairs.involve(playerId)) {
                interruptedSelection = InterruptedSelection(playerOutId = playerId, playerInId = null)
                _playerAlreadyScheduledAlert.value = PlayerAlreadyScheduledAlert(playerId = playerId)
                return
            }
            _selectedPlayerOut.value = playerId
        } else {
            // Player is not currently playing, show alert if preferences allow
            if (shouldShowInvalidSubstitutionAlertUseCase()) {
                _showInvalidSubstitutionAlert.value = true
            }
        }
    }

    fun clearPlayerOutSelection() {
        _selectedPlayerOut.value = null
    }

    fun dismissInvalidSubstitutionAlert(dontShowAgain: Boolean) {
        _showInvalidSubstitutionAlert.value = false
        if (dontShowAgain) {
            setShouldShowInvalidSubstitutionAlertUseCase(false)
        }
    }

    fun substitutePlayer(
        playerInId: String,
        scope: CoroutineScope,
        mode: SubstitutionMode,
        playerTimes: List<PlayerTimeItem>,
        squadPlayers: List<Player>,
        currentTimeMillis: Long,
        pendingPairs: List<SubstitutionPair>,
    ) {
        val playerOut = _selectedPlayerOut.value ?: return

        // Validate that the incoming player is not already playing
        if (!isValidSubstitution(playerInId, playerTimes, mode)) {
            // Clear the selection since the substitution is invalid
            _selectedPlayerOut.value = null
            return
        }

        val pair = SubstitutionPair(playerOutId = playerOut, playerInId = playerInId)
        if (mode == SubstitutionMode.SCHEDULED) {
            // The same question asked of the player going off, asked again of the one coming on —
            // the only moment at which this one has been chosen. Whoever was warned about at
            // selection is not warned about again when the pair is stored.
            if (pendingPairs.involve(playerInId)) {
                interruptedSelection = InterruptedSelection(playerOutId = playerOut, playerInId = playerInId)
                _playerAlreadyScheduledAlert.value = PlayerAlreadyScheduledAlert(playerId = playerInId)
                return
            }
            schedule(pair)
            return
        }

        performSubstitution(
            scope = scope,
            playerIn = playerInId,
            playerOut = playerOut,
            currentTimeMillis = currentTimeMillis,
            analyticsMessage = "Two-step substitution: $playerOut -> $playerInId",
            method = "two_step",
        )
    }

    fun substitutePlayerDirect(
        playerInId: String,
        playerOutId: String,
        scope: CoroutineScope,
        mode: SubstitutionMode,
        playerTimes: List<PlayerTimeItem>,
        currentTimeMillis: Long,
    ) {
        // Validate that the incoming player is not already playing
        if (!isValidSubstitution(playerInId, playerTimes, mode)) {
            // No selection state to clear for direct substitution
            return
        }

        performSubstitution(
            scope = scope,
            playerIn = playerInId,
            playerOut = playerOutId,
            currentTimeMillis = currentTimeMillis,
            analyticsMessage = "Direct substitution: $playerOutId -> $playerInId (drag-drop)",
            method = "drag_drop",
        )
    }

    /**
     * Queues [pair] instead of applying it, and lets the store drop whatever it displaces.
     *
     * Nothing is asked here any more. Both players were checked the moment they were picked — see
     * [selectPlayerOut] and [substitutePlayer] — so a warning at this point could only repeat one
     * the coach has already answered, and a second dialog for an already-granted permission is how
     * people learn to dismiss dialogs without reading them.
     */
    private fun schedule(pair: SubstitutionPair) {
        addPendingSubstitutionUseCase(matchId, pair)
        _selectedPlayerOut.value = null
    }

    /**
     * Goes ahead with the selection the warning interrupted.
     *
     * Confirming the player coming off only selects them — the coach still has to pick who comes
     * on, and may yet change their mind. Confirming the one coming on stores the pair, and the
     * store discards whatever it displaces.
     */
    fun confirmPlayerAlreadyScheduled() {
        val interrupted = interruptedSelection ?: return
        interruptedSelection = null
        _playerAlreadyScheduledAlert.value = null

        val playerIn = interrupted.playerInId
        if (playerIn == null) {
            _selectedPlayerOut.value = interrupted.playerOutId
        } else {
            schedule(SubstitutionPair(playerOutId = interrupted.playerOutId, playerInId = playerIn))
        }
    }

    /**
     * Backs out of the warning. Nothing is scheduled and nothing already scheduled is lost.
     *
     * A player already chosen to come off stays chosen: the coach was told the player they just
     * tapped is unavailable, not that they should start over, and throwing the first pick away
     * would make them repeat it.
     */
    fun dismissPlayerAlreadyScheduled() {
        interruptedSelection = null
        _playerAlreadyScheduledAlert.value = null
    }

    fun removePending(pair: SubstitutionPair) {
        removePendingSubstitutionUseCase(matchId, pair)
    }

    fun clearPending() {
        clearPendingSubstitutionsUseCase(matchId)
    }

    fun consumeLastResult() {
        _lastSubstitutionResult.value = null
    }

    /** Whether any queued pair has this player on either side of it. */
    private fun List<SubstitutionPair>.involve(playerId: String): Boolean = any { it.playerOutId == playerId || it.playerInId == playerId }

    /** Every queued card, read from the store — see [runAll] for why not the resolved list. */
    suspend fun queuedPairs(): List<SubstitutionPair> = observePendingSubstitutionsUseCase(matchId).first()

    /**
     * The one and only execution path: a card's play button, "substitute all" and the automatic
     * run after a break all land here, so a batch of one behaves exactly like a batch of many.
     *
     * Applied pairs are always unscheduled. Discarded ones survive a
     * [SubstitutionExecutionTrigger.MANUAL] run — the coach is watching, [lastSubstitutionResult]
     * tells them why, and deleting their card for them would lose work with no way back. On
     * [SubstitutionExecutionTrigger.RESUME] they are dropped: a card that outlived a resume would
     * fire again by itself at the next break, with nobody watching again.
     *
     * Running this while the match is paused is allowed and expected — the coach can schedule
     * during the break, so they can press the button there too. Every pair is then discarded with
     * PLAYER_OUT_NOT_PLAYING, because paused players are not PLAYING, the cards stay, and the
     * result says so. Nothing is lost: they apply on their own when the match resumes.
     */
    suspend fun run(
        pairs: List<SubstitutionPair>,
        trigger: SubstitutionExecutionTrigger,
        squadPlayers: List<Player>,
        currentTimeMillis: Long,
    ) {
        if (pairs.isEmpty()) return

        try {
            _isSubstitutionInProgress.value = true
            crashReporter.log("Running ${pairs.size} scheduled substitution(s), trigger=$trigger")

            val result =
                registerPlayerSubstitutionUseCase(
                    matchId = matchId,
                    substitutions = pairs,
                    currentTimeMillis = currentTimeMillis,
                )

            result.applied.forEach { removePendingSubstitutionUseCase(matchId, it) }
            if (trigger == SubstitutionExecutionTrigger.RESUME) {
                result.discarded.forEach { removePendingSubstitutionUseCase(matchId, it.pair) }
            }

            // Resolved before publishing: on a resume run the cards are gone by the time the
            // screen reads this, so ids alone would leave it with nobody to name.
            _lastSubstitutionResult.value =
                SubstitutionExecutionResult(
                    trigger = trigger,
                    applied = result.applied.mapNotNull { it.toPendingItem(squadPlayers) },
                    discarded =
                        result.discarded.mapNotNull { discarded ->
                            discarded.pair.toPendingItem(squadPlayers)?.let {
                                DiscardedSubstitutionItem(substitution = it, reason = discarded.reason)
                            }
                        },
                )
            reportUnresolvedInResult(result)

            val method = if (trigger == SubstitutionExecutionTrigger.RESUME) "scheduled_resume" else "scheduled_manual"
            result.applied.forEach { pair ->
                analyticsTracker.logEvent(
                    AnalyticsEvent.SUBSTITUTION_MADE,
                    mapOf(
                        AnalyticsParam.MATCH_ID to matchId,
                        AnalyticsParam.PLAYER_OUT to pair.playerOutId,
                        AnalyticsParam.PLAYER_IN to pair.playerInId,
                        AnalyticsParam.SUBSTITUTION_MINUTE to (currentTimeMillis / 60000).toString(),
                        AnalyticsParam.SUBSTITUTION_METHOD to method,
                    ),
                )
            }
        } catch (e: Exception) {
            // Reported, then swallowed. Not rethrown like performSubstitution does: that would
            // take down the app over a failed batch, and here nothing has been lost — no card was
            // unscheduled, so the coach can simply press again. Going unreported was the actual
            // problem; this path was the only substitution route invisible to diagnostics.
            crashReporter.recordException(e)
            crashReporter.log("Error running scheduled substitutions (trigger=$trigger): ${e.message}")
        } finally {
            _isSubstitutionInProgress.value = false
        }
    }

    /**
     * A pair whose players are not in the call-up cannot be named, so it is left out of the
     * published result rather than surfaced half-empty. Unreachable in practice; logged because a
     * report that quietly holds fewer entries than the batch would be hard to make sense of.
     */
    private fun reportUnresolvedInResult(result: SubstitutionBatchResult) {
        val published = _lastSubstitutionResult.value ?: return
        val missing =
            (result.applied.size - published.applied.size) +
                (result.discarded.size - published.discarded.size)
        if (missing > 0) {
            crashReporter.log("$missing substitution(s) left out of the result: players not in the call-up")
        }
    }

    /**
     * Validates that a player can be substituted in.
     * A valid substitution requires the incoming player to NOT be currently playing.
     */
    private fun isValidSubstitution(
        playerInId: String,
        playerTimes: List<PlayerTimeItem>,
        mode: SubstitutionMode,
    ): Boolean {
        val playerIn = playerTimes.find { it.player.id == playerInId }
        if (playerIn?.isOnPitch(mode) == true) {
            // Player is already playing, show alert and don't proceed with substitution
            if (shouldShowInvalidSubstitutionAlertUseCase()) {
                _showInvalidSubstitutionAlert.value = true
            }
            return false
        }
        return true
    }

    private fun performSubstitution(
        scope: CoroutineScope,
        playerIn: String,
        playerOut: String,
        currentTimeMillis: Long,
        analyticsMessage: String,
        method: String,
    ) {
        scope.launch {
            try {
                crashReporter.log(analyticsMessage)

                // Show blocking loading indicator during substitution
                _isSubstitutionInProgress.value = true

                registerPlayerSubstitutionUseCase(
                    matchId = matchId,
                    substitutions = listOf(SubstitutionPair(playerOutId = playerOut, playerInId = playerIn)),
                    currentTimeMillis = currentTimeMillis,
                )

                analyticsTracker.logEvent(
                    AnalyticsEvent.SUBSTITUTION_MADE,
                    mapOf(
                        AnalyticsParam.MATCH_ID to matchId,
                        AnalyticsParam.PLAYER_OUT to playerOut,
                        AnalyticsParam.PLAYER_IN to playerIn,
                        AnalyticsParam.SUBSTITUTION_MINUTE to (currentTimeMillis / 60000).toString(),
                        AnalyticsParam.SUBSTITUTION_METHOD to method,
                    ),
                )

                // Clear any existing selection
                _selectedPlayerOut.value = null

                // Hide loading indicator after substitution completes
                _isSubstitutionInProgress.value = false
            } catch (e: Exception) {
                // Ensure loading indicator is hidden even on error
                _isSubstitutionInProgress.value = false
                crashReporter.recordException(e)
                crashReporter.log("Error in $method substitution: ${e.message}")
                throw e
            }
        }
    }

    /**
     * Whether the player counts as being on the pitch for substitution purposes.
     *
     * In scheduled mode a paused player counts: during the break the coach must see who is on and
     * be able to queue changes. In live mode the predicate stays exactly as it was — widening it
     * there would let a substitution be attempted mid-break, only for the use case to discard it
     * on PLAYER_OUT_NOT_PLAYING with nothing queued to recover it.
     */
    private fun PlayerTimeItem.isOnPitch(mode: SubstitutionMode): Boolean = if (mode == SubstitutionMode.SCHEDULED) isRunning || isPaused else isRunning

    private fun SubstitutionPair.toPendingItem(players: List<Player>): PendingSubstitutionItem? {
        val out = players.find { it.id == playerOutId } ?: return null
        val incoming = players.find { it.id == playerInId } ?: return null
        return PendingSubstitutionItem(pair = this, playerOut = out, playerIn = incoming)
    }
}
