package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.jesuslcorominas.teamflowmanager.domain.utils.TimeProvider
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

/**
 * Firestore-based implementation of TimeProvider.
 * Synchronizes time with Firestore server timestamp to avoid device time discrepancies.
 */
class FirestoreTimeProvider(
    private val firestore: FirebaseFirestore
) : TimeProvider {

    companion object {
        private const val TAG = "FirestoreTimeProvider"
        private const val SYNC_COLLECTION = "time_sync"
        private const val SYNC_DOCUMENT = "server_time_check"
        private const val TIMESTAMP_FIELD = "timestamp"
    }

    // Server time offset in milliseconds (server time - device time)
    @Volatile
    private var serverOffset: Long = 0L

    override suspend fun getCurrentTime(): Long {
        return System.currentTimeMillis() + serverOffset
    }

    override suspend fun synchronize() {
        try {
            Log.d(TAG, "synchronize: Starting time synchronization with Firestore")
            
            val docRef = firestore.collection(SYNC_COLLECTION).document(SYNC_DOCUMENT)
            
            // Write a document with server timestamp
            val writeTime = System.currentTimeMillis()
            docRef.set(mapOf(TIMESTAMP_FIELD to FieldValue.serverTimestamp())).await()
            
            // Read the document back to get the server timestamp
            val readTime = System.currentTimeMillis()
            val snapshot = docRef.get().await()
            val serverTimestamp = snapshot.getTimestamp(TIMESTAMP_FIELD)
            
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
