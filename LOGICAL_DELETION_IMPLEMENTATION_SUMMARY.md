# Logical Deletion Implementation Summary

## Overview
Implemented logical deletion (soft delete) for the Player entity to preserve historical data when players are deleted from the app.

## Problem Statement
The Player entity could be deleted from the app, but this deletion was physical, removing all player data from the database. This caused loss of historical information including goals and playing time statistics associated with the deleted player.

## Solution
Changed from physical deletion to logical deletion by adding a `deleted` flag to the Player entity. When a player is "deleted", they are marked as deleted but remain in Firestore, preserving all historical data.

**Note:** Local database (Room) changes were intentionally omitted as this layer is legacy code that will be removed in a future update. Only Firestore (remote database) implementation was modified.

## Changes Made

### 1. Domain Layer
**File:** `domain/src/main/kotlin/com/jesuslcorominas/teamflowmanager/domain/model/Player.kt`
- Added `deleted: Boolean = false` field to the Player data class

### 2. Data Layer - Firestore (Remote Database)

**File:** `data/remote/src/main/java/com/jesuslcorominas/teamflowmanager/data/remote/firestore/PlayerFirestoreModel.kt`
- Added `deleted: Boolean = false` field to PlayerFirestoreModel
- Updated no-arg constructor to include deleted parameter
- Updated `toDomain()` and `toFirestoreModel()` mapper functions

**File:** `data/remote/src/main/java/com/jesuslcorominas/teamflowmanager/data/remote/datasource/PlayerFirestoreDataSourceImpl.kt`
- Removed `.whereEqualTo("deleted", false)` from Firestore queries for backward compatibility
- Added client-side filtering using `.filter { !it.deleted }` after retrieving documents
- This ensures existing documents without the `deleted` field are treated as not deleted
- Updated methods:
  - `getAllPlayers()` - filters deleted players after retrieval
  - `getPlayerById()` - filters deleted players after retrieval
  - `getCaptainPlayer()` - checks deleted flag after retrieval
  - `clearAllCaptains()` - only clears captain status for non-deleted players
  - `findDocumentIdByPlayerId()` - checks deleted flag in condition
- Changed `deletePlayer()` from physical document deletion to logical update
- Added documentation about image retention policy for soft-deleted players

## Key Features

### 1. Data Preservation
- Goals associated with deleted players are preserved
- Playing time history for deleted players is preserved
- Player images in Firebase Storage are retained

### 2. Backward Compatibility
- Existing Firestore documents without the `deleted` field are treated as not deleted (default behavior)
- Queries retrieve all documents and filter on the client side
- No data loss

### 3. Consistent Behavior
- Firestore implementation filters deleted players
- Deleted players don't appear in:
  - Player lists
  - Captain selection
  - Player queries by ID

### 4. Special Cases Handled
- Captain status queries exclude deleted players
- Helper methods for finding players filter out deleted records

## Testing
- Code review completed: ✅ No issues
- Security scan (CodeQL): ✅ No vulnerabilities detected

## Files Changed
Total: 3 files
- Domain: 1 file
- Data Remote (Firestore): 2 files

## Statistics
- Lines added: ~30
- Lines removed: ~10
- Net change: ~20 lines

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

## Notes
- The logical deletion is transparent to the application logic
- No changes needed to use cases or view models
- The delete operation signature remains the same
- Historical data queries can be expanded in the future to include deleted players if needed
- Local database (Room) layer intentionally not modified as it's legacy code scheduled for removal
