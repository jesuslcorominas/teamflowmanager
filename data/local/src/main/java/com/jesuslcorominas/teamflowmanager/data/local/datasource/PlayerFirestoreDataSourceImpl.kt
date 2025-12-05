package com.jesuslcorominas.teamflowmanager.data.local.datasource

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerLocalDataSource
import com.jesuslcorominas.teamflowmanager.data.local.firestore.PlayerFirestoreModel
import com.jesuslcorominas.teamflowmanager.data.local.firestore.toDomain
import com.jesuslcorominas.teamflowmanager.data.local.firestore.toFirestoreModel
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

/**
 * Firestore-based implementation of PlayerLocalDataSource.
 * This implementation stores player data in Firebase Firestore instead of local Room database.
 * Player documents are stored in the "players" collection with auto-generated document IDs.
 * The teamId field stores the Firestore document ID of the team, which is used by
 * security rules to validate that the authenticated user is the owner of the team.
 */
class PlayerFirestoreDataSourceImpl(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
) : PlayerLocalDataSource {

    companion object {
        private const val TAG = "PlayerFirestoreDS"
        private const val PLAYERS_COLLECTION = "players"
        private const val TEAMS_COLLECTION = "teams"
    }

    /**
     * Gets the team's Firestore document ID for the current authenticated user.
     * This is needed because security rules validate player access based on team ownership.
     */
    private suspend fun getTeamDocumentId(): String? {
        val currentUserId = firebaseAuth.currentUser?.uid ?: return null
        
        return try {
            val snapshot = firestore.collection(TEAMS_COLLECTION)
                .whereEqualTo("ownerId", currentUserId)
                .limit(1)
                .get()
                .await()
            
            snapshot.documents.firstOrNull()?.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error getting team document ID", e)
            null
        }
    }

    /**
     * Gets all players for the current user's team from Firestore as a real-time Flow.
     */
    override fun getAllPlayers(): Flow<List<Player>> = callbackFlow {
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId == null) {
            Log.w(TAG, "No authenticated user, cannot get players")
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        // First, get the team document ID
        val teamDocId = getTeamDocumentId()
        if (teamDocId == null) {
            Log.w(TAG, "No team found for user, cannot get players")
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val listenerRegistration = firestore.collection(PLAYERS_COLLECTION)
            .whereEqualTo("teamId", teamDocId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error getting players from Firestore", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val players = snapshot?.documents?.mapNotNull { document ->
                    val documentId = document.id
                    val firestoreModel = document.toObject(PlayerFirestoreModel::class.java)
                    firestoreModel?.let {
                        val modelWithId = if (it.id.isEmpty()) {
                            it.copy(id = documentId)
                        } else {
                            it
                        }
                        modelWithId.toDomain()
                    }
                } ?: emptyList()

                Log.d(TAG, "Loaded ${players.size} players for team: $teamDocId")
                trySend(players)
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    /**
     * Gets a player by its document ID.
     * Note: The playerId is a Long derived from the Firestore document ID hash.
     * We need to query all players and filter by the stable ID.
     */
    override suspend fun getPlayerById(playerId: Long): Player? {
        val teamDocId = getTeamDocumentId()
        if (teamDocId == null) {
            Log.w(TAG, "No team found, cannot get player")
            return null
        }

        return try {
            val snapshot = firestore.collection(PLAYERS_COLLECTION)
                .whereEqualTo("teamId", teamDocId)
                .get()
                .await()

            snapshot.documents.mapNotNull { document ->
                val documentId = document.id
                val firestoreModel = document.toObject(PlayerFirestoreModel::class.java)
                firestoreModel?.let {
                    val modelWithId = if (it.id.isEmpty()) {
                        it.copy(id = documentId)
                    } else {
                        it
                    }
                    modelWithId.toDomain()
                }
            }.find { it.id == playerId }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error getting player by id from Firestore", e)
            null
        }
    }

    /**
     * Gets the captain player.
     */
    override suspend fun getCaptainPlayer(): Player? {
        val teamDocId = getTeamDocumentId()
        if (teamDocId == null) {
            Log.w(TAG, "No team found, cannot get captain")
            return null
        }

        return try {
            val snapshot = firestore.collection(PLAYERS_COLLECTION)
                .whereEqualTo("teamId", teamDocId)
                .whereEqualTo("isCaptain", true)
                .limit(1)
                .get()
                .await()

            val document = snapshot.documents.firstOrNull()
            if (document == null) {
                return null
            }

            val documentId = document.id
            val firestoreModel = document.toObject(PlayerFirestoreModel::class.java)
            firestoreModel?.let {
                val modelWithId = if (it.id.isEmpty()) {
                    it.copy(id = documentId)
                } else {
                    it
                }
                modelWithId.toDomain()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error getting captain from Firestore", e)
            null
        }
    }

    /**
     * Sets a player as captain by their Long ID.
     */
    override suspend fun setPlayerAsCaptain(playerId: Long) {
        val teamDocId = getTeamDocumentId()
        if (teamDocId == null) {
            Log.e(TAG, "No team found, cannot set captain")
            throw IllegalStateException("Team must exist to set captain")
        }

        try {
            // First, clear all existing captains
            clearAllCaptains(teamDocId)

            // Then, find the document ID for this player
            val documentId = findDocumentIdByPlayerId(teamDocId, playerId)
            if (documentId == null) {
                Log.w(TAG, "Cannot find player with id: $playerId")
                return
            }

            // Update the player as captain
            firestore.collection(PLAYERS_COLLECTION)
                .document(documentId)
                .update("isCaptain", true)
                .await()
            Log.d(TAG, "Player set as captain: $documentId")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error setting player as captain", e)
            throw e
        }
    }

    /**
     * Removes captain status from a player.
     */
    override suspend fun removePlayerAsCaptain(playerId: Long) {
        val teamDocId = getTeamDocumentId()
        if (teamDocId == null) {
            Log.e(TAG, "No team found, cannot remove captain")
            throw IllegalStateException("Team must exist to remove captain")
        }

        try {
            // First, find the document ID for this player
            val documentId = findDocumentIdByPlayerId(teamDocId, playerId)
            if (documentId == null) {
                Log.w(TAG, "Cannot find player with id: $playerId")
                return
            }

            // Update the player to remove captain status
            firestore.collection(PLAYERS_COLLECTION)
                .document(documentId)
                .update("isCaptain", false)
                .await()
            Log.d(TAG, "Captain status removed from player: $documentId")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error removing captain status", e)
            throw e
        }
    }

    /**
     * Inserts a new player into Firestore.
     */
    override suspend fun insertPlayer(player: Player) {
        val teamDocId = getTeamDocumentId()
        if (teamDocId == null) {
            Log.e(TAG, "No team found, cannot insert player")
            throw IllegalStateException("Team must exist to create a player")
        }

        try {
            val firestoreModel = player.toFirestoreModel()
            // Use auto-generated document ID for new players
            val docRef = firestore.collection(PLAYERS_COLLECTION).document()
            // Set the teamId to the team's Firestore document ID
            val modelWithTeam = firestoreModel.copy(id = docRef.id, teamId = teamDocId)
            docRef.set(modelWithTeam).await()
            Log.d(TAG, "Player inserted successfully with id: ${docRef.id}, teamId: $teamDocId")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting player to Firestore", e)
            throw e
        }
    }

    /**
     * Deletes a player from Firestore.
     */
    override suspend fun deletePlayer(playerId: Long) {
        val teamDocId = getTeamDocumentId()
        if (teamDocId == null) {
            Log.e(TAG, "No team found, cannot delete player")
            throw IllegalStateException("Team must exist to delete a player")
        }

        try {
            // First, find the document ID for this player
            val documentId = findDocumentIdByPlayerId(teamDocId, playerId)
            if (documentId == null) {
                Log.w(TAG, "Cannot find player with id: $playerId to delete")
                return
            }

            firestore.collection(PLAYERS_COLLECTION)
                .document(documentId)
                .delete()
                .await()
            Log.d(TAG, "Player deleted successfully: $documentId")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting player from Firestore", e)
            throw e
        }
    }

    /**
     * Updates an existing player in Firestore.
     */
    override suspend fun updatePlayer(player: Player) {
        val teamDocId = getTeamDocumentId()
        if (teamDocId == null) {
            Log.e(TAG, "No team found, cannot update player")
            throw IllegalStateException("Team must exist to update a player")
        }

        try {
            // First, find the document ID for this player
            val documentId = findDocumentIdByPlayerId(teamDocId, player.id)
            if (documentId == null) {
                Log.w(TAG, "Cannot find player with id: ${player.id} to update")
                throw IllegalStateException("Cannot update player without document ID")
            }

            val firestoreModel = player.toFirestoreModel()
            // Ensure teamId and id are set correctly
            val modelWithTeam = firestoreModel.copy(id = documentId, teamId = teamDocId)
            firestore.collection(PLAYERS_COLLECTION)
                .document(documentId)
                .set(modelWithTeam)
                .await()
            Log.d(TAG, "Player updated successfully: $documentId")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error updating player in Firestore", e)
            throw e
        }
    }

    /**
     * Helper function to find the Firestore document ID for a player based on the Long player ID.
     */
    private suspend fun findDocumentIdByPlayerId(teamDocId: String, playerId: Long): String? {
        val snapshot = firestore.collection(PLAYERS_COLLECTION)
            .whereEqualTo("teamId", teamDocId)
            .get()
            .await()

        for (document in snapshot.documents) {
            val documentId = document.id
            val firestoreModel = document.toObject(PlayerFirestoreModel::class.java)
            firestoreModel?.let {
                val modelWithId = if (it.id.isEmpty()) {
                    it.copy(id = documentId)
                } else {
                    it
                }
                val player = modelWithId.toDomain()
                if (player.id == playerId) {
                    return documentId
                }
            }
        }
        return null
    }

    /**
     * Helper function to clear captain status from all players for the team.
     */
    private suspend fun clearAllCaptains(teamDocId: String) {
        val snapshot = firestore.collection(PLAYERS_COLLECTION)
            .whereEqualTo("teamId", teamDocId)
            .whereEqualTo("isCaptain", true)
            .get()
            .await()

        for (document in snapshot.documents) {
            firestore.collection(PLAYERS_COLLECTION)
                .document(document.id)
                .update("isCaptain", false)
                .await()
        }
        Log.d(TAG, "Cleared captain status from ${snapshot.documents.size} players")
    }
}
