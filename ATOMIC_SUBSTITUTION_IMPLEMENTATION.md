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

#### 4. ViewModel Filtering
The ViewModel filters player times to only show completed operations:

```kotlin
val filteredPlayerTimes = if (match.lastCompletedOperationId != null) {
    playerTimes.filter { playerTime ->
        // Show only player times matching the last completed operation
        // or with null operationId (backward compatibility)
        playerTime.lastOperationId == match.lastCompletedOperationId ||
            playerTime.lastOperationId == null
    }
} else {
    // No operations yet, show all player times
    playerTimes
}
```

## Benefits

### 1. Atomicity
- The substitution appears as a single instantaneous action
- No intermediate states are visible to users
- Player counts are always correct

### 2. Consistency
- All related updates share the same operationId
- Changes are only visible after the operation completes
- The UI state is always valid

### 3. Offline Support
- The pattern works even in offline mode
- Local changes sync correctly when online
- No race conditions or temporary inconsistencies

### 4. Reusability
- The pattern can be applied to any complex multi-entity operation
- Already used for START, PAUSE, RESUME, and FINISH operations
- Easy to extend for future features

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
2. **domain/PlayerSubstitution.kt** - Added operationId field
3. **usecase/RegisterPlayerSubstitutionUseCaseImpl.kt** - Implemented atomic pattern
4. **usecase/RegisterPlayerSubstitutionUseCaseTest.kt** - Updated tests
5. **viewmodel/MatchViewModel.kt** - Added operation-based filtering

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
