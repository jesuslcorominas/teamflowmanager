package com.jesuslcorominas.teamflowmanager.domain.model

/**
 * Why a [SubstitutionPair] of a batch could not be applied.
 *
 * A pair may break several rules at once — an unknown incoming player that also repeats an earlier
 * pair, for instance. The reported reason is then the first one in **declaration order**, so the
 * constants below are ordered from the most fundamental problem to the most contextual one. Keep
 * that order meaningful when adding a new reason: moving a constant changes what callers are told.
 */
enum class SubstitutionDiscardReason {
    /** The leaving player is not on the pitch, so there is nothing to substitute out. */
    PLAYER_OUT_NOT_PLAYING,

    /**
     * The incoming player is already on the pitch. Covers a self-substitution (`A -> A`) and a
     * chained batch (`A -> B`, `B -> C`), where benching B and restarting B's timer in the same
     * operation would leave B playing while the history claimed B left.
     */
    PLAYER_IN_ALREADY_PLAYING,

    /**
     * The incoming player has no player time for this match: nobody called them up. Starting their
     * timer would create a brand new record for a player that is not part of the squad.
     */
    PLAYER_IN_NOT_IN_MATCH,

    /**
     * The leaving or the incoming player was already used by an earlier pair of the same batch.
     * Applying only half of such a pair would silently leave the team a player short or long.
     */
    PLAYER_ALREADY_SUBSTITUTED_IN_BATCH,
}
