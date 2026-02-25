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
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.ClubMemberFirestoreModel
import io.mockk.coEvery
import kotlinx.coroutines.tasks.await
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.After
import org.junit.Before
import org.junit.Test

class ClubMemberFirestoreDataSourceImplTest {

    private val mockFirestore = mockk<FirebaseFirestore>()
    private val mockCollection = mockk<CollectionReference>()
    private val mockQuery = mockk<Query>()
    private val mockDocRef = mockk<DocumentReference>()
    private val mockQuerySnapshot = mockk<QuerySnapshot>()
    private val mockDocSnapshot = mockk<DocumentSnapshot>()
    private val mockListenerRegistration = mockk<ListenerRegistration>(relaxed = true)

    private lateinit var dataSource: ClubMemberFirestoreDataSourceImpl

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

        dataSource = ClubMemberFirestoreDataSourceImpl(mockFirestore)
    }

    // --- Validation tests ---

    @Test
    fun `givenBlankClubFirestoreId_whenGetClubMembers_thenThrowsIllegalArgumentException`() = runTest {
        val flow = dataSource.getClubMembers("  ")
        flow.test {
            val error = awaitError()
            assertTrue(error is IllegalArgumentException)
        }
    }

    @Test
    fun `givenBlankUserId_whenCreateOrUpdateClubMember_thenThrowsIllegalArgumentException`() = runTest {
        try {
            dataSource.createOrUpdateClubMember("", "John", "j@j.com", 1L, "club-id", listOf("player"))
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun `givenBlankName_whenCreateOrUpdateClubMember_thenThrowsIllegalArgumentException`() = runTest {
        try {
            dataSource.createOrUpdateClubMember("user-123", "", "j@j.com", 1L, "club-id", listOf("player"))
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun `givenBlankEmail_whenCreateOrUpdateClubMember_thenThrowsIllegalArgumentException`() = runTest {
        try {
            dataSource.createOrUpdateClubMember("user-123", "John", "", 1L, "club-id", listOf("player"))
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun `givenBlankClubFirestoreIdInCreate_whenCreateOrUpdateClubMember_thenThrowsIllegalArgumentException`() = runTest {
        try {
            dataSource.createOrUpdateClubMember("user-123", "John", "j@j.com", 1L, "", listOf("player"))
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun `givenEmptyRoles_whenCreateOrUpdateClubMember_thenThrowsIllegalArgumentException`() = runTest {
        try {
            dataSource.createOrUpdateClubMember("user-123", "John", "j@j.com", 1L, "club-id", emptyList())
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun `givenBlankUserId_whenUpdateClubMemberRoles_thenThrowsIllegalArgumentException`() = runTest {
        try {
            dataSource.updateClubMemberRoles("", "club-id", listOf("player"))
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun `givenBlankClubFirestoreId_whenUpdateClubMemberRoles_thenThrowsIllegalArgumentException`() = runTest {
        try {
            dataSource.updateClubMemberRoles("user-123", "", listOf("player"))
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // expected
        }
    }

    // --- getClubMemberByUserId tests ---

    @Test
    fun `givenEmptyUserId_whenGetClubMemberByUserId_thenEmitsNull`() = runTest {
        dataSource.getClubMemberByUserId("").test {
            val result = awaitItem()
            assertNull(result)
            cancel()
        }
    }

    @Test
    fun `givenExistingMember_whenGetClubMemberByUserId_thenEmitsMember`() = runTest {
        val listenerSlot = slot<EventListener<QuerySnapshot>>()

        every { mockFirestore.collection("clubMembers") } returns mockCollection
        every { mockCollection.whereEqualTo("userId", "user-123") } returns mockQuery
        every { mockQuery.limit(1) } returns mockQuery
        every { mockQuery.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        val model = ClubMemberFirestoreModel(
            id = "user-123_club-id",
            userId = "user-123",
            name = "John",
            email = "j@j.com",
            clubId = "club-id",
            roles = listOf("player")
        )
        every { mockDocSnapshot.id } returns "user-123_club-id"
        every { mockDocSnapshot.toObject(ClubMemberFirestoreModel::class.java) } returns model
        every { mockQuerySnapshot.documents } returns listOf(mockDocSnapshot)

        dataSource.getClubMemberByUserId("user-123").test {
            listenerSlot.captured.onEvent(mockQuerySnapshot, null)
            val result = awaitItem()
            assertNotNull(result)
            assertEquals("user-123", result!!.userId)
            cancel()
        }
    }

    @Test
    fun `givenNoMember_whenGetClubMemberByUserId_thenEmitsNull`() = runTest {
        val listenerSlot = slot<EventListener<QuerySnapshot>>()

        every { mockFirestore.collection("clubMembers") } returns mockCollection
        every { mockCollection.whereEqualTo("userId", "no-user") } returns mockQuery
        every { mockQuery.limit(1) } returns mockQuery
        every { mockQuery.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration
        every { mockQuerySnapshot.documents } returns emptyList()

        dataSource.getClubMemberByUserId("no-user").test {
            listenerSlot.captured.onEvent(mockQuerySnapshot, null)
            val result = awaitItem()
            assertNull(result)
            cancel()
        }
    }

    @Test
    fun `givenError_whenGetClubMemberByUserId_thenEmitsNull`() = runTest {
        val listenerSlot = slot<EventListener<QuerySnapshot>>()

        every { mockFirestore.collection("clubMembers") } returns mockCollection
        every { mockCollection.whereEqualTo("userId", "user-123") } returns mockQuery
        every { mockQuery.limit(1) } returns mockQuery
        every { mockQuery.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        val mockError = mockk<FirebaseFirestoreException>(relaxed = true)

        dataSource.getClubMemberByUserId("user-123").test {
            listenerSlot.captured.onEvent(null, mockError)
            val result = awaitItem()
            assertNull(result)
            cancel()
        }
    }

    // --- getClubMembers tests ---

    @Test
    fun `givenClubMembers_whenGetClubMembers_thenEmitsMembers`() = runTest {
        val listenerSlot = slot<EventListener<QuerySnapshot>>()

        every { mockFirestore.collection("clubMembers") } returns mockCollection
        every { mockCollection.whereEqualTo("clubId", "club-id") } returns mockQuery
        every { mockQuery.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        val model = ClubMemberFirestoreModel(
            id = "user-123_club-id",
            userId = "user-123",
            name = "John",
            email = "j@j.com",
            clubId = "club-id",
            roles = listOf("player")
        )
        every { mockDocSnapshot.id } returns "user-123_club-id"
        every { mockDocSnapshot.toObject(ClubMemberFirestoreModel::class.java) } returns model
        every { mockQuerySnapshot.documents } returns listOf(mockDocSnapshot)
        every { mockQuerySnapshot.isEmpty } returns false

        dataSource.getClubMembers("club-id").test {
            listenerSlot.captured.onEvent(mockQuerySnapshot, null)
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("user-123", result[0].userId)
            cancel()
        }
    }

    @Test
    fun `givenEmptySnapshot_whenGetClubMembers_thenEmitsEmptyList`() = runTest {
        val listenerSlot = slot<EventListener<QuerySnapshot>>()

        every { mockFirestore.collection("clubMembers") } returns mockCollection
        every { mockCollection.whereEqualTo("clubId", "club-id") } returns mockQuery
        every { mockQuery.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        every { mockQuerySnapshot.documents } returns emptyList()
        every { mockQuerySnapshot.isEmpty } returns true

        dataSource.getClubMembers("club-id").test {
            listenerSlot.captured.onEvent(mockQuerySnapshot, null)
            val result = awaitItem()
            assertEquals(0, result.size)
            cancel()
        }
    }

    @Test
    fun `givenError_whenGetClubMembers_thenEmitsEmptyList`() = runTest {
        val listenerSlot = slot<EventListener<QuerySnapshot>>()

        every { mockFirestore.collection("clubMembers") } returns mockCollection
        every { mockCollection.whereEqualTo("clubId", "club-id") } returns mockQuery
        every { mockQuery.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        val mockError = mockk<FirebaseFirestoreException>(relaxed = true)

        dataSource.getClubMembers("club-id").test {
            listenerSlot.captured.onEvent(null, mockError)
            val result = awaitItem()
            assertEquals(0, result.size)
            cancel()
        }
    }

    // --- createOrUpdateClubMember ---

    @Test
    fun `givenValidInputs_whenCreateOrUpdateClubMember_thenReturnsMember`() = runTest {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")

        every { mockFirestore.collection("clubMembers") } returns mockCollection
        every { mockCollection.document("user-123_club-id") } returns mockDocRef
        val voidTask = mockk<Task<Void>>()
        every { mockDocRef.set(any()) } returns voidTask
        coEvery { voidTask.await() } returns mockk()

        val result = dataSource.createOrUpdateClubMember(
            "user-123", "John", "j@j.com", 1L, "club-id", listOf("player")
        )

        assertNotNull(result)
        assertEquals("user-123", result.userId)
        assertEquals(listOf("player"), result.roles)
    }

    // --- updateClubMemberRoles ---

    @Test
    fun `givenValidInputs_whenUpdateClubMemberRoles_thenUpdatesRoles`() = runTest {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")

        every { mockFirestore.collection("clubMembers") } returns mockCollection
        every { mockCollection.document("user-123_club-id") } returns mockDocRef
        val voidTask = mockk<Task<Void>>()
        every { mockDocRef.update(any<Map<String, Any>>()) } returns voidTask
        coEvery { voidTask.await() } returns mockk()

        // Should not throw
        dataSource.updateClubMemberRoles("user-123", "club-id", listOf("player", "coach"))
    }

    // --- addClubMemberRole ---

    @Test
    fun `givenRoleNotPresent_whenAddClubMemberRole_thenAddsRole`() = runTest {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")

        every { mockFirestore.collection("clubMembers") } returns mockCollection
        every { mockCollection.document("user-123_club-id") } returns mockDocRef

        val getTask = mockk<Task<DocumentSnapshot>>()
        every { mockDocRef.get() } returns getTask
        coEvery { getTask.await() } returns mockDocSnapshot
        every { mockDocSnapshot.exists() } returns true

        val model = ClubMemberFirestoreModel(
            id = "user-123_club-id",
            userId = "user-123",
            name = "John",
            email = "j@j.com",
            clubId = "club-id",
            roles = listOf("player")
        )
        every { mockDocSnapshot.toObject(ClubMemberFirestoreModel::class.java) } returns model

        val voidTask = mockk<Task<Void>>()
        every { mockDocRef.update(any<Map<String, Any>>()) } returns voidTask
        coEvery { voidTask.await() } returns mockk()

        // Should not throw
        dataSource.addClubMemberRole("user-123", "club-id", "coach")
    }

    @Test
    fun `givenRoleAlreadyPresent_whenAddClubMemberRole_thenDoesNotUpdate`() = runTest {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")

        every { mockFirestore.collection("clubMembers") } returns mockCollection
        every { mockCollection.document("user-123_club-id") } returns mockDocRef

        val getTask = mockk<Task<DocumentSnapshot>>()
        every { mockDocRef.get() } returns getTask
        coEvery { getTask.await() } returns mockDocSnapshot
        every { mockDocSnapshot.exists() } returns true

        val model = ClubMemberFirestoreModel(
            id = "user-123_club-id",
            userId = "user-123",
            name = "John",
            email = "j@j.com",
            clubId = "club-id",
            roles = listOf("player", "coach")
        )
        every { mockDocSnapshot.toObject(ClubMemberFirestoreModel::class.java) } returns model

        // Should not throw and should not call update
        dataSource.addClubMemberRole("user-123", "club-id", "coach")
    }

    @Test
    fun `givenMemberNotFound_whenAddClubMemberRole_thenThrowsIllegalStateException`() = runTest {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")

        every { mockFirestore.collection("clubMembers") } returns mockCollection
        every { mockCollection.document("user-123_club-id") } returns mockDocRef

        val getTask = mockk<Task<DocumentSnapshot>>()
        every { mockDocRef.get() } returns getTask
        coEvery { getTask.await() } returns mockDocSnapshot
        every { mockDocSnapshot.exists() } returns false

        try {
            dataSource.addClubMemberRole("user-123", "club-id", "coach")
            fail("Expected IllegalStateException")
        } catch (e: IllegalStateException) {
            // expected
        }
    }

    @Test
    fun `givenBlankUserId_whenAddClubMemberRole_thenThrowsIllegalArgumentException`() = runTest {
        try {
            dataSource.addClubMemberRole("", "club-id", "player")
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun `givenBlankClubFirestoreId_whenAddClubMemberRole_thenThrowsIllegalArgumentException`() = runTest {
        try {
            dataSource.addClubMemberRole("user-123", "", "player")
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun `givenBlankRole_whenAddClubMemberRole_thenThrowsIllegalArgumentException`() = runTest {
        try {
            dataSource.addClubMemberRole("user-123", "club-id", "")
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun `givenBlankUserId_whenGetClubMemberByUserIdAndClub_thenThrowsIllegalArgumentException`() = runTest {
        try {
            dataSource.getClubMemberByUserIdAndClub("", "club-id")
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun `givenBlankClubFirestoreId_whenGetClubMemberByUserIdAndClub_thenThrowsIllegalArgumentException`() = runTest {
        try {
            dataSource.getClubMemberByUserIdAndClub("user-123", "")
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun `givenEmptyRoles_whenUpdateClubMemberRoles_thenThrowsIllegalArgumentException`() = runTest {
        try {
            dataSource.updateClubMemberRoles("user-123", "club-id", emptyList())
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun `givenNetworkError_whenCreateOrUpdateClubMember_thenPropagatesException`() = runTest {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")

        every { mockFirestore.collection("clubMembers") } returns mockCollection
        every { mockCollection.document("user-123_club-id") } returns mockDocRef
        val voidTask = mockk<Task<Void>>()
        every { mockDocRef.set(any()) } returns voidTask
        coEvery { voidTask.await() } throws RuntimeException("Network error")

        try {
            dataSource.createOrUpdateClubMember("user-123", "John", "j@j.com", 1L, "club-id", listOf("player"))
            fail("Expected RuntimeException")
        } catch (e: RuntimeException) {
            // expected
        }
    }

    @Test
    fun `givenNetworkError_whenUpdateClubMemberRoles_thenPropagatesException`() = runTest {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")

        every { mockFirestore.collection("clubMembers") } returns mockCollection
        every { mockCollection.document("user-123_club-id") } returns mockDocRef
        val voidTask = mockk<Task<Void>>()
        every { mockDocRef.update(any<Map<String, Any>>()) } returns voidTask
        coEvery { voidTask.await() } throws RuntimeException("Network error")

        try {
            dataSource.updateClubMemberRoles("user-123", "club-id", listOf("player"))
            fail("Expected RuntimeException")
        } catch (e: RuntimeException) {
            // expected
        }
    }

    @Test
    fun `givenNetworkError_whenGetClubMemberByUserIdAndClub_thenPropagatesException`() = runTest {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")

        every { mockFirestore.collection("clubMembers") } returns mockCollection
        every { mockCollection.document("user-123_club-id") } returns mockDocRef

        val getTask = mockk<Task<DocumentSnapshot>>()
        every { mockDocRef.get() } returns getTask
        coEvery { getTask.await() } throws RuntimeException("Network error")

        try {
            dataSource.getClubMemberByUserIdAndClub("user-123", "club-id")
            fail("Expected RuntimeException")
        } catch (e: RuntimeException) {
            // expected
        }
    }

    @Test
    fun `givenMemberWithEmptyModelId_whenGetClubMemberByUserIdAndClub_thenUsesDocumentId`() = runTest {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")

        every { mockFirestore.collection("clubMembers") } returns mockCollection
        every { mockCollection.document("user-123_club-id") } returns mockDocRef

        val getTask = mockk<Task<DocumentSnapshot>>()
        every { mockDocRef.get() } returns getTask
        coEvery { getTask.await() } returns mockDocSnapshot
        every { mockDocSnapshot.exists() } returns true
        every { mockDocSnapshot.id } returns "user-123_club-id"

        // Model with empty id to test the id fallback branch
        val model = ClubMemberFirestoreModel(
            id = "",
            userId = "user-123",
            name = "John",
            email = "j@j.com",
            clubId = "club-id",
            roles = listOf("player")
        )
        every { mockDocSnapshot.toObject(ClubMemberFirestoreModel::class.java) } returns model

        val result = dataSource.getClubMemberByUserIdAndClub("user-123", "club-id")

        assertNotNull(result)
        assertEquals("user-123", result!!.userId)
    }

    @Test
    fun `givenMemberWithEmptyModelId_whenGetClubMemberByUserId_thenUsesDocumentId`() = runTest {
        val listenerSlot = slot<EventListener<QuerySnapshot>>()

        every { mockFirestore.collection("clubMembers") } returns mockCollection
        every { mockCollection.whereEqualTo("userId", "user-123") } returns mockQuery
        every { mockQuery.limit(1) } returns mockQuery
        every { mockQuery.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        // Model with empty id to exercise the copy(id = documentId) branch
        val model = ClubMemberFirestoreModel(
            id = "",
            userId = "user-123",
            name = "John",
            email = "j@j.com",
            clubId = "club-id",
            roles = listOf("player")
        )
        every { mockDocSnapshot.id } returns "user-123_club-id"
        every { mockDocSnapshot.toObject(ClubMemberFirestoreModel::class.java) } returns model
        every { mockQuerySnapshot.documents } returns listOf(mockDocSnapshot)

        dataSource.getClubMemberByUserId("user-123").test {
            listenerSlot.captured.onEvent(mockQuerySnapshot, null)
            val result = awaitItem()
            assertNotNull(result)
            assertEquals("user-123", result!!.userId)
            cancel()
        }
    }

    @Test
    fun `givenMemberWithEmptyModelId_whenGetClubMembers_thenUsesDocumentId`() = runTest {
        val listenerSlot = slot<EventListener<QuerySnapshot>>()

        every { mockFirestore.collection("clubMembers") } returns mockCollection
        every { mockCollection.whereEqualTo("clubId", "club-id") } returns mockQuery
        every { mockQuery.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        // Model with empty id to exercise the copy(id = documentId) branch
        val model = ClubMemberFirestoreModel(
            id = "",
            userId = "user-123",
            name = "John",
            email = "j@j.com",
            clubId = "club-id",
            roles = listOf("player")
        )
        every { mockDocSnapshot.id } returns "user-123_club-id"
        every { mockDocSnapshot.toObject(ClubMemberFirestoreModel::class.java) } returns model
        every { mockQuerySnapshot.documents } returns listOf(mockDocSnapshot)
        every { mockQuerySnapshot.isEmpty } returns false

        dataSource.getClubMembers("club-id").test {
            listenerSlot.captured.onEvent(mockQuerySnapshot, null)
            val result = awaitItem()
            assertEquals(1, result.size)
            cancel()
        }
    }

    @Test
    fun `givenNullSnapshot_whenGetClubMembers_thenEmitsEmptyList`() = runTest {
        val listenerSlot = slot<EventListener<QuerySnapshot>>()

        every { mockFirestore.collection("clubMembers") } returns mockCollection
        every { mockCollection.whereEqualTo("clubId", "club-id") } returns mockQuery
        every { mockQuery.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        dataSource.getClubMembers("club-id").test {
            listenerSlot.captured.onEvent(null, null)
            val result = awaitItem()
            assertEquals(0, result.size)
            cancel()
        }
    }

    @Test
    fun `givenNetworkErrorOnAddRole_whenAddClubMemberRole_thenPropagatesException`() = runTest {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")

        every { mockFirestore.collection("clubMembers") } returns mockCollection
        every { mockCollection.document("user-123_club-id") } returns mockDocRef

        val getTask = mockk<Task<DocumentSnapshot>>()
        every { mockDocRef.get() } returns getTask
        coEvery { getTask.await() } throws RuntimeException("Network error")

        try {
            dataSource.addClubMemberRole("user-123", "club-id", "coach")
            fail("Expected RuntimeException")
        } catch (e: RuntimeException) {
            // expected
        }
    }

    // --- getClubMemberByUserIdAndClub ---

    @Test
    fun `givenExistingMember_whenGetClubMemberByUserIdAndClub_thenReturnsMember`() = runTest {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")

        every { mockFirestore.collection("clubMembers") } returns mockCollection
        every { mockCollection.document("user-123_club-id") } returns mockDocRef

        val getTask = mockk<Task<DocumentSnapshot>>()
        every { mockDocRef.get() } returns getTask
        coEvery { getTask.await() } returns mockDocSnapshot
        every { mockDocSnapshot.exists() } returns true
        every { mockDocSnapshot.id } returns "user-123_club-id"

        val model = ClubMemberFirestoreModel(
            id = "user-123_club-id",
            userId = "user-123",
            name = "John",
            email = "j@j.com",
            clubId = "club-id",
            roles = listOf("player")
        )
        every { mockDocSnapshot.toObject(ClubMemberFirestoreModel::class.java) } returns model

        val result = dataSource.getClubMemberByUserIdAndClub("user-123", "club-id")

        assertNotNull(result)
        assertEquals("user-123", result!!.userId)
    }

    @Test
    fun `givenNoMember_whenGetClubMemberByUserIdAndClub_thenReturnsNull`() = runTest {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")

        every { mockFirestore.collection("clubMembers") } returns mockCollection
        every { mockCollection.document("user-123_club-id") } returns mockDocRef

        val getTask = mockk<Task<DocumentSnapshot>>()
        every { mockDocRef.get() } returns getTask
        coEvery { getTask.await() } returns mockDocSnapshot
        every { mockDocSnapshot.exists() } returns false

        val result = dataSource.getClubMemberByUserIdAndClub("user-123", "club-id")

        assertNull(result)
    }
}
