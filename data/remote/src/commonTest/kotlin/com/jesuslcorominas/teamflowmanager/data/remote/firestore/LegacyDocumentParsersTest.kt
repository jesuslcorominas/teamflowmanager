package com.jesuslcorominas.teamflowmanager.data.remote.firestore

import com.jesuslcorominas.teamflowmanager.domain.model.MatchStatus
import com.jesuslcorominas.teamflowmanager.domain.model.PlayerTimeStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LegacyDocumentParsersTest {

    // region parseGoalDocument

    @Test
    fun `parseGoalDocument returns null for null map`() {
        assertNull(parseGoalDocument(null, "doc1", "match1"))
    }

    @Test
    fun `parseGoalDocument returns null for empty map`() {
        val result = parseGoalDocument(emptyMap(), "doc1", "match1")
        assertNull(result?.scorerId)
        assertEquals("match1", result?.matchId)
    }

    @Test
    fun `parseGoalDocument tolerates legacy Long scorerId and overrides matchId with the passed String`() {
        val rawData = mapOf("scorerId" to 123456789L)
        val result = parseGoalDocument(rawData, "doc1", "match-string-id")
        assertEquals("match-string-id", result?.matchId)
        assertEquals("123456789", result?.scorerId)
    }

    @Test
    fun `parseGoalDocument preserves non-zero matchElapsedTimeMillis`() {
        val rawData = mapOf("matchElapsedTimeMillis" to 954000L)
        val result = parseGoalDocument(rawData, "doc1", "match1")
        assertEquals(954000L, result?.matchElapsedTimeMillis)
    }

    @Test
    fun `parseGoalDocument does not default matchElapsedTimeMillis to 0 when present`() {
        val rawData = mapOf("matchElapsedTimeMillis" to 42L)
        val result = parseGoalDocument(rawData, "doc1", "match1")
        assertEquals(42L, result?.matchElapsedTimeMillis)
    }

    @Test
    fun `parseGoalDocument new-format map maps all fields correctly`() {
        val rawData =
            mapOf(
                "teamId" to "team1",
                "scorerId" to "player-1",
                "goalTimeMillis" to 1000L,
                "matchElapsedTimeMillis" to 500L,
                "opponentGoal" to true,
                "ownGoal" to false,
            )
        val result = parseGoalDocument(rawData, "doc1", "match1")
        assertEquals("doc1", result?.id)
        assertEquals("player-1", result?.scorerId)
        assertEquals(1000L, result?.goalTimeMillis)
        assertEquals(500L, result?.matchElapsedTimeMillis)
        assertTrue(result?.isOpponentGoal == true)
        assertFalse(result?.isOwnGoal == true)
    }

    @Test
    fun `parseGoalDocument coerces Int goalTimeMillis to Long`() {
        val rawData = mapOf("goalTimeMillis" to 1500)
        val result = parseGoalDocument(rawData, "doc1", "match1")
        assertEquals(1500L, result?.goalTimeMillis)
    }

    @Test
    fun `parseGoalDocument coerces Double goalTimeMillis to Long`() {
        val rawData = mapOf("goalTimeMillis" to 1500.0)
        val result = parseGoalDocument(rawData, "doc1", "match1")
        assertEquals(1500L, result?.goalTimeMillis)
    }

    @Test
    fun `parseGoalDocument coerces Int matchElapsedTimeMillis to Long`() {
        val rawData = mapOf("matchElapsedTimeMillis" to 1500)
        val result = parseGoalDocument(rawData, "doc1", "match1")
        assertEquals(1500L, result?.matchElapsedTimeMillis)
    }

    @Test
    fun `parseGoalDocument coerces Double matchElapsedTimeMillis to Long`() {
        val rawData = mapOf("matchElapsedTimeMillis" to 1500.0)
        val result = parseGoalDocument(rawData, "doc1", "match1")
        assertEquals(1500L, result?.matchElapsedTimeMillis)
    }

    @Test
    fun `parseGoalDocument defaults boolean fields when missing`() {
        val rawData = mapOf("scorerId" to "player-1")
        val result = parseGoalDocument(rawData, "doc1", "match1")
        assertFalse(result?.isOpponentGoal == true)
        assertFalse(result?.isOwnGoal == true)
    }

    @Test
    fun `parseGoalDocument coerces Number 1 opponentGoal to true`() {
        val rawData = mapOf("opponentGoal" to 1L)
        val result = parseGoalDocument(rawData, "doc1", "match1")
        assertTrue(result?.isOpponentGoal == true)
    }

    @Test
    fun `parseGoalDocument coerces Number 0 opponentGoal to false`() {
        val rawData = mapOf("opponentGoal" to 0L)
        val result = parseGoalDocument(rawData, "doc1", "match1")
        assertFalse(result?.isOpponentGoal == true)
    }

    @Test
    fun `parseGoalDocument coerces Number 1 ownGoal to true`() {
        val rawData = mapOf("ownGoal" to 1L)
        val result = parseGoalDocument(rawData, "doc1", "match1")
        assertTrue(result?.isOwnGoal == true)
    }

    @Test
    fun `parseGoalDocument coerces Number 0 ownGoal to false`() {
        val rawData = mapOf("ownGoal" to 0L)
        val result = parseGoalDocument(rawData, "doc1", "match1")
        assertFalse(result?.isOwnGoal == true)
    }

    // endregion

    // region parseSubstitutionDocument

    @Test
    fun `parseSubstitutionDocument returns null for null map`() {
        assertNull(parseSubstitutionDocument(null, "doc1", "match1"))
    }

    @Test
    fun `parseSubstitutionDocument tolerates legacy Long cross-ref fields`() {
        val rawData =
            mapOf(
                "playerOutId" to 111L,
                "playerInId" to 222L,
                "substitutionTimeMillis" to 2000L,
                "matchElapsedTimeMillis" to 1500L,
            )
        val result = parseSubstitutionDocument(rawData, "doc1", "match-string-id")
        assertEquals("111", result?.playerOutId)
        assertEquals("222", result?.playerInId)
        assertEquals("match-string-id", result?.matchId)
        assertEquals(1500L, result?.matchElapsedTimeMillis)
    }

    @Test
    fun `parseSubstitutionDocument new-format map maps all fields correctly`() {
        val rawData =
            mapOf(
                "playerOutId" to "player-out",
                "playerInId" to "player-in",
                "substitutionTimeMillis" to 3000L,
                "matchElapsedTimeMillis" to 2500L,
            )
        val result = parseSubstitutionDocument(rawData, "doc1", "match1")
        assertEquals("doc1", result?.id)
        assertEquals("player-out", result?.playerOutId)
        assertEquals("player-in", result?.playerInId)
        assertEquals(3000L, result?.substitutionTimeMillis)
        assertEquals(2500L, result?.matchElapsedTimeMillis)
    }

    @Test
    fun `parseSubstitutionDocument coerces Int substitutionTimeMillis to Long`() {
        val rawData = mapOf("substitutionTimeMillis" to 2000)
        val result = parseSubstitutionDocument(rawData, "doc1", "match1")
        assertEquals(2000L, result?.substitutionTimeMillis)
    }

    @Test
    fun `parseSubstitutionDocument coerces Double substitutionTimeMillis to Long`() {
        val rawData = mapOf("substitutionTimeMillis" to 2000.0)
        val result = parseSubstitutionDocument(rawData, "doc1", "match1")
        assertEquals(2000L, result?.substitutionTimeMillis)
    }

    @Test
    fun `parseSubstitutionDocument coerces Int matchElapsedTimeMillis to Long`() {
        val rawData = mapOf("matchElapsedTimeMillis" to 1500)
        val result = parseSubstitutionDocument(rawData, "doc1", "match1")
        assertEquals(1500L, result?.matchElapsedTimeMillis)
    }

    @Test
    fun `parseSubstitutionDocument coerces Double matchElapsedTimeMillis to Long`() {
        val rawData = mapOf("matchElapsedTimeMillis" to 1500.0)
        val result = parseSubstitutionDocument(rawData, "doc1", "match1")
        assertEquals(1500L, result?.matchElapsedTimeMillis)
    }

    @Test
    fun `parseSubstitutionDocument defaults empty cross-ref fields to empty string`() {
        val result = parseSubstitutionDocument(emptyMap(), "doc1", "match1")
        assertEquals("", result?.playerOutId)
        assertEquals("", result?.playerInId)
    }

    // endregion

    // region parsePlayerTimeDocument

    @Test
    fun `parsePlayerTimeDocument returns null for null map`() {
        assertNull(parsePlayerTimeDocument(null, "match1"))
    }

    @Test
    fun `parsePlayerTimeDocument tolerates legacy Long playerId`() {
        val rawData = mapOf("playerId" to 999L)
        val result = parsePlayerTimeDocument(rawData, "match1")
        assertEquals("999", result?.playerId)
    }

    @Test
    fun `parsePlayerTimeDocument defaults status to ON_BENCH when missing`() {
        val result = parsePlayerTimeDocument(emptyMap(), "match1")
        assertEquals(PlayerTimeStatus.ON_BENCH, result?.status)
    }

    @Test
    fun `parsePlayerTimeDocument new-format map maps all fields correctly`() {
        val rawData =
            mapOf(
                "playerId" to "player-1",
                "elapsedTimeMillis" to 12000L,
                "running" to true,
                "lastStartTimeMillis" to 5000L,
                "status" to "PLAYING",
                "lastOperationId" to "op-1",
            )
        val result = parsePlayerTimeDocument(rawData, "match1")
        assertEquals("player-1", result?.playerId)
        assertEquals(12000L, result?.elapsedTimeMillis)
        assertTrue(result?.isRunning == true)
        assertEquals(5000L, result?.lastStartTimeMillis)
        assertEquals(PlayerTimeStatus.PLAYING, result?.status)
        assertEquals("op-1", result?.lastOperationId)
    }

    @Test
    fun `parsePlayerTimeDocument coerces Int elapsedTimeMillis to Long`() {
        val rawData = mapOf("elapsedTimeMillis" to 12000)
        val result = parsePlayerTimeDocument(rawData, "match1")
        assertEquals(12000L, result?.elapsedTimeMillis)
    }

    @Test
    fun `parsePlayerTimeDocument coerces Double elapsedTimeMillis to Long`() {
        val rawData = mapOf("elapsedTimeMillis" to 12000.0)
        val result = parsePlayerTimeDocument(rawData, "match1")
        assertEquals(12000L, result?.elapsedTimeMillis)
    }

    @Test
    fun `parsePlayerTimeDocument coerces Int lastStartTimeMillis to Long`() {
        val rawData = mapOf("lastStartTimeMillis" to 5000)
        val result = parsePlayerTimeDocument(rawData, "match1")
        assertEquals(5000L, result?.lastStartTimeMillis)
    }

    @Test
    fun `parsePlayerTimeDocument coerces Double lastStartTimeMillis to Long`() {
        val rawData = mapOf("lastStartTimeMillis" to 5000.0)
        val result = parsePlayerTimeDocument(rawData, "match1")
        assertEquals(5000L, result?.lastStartTimeMillis)
    }

    @Test
    fun `parsePlayerTimeDocument defaults running to false when missing`() {
        val result = parsePlayerTimeDocument(emptyMap(), "match1")
        assertFalse(result?.isRunning == true)
    }

    @Test
    fun `parsePlayerTimeDocument coerces Number 1 running to true`() {
        val rawData = mapOf("running" to 1L)
        val result = parsePlayerTimeDocument(rawData, "match1")
        assertTrue(result?.isRunning == true)
    }

    @Test
    fun `parsePlayerTimeDocument coerces Number 0 running to false`() {
        val rawData = mapOf("running" to 0L)
        val result = parsePlayerTimeDocument(rawData, "match1")
        assertFalse(result?.isRunning == true)
    }

    @Test
    fun `parsePlayerTimeDocument defaults invalid status to ON_BENCH`() {
        val rawData = mapOf("status" to "INVALID_STATUS")
        val result = parsePlayerTimeDocument(rawData, "match1")
        assertEquals(PlayerTimeStatus.ON_BENCH, result?.status)
    }

    @Test
    fun `parsePlayerTimeDocument defaults all fields for empty map`() {
        val result = parsePlayerTimeDocument(emptyMap(), "match1")
        assertEquals("", result?.playerId)
        assertEquals(0L, result?.elapsedTimeMillis)
        assertFalse(result?.isRunning == true)
        assertEquals(null, result?.lastStartTimeMillis)
        assertEquals(PlayerTimeStatus.ON_BENCH, result?.status)
        assertEquals(null, result?.lastOperationId)
    }

    // endregion

    // region parsePlayerTimeHistoryDocument

    @Test
    fun `parsePlayerTimeHistoryDocument returns null for null map`() {
        assertNull(parsePlayerTimeHistoryDocument(null, "doc1", "player1", "match1"))
    }

    @Test
    fun `parsePlayerTimeHistoryDocument new-format map maps all fields correctly`() {
        val rawData =
            mapOf(
                "elapsedTimeMillis" to 7000L,
                "savedAtMillis" to 8000L,
            )
        val result = parsePlayerTimeHistoryDocument(rawData, "doc1", "player1", "match1")
        assertEquals("doc1", result?.id)
        assertEquals("player1", result?.playerId)
        assertEquals("match1", result?.matchId)
        assertEquals(7000L, result?.elapsedTimeMillis)
        assertEquals(8000L, result?.savedAtMillis)
    }

    @Test
    fun `parsePlayerTimeHistoryDocument tolerates missing numeric fields with 0 defaults`() {
        val result = parsePlayerTimeHistoryDocument(emptyMap(), "doc1", "player1", "match1")
        assertEquals(0L, result?.elapsedTimeMillis)
        assertEquals(0L, result?.savedAtMillis)
    }

    @Test
    fun `parsePlayerTimeHistoryDocument coerces Int elapsedTimeMillis to Long`() {
        val rawData = mapOf("elapsedTimeMillis" to 7000)
        val result = parsePlayerTimeHistoryDocument(rawData, "doc1", "player1", "match1")
        assertEquals(7000L, result?.elapsedTimeMillis)
    }

    @Test
    fun `parsePlayerTimeHistoryDocument coerces Double elapsedTimeMillis to Long`() {
        val rawData = mapOf("elapsedTimeMillis" to 7000.0)
        val result = parsePlayerTimeHistoryDocument(rawData, "doc1", "player1", "match1")
        assertEquals(7000L, result?.elapsedTimeMillis)
    }

    @Test
    fun `parsePlayerTimeHistoryDocument coerces Int savedAtMillis to Long`() {
        val rawData = mapOf("savedAtMillis" to 8000)
        val result = parsePlayerTimeHistoryDocument(rawData, "doc1", "player1", "match1")
        assertEquals(8000L, result?.savedAtMillis)
    }

    @Test
    fun `parsePlayerTimeHistoryDocument coerces Double savedAtMillis to Long`() {
        val rawData = mapOf("savedAtMillis" to 8000.0)
        val result = parsePlayerTimeHistoryDocument(rawData, "doc1", "player1", "match1")
        assertEquals(8000L, result?.savedAtMillis)
    }

    // endregion
}

class ParseMatchDocumentTest {
    private val legacyMatch =
        mapOf<String, Any?>(
            "teamId" to "team1",
            "teamName" to "Team B",
            "opponent" to "Team A",
            "location" to "Stadium",
            "dateTime" to 1_700_000_000_000L,
            "numberOfPeriods" to 2L,
            // Pre-migration: player references are Long hashes of the document ID
            "captainId" to 9_876_543L,
            "squadCallUpIds" to listOf(9_876_543L, 1_234_567L),
            "startingLineupIds" to listOf(9_876_543L),
            "status" to "FINISHED",
            "archived" to false,
            "pauseCount" to 1L,
            "goals" to 2L,
            "opponentGoals" to 1L,
            "timeoutStartTimeMillis" to 0L,
            "periods" to
                listOf(
                    mapOf(
                        "periodNumber" to 1L,
                        "periodDuration" to 1_500_000L,
                        "startTimeMillis" to 1_000L,
                        "endTimeMillis" to 1_501_000L,
                    ),
                    mapOf(
                        "periodNumber" to 2L,
                        "periodDuration" to 1_500_000L,
                        "startTimeMillis" to 2_000_000L,
                        "endTimeMillis" to 3_500_000L,
                    ),
                ),
            "lastCompletedOperationId" to "op1",
        )

    @Test
    fun `parseMatchDocument returns null for null map`() {
        assertNull(parseMatchDocument(null, "match1", "team1"))
    }

    @Test
    fun `parseMatchDocument parses a legacy document instead of failing`() {
        val result = parseMatchDocument(legacyMatch, "match1", "team1")

        assertEquals("match1", result?.id)
        assertEquals("team1", result?.teamId)
        assertEquals("Team A", result?.opponent)
        assertEquals(MatchStatus.FINISHED, result?.status)
        assertEquals(2, result?.goals)
        assertEquals(1, result?.opponentGoals)
        assertEquals("op1", result?.lastCompletedOperationId)
    }

    @Test
    fun `parseMatchDocument keeps legacy player references as strings`() {
        val result = parseMatchDocument(legacyMatch, "match1", "team1")

        assertEquals("9876543", result?.captainId)
        assertEquals(listOf("9876543", "1234567"), result?.squadCallUpIds)
        assertEquals(listOf("9876543"), result?.startingLineupIds)
    }

    @Test
    fun `parseMatchDocument preserves period timestamps`() {
        val result = parseMatchDocument(legacyMatch, "match1", "team1")

        assertEquals(2, result?.periods?.size)
        assertEquals(1_000L, result?.periods?.get(0)?.startTimeMillis)
        assertEquals(1_501_000L, result?.periods?.get(0)?.endTimeMillis)
        assertEquals(2_000_000L, result?.periods?.get(1)?.startTimeMillis)
        assertEquals(3_500_000L, result?.periods?.get(1)?.endTimeMillis)
    }

    @Test
    fun `parseMatchDocument falls back to default periods when the array is missing`() {
        val result = parseMatchDocument(legacyMatch - "periods", "match1", "team1")

        assertEquals(2, result?.periods?.size)
        assertEquals(0L, result?.periods?.get(0)?.startTimeMillis)
        assertTrue((result?.periods?.get(0)?.periodDuration ?: 0L) > 0L)
    }

    @Test
    fun `parseMatchDocument defaults an unknown status to SCHEDULED`() {
        val result = parseMatchDocument(legacyMatch + ("status" to "NOT_A_STATUS"), "match1", "team1")

        assertEquals(MatchStatus.SCHEDULED, result?.status)
    }
}
