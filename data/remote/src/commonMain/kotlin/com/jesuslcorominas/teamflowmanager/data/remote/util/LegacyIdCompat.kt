package com.jesuslcorominas.teamflowmanager.data.remote.util

/**
 * Computes the pre-migration Long ID stored in Firestore before the Long→String migration.
 * Cross-reference fields (matchId, playerId, playerOutId, playerInId) were written as Long
 * hashes of the Firestore document ID. This function reimplements that hash so we can query
 * both old and new formats during the backward-compat window.
 *
 * NOTE: this hash is NOT reversible — we cannot recover the original document ID from it.
 *
 * TODO: remove after backward-compat window closes.
 */
internal fun String.toLegacyId(): Long {
    var result = 0L
    var multiplier = 1L
    for (char in this) {
        result += char.code.toLong() * multiplier
        multiplier *= 31L
    }
    return kotlin.math.abs(result)
}
