package com.jesuslcorominas.teamflowmanager.data.remote.firestore

import com.jesuslcorominas.teamflowmanager.domain.model.NotificationType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PresidentNotificationFirestoreMappersTest {

    private fun notificationFields(
        id: String = "notif-1",
        type: String = "USER_WAITING_FOR_ASSIGNMENT",
        title: String = "Title",
        body: String = "Body",
        userData: Map<String, String> = emptyMap(),
        createdAt: Long = 1000L,
        read: Boolean = false,
    ): PresidentNotificationFields = object : PresidentNotificationFields {
        override val id = id
        override val type = type
        override val title = title
        override val body = body
        override val userData = userData
        override val createdAt = createdAt
        override val read = read
    }

    @Test
    fun `toDomain maps all fields correctly`() {
        val result = notificationFields(
            id = "n-123",
            type = "GOAL",
            title = "Goal Scored",
            body = "Your team scored",
            userData = mapOf("teamId" to "t-1"),
            createdAt = 9999L,
            read = true,
        ).toDomain()

        assertEquals("n-123", result.id)
        assertEquals(NotificationType.GOAL, result.type)
        assertEquals("Goal Scored", result.title)
        assertEquals("Your team scored", result.body)
        assertEquals(mapOf("teamId" to "t-1"), result.userData)
        assertEquals(9999L, result.createdAt)
        assertTrue(result.read)
    }

    @Test
    fun `toDomain unknown type string defaults to USER_WAITING_FOR_ASSIGNMENT`() {
        val result = notificationFields(type = "UNKNOWN_TYPE").toDomain()
        assertEquals(NotificationType.USER_WAITING_FOR_ASSIGNMENT, result.type)
    }

    @Test
    fun `toDomain empty type string defaults to USER_WAITING_FOR_ASSIGNMENT`() {
        val result = notificationFields(type = "").toDomain()
        assertEquals(NotificationType.USER_WAITING_FOR_ASSIGNMENT, result.type)
    }

    @Test
    fun `toDomain ASSIGNED_AS_COACH type maps correctly`() {
        val result = notificationFields(type = "ASSIGNED_AS_COACH").toDomain()
        assertEquals(NotificationType.ASSIGNED_AS_COACH, result.type)
    }

    @Test
    fun `toDomain MATCH_START type maps correctly`() {
        val result = notificationFields(type = "MATCH_START").toDomain()
        assertEquals(NotificationType.MATCH_START, result.type)
    }

    @Test
    fun `toDomain MATCH_END type maps correctly`() {
        val result = notificationFields(type = "MATCH_END").toDomain()
        assertEquals(NotificationType.MATCH_END, result.type)
    }

    @Test
    fun `toDomain GOAL type maps correctly`() {
        val result = notificationFields(type = "GOAL").toDomain()
        assertEquals(NotificationType.GOAL, result.type)
    }

    @Test
    fun `toDomain USER_WAITING_FOR_ASSIGNMENT type maps correctly`() {
        val result = notificationFields(type = "USER_WAITING_FOR_ASSIGNMENT").toDomain()
        assertEquals(NotificationType.USER_WAITING_FOR_ASSIGNMENT, result.type)
    }

    @Test
    fun `toDomain empty userData maps correctly`() {
        val result = notificationFields(userData = emptyMap()).toDomain()
        assertTrue(result.userData.isEmpty())
    }

    @Test
    fun `toDomain read true maps correctly`() {
        val result = notificationFields(read = true).toDomain()
        assertTrue(result.read)
    }

    @Test
    fun `toDomain read false maps correctly`() {
        val result = notificationFields(read = false).toDomain()
        assertFalse(result.read)
    }
}
