package com.jesuslcorominas.teamflowmanager.usecase.repository

import com.jesuslcorominas.teamflowmanager.domain.model.SubstitutionPair
import kotlinx.coroutines.flow.Flow

/**
 * Local store of the substitutions a coach leaves scheduled before executing them. Pendings are
 * local to the device, keyed by match, and survive an app restart. They are never synced to
 * Firestore.
 *
 * Guaranteed invariants:
 * - Each pair matches exactly one outgoing player with one incoming player, and the two differ.
 * - A player never appears in two pairs at once, neither as outgoing nor as incoming.
 * - There are no exactly duplicated pairs.
 * - Insertion order is preserved.
 *
 * Threading: implementations are NOT thread-safe. Every mutating call for a given match must come
 * from a single thread — in practice the main thread, where the ViewModel owning the match screen
 * lives. Only the flow returned by [observe] may be collected from anywhere.
 */
interface PendingSubstitutionsRepository {
    /**
     * Emits the current pendings of [matchId] and every later change.
     * A match with nothing stored emits an empty list.
     */
    fun observe(matchId: String): Flow<List<SubstitutionPair>>

    /**
     * Pairs already scheduled for [matchId] that share a player with [pair], and that [add] would
     * therefore discard. Empty when [pair] displaces nothing — including when [pair] is already
     * scheduled exactly, or when it is degenerate and [add] would ignore it.
     *
     * Lets the UI warn before the destructive write without restating the conflict rule.
     */
    fun conflictsFor(
        matchId: String,
        pair: SubstitutionPair,
    ): List<SubstitutionPair>

    /**
     * Appends [pair] to [matchId], discarding any previously scheduled pair that shares its
     * outgoing or incoming player — see [conflictsFor] to know which ones beforehand.
     *
     * Does nothing when [pair] is already scheduled exactly (it keeps its position), or when both
     * of its players are the same, which is not a substitution.
     */
    fun add(
        matchId: String,
        pair: SubstitutionPair,
    )

    /** Removes exactly [pair] from [matchId]. Does nothing when it is not scheduled. */
    fun remove(
        matchId: String,
        pair: SubstitutionPair,
    )

    /** Removes every pending of [matchId]. Leaves other matches untouched. */
    fun clear(matchId: String)
}
