package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.ClubFirestoreModel
import com.jesuslcorominas.teamflowmanager.data.remote.util.InvitationCodeGenerator
import io.mockk.coEvery
import kotlinx.coroutines.tasks.await
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class ClubFirestoreDataSourceImplTest {

    private val mockFirestore = mockk<FirebaseFirestore>()
    private lateinit var dataSource: ClubFirestoreDataSourceImpl

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

        dataSource = ClubFirestoreDataSourceImpl(mockFirestore)
    }

    // --- createClubWithOwner validation tests ---

    @Test
    fun `givenBlankClubName_whenCreateClubWithOwner_thenThrowsIllegalArgumentException`() = runTest {
        try {
            dataSource.createClubWithOwner("", "user-123", "John", "john@test.com")
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun `givenBlankUserId_whenCreateClubWithOwner_thenThrowsIllegalArgumentException`() = runTest {
        try {
            dataSource.createClubWithOwner("My Club", "", "John", "john@test.com")
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun `givenBlankUserName_whenCreateClubWithOwner_thenThrowsIllegalArgumentException`() = runTest {
        try {
            dataSource.createClubWithOwner("My Club", "user-123", "", "john@test.com")
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun `givenBlankUserEmail_whenCreateClubWithOwner_thenThrowsIllegalArgumentException`() = runTest {
        try {
            dataSource.createClubWithOwner("My Club", "user-123", "John", "")
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // expected
        }
    }

    // --- getClubByInvitationCode validation test ---

    @Test
    fun `givenBlankInvitationCode_whenGetClubByInvitationCode_thenThrowsIllegalArgumentException`() = runTest {
        try {
            dataSource.getClubByInvitationCode("")
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // expected
        }
    }

    // --- Happy path tests ---

    @Test
    fun `givenValidInputs_whenCreateClubWithOwner_thenCreatesClubAndReturnsIt`() = runTest {
        mockkObject(InvitationCodeGenerator)
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")

        every { InvitationCodeGenerator.generate() } returns "ABCD1234"

        val mockClubDocRef = mockk<DocumentReference>()
        val mockMemberDocRef = mockk<DocumentReference>()
        every { mockClubDocRef.id } returns "club-doc-id"
        every { mockMemberDocRef.id } returns "user-123_club-doc-id"

        val clubsCollection = mockk<CollectionReference>()
        val membersCollection = mockk<CollectionReference>()
        every { mockFirestore.collection("clubs") } returns clubsCollection
        every { mockFirestore.collection("clubMembers") } returns membersCollection
        every { clubsCollection.document() } returns mockClubDocRef
        every { membersCollection.document("user-123_club-doc-id") } returns mockMemberDocRef

        val voidTask1 = mockk<Task<Void>>()
        val voidTask2 = mockk<Task<Void>>()
        every { mockClubDocRef.set(any()) } returns voidTask1
        every { mockMemberDocRef.set(any()) } returns voidTask2
        coEvery { voidTask1.await() } returns mockk()
        coEvery { voidTask2.await() } returns mockk()

        val result = dataSource.createClubWithOwner("My Club", "user-123", "John", "john@test.com")

        assertNotNull(result)
        assertEquals("My Club", result.name)
        assertEquals("ABCD1234", result.invitationCode)
    }

    @Test
    fun `givenMatchingCode_whenGetClubByInvitationCode_thenReturnsClub`() = runTest {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")

        val clubsCollection = mockk<CollectionReference>()
        val query = mockk<Query>()
        val querySnapshot = mockk<QuerySnapshot>()
        val docSnapshot = mockk<DocumentSnapshot>()

        every { mockFirestore.collection("clubs") } returns clubsCollection
        every { clubsCollection.whereEqualTo("invitationCode", "ABCD1234") } returns query
        every { query.limit(1) } returns query
        val queryTask = mockk<Task<QuerySnapshot>>()
        every { query.get() } returns queryTask
        coEvery { queryTask.await() } returns querySnapshot
        every { querySnapshot.isEmpty } returns false
        every { querySnapshot.documents } returns listOf(docSnapshot)
        every { docSnapshot.id } returns "club-doc-id"

        val clubModel = ClubFirestoreModel(
            id = "club-doc-id",
            ownerId = "user-123",
            name = "My Club",
            invitationCode = "ABCD1234"
        )
        every { docSnapshot.toObject(ClubFirestoreModel::class.java) } returns clubModel

        val result = dataSource.getClubByInvitationCode("ABCD1234")

        assertNotNull(result)
        assertEquals("My Club", result!!.name)
        assertEquals("ABCD1234", result.invitationCode)
    }

    @Test
    fun `givenNoMatchingCode_whenGetClubByInvitationCode_thenReturnsNull`() = runTest {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")

        val clubsCollection = mockk<CollectionReference>()
        val query = mockk<Query>()
        val querySnapshot = mockk<QuerySnapshot>()

        every { mockFirestore.collection("clubs") } returns clubsCollection
        every { clubsCollection.whereEqualTo("invitationCode", "XXXXX") } returns query
        every { query.limit(1) } returns query
        val queryTask = mockk<Task<QuerySnapshot>>()
        every { query.get() } returns queryTask
        coEvery { queryTask.await() } returns querySnapshot
        every { querySnapshot.isEmpty } returns true
        every { querySnapshot.documents } returns emptyList()

        val result = dataSource.getClubByInvitationCode("XXXXX")

        assertNull(result)
    }

    @Test
    fun `givenNullDeserialization_whenGetClubByInvitationCode_thenReturnsNull`() = runTest {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")

        val clubsCollection = mockk<CollectionReference>()
        val query = mockk<Query>()
        val querySnapshot = mockk<QuerySnapshot>()
        val docSnapshot = mockk<DocumentSnapshot>()

        every { mockFirestore.collection("clubs") } returns clubsCollection
        every { clubsCollection.whereEqualTo("invitationCode", "ABCD1234") } returns query
        every { query.limit(1) } returns query
        val queryTask = mockk<Task<QuerySnapshot>>()
        every { query.get() } returns queryTask
        coEvery { queryTask.await() } returns querySnapshot
        every { querySnapshot.isEmpty } returns false
        every { querySnapshot.documents } returns listOf(docSnapshot)
        every { docSnapshot.id } returns "club-doc-id"
        every { docSnapshot.toObject(ClubFirestoreModel::class.java) } returns null

        val result = dataSource.getClubByInvitationCode("ABCD1234")

        assertNull(result)
    }

    @Test
    fun `givenDocumentWithEmptyId_whenGetClubByInvitationCode_thenSetsIdFromDocumentId`() = runTest {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")

        val clubsCollection = mockk<CollectionReference>()
        val query = mockk<Query>()
        val querySnapshot = mockk<QuerySnapshot>()
        val docSnapshot = mockk<DocumentSnapshot>()

        every { mockFirestore.collection("clubs") } returns clubsCollection
        every { clubsCollection.whereEqualTo("invitationCode", "ABCD1234") } returns query
        every { query.limit(1) } returns query
        val queryTask = mockk<Task<QuerySnapshot>>()
        every { query.get() } returns queryTask
        coEvery { queryTask.await() } returns querySnapshot
        every { querySnapshot.isEmpty } returns false
        every { querySnapshot.documents } returns listOf(docSnapshot)
        every { docSnapshot.id } returns "auto-doc-id"

        val clubModel = ClubFirestoreModel(
            id = "",
            ownerId = "user-123",
            name = "My Club",
            invitationCode = "ABCD1234"
        )
        every { docSnapshot.toObject(ClubFirestoreModel::class.java) } returns clubModel

        val result = dataSource.getClubByInvitationCode("ABCD1234")

        assertNotNull(result)
        assertEquals("auto-doc-id", result!!.remoteId)
    }
}
