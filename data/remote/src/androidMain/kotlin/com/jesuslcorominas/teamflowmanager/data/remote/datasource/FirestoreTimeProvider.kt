package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.jesuslcorominas.teamflowmanager.domain.utils.TimeProvider
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

/**
 * Firestore-based implementation of TimeProvider.
 * Synchronizes time with Firestore server timestamp to avoid device time discrepancies.
 *
 * Uses the user's team document to store time sync data, ensuring proper permissions.
 */
class FirestoreTimeProvider(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : TimeProvider {

    companion object {
        private const val TAG = "FirestoreTimeProvider"
        private const val TEAMS_COLLECTION = "teams"
        private const val TIME_SYNC_FIELD = "lastTimeSync"
    }

    // Server time offset in milliseconds (server time - device time)
    @Volatile
    private var serverOffset: Long = 0L

    override fun getCurrentTime(): Long {
        return System.currentTimeMillis() + serverOffset
    }

    override suspend fun synchronize() {
        try {
            Log.d(TAG, "synchronize: Starting time synchronization with Firestore")

            val currentUserId = firebaseAuth.currentUser?.uid
            if (currentUserId == null) {
                Log.w(TAG, "synchronize: No authenticated user, skipping time sync")
                return
            }

            // Find the user's team document
            val teamQuery = firestore.collection(TEAMS_COLLECTION)
                .whereEqualTo("assignedCoachId", currentUserId)
                .limit(1)
                .get()
                .await()

            val teamDoc = teamQuery.documents.firstOrNull()
            if (teamDoc == null) {
                Log.w(TAG, "synchronize: No team found for user, skipping time sync")
                return
            }

            val docRef = teamDoc.reference

            // Write a field with server timestamp
            val writeTime = System.currentTimeMillis()
            docRef.update(TIME_SYNC_FIELD, FieldValue.serverTimestamp()).await()
            // Read the document back to get the server timestamp
            val readTime = System.currentTimeMillis()
            val snapshot = docRef.get().await()
            val serverTimestamp = snapshot.getTimestamp(TIME_SYNC_FIELD)
            if (serverTimestamp != null) {
                val serverTimeMillis = serverTimestamp.toDate().time

                // Calculate offset considering the round-trip time
                // The server timestamp was recorded when the write occurred
                // We estimate that occurred at writeTime + (roundTripTime / 2)
                val roundTripTime = readTime - writeTime
                val estimatedWriteTime = writeTime + (roundTripTime / 2)

                serverOffset = serverTimeMillis - estimatedWriteTime

                Log.d(TAG, "synchronize: Time sync complete - offset: $serverOffset ms, round-trip: $roundTripTime ms")
            } else {
                Log.w(TAG, "synchronize: Server timestamp is null")
            }
        } catch (e: CancellationException) {
            Log.w(TAG, "synchronize: Time synchronization was cancelled")
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "synchronize: Error synchronizing time with server", e)
            // Do not throw - keep using existing offset or 0 if first sync fails
        }
    }

    override fun getOffset(): Long = serverOffset
}
