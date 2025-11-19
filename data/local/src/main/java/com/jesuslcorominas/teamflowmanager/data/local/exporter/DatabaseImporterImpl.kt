package com.jesuslcorominas.teamflowmanager.data.local.exporter

import com.jesuslcorominas.teamflowmanager.data.local.database.TeamFlowManagerDatabase
import com.jesuslcorominas.teamflowmanager.domain.utils.DatabaseImporter
import com.jesuslcorominas.teamflowmanager.domain.utils.FileHandler
import com.jesuslcorominas.teamflowmanager.domain.utils.TransactionRunner
import java.io.BufferedReader
import java.io.InputStreamReader

class DatabaseImporterImpl(
    private val fileHandler: FileHandler,
    private val database: TeamFlowManagerDatabase,
    private val transactionRunner: TransactionRunner
) : DatabaseImporter {

    override suspend fun importDatabase(fileUri: String): Boolean {
        return try {
            // Run the entire import operation as a single transaction
            // This ensures atomicity - either all data is imported or none is
            transactionRunner.run {
                fileHandler.openImportInputStream(fileUri)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        // Clear existing data first (within transaction)
                        database.clearAllTables()

                        // Read and execute SQL statements
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            line?.let { sql ->
                                if (sql.isNotBlank() && sql.trim().startsWith("INSERT", ignoreCase = true)) {
                                    // Execute the raw SQL statement
                                    database.openHelper.writableDatabase.execSQL(sql.trim())
                                }
                            }
                        }
                    }
                } ?: throw IllegalStateException("Could not open file for import")
                
                // Get all table names dynamically for invalidation
                val tableNames = getTableNames()
                
                // Manually invalidate all tables to trigger Room's Flow observers
                // This ensures UI updates after raw SQL inserts
                database.invalidationTracker.notifyObserversByTableNames(*tableNames.toTypedArray())
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Get all table names from the database except system tables
     */
    private fun getTableNames(): List<String> {
        val tableNames = mutableListOf<String>()
        database.openHelper.readableDatabase.query(
            "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' AND name NOT LIKE 'android_%' AND name NOT LIKE 'room_%' ORDER BY name"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                tableNames.add(cursor.getString(0))
            }
        }
        return tableNames
    }
}
