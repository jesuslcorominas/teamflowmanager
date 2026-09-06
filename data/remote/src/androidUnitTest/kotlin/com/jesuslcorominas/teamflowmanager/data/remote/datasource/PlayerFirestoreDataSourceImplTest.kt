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
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.jesuslcorominas.teamflowmanager.data.core.datasource.ImageStorageDataSource
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.PlayerFirestoreModel
import com.jesuslcorominas.teamflowmanager.domain.model.Player
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.After
import org.junit.Before
import org.junit.Test

class PlayerFirestoreDataSourceImplTest {

    private val mockFirestore = mockk<FirebaseFirestore>()
    private val mockAuth = mockk<FirebaseAuth>()
    private val mockUser = mockk<FirebaseUser>()
    private val mockImageStorage = mockk<ImageStorageDataSource>()
    private val mockListenerRegistration = mockk<ListenerRegistration>(relaxed = true)
    private lateinit var dataSource: PlayerFirestoreDataSourceImpl

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

        dataSource = PlayerFirestoreDataSourceImpl(mockFirestore, mockAuth, mockImageStorage)
    }

    private fun setupUserWithTeam(userId: String = "user-123", teamDocId: String = "team-doc-id") {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        every { mockAuth.currentUser } returns mockUser
        every { mockUser.uid } returns userId
        val teamsCollection = mockk<CollectionReference>()
        val teamQuery = mockk<Query>()
        val teamSnapshot = mockk<QuerySnapshot>()
        val teamDoc = mockk<DocumentSnapshot>()
        every { mockFirestore.collection("teams") } returns teamsCollection
        every { teamsCollection.whereEqualTo("assignedCoachId", userId) } returns teamQuery
        every { teamQuery.limit(1) } returns teamQuery
        val teamTask = mockk<Task<QuerySnapshot>>()
        every { teamQuery.get() } returns teamTask
        coEvery { teamTask.await() } returns teamSnapshot
        every { teamSnapshot.documents } returns listOf(teamDoc)
        every { teamDoc.id } returns teamDocId
    }

    private fun setupUserWithNoTeam(userId: String = "user-123") {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        every { mockAuth.currentUser } returns mockUser
        every { mockUser.uid } returns userId
        val teamsCollection = mockk<CollectionReference>()
        val teamQuery = mockk<Query>()
        val teamSnapshot = mockk<QuerySnapshot>()
        every { mockFirestore.collection("teams") } returns teamsCollection
        every { teamsCollection.whereEqualTo("assignedCoachId", userId) } returns teamQuery
        every { teamQuery.limit(1) } returns teamQuery
        val teamTask = mockk<Task<QuerySnapshot>>()
        every { teamQuery.get() } returns teamTask
        coEvery { teamTask.await() } returns teamSnapshot
        every { teamSnapshot.documents } returns emptyList()
    }

    @Test
    fun `getAllPlayersDirect_returnsEmptyList`() = runTest {
        val result = dataSource.getAllPlayersDirect()
        assertEquals(emptyList<Player>(), result)
    }

    @Test
    fun `clearLocalData_isNoOp`() = runTest {
        dataSource.clearLocalData()
    }

    @Test
    fun `givenNoAuthenticatedUser_whenGetAllPlayers_thenEmitsEmptyList`() = runTest {
        every { mockAuth.currentUser } returns null

        dataSource.getAllPlayers().test {
            val result = awaitItem()
            assertEquals(emptyList<Player>(), result)
            cancel()
        }
    }

    @Test
    fun `givenNoTeam_whenGetAllPlayers_thenEmitsEmptyList`() = runTest {
        setupUserWithNoTeam()

        dataSource.getAllPlayers().test {
            val result = awaitItem()
            assertEquals(emptyList<Player>(), result)
            cancel()
        }
    }

    @Test
    fun `givenNoTeam_whenGetPlayerById_thenReturnsNull`() = runTest {
        setupUserWithNoTeam()

        val result = dataSource.getPlayerById("1")

        assertNull(result)
    }

    @Test
    fun `givenNoTeam_whenGetCaptainPlayer_thenReturnsNull`() = runTest {
        setupUserWithNoTeam()

        val result = dataSource.getCaptainPlayer()

        assertNull(result)
    }

    @Test
    fun `givenNoTeam_whenSetPlayerAsCaptain_thenThrowsIllegalStateException`() = runTest {
        setupUserWithNoTeam()

        try {
            dataSource.setPlayerAsCaptain("1")
            fail("Expected IllegalStateException")
        } catch (e: IllegalStateException) {
            // expected
        }
    }

    @Test
    fun `givenNoTeam_whenRemovePlayerAsCaptain_thenDoesNotThrow`() = runTest {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        // Production now directly calls document(playerId).update("isCaptain", false) - no team lookup.
        val playersCollection = mockk<CollectionReference>()
        val playerDocRef = mockk<DocumentReference>()

        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.document("1") } returns playerDocRef
        val updateTask = mockk<Task<Void>>()
        every { playerDocRef.update("isCaptain", false) } returns updateTask
        coEvery { updateTask.await() } returns mockk()

        dataSource.removePlayerAsCaptain("1")
    }

    @Test
    fun `givenNoTeam_whenInsertPlayer_thenThrowsIllegalStateException`() = runTest {
        setupUserWithNoTeam()

        val player = mockk<Player>(relaxed = true)

        try {
            dataSource.insertPlayer(player)
            fail("Expected IllegalStateException")
        } catch (e: IllegalStateException) {
            // expected
        }
    }

    @Test
    fun `givenNoTeam_whenDeletePlayer_thenDoesNotThrow`() = runTest {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        // Production now directly calls document(playerId).update("deleted", true) - no team lookup.
        val playersCollection = mockk<CollectionReference>()
        val playerDocRef = mockk<DocumentReference>()

        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.document("1") } returns playerDocRef
        val updateTask = mockk<Task<Void>>()
        every { playerDocRef.update("deleted", true) } returns updateTask
        coEvery { updateTask.await() } returns mockk()

        dataSource.deletePlayer("1")
    }

    @Test
    fun `givenNoTeam_whenUpdatePlayer_thenThrowsIllegalStateException`() = runTest {
        setupUserWithNoTeam()

        val player = mockk<Player>(relaxed = true)

        try {
            dataSource.updatePlayer(player)
            fail("Expected IllegalStateException")
        } catch (e: IllegalStateException) {
            // expected
        }
    }

    @Test
    fun `givenValidPlayer_whenInsertPlayer_thenReturnsStableId`() = runTest {
        setupUserWithTeam()

        val playersCollection = mockk<CollectionReference>()
        val playerDocRef = mockk<DocumentReference>()
        every { playerDocRef.id } returns "player-doc-id"
        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.document() } returns playerDocRef

        val voidTask = mockk<Task<Void>>()
        every { playerDocRef.set(any()) } returns voidTask
        coEvery { voidTask.await() } returns mockk()

        val player = mockk<Player>(relaxed = true)
        every { player.imageUri } returns null

        val result = dataSource.insertPlayer(player)

        assertTrue(result.isNotEmpty())
    }

    @Test
    fun `givenPlayerNotFound_whenDeletePlayer_thenDoesNotThrow`() = runTest {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        // Production now directly calls document(playerId).update("deleted", true) - no query needed.
        val playersCollection = mockk<CollectionReference>()
        val playerDocRef = mockk<DocumentReference>()

        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.document("99999") } returns playerDocRef
        val updateTask = mockk<Task<Void>>()
        every { playerDocRef.update("deleted", true) } returns updateTask
        coEvery { updateTask.await() } returns mockk()

        // Should not throw when player not found (Firestore just updates or fails silently)
        dataSource.deletePlayer("99999")
    }

    @Test
    fun `givenFirebaseStorageUrl_whenUploadPlayerImageIfNeeded_thenReturnsOriginalUrl`() = runTest {
        setupUserWithTeam()

        val playersCollection = mockk<CollectionReference>()
        val playerDocRef = mockk<DocumentReference>()
        every { playerDocRef.id } returns "player-doc-id"
        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.document() } returns playerDocRef

        val voidTask = mockk<Task<Void>>()
        every { playerDocRef.set(any()) } returns voidTask
        coEvery { voidTask.await() } returns mockk()

        val firebaseUrl = "https://firebasestorage.googleapis.com/v0/b/bucket/o/image.jpg"
        val player = mockk<Player>(relaxed = true)
        every { player.imageUri } returns firebaseUrl

        val result = dataSource.insertPlayer(player)

        // Should succeed without calling imageStorageDataSource.uploadImage
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun `givenNullImageUri_whenUploadPlayerImageIfNeeded_thenReturnsNull`() = runTest {
        setupUserWithTeam()

        val playersCollection = mockk<CollectionReference>()
        val playerDocRef = mockk<DocumentReference>()
        every { playerDocRef.id } returns "player-doc-id"
        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.document() } returns playerDocRef

        val voidTask = mockk<Task<Void>>()
        every { playerDocRef.set(any()) } returns voidTask
        coEvery { voidTask.await() } returns mockk()

        val player = mockk<Player>(relaxed = true)
        every { player.imageUri } returns null

        val result = dataSource.insertPlayer(player)

        assertTrue(result.isNotEmpty())
    }

    @Test
    fun `givenLocalImageUri_whenUploadPlayerImageIfNeeded_thenUploadsImage`() = runTest {
        setupUserWithTeam()

        val playersCollection = mockk<CollectionReference>()
        val playerDocRef = mockk<DocumentReference>()
        every { playerDocRef.id } returns "player-doc-id"
        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.document() } returns playerDocRef

        val voidTask = mockk<Task<Void>>()
        every { playerDocRef.set(any()) } returns voidTask
        coEvery { voidTask.await() } returns mockk()

        coEvery { mockImageStorage.uploadImage(any(), any()) } returns "https://firebasestorage.googleapis.com/uploaded.jpg"

        val player = mockk<Player>(relaxed = true)
        every { player.imageUri } returns "content://media/image.jpg"

        val result = dataSource.insertPlayer(player)

        assertTrue(result.isNotEmpty())
    }

    @Test
    fun `givenExistingPlayer_whenGetPlayerById_thenReturnsPlayer`() = runTest {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")

        val playersCollection = mockk<CollectionReference>()
        val playerDocRef = mockk<DocumentReference>()
        val playerDoc = mockk<DocumentSnapshot>()

        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.document("player-doc-id") } returns playerDocRef
        val playerTask = mockk<Task<DocumentSnapshot>>()
        every { playerDocRef.get() } returns playerTask
        coEvery { playerTask.await() } returns playerDoc

        val playerModel = PlayerFirestoreModel(
            id = "player-doc-id",
            teamId = "team-doc-id",
            firstName = "John",
            lastName = "Doe",
            deleted = false
        )
        every { playerDoc.id } returns "player-doc-id"
        every { playerDoc.exists() } returns true
        every { playerDoc.toObject(PlayerFirestoreModel::class.java) } returns playerModel

        val result = dataSource.getPlayerById("player-doc-id")

        assertNotNull(result)
    }

    @Test
    fun `givenNoCaptain_whenGetCaptainPlayer_thenReturnsNull`() = runTest {
        setupUserWithTeam()

        val playersCollection = mockk<CollectionReference>()
        val playerQuery = mockk<Query>(relaxed = true)
        val playerSnapshot = mockk<QuerySnapshot>()

        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.whereEqualTo("teamId", "team-doc-id") } returns playerQuery
        every { playerQuery.whereEqualTo(any<String>(), any()) } returns playerQuery
        every { playerQuery.limit(any()) } returns playerQuery
        val playerTask = mockk<Task<QuerySnapshot>>()
        every { playerQuery.get() } returns playerTask
        coEvery { playerTask.await() } returns playerSnapshot
        every { playerSnapshot.documents } returns emptyList()

        val result = dataSource.getCaptainPlayer()

        assertNull(result)
    }

    @Test
    fun `givenCaptainPlayer_whenGetCaptainPlayer_thenReturnsCaptain`() = runTest {
        setupUserWithTeam()

        val playersCollection = mockk<CollectionReference>()
        val playerQuery = mockk<Query>(relaxed = true)
        val playerSnapshot = mockk<QuerySnapshot>()
        val playerDoc = mockk<DocumentSnapshot>()

        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.whereEqualTo("teamId", "team-doc-id") } returns playerQuery
        every { playerQuery.whereEqualTo(any<String>(), any()) } returns playerQuery
        every { playerQuery.limit(any()) } returns playerQuery
        val playerTask = mockk<Task<QuerySnapshot>>()
        every { playerQuery.get() } returns playerTask
        coEvery { playerTask.await() } returns playerSnapshot

        val playerModel = PlayerFirestoreModel(
            id = "player-doc-id",
            teamId = "team-doc-id",
            firstName = "Captain",
            lastName = "America",
            isCaptain = true,
            deleted = false
        )
        every { playerDoc.id } returns "player-doc-id"
        every { playerDoc.toObject(PlayerFirestoreModel::class.java) } returns playerModel
        every { playerSnapshot.documents } returns listOf(playerDoc)

        val result = dataSource.getCaptainPlayer()

        assertNotNull(result)
    }

    @Test
    fun `givenPlayerNotFound_whenSetPlayerAsCaptain_thenDoesNotThrow`() = runTest {
        setupUserWithTeam()

        val playersCollection = mockk<CollectionReference>()
        every { mockFirestore.collection("players") } returns playersCollection

        // Setup for clearAllCaptains: whereEqualTo("teamId") -> whereEqualTo("isCaptain", true) -> get()
        val allTeamQuery = mockk<Query>()
        val captainsQuery = mockk<Query>()
        val captainsSnapshot = mockk<QuerySnapshot>()
        every { playersCollection.whereEqualTo("teamId", "team-doc-id") } returns allTeamQuery
        every { allTeamQuery.whereEqualTo("isCaptain", true) } returns captainsQuery
        val captainsTask = mockk<Task<QuerySnapshot>>()
        every { captainsQuery.get() } returns captainsTask
        coEvery { captainsTask.await() } returns captainsSnapshot
        every { captainsSnapshot.documents } returns emptyList()

        // Setup for document(playerId).update("isCaptain", true)
        val playerDocRef = mockk<DocumentReference>()
        every { playersCollection.document("99999") } returns playerDocRef
        val updateTask = mockk<Task<Void>>()
        every { playerDocRef.update("isCaptain", true) } returns updateTask
        coEvery { updateTask.await() } returns mockk()

        dataSource.setPlayerAsCaptain("99999")
    }

    @Test
    fun `givenValidPlayer_whenDeletePlayer_thenSetsDeletedFlag`() = runTest {
        setupUserWithTeam()

        val playersCollection = mockk<CollectionReference>()
        val playerQuery = mockk<Query>()
        val playerSnapshot = mockk<QuerySnapshot>()
        val playerDoc = mockk<DocumentSnapshot>()
        val playerDocRef = mockk<DocumentReference>()

        val playerModel = PlayerFirestoreModel(
            id = "player-doc-id",
            teamId = "team-doc-id",
            firstName = "Player",
            lastName = "One",
            deleted = false
        )

        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.whereEqualTo("teamId", "team-doc-id") } returns playerQuery
        val playerTask = mockk<Task<QuerySnapshot>>()
        every { playerQuery.get() } returns playerTask
        coEvery { playerTask.await() } returns playerSnapshot
        every { playerSnapshot.documents } returns listOf(playerDoc)
        every { playerDoc.id } returns "player-doc-id"
        every { playerDoc.toObject(PlayerFirestoreModel::class.java) } returns playerModel

        every { playersCollection.document("player-doc-id") } returns playerDocRef
        val updateTask = mockk<Task<Void>>()
        every { playerDocRef.update("deleted", true) } returns updateTask
        coEvery { updateTask.await() } returns mockk()

        dataSource.deletePlayer("player-doc-id")
    }

    @Test
    fun `givenPlayerNotFound_whenRemovePlayerAsCaptain_thenDoesNotThrow`() = runTest {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        // Production now directly calls document(playerId).update("isCaptain", false) - no query needed.
        val playersCollection = mockk<CollectionReference>()
        val playerDocRef = mockk<DocumentReference>()

        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.document("99999") } returns playerDocRef
        val updateTask = mockk<Task<Void>>()
        every { playerDocRef.update("isCaptain", false) } returns updateTask
        coEvery { updateTask.await() } returns mockk()

        dataSource.removePlayerAsCaptain("99999")
    }

    @Test
    fun `givenValidPlayerWithSameImage_whenUpdatePlayer_thenSucceeds`() = runTest {
        setupUserWithTeam()

        val existingImageUrl = "https://firebasestorage.googleapis.com/v0/b/bucket/o/image.jpg"

        val playersCollection = mockk<CollectionReference>()
        val playerQuery = mockk<Query>()
        val playerSnapshot = mockk<QuerySnapshot>()
        val playerDoc = mockk<DocumentSnapshot>()
        val playerDocRef = mockk<DocumentReference>()

        val playerModel = PlayerFirestoreModel(
            id = "player-doc-id",
            teamId = "team-doc-id",
            firstName = "Player",
            lastName = "One",
            deleted = false,
            imageUri = existingImageUrl
        )

        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.whereEqualTo("teamId", "team-doc-id") } returns playerQuery
        val playerTask = mockk<Task<QuerySnapshot>>()
        every { playerQuery.get() } returns playerTask
        coEvery { playerTask.await() } returns playerSnapshot
        every { playerSnapshot.documents } returns listOf(playerDoc)
        every { playerDoc.id } returns "player-doc-id"
        every { playerDoc.toObject(PlayerFirestoreModel::class.java) } returns playerModel

        every { playersCollection.document("player-doc-id") } returns playerDocRef
        val setTask = mockk<Task<Void>>()
        every { playerDocRef.set(any()) } returns setTask
        coEvery { setTask.await() } returns mockk()

        val player = mockk<Player>(relaxed = true)
        every { player.id } returns "player-doc-id"
        every { player.imageUri } returns existingImageUrl

        dataSource.updatePlayer(player)
    }

    @Test
    fun `givenValidPlayerWithNullImage_whenUpdatePlayer_thenDeletesOldImageAndSetsNull`() = runTest {
        setupUserWithTeam()

        val existingImageUrl = "https://firebasestorage.googleapis.com/v0/b/bucket/o/image.jpg"

        val playersCollection = mockk<CollectionReference>()
        val playerQuery = mockk<Query>()
        val playerSnapshot = mockk<QuerySnapshot>()
        val playerDoc = mockk<DocumentSnapshot>()
        val playerDocRef = mockk<DocumentReference>()

        val playerModel = PlayerFirestoreModel(
            id = "player-doc-id",
            teamId = "team-doc-id",
            firstName = "Player",
            lastName = "One",
            deleted = false,
            imageUri = existingImageUrl
        )

        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.whereEqualTo("teamId", "team-doc-id") } returns playerQuery
        val playerTask = mockk<Task<QuerySnapshot>>()
        every { playerQuery.get() } returns playerTask
        coEvery { playerTask.await() } returns playerSnapshot
        every { playerSnapshot.documents } returns listOf(playerDoc)
        every { playerDoc.id } returns "player-doc-id"
        every { playerDoc.toObject(PlayerFirestoreModel::class.java) } returns playerModel

        coEvery { mockImageStorage.deleteImage(existingImageUrl) } returns true

        every { playersCollection.document("player-doc-id") } returns playerDocRef
        val setTask = mockk<Task<Void>>()
        every { playerDocRef.set(any()) } returns setTask
        coEvery { setTask.await() } returns mockk()

        val player = mockk<Player>(relaxed = true)
        every { player.id } returns "player-doc-id"
        every { player.imageUri } returns null

        dataSource.updatePlayer(player)
    }

    @Test
    fun `givenValidPlayerWithNewLocalImage_whenUpdatePlayer_thenUploadsNewImageAndDeletesOld`() = runTest {
        setupUserWithTeam()

        val existingImageUrl = "https://firebasestorage.googleapis.com/v0/b/bucket/o/old_image.jpg"
        val newLocalUri = "content://media/new_image.jpg"
        val uploadedUrl = "https://firebasestorage.googleapis.com/v0/b/bucket/o/new_image.jpg"

        val playersCollection = mockk<CollectionReference>()
        val playerQuery = mockk<Query>()
        val playerSnapshot = mockk<QuerySnapshot>()
        val playerDoc = mockk<DocumentSnapshot>()
        val playerDocRef = mockk<DocumentReference>()

        val playerModel = PlayerFirestoreModel(
            id = "player-doc-id",
            teamId = "team-doc-id",
            firstName = "Player",
            lastName = "One",
            deleted = false,
            imageUri = existingImageUrl
        )

        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.whereEqualTo("teamId", "team-doc-id") } returns playerQuery
        val playerTask = mockk<Task<QuerySnapshot>>()
        every { playerQuery.get() } returns playerTask
        coEvery { playerTask.await() } returns playerSnapshot
        every { playerSnapshot.documents } returns listOf(playerDoc)
        every { playerDoc.id } returns "player-doc-id"
        every { playerDoc.toObject(PlayerFirestoreModel::class.java) } returns playerModel

        coEvery { mockImageStorage.deleteImage(existingImageUrl) } returns true
        coEvery { mockImageStorage.uploadImage(newLocalUri, any()) } returns uploadedUrl

        every { playersCollection.document("player-doc-id") } returns playerDocRef
        val setTask = mockk<Task<Void>>()
        every { playerDocRef.set(any()) } returns setTask
        coEvery { setTask.await() } returns mockk()

        val player = mockk<Player>(relaxed = true)
        every { player.id } returns "player-doc-id"
        every { player.imageUri } returns newLocalUri

        dataSource.updatePlayer(player)
    }

    @Test
    fun `givenPlayerNotFound_whenUpdatePlayer_thenUpserts`() = runTest {
        // Production now calls getPlayerById (document().get()) and if not found, proceeds to upsert.
        setupUserWithTeam()

        val playersCollection = mockk<CollectionReference>()
        val playerDocRef = mockk<DocumentReference>()
        val playerDoc = mockk<DocumentSnapshot>()

        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.document("99999") } returns playerDocRef
        val getTask = mockk<Task<DocumentSnapshot>>()
        every { playerDocRef.get() } returns getTask
        coEvery { getTask.await() } returns playerDoc
        every { playerDoc.exists() } returns false

        val setTask = mockk<Task<Void>>()
        every { playerDocRef.set(any()) } returns setTask
        coEvery { setTask.await() } returns mockk()

        val player = mockk<Player>(relaxed = true)
        every { player.id } returns "99999"
        every { player.imageUri } returns null

        // Should not throw - player is upserted
        dataSource.updatePlayer(player)
    }

    @Test
    fun `givenValidPlayerWithFirebaseUrl_whenUpdatePlayer_thenUsesExistingUrl`() = runTest {
        setupUserWithTeam()

        val otherFirebaseUrl = "https://storage.googleapis.com/bucket/other.jpg"

        val playersCollection = mockk<CollectionReference>()
        val playerQuery = mockk<Query>()
        val playerSnapshot = mockk<QuerySnapshot>()
        val playerDoc = mockk<DocumentSnapshot>()
        val playerDocRef = mockk<DocumentReference>()

        // Current player has no image
        val playerModel = PlayerFirestoreModel(
            id = "player-doc-id",
            teamId = "team-doc-id",
            firstName = "Player",
            lastName = "One",
            deleted = false,
            imageUri = null
        )

        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.whereEqualTo("teamId", "team-doc-id") } returns playerQuery
        val playerTask = mockk<Task<QuerySnapshot>>()
        every { playerQuery.get() } returns playerTask
        coEvery { playerTask.await() } returns playerSnapshot
        every { playerSnapshot.documents } returns listOf(playerDoc)
        every { playerDoc.id } returns "player-doc-id"
        every { playerDoc.toObject(PlayerFirestoreModel::class.java) } returns playerModel

        every { playersCollection.document("player-doc-id") } returns playerDocRef
        val setTask = mockk<Task<Void>>()
        every { playerDocRef.set(any()) } returns setTask
        coEvery { setTask.await() } returns mockk()

        val player = mockk<Player>(relaxed = true)
        every { player.id } returns "player-doc-id"
        // Player has a firebase storage URL that is different from current (else branch)
        every { player.imageUri } returns otherFirebaseUrl

        dataSource.updatePlayer(player)
    }

    @Test
    fun `givenAuthenticatedUserWithTeam_whenGetAllPlayers_thenEmitsPlayers`() = runTest {
        setupUserWithTeam()

        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        val playersCollection = mockk<CollectionReference>()
        val playersQuery = mockk<Query>()
        val querySnapshot = mockk<QuerySnapshot>()
        val docSnapshot = mockk<DocumentSnapshot>()

        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.whereEqualTo("teamId", "team-doc-id") } returns playersQuery
        every { playersQuery.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        val playerModel = PlayerFirestoreModel(
            id = "player-doc-id",
            teamId = "team-doc-id",
            firstName = "John",
            lastName = "Doe",
            deleted = false
        )
        every { docSnapshot.id } returns "player-doc-id"
        every { docSnapshot.toObject(PlayerFirestoreModel::class.java) } returns playerModel
        every { querySnapshot.documents } returns listOf(docSnapshot)

        dataSource.getAllPlayers().test {
            listenerSlot.captured.onEvent(querySnapshot, null)
            val result = awaitItem()
            assertEquals(1, result.size)
            cancel()
        }
    }

    @Test
    fun `givenFirestoreError_whenGetAllPlayers_thenEmitsEmptyList`() = runTest {
        setupUserWithTeam()

        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        val playersCollection = mockk<CollectionReference>()
        val playersQuery = mockk<Query>()

        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.whereEqualTo("teamId", "team-doc-id") } returns playersQuery
        every { playersQuery.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        val mockError = mockk<com.google.firebase.firestore.FirebaseFirestoreException>(relaxed = true)

        dataSource.getAllPlayers().test {
            listenerSlot.captured.onEvent(null, mockError)
            val result = awaitItem()
            assertEquals(emptyList<Player>(), result)
            cancel()
        }
    }

    @Test
    fun `givenDeletedCaptainPlayer_whenGetCaptainPlayer_thenReturnsNull`() = runTest {
        setupUserWithTeam()

        val playersCollection = mockk<CollectionReference>()
        val playerQuery = mockk<Query>(relaxed = true)
        val playerSnapshot = mockk<QuerySnapshot>()
        val playerDoc = mockk<DocumentSnapshot>()

        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.whereEqualTo("teamId", "team-doc-id") } returns playerQuery
        every { playerQuery.whereEqualTo(any<String>(), any()) } returns playerQuery
        every { playerQuery.limit(any()) } returns playerQuery
        val playerTask = mockk<Task<QuerySnapshot>>()
        every { playerQuery.get() } returns playerTask
        coEvery { playerTask.await() } returns playerSnapshot

        // Captain is deleted
        val playerModel = PlayerFirestoreModel(
            id = "player-doc-id",
            teamId = "team-doc-id",
            firstName = "Captain",
            lastName = "Deleted",
            isCaptain = true,
            deleted = true
        )
        every { playerDoc.id } returns "player-doc-id"
        every { playerDoc.toObject(PlayerFirestoreModel::class.java) } returns playerModel
        every { playerSnapshot.documents } returns listOf(playerDoc)

        val result = dataSource.getCaptainPlayer()

        assertNull(result)
    }

    @Test
    fun `givenValidPlayer_whenSetPlayerAsCaptain_thenUpdatesFirestore`() = runTest {
        setupUserWithTeam()

        val playersCollection = mockk<CollectionReference>()
        val playerDocRef = mockk<DocumentReference>()

        every { mockFirestore.collection("players") } returns playersCollection

        // Setup for clearAllCaptains (whereEqualTo("teamId") + whereEqualTo("isCaptain",true))
        val captainsQuery = mockk<Query>()
        val captainsSnapshot = mockk<QuerySnapshot>()
        // First call: whereEqualTo("teamId") for clearAllCaptains
        val allTeamQuery = mockk<Query>()
        every { playersCollection.whereEqualTo("teamId", "team-doc-id") } returns allTeamQuery
        every { allTeamQuery.whereEqualTo("isCaptain", true) } returns captainsQuery
        val captainsTask = mockk<Task<QuerySnapshot>>()
        every { captainsQuery.get() } returns captainsTask
        coEvery { captainsTask.await() } returns captainsSnapshot
        every { captainsSnapshot.documents } returns emptyList()

        // Setup for findDocumentIdByPlayerId
        val findQuery = mockk<Query>()
        val findSnapshot = mockk<QuerySnapshot>()
        val findDoc = mockk<DocumentSnapshot>()

        every { allTeamQuery.get() } returns mockk<Task<QuerySnapshot>>().also {
            coEvery { it.await() } returns findSnapshot
        }

        val playerModel = PlayerFirestoreModel(
            id = "player-doc-id",
            teamId = "team-doc-id",
            firstName = "Player",
            lastName = "One",
            deleted = false
        )
        every { findDoc.id } returns "player-doc-id"
        every { findDoc.toObject(PlayerFirestoreModel::class.java) } returns playerModel
        every { findSnapshot.documents } returns listOf(findDoc)

        // Setup for update("isCaptain", true)
        every { playersCollection.document("player-doc-id") } returns playerDocRef
        val updateTask = mockk<Task<Void>>()
        every { playerDocRef.update("isCaptain", true) } returns updateTask
        coEvery { updateTask.await() } returns mockk()

        dataSource.setPlayerAsCaptain("player-doc-id")
    }

    @Test
    fun `givenValidPlayer_whenRemovePlayerAsCaptain_thenUpdatesFirestore`() = runTest {
        setupUserWithTeam()

        val playersCollection = mockk<CollectionReference>()
        val playerQuery = mockk<Query>()
        val playerSnapshot = mockk<QuerySnapshot>()
        val playerDoc = mockk<DocumentSnapshot>()
        val playerDocRef = mockk<DocumentReference>()

        val playerModel = PlayerFirestoreModel(
            id = "player-doc-id",
            teamId = "team-doc-id",
            firstName = "Player",
            lastName = "One",
            deleted = false
        )

        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.whereEqualTo("teamId", "team-doc-id") } returns playerQuery
        val playerTask = mockk<Task<QuerySnapshot>>()
        every { playerQuery.get() } returns playerTask
        coEvery { playerTask.await() } returns playerSnapshot
        every { playerSnapshot.documents } returns listOf(playerDoc)
        every { playerDoc.id } returns "player-doc-id"
        every { playerDoc.toObject(PlayerFirestoreModel::class.java) } returns playerModel

        every { playersCollection.document("player-doc-id") } returns playerDocRef
        val updateTask = mockk<Task<Void>>()
        every { playerDocRef.update("isCaptain", false) } returns updateTask
        coEvery { updateTask.await() } returns mockk()

        dataSource.removePlayerAsCaptain("player-doc-id")
    }

    @Test
    fun `givenExceptionInGetPlayerById_whenGetPlayerById_thenReturnsNull`() = runTest {
        setupUserWithTeam()

        val playersCollection = mockk<CollectionReference>()
        val playerQuery = mockk<Query>()

        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.whereEqualTo("teamId", "team-doc-id") } returns playerQuery
        val playerTask = mockk<Task<QuerySnapshot>>()
        every { playerQuery.get() } returns playerTask
        coEvery { playerTask.await() } throws RuntimeException("Firestore error")

        val result = dataSource.getPlayerById("exception-player-id")

        assertNull(result)
    }

    @Test
    fun `givenPlayerWithEmptyId_whenGetAllPlayers_thenSetsIdFromDocumentId`() = runTest {
        setupUserWithTeam()

        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        val playersCollection = mockk<CollectionReference>()
        val playersQuery = mockk<Query>()
        val querySnapshot = mockk<QuerySnapshot>()
        val docSnapshot = mockk<DocumentSnapshot>()

        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.whereEqualTo("teamId", "team-doc-id") } returns playersQuery
        every { playersQuery.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        // Model has empty id - should be set from document id
        val playerModel = PlayerFirestoreModel(
            id = "",
            teamId = "team-doc-id",
            firstName = "John",
            lastName = "Doe",
            deleted = false
        )
        every { docSnapshot.id } returns "player-doc-id"
        every { docSnapshot.toObject(PlayerFirestoreModel::class.java) } returns playerModel
        every { querySnapshot.documents } returns listOf(docSnapshot)

        dataSource.getAllPlayers().test {
            listenerSlot.captured.onEvent(querySnapshot, null)
            val result = awaitItem()
            assertEquals(1, result.size)
            cancel()
        }
    }

    @Test
    fun `givenLocalFileUri_whenInsertPlayer_thenUploadsImageAndReturnsStableId`() = runTest {
        setupUserWithTeam()

        val playersCollection = mockk<CollectionReference>()
        val playerDocRef = mockk<DocumentReference>()
        every { playerDocRef.id } returns "player-doc-id"
        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.document() } returns playerDocRef

        val voidTask = mockk<Task<Void>>()
        every { playerDocRef.set(any()) } returns voidTask
        coEvery { voidTask.await() } returns mockk()

        val uploadedUrl = "https://firebasestorage.googleapis.com/uploaded.jpg"
        coEvery { mockImageStorage.uploadImage("file:///storage/image.jpg", any()) } returns uploadedUrl

        val player = mockk<Player>(relaxed = true)
        every { player.imageUri } returns "file:///storage/image.jpg"

        val result = dataSource.insertPlayer(player)
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun `givenExistingCaptain_whenSetPlayerAsCaptain_thenClearsOldCaptainAndSetsNew`() = runTest {
        setupUserWithTeam()

        val playersCollection = mockk<CollectionReference>()
        val allTeamQuery = mockk<Query>()
        val captainsQuery = mockk<Query>()
        val captainsSnapshot = mockk<QuerySnapshot>()
        val captainDoc = mockk<DocumentSnapshot>()
        val captainDocRef = mockk<DocumentReference>()
        val findSnapshot = mockk<QuerySnapshot>()
        val findDoc = mockk<DocumentSnapshot>()
        val newPlayerDocRef = mockk<DocumentReference>()

        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.whereEqualTo("teamId", "team-doc-id") } returns allTeamQuery
        every { allTeamQuery.whereEqualTo("isCaptain", true) } returns captainsQuery

        val captainsTask = mockk<Task<QuerySnapshot>>()
        every { captainsQuery.get() } returns captainsTask
        coEvery { captainsTask.await() } returns captainsSnapshot

        val existingCaptainModel = PlayerFirestoreModel(
            id = "old-captain-id",
            teamId = "team-doc-id",
            firstName = "Old",
            lastName = "Captain",
            isCaptain = true,
            deleted = false
        )
        every { captainDoc.id } returns "old-captain-id"
        every { captainDoc.toObject(PlayerFirestoreModel::class.java) } returns existingCaptainModel
        every { captainsSnapshot.documents } returns listOf(captainDoc)

        every { playersCollection.document("old-captain-id") } returns captainDocRef
        val clearTask = mockk<Task<Void>>()
        every { captainDocRef.update("isCaptain", false) } returns clearTask
        coEvery { clearTask.await() } returns mockk()

        val findTask = mockk<Task<QuerySnapshot>>()
        every { allTeamQuery.get() } returns findTask
        coEvery { findTask.await() } returns findSnapshot

        val newPlayerModel = PlayerFirestoreModel(
            id = "new-player-id",
            teamId = "team-doc-id",
            firstName = "New",
            lastName = "Captain",
            deleted = false
        )
        every { findDoc.id } returns "new-player-id"
        every { findDoc.toObject(PlayerFirestoreModel::class.java) } returns newPlayerModel
        every { findSnapshot.documents } returns listOf(findDoc)

        every { playersCollection.document("new-player-id") } returns newPlayerDocRef
        val setTask = mockk<Task<Void>>()
        every { newPlayerDocRef.update("isCaptain", true) } returns setTask
        coEvery { setTask.await() } returns mockk()

        dataSource.setPlayerAsCaptain("new-player-id")
    }

    @Test
    fun `givenDeletedCaptain_whenSetPlayerAsCaptain_thenSkipsDeletedCaptainUpdate`() = runTest {
        setupUserWithTeam()

        val playersCollection = mockk<CollectionReference>()
        val allTeamQuery = mockk<Query>()
        val captainsQuery = mockk<Query>()
        val captainsSnapshot = mockk<QuerySnapshot>()
        val captainDoc = mockk<DocumentSnapshot>()
        val findSnapshot = mockk<QuerySnapshot>()
        val findDoc = mockk<DocumentSnapshot>()
        val playerDocRef = mockk<DocumentReference>()

        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.whereEqualTo("teamId", "team-doc-id") } returns allTeamQuery
        every { allTeamQuery.whereEqualTo("isCaptain", true) } returns captainsQuery

        val captainsTask = mockk<Task<QuerySnapshot>>()
        every { captainsQuery.get() } returns captainsTask
        coEvery { captainsTask.await() } returns captainsSnapshot

        val deletedCaptainModel = PlayerFirestoreModel(
            id = "old-captain-id",
            teamId = "team-doc-id",
            firstName = "Deleted",
            lastName = "Captain",
            isCaptain = true,
            deleted = true
        )
        every { captainDoc.id } returns "old-captain-id"
        every { captainDoc.toObject(PlayerFirestoreModel::class.java) } returns deletedCaptainModel
        every { captainsSnapshot.documents } returns listOf(captainDoc)

        val findTask = mockk<Task<QuerySnapshot>>()
        every { allTeamQuery.get() } returns findTask
        coEvery { findTask.await() } returns findSnapshot

        val newPlayerModel = PlayerFirestoreModel(
            id = "player-doc-id",
            teamId = "team-doc-id",
            firstName = "New",
            lastName = "Captain",
            deleted = false
        )
        every { findDoc.id } returns "player-doc-id"
        every { findDoc.toObject(PlayerFirestoreModel::class.java) } returns newPlayerModel
        every { findSnapshot.documents } returns listOf(findDoc)

        every { playersCollection.document("player-doc-id") } returns playerDocRef
        val setTask = mockk<Task<Void>>()
        every { playerDocRef.update("isCaptain", true) } returns setTask
        coEvery { setTask.await() } returns mockk()

        dataSource.setPlayerAsCaptain("player-doc-id")
    }

    @Test
    fun `givenPlayerWithEmptyIdModel_whenGetPlayerById_thenSetsIdFromDocumentId`() = runTest {
        // Production uses document(playerId).get() directly; the model.copy(id = document.id) sets the id.
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")

        val playersCollection = mockk<CollectionReference>()
        val playerDocRef = mockk<DocumentReference>()
        val playerDoc = mockk<DocumentSnapshot>()

        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.document("player-doc-id") } returns playerDocRef
        val playerTask = mockk<Task<DocumentSnapshot>>()
        every { playerDocRef.get() } returns playerTask
        coEvery { playerTask.await() } returns playerDoc

        val playerModel = PlayerFirestoreModel(
            id = "",
            teamId = "team-doc-id",
            firstName = "John",
            lastName = "Doe",
            deleted = false
        )
        every { playerDoc.id } returns "player-doc-id"
        every { playerDoc.exists() } returns true
        every { playerDoc.toObject(PlayerFirestoreModel::class.java) } returns playerModel

        val result = dataSource.getPlayerById("player-doc-id")

        assertNotNull(result)
    }

    @Test
    fun `givenPlayerWithEmptyIdModel_whenGetCaptainPlayer_thenSetsIdFromDocumentId`() = runTest {
        setupUserWithTeam()

        val playersCollection = mockk<CollectionReference>()
        val playerQuery = mockk<Query>(relaxed = true)
        val playerSnapshot = mockk<QuerySnapshot>()
        val playerDoc = mockk<DocumentSnapshot>()

        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.whereEqualTo("teamId", "team-doc-id") } returns playerQuery
        every { playerQuery.whereEqualTo(any<String>(), any()) } returns playerQuery
        every { playerQuery.limit(any()) } returns playerQuery
        val playerTask = mockk<Task<QuerySnapshot>>()
        every { playerQuery.get() } returns playerTask
        coEvery { playerTask.await() } returns playerSnapshot

        val playerModel = PlayerFirestoreModel(
            id = "",
            teamId = "team-doc-id",
            firstName = "Captain",
            lastName = "America",
            isCaptain = true,
            deleted = false
        )
        every { playerDoc.id } returns "captain-doc-id"
        every { playerDoc.toObject(PlayerFirestoreModel::class.java) } returns playerModel
        every { playerSnapshot.documents } returns listOf(playerDoc)

        val result = dataSource.getCaptainPlayer()

        assertNotNull(result)
    }

    @Test
    fun `givenPlayerWithEmptyIdModel_whenDeletePlayer_thenSetsIdFromDocumentIdAndDeletes`() = runTest {
        setupUserWithTeam()

        val playersCollection = mockk<CollectionReference>()
        val playerQuery = mockk<Query>()
        val playerSnapshot = mockk<QuerySnapshot>()
        val playerDoc = mockk<DocumentSnapshot>()
        val playerDocRef = mockk<DocumentReference>()

        val playerModel = PlayerFirestoreModel(
            id = "",
            teamId = "team-doc-id",
            firstName = "Player",
            lastName = "One",
            deleted = false
        )

        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.whereEqualTo("teamId", "team-doc-id") } returns playerQuery
        val playerTask = mockk<Task<QuerySnapshot>>()
        every { playerQuery.get() } returns playerTask
        coEvery { playerTask.await() } returns playerSnapshot
        every { playerSnapshot.documents } returns listOf(playerDoc)
        every { playerDoc.id } returns "player-doc-id"
        every { playerDoc.toObject(PlayerFirestoreModel::class.java) } returns playerModel

        every { playersCollection.document("player-doc-id") } returns playerDocRef
        val updateTask = mockk<Task<Void>>()
        every { playerDocRef.update("deleted", true) } returns updateTask
        coEvery { updateTask.await() } returns mockk()

        dataSource.deletePlayer("player-doc-id")
    }

    @Test
    fun `givenPlayerWithNullImageAndNoExistingImage_whenUpdatePlayer_thenSucceeds`() = runTest {
        setupUserWithTeam()

        val playersCollection = mockk<CollectionReference>()
        val playerQuery = mockk<Query>()
        val playerSnapshot = mockk<QuerySnapshot>()
        val playerDoc = mockk<DocumentSnapshot>()
        val playerDocRef = mockk<DocumentReference>()

        val playerModel = PlayerFirestoreModel(
            id = "player-doc-id",
            teamId = "team-doc-id",
            firstName = "Player",
            lastName = "One",
            deleted = false,
            imageUri = null
        )

        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.whereEqualTo("teamId", "team-doc-id") } returns playerQuery
        val playerTask = mockk<Task<QuerySnapshot>>()
        every { playerQuery.get() } returns playerTask
        coEvery { playerTask.await() } returns playerSnapshot
        every { playerSnapshot.documents } returns listOf(playerDoc)
        every { playerDoc.id } returns "player-doc-id"
        every { playerDoc.toObject(PlayerFirestoreModel::class.java) } returns playerModel

        every { playersCollection.document("player-doc-id") } returns playerDocRef
        val setTask = mockk<Task<Void>>()
        every { playerDocRef.set(any()) } returns setTask
        coEvery { setTask.await() } returns mockk()

        val player = mockk<Player>(relaxed = true)
        every { player.id } returns "player-doc-id"
        every { player.imageUri } returns null

        dataSource.updatePlayer(player)
    }



    @Test
    fun `givenNullDocumentModel_whenGetPlayerById_thenReturnsNull`() = runTest {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")

        val playersCollection = mockk<CollectionReference>()
        val playerDocRef = mockk<DocumentReference>()
        val playerDoc = mockk<DocumentSnapshot>()

        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.document("player-doc-id") } returns playerDocRef
        val playerTask = mockk<Task<DocumentSnapshot>>()
        every { playerDocRef.get() } returns playerTask
        coEvery { playerTask.await() } returns playerDoc

        every { playerDoc.exists() } returns true
        every { playerDoc.toObject(PlayerFirestoreModel::class.java) } returns null

        val result = dataSource.getPlayerById("player-doc-id")

        assertNull(result)
    }

    @Test
    fun `givenPlayerNotExist_whenGetPlayerById_thenReturnsNull`() = runTest {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")

        val playersCollection = mockk<CollectionReference>()
        val playerDocRef = mockk<DocumentReference>()
        val playerDoc = mockk<DocumentSnapshot>()

        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.document("player-doc-id") } returns playerDocRef
        val playerTask = mockk<Task<DocumentSnapshot>>()
        every { playerDocRef.get() } returns playerTask
        coEvery { playerTask.await() } returns playerDoc

        every { playerDoc.exists() } returns false

        val result = dataSource.getPlayerById("player-doc-id")

        assertNull(result)
    }

    @Test
    fun `givenDeletedPlayer_whenGetPlayerById_thenReturnsNull`() = runTest {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")

        val playersCollection = mockk<CollectionReference>()
        val playerDocRef = mockk<DocumentReference>()
        val playerDoc = mockk<DocumentSnapshot>()

        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.document("player-doc-id") } returns playerDocRef
        val playerTask = mockk<Task<DocumentSnapshot>>()
        every { playerDocRef.get() } returns playerTask
        coEvery { playerTask.await() } returns playerDoc

        val playerModel = PlayerFirestoreModel(
            id = "player-doc-id",
            teamId = "team-doc-id",
            firstName = "John",
            lastName = "Doe",
            deleted = true
        )
        every { playerDoc.exists() } returns true
        every { playerDoc.toObject(PlayerFirestoreModel::class.java) } returns playerModel

        val result = dataSource.getPlayerById("player-doc-id")

        assertNull(result)
    }

    @Test
    fun `givenAuthenticatedUserWithNoTeam_whenGetAllPlayers_thenReturnsEmptyThenClosesFlow`() =
        runTest {
            val listenerSlot = slot<EventListener<QuerySnapshot>>()
            every { mockAuth.currentUser } returns mockUser
            every { mockUser.uid } returns "user-123"
            val teamsCollection = mockk<CollectionReference>()
            val teamQuery = mockk<Query>()
            val teamSnapshot = mockk<QuerySnapshot>()

            every { mockFirestore.collection("teams") } returns teamsCollection
            every { teamsCollection.whereEqualTo("assignedCoachId", "user-123") } returns teamQuery
            every { teamQuery.limit(1) } returns teamQuery
            val teamTask = mockk<Task<QuerySnapshot>>()
            every { teamQuery.get() } returns teamTask
            mockkStatic("kotlinx.coroutines.tasks.TasksKt")
            coEvery { teamTask.await() } returns teamSnapshot
            every { teamSnapshot.documents } returns emptyList()

            dataSource.getAllPlayers().test {
                val result = awaitItem()
                assertEquals(emptyList<Player>(), result)
                cancel()
            }
        }

    @Test
    fun `givenValidPlayer_whenGetPlayersByTeam_thenEmitsPlayers`() = runTest {
        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        val playersCollection = mockk<CollectionReference>()
        val playersQuery = mockk<Query>()
        val querySnapshot = mockk<QuerySnapshot>()
        val docSnapshot = mockk<DocumentSnapshot>()

        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.whereEqualTo("teamId", "team-doc-id") } returns playersQuery
        every { playersQuery.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        val playerModel = PlayerFirestoreModel(
            id = "player-doc-id",
            teamId = "team-doc-id",
            firstName = "John",
            lastName = "Doe",
            deleted = false
        )
        every { docSnapshot.id } returns "player-doc-id"
        every { docSnapshot.toObject(PlayerFirestoreModel::class.java) } returns playerModel
        every { querySnapshot.documents } returns listOf(docSnapshot)

        dataSource.getPlayersByTeam("team-doc-id").test {
            listenerSlot.captured.onEvent(querySnapshot, null)
            val result = awaitItem()
            assertEquals(1, result.size)
            cancel()
        }
    }

    @Test
    fun `givenFirestoreErrorInGetPlayersByTeam_thenEmitsEmptyList`() = runTest {
        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        val playersCollection = mockk<CollectionReference>()
        val playersQuery = mockk<Query>()

        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.whereEqualTo("teamId", "team-doc-id") } returns playersQuery
        every { playersQuery.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        val mockError = mockk<FirebaseFirestoreException>(relaxed = true)

        dataSource.getPlayersByTeam("team-doc-id").test {
            listenerSlot.captured.onEvent(null, mockError)
            val result = awaitItem()
            assertEquals(emptyList<Player>(), result)
            cancel()
        }
    }

    @Test
    fun `givenDeletedPlayerInList_whenGetPlayersByTeam_thenFiltersDeleted`() = runTest {
        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        val playersCollection = mockk<CollectionReference>()
        val playersQuery = mockk<Query>()
        val querySnapshot = mockk<QuerySnapshot>()
        val deletedDoc = mockk<DocumentSnapshot>()
        val validDoc = mockk<DocumentSnapshot>()

        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.whereEqualTo("teamId", "team-doc-id") } returns playersQuery
        every { playersQuery.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        val deletedModel = PlayerFirestoreModel(
            id = "deleted-id",
            teamId = "team-doc-id",
            firstName = "Deleted",
            lastName = "Player",
            deleted = true
        )
        val validModel = PlayerFirestoreModel(
            id = "valid-id",
            teamId = "team-doc-id",
            firstName = "Valid",
            lastName = "Player",
            deleted = false
        )
        every { deletedDoc.id } returns "deleted-id"
        every { deletedDoc.toObject(PlayerFirestoreModel::class.java) } returns deletedModel
        every { validDoc.id } returns "valid-id"
        every { validDoc.toObject(PlayerFirestoreModel::class.java) } returns validModel
        every { querySnapshot.documents } returns listOf(deletedDoc, validDoc)

        dataSource.getPlayersByTeam("team-doc-id").test {
            listenerSlot.captured.onEvent(querySnapshot, null)
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("valid-id", result[0].id)
            cancel()
        }
    }

    @Test
    fun `givenNullSnapshotInGetPlayersByTeam_thenEmitsEmptyList`() = runTest {
        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        val playersCollection = mockk<CollectionReference>()
        val playersQuery = mockk<Query>()

        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.whereEqualTo("teamId", "team-doc-id") } returns playersQuery
        every { playersQuery.addSnapshotListener(capture(listenerSlot)) } returns mockListenerRegistration

        dataSource.getPlayersByTeam("team-doc-id").test {
            listenerSlot.captured.onEvent(null, null)
            val result = awaitItem()
            assertEquals(emptyList<Player>(), result)
            cancel()
        }
    }

    @Test
    fun `givenStorageGooglesDotComUrl_whenIsFirebaseStorageUrl_thenReturnsTrue`() = runTest {
        setupUserWithTeam()

        val playersCollection = mockk<CollectionReference>()
        val playerDocRef = mockk<DocumentReference>()
        every { playerDocRef.id } returns "player-doc-id"
        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.document() } returns playerDocRef

        val voidTask = mockk<Task<Void>>()
        every { playerDocRef.set(any()) } returns voidTask
        coEvery { voidTask.await() } returns mockk()

        val googleStorageUrl = "https://storage.googleapis.com/bucket/image.jpg"
        val player = mockk<Player>(relaxed = true)
        every { player.imageUri } returns googleStorageUrl

        val result = dataSource.insertPlayer(player)

        assertTrue(result.isNotEmpty())
    }

    @Test
    fun `givenCaptainPlayerInMultipleDocuments_whenGetCaptainPlayer_thenReturnsCaptainFromFirst`() =
        runTest {
            setupUserWithTeam()

            val playersCollection = mockk<CollectionReference>()
            val playerQuery = mockk<Query>(relaxed = true)
            val playerSnapshot = mockk<QuerySnapshot>()
            val playerDoc = mockk<DocumentSnapshot>()

            every { mockFirestore.collection("players") } returns playersCollection
            every { playersCollection.whereEqualTo("teamId", "team-doc-id") } returns playerQuery
            every { playerQuery.whereEqualTo(any<String>(), any()) } returns playerQuery
            every { playerQuery.limit(any()) } returns playerQuery
            val playerTask = mockk<Task<QuerySnapshot>>()
            every { playerQuery.get() } returns playerTask
            coEvery { playerTask.await() } returns playerSnapshot

            val playerModel = PlayerFirestoreModel(
                id = "captain-id",
                teamId = "team-doc-id",
                firstName = "Captain",
                lastName = "One",
                isCaptain = true,
                deleted = false
            )
            every { playerDoc.id } returns "captain-id"
            every { playerDoc.toObject(PlayerFirestoreModel::class.java) } returns playerModel
            every { playerSnapshot.documents } returns listOf(playerDoc)

            val result = dataSource.getCaptainPlayer()

            assertNotNull(result)
            assertEquals("captain-id", result!!.id)
        }

    @Test
    fun `givenNoAuthUser_whenGetTeamDocumentId_thenReturnsNull`() = runTest {
        every { mockAuth.currentUser } returns null

        val result = dataSource.getPlayerById("player-123")

        assertNull(result)
    }

    @Test
    fun `givenExceptionInGetTeamDocumentId_whenGetAllPlayers_thenEmitsEmptyList`() = runTest {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        every { mockAuth.currentUser } returns mockUser
        every { mockUser.uid } returns "user-123"
        val teamsCollection = mockk<CollectionReference>()
        val teamQuery = mockk<Query>()

        every { mockFirestore.collection("teams") } returns teamsCollection
        every { teamsCollection.whereEqualTo("assignedCoachId", "user-123") } returns teamQuery
        every { teamQuery.limit(1) } returns teamQuery
        val teamTask = mockk<Task<QuerySnapshot>>()
        every { teamQuery.get() } returns teamTask
        coEvery { teamTask.await() } throws RuntimeException("Firestore error")

        dataSource.getAllPlayers().test {
            val result = awaitItem()
            assertEquals(emptyList<Player>(), result)
            cancel()
        }
    }

    @Test
    fun `givenValidPlayerWithNullImageUrl_whenUpdatePlayer_thenSucceeds`() = runTest {
        setupUserWithTeam()

        val playersCollection = mockk<CollectionReference>()
        val playerDocRef = mockk<DocumentReference>()
        val playerDoc = mockk<DocumentSnapshot>()

        every { mockFirestore.collection("players") } returns playersCollection
        every { playersCollection.document("player-doc-id") } returns playerDocRef
        val getTask = mockk<Task<DocumentSnapshot>>()
        every { playerDocRef.get() } returns getTask
        coEvery { getTask.await() } returns playerDoc

        val playerModel = PlayerFirestoreModel(
            id = "player-doc-id",
            teamId = "team-doc-id",
            firstName = "Player",
            lastName = "One",
            deleted = false,
            imageUri = null
        )
        every { playerDoc.exists() } returns true
        every { playerDoc.toObject(PlayerFirestoreModel::class.java) } returns playerModel

        val setTask = mockk<Task<Void>>()
        every { playerDocRef.set(any()) } returns setTask
        coEvery { setTask.await() } returns mockk()

        val player = mockk<Player>(relaxed = true)
        every { player.id } returns "player-doc-id"
        every { player.imageUri } returns null

        dataSource.updatePlayer(player)
    }
}
