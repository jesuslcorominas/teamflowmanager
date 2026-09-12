package com.jesuslcorominas.teamflowmanager.domain.model

/**
 * Outcome of registering a batch of substitutions.
 *
 * [applied] holds exactly the pairs that were recorded as a [PlayerSubstitution]; [discarded] holds
 * the rest, each with its reason. Both preserve the order of the requested batch. A caller running
 * the batch without a user in front of it — resuming a match after half time, say — needs this to
 * tell "three changes applied" from "none applied, all discarded".
 */
data class SubstitutionBatchResult(
    val applied: List<SubstitutionPair>,
    val discarded: List<DiscardedSubstitution>,
)
