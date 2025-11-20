package com.jesuslcorominas.teamflowmanager.ui.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.jesuslcorominas.teamflowmanager.domain.utils.FileHandler
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

/**
 * Android-specific implementation of FileHandler.
 * Handles file operations using Android Context, FileProvider, and ContentResolver.
 */
class AndroidFileHandler(
    private val context: Context
) : FileHandler {

    private var currentExportFile: File? = null

    override fun createExportOutputStream(): OutputStream? {
        return try {
            val fileName = "teamflowmanager_backup_${System.currentTimeMillis()}.tfm"
            val file = File(context.cacheDir, fileName)
            currentExportFile = file
            FileOutputStream(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun finalizeExport(): String? {
        return try {
            currentExportFile?.let { file ->
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                uri.toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            currentExportFile = null
        }
    }

    override fun openImportInputStream(fileUri: String): InputStream? {
        return try {
            val uri = Uri.parse(fileUri)
            context.contentResolver.openInputStream(uri)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
