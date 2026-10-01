package com.jesuslcorominas.teamflowmanager.data.core.datasource

/**
 * Opaque key-value storage for a match's pendings. It does not know the format of the [String]:
 * serialization lives in data/core.
 */
interface PendingSubstitutionsDataSource {
    /** Returns the payload stored for [matchId], or `null` when there is none. */
    fun getRaw(matchId: String): String?

    /** Stores [raw] for [matchId]. A `null` [raw] deletes the entry. */
    fun setRaw(
        matchId: String,
        raw: String?,
    )
}
