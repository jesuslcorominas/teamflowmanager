package com.jesuslcorominas.teamflowmanager.data.core.datasource

/**
 * Data source for image storage operations.
 * This interface abstracts the storage mechanism for images (e.g., Firebase Storage).
 */
interface ImageStorageDataSource {
    /**
     * Uploads an image from a local URI and returns the download URL.
     * @param localUri The local URI of the image (content:// or file://)
     * @param path The storage path where the image should be stored (e.g., "players/{playerId}")
     * @return The download URL of the uploaded image, or null if upload failed
     */
    suspend fun uploadImage(
        localUri: String,
        path: String,
    ): String?

    /**
     * Deletes an image from storage.
     * @param downloadUrl The download URL of the image to delete
     * @return true if deletion was successful, false otherwise
     */
    suspend fun deleteImage(downloadUrl: String): Boolean
}
