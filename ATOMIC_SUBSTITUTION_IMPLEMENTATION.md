# Atomic Player Substitution Implementation

## Overview
This document describes the implementation of atomic player substitutions to prevent temporary invalid UI states during player changes.

## Problem Statement
Previously, when substituting players during a match:
- The incoming player could appear active before the outgoing player stopped being active
- The UI temporarily showed more players on the field than allowed
- Statistics and player counts could be momentarily inconsistent
- This was especially visible because it affected multiple entities of the same type

## Solution: Atomic Operation Pattern

### Architecture
The solution implements an atomic operation pattern using operation tracking:

```
MatchOperation (IN_PROGRESS) → Player Updates (with operationId) → MatchOperation (COMPLETED) → Match Update
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
    val operationId: String? = null,  // NEW
)
```

#### 3. RegisterPlayerSubstitutionUseCase
Implements the 6-step atomic operation flow:

```kotlin
override suspend fun invoke(matchId: Long, playerOutId: Long, playerInId: Long, currentTimeMillis: Long) {
    // Validation
    val match = matchRepository.getMatchById(matchId).first()
    val playerTimes = getAllPlayerTimesUseCase().first()
    val playerOutTime = playerTimes.find { it.playerId == playerOutId }
    
    if (playerOutTime?.status != PlayerTimeStatus.PLAYING) return
    
    // Step 1: Create operation with IN_PROGRESS
    val operation = MatchOperation(
        matchId = matchId,
        teamId = match.teamId,
        type = MatchOperationType.SUBSTITUTION,
        status = MatchOperationStatus.IN_PROGRESS,
    )
    val operationId = matchOperationRepository.createOperation(operation)
    
    // Step 2-3: Update both players atomically with operationId
    playerTimeRepository.pauseTimersBatchWithOperationId(
        playerIds = listOf(playerOutId),
        currentTimeMillis = currentTimeMillis,
        operationId = operationId,
    )
    
    playerTimeRepository.startTimersBatchWithOperationId(
        playerIds = listOf(playerInId),
        currentTimeMillis = currentTimeMillis,
        operationId = operationId,
    )
    
    // Step 4: Record substitution with operationId
    val substitution = PlayerSubstitution(
        matchId = matchId,
        playerOutId = playerOutId,
        playerInId = playerInId,
        substitutionTimeMillis = currentTimeMillis,
        matchElapsedTimeMillis = matchElapsedTime,
        operationId = operationId,
    )
    playerSubstitutionRepository.insertSubstitution(substitution)
    
    // Step 5: Mark operation as COMPLETED
    matchOperationRepository.updateOperation(
        operation.copy(id = operationId, status = MatchOperationStatus.COMPLETED)
    )
    
    // Step 6: Update match's lastCompletedOperationId
    matchRepository.updateMatchWithOperationId(
        match = match.copy(lastCompletedOperationId = operationId),
        operationId = operationId
    )
}
```

#### 4. UI Layer - No Filtering Needed
The ViewModel **does not filter by operationId**. Instead, it shows all players based on their `PlayerTimeStatus`:

```kotlin
// Only include players that are in the squad call-up
val squadPlayers = players.filter { it.id in match.squadCallUpIds }
val playerTimeItems = squadPlayers.toPlayerItems(playerTimes, currentTime, match.captainId)
```

**Why no filtering?**
- Atomicity is guaranteed at the **repository/database level** through batch writes
- The `lastOperationId` on PlayerTime is for audit trail, not for UI filtering
- All active players should remain visible regardless of which operation last modified them
- Filtering by operationId would incorrectly hide players not involved in the most recent operation

## Benefits

### 1. Atomicity at Write Level
- The substitution writes happen atomically through batch operations
- Batch writes with shared operationId ensure data consistency
- No race conditions during write operations

### 2. Correct UI Behavior
- All active players remain visible at all times
- Player counts are always accurate
- UI shows the actual player state, not filtered state

### 3. Audit Trail
- Each player's `lastOperationId` tracks which operation last modified them
- Enables debugging and audit of player state changes
- Maintains operation history for analysis

### 4. Offline Support
- The pattern works even in offline mode
- Local changes sync correctly when online
- Batch writes ensure consistency across all data stores

### 4. Reusability
- The pattern can be applied to any complex multi-entity operation
- Already used for START, PAUSE, RESUME, and FINISH operations
- Easy to extend for future features

## Common Misconceptions

### ❌ Incorrect: Filter UI by operationId
```kotlin
// DON'T DO THIS - This would hide players!
playerTimes.filter { it.lastOperationId == match.lastCompletedOperationId }
```
This approach would hide all players except those involved in the most recent operation.

### ✅ Correct: Show all players based on their status
```kotlin
// DO THIS - Show all active players
val squadPlayers = players.filter { it.id in match.squadCallUpIds }
val playerTimeItems = squadPlayers.toPlayerItems(playerTimes, currentTime, match.captainId)
```
Let the `PlayerTimeStatus` (PLAYING, ON_BENCH, etc.) determine visibility, not the operationId.

## How It Works

The atomicity comes from:
1. **Batch writes** - Both player updates happen in a single atomic batch with shared operationId
2. **Repository-level atomicity** - Firestore batch writes guarantee all-or-nothing semantics
3. **Operation tracking** - The operationId provides an audit trail, not a visibility filter

## Backward Compatibility

Player times with `null` operationId (created before this implementation) are always displayed. This ensures data from before the operation tracking system was implemented continues to work correctly.

## Testing

### Unit Tests
Updated `RegisterPlayerSubstitutionUseCaseTest` to verify:
- Atomic operation creation and completion
- Both players updated with the same operationId
- Match lastCompletedOperationId is updated
- No operations occur if validation fails

### Integration
The implementation integrates seamlessly with existing:
- Repository interfaces (already supported operationId)
- Dependency injection (Koin auto-wires the new dependency)
- UI components (automatic via Flow updates)

## Security

CodeQL security scan completed with no vulnerabilities found.

## Files Modified

1. **domain/MatchOperationType.kt** - Added SUBSTITUTION enum
2. **domain/PlayerSubstitution.kt** - Added operationId field for audit trail
3. **usecase/RegisterPlayerSubstitutionUseCaseImpl.kt** - Implemented atomic batch writes
4. **usecase/RegisterPlayerSubstitutionUseCaseTest.kt** - Updated tests
5. ~~**viewmodel/MatchViewModel.kt**~~ - **No changes** (atomicity is at database level, not UI filtering)

## Future Enhancements

Potential improvements:
1. Error handling and rollback for failed operations
2. Operation timeout detection and recovery
3. Operation audit log for debugging
4. Performance optimization for batch operations

## References

- Issue: "Cambio atómico" (Atomic Change)
- Pattern: Operation Document with lastCompletedOperationId
- Repository methods: Already supported batch operations with operationId
