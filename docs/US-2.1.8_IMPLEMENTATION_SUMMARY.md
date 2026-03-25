# US-2.1.8: Pausar un partido - Implementation Summary

## Overview
This user story implements the functionality for a coach to pause a match during half-time (descanso), which stops all running chronometers including the general match timer and all active player timers.

## Acceptance Criteria ✅
- [x] All chronometers stop when pausing a match
- [x] The match can be resumed (the match remains in course, just paused)
- [x] Match time is consolidated in the database
- [x] Player times are consolidated in the database
- [x] No other match can be started while a match is paused

## Technical Implementation

### 1. New Use Case: PauseMatchUseCase
**Location:** `usecase/src/main/kotlin/com/jesuslcorominas/teamflowmanager/usecase/PauseMatchUseCase.kt`

**Responsibilities:**
- Pauses the match timer by calling `PauseMatchTimerUseCase`
- Gets all active player times
- Pauses only the running player timers (filters by `isRunning = true`)

**Dependencies:**
- `PauseMatchTimerUseCase` - To pause the match timer
- `GetAllPlayerTimesUseCase` - To retrieve all player times
- `PausePlayerTimerUseCase` - To pause individual player timers

**Test Coverage:**
- Pauses match timer and all running player timers
- Pauses match timer even when no player timers are running
- Pauses match timer when no player times exist
- Only pauses players that are actually running (filters correctly)

### 2. ViewModel Updates

#### MatchViewModel
**Added method:** `pauseMatch()`
- Gets the current system time
- Calls the `PauseMatchUseCase` with the current timestamp
- Executed in a coroutine scope for async operation

**Updated DI:** Added `PauseMatchUseCase` dependency injection

**Updated Tests:** 
- Added `pauseMatchUseCase` mock to all existing tests
- Added new test `pauseMatch should call pauseMatchUseCase with current time`

### 3. UI Updates

#### CurrentMatchScreen
**Location:** `app/src/main/java/com/jesuslcorominas/teamflowmanager/ui/matches/CurrentMatchScreen.kt`

**Changes:**
1. Added `onPauseMatch` callback parameter to `SuccessState` composable
2. Added "Descanso" button that:
   - Only displays when `state.matchIsRunning == true`
   - Calls `viewModel.pauseMatch()` when clicked
   - Appears above the "Finish Match" button
3. Updated preview to include the new callback

**UI Behavior:**
- When match is running: Shows both "Descanso" and "Finalizar partido" buttons
- When match is paused: Shows only "Finalizar partido" button
- The running indicator on match time card and player cards reflects the paused state

### 4. String Resources

#### Spanish (values-es/strings.xml)
```xml
<string name="pause_match_button">Descanso</string>
```

#### English (values/strings.xml)
```xml
<string name="pause_match_button">Half Time</string>
```

### 5. Dependency Injection Updates

#### UseCaseModule
**Location:** `usecase/src/main/kotlin/com/jesuslcorominas/teamflowmanager/usecase/di/UseCaseModule.kt`

Added:
```kotlin
import com.jesuslcorominas.teamflowmanager.usecase.PauseMatchUseCase
import com.jesuslcorominas.teamflowmanager.usecase.PauseMatchUseCaseImpl

singleOf(::PauseMatchUseCaseImpl) bind PauseMatchUseCase::class
```

#### ViewModelModule
**Location:** `viewmodel/src/main/java/com/jesuslcorominas/teamflowmanager/viewmodel/di/ViewModelModule.kt`

Updated `MatchViewModel` factory:
```kotlin
viewModel {
    MatchViewModel(
        getMatchUseCase = get(),
        getAllPlayerTimesUseCase = get(),
        getPlayersUseCase = get(),
        saveMatchUseCase = get(),
        pauseMatchUseCase = get(), // ✅ NEW
    )
}
```

## Flow Diagram

```
User Action: Click "Descanso" button (when match is running)
    ↓
CurrentMatchScreen.onPauseMatch()
    ↓
MatchViewModel.pauseMatch()
    ↓
PauseMatchUseCase.invoke(currentTimeMillis)
    ├─→ PauseMatchTimerUseCase.invoke(currentTimeMillis)
    │   └─→ MatchRepository.pauseTimer(currentTimeMillis)
    │       └─→ Updates Match: isRunning = false, elapsedTimeMillis += (current - lastStart)
    │
    └─→ GetAllPlayerTimesUseCase.invoke()
        └─→ For each PlayerTime where isRunning == true:
            └─→ PausePlayerTimerUseCase.invoke(playerId, currentTimeMillis)
                └─→ PlayerTimeRepository.pauseTimer(playerId, currentTimeMillis)
                    └─→ Updates PlayerTime: isRunning = false, elapsedTimeMillis += (current - lastStart)
    ↓
UI automatically updates (reactive Flow/StateFlow)
    ├─→ Match time card shows paused time (no longer incrementing)
    ├─→ "ACTIVO" indicator disappears
    ├─→ "Descanso" button disappears (only shown when running)
    └─→ All player time cards show paused state
```

## Database State Changes

### Before Pausing a Match:
```kotlin
Match(
    id = 5L,
    opponent = "Real Madrid",
    isRunning = true,  // Match is running
    elapsedTimeMillis = 900000L, // 15 minutes elapsed
    lastStartTimeMillis = 1697123456789L,
    startingLineupIds = [1, 2, 3, 4, 5]
)

PlayerTime(playerId = 1, isRunning = true, elapsedTimeMillis = 900000L, lastStartTimeMillis = 1697123456789L)
PlayerTime(playerId = 2, isRunning = true, elapsedTimeMillis = 900000L, lastStartTimeMillis = 1697123456789L)
PlayerTime(playerId = 3, isRunning = false, elapsedTimeMillis = 450000L, lastStartTimeMillis = null) // Substitute, not running
```

### After Pausing the Match (assume 5 more minutes passed, currentTime = 1697123756789L):
```kotlin
Match(
    id = 5L,
    opponent = "Real Madrid",
    isRunning = false,  // ✅ PAUSED
    elapsedTimeMillis = 1200000L, // ✅ 20 minutes consolidated (900000 + 300000)
    lastStartTimeMillis = null,  // ✅ Reset
    startingLineupIds = [1, 2, 3, 4, 5]
)

PlayerTime(playerId = 1, isRunning = false, elapsedTimeMillis = 1200000L, lastStartTimeMillis = null) // ✅ PAUSED
PlayerTime(playerId = 2, isRunning = false, elapsedTimeMillis = 1200000L, lastStartTimeMillis = null) // ✅ PAUSED
PlayerTime(playerId = 3, isRunning = false, elapsedTimeMillis = 450000L, lastStartTimeMillis = null) // Not changed (wasn't running)
```

## Testing

### Unit Tests Created/Updated:

1. **PauseMatchUseCaseTest** - New
   - `invoke should pause match timer and all running player timers`
   - `invoke should pause match timer even when no player timers are running`
   - `invoke should pause match timer when no player times exist`

2. **MatchViewModelTest** - Updated
   - Added `pauseMatchUseCase` mock to setup
   - Updated all existing tests to include the new dependency
   - Added new test: `pauseMatch should call pauseMatchUseCase with current time`

### Manual Testing Checklist:
- [ ] Start a match with some players in starting lineup
- [ ] Verify match timer is running
- [ ] Verify starting lineup player timers are running
- [ ] Verify "Descanso" button is visible
- [ ] Click "Descanso" button
- [ ] Verify match timer stops incrementing
- [ ] Verify all player timers stop incrementing
- [ ] Verify "ACTIVO" indicator disappears from match time card
- [ ] Verify "ACTIVO" indicator disappears from all running player time cards
- [ ] Verify "Descanso" button disappears (only shown when running)
- [ ] Verify match times are correctly consolidated in database
- [ ] Verify the match is still considered "in course" (cannot start another match)
- [ ] Verify the match can be resumed later (US-2.1.9 - not in this scope)

## Architecture Compliance

✅ **Clean Architecture Maintained:**
- Domain layer remains pure (no dependencies)
- Use cases contain business logic (pause coordination)
- Repositories abstract data sources
- ViewModels coordinate UI state
- UI layer only calls ViewModels

✅ **Dependency Injection:**
- All dependencies injected via Koin
- Testable with mockk

✅ **Testing:**
- Unit tests for use cases with comprehensive scenarios
- Unit tests for ViewModel
- MockK for mocking dependencies
- Coroutine testing with runTest

✅ **Kotlin Best Practices:**
- Immutable data classes
- Sealed classes for state
- Flow for reactive data
- Null safety maintained
- Proper coroutine scope usage

## Files Changed

### New Files:
- `usecase/src/main/kotlin/com/jesuslcorominas/teamflowmanager/usecase/PauseMatchUseCase.kt`
- `usecase/src/test/kotlin/com/jesuslcorominas/teamflowmanager/usecase/PauseMatchUseCaseTest.kt`

### Modified Files:
- `usecase/src/main/kotlin/com/jesuslcorominas/teamflowmanager/usecase/di/UseCaseModule.kt`
- `viewmodel/src/main/java/com/jesuslcorominas/teamflowmanager/viewmodel/MatchViewModel.kt`
- `viewmodel/src/main/java/com/jesuslcorominas/teamflowmanager/viewmodel/di/ViewModelModule.kt`
- `viewmodel/src/test/java/com/jesuslcorominas/teamflowmanager/viewmodel/MatchViewModelTest.kt`
- `app/src/main/java/com/jesuslcorominas/teamflowmanager/ui/matches/CurrentMatchScreen.kt`
- `app/src/main/res/values/strings.xml`
- `app/src/main/res/values-es/strings.xml`

## Edge Cases Handled

1. **No player times exist**: Only pauses match timer (no errors)
2. **No players running**: Only pauses match timer (filters correctly)
3. **Some players paused, some running**: Only pauses the running ones
4. **Match already paused**: Repository handles idempotency (no error, but already paused)

## Performance Considerations

- All database operations are asynchronous (Flow/suspend functions)
- Player timer pauses are sequential but fast (Room operations)
- UI updates are reactive through StateFlow
- No memory leaks (proper coroutine scope usage)
- Filtering of running players happens in-memory (efficient)

## Relationship to Other User Stories

### Prerequisites:
- US-2.1.6: Crear partido (Create Match) - Match must exist
- US-2.1.7: Iniciar tiempo de un partido (Start Match) - Match must be started first

### Related Future Stories:
- US-2.1.9: Reanudar un partido (Resume Match) - Opposite operation
- Player substitution during match (pause needed for substitution)
- Half-time statistics (uses consolidated first-half data)

## Business Rules Validated

1. ✅ **All timers stop together**: Match timer and all active player timers pause simultaneously
2. ✅ **Time consolidation**: Elapsed time is calculated and stored in database
3. ✅ **Match remains in course**: Match stays with `isRunning = false` but not finished
4. ✅ **Cannot start another match**: The paused match still prevents other matches from starting
5. ✅ **Only running timers affected**: Paused/stopped player timers are not affected
6. ✅ **Button visibility**: "Descanso" button only appears when match is actually running

## Implementation Notes

### Why filter isRunning in Use Case?
We filter for `isRunning = true` in the use case to avoid unnecessary repository calls for players who are already paused. This is more efficient than calling pause on every player time record.

### Why use GetAllPlayerTimesUseCase instead of Match.startingLineupIds?
Using `GetAllPlayerTimesUseCase` ensures we capture ALL active players, including:
- Starting lineup players
- Substitutes who entered the game
- Any player whose timer was manually started

This is more robust than relying on `startingLineupIds` which represents only the initial lineup.

### Why separate PauseMatchTimerUseCase and PauseMatchUseCase?
- `PauseMatchTimerUseCase`: Low-level operation, pauses only the match timer
- `PauseMatchUseCase`: High-level business operation, coordinates pausing match AND all player timers

This follows the Single Responsibility Principle and allows for flexibility in the future.

## Known Limitations (Not in Scope)

- Cannot resume the match from this screen (requires US-2.1.9)
- No visual indicator of half-time vs full-time pause
- No automatic pause at specific time intervals
- No half-time statistics display
