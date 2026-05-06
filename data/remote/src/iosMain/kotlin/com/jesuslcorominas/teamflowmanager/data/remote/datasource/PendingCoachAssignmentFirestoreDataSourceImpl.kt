package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import com.jesuslcorominas.teamflowmanager.data.core.datasource.PendingCoachAssignmentDataSource
import com.jesuslcorominas.teamflowmanager.domain.model.PendingCoachAssignment
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.where
import kotlin.coroutines.cancellation.CancellationException

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
        try {
            firestore.collection(COLLECTION).document(teamId)
                .set(mapOf("teamId" to teamId, "clubId" to clubId, "email" to email))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun delete(teamId: String) {
        try {
            firestore.collection(COLLECTION).document(teamId).delete()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun getByEmail(email: String): List<PendingCoachAssignment> {
        return try {
            val snapshot =
                firestore.collection(COLLECTION)
                    .where { "email" equalTo email }
                    .get()
            snapshot.documents.mapNotNull { doc ->
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
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw e
        }
    }
}
