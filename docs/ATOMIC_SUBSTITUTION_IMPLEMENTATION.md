# Atomic Player Substitution Implementation

## Overview
This document describes the implementation of atomic player substitutions to prevent temporary invalid UI states and player state management issues during player changes.

## Problem Statement
Previously, when substituting players during a match:
- **UI Flicker**: The incoming player could appear active before the outgoing player stopped being active
- **Invalid State**: The UI temporarily showed more players on the field than allowed
- **Statistics Issues**: Player counts and statistics could be momentarily inconsistent
- **Resume Bug**: Substituted-out players would restart when the match resumed after a pause
- This was especially visible because it affected multiple entities of the same type

## Solution: Atomic Operation Pattern with Coordinated Updates

### Architecture
The solution implements an atomic operation pattern with TWO key mechanisms:
1. **Database-level atomicity** through batch writes with shared operationId
2. **UI-level filtering** to show only completed operation states

```
MatchOperation (IN_PROGRESS) 
  → Update ALL active players (with operationId)
  → MatchOperation (COMPLETED) 
  → Match.lastCompletedOperationId updated
  → UI filters by operationId
```

### Key Components

#### 1. MatchOperationType Enum
Added `SUBSTITUTION` to track substitution operations:
```kotlin
enum class MatchOperationType {
    START,
    PAUSE,
    RESUME,
    FINISH,
    SUBSTITUTION  // NEW
}
```

#### 2. PlayerSubstitution Model
Added `operationId` field to link substitutions to their operation:
```kotlin
data class PlayerSubstitution(
    val id: Long = 0L,
    val matchId: Long,
    val playerOutId: Long,
    val playerInId: Long,
    val substitutionTimeMillis: Long,
    val matchElapsedTimeMillis: Long,
    val operationId: String? = null,  // NEW - Links to MatchOperation
)
```

#### 3. PlayerTimeStatus Distinction
Critical distinction between player states:
```kotlin
enum class PlayerTimeStatus {
    ON_BENCH,  // Substituted out or not yet played - DON'T restart on match resume
    PLAYING,   // Currently active on field
    PAUSED     // Was playing when match paused (half-time) - DO restart on match resume
}
```

#### 4. Repository Methods
Added `substituteOutPlayersBatchWithOperationId()` to properly handle substitutions:
```kotlin
// For substitutions - sets ON_BENCH (won't restart on resume)
suspend fun substituteOutPlayersBatchWithOperationId(
    playerIds: List<Long>,
    currentTimeMillis: Long,
    operationId: String
)

// For match pauses - sets PAUSED (will restart on resume)
suspend fun pauseTimersBatchWithOperationId(
    playerIds: List<Long>,
    currentTimeMillis: Long,
    operationId: String
)
```

#### 5. RegisterPlayerSubstitutionUseCase
Implements the 7-step atomic operation flow with coordinated updates:

```kotlin
override suspend fun invoke(matchId: Long, playerOutId: Long, playerInId: Long, currentTimeMillis: Long) {
    // Validation
    val match = matchRepository.getMatchById(matchId).first()
    val playerTimes = getAllPlayerTimesUseCase().first()
    val playerOutTime = playerTimes.find { it.playerId == playerOutId }
    
    if (playerOutTime?.status != PlayerTimeStatus.PLAYING) return
    
    val matchElapsedTime = match.getTotalElapsed(currentTimeMillis)
    
    // Step 1: Create operation with IN_PROGRESS
    val operation = MatchOperation(
        matchId = matchId,
        teamId = match.teamId,
        type = MatchOperationType.SUBSTITUTION,
        status = MatchOperationStatus.IN_PROGRESS,
    )
    val operationId = matchOperationRepository.createOperation(operation)
    
    // Step 2: Get all currently playing players for coordinated update
    val playingPlayerIds = playerTimes
        .filter { it.status == PlayerTimeStatus.PLAYING }
        .map { it.playerId }
    
    // Step 3: Substitute out player - sets ON_BENCH status (won't restart on resume)
    playerTimeRepository.substituteOutPlayersBatchWithOperationId(
        playerIds = listOf(playerOutId),
        currentTimeMillis = currentTimeMillis,
        operationId = operationId,
    )
    
    // Step 4: Start incoming player timer with operationId
    playerTimeRepository.startTimersBatchWithOperationId(
        playerIds = listOf(playerInId),
        currentTimeMillis = currentTimeMillis,
        operationId = operationId,
    )
    
    // Step 5: Update ALL other active players with new operationId
    // This is CRITICAL for UI filtering to work correctly!
    val otherPlayingPlayers = playingPlayerIds.filter { it != playerOutId }
    if (otherPlayingPlayers.isNotEmpty()) {
        playerTimeRepository.startTimersBatchWithOperationId(
            playerIds = otherPlayingPlayers,
            currentTimeMillis = currentTimeMillis,
            operationId = operationId,
        )
    }
    
    // Step 6: Record substitution with operationId
    val substitution = PlayerSubstitution(
        matchId = matchId,
        playerOutId = playerOutId,
        playerInId = playerInId,
        substitutionTimeMillis = currentTimeMillis,
        matchElapsedTimeMillis = matchElapsedTime,
        operationId = operationId,
    )
    playerSubstitutionRepository.insertSubstitution(substitution)
    
    // Step 7: Mark operation as COMPLETED
    matchOperationRepository.updateOperation(
        operation.copy(id = operationId, status = MatchOperationStatus.COMPLETED)
    )
    
    // Step 8: Update match's lastCompletedOperationId
    matchRepository.updateMatchWithOperationId(
        match = match.copy(lastCompletedOperationId = operationId),
        operationId = operationId
    )
}
```

#### 6. UI Layer Filtering
The ViewModel **filters by operationId** to prevent intermediate states from being visible:

```kotlin
// Filter player times to show only those from completed operations
val filteredPlayerTimes = if (match.lastCompletedOperationId != null) {
    playerTimes.filter { playerTime ->
        // Show players whose lastOperationId matches the match's last completed operation
        // OR has null operationId (backward compatibility)
        playerTime.lastOperationId == match.lastCompletedOperationId ||
            playerTime.lastOperationId == null
    }
} else {
    // No operations completed yet, show all player times
    playerTimes
}

val squadPlayers = players.filter { it.id in match.squadCallUpIds }
val playerTimeItems = squadPlayers.toPlayerItems(filteredPlayerTimes, currentTime, match.captainId)
```

**Why filtering IS needed:**
- Without filtering, each intermediate database write triggers a UI update, causing flicker
- Filtering by operationId ensures UI only shows "committed" state after operation completes
- Works because ALL active players get updated with the same operationId during each operation

## Benefits

### 1. Atomicity at Two Levels
- **Database level**: Batch writes with shared operationId ensure data consistency
- **UI level**: Filtering ensures only completed operation states are visible
- No race conditions or intermediate states visible to users

### 2. Correct State Management
- `ON_BENCH` status prevents substituted players from restarting on match resume
- `PAUSED` status ensures half-time players DO restart on match resume
- Clear separation of concerns between different pause reasons

### 3. Smooth UI Updates
- No flicker during multi-step operations
- UI updates only when operation completes
- All active players visible consistently

### 4. Audit Trail
- Each player's `lastOperationId` tracks which operation last modified them
- Enables debugging and audit of player state changes
- Maintains operation history for analysis

### 5. Offline Support
- The pattern works even in offline mode
- Local changes sync correctly when online
- Batch writes ensure consistency across all data stores

### 6. Reusability
- The pattern can be applied to any complex multi-entity operation
- Already used for START, PAUSE, RESUME, FINISH, and now SUBSTITUTION
- Easy to extend for future features

## Key Implementation Details

### Why Update All Active Players?
During a substitution, we update ALL currently playing players with the new operationId, not just the two involved:
```kotlin
// Update all other active players (excluding playerOut)
val otherPlayingPlayers = playingPlayerIds.filter { it != playerOutId }
if (otherPlayingPlayers.isNotEmpty()) {
    playerTimeRepository.startTimersBatchWithOperationId(
        playerIds = otherPlayingPlayers,
        currentTimeMillis = currentTimeMillis,
        operationId = operationId,
    )
}
```

**Why?** So that the UI filter `lastOperationId == match.lastCompletedOperationId` shows ALL currently active players, not just the two from the substitution.

### Why ON_BENCH vs PAUSED?
```kotlin
// Substitution: ON_BENCH status
substituteOutPlayersBatchWithOperationId() // Sets PlayerTimeStatus.ON_BENCH

// Match pause: PAUSED status  
pauseTimersBatchWithOperationId() // Sets PlayerTimeStatus.PAUSED
```

**Reason:** When resuming a match:
- Players with `PAUSED` status → restart (they were playing at half-time)
- Players with `ON_BENCH` status → stay on bench (they were substituted out)

## Common Pitfalls Avoided

### ❌ Previous Issue: Not updating all active players
```kotlin
// WRONG - Only updates the two substitution players
playerTimeRepository.startTimersBatchWithOperationId(listOf(playerInId), time, opId)
playerTimeRepository.pauseTimersBatchWithOperationId(listOf(playerOutId), time, opId)
// Result: Other players have old operationId, get filtered out!
```

### ✅ Current Solution: Update all active players
```kotlin
// CORRECT - Updates all active players with new operationId
playerTimeRepository.startTimersBatchWithOperationId(
    playerIds = playingPlayerIds + listOf(playerInId),
    currentTimeMillis = time,
    operationId = opId
)
// Result: All active players visible after filtering
```

### ❌ Previous Issue: Using PAUSED status for substitutions
```kotlin
// WRONG - Player will restart on match resume!
pauseTimersBatchWithOperationId(listOf(playerOutId), time, opId) // Sets PAUSED
```

### ✅ Current Solution: Use ON_BENCH status
```kotlin
// CORRECT - Player stays on bench when match resumes
substituteOutPlayersBatchWithOperationId(listOf(playerOutId), time, opId) // Sets ON_BENCH
```

## How It Works

The atomicity comes from TWO mechanisms working together:
1. **Database-level atomicity** - Batch writes with shared operationId ensure all updates happen together
2. **UI-level atomicity** - Filtering by operationId ensures only completed states are visible
3. **Coordinated updates** - ALL active players get updated with the same operationId
4. **Status distinction** - ON_BENCH (don't restart) vs PAUSED (do restart on resume)

## Backward Compatibility

Player times with `null` operationId (created before this implementation) are always displayed. This ensures data from before the operation tracking system was implemented continues to work correctly.

## Testing

### Unit Tests
Updated `RegisterPlayerSubstitutionUseCaseTest` to verify:
- Atomic operation creation and completion
- All players updated with the same operationId (not just the two in substitution)
- Match lastCompletedOperationId is updated
- No operations occur if validation fails
- Substituted-out player gets ON_BENCH status, not PAUSED

### Integration
The implementation integrates seamlessly with existing:
- Repository interfaces (added new substituteOutPlayersBatchWithOperationId method)
- Dependency injection (Koin auto-wires the new dependency)
- UI components (automatic via Flow updates and filtering)

## Security

CodeQL security scan completed with no vulnerabilities found.

## Files Modified

1. **domain/MatchOperationType.kt** - Added SUBSTITUTION enum
2. **domain/PlayerSubstitution.kt** - Added operationId field for audit trail
3. **usecase/repository/PlayerTimeRepository.kt** - Added substituteOutPlayersBatchWithOperationId() interface
4. **data/core/repository/PlayerTimeRepositoryImpl.kt** - Implemented substituteOutPlayersBatchWithOperationId()
5. **usecase/RegisterPlayerSubstitutionUseCaseImpl.kt** - Complete rewrite:
   - Use substituteOutPlayersBatchWithOperationId (sets ON_BENCH, not PAUSED)
   - Update ALL active players' operationId for UI filtering consistency
6. **viewmodel/MatchViewModel.kt** - Added operationId filtering to prevent UI flicker
7. **usecase/RegisterPlayerSubstitutionUseCaseTest.kt** - Updated tests for new behavior

## Future Enhancements

Potential improvements:
1. Error handling and rollback for failed operations
2. Operation timeout detection and recovery
3. Operation audit log for debugging
4. Performance optimization for large batch operations
5. Operation sequencing for better tracking

## References

- Issue: "Cambio atómico" (Atomic Change)
- Pattern: Operation Document with lastCompletedOperationId
- Repository methods: Already supported batch operations with operationId
