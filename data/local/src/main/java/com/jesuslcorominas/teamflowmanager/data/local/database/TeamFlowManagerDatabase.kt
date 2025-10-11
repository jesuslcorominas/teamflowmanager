package com.jesuslcorominas.teamflowmanager.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jesuslcorominas.teamflowmanager.data.local.dao.MatchDao
import com.jesuslcorominas.teamflowmanager.data.local.dao.PlayerDao
import com.jesuslcorominas.teamflowmanager.data.local.dao.PlayerTimeDao
import com.jesuslcorominas.teamflowmanager.data.local.dao.PlayerTimeHistoryDao
import com.jesuslcorominas.teamflowmanager.data.local.dao.TeamDao
import com.jesuslcorominas.teamflowmanager.data.local.entity.MatchEntity
import com.jesuslcorominas.teamflowmanager.data.local.entity.PlayerEntity
import com.jesuslcorominas.teamflowmanager.data.local.entity.PlayerTimeEntity
import com.jesuslcorominas.teamflowmanager.data.local.entity.PlayerTimeHistoryEntity
import com.jesuslcorominas.teamflowmanager.data.local.entity.TeamEntity

@Database(
    entities = [PlayerEntity::class, TeamEntity::class, MatchEntity::class, PlayerTimeEntity::class, PlayerTimeHistoryEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class TeamFlowManagerDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao

    abstract fun teamDao(): TeamDao

    abstract fun matchDao(): MatchDao

    abstract fun playerTimeDao(): PlayerTimeDao

    abstract fun playerTimeHistoryDao(): PlayerTimeHistoryDao
}

val MIGRATION_1_2 =
    object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Create new match table with updated schema
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS match_new (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    teamId INTEGER NOT NULL DEFAULT 1,
                    opponent TEXT,
                    location TEXT,
                    date INTEGER,
                    startingLineupIds TEXT NOT NULL DEFAULT '',
                    substituteIds TEXT NOT NULL DEFAULT '',
                    elapsedTimeMillis INTEGER NOT NULL,
                    isRunning INTEGER NOT NULL,
                    lastStartTimeMillis INTEGER
                )
                """.trimIndent(),
            )

            // Copy data from old table to new table
            db.execSQL(
                """
                INSERT INTO match_new (id, teamId, elapsedTimeMillis, isRunning, lastStartTimeMillis)
                SELECT id, 1, elapsedTimeMillis, isRunning, lastStartTimeMillis FROM match
                """.trimIndent(),
            )

            // Drop old table
            db.execSQL("DROP TABLE match")

            // Rename new table to match
            db.execSQL("ALTER TABLE match_new RENAME TO match")
        }
    }

