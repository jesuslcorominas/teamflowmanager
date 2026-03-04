package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import com.jesuslcorominas.teamflowmanager.data.core.datasource.PlayerDataSource
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.PlayerFirestoreModel
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toDomain
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toFirestoreModel
import com.jesuslcorominas.teamflowmanager.data.remote.util.toStableId
import com.jesuslcorominas.teamflowmanager.domain.model.Player
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.FirebaseFirestoreException
import dev.gitlive.firebase.firestore.where
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlin.coroutines.cancellation.CancellationException

class PlayerFirestoreDataSourceImpl(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
) : PlayerDataSource {

    companion object {
        private const val PLAYERS_COLLECTION = "players"
        private const val TEAMS_COLLECTION = "teams"
    }

    private suspend fun getTeamDocumentId(): String? {
        val currentUserId = firebaseAuth.currentUser?.uid ?: return null
        return try {
            val snapshot = firestore.collection(TEAMS_COLLECTION)
                .where { "assignedCoachId" equalTo currentUserId }
                .limit(1)
                .get()
            snapshot.documents.firstOrNull()?.id
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun findDocumentIdByPlayerId(teamDocId: String, playerId: Long): String? {
        return try {
            val snapshot = firestore.collection(PLAYERS_COLLECTION)
                .where { "teamId" equalTo teamDocId }
                .get()
            snapshot.documents.firstOrNull { doc ->
                doc.id.toStableId() == playerId
            }?.id
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
    }

    override fun getAllPlayers(): Flow<List<Player>> = flow {
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId == null) {
            emit(emptyList())
            return@flow
        }
        val teamDocId = getTeamDocumentId()
        if (teamDocId == null) {
            emit(emptyList())
            return@flow
        }
        val snapshots = firestore.collection(PLAYERS_COLLECTION)
            .where { "teamId" equalTo teamDocId }
            .where { "deleted" equalTo false }
            .snapshots
        emitAll(
            snapshots.map { qs ->
                qs.documents.mapNotNull { doc ->
                    try {
                        val model = doc.data<PlayerFirestoreModel>()
                        model.copy(id = doc.id, teamId = teamDocId).toDomain()
                    } catch (_: Exception) {
                        null
                    }
                }
            }.catch { e ->
                if (e is FirebaseFirestoreException) emit(emptyList()) else throw e
            },
        )
    }

    override suspend fun getPlayerById(playerId: Long): Player? {
        val teamDocId = getTeamDocumentId() ?: return null
        return try {
            val snapshot = firestore.collection(PLAYERS_COLLECTION)
                .where { "teamId" equalTo teamDocId }
                .where { "deleted" equalTo false }
                .get()
            snapshot.documents.mapNotNull { doc ->
                try {
                    val model = doc.data<PlayerFirestoreModel>()
                    model.copy(id = doc.id, teamId = teamDocId).toDomain()
                } catch (_: Exception) {
                    null
                }
            }.find { it.id == playerId }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun getCaptainPlayer(): Player? {
        val teamDocId = getTeamDocumentId() ?: return null
        return try {
            val snapshot = firestore.collection(PLAYERS_COLLECTION)
                .where { "teamId" equalTo teamDocId }
                .where { "captain" equalTo true }
                .where { "deleted" equalTo false }
                .get()
            snapshot.documents.firstOrNull()?.let { doc ->
                try {
                    val model = doc.data<PlayerFirestoreModel>()
                    model.copy(id = doc.id, teamId = teamDocId).toDomain()
                } catch (_: Exception) {
                    null
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun setPlayerAsCaptain(playerId: Long) {
        val teamDocId = getTeamDocumentId() ?: return
        // Clear existing captains first
        try {
            val snapshot = firestore.collection(PLAYERS_COLLECTION)
                .where { "teamId" equalTo teamDocId }
                .where { "captain" equalTo true }
                .get()
            snapshot.documents.forEach { doc ->
                firestore.collection(PLAYERS_COLLECTION).document(doc.id)
                    .update("captain" to false)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Best-effort clear
        }
        // Set new captain
        val documentId = findDocumentIdByPlayerId(teamDocId, playerId) ?: return
        try {
            firestore.collection(PLAYERS_COLLECTION).document(documentId)
                .update("captain" to true)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Best-effort
        }
    }

    override suspend fun removePlayerAsCaptain(playerId: Long) {
        val teamDocId = getTeamDocumentId() ?: return
        val documentId = findDocumentIdByPlayerId(teamDocId, playerId) ?: return
        try {
            firestore.collection(PLAYERS_COLLECTION).document(documentId)
                .update("captain" to false)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Best-effort
        }
    }

    override suspend fun insertPlayer(player: Player): Long {
        val teamDocId = getTeamDocumentId()
            ?: throw IllegalStateException("No team found for current user")
        val model = player.toFirestoreModel().copy(teamId = teamDocId)
        return try {
            val docRef = firestore.collection(PLAYERS_COLLECTION).add(model)
            docRef.id.toStableId()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun updatePlayer(player: Player) {
        val teamDocId = getTeamDocumentId()
            ?: throw IllegalStateException("No team found for current user")
        val documentId = findDocumentIdByPlayerId(teamDocId, player.id)
            ?: throw IllegalStateException("Cannot find Firestore document for player ${player.id}")
        val model = player.toFirestoreModel().copy(id = documentId, teamId = teamDocId)
        try {
            firestore.collection(PLAYERS_COLLECTION).document(documentId).set(model)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun deletePlayer(playerId: Long) {
        val teamDocId = getTeamDocumentId() ?: return
        val documentId = findDocumentIdByPlayerId(teamDocId, playerId) ?: return
        try {
            firestore.collection(PLAYERS_COLLECTION).document(documentId)
                .update("deleted" to true)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Best-effort
        }
    }

    override suspend fun getAllPlayersDirect(): List<Player> {
        val teamDocId = getTeamDocumentId() ?: return emptyList()
        return try {
            val snapshot = firestore.collection(PLAYERS_COLLECTION)
                .where { "teamId" equalTo teamDocId }
                .where { "deleted" equalTo false }
                .get()
            snapshot.documents.mapNotNull { doc ->
                try {
                    val model = doc.data<PlayerFirestoreModel>()
                    model.copy(id = doc.id, teamId = teamDocId).toDomain()
                } catch (_: Exception) {
                    null
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun clearLocalData() {
        // No-op for remote data source
    }
}
