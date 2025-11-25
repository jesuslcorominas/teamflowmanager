package com.jesuslcorominas.teamflowmanager.domain.utils

/**
 * Platform-agnostic interface for file operations.
 * Implementations handle platform-specific file access (Android, iOS, etc.)
 *
 * Note: This interface uses ByteArray instead of InputStream/OutputStream
 * to maintain multiplatform compatibility.
 */
interface FileHandler {
    /**
     * Creates an export file and returns its content as bytes.
     * @param content The content to write to the export file
     * @return True if export was successful, false otherwise
     */
    fun writeExportFile(content: ByteArray): Boolean

    /**
     * Finalizes the export file and returns a shareable URI/path.
     * @return Platform-specific shareable file identifier (URI on Android, path on iOS)
     */
    fun finalizeExport(): String?

    /**
     * Reads content from a file URI/path.
     * @param fileUri Platform-specific file identifier
     * @return File content as ByteArray, or null if reading fails
     */
    fun readImportFile(fileUri: String): ByteArray?
}
