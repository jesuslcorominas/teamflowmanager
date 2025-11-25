package com.jesuslcorominas.teamflowmanager.data.local.exporter

import androidx.sqlite.db.SimpleSQLiteQuery
import com.jesuslcorominas.teamflowmanager.data.local.database.TeamFlowManagerDatabase
import com.jesuslcorominas.teamflowmanager.data.local.utils.FileHandler
import com.jesuslcorominas.teamflowmanager.domain.utils.DatabaseExporter
import java.io.OutputStreamWriter

class DatabaseExporterImpl(
    private val fileHandler: FileHandler,
    private val database: TeamFlowManagerDatabase
) : DatabaseExporter {

    override suspend fun exportDatabase(): String? {
        return try {
            fileHandler.createExportOutputStream()?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    // Get all table names from the database dynamically
                    val tableNames = getTableNames()
                    
                    // Export each table
                    tableNames.forEach { tableName ->
                        exportTable(tableName, writer)
                    }
                }
            }

            fileHandler.finalizeExport()
        } catch (e: Exception) {
            e.printStackTrace()
            null
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

    /**
     * Export a single table by dynamically generating INSERT statements
     */
    private fun exportTable(tableName: String, writer: OutputStreamWriter) {
        // Query all rows from the table
        database.openHelper.readableDatabase.query(
            SimpleSQLiteQuery("SELECT * FROM `$tableName`")
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                val columnNames = cursor.columnNames
                
                do {
                    // Build INSERT statement for each row
                    val values = mutableListOf<String>()
                    
                    for (i in columnNames.indices) {
                        val value = when (cursor.getType(i)) {
                            android.database.Cursor.FIELD_TYPE_NULL -> "NULL"
                            android.database.Cursor.FIELD_TYPE_INTEGER -> cursor.getLong(i).toString()
                            android.database.Cursor.FIELD_TYPE_FLOAT -> cursor.getDouble(i).toString()
                            android.database.Cursor.FIELD_TYPE_STRING -> "'${escapeSql(cursor.getString(i))}'"
                            android.database.Cursor.FIELD_TYPE_BLOB -> {
                                // Handle BLOB as hex string
                                val blob = cursor.getBlob(i)
                                "X'${blob.joinToString("") { "%02X".format(it) }}'"
                            }
                            else -> "NULL"
                        }
                        values.add(value)
                    }
                    
                    // Write INSERT statement
                    writer.write(
                        "INSERT INTO `$tableName` (${columnNames.joinToString(", ") { "`$it`" }}) " +
                                "VALUES (${values.joinToString(", ")});\n"
                    )
                } while (cursor.moveToNext())
            }
        }
    }

    private fun escapeSql(value: String): String {
        return value.replace("'", "''")
    }
}
