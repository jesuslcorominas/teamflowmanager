package com.jesuslcorominas.teamflowmanager.usecase.util

import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.utils.toLegacyId

/**
 * Resolves a player reference that may still be stored in the pre-migration (legacy) format.
 *
 * Before the Long→String ID migration, cross-reference fields (`scorerId`, `playerOutId`,
 * `playerInId`, `playerId`, `captainId`, `startingLineupIds`, …) were persisted as a Long hash of
 * the player's Firestore document ID. The Firestore parsers surface those values verbatim as their
 * `toString()`, so a plain `players.find { it.id == ref }` never matches and the whole record ends
 * up silently discarded — which is what left the President's Summary tab empty and its Timeline
 * showing goals only.
 *
 * The hash is not reversible, but it is forward-computable: hashing every candidate player ID and
 * comparing against the legacy reference resolves it exactly.
 *
 * TODO: remove after backward-compat window closes.
 */
fun List<Player>.findByIdOrLegacy(playerRef: String?): Player? {
    if (playerRef.isNullOrEmpty()) return null
    find { it.id == playerRef }?.let { return it }
    val legacyId = playerRef.toLongOrNull() ?: return null
    return find { it.id.toLegacyId() == legacyId }
}

/**
 * Same resolution as [findByIdOrLegacy] but keeping only the matched IDs, for reference lists such
 * as `Match.startingLineupIds` / `Match.squadCallUpIds`.
 */
fun List<Player>.filterByIdsOrLegacy(playerRefs: List<String>): List<Player> = playerRefs.mapNotNull { findByIdOrLegacy(it) }
