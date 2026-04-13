package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import android.util.Log
import app.cash.turbine.test
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.PresidentNotificationFirestoreModel
import com.jesuslcorominas.teamflowmanager.domain.model.NotificationType
import com.jesuslcorominas.teamflowmanager.domain.model.PresidentNotification
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PresidentNotificationFirestoreDataSourceImplTest {

    private val mockFirestore = mockk<FirebaseFirestore>()
    private val mockParentCollection = mockk<CollectionReference>()
    private val mockClubDocRef = mockk<DocumentReference>()
    private val mockNotificationsCollection = mockk<CollectionReference>()
    private val mockQuery = mockk<Query>()
    private val mockDocRef = mockk<DocumentReference>()
    private val mockQuerySnapshot = mockk<QuerySnapshot>()
    private val mockDocSnapshot = mockk<DocumentSnapshot>()
    private val mockListenerRegistration = mockk<ListenerRegistration>(relaxed = true)

    private lateinit var dataSource: PresidentNotificationFirestoreDataSourceImpl

    private val clubId = "club-fs-1"

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.e(any(), any<String>()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0

        dataSource = PresidentNotificationFirestoreDataSourceImpl(mockFirestore)
    }

    private fun setupNotificationsCollection() {
        every { mockFirestore.collection("presidentNotifications") } returns mockParentCollection
        every { mockParentCollection.document(clubId) } returns mockClubDocRef
        every { mockClubDocRef.collection("notifications") } returns mockNotificationsCollection
    }

    private fun aFirestoreModel(
        id: String = "notif1",
        read: Boolean = false,
        type: String = "USER_WAITING_FOR_ASSIGNMENT",
    ) = PresidentNotificationFirestoreModel(
        id = id,
        type = type,
        title = "Test Title",
        body = "Test Body",
        userData = emptyMap(),
        createdAt = 1000L,
        read = read,
    )

    // --- getNotifications ---

    @Test
    fun `getNotifications emits empty list when snapshot is empty`() =
        runTest {
            setupNotificationsCollection()
            val listenerSlot = slot<EventListener<QuerySnapshot>>()

            every { mockNotificationsCollection.orderBy("createdAt", Query.Direction.DESCENDING) } returns mockQuery
            every { mockQuery.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

            every { mockQuerySnapshot.isEmpty } returns true
            every { mockQuerySnapshot.documents } returns emptyList()

            dataSource.getNotifications(clubId).test {
                listenerSlot.captured.onEvent(mockQuerySnapshot, null)
                val result = awaitItem()
                assertEquals(0, result.size)
                cancel()
            }
        }

    @Test
    fun `getNotifications emits list when documents exist`() =
        runTest {
            setupNotificationsCollection()
            val listenerSlot = slot<EventListener<QuerySnapshot>>()

            every { mockNotificationsCollection.orderBy("createdAt", Query.Direction.DESCENDING) } returns mockQuery
            every { mockQuery.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

            val model = aFirestoreModel()
            every { mockDocSnapshot.id } returns "notif1"
            every { mockDocSnapshot.toObject(PresidentNotificationFirestoreModel::class.java) } returns model
            every { mockQuerySnapshot.isEmpty } returns false
            every { mockQuerySnapshot.documents } returns listOf(mockDocSnapshot)

            dataSource.getNotifications(clubId).test {
                listenerSlot.captured.onEvent(mockQuerySnapshot, null)
                val result = awaitItem()
                assertEquals(1, result.size)
                assertEquals("notif1", result[0].id)
                cancel()
            }
        }

    @Test
    fun `getNotifications emits empty list on error`() =
        runTest {
            setupNotificationsCollection()
            val listenerSlot = slot<EventListener<QuerySnapshot>>()

            every { mockNotificationsCollection.orderBy("createdAt", Query.Direction.DESCENDING) } returns mockQuery
            every { mockQuery.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

            val mockError = mockk<FirebaseFirestoreException>(relaxed = true)

            dataSource.getNotifications(clubId).test {
                listenerSlot.captured.onEvent(null, mockError)
                val result = awaitItem()
                assertEquals(0, result.size)
                cancel()
            }
        }

    @Test
    fun `getNotifications emits empty list when snapshot is null`() =
        runTest {
            setupNotificationsCollection()
            val listenerSlot = slot<EventListener<QuerySnapshot>>()

            every { mockNotificationsCollection.orderBy("createdAt", Query.Direction.DESCENDING) } returns mockQuery
            every { mockQuery.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

            dataSource.getNotifications(clubId).test {
                listenerSlot.captured.onEvent(null, null)
                val result = awaitItem()
                assertEquals(0, result.size)
                cancel()
            }
        }

    @Test
    fun `getNotifications uses document id when model id is empty`() =
        runTest {
            setupNotificationsCollection()
            val listenerSlot = slot<EventListener<QuerySnapshot>>()

            every { mockNotificationsCollection.orderBy("createdAt", Query.Direction.DESCENDING) } returns mockQuery
            every { mockQuery.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

            val modelWithEmptyId = aFirestoreModel(id = "")
            every { mockDocSnapshot.id } returns "doc-id-from-firestore"
            every { mockDocSnapshot.toObject(PresidentNotificationFirestoreModel::class.java) } returns modelWithEmptyId
            every { mockQuerySnapshot.isEmpty } returns false
            every { mockQuerySnapshot.documents } returns listOf(mockDocSnapshot)

            dataSource.getNotifications(clubId).test {
                listenerSlot.captured.onEvent(mockQuerySnapshot, null)
                val result = awaitItem()
                assertEquals(1, result.size)
                assertEquals("doc-id-from-firestore", result[0].id)
                cancel()
            }
        }

    // --- getUnreadCount ---

    @Test
    fun `getUnreadCount emits 0 when no unread notifications`() =
        runTest {
            setupNotificationsCollection()
            val listenerSlot = slot<EventListener<QuerySnapshot>>()

            every { mockNotificationsCollection.whereEqualTo("read", false) } returns mockQuery
            every { mockQuery.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

            every { mockQuerySnapshot.size() } returns 0

            dataSource.getUnreadCount(clubId).test {
                listenerSlot.captured.onEvent(mockQuerySnapshot, null)
                val result = awaitItem()
                assertEquals(0, result)
                cancel()
            }
        }

    @Test
    fun `getUnreadCount emits count when unread notifications exist`() =
        runTest {
            setupNotificationsCollection()
            val listenerSlot = slot<EventListener<QuerySnapshot>>()

            every { mockNotificationsCollection.whereEqualTo("read", false) } returns mockQuery
            every { mockQuery.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

            every { mockQuerySnapshot.size() } returns 3

            dataSource.getUnreadCount(clubId).test {
                listenerSlot.captured.onEvent(mockQuerySnapshot, null)
                val result = awaitItem()
                assertEquals(3, result)
                cancel()
            }
        }

    @Test
    fun `getUnreadCount emits 0 on error`() =
        runTest {
            setupNotificationsCollection()
            val listenerSlot = slot<EventListener<QuerySnapshot>>()

            every { mockNotificationsCollection.whereEqualTo("read", false) } returns mockQuery
            every { mockQuery.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

            val mockError = mockk<FirebaseFirestoreException>(relaxed = true)

            dataSource.getUnreadCount(clubId).test {
                listenerSlot.captured.onEvent(null, mockError)
                val result = awaitItem()
                assertEquals(0, result)
                cancel()
            }
        }

    // --- markAsRead ---

    @Test
    fun `markAsRead updates read field to true`() =
        runTest {
            mockkStatic("kotlinx.coroutines.tasks.TasksKt")
            setupNotificationsCollection()

            every { mockNotificationsCollection.document("notif1") } returns mockDocRef
            val voidTask = mockk<Task<Void>>()
            every { mockDocRef.update("read", true) } returns voidTask
            coEvery { voidTask.await() } returns mockk()

            dataSource.markAsRead(clubId, "notif1")

            verify { mockDocRef.update("read", true) }
        }

    @Test
    fun `markAsRead propagates exception on failure`() =
        runTest {
            mockkStatic("kotlinx.coroutines.tasks.TasksKt")
            setupNotificationsCollection()

            every { mockNotificationsCollection.document("notif1") } returns mockDocRef
            val voidTask = mockk<Task<Void>>()
            every { mockDocRef.update("read", true) } returns voidTask
            coEvery { voidTask.await() } throws RuntimeException("Network error")

            try {
                dataSource.markAsRead(clubId, "notif1")
                assert(false) { "Expected RuntimeException" }
            } catch (e: RuntimeException) {
                assertEquals("Network error", e.message)
            }
        }

    // --- markAsUnread ---

    @Test
    fun `markAsUnread updates read field to false`() =
        runTest {
            mockkStatic("kotlinx.coroutines.tasks.TasksKt")
            setupNotificationsCollection()

            every { mockNotificationsCollection.document("notif1") } returns mockDocRef
            val voidTask = mockk<Task<Void>>()
            every { mockDocRef.update("read", false) } returns voidTask
            coEvery { voidTask.await() } returns mockk()

            dataSource.markAsUnread(clubId, "notif1")

            verify { mockDocRef.update("read", false) }
        }

    @Test
    fun `markAsUnread propagates exception on failure`() =
        runTest {
            mockkStatic("kotlinx.coroutines.tasks.TasksKt")
            setupNotificationsCollection()

            every { mockNotificationsCollection.document("notif1") } returns mockDocRef
            val voidTask = mockk<Task<Void>>()
            every { mockDocRef.update("read", false) } returns voidTask
            coEvery { voidTask.await() } throws RuntimeException("Network error")

            try {
                dataSource.markAsUnread(clubId, "notif1")
                assert(false) { "Expected RuntimeException" }
            } catch (e: RuntimeException) {
                assertEquals("Network error", e.message)
            }
        }

    // --- deleteNotification ---

    @Test
    fun `deleteNotification deletes the document`() =
        runTest {
            mockkStatic("kotlinx.coroutines.tasks.TasksKt")
            setupNotificationsCollection()

            every { mockNotificationsCollection.document("notif1") } returns mockDocRef
            val voidTask = mockk<Task<Void>>()
            every { mockDocRef.delete() } returns voidTask
            coEvery { voidTask.await() } returns mockk()

            dataSource.deleteNotification(clubId, "notif1")

            verify { mockDocRef.delete() }
        }

    @Test
    fun `deleteNotification propagates exception on failure`() =
        runTest {
            mockkStatic("kotlinx.coroutines.tasks.TasksKt")
            setupNotificationsCollection()

            every { mockNotificationsCollection.document("notif1") } returns mockDocRef
            val voidTask = mockk<Task<Void>>()
            every { mockDocRef.delete() } returns voidTask
            coEvery { voidTask.await() } throws RuntimeException("Delete failed")

            try {
                dataSource.deleteNotification(clubId, "notif1")
                assert(false) { "Expected RuntimeException" }
            } catch (e: RuntimeException) {
                assertEquals("Delete failed", e.message)
            }
        }

    // --- createNotification ---

    private fun aPresidentNotification(id: String = "notif-new") =
        PresidentNotification(
            id = id,
            type = NotificationType.USER_WAITING_FOR_ASSIGNMENT,
            title = "New member waiting",
            body = "John Doe is waiting for team assignment",
            userData = mapOf("userName" to "John Doe", "userEmail" to "john@example.com"),
            createdAt = 1000L,
            read = false,
        )

    @Test
    fun `createNotification sets document with notification id when id is not empty`() =
        runTest {
            mockkStatic("kotlinx.coroutines.tasks.TasksKt")
            setupNotificationsCollection()

            val notification = aPresidentNotification(id = "explicit-id")
            every { mockNotificationsCollection.document("explicit-id") } returns mockDocRef
            val voidTask = mockk<Task<Void>>()
            every { mockDocRef.set(any()) } returns voidTask
            coEvery { voidTask.await() } returns mockk()

            dataSource.createNotification(clubId, notification)

            verify { mockNotificationsCollection.document("explicit-id") }
            verify { mockDocRef.set(any()) }
        }

    @Test
    fun `createNotification generates uuid when notification id is empty`() =
        runTest {
            mockkStatic("kotlinx.coroutines.tasks.TasksKt")
            setupNotificationsCollection()

            val notification = aPresidentNotification(id = "")
            every { mockNotificationsCollection.document(any()) } returns mockDocRef
            val voidTask = mockk<Task<Void>>()
            every { mockDocRef.set(any()) } returns voidTask
            coEvery { voidTask.await() } returns mockk()

            dataSource.createNotification(clubId, notification)

            // document() called with a generated UUID (non-empty string)
            verify { mockNotificationsCollection.document(match { it.isNotEmpty() }) }
            verify { mockDocRef.set(any()) }
        }

    @Test
    fun `createNotification propagates exception on failure`() =
        runTest {
            mockkStatic("kotlinx.coroutines.tasks.TasksKt")
            setupNotificationsCollection()

            val notification = aPresidentNotification()
            every { mockNotificationsCollection.document(any()) } returns mockDocRef
            val voidTask = mockk<Task<Void>>()
            every { mockDocRef.set(any()) } returns voidTask
            coEvery { voidTask.await() } throws RuntimeException("Write failed")

            try {
                dataSource.createNotification(clubId, notification)
                assert(false) { "Expected RuntimeException" }
            } catch (e: RuntimeException) {
                assertEquals("Write failed", e.message)
            }
        }
}