package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import com.jesuslcorominas.teamflowmanager.data.core.datasource.PresidentNotificationDataSource
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.PresidentNotificationFirestoreModel
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toDomain
import com.jesuslcorominas.teamflowmanager.domain.model.PresidentNotification
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.FirebaseFirestoreException
import dev.gitlive.firebase.firestore.where
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class PresidentNotificationFirestoreDataSourceImpl(
    private val firestore: FirebaseFirestore,
) : PresidentNotificationDataSource {
    companion object {
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
        flow {
            emitAll(
                notificationsCollection(clubId)
                    .orderBy(FIELD_CREATED_AT, Direction.DESCENDING)
                    .snapshots
                    .map { qs ->
                        qs.documents.mapNotNull { doc ->
                            try {
                                doc.data<PresidentNotificationFirestoreModel>().copy(id = doc.id).toDomain()
                            } catch (_: Exception) {
                                null
                            }
                        }
                    },
            )
        }.catch { e ->
            if (e is FirebaseFirestoreException) emit(emptyList()) else throw e
        }

    override fun getUnreadCount(clubId: String): Flow<Int> =
        flow {
            emitAll(
                notificationsCollection(clubId)
                    .where { FIELD_READ equalTo false }
                    .snapshots
                    .map { qs -> qs.documents.size },
            )
        }.catch { e ->
            if (e is FirebaseFirestoreException) emit(0) else throw e
        }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun createNotification(
        clubId: String,
        notification: PresidentNotification,
    ) {
        val docId = if (notification.id.isNotEmpty()) notification.id else Uuid.random().toString()
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
    }

    override suspend fun markAsRead(
        clubId: String,
        notificationId: String,
    ) {
        notificationsCollection(clubId)
            .document(notificationId)
            .update(FIELD_READ to true)
    }

    override suspend fun markAsUnread(
        clubId: String,
        notificationId: String,
    ) {
        notificationsCollection(clubId)
            .document(notificationId)
            .update(FIELD_READ to false)
    }

    override suspend fun deleteNotification(
        clubId: String,
        notificationId: String,
    ) {
        notificationsCollection(clubId)
            .document(notificationId)
            .delete()
    }
}
