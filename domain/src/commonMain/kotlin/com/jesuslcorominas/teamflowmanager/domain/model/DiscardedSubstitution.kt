package com.jesuslcorominas.teamflowmanager.domain.model

/**
 * A substitution that was not applied, together with the reason. Carrying the reason per pair lets
 * the caller tell the user which change failed and why, instead of only how many were dropped.
 */
data class DiscardedSubstitution(
    val pair: SubstitutionPair,
    val reason: SubstitutionDiscardReason,
)
