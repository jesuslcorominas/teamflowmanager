package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PresidentNotificationDataSource
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.PresidentNotificationFirestoreModel
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toDomain
import com.jesuslcorominas.teamflowmanager.domain.model.PresidentNotification
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

@Suppress("TooManyFunctions")
class PresidentNotificationFirestoreDataSourceImpl(
    private val firestore: FirebaseFirestore,
) : PresidentNotificationDataSource {
    companion object {
        private const val TAG = "PresidentNotifDS"
        private const val PARENT_COLLECTION = "presidentNotifications"
        private const val NOTIFICATIONS_COLLECTION = "notifications"
        private const val FIELD_CREATED_AT = "createdAt"
        private const val FIELD_READ = "read"
    }

    private fun notificationsCollection(clubId: String) =
        firestore
            .collection(PARENT_COLLECTION)
            .document(clubId)
            .collection(NOTIFICATIONS_COLLECTION)

    override fun getNotifications(clubId: String): Flow<List<PresidentNotification>> =
        callbackFlow {
            val registration =
                notificationsCollection(clubId)
                    .orderBy(FIELD_CREATED_AT, com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.e(TAG, "Error getting notifications for club=$clubId", error)
                            trySend(emptyList())
                            return@addSnapshotListener
                        }

                        if (snapshot == null || snapshot.isEmpty) {
                            trySend(emptyList())
                            return@addSnapshotListener
                        }

                        val notifications =
                            snapshot.documents.mapNotNull { doc ->
                                val model = doc.toObject(PresidentNotificationFirestoreModel::class.java)
                                model?.let {
                                    val withId = if (it.id.isEmpty()) it.copy(id = doc.id) else it
                                    withId.toDomain()
                                }
                            }

                        Log.d(TAG, "Loaded ${notifications.size} notifications for club=$clubId")
                        trySend(notifications)
                    }

            awaitClose { registration.remove() }
        }

    override fun getUnreadCount(clubId: String): Flow<Int> =
        callbackFlow {
            val registration =
                notificationsCollection(clubId)
                    .whereEqualTo(FIELD_READ, false)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.e(TAG, "Error getting unread count for club=$clubId", error)
                            trySend(0)
                            return@addSnapshotListener
                        }

                        val count = snapshot?.size() ?: 0
                        Log.d(TAG, "Unread notification count for club=$clubId: $count")
                        trySend(count)
                    }

            awaitClose { registration.remove() }
        }

    override suspend fun createNotification(
        clubId: String,
        notification: PresidentNotification,
    ) {
        try {
            val docId = if (notification.id.isNotEmpty()) notification.id else UUID.randomUUID().toString()
            val model =
                PresidentNotificationFirestoreModel(
                    id = docId,
                    type = notification.type.key,
                    title = notification.title,
                    body = notification.body,
                    userData = notification.userData,
                    createdAt = notification.createdAt,
                    read = notification.read,
                )
            notificationsCollection(clubId)
                .document(docId)
                .set(model)
                .await()
            Log.d(TAG, "Created notification=$docId in club=$clubId")
        } catch (e: Exception) {
            Log.e(TAG, "Error creating notification in club=$clubId", e)
            throw e
        }
    }

    override suspend fun markAsRead(
        clubId: String,
        notificationId: String,
    ) {
        try {
            notificationsCollection(clubId)
                .document(notificationId)
                .update(FIELD_READ, true)
                .await()
            Log.d(TAG, "Marked notification=$notificationId as read in club=$clubId")
        } catch (e: Exception) {
            Log.e(TAG, "Error marking notification=$notificationId as read", e)
            throw e
        }
    }

    override suspend fun markAsUnread(
        clubId: String,
        notificationId: String,
    ) {
        try {
            notificationsCollection(clubId)
                .document(notificationId)
                .update(FIELD_READ, false)
                .await()
            Log.d(TAG, "Marked notification=$notificationId as unread in club=$clubId")
        } catch (e: Exception) {
            Log.e(TAG, "Error marking notification=$notificationId as unread", e)
            throw e
        }
    }

    override suspend fun deleteNotification(
        clubId: String,
        notificationId: String,
    ) {
        try {
            notificationsCollection(clubId)
                .document(notificationId)
                .delete()
                .await()
            Log.d(TAG, "Deleted notification=$notificationId from club=$clubId")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting notification=$notificationId", e)
            throw e
        }
    }
}
