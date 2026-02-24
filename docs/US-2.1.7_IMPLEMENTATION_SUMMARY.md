# US-2.1.7: Iniciar tiempo de un partido - Implementation Summary

## Overview
This user story implements the functionality for a coach to start a match, which triggers all chronometers for the starting lineup players and the general match timer.

## Acceptance Criteria ✅
- [x] All chronometers start when initiating a match
- [x] The match is marked as started
- [x] Visual indication in the match list that a match is running
- [x] Only one match can be running at a time (other matches cannot be started)

## Technical Implementation

### 1. New Use Case: StartMatchUseCase
**Location:** `usecase/src/main/kotlin/com/jesuslcorominas/teamflowmanager/usecase/StartMatchUseCase.kt`

**Responsibilities:**
- Loads a match by its ID
- Updates the match to mark it as running (`isRunning = true`, sets `lastStartTimeMillis`)
- Starts timers for all players in the starting lineup
- Throws an exception if the match is not found

**Dependencies:**
- `GetMatchByIdUseCase` - To retrieve the match to start
- `StartPlayerTimerUseCase` - To start individual player timers
- `MatchRepository` - To update the match status

**Test Coverage:**
- Normal flow with starting lineup players
- Empty starting lineup handling
- Match not found error handling

### 2. ViewModel Updates

#### MatchListViewModel
**Added method:** `startMatch(matchId: Long)`
- Takes a match ID parameter
- Calls the `StartMatchUseCase` with the current system time
- Executed in a coroutine scope for async operation

**Updated DI:** Added `StartMatchUseCase` dependency injection

### 3. Data Layer Updates

#### MatchDao Query Fix
**Location:** `data/local/src/main/java/com/jesuslcorominas/teamflowmanager/data/local/dao/MatchDao.kt`

**Change:**
```kotlin
// Before (hardcoded to match with ID = 1)
@Query("SELECT * FROM match WHERE id = 1 LIMIT 1")
fun getMatch(): Flow<MatchEntity?>

// After (finds any running match)
@Query("SELECT * FROM match WHERE isRunning = 1 LIMIT 1")
fun getMatch(): Flow<MatchEntity?>
```

**Impact:**
- Any match can now become the "current match" when started
- Only one match can be running at a time (database constraint by design)
- The CurrentMatchScreen displays whichever match is currently running

### 4. UI Updates

#### MatchListScreen
**Location:** `app/src/main/java/com/jesuslcorominas/teamflowmanager/ui/matches/MatchListScreen.kt`

**Change:** Updated the "Start Match" button click handler to:
1. Call `viewModel.startMatch(match.id)` to start the match
2. Navigate to `CurrentMatchScreen`

**Existing UI features maintained:**
- Button is disabled if another match is already active (`hasActiveMatch` check)
- Warning message displayed when trying to start while another match is running
- Visual distinction between pending and played matches

### 5. Bonus Fix: FinishMatchUseCase
**Location:** `usecase/src/main/kotlin/com/jesuslcorominas/teamflowmanager/usecase/FinishMatchUseCase.kt`

**Enhancement:**
- Now properly updates the match to `isRunning = false` when finishing
- Calculates final elapsed time for both match and players
- Ensures the match no longer appears as "current match" after finishing
- Complete match lifecycle: create → start → finish

## Flow Diagram

```
User Action: Click "Start Match" button
    ↓
MatchListScreen.onStart(match.id)
    ↓
MatchListViewModel.startMatch(matchId)
    ↓
StartMatchUseCase.invoke(matchId, currentTime)
    ├─→ GetMatchByIdUseCase.invoke(matchId)
    ├─→ MatchRepository.updateMatch(match.copy(isRunning=true, ...))
    └─→ For each player in startingLineupIds:
        └─→ StartPlayerTimerUseCase.invoke(playerId, currentTime)
    ↓
Navigate to CurrentMatchScreen
    ↓
MatchViewModel observes getMatch() → Shows running match
```

## Database State Changes

### Before Starting a Match:
```kotlin
Match(
    id = 5L,
    opponent = "Real Madrid",
    isRunning = false,
    elapsedTimeMillis = 0L,
    lastStartTimeMillis = null,
    startingLineupIds = [1, 2, 3, 4, 5]
)
```

### After Starting the Match:
```kotlin
Match(
    id = 5L,
    opponent = "Real Madrid",
    isRunning = true,  // ✅ CHANGED
    elapsedTimeMillis = 0L,
    lastStartTimeMillis = 1697123456789L,  // ✅ CHANGED
    startingLineupIds = [1, 2, 3, 4, 5]
)

// Also creates/updates PlayerTime entries:
PlayerTime(playerId = 1, isRunning = true, lastStartTimeMillis = 1697123456789L)
PlayerTime(playerId = 2, isRunning = true, lastStartTimeMillis = 1697123456789L)
PlayerTime(playerId = 3, isRunning = true, lastStartTimeMillis = 1697123456789L)
PlayerTime(playerId = 4, isRunning = true, lastStartTimeMillis = 1697123456789L)
PlayerTime(playerId = 5, isRunning = true, lastStartTimeMillis = 1697123456789L)
```

## Testing

### Unit Tests Created/Updated:
1. **StartMatchUseCaseTest** - New
   - Tests normal flow with starting lineup
   - Tests empty starting lineup
   - Tests error handling for missing match

2. **FinishMatchUseCaseTest** - Updated
   - Added verification that match is marked as not running
   - Tests calculate final elapsed time for running match

### Manual Testing Checklist:
- [ ] Create a new match with starting lineup and substitutes
- [ ] Verify "Start Match" button is enabled
- [ ] Click "Start Match" button
- [ ] Verify navigation to CurrentMatchScreen
- [ ] Verify match timer is running
- [ ] Verify all starting lineup player timers are running
- [ ] Verify substitute player timers are NOT running
- [ ] Go back to match list
- [ ] Verify the started match shows as "active"
- [ ] Verify other matches cannot be started (button disabled)
- [ ] Go to CurrentMatchScreen
- [ ] Click "Finish Match"
- [ ] Verify match no longer shows as current
- [ ] Verify match appears in "played matches" section

## Dependencies

### Module Dependencies:
```
app → viewmodel → usecase → domain
                     ↓
              data:core → data:local
```

### Koin DI Configuration:
- `useCaseModule`: Provides `StartMatchUseCase` implementation
- `viewModelModule`: Injects `StartMatchUseCase` into `MatchListViewModel`

## Edge Cases Handled

1. **Match not found**: Throws `IllegalArgumentException`
2. **Empty starting lineup**: Does not start any player timers (no error)
3. **Multiple start attempts**: UI prevents starting if another match is already active
4. **Match already running**: Would update timestamp (though UI prevents this)

## Performance Considerations

- All database operations are asynchronous (Flow/suspend functions)
- Player timer starts are sequential but fast (Room operations)
- UI updates are reactive through StateFlow
- No memory leaks (proper coroutine scope usage)

## Future Enhancements (Not in Scope)

- Pause/resume match functionality
- Player substitutions during match
- Real-time timer updates (currently 1-second polling)
- Match score tracking
- Match statistics during play

## Related User Stories

- US-2.1.6: Crear partido (Create Match) - Prerequisite
- Future: Pause/Resume match timers
- Future: Player substitutions
- Future: Match statistics and reporting

## Files Changed

### New Files:
- `usecase/src/main/kotlin/com/jesuslcorominas/teamflowmanager/usecase/StartMatchUseCase.kt`
- `usecase/src/test/kotlin/com/jesuslcorominas/teamflowmanager/usecase/StartMatchUseCaseTest.kt`

### Modified Files:
- `usecase/src/main/kotlin/com/jesuslcorominas/teamflowmanager/usecase/di/UseCaseModule.kt`
- `usecase/src/main/kotlin/com/jesuslcorominas/teamflowmanager/usecase/FinishMatchUseCase.kt`
- `usecase/src/test/kotlin/com/jesuslcorominas/teamflowmanager/usecase/FinishMatchUseCaseTest.kt`
- `viewmodel/src/main/java/com/jesuslcorominas/teamflowmanager/viewmodel/MatchListViewModel.kt`
- `viewmodel/src/main/java/com/jesuslcorominas/teamflowmanager/viewmodel/di/ViewModelModule.kt`
- `data/local/src/main/java/com/jesuslcorominas/teamflowmanager/data/local/dao/MatchDao.kt`
- `app/src/main/java/com/jesuslcorominas/teamflowmanager/ui/matches/MatchListScreen.kt`

## Architecture Compliance

✅ **Clean Architecture Maintained:**
- Domain layer remains pure (no dependencies)
- Use cases contain business logic
- Repositories abstract data sources
- ViewModels coordinate UI state
- UI layer only calls ViewModels

✅ **Dependency Injection:**
- All dependencies injected via Koin
- Testable with mockk

✅ **Testing:**
- Unit tests for use cases
- MockK for mocking dependencies
- Coroutine testing with runTest

✅ **Kotlin Best Practices:**
- Immutable data classes
- Sealed classes for state
- Extension functions where appropriate
- Null safety maintained
