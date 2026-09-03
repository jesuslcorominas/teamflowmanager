package com.jesuslcorominas.teamflowmanager.usecase.util

import com.jesuslcorominas.teamflowmanager.domain.model.Player
import com.jesuslcorominas.teamflowmanager.domain.model.Position
import com.jesuslcorominas.teamflowmanager.domain.utils.toLegacyId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LegacyPlayerRefResolverTest {
    private fun player(id: String) =
        Player(
            id = id,
            firstName = "John",
            lastName = "Doe",
            number = 10,
            positions = listOf(Position.Forward),
            teamId = "team1",
            isCaptain = false,
        )

    private val players = listOf(player("abc123"), player("def456"))

    @Test
    fun `givenCurrentDocumentId_whenFindByIdOrLegacy_thenResolvesPlayer`() {
        assertEquals("abc123", players.findByIdOrLegacy("abc123")?.id)
    }

    @Test
    fun `givenLegacyLongHash_whenFindByIdOrLegacy_thenResolvesPlayer`() {
        // Given — the pre-migration Long hash of the player document ID, as stored in Firestore
        val legacyRef = "abc123".toLegacyId().toString()

        // When / Then — resolved instead of silently dropped
        assertEquals("abc123", players.findByIdOrLegacy(legacyRef)?.id)
    }

    @Test
    fun `givenLegacyHashOfAbsentPlayer_whenFindByIdOrLegacy_thenReturnsNull`() {
        assertNull(players.findByIdOrLegacy("zzz999".toLegacyId().toString()))
    }

    @Test
    fun `givenNullOrEmptyRef_whenFindByIdOrLegacy_thenReturnsNull`() {
        assertNull(players.findByIdOrLegacy(null))
        assertNull(players.findByIdOrLegacy(""))
    }

    @Test
    fun `givenNonNumericUnknownRef_whenFindByIdOrLegacy_thenReturnsNull`() {
        assertNull(players.findByIdOrLegacy("not-a-player"))
    }

    @Test
    fun `givenMixedRefs_whenFilterByIdsOrLegacy_thenResolvesBothFormatsAndDropsUnknown`() {
        // Given — a legacy starting lineup mixing both ID formats plus a deleted player
        val refs = listOf("abc123".toLegacyId().toString(), "def456", "999999999")

        // When
        val result = players.filterByIdsOrLegacy(refs)

        // Then
        assertEquals(listOf("abc123", "def456"), result.map { it.id })
    }
}
