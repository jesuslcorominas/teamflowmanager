# Logical Deletion Implementation Summary

## Overview
Implemented logical deletion (soft delete) for the Player entity to preserve historical data when players are deleted from the app.

## Problem Statement
The Player entity could be deleted from the app, but this deletion was physical, removing all player data from the database. This caused loss of historical information including goals and playing time statistics associated with the deleted player.

## Solution
Changed from physical deletion to logical deletion by adding a `deleted` flag to the Player entity. When a player is "deleted", they are marked as deleted but remain in the database, preserving all historical data.

## Changes Made

### 1. Domain Layer
**File:** `domain/src/main/kotlin/com/jesuslcorominas/teamflowmanager/domain/model/Player.kt`
- Added `deleted: Boolean = false` field to the Player data class

### 2. Data Layer - Room (Local Database)

**File:** `data/local/src/main/java/com/jesuslcorominas/teamflowmanager/data/local/entity/PlayerEntity.kt`
- Added `deleted: Boolean = false` field to PlayerEntity
- Updated `toDomain()` and `toEntity()` mapper functions to include deleted field

**File:** `data/local/src/main/java/com/jesuslcorominas/teamflowmanager/data/local/dao/PlayerDao.kt`
- Updated all queries to filter out deleted players using `WHERE deleted = 0`
  - `getAllPlayers()`
  - `getAllPlayersDirect()`
  - `getCaptainPlayer()`
  - `getPlayerById()`
  - `clearAllCaptains()`
- Changed `deletePlayer()` from physical DELETE to logical update: `UPDATE players SET deleted = 1`
- Added documentation to `deleteAllPlayers()` clarifying it's for migration/data clearing only

**File:** `data/local/src/main/java/com/jesuslcorominas/teamflowmanager/data/local/database/TeamFlowManagerDatabase.kt`
- Incremented database version from 5 to 6

**File:** `data/local/src/main/java/com/jesuslcorominas/teamflowmanager/data/local/database/migration/Migrations.kt`
- Added `MIGRATION_5_6` to add `deleted` column with default value of 0 (false)

**File:** `data/local/src/main/java/com/jesuslcorominas/teamflowmanager/data/local/di/DataLocalModule.kt`
- Added `MIGRATION_5_6` to the database builder's migration list

### 3. Data Layer - Firestore (Remote Database)

**File:** `data/remote/src/main/java/com/jesuslcorominas/teamflowmanager/data/remote/firestore/PlayerFirestoreModel.kt`
- Added `deleted: Boolean = false` field to PlayerFirestoreModel
- Updated no-arg constructor to include deleted parameter
- Updated `toDomain()` and `toFirestoreModel()` mapper functions

**File:** `data/remote/src/main/java/com/jesuslcorominas/teamflowmanager/data/remote/datasource/PlayerFirestoreDataSourceImpl.kt`
- Updated all Firestore queries to filter deleted players using `.whereEqualTo("deleted", false)`
  - `getAllPlayers()`
  - `getPlayerById()`
  - `getCaptainPlayer()`
  - `clearAllCaptains()`
  - `findDocumentIdByPlayerId()`
- Changed `deletePlayer()` from physical document deletion to logical update
- Added documentation about image retention policy for soft-deleted players

## Key Features

### 1. Data Preservation
- Goals associated with deleted players are preserved
- Playing time history for deleted players is preserved
- Player images in Firebase Storage are retained

### 2. Backward Compatibility
- Database migration adds `deleted` column with default value of 0 (false)
- Existing players automatically get `deleted = false`
- No data loss during migration

### 3. Consistent Behavior
- Both Room (local) and Firestore (remote) implementations filter deleted players
- Deleted players don't appear in:
  - Player lists
  - Captain selection
  - Player queries by ID

### 4. Special Cases Handled
- `deleteAllPlayers()` method remains for physical deletion during data migration
- Captain status queries exclude deleted players
- Helper methods for finding players filter out deleted records

## Testing
- Code review completed: ✅ No issues
- Security scan (CodeQL): ✅ No vulnerabilities detected
- Backward compatibility: ✅ Migration handles existing data

## Files Changed
Total: 8 files
- Domain: 1 file
- Data Local: 5 files
- Data Remote: 2 files

## Statistics
- Lines added: 49
- Lines removed: 23
- Net change: +26 lines

## Future Considerations

### Image Cleanup
Player images in Firebase Storage are intentionally retained for historical data. If storage cleanup becomes necessary in the future, consider implementing:
- A maintenance task to identify and remove orphaned images
- A configurable retention policy for images of deleted players
- An admin tool to manually clean up storage

### Physical Deletion Option
If complete removal of player data is needed (e.g., for GDPR compliance), consider adding:
- A separate "permanent delete" function
- Admin-only access to physical deletion
- Cascade deletion of all related data (goals, times, etc.)

## Migration Path
1. Database automatically migrates from version 5 to 6 on app upgrade
2. `deleted` column is added to players table with default value 0
3. All existing players are marked as not deleted
4. No user action required

## Notes
- The logical deletion is transparent to the application logic
- No changes needed to use cases or view models
- The delete operation signature remains the same
- Historical data queries can be expanded in the future to include deleted players if needed
