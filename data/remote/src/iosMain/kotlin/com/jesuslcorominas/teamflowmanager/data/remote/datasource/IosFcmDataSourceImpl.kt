package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import com.jesuslcorominas.teamflowmanager.data.core.datasource.FcmDataSource
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.FcmTokenFirestoreModel
import com.jesuslcorominas.teamflowmanager.domain.model.FcmTokenEntry
import com.jesuslcorominas.teamflowmanager.domain.model.NotificationPayload
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.where
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.content.TextContent
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import platform.Foundation.NSDate
import platform.Foundation.NSLog
import platform.Foundation.timeIntervalSince1970

/**
 * iOS counterpart of `AndroidFcmDataSourceImpl`: same `fcmTokens` documents and the
 * same `sendNotification` Cloud Function. Ktorfit is generated for Android only, so
 * the function is called through the plain Ktor client, like the account deletion.
 */
internal class IosFcmDataSourceImpl(
    private val firestore: FirebaseFirestore,
    private val httpClient: HttpClient,
    private val functionsBaseUrl: String,
) : FcmDataSource {
    override suspend fun saveToken(
        userId: String,
        token: String,
        platform: String,
        topic: String?,
    ) {
        firestore.collection(COLLECTION).document(docId(userId, token)).set(
            FcmTokenFirestoreModel(
                userId = userId,
                token = token,
                platform = platform,
                topic = topic,
                updatedAt = (NSDate().timeIntervalSince1970 * MILLIS_PER_SECOND).toLong(),
            ),
        )
    }

    override suspend fun deleteToken(
        userId: String,
        token: String,
    ) {
        firestore.collection(COLLECTION).document(docId(userId, token)).delete()
    }

    override suspend fun getTokenEntry(
        userId: String,
        token: String,
    ): FcmTokenEntry? {
        val snapshot = firestore.collection(COLLECTION).document(docId(userId, token)).get()
        if (!snapshot.exists) return null
        val model = snapshot.toModelOrNull()
        return FcmTokenEntry(
            docId = snapshot.id,
            userId = model?.userId?.ifEmpty { null } ?: userId,
            topic = model?.topic,
        )
    }

    override suspend fun findTokensForOtherUsers(
        token: String,
        currentUserId: String,
    ): List<FcmTokenEntry> =
        firestore.collection(COLLECTION)
            .where { FIELD_TOKEN equalTo token }
            .get()
            .documents
            .mapNotNull { doc -> doc.toModelOrNull()?.let { doc.id to it } }
            .filter { (_, model) -> model.userId != currentUserId }
            .map { (docId, model) -> FcmTokenEntry(docId = docId, userId = model.userId, topic = model.topic) }

    override suspend fun getTokensByUserId(userId: String): List<String> =
        firestore.collection(COLLECTION)
            .where { FIELD_USER_ID equalTo userId }
            .get()
            .documents
            .mapNotNull { it.toModelOrNull()?.token?.ifEmpty { null } }

    override suspend fun sendNotification(
        token: String,
        payload: NotificationPayload,
    ) {
        val request =
            when (payload) {
                is NotificationPayload.FreeText ->
                    SendNotificationBody(token = token, title = payload.title, body = payload.body)
                is NotificationPayload.Typed ->
                    SendNotificationBody(
                        token = token,
                        type = payload.type.key,
                        params = payload.params.ifEmpty { null },
                    )
            }
        val response =
            httpClient.post("${functionsBaseUrl.trimEnd('/')}/$SEND_NOTIFICATION_PATH") {
                setBody(TextContent(json.encodeToString(SendNotificationBody.serializer(), request), ContentType.Application.Json))
            }
        // Like the Android Ktorfit call, a non-2xx answer does not throw: the caller sends to
        // every token of a user in a loop and one stale token must not stop the rest.
        if (!response.status.isSuccess()) {
            NSLog("TFMPush sendNotification returned ${response.status.value}".replace("%", "%%"))
        }
    }

    private fun DocumentSnapshot.toModelOrNull(): FcmTokenFirestoreModel? =
        try {
            data<FcmTokenFirestoreModel>()
        } catch (_: Exception) {
            null
        }

    private fun docId(
        userId: String,
        token: String,
    ) = "${userId}_${token.takeLast(16)}"

    @Serializable
    private data class SendNotificationBody(
        val token: String,
        val title: String? = null,
        val body: String? = null,
        val type: String? = null,
        val params: Map<String, String>? = null,
    )

    private companion object {
        const val COLLECTION = "fcmTokens"
        const val FIELD_TOKEN = "token"
        const val FIELD_USER_ID = "userId"
        const val SEND_NOTIFICATION_PATH = "sendNotification"
        const val MILLIS_PER_SECOND = 1000.0
        val json = Json { explicitNulls = false }
    }
}
