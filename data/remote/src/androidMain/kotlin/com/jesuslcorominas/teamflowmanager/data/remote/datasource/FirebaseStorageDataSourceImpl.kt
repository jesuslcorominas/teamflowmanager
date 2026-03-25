package com.jesuslcorominas.teamflowmanager.data.remote.datasource

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.jesuslcorominas.teamflowmanager.data.core.datasource.ImageStorageDataSource
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

/**
 * Firebase Storage implementation of ImageStorageDataSource.
 * Uploads player images to Firebase Storage and returns download URLs.
 */
class FirebaseStorageDataSourceImpl(
    private val firebaseStorage: FirebaseStorage,
) : ImageStorageDataSource {
    companion object {
        private const val TAG = "FirebaseStorageDS"
    }

    override suspend fun uploadImage(
        localUri: String,
        path: String,
    ): String? {
        return try {
            val uri = Uri.parse(localUri)
            val storageRef = firebaseStorage.reference.child(path)

            Log.d(TAG, "Uploading image to path: $path")

            // Upload the file
            storageRef.putFile(uri).await()

            // Get the download URL
            val downloadUrl = storageRef.downloadUrl.await().toString()

            Log.d(TAG, "Image uploaded successfully. Download URL: $downloadUrl")
            downloadUrl
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading image to Firebase Storage", e)
            null
        }
    }

    override suspend fun deleteImage(downloadUrl: String): Boolean {
        return try {
            // Extract the storage path from the download URL
            val storageRef = firebaseStorage.getReferenceFromUrl(downloadUrl)

            Log.d(TAG, "Deleting image: ${storageRef.path}")

            storageRef.delete().await()

            Log.d(TAG, "Image deleted successfully")
            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting image from Firebase Storage", e)
            false
        }
    }
}
