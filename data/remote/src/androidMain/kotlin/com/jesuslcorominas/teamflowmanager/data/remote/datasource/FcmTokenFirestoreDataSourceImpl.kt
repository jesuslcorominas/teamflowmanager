package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import com.google.firebase.firestore.FirebaseFirestore
import com.jesuslcorominas.teamflowmanager.data.core.datasource.FcmTokenDataSource
import kotlinx.coroutines.tasks.await

class FcmTokenFirestoreDataSourceImpl(
    private val firestore: FirebaseFirestore,
) : FcmTokenDataSource {

    override suspend fun saveToken(userId: String, token: String, platform: String) {
        val docId = "${userId}_${token.takeLast(16)}"
        firestore.collection(COLLECTION).document(docId).set(
            mapOf(
                "userId" to userId,
                "token" to token,
                "platform" to platform,
                "updatedAt" to System.currentTimeMillis(),
            )
        ).await()
    }

    override suspend fun deleteToken(userId: String, token: String) {
        val docId = "${userId}_${token.takeLast(16)}"
        firestore.collection(COLLECTION).document(docId).delete().await()
    }

    companion object {
        private const val COLLECTION = "fcmTokens"
    }
}
