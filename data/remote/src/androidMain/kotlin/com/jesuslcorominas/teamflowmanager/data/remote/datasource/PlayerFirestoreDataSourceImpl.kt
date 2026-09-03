package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.jesuslcorominas.teamflowmanager.data.core.datasource.ImageStorageDataSource
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerDataSource
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.PlayerFirestoreModel
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toDomain
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toFirestoreModel
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

/**
 * Firestore-based implementation of PlayerDataSource.
 */
class PlayerFirestoreDataSourceImpl(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val imageStorageDataSource: ImageStorageDataSource,
) : PlayerDataSource {
    companion object {
        private const val PLAYERS_COLLECTION = "players"
        private const val TEAMS_COLLECTION = "teams"
        private const val PLAYER_IMAGES_PATH = "players_images"
    }

    private suspend fun getTeamDocumentId(): String? {
        val currentUserId = firebaseAuth.currentUser?.uid ?: return null
        return try {
            val snapshot =
                firestore.collection(TEAMS_COLLECTION)
                    .whereEqualTo("assignedCoachId", currentUserId)
                    .limit(1)
                    .get()
                    .await()
            snapshot.documents.firstOrNull()?.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }

    private fun documentToPlayer(document: DocumentSnapshot): Player? {
        return try {
            val model = document.toObject(PlayerFirestoreModel::class.java) ?: return null
            model.copy(id = document.id).toDomain()
        } catch (_: Exception) {
            null
        }
    }

    override fun getPlayersByTeam(teamId: String): Flow<List<Player>> =
        callbackFlow {
            val listenerRegistration =
                firestore.collection(PLAYERS_COLLECTION)
                    .whereEqualTo("teamId", teamId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            trySend(emptyList())
                            return@addSnapshotListener
                        }
                        val players =
                            snapshot?.documents
                                ?.mapNotNull { documentToPlayer(it) }
                                ?.filter { !it.deleted }
                                ?: emptyList()
                        trySend(players)
                    }
            awaitClose { listenerRegistration.remove() }
        }

    override fun getAllPlayers(): Flow<List<Player>> =
        callbackFlow {
            val currentUserId = firebaseAuth.currentUser?.uid
            if (currentUserId == null) {
                trySend(emptyList())
                awaitClose { }
                return@callbackFlow
            }

            val teamDocId = getTeamDocumentId()
            if (teamDocId == null) {
                trySend(emptyList())
                awaitClose { }
                return@callbackFlow
            }

            val listenerRegistration =
                firestore.collection(PLAYERS_COLLECTION)
                    .whereEqualTo("teamId", teamDocId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            trySend(emptyList())
                            return@addSnapshotListener
                        }
                        val players =
                            snapshot?.documents
                                ?.mapNotNull { documentToPlayer(it) }
                                ?.filter { !it.deleted }
                                ?: emptyList()
                        trySend(players)
                    }

            awaitClose { listenerRegistration.remove() }
        }

    override suspend fun getPlayerById(playerId: String): Player? {
        return try {
            val snapshot =
                firestore.collection(PLAYERS_COLLECTION)
                    .document(playerId)
                    .get()
                    .await()
            if (!snapshot.exists()) return null
            documentToPlayer(snapshot)?.takeIf { !it.deleted }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun getCaptainPlayer(): Player? {
        val teamDocId = getTeamDocumentId() ?: return null
        return try {
            val snapshot =
                firestore.collection(PLAYERS_COLLECTION)
                    .whereEqualTo("teamId", teamDocId)
                    .whereEqualTo("isCaptain", true)
                    .limit(1)
                    .get()
                    .await()
            val document = snapshot.documents.firstOrNull() ?: return null
            documentToPlayer(document)?.takeIf { !it.deleted }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun setPlayerAsCaptain(playerId: String) {
        val teamDocId = getTeamDocumentId() ?: throw IllegalStateException("Team must exist to set captain")
        try {
            clearAllCaptains(teamDocId)
            firestore.collection(PLAYERS_COLLECTION)
                .document(playerId)
                .update("isCaptain", true)
                .await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun removePlayerAsCaptain(playerId: String) {
        try {
            firestore.collection(PLAYERS_COLLECTION)
                .document(playerId)
                .update("isCaptain", false)
                .await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun insertPlayer(player: Player): String {
        val teamDocId = getTeamDocumentId() ?: throw IllegalStateException("Team must exist to create a player")
        try {
            val docRef = firestore.collection(PLAYERS_COLLECTION).document()
            val imageUrl = uploadPlayerImageIfNeeded(player.imageUri, docRef.id)
            val firestoreModel = player.toFirestoreModel().copy(id = docRef.id, teamId = teamDocId, imageUri = imageUrl)
            docRef.set(firestoreModel).await()
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun deletePlayer(playerId: String) {
        try {
            firestore.collection(PLAYERS_COLLECTION)
                .document(playerId)
                .update("deleted", true)
                .await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun updatePlayer(player: Player) {
        val teamDocId = getTeamDocumentId() ?: throw IllegalStateException("Team must exist to update a player")
        try {
            val currentPlayer = getPlayerById(player.id)
            val currentImageUrl = currentPlayer?.imageUri

            val newImageUrl =
                when {
                    player.imageUri == null -> {
                        currentImageUrl?.let {
                            if (isFirebaseStorageUrl(it)) imageStorageDataSource.deleteImage(it)
                        }
                        null
                    }
                    player.imageUri == currentImageUrl -> currentImageUrl
                    player.imageUri != null && isLocalUri(player.imageUri!!) -> {
                        currentImageUrl?.let {
                            if (isFirebaseStorageUrl(it)) imageStorageDataSource.deleteImage(it)
                        }
                        uploadPlayerImageIfNeeded(player.imageUri, player.id)
                    }
                    else -> player.imageUri
                }

            val firestoreModel = player.toFirestoreModel().copy(id = player.id, teamId = teamDocId, imageUri = newImageUrl)
            firestore.collection(PLAYERS_COLLECTION)
                .document(player.id)
                .set(firestoreModel)
                .await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw e
        }
    }

    private suspend fun uploadPlayerImageIfNeeded(
        imageUri: String?,
        playerId: String,
    ): String? {
        if (imageUri == null) return null
        if (isFirebaseStorageUrl(imageUri)) return imageUri
        if (isLocalUri(imageUri)) {
            val ownerId = firebaseAuth.currentUser?.uid ?: return null
            val storagePath = "$PLAYER_IMAGES_PATH/$ownerId/$playerId.jpg"
            return imageStorageDataSource.uploadImage(imageUri, storagePath)
        }
        return imageUri
    }

    private fun isLocalUri(uri: String): Boolean = uri.startsWith("content://") || uri.startsWith("file://")

    private fun isFirebaseStorageUrl(url: String): Boolean = url.contains("firebasestorage.googleapis.com") || url.contains("storage.googleapis.com")

    private suspend fun clearAllCaptains(teamDocId: String) {
        val snapshot =
            firestore.collection(PLAYERS_COLLECTION)
                .whereEqualTo("teamId", teamDocId)
                .whereEqualTo("isCaptain", true)
                .get()
                .await()
        for (document in snapshot.documents) {
            val player = documentToPlayer(document) ?: continue
            if (!player.deleted) {
                firestore.collection(PLAYERS_COLLECTION)
                    .document(document.id)
                    .update("isCaptain", false)
                    .await()
            }
        }
    }

    override suspend fun getAllPlayersDirect(): List<Player> = emptyList()

    override suspend fun clearLocalData() {
        // No-op for remote data source
    }
}
