package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import com.jesuslcorominas.teamflowmanager.data.core.datasource.NotificationPreferencesDataSource
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.NotificationPreferencesFirestoreModel
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toDomain
import com.jesuslcorominas.teamflowmanager.domain.model.NotificationEventType
import com.jesuslcorominas.teamflowmanager.domain.model.UserNotificationPreferences
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.FirebaseFirestoreException
import dev.gitlive.firebase.firestore.FirestoreExceptionCode
import dev.gitlive.firebase.firestore.code
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class NotificationPreferencesFirestoreDataSourceImpl(
    private val firestore: FirebaseFirestore,
) : NotificationPreferencesDataSource {
    companion object {
        private const val PARENT_COLLECTION = "clubs"
        private const val COLLECTION = "notificationPreferences"
        private const val FIELD_MATCH_EVENTS = "matchEvents"
        private const val FIELD_GOALS = "goals"
        private const val FIELD_TEAMS = "teams"
    }

    private fun prefsDocument(
        clubId: String,
        userId: String,
    ) = firestore
        .collection(PARENT_COLLECTION)
        .document(clubId)
        .collection(COLLECTION)
        .document(userId)

    override fun getPreferences(
        userId: String,
        clubId: String,
    ): Flow<UserNotificationPreferences> =
        flow {
            emitAll(
                prefsDocument(clubId, userId).snapshots.map { doc ->
                    if (doc.exists) {
                        try {
                            doc.data<NotificationPreferencesFirestoreModel>().toDomain(userId)
                        } catch (_: Exception) {
                            UserNotificationPreferences(userId = userId)
                        }
                    } else {
                        UserNotificationPreferences(userId = userId)
                    }
                },
            )
        }.catch { e ->
            if (e is FirebaseFirestoreException) emit(UserNotificationPreferences(userId = userId)) else throw e
        }

    override suspend fun updateGlobalPreference(
        userId: String,
        clubId: String,
        type: NotificationEventType,
        enabled: Boolean,
    ) {
        val fieldName =
            when (type) {
                NotificationEventType.MATCH_EVENTS -> FIELD_MATCH_EVENTS
                NotificationEventType.GOALS -> FIELD_GOALS
            }
        prefsDocument(clubId, userId).set(mapOf(fieldName to enabled), merge = true)
    }

    override suspend fun updateTeamPreference(
        userId: String,
        clubId: String,
        teamRemoteId: String,
        type: NotificationEventType,
        enabled: Boolean,
    ) {
        val fieldName =
            when (type) {
                NotificationEventType.MATCH_EVENTS -> FIELD_MATCH_EVENTS
                NotificationEventType.GOALS -> FIELD_GOALS
            }
        try {
            prefsDocument(clubId, userId)
                .update("$FIELD_TEAMS.$teamRemoteId.$fieldName" to enabled)
        } catch (e: FirebaseFirestoreException) {
            if (e.code == FirestoreExceptionCode.NOT_FOUND) {
                val nested =
                    mapOf(
                        FIELD_TEAMS to
                            mapOf(
                                teamRemoteId to mapOf(fieldName to enabled),
                            ),
                    )
                prefsDocument(clubId, userId).set(nested, merge = true)
            } else {
                throw e
            }
        }
    }
}
