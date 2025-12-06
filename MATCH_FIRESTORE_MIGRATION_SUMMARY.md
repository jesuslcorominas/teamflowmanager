# Match In-Progress Functionality - Firestore Migration Summary

## Overview

This document summarizes the migration of all match in-progress functionality from local Room database to Firebase Firestore. The migration was completed to enable real-time synchronization and cloud-based match management as requested in the issue "Gestión de partido en curso".

## Migrated Components

### 1. Goals (Goles)
**New Files Created:**
- `data/remote/src/main/java/com/jesuslcorominas/teamflowmanager/data/remote/firestore/GoalFirestoreModel.kt`
  - Firestore serialization model for Goal entities
  - Includes conversion functions to/from domain Goal model
  - Contains `@DocumentId` annotation for automatic document ID handling
  
- `data/remote/src/main/java/com/jesuslcorominas/teamflowmanager/data/remote/datasource/GoalFirestoreDataSourceImpl.kt`
  - Implements `GoalDataSource` interface using Firestore
  - Provides real-time listeners for match goals
  - Uses stable ID generation from Firestore document IDs
  - Validates team ownership through teamId field

**Functionality:**
- ✅ Register goals during a match
- ✅ Track goal scorer, time, and match elapsed time
- ✅ Support for opponent goals and own goals
- ✅ Real-time updates when goals are added

### 2. Player Substitutions (Cambios)
**New Files Created:**
- `data/remote/src/main/java/com/jesuslcorominas/teamflowmanager/data/remote/firestore/PlayerSubstitutionFirestoreModel.kt`
  - Firestore serialization model for PlayerSubstitution entities
  - Includes conversion functions to/from domain PlayerSubstitution model
  - Contains `@DocumentId` annotation for automatic document ID handling

- `data/remote/src/main/java/com/jesuslcorominas/teamflowmanager/data/remote/datasource/PlayerSubstitutionFirestoreDataSourceImpl.kt`
  - Implements `PlayerSubstitutionDataSource` interface using Firestore
  - Provides real-time listeners for match substitutions
  - Uses stable ID generation from Firestore document IDs
  - Validates team ownership through teamId field

**Functionality:**
- ✅ Register player substitutions during a match
- ✅ Track players going in/out and substitution time
- ✅ Record match elapsed time at substitution
- ✅ Real-time updates when substitutions occur

### 3. Player Time Tracking (Tiempo de Juego)
**New Files Created:**
- `data/remote/src/main/java/com/jesuslcorominas/teamflowmanager/data/remote/firestore/PlayerTimeFirestoreModel.kt`
  - Firestore serialization model for PlayerTime entities
  - Includes conversion functions to/from domain PlayerTime model
  - Contains `@DocumentId` annotation for automatic document ID handling

- `data/remote/src/main/java/com/jesuslcorominas/teamflowmanager/data/remote/datasource/PlayerTimeFirestoreDataSourceImpl.kt`
  - Implements `PlayerTimeDataSource` interface using Firestore
  - Provides real-time listeners for player time state
  - Uses playerId as document ID for efficient retrieval
  - Validates team ownership through teamId field

**Functionality:**
- ✅ Track current playing time for each player during active match
- ✅ Monitor player status (playing, on bench, paused)
- ✅ Track elapsed time and running state
- ✅ Real-time updates when player time changes
- ✅ Reset all player times when match finishes

### 4. Player Time History (Historial de Tiempos)
**New Files Created:**
- `data/remote/src/main/java/com/jesuslcorominas/teamflowmanager/data/remote/firestore/PlayerTimeHistoryFirestoreModel.kt`
  - Firestore serialization model for PlayerTimeHistory entities
  - Includes conversion functions to/from domain PlayerTimeHistory model
  - Contains `@DocumentId` annotation for automatic document ID handling

- `data/remote/src/main/java/com/jesuslcorominas/teamflowmanager/data/remote/datasource/PlayerTimeHistoryFirestoreDataSourceImpl.kt`
  - Implements `PlayerTimeHistoryDataSource` interface using Firestore
  - Provides real-time listeners for historical player time data
  - Uses stable ID generation from Firestore document IDs
  - Validates team ownership through teamId field

**Functionality:**
- ✅ Store historical player time records after match completion
- ✅ Query player time history by player
- ✅ Query player time history by match
- ✅ Support for player time statistics and analysis
- ✅ Real-time updates when history is saved

### 5. Match Timer Operations (Already on Firestore)
**Existing Implementation:**
- Match timer operations were already using Firestore through `MatchFirestoreDataSourceImpl`
- No changes needed to these operations

**Functionality:**
- ✅ Start match (inicio de partido)
- ✅ Pause match (pausa)
- ✅ Resume match after pause
- ✅ Start timeout (tiempo muerto)
- ✅ End timeout
- ✅ Finish match (fin de partido)

## Architecture

The implementation follows clean architecture principles with the following flow:

```
┌──────────────────────────────────────────────────────────┐
│                     UI Layer (Compose)                    │
└────────────────────────┬─────────────────────────────────┘
                         │
                         ▼
┌──────────────────────────────────────────────────────────┐
│                   ViewModels (app)                        │
└────────────────────────┬─────────────────────────────────┘
                         │
                         ▼
┌──────────────────────────────────────────────────────────┐
│  Use Cases (usecase module)                              │
│  - RegisterGoalUseCase                                   │
│  - RegisterPlayerSubstitutionUseCase                     │
│  - StartMatchTimerUseCase                                │
│  - PauseMatchUseCase / ResumeMatchUseCase                │
│  - StartTimeoutUseCase / EndTimeoutUseCase               │
│  - FinishMatchUseCase                                    │
└────────────────────────┬─────────────────────────────────┘
                         │
                         ▼
┌──────────────────────────────────────────────────────────┐
│  Repositories (data:core module)                         │
│  - GoalRepositoryImpl                                    │
│  - PlayerSubstitutionRepositoryImpl                      │
│  - PlayerTimeRepositoryImpl                              │
│  - PlayerTimeHistoryRepositoryImpl                       │
│  - MatchRepositoryImpl                                   │
└────────────────────────┬─────────────────────────────────┘
                         │
                         ▼
┌──────────────────────────────────────────────────────────┐
│  Data Sources (data:remote module)                       │
│  - GoalFirestoreDataSourceImpl         ← NEW            │
│  - PlayerSubstitutionFirestoreDataSourceImpl ← NEW      │
│  - PlayerTimeFirestoreDataSourceImpl ← NEW              │
│  - PlayerTimeHistoryFirestoreDataSourceImpl ← NEW       │
│  - MatchFirestoreDataSourceImpl                          │
└────────────────────────┬─────────────────────────────────┘
                         │
                         ▼
┌──────────────────────────────────────────────────────────┐
│              Firebase Firestore (Cloud)                   │
│  Collections:                                            │
│  - goals/                                                │
│  - substitutions/                                        │
│  - playerTimes/                                          │
│  - playerTimeHistory/                                    │
│  - matches/                                              │
└──────────────────────────────────────────────────────────┘
```

## Dependency Injection Changes

### DataRemoteModule (data:remote)
**Added:**
```kotlin
singleOf(::GoalFirestoreDataSourceImpl) bind GoalDataSource::class
singleOf(::PlayerSubstitutionFirestoreDataSourceImpl) bind PlayerSubstitutionDataSource::class
singleOf(::PlayerTimeFirestoreDataSourceImpl) bind PlayerTimeDataSource::class
singleOf(::PlayerTimeHistoryFirestoreDataSourceImpl) bind PlayerTimeHistoryDataSource::class
```

### DataLocalModule (data:local)
**Removed DI Bindings (implementations kept):**
```kotlin
// Removed from DI:
// singleOf(::GoalLocalDataSourceImpl) bind GoalDataSource::class
// singleOf(::PlayerSubstitutionLocalDataSourceImpl) bind PlayerSubstitutionDataSource::class
// singleOf(::PlayerTimeLocalDataSourceImpl) bind PlayerTimeDataSource::class
// singleOf(::PlayerTimeHistoryLocalDataSourceImpl) bind PlayerTimeHistoryDataSource::class
```

## Firestore Collections Structure

### goals Collection
```
goals/
  {documentId}/
    id: String (Firestore document ID)
    teamId: String (Firestore team document ID)
    matchId: Long (stable hash of match document ID)
    scorerId: Long? (player ID who scored, null for opponent goals)
    goalTimeMillis: Long (system timestamp when goal occurred)
    matchElapsedTimeMillis: Long (elapsed match time when goal occurred)
    isOpponentGoal: Boolean
    isOwnGoal: Boolean
```

### substitutions Collection
```
substitutions/
  {documentId}/
    id: String (Firestore document ID)
    teamId: String (Firestore team document ID)
    matchId: Long (stable hash of match document ID)
    playerOutId: Long (player leaving the field)
    playerInId: Long (player entering the field)
    substitutionTimeMillis: Long (system timestamp of substitution)
    matchElapsedTimeMillis: Long (elapsed match time at substitution)
```

### playerTimes Collection
```
playerTimes/
  player_{playerId}/
    id: String (document ID based on playerId)
    teamId: String (Firestore team document ID)
    playerId: Long (player ID)
    elapsedTimeMillis: Long (total elapsed playing time)
    isRunning: Boolean (whether timer is currently running)
    lastStartTimeMillis: Long? (timestamp when timer was last started)
    status: String (player status: ON_BENCH, PLAYING, PAUSED)
```

### playerTimeHistory Collection
```
playerTimeHistory/
  {documentId}/
    id: String (Firestore document ID)
    teamId: String (Firestore team document ID)
    playerId: Long (player ID)
    matchId: Long (stable hash of match document ID)
    elapsedTimeMillis: Long (total time played in this match)
    savedAtMillis: Long (timestamp when history was saved)
```

## Security

All new data sources follow the same security pattern as existing Firestore implementations:

1. **Authentication Required**: All operations require a logged-in Firebase user
2. **Team Ownership Validation**: All documents include a `teamId` field that references the team's Firestore document ID
3. **Security Rules**: Firestore security rules should validate that `request.auth.uid` matches the team's `ownerId`
4. **Error Handling**: Proper exception handling for permission denied and network errors

## Real-Time Synchronization

All Firestore data sources use real-time listeners via `callbackFlow`:

- **Goals**: Updates immediately when any goal is added for a match
- **Substitutions**: Updates immediately when any substitution is recorded
- **Player Times**: Updates immediately when player time state changes during match
- **Player Time History**: Updates immediately when history is saved after match completion
- **Match State**: Updates immediately when match timer state changes (already implemented)

This enables real-time collaboration and instant UI updates without polling.

## Local Data Sources Preservation

As requested in the requirements, local data sources were NOT deleted:

**Preserved Files:**
- `data/local/src/main/java/com/jesuslcorominas/teamflowmanager/data/local/datasource/GoalLocalDataSourceImpl.kt`
- `data/local/src/main/java/com/jesuslcorominas/teamflowmanager/data/local/datasource/PlayerSubstitutionLocalDataSourceImpl.kt`
- `data/local/src/main/java/com/jesuslcorominas/teamflowmanager/data/local/datasource/MatchLocalDataSourceImpl.kt`
- `data/local/src/main/java/com/jesuslcorominas/teamflowmanager/data/local/datasource/PlayerTimeLocalDataSourceImpl.kt`
- `data/local/src/main/java/com/jesuslcorominas/teamflowmanager/data/local/datasource/PlayerTimeHistoryLocalDataSourceImpl.kt`

These implementations remain available as:
- Reference implementations
- Potential fallback for offline scenarios (future feature)
- Historical code documentation

## Code Review Notes

The code review identified potential optimizations:

1. **Team Document ID Caching**: The `getTeamDocumentId()` function is called frequently in Flow operations. This follows the existing pattern in `MatchFirestoreDataSourceImpl` but could be optimized with caching in a future PR.

2. **Code Duplication**: The `getTeamDocumentId()` function is duplicated across multiple Firestore data sources. This is consistent with the existing codebase but could be extracted to a shared helper class in future refactoring.

These are noted for future optimization but don't impact functionality or correctness.

## Testing Recommendations

To fully validate this migration, the following testing should be performed:

1. **Unit Tests**: Existing use case tests should pass without modification (they use mocked repositories)

2. **Integration Tests**:
   - Create a match and start it
   - Register goals during the match
   - Verify goals appear in real-time in the UI
   - Register player substitutions
   - Verify substitutions appear in real-time
   - Track player times during match
   - Verify player time updates in real-time
   - Pause and resume the match
   - Start and end timeouts
   - Finish the match and verify history is saved

3. **Multi-Device Testing**:
   - Open the same match on two devices
   - Register a goal on device 1
   - Verify it appears immediately on device 2
   - Same test for substitutions and player time changes

4. **Offline/Online Testing**:
   - Test behavior when device goes offline
   - Verify Firestore offline persistence works
   - Test syncing when device comes back online

## Migration Completion

All match in-progress functionality is now running on Firestore:

- ✅ Match creation and scheduling
- ✅ Match timer (start, pause, resume)
- ✅ Timeouts (start, end)
- ✅ Goals registration
- ✅ Player substitutions
- ✅ Player time tracking
- ✅ Player time history
- ✅ Match finish
- ✅ Match timer (start, pause, resume)
- ✅ Timeouts (start, end)
- ✅ Goals registration
- ✅ Player substitutions
- ✅ Match finish

The application now has full cloud-based match management with real-time synchronization across devices.

## Future Enhancements

Potential future improvements:

1. **Team Document ID Caching**: Implement caching to reduce Firestore queries
2. **Shared Base Class**: Extract common Firestore logic into a base class
3. **Offline Mode**: Implement explicit offline mode with local database fallback
4. **Batch Operations**: Use Firestore batch writes for atomic multi-document updates
5. **Indexes**: Create composite indexes for common queries (e.g., teamId + matchId)

## Related Documentation

- Firebase Firestore documentation: https://firebase.google.com/docs/firestore
- Project architecture: See README.md
- Previous migrations: US-1.1.6, US-1.1.7, US-2.1.x (Player and Team migrations)
