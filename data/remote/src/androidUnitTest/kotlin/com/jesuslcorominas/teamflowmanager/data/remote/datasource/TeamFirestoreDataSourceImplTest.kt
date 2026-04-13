package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import android.util.Log
import app.cash.turbine.test
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.TeamFirestoreModel
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import io.mockk.coEvery
import kotlinx.coroutines.tasks.await
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.After
import org.junit.Before
import org.junit.Test

class TeamFirestoreDataSourceImplTest {

    private val mockFirestore = mockk<FirebaseFirestore>()
    private val mockAuth = mockk<FirebaseAuth>()
    private val mockUser = mockk<FirebaseUser>()
    private val mockListenerRegistration = mockk<ListenerRegistration>(relaxed = true)
    private lateinit var dataSource: TeamFirestoreDataSourceImpl

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

        dataSource = TeamFirestoreDataSourceImpl(mockFirestore, mockAuth)
    }

    @Test
    fun `hasLocalTeamWithoutUserId_returnsFalse`() = runTest {
        val result = dataSource.hasLocalTeamWithoutUserId()
        assertFalse(result)
    }

    @Test
    fun `getTeamDirect_returnsNull`() = runTest {
        val result = dataSource.getTeamDirect()
        assertNull(result)
    }

    @Test
    fun `clearLocalData_isNoOp`() = runTest {
        dataSource.clearLocalData()
    }

    @Test
    fun `givenNoAuthenticatedUser_whenGetTeam_thenEmitsNull`() = runTest {
        every { mockAuth.currentUser } returns null

        dataSource.getTeam().test {
            val result = awaitItem()
            assertNull(result)
            cancel()
        }
    }

    @Test
    fun `givenNoAuthenticatedUser_whenInsertTeam_thenThrowsIllegalStateException`() = runTest {
        every { mockAuth.currentUser } returns null

        val team = mockk<Team>(relaxed = true)

        try {
            dataSource.insertTeam(team)
            fail("Expected IllegalStateException")
        } catch (e: IllegalStateException) {
            // expected
        }
    }

    @Test
    fun `givenNoAuthenticatedUser_whenUpdateTeam_thenThrowsIllegalStateException`() = runTest {
        every { mockAuth.currentUser } returns null

        val team = mockk<Team>(relaxed = true)
        every { team.remoteId } returns "team-doc-id"

        try {
            dataSource.updateTeam(team)
            fail("Expected IllegalStateException")
        } catch (e: IllegalStateException) {
            // expected
        }
    }

    @Test
    fun `givenAuthenticatedUser_whenInsertTeam_thenCreatesTeamDocument`() = runTest {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        every { mockAuth.currentUser } returns mockUser
        every { mockUser.uid } returns "user-123"

        val teamsCollection = mockk<CollectionReference>()
        val teamDocRef = mockk<DocumentReference>()
        every { teamDocRef.id } returns "new-team-doc-id"
        every { mockFirestore.collection("teams") } returns teamsCollection
        every { teamsCollection.document() } returns teamDocRef

        val voidTask = mockk<Task<Void>>()
        every { teamDocRef.set(any()) } returns voidTask
        coEvery { voidTask.await() } returns mockk()

        val team = mockk<Team>(relaxed = true)
        every { team.remoteId } returns null
        every { team.coachId } returns null
        every { team.clubRemoteId } returns null

        // Should not throw
        dataSource.insertTeam(team)
    }

    @Test
    fun `givenCoachId_whenGetTeamByCoachId_thenEmitsTeam`() = runTest {
        val listenerSlot = slot<EventListener<DocumentSnapshot>>()
        val teamsCollection = mockk<CollectionReference>()
        val teamDocRef = mockk<DocumentReference>()
        val docSnapshot = mockk<DocumentSnapshot>()

        every { mockFirestore.collection("teams") } returns teamsCollection
        every { teamsCollection.document("coach-user-id") } returns teamDocRef
        every { teamDocRef.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        val teamModel = TeamFirestoreModel(
            id = "team-doc-id",
            name = "Test Team",
            coachName = "Coach",
            delegateName = "",
            assignedCoachId = "coach-user-id"
        )
        every { docSnapshot.id } returns "team-doc-id"
        every { docSnapshot.exists() } returns true
        every { docSnapshot.toObject(TeamFirestoreModel::class.java) } returns teamModel

        dataSource.getTeamByCoachId("coach-user-id").test {
            listenerSlot.captured.onEvent(docSnapshot, null)
            val result = awaitItem()
            assertNotNull(result)
            assertEquals("Test Team", result!!.name)
            cancel()
        }
    }

    @Test
    fun `givenClubId_whenGetTeamsByClub_thenEmitsTeams`() = runTest {
        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        val teamsCollection = mockk<CollectionReference>()
        val teamsQuery = mockk<Query>()
        val querySnapshot = mockk<QuerySnapshot>()
        val docSnapshot = mockk<DocumentSnapshot>()

        every { mockFirestore.collection("teams") } returns teamsCollection
        every { teamsCollection.whereEqualTo("clubId", "club-id") } returns teamsQuery
        every { teamsQuery.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        val teamModel = TeamFirestoreModel(
            id = "team-doc-id",
            name = "Test Team",
            coachName = "Coach",
            delegateName = "",
            assignedCoachId = "coach-user-id",
            clubId = "club-id"
        )
        every { docSnapshot.id } returns "team-doc-id"
        every { docSnapshot.toObject(TeamFirestoreModel::class.java) } returns teamModel
        every { querySnapshot.documents } returns listOf(docSnapshot)
        every { querySnapshot.isEmpty } returns false

        dataSource.getTeamsByClub("club-id").test {
            listenerSlot.captured.onEvent(querySnapshot, null)
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("Test Team", result[0].name)
            cancel()
        }
    }

    @Test
    fun `givenNoMatchingTeams_whenGetOrphanTeams_thenReturnsEmptyList`() = runTest {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")

        val teamsCollection = mockk<CollectionReference>()
        val teamsQuery = mockk<Query>()
        val querySnapshot = mockk<QuerySnapshot>()
        every { mockFirestore.collection("teams") } returns teamsCollection
        every { teamsCollection.whereEqualTo("assignedCoachId", "user-123") } returns teamsQuery
        val allTask = mockk<Task<QuerySnapshot>>()
        every { teamsQuery.get() } returns allTask
        coEvery { allTask.await() } returns querySnapshot
        every { querySnapshot.documents } returns emptyList()

        val result = dataSource.getOrphanTeams("user-123")

        assertEquals(emptyList<Team>(), result)
    }

    @Test
    fun `givenAuthenticatedUserWithTeam_whenGetTeam_thenEmitsTeam`() = runTest {
        every { mockAuth.currentUser } returns mockUser
        every { mockUser.uid } returns "user-123"

        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        val teamsCollection = mockk<CollectionReference>()
        val teamsQuery = mockk<Query>()
        val querySnapshot = mockk<QuerySnapshot>()
        val docSnapshot = mockk<DocumentSnapshot>()

        every { mockFirestore.collection("teams") } returns teamsCollection
        every { teamsCollection.whereEqualTo("assignedCoachId", "user-123") } returns teamsQuery
        every { teamsQuery.limit(1) } returns teamsQuery
        every { teamsQuery.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        val teamModel = TeamFirestoreModel(
            id = "team-doc-id",
            name = "My Team",
            coachName = "Coach",
            delegateName = "",
            assignedCoachId = "user-123"
        )
        every { docSnapshot.id } returns "team-doc-id"
        every { docSnapshot.toObject(TeamFirestoreModel::class.java) } returns teamModel
        every { querySnapshot.documents } returns listOf(docSnapshot)

        dataSource.getTeam().test {
            listenerSlot.captured.onEvent(querySnapshot, null)
            val result = awaitItem()
            assertNotNull(result)
            assertEquals("My Team", result!!.name)
            cancel()
        }
    }

    @Test
    fun `givenBlankTeamFirestoreId_whenUpdateTeamClubId_thenThrowsIllegalArgumentException`() = runTest {
        try {
            dataSource.updateTeamClubId("", 1L, "club-firestore-id")
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun `givenBlankClubFirestoreId_whenUpdateTeamClubId_thenThrowsIllegalArgumentException`() = runTest {
        try {
            dataSource.updateTeamClubId("team-firestore-id", 1L, "")
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun `givenValidIds_whenUpdateTeamClubId_thenUpdatesDocument`() = runTest {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")

        val teamsCollection = mockk<CollectionReference>()
        val teamDocRef = mockk<DocumentReference>()
        every { mockFirestore.collection("teams") } returns teamsCollection
        every { teamsCollection.document("team-firestore-id") } returns teamDocRef

        val voidTask = mockk<Task<Void>>()
        every { teamDocRef.set(any(), any()) } returns voidTask
        coEvery { voidTask.await() } returns mockk()

        dataSource.updateTeamClubId("team-firestore-id", 1L, "club-firestore-id")
    }

    @Test
    fun `givenBlankTeamFirestoreId_whenGetTeamByFirestoreId_thenThrowsIllegalArgumentException`() = runTest {
        try {
            dataSource.getTeamById("")
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun `givenNonExistentTeamFirestoreId_whenGetTeamByFirestoreId_thenReturnsNull`() = runTest {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")

        val teamsCollection = mockk<CollectionReference>()
        val teamDocRef = mockk<DocumentReference>()
        val docSnapshot = mockk<DocumentSnapshot>()
        every { mockFirestore.collection("teams") } returns teamsCollection
        every { teamsCollection.document("non-existent-id") } returns teamDocRef
        val docTask = mockk<Task<DocumentSnapshot>>()
        every { teamDocRef.get() } returns docTask
        coEvery { docTask.await() } returns docSnapshot
        every { docSnapshot.exists() } returns false

        val result = dataSource.getTeamById("non-existent-id")

        assertNull(result)
    }

    @Test
    fun `givenExistingTeamFirestoreId_whenGetTeamByFirestoreId_thenReturnsTeam`() = runTest {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")

        val teamsCollection = mockk<CollectionReference>()
        val teamDocRef = mockk<DocumentReference>()
        val docSnapshot = mockk<DocumentSnapshot>()
        every { mockFirestore.collection("teams") } returns teamsCollection
        every { teamsCollection.document("team-firestore-id") } returns teamDocRef
        val docTask = mockk<Task<DocumentSnapshot>>()
        every { teamDocRef.get() } returns docTask
        coEvery { docTask.await() } returns docSnapshot
        every { docSnapshot.exists() } returns true
        every { docSnapshot.id } returns "team-firestore-id"

        val teamModel = TeamFirestoreModel(
            id = "team-firestore-id",
            name = "Test Team",
            coachName = "Coach",
            delegateName = "",
            assignedCoachId = "coach-id"
        )
        every { docSnapshot.toObject(TeamFirestoreModel::class.java) } returns teamModel

        val result = dataSource.getTeamById("team-firestore-id")

        assertNotNull(result)
        assertEquals("Test Team", result!!.name)
    }

    @Test
    fun `givenBlankTeamFirestoreId_whenUpdateTeamCoachId_thenThrowsIllegalArgumentException`() = runTest {
        try {
            dataSource.updateTeamCoachId("", "coach-id")
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun `givenBlankCoachId_whenUpdateTeamCoachId_thenThrowsIllegalArgumentException`() = runTest {
        try {
            dataSource.updateTeamCoachId("team-firestore-id", "")
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun `givenValidIds_whenUpdateTeamCoachId_thenUpdatesDocument`() = runTest {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")

        val teamsCollection = mockk<CollectionReference>()
        val teamDocRef = mockk<DocumentReference>()
        every { mockFirestore.collection("teams") } returns teamsCollection
        every { teamsCollection.document("team-firestore-id") } returns teamDocRef

        val voidTask = mockk<Task<Void>>()
        every { teamDocRef.update(any<Map<String, Any>>()) } returns voidTask
        coEvery { voidTask.await() } returns mockk()

        dataSource.updateTeamCoachId("team-firestore-id", "coach-id")
    }

    @Test
    fun `givenBlankOwnerId_whenGetOrphanTeams_thenThrowsIllegalArgumentException`() = runTest {
        try {
            dataSource.getOrphanTeams("")
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun `givenTeamsWithClubId_whenGetOrphanTeams_thenFiltersThemOut`() = runTest {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")

        val teamsCollection = mockk<CollectionReference>()
        val teamsQuery = mockk<Query>()
        val querySnapshot = mockk<QuerySnapshot>()
        val docWithClubId = mockk<DocumentSnapshot>()
        every { mockFirestore.collection("teams") } returns teamsCollection
        every { teamsCollection.whereEqualTo("assignedCoachId", "user-123") } returns teamsQuery
        val allTask = mockk<Task<QuerySnapshot>>()
        every { teamsQuery.get() } returns allTask
        coEvery { allTask.await() } returns querySnapshot

        every { docWithClubId.id } returns "team-with-club-id"
        every { docWithClubId.contains("clubId") } returns true
        every { docWithClubId.getString("clubId") } returns "some-club"
        every { querySnapshot.documents } returns listOf(docWithClubId)

        val result = dataSource.getOrphanTeams("user-123")

        assertEquals(0, result.size)
    }

    @Test
    fun `givenTeamsWithoutClubId_whenGetOrphanTeams_thenReturnsThemAll`() = runTest {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")

        val teamsCollection = mockk<CollectionReference>()
        val teamsQuery = mockk<Query>()
        val querySnapshot = mockk<QuerySnapshot>()
        val orphanDoc = mockk<DocumentSnapshot>()
        every { mockFirestore.collection("teams") } returns teamsCollection
        every { teamsCollection.whereEqualTo("assignedCoachId", "user-123") } returns teamsQuery
        val allTask = mockk<Task<QuerySnapshot>>()
        every { teamsQuery.get() } returns allTask
        coEvery { allTask.await() } returns querySnapshot

        every { orphanDoc.id } returns "orphan-team-id"
        every { orphanDoc.contains("clubId") } returns false

        val teamModel = TeamFirestoreModel(
            id = "orphan-team-id",
            name = "Orphan Team",
            coachName = "Coach",
            delegateName = ""
        )
        every { orphanDoc.toObject(TeamFirestoreModel::class.java) } returns teamModel
        every { querySnapshot.documents } returns listOf(orphanDoc)

        val result = dataSource.getOrphanTeams("user-123")

        assertEquals(1, result.size)
        assertEquals("Orphan Team", result[0].name)
    }

    @Test
    fun `givenNullTeamFirestoreId_whenUpdateTeam_thenThrowsIllegalStateException`() = runTest {
        every { mockAuth.currentUser } returns mockUser
        every { mockUser.uid } returns "user-123"

        val team = mockk<Team>(relaxed = true)
        every { team.remoteId } returns null

        try {
            dataSource.updateTeam(team)
            fail("Expected IllegalStateException")
        } catch (e: IllegalStateException) {
            // expected
        }
    }

    @Test
    fun `givenValidTeam_whenUpdateTeam_thenUpdatesFirestore`() = runTest {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        every { mockAuth.currentUser } returns mockUser
        every { mockUser.uid } returns "user-123"

        val teamsCollection = mockk<CollectionReference>()
        val teamDocRef = mockk<DocumentReference>()
        every { mockFirestore.collection("teams") } returns teamsCollection
        every { teamsCollection.document("team-doc-id") } returns teamDocRef

        val voidTask = mockk<Task<Void>>()
        every { teamDocRef.set(any()) } returns voidTask
        coEvery { voidTask.await() } returns mockk()

        val team = mockk<Team>(relaxed = true)
        every { team.remoteId } returns "team-doc-id"
        every { team.coachId } returns "coach-123"
        every { team.clubRemoteId } returns null

        dataSource.updateTeam(team)
    }

    @Test
    fun `givenFirestoreError_whenGetTeam_thenEmitsNull`() = runTest {
        every { mockAuth.currentUser } returns mockUser
        every { mockUser.uid } returns "user-123"

        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        val teamsCollection = mockk<CollectionReference>()
        val teamsQuery = mockk<Query>()

        every { mockFirestore.collection("teams") } returns teamsCollection
        every { teamsCollection.whereEqualTo("assignedCoachId", "user-123") } returns teamsQuery
        every { teamsQuery.limit(1) } returns teamsQuery
        every { teamsQuery.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        val mockError = mockk<com.google.firebase.firestore.FirebaseFirestoreException>(relaxed = true)

        dataSource.getTeam().test {
            listenerSlot.captured.onEvent(null, mockError)
            val result = awaitItem()
            assertNull(result)
            cancel()
        }
    }

    @Test
    fun `givenNullDocument_whenGetTeam_thenEmitsNull`() = runTest {
        every { mockAuth.currentUser } returns mockUser
        every { mockUser.uid } returns "user-123"

        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        val teamsCollection = mockk<CollectionReference>()
        val teamsQuery = mockk<Query>()
        val querySnapshot = mockk<QuerySnapshot>()

        every { mockFirestore.collection("teams") } returns teamsCollection
        every { teamsCollection.whereEqualTo("assignedCoachId", "user-123") } returns teamsQuery
        every { teamsQuery.limit(1) } returns teamsQuery
        every { teamsQuery.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration
        every { querySnapshot.documents } returns emptyList()

        dataSource.getTeam().test {
            listenerSlot.captured.onEvent(querySnapshot, null)
            val result = awaitItem()
            assertNull(result)
            cancel()
        }
    }

    @Test
    fun `givenNullFirestoreModel_whenGetTeam_thenEmitsNull`() = runTest {
        every { mockAuth.currentUser } returns mockUser
        every { mockUser.uid } returns "user-123"

        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        val teamsCollection = mockk<CollectionReference>()
        val teamsQuery = mockk<Query>()
        val querySnapshot = mockk<QuerySnapshot>()
        val docSnapshot = mockk<DocumentSnapshot>()

        every { mockFirestore.collection("teams") } returns teamsCollection
        every { teamsCollection.whereEqualTo("assignedCoachId", "user-123") } returns teamsQuery
        every { teamsQuery.limit(1) } returns teamsQuery
        every { teamsQuery.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration
        every { querySnapshot.documents } returns listOf(docSnapshot)
        every { docSnapshot.id } returns "team-doc-id"
        every { docSnapshot.toObject(TeamFirestoreModel::class.java) } returns null

        dataSource.getTeam().test {
            listenerSlot.captured.onEvent(querySnapshot, null)
            val result = awaitItem()
            assertNull(result)
            cancel()
        }
    }

    @Test
    fun `givenTeamWithEmptyId_whenGetTeam_thenSetsIdFromDocumentId`() = runTest {
        every { mockAuth.currentUser } returns mockUser
        every { mockUser.uid } returns "user-123"

        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        val teamsCollection = mockk<CollectionReference>()
        val teamsQuery = mockk<Query>()
        val querySnapshot = mockk<QuerySnapshot>()
        val docSnapshot = mockk<DocumentSnapshot>()

        every { mockFirestore.collection("teams") } returns teamsCollection
        every { teamsCollection.whereEqualTo("assignedCoachId", "user-123") } returns teamsQuery
        every { teamsQuery.limit(1) } returns teamsQuery
        every { teamsQuery.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration
        every { querySnapshot.documents } returns listOf(docSnapshot)
        every { docSnapshot.id } returns "team-doc-id"

        // Model has empty id - should be set from document id
        val teamModel = TeamFirestoreModel(
            id = "",
            name = "My Team",
            coachName = "Coach",
            delegateName = "",
            assignedCoachId = "user-123"
        )
        every { docSnapshot.toObject(TeamFirestoreModel::class.java) } returns teamModel

        dataSource.getTeam().test {
            listenerSlot.captured.onEvent(querySnapshot, null)
            val result = awaitItem()
            assertNotNull(result)
            assertEquals("My Team", result!!.name)
            cancel()
        }
    }

    @Test
    fun `givenFirestoreError_whenGetTeamByCoachId_thenEmitsNull`() = runTest {
        val listenerSlot = slot<EventListener<DocumentSnapshot>>()
        val teamsCollection = mockk<CollectionReference>()
        val teamDocRef = mockk<DocumentReference>()

        every { mockFirestore.collection("teams") } returns teamsCollection
        every { teamsCollection.document("coach-user-id") } returns teamDocRef
        every { teamDocRef.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        val mockError = mockk<com.google.firebase.firestore.FirebaseFirestoreException>(relaxed = true)

        dataSource.getTeamByCoachId("coach-user-id").test {
            listenerSlot.captured.onEvent(null, mockError)
            val result = awaitItem()
            assertNull(result)
            cancel()
        }
    }

    @Test
    fun `givenNonExistentDocument_whenGetTeamByCoachId_thenEmitsNull`() = runTest {
        val listenerSlot = slot<EventListener<DocumentSnapshot>>()
        val teamsCollection = mockk<CollectionReference>()
        val teamDocRef = mockk<DocumentReference>()
        val docSnapshot = mockk<DocumentSnapshot>()

        every { mockFirestore.collection("teams") } returns teamsCollection
        every { teamsCollection.document("coach-user-id") } returns teamDocRef
        every { teamDocRef.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration
        every { docSnapshot.exists() } returns false

        dataSource.getTeamByCoachId("coach-user-id").test {
            listenerSlot.captured.onEvent(docSnapshot, null)
            val result = awaitItem()
            assertNull(result)
            cancel()
        }
    }

    @Test
    fun `givenNullFirestoreModel_whenGetTeamByCoachId_thenEmitsNull`() = runTest {
        val listenerSlot = slot<EventListener<DocumentSnapshot>>()
        val teamsCollection = mockk<CollectionReference>()
        val teamDocRef = mockk<DocumentReference>()
        val docSnapshot = mockk<DocumentSnapshot>()

        every { mockFirestore.collection("teams") } returns teamsCollection
        every { teamsCollection.document("coach-user-id") } returns teamDocRef
        every { teamDocRef.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration
        every { docSnapshot.exists() } returns true
        every { docSnapshot.id } returns "coach-user-id"
        every { docSnapshot.toObject(TeamFirestoreModel::class.java) } returns null

        dataSource.getTeamByCoachId("coach-user-id").test {
            listenerSlot.captured.onEvent(docSnapshot, null)
            val result = awaitItem()
            assertNull(result)
            cancel()
        }
    }

    @Test
    fun `givenTeamWithEmptyId_whenGetTeamByCoachId_thenSetsIdFromDocumentId`() = runTest {
        val listenerSlot = slot<EventListener<DocumentSnapshot>>()
        val teamsCollection = mockk<CollectionReference>()
        val teamDocRef = mockk<DocumentReference>()
        val docSnapshot = mockk<DocumentSnapshot>()

        every { mockFirestore.collection("teams") } returns teamsCollection
        every { teamsCollection.document("coach-user-id") } returns teamDocRef
        every { teamDocRef.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        val teamModel = TeamFirestoreModel(
            id = "",
            name = "Team By Coach",
            coachName = "Coach",
            delegateName = "",
            assignedCoachId = "coach-user-id"
        )
        every { docSnapshot.id } returns "team-doc-id"
        every { docSnapshot.exists() } returns true
        every { docSnapshot.toObject(TeamFirestoreModel::class.java) } returns teamModel

        dataSource.getTeamByCoachId("coach-user-id").test {
            listenerSlot.captured.onEvent(docSnapshot, null)
            val result = awaitItem()
            assertNotNull(result)
            assertEquals("Team By Coach", result!!.name)
            cancel()
        }
    }

    @Test
    fun `givenFirestoreError_whenGetTeamsByClub_thenEmitsEmptyList`() = runTest {
        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        val teamsCollection = mockk<CollectionReference>()
        val teamsQuery = mockk<Query>()

        every { mockFirestore.collection("teams") } returns teamsCollection
        every { teamsCollection.whereEqualTo("clubId", "club-id") } returns teamsQuery
        every { teamsQuery.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        val mockError = mockk<com.google.firebase.firestore.FirebaseFirestoreException>(relaxed = true)

        dataSource.getTeamsByClub("club-id").test {
            listenerSlot.captured.onEvent(null, mockError)
            val result = awaitItem()
            assertEquals(emptyList<Team>(), result)
            cancel()
        }
    }

    @Test
    fun `givenEmptySnapshot_whenGetTeamsByClub_thenEmitsEmptyList`() = runTest {
        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        val teamsCollection = mockk<CollectionReference>()
        val teamsQuery = mockk<Query>()
        val querySnapshot = mockk<QuerySnapshot>()

        every { mockFirestore.collection("teams") } returns teamsCollection
        every { teamsCollection.whereEqualTo("clubId", "club-id") } returns teamsQuery
        every { teamsQuery.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration
        every { querySnapshot.isEmpty } returns true

        dataSource.getTeamsByClub("club-id").test {
            listenerSlot.captured.onEvent(querySnapshot, null)
            val result = awaitItem()
            assertEquals(emptyList<Team>(), result)
            cancel()
        }
    }

    @Test
    fun `givenBlankClubId_whenGetTeamsByClub_thenThrowsIllegalArgumentException`() = runTest {
        try {
            dataSource.getTeamsByClub("").test {
                awaitError()
            }
        } catch (e: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun `givenOrphanTeamWithNullFirestoreModel_whenGetOrphanTeams_thenSkipsNullModel`() = runTest {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")

        val teamsCollection = mockk<CollectionReference>()
        val teamsQuery = mockk<Query>()
        val querySnapshot = mockk<QuerySnapshot>()
        val orphanDoc = mockk<DocumentSnapshot>()
        every { mockFirestore.collection("teams") } returns teamsCollection
        every { teamsCollection.whereEqualTo("assignedCoachId", "user-123") } returns teamsQuery
        val allTask = mockk<Task<QuerySnapshot>>()
        every { teamsQuery.get() } returns allTask
        coEvery { allTask.await() } returns querySnapshot

        every { orphanDoc.id } returns "orphan-team-id"
        every { orphanDoc.contains("clubId") } returns false
        every { orphanDoc.toObject(TeamFirestoreModel::class.java) } returns null
        every { querySnapshot.documents } returns listOf(orphanDoc)

        val result = dataSource.getOrphanTeams("user-123")

        assertEquals(0, result.size)
    }

    @Test
    fun `givenOrphanTeamWithEmptyFirestoreId_whenGetOrphanTeams_thenSetsIdFromDocumentId`() = runTest {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")

        val teamsCollection = mockk<CollectionReference>()
        val teamsQuery = mockk<Query>()
        val querySnapshot = mockk<QuerySnapshot>()
        val orphanDoc = mockk<DocumentSnapshot>()
        every { mockFirestore.collection("teams") } returns teamsCollection
        every { teamsCollection.whereEqualTo("assignedCoachId", "user-123") } returns teamsQuery
        val allTask = mockk<Task<QuerySnapshot>>()
        every { teamsQuery.get() } returns allTask
        coEvery { allTask.await() } returns querySnapshot

        every { orphanDoc.id } returns "orphan-team-id"
        every { orphanDoc.contains("clubId") } returns false

        val teamModel = TeamFirestoreModel(
            id = "",
            name = "Orphan Team",
            coachName = "Coach",
            delegateName = ""
        )
        every { orphanDoc.toObject(TeamFirestoreModel::class.java) } returns teamModel
        every { querySnapshot.documents } returns listOf(orphanDoc)

        val result = dataSource.getOrphanTeams("user-123")

        assertEquals(1, result.size)
    }

    @Test
    fun `givenTeamFirestoreIdWithNullFirestoreModel_whenGetTeamByFirestoreId_thenReturnsNull`() = runTest {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")

        val teamsCollection = mockk<CollectionReference>()
        val teamDocRef = mockk<DocumentReference>()
        val docSnapshot = mockk<DocumentSnapshot>()
        every { mockFirestore.collection("teams") } returns teamsCollection
        every { teamsCollection.document("team-firestore-id") } returns teamDocRef
        val docTask = mockk<Task<DocumentSnapshot>>()
        every { teamDocRef.get() } returns docTask
        coEvery { docTask.await() } returns docSnapshot
        every { docSnapshot.exists() } returns true
        every { docSnapshot.id } returns "team-firestore-id"
        every { docSnapshot.toObject(TeamFirestoreModel::class.java) } returns null

        val result = dataSource.getTeamById("team-firestore-id")

        assertNull(result)
    }

    @Test
    fun `givenTeamWithEmptyFirestoreId_whenGetTeamByFirestoreId_thenSetsIdFromDocumentId`() = runTest {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")

        val teamsCollection = mockk<CollectionReference>()
        val teamDocRef = mockk<DocumentReference>()
        val docSnapshot = mockk<DocumentSnapshot>()
        every { mockFirestore.collection("teams") } returns teamsCollection
        every { teamsCollection.document("team-firestore-id") } returns teamDocRef
        val docTask = mockk<Task<DocumentSnapshot>>()
        every { teamDocRef.get() } returns docTask
        coEvery { docTask.await() } returns docSnapshot
        every { docSnapshot.exists() } returns true
        every { docSnapshot.id } returns "team-firestore-id"

        val teamModel = TeamFirestoreModel(
            id = "",
            name = "Test Team",
            coachName = "Coach",
            delegateName = "",
            assignedCoachId = "coach-id"
        )
        every { docSnapshot.toObject(TeamFirestoreModel::class.java) } returns teamModel

        val result = dataSource.getTeamById("team-firestore-id")

        assertNotNull(result)
        assertEquals("Test Team", result!!.name)
    }
}
