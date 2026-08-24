package com.jesuslcorominas.teamflowmanager.data.remote.firestore

import com.jesuslcorominas.teamflowmanager.domain.model.Goal
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerSubstitution
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTime
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeHistory
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeStatus

/*
 * Pure, platform-agnostic field-by-field parsers for Firestore documents that may still be
 * stored in the pre-migration (legacy) Long-ID format. kotlinx-serialization / typed
 * deserialization throws on the whole document when a field's runtime type doesn't match the
 * declared model (e.g. a legacy `Long` where the model declares `String`), silently dropping the
 * entire result set. Reading from a raw `Map<String, Any?>` field-by-field tolerates both the
 * legacy and the current document shapes.
 *
 * Shared between the Android (`androidMain`) and iOS (`iosMain`) Firestore datasources so the
 * parsing logic has a single source of truth (#385.4).
 *
 * TODO: remove after backward-compat window closes.
 */

/**
 * Reads a numeric Firestore field as [Long] regardless of the concrete [Number] subtype the
 * platform SDK produced. Android Play-services returns integer fields as [Long], but iOS/GitLive
 * decoding of a raw `Map<String, Any?>` may surface them as [Int] or [Double]; a plain `as? Long`
 * would then yield `null` and silently zero the value (this is what kept #384's chart flat).
 */
private fun Map<String, Any?>.asLong(key: String): Long? = (this[key] as? Number)?.toLong()

fun parseGoalDocument(
    rawData: Map<String, Any?>?,
    docId: String,
    teamDocId: String,
    matchId: String,
): Goal? {
    if (rawData == null) return null
    return try {
        Goal(
            id = docId,
            matchId = matchId,
            scorerId = rawData["scorerId"]?.toString(),
            goalTimeMillis = rawData.asLong("goalTimeMillis") ?: 0L,
            matchElapsedTimeMillis = rawData.asLong("matchElapsedTimeMillis") ?: 0L,
            isOpponentGoal = rawData["opponentGoal"] as? Boolean ?: false,
            isOwnGoal = rawData["ownGoal"] as? Boolean ?: false,
        )
    } catch (_: Exception) {
        null
    }
}

fun parseSubstitutionDocument(
    rawData: Map<String, Any?>?,
    docId: String,
    teamDocId: String,
    matchId: String,
): PlayerSubstitution? {
    if (rawData == null) return null
    return try {
        PlayerSubstitution(
            id = docId,
            matchId = matchId,
            playerOutId = rawData["playerOutId"]?.toString() ?: "",
            playerInId = rawData["playerInId"]?.toString() ?: "",
            substitutionTimeMillis = rawData.asLong("substitutionTimeMillis") ?: 0L,
            matchElapsedTimeMillis = rawData.asLong("matchElapsedTimeMillis") ?: 0L,
        )
    } catch (_: Exception) {
        null
    }
}

fun parsePlayerTimeDocument(
    rawData: Map<String, Any?>?,
    docId: String,
    teamDocId: String,
    matchId: String,
): PlayerTime? {
    if (rawData == null) return null
    return try {
        val rawPlayerId = rawData["playerId"]?.toString() ?: ""
        val rawStatus = rawData["status"] as? String ?: PlayerTimeStatus.ON_BENCH.name
        PlayerTime(
            playerId = rawPlayerId,
            matchId = matchId,
            elapsedTimeMillis = rawData.asLong("elapsedTimeMillis") ?: 0L,
            isRunning = rawData["running"] as? Boolean ?: false,
            lastStartTimeMillis = rawData.asLong("lastStartTimeMillis"),
            status =
                try {
                    PlayerTimeStatus.valueOf(rawStatus)
                } catch (_: Exception) {
                    PlayerTimeStatus.ON_BENCH
                },
            lastOperationId = rawData["lastOperationId"] as? String,
        )
    } catch (_: Exception) {
        null
    }
}

fun parsePlayerTimeHistoryDocument(
    rawData: Map<String, Any?>?,
    docId: String,
    teamDocId: String,
    playerId: String,
    matchId: String,
): PlayerTimeHistory? {
    if (rawData == null) return null
    return try {
        PlayerTimeHistory(
            id = docId,
            playerId = playerId,
            matchId = matchId,
            elapsedTimeMillis = rawData.asLong("elapsedTimeMillis") ?: 0L,
            savedAtMillis = rawData.asLong("savedAtMillis") ?: 0L,
        )
    } catch (_: Exception) {
        null
    }
}
