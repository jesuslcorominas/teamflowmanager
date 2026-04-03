package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PendingCoachAssignmentDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.PendingCoachAssignment
import kotlinx.coroutines.tasks.await

class PendingCoachAssignmentFirestoreDataSourceImpl(
    private val firestore: FirebaseFirestore,
) : PendingCoachAssignmentDataSource {
    companion object {
        private const val TAG = "PendingCoachAssignDS"
        private const val COLLECTION = "pendingCoachAssignments"
    }

    override suspend fun create(teamId: String, clubId: String, email: String) {
        try {
            firestore.collection(COLLECTION).document(teamId)
                .set(mapOf("teamId" to teamId, "clubId" to clubId, "email" to email))
                .await()
            Log.d(TAG, "Created pending coach assignment for teamId=$teamId email=$email")
        } catch (e: Exception) {
            Log.e(TAG, "Error creating pending coach assignment", e)
            throw e
        }
    }

    override suspend fun delete(teamId: String) {
        try {
            firestore.collection(COLLECTION).document(teamId).delete().await()
            Log.d(TAG, "Deleted pending coach assignment for teamId=$teamId")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting pending coach assignment", e)
            throw e
        }
    }

    override suspend fun getByEmail(email: String): List<PendingCoachAssignment> {
        return try {
            val snapshot = firestore.collection(COLLECTION)
                .whereEqualTo("email", email)
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                val teamId = doc.getString("teamId") ?: return@mapNotNull null
                val clubId = doc.getString("clubId") ?: return@mapNotNull null
                val docEmail = doc.getString("email") ?: return@mapNotNull null
                PendingCoachAssignment(teamId, clubId, docEmail)
            }.also {
                Log.d(TAG, "Found ${it.size} pending assignments for email=$email")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting pending coach assignments by email", e)
            throw e
        }
    }
}