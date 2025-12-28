package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.jesuslcorominas.teamflowmanager.data.core.datasource.ClubDataSource
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.ClubFirestoreModel
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toDomain
import com.jesuslcorominas.teamflowmanager.domain.model.Club
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Firestore-based implementation of ClubDataSource.
 * This implementation queries club data from Firebase Firestore.
 */
class ClubFirestoreDataSourceImpl(
    private val firestore: FirebaseFirestore,
) : ClubDataSource {

    companion object {
        private const val TAG = "ClubFirestoreDataSource"
        private const val CLUBS_COLLECTION = "clubs"
    }

    override fun findClubByInvitationCode(invitationCode: String): Flow<Club?> = callbackFlow {
        if (invitationCode.isBlank()) {
            Log.w(TAG, "Empty invitation code provided")
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }

        val listenerRegistration = firestore.collection(CLUBS_COLLECTION)
            .whereEqualTo("invitationCode", invitationCode)
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error finding club by invitation code", error)
                    trySend(null)
                    return@addSnapshotListener
                }

                val document = snapshot?.documents?.firstOrNull()
                if (document == null) {
                    trySend(null)
                    return@addSnapshotListener
                }

                val documentId = document.id
                val firestoreModel = document.toObject(ClubFirestoreModel::class.java)

                if (firestoreModel != null) {
                    val modelWithId = if (firestoreModel.id.isEmpty()) {
                        firestoreModel.copy(id = documentId)
                    } else {
                        firestoreModel
                    }
                    val club = modelWithId.toDomain()
                    Log.d(TAG, "Club found with invitation code: $invitationCode, clubId: ${club.firestoreId}")
                    trySend(club)
                } else {
                    trySend(null)
                }
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    override fun getClubByFirestoreId(firestoreId: String): Flow<Club?> = callbackFlow {
        if (firestoreId.isBlank()) {
            Log.w(TAG, "Empty firestore ID provided")
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }

        val listenerRegistration = firestore.collection(CLUBS_COLLECTION)
            .document(firestoreId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error getting club by firestore ID", error)
                    trySend(null)
                    return@addSnapshotListener
                }

                if (snapshot == null || !snapshot.exists()) {
                    trySend(null)
                    return@addSnapshotListener
                }

                val documentId = snapshot.id
                val firestoreModel = snapshot.toObject(ClubFirestoreModel::class.java)

                if (firestoreModel != null) {
                    val modelWithId = if (firestoreModel.id.isEmpty()) {
                        firestoreModel.copy(id = documentId)
                    } else {
                        firestoreModel
                    }
                    val club = modelWithId.toDomain()
                    Log.d(TAG, "Club found with firestoreId: $firestoreId")
                    trySend(club)
                } else {
                    trySend(null)
                }
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }
}
