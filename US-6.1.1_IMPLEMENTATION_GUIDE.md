# US-6.1.1: Match Summary Implementation Guide

## Overview
This document describes the implementation of the match summary feature that displays a comprehensive summary after a match is finished.

## User Story
**Como entrenador** quiero poder ver el resumen de un partido finalizado. 

### Requirements
- Show match summary when a match is finished instead of blank screen
- Display:
  - Match result and final score
  - Total time
  - Time played in each half (25 minutes + stoppage time)
  - List of players with total times played by each
  - Substitutions section showing:
    - Player out (red arrow ⬇)
    - Player in (green arrow ⬆)
    - Moment of substitution
    - Player numbers in roster format
- Allow navigation to match summary from finished matches list
- Make match summary read-only (no edit/pause/resume)

## Implementation Architecture

### 1. Domain Layer

#### MatchStatus Update
**File**: `usecase/src/main/kotlin/.../usecase/FinishMatchUseCase.kt`

Updated to set match status to `FINISHED` when finishing a match:
```kotlin
val finishedMatch = match.copy(
    isRunning = false,
    elapsedTimeMillis = matchFinalElapsedTime,
    lastStartTimeMillis = null,
    status = MatchStatus.FINISHED,
)
```

#### GetMatchSummaryUseCase
**File**: `usecase/src/main/kotlin/.../usecase/GetMatchSummaryUseCase.kt`

New use case to retrieve complete match summary:
- **Input**: `matchId: Long`
- **Output**: `Flow<MatchSummary?>`
- **Data Classes**:
  - `MatchSummary`: Contains match, playerTimes, and substitutions
  - `PlayerTimeSummary`: Player and elapsed time
  - `SubstitutionSummary`: Players involved and substitution time

**Logic**:
1. Combines data from:
   - Match repository (match details)
   - PlayerTimeHistory repository (saved player times)
   - PlayerSubstitution repository (substitutions)
   - Player repository (player info)
2. Sorts player times by elapsed time (descending)
3. Sorts substitutions chronologically
4. Returns null if match not found

### 2. ViewModel Layer

#### MatchViewModel Updates
**File**: `viewmodel/src/main/java/.../viewmodel/MatchViewModel.kt`

**New State**: Added `MatchUiState.Finished` state:
```kotlin
data class Finished(
    val matchId: Long,
    val matchTimeMillis: Long,
    val opponent: String,
    val location: String,
    val playerTimes: List<PlayerTimeItem>,
    val substitutions: List<SubstitutionItem>,
) : MatchUiState()
```

**Updated**: `loadMatchData()` to detect finished matches and load summary

#### MatchSummaryViewModel
**File**: `viewmodel/src/main/java/.../viewmodel/MatchSummaryViewModel.kt`

New ViewModel for dedicated match summary viewing:
- **State**: `MatchSummaryUiState` (Loading, NotFound, Success)
- **Method**: `loadMatchSummary(matchId: Long)`
- Uses `GetMatchSummaryUseCase` to load data
- Transforms domain models to UI models

### 3. UI Layer

#### MatchSummaryScreen
**File**: `app/src/main/java/.../ui/matches/MatchSummaryScreen.kt`

New dedicated screen for viewing finished matches:
- Accessed via navigation with match ID
- Displays:
  - Match header card (opponent, location, total time)
  - Player times list (sorted by time)
  - Substitutions list (chronological)
- Read-only (no interaction)

**Key Components**:
- `MatchSummaryContent`: Main content layout
- `PlayerTimeCard`: Displays player with time played
- `SubstitutionCard`: Shows substitution with visual indicators

#### CurrentMatchScreen Updates
**File**: `app/src/main/java/.../ui/matches/CurrentMatchScreen.kt`

Added `FinishedMatchState` composable to handle finished state:
- Similar layout to MatchSummaryScreen
- Used when current match is finished
- Shows when navigating to CurrentMatch after finishing

#### MatchListScreen Updates
**File**: `app/src/main/java/.../ui/matches/MatchListScreen.kt`

- Made `PlayedMatchCard` clickable
- Added `onNavigateToMatchSummary` callback
- Navigates to MatchSummaryScreen when clicking finished match

#### Navigation
**Files**: 
- `app/src/main/java/.../ui/navigation/Route.kt`
- `app/src/main/java/.../ui/navigation/Navigation.kt`

Added new route:
```kotlin
object MatchSummary : Route(
    path = "match_summary",
    showTopBar = true,
    showBottomBar = false,
    canGoBack = true,
) {
    fun createRoute(matchId: Long): String = "$path/$matchId"
}
```

Navigation flow:
1. Match List → Click finished match → MatchSummaryScreen(matchId)
2. Current Match → Finish match → Shows finished state

### 4. UI Design

#### Match Summary Header
```
┌─────────────────────────────────────┐
│        Match Finished               │
│                                     │
│        Team A                       │
│        Stadium Name                 │
│                                     │
│        Total Time                   │
│        50:30                        │
└─────────────────────────────────────┘
```

#### Player Times Section
```
Player Times
┌─────────────────────────────────────┐
│ John Doe                  45:23    │
│ #10                                 │
└─────────────────────────────────────┘
┌─────────────────────────────────────┐
│ Jane Smith                38:15    │
│ #5                                  │
└─────────────────────────────────────┘
```

#### Substitutions Section
```
Substitutions
┌─────────────────────────────────────┐
│ 15:30  ⬇ John Doe    ⬆ Bob Johnson │
│         #10           #7            │
└─────────────────────────────────────┘
```

**Visual Indicators**:
- ⬇ Red arrow for player out (MaterialTheme.colorScheme.error)
- ⬆ Green arrow for player in (Color(0xFF4CAF50))

### 5. Dependency Injection

#### UseCaseModule
**File**: `usecase/src/main/kotlin/.../usecase/di/UseCaseModule.kt`

Registered `GetMatchSummaryUseCase`:
```kotlin
singleOf(::GetMatchSummaryUseCaseImpl) bind GetMatchSummaryUseCase::class
```

#### ViewModelModule
**File**: `viewmodel/src/main/java/.../viewmodel/di/ViewModelModule.kt`

1. Added `GetMatchSummaryUseCase` to `MatchViewModel` constructor
2. Registered `MatchSummaryViewModel`:
```kotlin
viewModel {
    MatchSummaryViewModel(
        getMatchSummaryUseCase = get(),
    )
}
```

### 6. Tests

#### GetMatchSummaryUseCaseTest
**File**: `usecase/src/test/kotlin/.../usecase/GetMatchSummaryUseCaseTest.kt`

**Test Cases** (4):
1. `invoke should return null when match does not exist`
2. `invoke should return match summary with player times sorted by time descending`
3. `invoke should return match summary with substitutions sorted by time ascending`
4. `invoke should return match summary with empty lists when no player times or substitutions`

#### MatchSummaryViewModelTest
**File**: `viewmodel/src/test/java/.../viewmodel/MatchSummaryViewModelTest.kt`

**Test Cases** (3):
1. `loadMatchSummary should update uiState to NotFound when summary is null`
2. `loadMatchSummary should update uiState to Success when summary is available`
3. `initial uiState should be Loading`

#### FinishMatchUseCaseTest Updates
**File**: `usecase/src/test/kotlin/.../usecase/FinishMatchUseCaseTest.kt`

Updated existing tests to verify:
- Match status is set to `MatchStatus.FINISHED`
- All other finish match logic still works correctly

## String Resources

Added to `app/src/main/res/values/strings.xml`:
```xml
<string name="match_finished">Match Finished</string>
<string name="match_summary_title">Match Summary</string>
<string name="match_not_found">Match not found</string>
<string name="total_time_label">Total Time</string>
<string name="substitutions_title">Substitutions</string>
<string name="player_out">Player Out</string>
<string name="player_in">Player In</string>
```

## Data Flow

### When Finishing a Match
1. User clicks "Finish Match" button
2. `MatchViewModel.saveMatch()` calls `FinishMatchUseCase`
3. `FinishMatchUseCase`:
   - Calculates final elapsed time
   - Sets match status to `FINISHED`
   - Saves player times to `PlayerTimeHistory`
   - Resets active player times
4. UI detects finished state
5. Shows `FinishedMatchState` in CurrentMatchScreen

### When Viewing Finished Match from List
1. User clicks finished match in list
2. Navigation → `MatchSummaryScreen(matchId)`
3. `MatchSummaryViewModel.loadMatchSummary(matchId)`
4. `GetMatchSummaryUseCase` retrieves:
   - Match details
   - Player times from history
   - Substitutions
   - Player information
5. ViewModel transforms to UI state
6. Screen renders match summary

## Edge Cases Handled

1. **Match not found**: Shows "Match not found" message
2. **No player times**: Shows empty list in player times section
3. **No substitutions**: Hides substitutions section entirely
4. **Zero elapsed time players**: Not saved to history (filtered in FinishMatchUseCase)

## Future Enhancements (Not Implemented)

These were mentioned in the requirements but not implemented in this PR:
- Display actual match score (commented as TODO in existing code)
- Show time played in each half separately (currently shows total time only)
- The requirement mentions "25 minutes + stoppage" but the app doesn't track halves separately yet

## Testing Notes

Due to Gradle network issues in the CI environment, tests could not be executed automatically. However:
- All test files have been created with proper structure
- Tests use MockK for mocking (consistent with existing tests)
- Tests follow existing patterns in the codebase
- Syntax has been manually verified

To run tests locally:
```bash
./gradlew :usecase:test --tests GetMatchSummaryUseCaseTest
./gradlew :viewmodel:test --tests MatchSummaryViewModelTest
./gradlew :usecase:test --tests FinishMatchUseCaseTest
```

## Files Changed/Added

### New Files
- `usecase/src/main/kotlin/.../usecase/GetMatchSummaryUseCase.kt`
- `usecase/src/test/kotlin/.../usecase/GetMatchSummaryUseCaseTest.kt`
- `viewmodel/src/main/java/.../viewmodel/MatchSummaryViewModel.kt`
- `viewmodel/src/test/java/.../viewmodel/MatchSummaryViewModelTest.kt`
- `app/src/main/java/.../ui/matches/MatchSummaryScreen.kt`

### Modified Files
- `usecase/src/main/kotlin/.../usecase/FinishMatchUseCase.kt`
- `usecase/src/main/kotlin/.../usecase/di/UseCaseModule.kt`
- `usecase/src/test/kotlin/.../usecase/FinishMatchUseCaseTest.kt`
- `viewmodel/src/main/java/.../viewmodel/MatchViewModel.kt`
- `viewmodel/src/main/java/.../viewmodel/di/ViewModelModule.kt`
- `app/src/main/java/.../ui/matches/CurrentMatchScreen.kt`
- `app/src/main/java/.../ui/matches/MatchListScreen.kt`
- `app/src/main/java/.../ui/navigation/Navigation.kt`
- `app/src/main/java/.../ui/navigation/Route.kt`
- `app/src/main/res/values/strings.xml`

## Conclusion

The implementation follows clean architecture principles with clear separation of concerns:
- **Domain**: Match status and data structures
- **Use Cases**: Business logic for match summary retrieval
- **ViewModels**: UI state management
- **UI**: Composable screens with Material Design 3

The feature is fully integrated with the existing navigation and dependency injection system, making it maintainable and extensible.
