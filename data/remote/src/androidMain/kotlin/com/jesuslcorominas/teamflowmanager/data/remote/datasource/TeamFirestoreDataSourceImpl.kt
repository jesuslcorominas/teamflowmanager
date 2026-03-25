package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.jesuslcorominas.teamflowmanager.data.core.datasource.TeamDataSource
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.TeamFirestoreModel
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toDomain
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toFirestoreModel
import com.jesuslcorominas.teamflowmanager.domain.model.Team
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

/**
 * Firestore-based implementation of TeamLocalDataSource.
 * This implementation stores team data in Firebase Firestore as a remote data source.
 * Team documents are stored in the "teams" collection with auto-generated document IDs.
 * The assignedCoachId field stores the user ID of the assigned coach (null if no coach assigned yet).
 * Teams are associated with clubs via the clubId field.
 */
class TeamFirestoreDataSourceImpl(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
) : TeamDataSource {
    companion object {
        private const val TEAMS_COLLECTION = "teams"
    }

    private fun documentToTeam(document: DocumentSnapshot): Team? {
        return try {
            val model = document.toObject(TeamFirestoreModel::class.java) ?: return null
            model.copy(id = document.id).toDomain()
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Gets the first team assigned to the current user (as coach) from Firestore.
     */
    override fun getTeam(): Flow<Team?> =
        callbackFlow {
            val currentUserId = firebaseAuth.currentUser?.uid
            if (currentUserId == null) {
                trySend(null)
                awaitClose { }
                return@callbackFlow
            }

            val listenerRegistration =
                firestore.collection(TEAMS_COLLECTION)
                    .whereEqualTo("assignedCoachId", currentUserId)
                    .limit(1)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            trySend(null)
                            return@addSnapshotListener
                        }

                        val document = snapshot?.documents?.firstOrNull()
                        trySend(document?.let { documentToTeam(it) })
                    }

            awaitClose {
                listenerRegistration.remove()
            }
        }

    override suspend fun insertTeam(team: Team) {
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId == null) {
            throw IllegalStateException("User must be authenticated to create a team")
        }

        try {
            val firestoreModel = team.toFirestoreModel()
            // Use auto-generated document ID for new teams
            val docRef = firestore.collection(TEAMS_COLLECTION).document()
            // Set the document id
            val modelWithId = firestoreModel.copy(id = docRef.id)
            docRef.set(modelWithId).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun updateTeam(team: Team) {
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId == null) {
            throw IllegalStateException("User must be authenticated to update a team")
        }

        try {
            val documentId = team.firestoreId
            if (documentId.isNullOrEmpty()) {
                throw IllegalStateException("Cannot update team without Firestore document ID")
            }

            val firestoreModel = team.toFirestoreModel()
            firestore.collection(TEAMS_COLLECTION)
                .document(documentId)
                .set(firestoreModel)
                .await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw e
        }
    }

    override fun getTeamByCoachId(coachId: String): Flow<Team?> =
        callbackFlow {
            val listenerRegistration =
                firestore.collection(TEAMS_COLLECTION)
                    .document(coachId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            trySend(null)
                            return@addSnapshotListener
                        }

                        if (snapshot == null || !snapshot.exists()) {
                            trySend(null)
                            return@addSnapshotListener
                        }

                        trySend(documentToTeam(snapshot))
                    }

            awaitClose {
                listenerRegistration.remove()
            }
        }

    override fun getTeamsByClub(clubFirestoreId: String): Flow<List<Team>> =
        callbackFlow {
            require(clubFirestoreId.isNotBlank()) { "Club Firestore ID cannot be blank" }

            val listenerRegistration =
                firestore.collection(TEAMS_COLLECTION)
                    .whereEqualTo("clubId", clubFirestoreId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            trySend(emptyList())
                            return@addSnapshotListener
                        }

                        if (snapshot == null || snapshot.isEmpty) {
                            trySend(emptyList())
                            return@addSnapshotListener
                        }

                        val teams = snapshot.documents.mapNotNull { documentToTeam(it) }

                        trySend(teams)
                    }

            awaitClose {
                listenerRegistration.remove()
            }
        }

    /**
     * This method is not applicable for remote Firestore data source.
     * It's only relevant for local Room database.
     * @return false as remote storage always has userId association
     */
    override suspend fun hasLocalTeamWithoutUserId(): Boolean = false

    /**
     * This method is not applicable for remote Firestore data source.
     * @return null as direct access is not needed for remote storage
     */
    override suspend fun getTeamDirect(): Team? = null

    /**
     * This method is not applicable for remote Firestore data source.
     * Only relevant for local Room database cleanup.
     */
    override suspend fun clearLocalData() {
        // No-op for remote data source
    }

    override suspend fun getOrphanTeams(ownerId: String): List<Team> {
        require(ownerId.isNotBlank()) { "Owner ID cannot be blank" }

        try {
            // Get all teams that don't have a clubId (orphan teams)
            // Since we removed ownerId, orphan teams are simply teams without a clubId
            val querySnapshot =
                firestore.collection(TEAMS_COLLECTION)
                    .get()
                    .await()

            // Filter to only include teams without a clubId field (orphan teams)
            val teams =
                querySnapshot.documents.mapNotNull { document ->
                    val hasClubId = document.contains("clubId") && document.getString("clubId") != null
                    if (hasClubId) return@mapNotNull null
                    documentToTeam(document)
                }

            return teams
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun updateTeamClubId(
        teamFirestoreId: String,
        clubId: Long,
        clubFirestoreId: String,
    ) {
        require(teamFirestoreId.isNotBlank()) { "Team Firestore ID cannot be blank" }
        require(clubFirestoreId.isNotBlank()) { "Club Firestore ID cannot be blank" }

        try {
            val updates =
                mapOf(
                    "clubId" to clubFirestoreId,
                )

            // Use set with merge to ensure the operation succeeds even if the document
            // doesn't have all fields yet
            firestore.collection(TEAMS_COLLECTION)
                .document(teamFirestoreId)
                .set(updates, com.google.firebase.firestore.SetOptions.merge())
                .await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun getTeamByFirestoreId(teamFirestoreId: String): Team? {
        require(teamFirestoreId.isNotBlank()) { "Team Firestore ID cannot be blank" }

        try {
            val document =
                firestore.collection(TEAMS_COLLECTION)
                    .document(teamFirestoreId)
                    .get()
                    .await()

            if (!document.exists()) {
                return null
            }

            return documentToTeam(document)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun updateTeamCoachId(
        teamFirestoreId: String,
        coachId: String,
    ) {
        require(teamFirestoreId.isNotBlank()) { "Team Firestore ID cannot be blank" }
        require(coachId.isNotBlank()) { "Coach ID cannot be blank" }

        try {
            val updates =
                mapOf(
                    "assignedCoachId" to coachId,
                )

            firestore.collection(TEAMS_COLLECTION)
                .document(teamFirestoreId)
                .update(updates)
                .await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw e
        }
    }
}
