package com.jesuslcorominas.teamflowmanager.data.core.repository

import com.jesuslcorominas.teamflowmanager.data.core.datasource.PresidentNotificationDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.NotificationType
import com.jesuslcorominas.teamflowmanager.domain.model.PresidentNotification
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PresidentNotificationRepositoryImplTest {

    private lateinit var dataSource: PresidentNotificationDataSource
    private lateinit var repository: PresidentNotificationRepositoryImpl

    @Before
    fun setup() {
        dataSource = mockk(relaxed = true)
        repository = PresidentNotificationRepositoryImpl(dataSource)
    }

    private fun aNotification(
        id: String = "notif-1",
        read: Boolean = false,
    ) = PresidentNotification(
        id = id,
        type = NotificationType.USER_WAITING_FOR_ASSIGNMENT,
        title = "New member waiting",
        body = "John Doe is waiting for team assignment",
        userData = mapOf("userName" to "John Doe", "userEmail" to "john@example.com"),
        createdAt = 1000L,
        read = read,
    )

    // --- getNotifications ---

    @Test
    fun `getNotifications delegates to datasource`() = runTest {
        val notifications = listOf(aNotification("notif-1"), aNotification("notif-2"))
        every { dataSource.getNotifications("club-1") } returns flowOf(notifications)

        val result = repository.getNotifications("club-1").first()

        assertEquals(notifications, result)
        verify { dataSource.getNotifications("club-1") }
    }

    @Test
    fun `getNotifications returns empty list when datasource emits empty`() = runTest {
        every { dataSource.getNotifications("club-1") } returns flowOf(emptyList())

        val result = repository.getNotifications("club-1").first()

        assertEquals(emptyList<PresidentNotification>(), result)
    }

    // --- getUnreadCount ---

    @Test
    fun `getUnreadCount delegates to datasource`() = runTest {
        every { dataSource.getUnreadCount("club-1") } returns flowOf(3)

        val result = repository.getUnreadCount("club-1").first()

        assertEquals(3, result)
        verify { dataSource.getUnreadCount("club-1") }
    }

    @Test
    fun `getUnreadCount returns 0 when no unread notifications`() = runTest {
        every { dataSource.getUnreadCount("club-1") } returns flowOf(0)

        val result = repository.getUnreadCount("club-1").first()

        assertEquals(0, result)
    }

    // --- createNotification ---

    @Test
    fun `createNotification delegates to datasource`() = runTest {
        val notification = aNotification()

        repository.createNotification("club-1", notification)

        coVerify { dataSource.createNotification("club-1", notification) }
    }

    @Test
    fun `createNotification propagates exception from datasource`() = runTest {
        val notification = aNotification()
        coEvery { dataSource.createNotification("club-1", notification) } throws RuntimeException("Firestore error")

        try {
            repository.createNotification("club-1", notification)
            assert(false) { "Expected RuntimeException" }
        } catch (e: RuntimeException) {
            assertEquals("Firestore error", e.message)
        }
    }

    // --- markAsRead ---

    @Test
    fun `markAsRead delegates to datasource`() = runTest {
        repository.markAsRead("club-1", "notif-1")

        coVerify { dataSource.markAsRead("club-1", "notif-1") }
    }

    @Test
    fun `markAsRead propagates exception from datasource`() = runTest {
        coEvery { dataSource.markAsRead("club-1", "notif-1") } throws RuntimeException("Network error")

        try {
            repository.markAsRead("club-1", "notif-1")
            assert(false) { "Expected RuntimeException" }
        } catch (e: RuntimeException) {
            assertEquals("Network error", e.message)
        }
    }

    // --- markAsUnread ---

    @Test
    fun `markAsUnread delegates to datasource`() = runTest {
        repository.markAsUnread("club-1", "notif-1")

        coVerify { dataSource.markAsUnread("club-1", "notif-1") }
    }

    @Test
    fun `markAsUnread propagates exception from datasource`() = runTest {
        coEvery { dataSource.markAsUnread("club-1", "notif-1") } throws RuntimeException("Network error")

        try {
            repository.markAsUnread("club-1", "notif-1")
            assert(false) { "Expected RuntimeException" }
        } catch (e: RuntimeException) {
            assertEquals("Network error", e.message)
        }
    }

    // --- deleteNotification ---

    @Test
    fun `deleteNotification delegates to datasource`() = runTest {
        repository.deleteNotification("club-1", "notif-1")

        coVerify { dataSource.deleteNotification("club-1", "notif-1") }
    }

    @Test
    fun `deleteNotification propagates exception from datasource`() = runTest {
        coEvery { dataSource.deleteNotification("club-1", "notif-1") } throws RuntimeException("Delete failed")

        try {
            repository.deleteNotification("club-1", "notif-1")
            assert(false) { "Expected RuntimeException" }
        } catch (e: RuntimeException) {
            assertEquals("Delete failed", e.message)
        }
    }
}