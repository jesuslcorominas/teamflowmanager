package com.jesuslcorominas.teamflowmanager.domain.model

/**
 * How player substitutions are applied during a match. Device-global setting; defaults to
 * [SCHEDULED] when nothing has been stored.
 */
enum class SubstitutionMode {
    /** Each substitution takes effect the moment it is made. */
    LIVE,

    /** Substitutions are queued and applied together when confirmed. */
    SCHEDULED,
}
