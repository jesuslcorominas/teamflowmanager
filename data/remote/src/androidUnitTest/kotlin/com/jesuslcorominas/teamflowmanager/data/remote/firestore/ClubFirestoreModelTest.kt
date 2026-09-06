package com.jesuslcorominas.teamflowmanager.data.remote.firestore

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ClubFirestoreModelTest {

    @Test
    fun `ClubFirestoreModel stores all fields correctly`() {
        val model = ClubFirestoreModel(
            id = "club-remote-123",
            ownerId = "user-456",
            name = "Test Club",
            invitationCode = "ABCD1234",
            homeGround = "Central Stadium",
        )

        assertEquals("club-remote-123", model.id)
        assertEquals("user-456", model.ownerId)
        assertEquals("Test Club", model.name)
        assertEquals("ABCD1234", model.invitationCode)
        assertEquals("Central Stadium", model.homeGround)
    }

    @Test
    fun `ClubFirestoreModel handles null homeGround`() {
        val model = ClubFirestoreModel(
            id = "club-456",
            ownerId = "user-789",
            name = "Another Club",
            invitationCode = "WXYZ9876",
            homeGround = null,
        )

        assertEquals("club-456", model.id)
        assertEquals("user-789", model.ownerId)
        assertEquals("Another Club", model.name)
        assertNull(model.homeGround)
    }

    @Test
    fun `ClubFirestoreModel works with empty id`() {
        val model = ClubFirestoreModel(
            id = "",
            ownerId = "user-111",
            name = "Empty ID Club",
            invitationCode = "TEST0000",
        )

        assertEquals("", model.id)
        assertEquals("user-111", model.ownerId)
        assertEquals("Empty ID Club", model.name)
    }

    @Test
    fun `ClubFirestoreModel default constructor creates valid instance`() {
        val model = ClubFirestoreModel()

        assertEquals("", model.id)
        assertEquals("", model.ownerId)
        assertEquals("", model.name)
        assertEquals("", model.invitationCode)
        assertNull(model.homeGround)
    }

    @Test
    fun `ClubFirestoreModel field mapping for Club domain object`() {
        val model = ClubFirestoreModel(
            id = "remote-123",
            ownerId = "owner-456",
            name = "Mapped Club",
            invitationCode = "MAP1234",
            homeGround = "Stadium",
        )

        // Verify field correspondence
        assertEquals(model.id, "remote-123")
        assertEquals(model.ownerId, "owner-456")
        assertEquals(model.name, "Mapped Club")
        assertEquals(model.invitationCode, "MAP1234")
        assertEquals(model.homeGround, "Stadium")
    }

    @Test
    fun `ClubFirestoreModel with partial fields initializes defaults`() {
        val model = ClubFirestoreModel(
            id = "partial-id",
            ownerId = "partial-owner",
        )

        assertEquals("partial-id", model.id)
        assertEquals("partial-owner", model.ownerId)
        assertEquals("", model.name)
        assertEquals("", model.invitationCode)
        assertNull(model.homeGround)
    }

    @Test
    fun `ClubFirestoreModel creation with all nulldefaults creates empty values`() {
        val model = ClubFirestoreModel()

        assertEquals("", model.id)
        assertEquals("", model.ownerId)
        assertEquals("", model.name)
        assertEquals("", model.invitationCode)
        assertNull(model.homeGround)
    }
}
