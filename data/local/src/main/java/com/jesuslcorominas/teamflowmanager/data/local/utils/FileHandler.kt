package com.jesuslcorominas.teamflowmanager.data.local.utils

import java.io.InputStream
import java.io.OutputStream

/**
 * Android-specific interface for file operations.
 * Implementations handle platform-specific file access.
 */
interface FileHandler {
    /**
     * Creates an output stream for writing to a temporary export file.
     * @return OutputStream to write to, or null if creation fails
     */
    fun createExportOutputStream(): OutputStream?

    /**
     * Finalizes the export file and returns a shareable URI/path.
     * @return Platform-specific shareable file identifier (URI on Android)
     */
    fun finalizeExport(): String?

    /**
     * Opens an input stream from a file URI/path.
     * @param fileUri Platform-specific file identifier
     * @return InputStream to read from, or null if opening fails
     */
    fun openImportInputStream(fileUri: String): InputStream?
}
