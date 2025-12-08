package com.jesuslcorominas.teamflowmanager.data.local.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration from version 2 to 3
 * Removes the deprecated elapsedTimeMillis column from the match table.
 * This field was never used in the domain model - elapsed time is calculated from periods.
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // SQLite doesn't support dropping columns directly in older versions
        // We need to: create new table, copy data, drop old table, rename new table

        // 1. Create new match table without elapsedTimeMillis
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS match_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                teamId INTEGER NOT NULL,
                teamName TEXT NOT NULL,
                opponent TEXT NOT NULL,
                location TEXT NOT NULL,
                dateTime INTEGER,
                numberOfPeriods INTEGER NOT NULL,
                squadCallUpIds TEXT NOT NULL,
                captainId INTEGER NOT NULL,
                startingLineupIds TEXT NOT NULL,
                lastStartTimeMillis INTEGER,
                status TEXT NOT NULL,
                archived INTEGER NOT NULL,
                currentPeriod INTEGER NOT NULL,
                pauseCount INTEGER NOT NULL,
                goals INTEGER NOT NULL,
                opponentGoals INTEGER NOT NULL,
                timeoutStartTimeMillis INTEGER NOT NULL,
                periods TEXT NOT NULL,
                periodType INTEGER NOT NULL
            )
        """.trimIndent())

        // 2. Copy data from old table to new table (excluding elapsedTimeMillis)
        db.execSQL("""
            INSERT INTO match_new (
                id, teamId, teamName, opponent, location, dateTime, numberOfPeriods,
                squadCallUpIds, captainId, startingLineupIds, lastStartTimeMillis,
                status, archived, currentPeriod, pauseCount, goals, opponentGoals,
                timeoutStartTimeMillis, periods, periodType
            )
            SELECT
                id, teamId, teamName, opponent, location, dateTime, numberOfPeriods,
                squadCallUpIds, captainId, startingLineupIds, lastStartTimeMillis,
                status, archived, currentPeriod, pauseCount, goals, opponentGoals,
                timeoutStartTimeMillis, periods, periodType
            FROM match
        """.trimIndent())

        // 3. Drop old table
        db.execSQL("DROP TABLE match")

        // 4. Rename new table to match
        db.execSQL("ALTER TABLE match_new RENAME TO match")
    }
}

/**
 * Migration from version 3 to 4
 * Adds teamType column to team table to support different football formats (5, 7, 8, 11 players).
 * Default value is 5 for new teams.
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add teamType column with default value of 5 (FOOTBALL_5)
        db.execSQL("ALTER TABLE team ADD COLUMN teamType INTEGER NOT NULL DEFAULT 5")
    }
}

/**
 * Migration from version 4 to 5
 * Adds coachId column to team table to support Firestore integration.
 * The coachId links the team to a Firebase user.
 */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add coachId column as nullable (existing teams won't have a coachId)
        db.execSQL("ALTER TABLE team ADD COLUMN coachId TEXT")
    }
}
