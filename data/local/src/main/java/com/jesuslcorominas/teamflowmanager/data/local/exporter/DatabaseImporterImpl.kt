package com.jesuslcorominas.teamflowmanager.data.local.exporter

import com.jesuslcorominas.teamflowmanager.data.local.database.TeamFlowManagerDatabase
import com.jesuslcorominas.teamflowmanager.domain.utils.DatabaseImporter
import com.jesuslcorominas.teamflowmanager.domain.utils.FileHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class DatabaseImporterImpl(
    private val fileHandler: FileHandler,
    private val database: TeamFlowManagerDatabase
) : DatabaseImporter {

    override suspend fun importDatabase(fileUri: String): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                fileHandler.openImportInputStream(fileUri)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        // Clear existing data (in reverse order to respect foreign keys)
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
                        
                        // Manually invalidate all tables to trigger Room's Flow observers
                        // This ensures UI updates after raw SQL inserts
                        database.invalidationTracker.notifyObserversByTableNames(
                            "team",
                            "players", 
                            "match",
                            "player_time",
                            "player_time_history",
                            "player_substitution",
                            "goal"
                        )
                    }
                }
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
