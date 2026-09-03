package com.jesuslcorominas.teamflowmanager.domain.utils

/**
 * Computes the pre-migration Long ID stored in Firestore before the Long→String migration.
 * Cross-reference fields (matchId, playerId, playerOutId, playerInId, scorerId) were written as
 * Long hashes of the Firestore document ID. This function reimplements that hash so we can both
 * query and resolve old and new formats during the backward-compat window.
 *
 * NOTE: the hash is NOT reversible — the original document ID cannot be recovered from it. It is
 * however forward-computable, so a legacy reference can be matched by hashing the candidate
 * document IDs (see `findByIdOrLegacy` in the use case layer).
 *
 * Lives in `domain` because both the Firestore data sources (to build legacy queries) and the use
 * case layer (to resolve legacy player references) need the exact same hash.
 *
 * TODO: remove after backward-compat window closes.
 */
fun String.toLegacyId(): Long {
    var result = 0L
    var multiplier = 1L
    for (char in this) {
        result += char.code.toLong() * multiplier
        multiplier *= 31L
    }
    return kotlin.math.abs(result)
}
