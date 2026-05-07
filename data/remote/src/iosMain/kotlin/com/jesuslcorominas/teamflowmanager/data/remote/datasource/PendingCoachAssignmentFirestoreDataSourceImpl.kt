package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import com.jesuslcorominas.teamflowmanager.data.core.datasource.PendingCoachAssignmentDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.PendingCoachAssignment
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.where

class PendingCoachAssignmentFirestoreDataSourceImpl(
    private val firestore: FirebaseFirestore,
) : PendingCoachAssignmentDataSource {
    companion object {
        private const val COLLECTION = "pendingCoachAssignments"
    }

    override suspend fun create(
        teamId: String,
        clubId: String,
        email: String,
    ) {
        firestore.collection(COLLECTION).document(teamId)
            .set(mapOf("teamId" to teamId, "clubId" to clubId, "email" to email))
    }

    override suspend fun delete(teamId: String) {
        firestore.collection(COLLECTION).document(teamId).delete()
    }

    override suspend fun getByEmail(email: String): List<PendingCoachAssignment> {
        val snapshot =
            firestore.collection(COLLECTION)
                .where { "email" equalTo email }
                .get()
        return snapshot.documents.mapNotNull { doc ->
            try {
                val data = doc.data<Map<String, String>>()
                val teamId = data["teamId"] ?: return@mapNotNull null
                val clubId = data["clubId"] ?: return@mapNotNull null
                val docEmail = data["email"] ?: return@mapNotNull null
                PendingCoachAssignment(teamId, clubId, docEmail)
            } catch (_: Exception) {
                null
            }
        }
    }
}
