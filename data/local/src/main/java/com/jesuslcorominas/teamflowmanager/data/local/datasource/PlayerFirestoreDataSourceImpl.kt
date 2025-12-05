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
 * The ownerId field is set to the current authenticated user's ID as required by security rules.
 */
class PlayerFirestoreDataSourceImpl(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
) : PlayerLocalDataSource {

    companion object {
        private const val TAG = "PlayerFirestoreDS"
        private const val PLAYERS_COLLECTION = "players"
    }

    /**
     * Gets all players owned by the current user from Firestore as a real-time Flow.
     */
    override fun getAllPlayers(): Flow<List<Player>> = callbackFlow {
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId == null) {
            Log.w(TAG, "No authenticated user, cannot get players")
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val listenerRegistration = firestore.collection(PLAYERS_COLLECTION)
            .whereEqualTo("ownerId", currentUserId)
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

                Log.d(TAG, "Loaded ${players.size} players")
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
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId == null) {
            Log.w(TAG, "No authenticated user, cannot get player")
            return null
        }

        return try {
            val snapshot = firestore.collection(PLAYERS_COLLECTION)
                .whereEqualTo("ownerId", currentUserId)
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
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId == null) {
            Log.w(TAG, "No authenticated user, cannot get captain")
            return null
        }

        return try {
            val snapshot = firestore.collection(PLAYERS_COLLECTION)
                .whereEqualTo("ownerId", currentUserId)
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
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId == null) {
            Log.e(TAG, "No authenticated user, cannot set captain")
            throw IllegalStateException("User must be authenticated to set captain")
        }

        try {
            // First, clear all existing captains
            clearAllCaptains(currentUserId)

            // Then, find the document ID for this player
            val documentId = findDocumentIdByPlayerId(currentUserId, playerId)
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
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId == null) {
            Log.e(TAG, "No authenticated user, cannot remove captain")
            throw IllegalStateException("User must be authenticated to remove captain")
        }

        try {
            // First, find the document ID for this player
            val documentId = findDocumentIdByPlayerId(currentUserId, playerId)
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
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId == null) {
            Log.e(TAG, "No authenticated user, cannot insert player")
            throw IllegalStateException("User must be authenticated to create a player")
        }

        try {
            val firestoreModel = player.toFirestoreModel()
            // Use auto-generated document ID for new players
            val docRef = firestore.collection(PLAYERS_COLLECTION).document()
            // Set the ownerId to current user and document id
            val modelWithOwner = firestoreModel.copy(id = docRef.id, ownerId = currentUserId)
            docRef.set(modelWithOwner).await()
            Log.d(TAG, "Player inserted successfully with id: ${docRef.id}, ownerId: $currentUserId")
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
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId == null) {
            Log.e(TAG, "No authenticated user, cannot delete player")
            throw IllegalStateException("User must be authenticated to delete a player")
        }

        try {
            // First, find the document ID for this player
            val documentId = findDocumentIdByPlayerId(currentUserId, playerId)
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
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId == null) {
            Log.e(TAG, "No authenticated user, cannot update player")
            throw IllegalStateException("User must be authenticated to update a player")
        }

        try {
            // First, find the document ID for this player
            val documentId = findDocumentIdByPlayerId(currentUserId, player.id)
            if (documentId == null) {
                Log.w(TAG, "Cannot find player with id: ${player.id} to update")
                throw IllegalStateException("Cannot update player without document ID")
            }

            val firestoreModel = player.toFirestoreModel()
            // Ensure ownerId and id are set correctly
            val modelWithOwner = firestoreModel.copy(id = documentId, ownerId = currentUserId)
            firestore.collection(PLAYERS_COLLECTION)
                .document(documentId)
                .set(modelWithOwner)
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
    private suspend fun findDocumentIdByPlayerId(ownerId: String, playerId: Long): String? {
        val snapshot = firestore.collection(PLAYERS_COLLECTION)
            .whereEqualTo("ownerId", ownerId)
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
     * Helper function to clear captain status from all players for the current owner.
     */
    private suspend fun clearAllCaptains(ownerId: String) {
        val snapshot = firestore.collection(PLAYERS_COLLECTION)
            .whereEqualTo("ownerId", ownerId)
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
