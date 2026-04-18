package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.jesuslcorominas.teamflowmanager.data.core.datasource.NotificationPreferencesDataSource
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.NotificationPreferencesFirestoreModel
import com.jesuslcorominas.teamflowmanager.data.remote.firestore.toDomain
import com.jesuslcorominas.teamflowmanager.domain.model.NotificationEventType
import com.jesuslcorominas.teamflowmanager.domain.model.UserNotificationPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class NotificationPreferencesFirestoreDataSourceImpl(
    private val firestore: FirebaseFirestore,
) : NotificationPreferencesDataSource {
    companion object {
        private const val TAG = "NotifPrefsDS"
        private const val COLLECTION = "notificationPreferences"
        private const val PARENT_COLLECTION = "clubs"
        private const val FIELD_MATCH_EVENTS = "matchEvents"
        private const val FIELD_GOALS = "goals"
        private const val FIELD_TEAMS = "teams"
    }

    private fun document(
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
        callbackFlow {
            val registration =
                document(clubId, userId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.e(TAG, "Error getting preferences for userId=$userId clubId=$clubId", error)
                            trySend(UserNotificationPreferences(userId = userId))
                            return@addSnapshotListener
                        }

                        if (snapshot == null || !snapshot.exists()) {
                            trySend(UserNotificationPreferences(userId = userId))
                            return@addSnapshotListener
                        }

                        val model = snapshot.toObject(NotificationPreferencesFirestoreModel::class.java)
                        if (model != null) {
                            trySend(model.toDomain(userId))
                        } else {
                            trySend(UserNotificationPreferences(userId = userId))
                        }
                    }

            awaitClose { registration.remove() }
        }

    override suspend fun updateGlobalPreference(
        userId: String,
        clubId: String,
        type: NotificationEventType,
        enabled: Boolean,
    ) {
        try {
            val fieldName =
                when (type) {
                    NotificationEventType.MATCH_EVENTS -> FIELD_MATCH_EVENTS
                    NotificationEventType.GOALS -> FIELD_GOALS
                }

            document(clubId, userId)
                .set(mapOf(fieldName to enabled), com.google.firebase.firestore.SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating global preference for userId=$userId clubId=$clubId", e)
            throw e
        }
    }

    override suspend fun updateTeamPreference(
        userId: String,
        clubId: String,
        teamRemoteId: String,
        type: NotificationEventType,
        enabled: Boolean,
    ) {
        try {
            val fieldName =
                when (type) {
                    NotificationEventType.MATCH_EVENTS -> FIELD_MATCH_EVENTS
                    NotificationEventType.GOALS -> FIELD_GOALS
                }

            val updates =
                mapOf<String, Any>(
                    "$FIELD_TEAMS.$teamRemoteId.$fieldName" to enabled,
                )

            document(clubId, userId)
                .set(updates, com.google.firebase.firestore.SetOptions.merge())
                .await()

            Log.d(TAG, "Updated team $fieldName=$enabled for teamId=$teamRemoteId userId=$userId clubId=$clubId")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating team preference for teamId=$teamRemoteId userId=$userId clubId=$clubId", e)
            throw e
        }
    }
}
