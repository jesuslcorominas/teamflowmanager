package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import com.jesuslcorominas.teamflowmanager.data.core.datasource.PendingCoachAssignmentDataSource
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toPendingCoachAssignment
import com.jesuslcorominas.teamflowmanager.domain.model.PendingCoachAssignment
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.where

class PendingCoachAssignmentFirestoreDataSourceImpl(
    private val firestore: FirebaseFirestore,
) : PendingCoachAssignmentDataSource {
    companion object {
        private const val COLLECTION = "pendingCoachAssignments"
        private const val FIELD_TEAM_ID = "teamId"
        private const val FIELD_CLUB_ID = "clubId"
        private const val FIELD_EMAIL = "email"
    }

    override suspend fun create(
        teamId: String,
        clubId: String,
        email: String,
    ) {
        firestore.collection(COLLECTION).document(teamId)
            .set(mapOf(FIELD_TEAM_ID to teamId, FIELD_CLUB_ID to clubId, FIELD_EMAIL to email))
    }

    override suspend fun delete(teamId: String) {
        firestore.collection(COLLECTION).document(teamId).delete()
    }

    override suspend fun getByEmail(email: String): List<PendingCoachAssignment> {
        val snapshot =
            firestore.collection(COLLECTION)
                .where { FIELD_EMAIL equalTo email }
                .get()
        return snapshot.documents.mapNotNull { doc ->
            try {
                doc.data<Map<String, String>>()?.toPendingCoachAssignment()
            } catch (_: Exception) {
                null
            }
        }
    }
}
