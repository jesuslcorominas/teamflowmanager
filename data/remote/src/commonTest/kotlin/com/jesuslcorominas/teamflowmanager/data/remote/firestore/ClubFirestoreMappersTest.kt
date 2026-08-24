package com.jesuslcorominas.teamflowmanager.data.remote.firestore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ClubFirestoreMappersTest {

    private fun clubFields(
        id: String = "",
        ownerId: String = "",
        name: String = "",
        invitationCode: String = "",
        homeGround: String? = null,
    ): ClubFields = object : ClubFields {
        override val id = id
        override val ownerId = ownerId
        override val name = name
        override val invitationCode = invitationCode
        override val homeGround = homeGround
    }

    @Test
    fun `toDomain maps ownerId correctly`() {
        val result = clubFields(id = "abc", ownerId = "user-1").toDomain()
        assertEquals("user-1", result.ownerId)
    }

    @Test
    fun `toDomain maps name correctly`() {
        val result = clubFields(id = "abc", name = "FC Test").toDomain()
        assertEquals("FC Test", result.name)
    }

    @Test
    fun `toDomain maps invitationCode correctly`() {
        val result = clubFields(id = "abc", invitationCode = "CODE123").toDomain()
        assertEquals("CODE123", result.invitationCode)
    }

    @Test
    fun `toDomain maps id string directly`() {
        val result = clubFields(id = "remote-id-xyz").toDomain()
        assertEquals("remote-id-xyz", result.id)
    }

    @Test
    fun `toDomain maps id string without conversion`() {
        val result = clubFields(id = "abc").toDomain()
        assertEquals("abc", result.id)
    }

    @Test
    fun `toDomain empty id produces empty string`() {
        val result = clubFields(id = "").toDomain()
        assertEquals("", result.id)
    }

    @Test
    fun `toDomain maps homeGround correctly`() {
        val result = clubFields(id = "abc", homeGround = "Central Stadium").toDomain()
        assertEquals("Central Stadium", result.homeGround)
    }

    @Test
    fun `toDomain null homeGround produces null in result`() {
        val result = clubFields(id = "abc", homeGround = null).toDomain()
        assertNull(result.homeGround)
    }
}
