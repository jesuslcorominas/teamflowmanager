package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import com.google.firebase.firestore.FirebaseFirestore
import com.jesuslcorominas.teamflowmanager.data.core.datasource.FcmDataSource
import com.jesuslcorominas.teamflowmanager.data.remote.api.FcmNotificationApi
import com.jesuslcorominas.teamflowmanager.data.remote.api.model.SendNotificationRequest
import com.jesuslcorominas.teamflowmanager.domain.model.FcmTokenEntry
import kotlinx.coroutines.tasks.await

internal class AndroidFcmDataSourceImpl(
    private val firestore: FirebaseFirestore,
    private val fcmNotificationApi: FcmNotificationApi,
) : FcmDataSource {
    override suspend fun saveToken(
        userId: String,
        token: String,
        platform: String,
        topic: String?,
    ) {
        val docId = docId(userId, token)
        firestore.collection(COLLECTION).document(docId).set(
            mapOf(
                "userId" to userId,
                "token" to token,
                "platform" to platform,
                "topic" to topic,
                "updatedAt" to System.currentTimeMillis(),
            ),
        ).await()
    }

    override suspend fun deleteToken(
        userId: String,
        token: String,
    ) {
        firestore.collection(COLLECTION).document(docId(userId, token)).delete().await()
    }

    override suspend fun getTokenEntry(
        userId: String,
        token: String,
    ): FcmTokenEntry? {
        val snapshot = firestore.collection(COLLECTION).document(docId(userId, token)).get().await()
        return if (snapshot.exists()) {
            FcmTokenEntry(
                docId = snapshot.id,
                userId = snapshot.getString("userId") ?: userId,
                topic = snapshot.getString("topic"),
            )
        } else {
            null
        }
    }

    override suspend fun findTokensForOtherUsers(
        token: String,
        currentUserId: String,
    ): List<FcmTokenEntry> {
        val snapshot =
            firestore.collection(COLLECTION)
                .whereEqualTo("token", token)
                .get()
                .await()
        return snapshot.documents
            .filter { it.getString("userId") != currentUserId }
            .map { doc ->
                FcmTokenEntry(
                    docId = doc.id,
                    userId = doc.getString("userId") ?: "",
                    topic = doc.getString("topic"),
                )
            }
    }

    override suspend fun getTokensByUserId(userId: String): List<String> {
        val snapshot =
            firestore.collection(COLLECTION)
                .whereEqualTo("userId", userId)
                .get()
                .await()
        return snapshot.documents.mapNotNull { it.getString("token") }
    }

    override suspend fun sendNotification(
        token: String,
        title: String,
        body: String,
    ) {
        fcmNotificationApi.sendNotification(SendNotificationRequest(token = token, title = title, body = body))
    }

    private fun docId(
        userId: String,
        token: String,
    ) = "${userId}_${token.takeLast(16)}"

    companion object {
        private const val COLLECTION = "fcmTokens"
    }
}
