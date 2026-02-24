# US-2.2.2/4: Añadir goles y registrar goleadores - Implementation Summary

## Overview
This user story implements the functionality for a coach to add goals to their team's scoreboard and associate them with the goal scorers during an ongoing match.

## Acceptance Criteria ✅
- [x] My team's scoreboard is updated when a goal is added
- [x] The goal is associated with the player who scored it
- [x] The scoreboard displays the current goal count during the match
- [x] Goals can only be added when the match is running

## Technical Implementation

### 1. Domain Model: Goal
**Location:** `domain/src/main/kotlin/com/jesuslcorominas/teamflowmanager/domain/model/Goal.kt`

```kotlin
data class Goal(
    val id: Long = 0L,
    val matchId: Long,
    val scorerId: Long,
    val goalTimeMillis: Long,
    val matchElapsedTimeMillis: Long,
)
```

**Properties:**
- `id` - Auto-generated unique identifier
- `matchId` - Reference to the match where the goal was scored
- `scorerId` - Reference to the player who scored the goal
- `goalTimeMillis` - System timestamp when the goal was registered
- `matchElapsedTimeMillis` - Match elapsed time when the goal occurred

### 2. Database Layer

#### GoalEntity
**Location:** `data/local/src/main/java/com/jesuslcorominas/teamflowmanager/data/local/entity/GoalEntity.kt`

- Room entity with foreign keys to `MatchEntity` and `PlayerEntity`
- Cascade delete when match or player is deleted
- Includes mapper functions `toDomain()` and `toEntity()`

#### GoalDao
**Location:** `data/local/src/main/java/com/jesuslcorominas/teamflowmanager/data/local/dao/GoalDao.kt`

**Methods:**
- `getMatchGoals(matchId: Long): Flow<List<GoalEntity>>` - Retrieves all goals for a specific match ordered by time
- `insert(goal: GoalEntity): Long` - Inserts a new goal record

#### Database Update
**Location:** `data/local/src/main/java/com/jesuslcorominas/teamflowmanager/data/local/database/TeamFlowManagerDatabase.kt`

- Added `GoalEntity` to the entities list
- Incremented database version from 1 to 2
- Added `goalDao()` abstract method
- Added `.fallbackToDestructiveMigration()` to handle schema changes

### 3. Data Sources and Repositories

#### GoalLocalDataSource
**Location:** `data/core/src/main/kotlin/com/jesuslcorominas/teamflowmanager/data/core/datasource/GoalLocalDataSource.kt`

Interface defining data source operations for goals.

#### GoalLocalDataSourceImpl
**Location:** `data/local/src/main/java/com/jesuslcorominas/teamflowmanager/data/local/datasource/GoalLocalDataSourceImpl.kt`

Implementation that wraps `GoalDao` and handles domain/entity conversions.

#### GoalRepository
**Location:** `usecase/src/main/kotlin/com/jesuslcorominas/teamflowmanager/usecase/repository/GoalRepository.kt`

Repository interface for use cases.

#### GoalRepositoryImpl
**Location:** `data/core/src/main/kotlin/com/jesuslcorominas/teamflowmanager/data/core/repository/GoalRepositoryImpl.kt`

Implementation that delegates to `GoalLocalDataSource`.

### 4. Use Cases

#### RegisterGoalUseCase
**Location:** `usecase/src/main/kotlin/com/jesuslcorominas/teamflowmanager/usecase/RegisterGoalUseCase.kt`

**Responsibilities:**
- Retrieves the current match state
- Calculates the match elapsed time (accounting for running/paused state)
- Creates a Goal domain object
- Inserts the goal into the repository

**Dependencies:**
- `MatchRepository` - To get current match state
- `GoalRepository` - To insert the goal

**Test Coverage:**
- Records goal with correct match elapsed time when match is running
- Uses elapsed time when match is paused
- Calculates correct match elapsed time with different values
- Throws exception when no active match is found

#### GetGoalsForMatchUseCase
**Location:** `usecase/src/main/kotlin/com/jesuslcorominas/teamflowmanager/usecase/GetGoalsForMatchUseCase.kt`

**Responsibilities:**
- Retrieves all goals for a specific match

**Dependencies:**
- `GoalRepository`

**Test Coverage:**
- Returns goals from repository
- Returns empty list when no goals exist

### 5. ViewModel Updates

#### MatchViewModel
**Location:** `viewmodel/src/main/java/com/jesuslcorominas/teamflowmanager/viewmodel/MatchViewModel.kt`

**New Dependencies:**
- `RegisterGoalUseCase`
- `GetGoalsForMatchUseCase`

**New State:**
- `_showGoalScorerDialog: MutableStateFlow<Boolean>` - Controls goal scorer selection dialog visibility

**New Methods:**
- `showGoalScorerDialog()` - Shows the goal scorer selection dialog
- `dismissGoalScorerDialog()` - Dismisses the goal scorer selection dialog
- `registerGoal(scorerId: Long)` - Registers a goal for the specified player

**Updated Methods:**
- `loadMatchData()` - Now fetches goals count and includes it in `MatchUiState.Success`

**Updated UI State:**
- `MatchUiState.Success` - Added `goalsCount: Int` property

**Test Coverage:**
- `showGoalScorerDialog()` sets state correctly
- `dismissGoalScorerDialog()` clears state correctly
- `registerGoal()` calls use case and dismisses dialog
- Goals count is reflected in UI state

### 6. UI Updates

#### CurrentMatchScreen
**Location:** `app/src/main/java/com/jesuslcorominas/teamflowmanager/ui/matches/CurrentMatchScreen.kt`

**Changes:**

1. **State Management:**
   - Added `showGoalScorerDialog` state collection
   - Added goal scorer dialog to UI composition

2. **MatchTimeCard Updates:**
   - Added `goalsCount` parameter
   - Displays scoreboard label
   - Shows current goal count prominently in large display text

3. **OngoingMatchView Updates:**
   - Added `onAddGoal` callback parameter
   - Added "Add Goal" button that:
     - Is only enabled when match is running
     - Opens the goal scorer selection dialog when clicked
     - Is positioned above the Play/Pause and Stop buttons

4. **New Component: GoalScorerSelectionDialog:**
   - AlertDialog with title "Select Goal Scorer"
   - Lists all players in a scrollable LazyColumn
   - Each player shown in a Card with their number and name
   - Tapping a player registers the goal and closes the dialog
   - Includes a Cancel button

### 7. String Resources

#### Spanish (values-es/strings.xml)
```xml
<string name="add_goal_button">Añadir Gol</string>
<string name="scoreboard_label">Marcador</string>
<string name="select_goal_scorer_title">Seleccionar Goleador</string>
```

#### English (values/strings.xml)
```xml
<string name="add_goal_button">Add Goal</string>
<string name="scoreboard_label">Scoreboard</string>
<string name="select_goal_scorer_title">Select Goal Scorer</string>
```

### 8. Dependency Injection Updates

#### UseCaseModule
**Location:** `usecase/src/main/kotlin/com/jesuslcorominas/teamflowmanager/usecase/di/UseCaseModule.kt`

Added:
```kotlin
singleOf(::RegisterGoalUseCaseImpl) bind RegisterGoalUseCase::class
singleOf(::GetGoalsForMatchUseCaseImpl) bind GetGoalsForMatchUseCase::class
```

#### DataCoreModule
**Location:** `data/core/src/main/kotlin/com/jesuslcorominas/teamflowmanager/data/core/di/DataCoreModule.kt`

Added:
```kotlin
singleOf(::GoalRepositoryImpl) bind GoalRepository::class
```

#### DataLocalModule
**Location:** `data/local/src/main/java/com/jesuslcorominas/teamflowmanager/data/local/di/DataLocalModule.kt`

Added:
```kotlin
single { get<TeamFlowManagerDatabase>().goalDao() }
singleOf(::GoalLocalDataSourceImpl) bind GoalLocalDataSource::class
```

#### ViewModelModule
**Location:** `viewmodel/src/main/java/com/jesuslcorominas/teamflowmanager/viewmodel/di/ViewModelModule.kt`

Updated `MatchViewModel` factory:
```kotlin
viewModel {
    MatchViewModel(
        // ... existing dependencies
        registerGoalUseCase = get(),
        getGoalsForMatchUseCase = get(),
        // ... remaining dependencies
    )
}
```

## Architecture Flow

```
UI Layer (CurrentMatchScreen)
    ↓
ViewModel (MatchViewModel)
    ↓
Use Cases (RegisterGoalUseCase, GetGoalsForMatchUseCase)
    ↓
Repository (GoalRepository)
    ↓
Data Source (GoalLocalDataSource)
    ↓
DAO (GoalDao)
    ↓
Room Database (Goal table)
```

## User Flow

1. **During Match:**
   - Coach sees the current scoreboard in the MatchTimeCard
   - Scoreboard shows the number of goals scored

2. **Adding a Goal:**
   - Coach taps the "Add Goal" button (only enabled when match is running)
   - A dialog appears with all players listed
   - Coach taps on the player who scored
   - The goal is registered with:
     - Match ID
     - Scorer ID
     - Current system time
     - Match elapsed time
   - Dialog closes automatically
   - Scoreboard updates immediately to reflect the new goal

3. **Goal Storage:**
   - Goals are stored in the database with foreign keys to match and player
   - Goals include timestamp and match elapsed time for statistics
   - Goals are automatically deleted if the match or player is deleted (cascade)

## Testing

### Unit Tests Created:

1. **RegisterGoalUseCaseTest** - 4 test cases
   - ✅ Records goal with correct match elapsed time when match is running
   - ✅ Uses elapsed time when match is paused
   - ✅ Calculates correct match elapsed time with different values
   - ✅ Throws exception when no active match found

2. **GetGoalsForMatchUseCaseTest** - 2 test cases
   - ✅ Returns goals from repository
   - ✅ Returns empty list when no goals exist

3. **MatchViewModelGoalTest** - 4 test cases
   - ✅ showGoalScorerDialog sets state to true
   - ✅ dismissGoalScorerDialog sets state to false
   - ✅ registerGoal calls use case and dismisses dialog
   - ✅ Goals count is reflected in UI state

## Database Schema Changes

**Version:** 1 → 2

**New Table: goal**
```sql
CREATE TABLE goal (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    matchId INTEGER NOT NULL,
    scorerId INTEGER NOT NULL,
    goalTimeMillis INTEGER NOT NULL,
    matchElapsedTimeMillis INTEGER NOT NULL,
    FOREIGN KEY(matchId) REFERENCES match(id) ON DELETE CASCADE,
    FOREIGN KEY(scorerId) REFERENCES player(id) ON DELETE CASCADE
);
CREATE INDEX index_goal_matchId ON goal(matchId);
CREATE INDEX index_goal_scorerId ON goal(scorerId);
```

## Technical Decisions

1. **Database Migration Strategy:**
   - Used `.fallbackToDestructiveMigration()` for simplicity during development
   - In production, proper migrations should be implemented

2. **Goal Time Tracking:**
   - Store both system time (`goalTimeMillis`) and match elapsed time (`matchElapsedTimeMillis`)
   - System time for absolute ordering
   - Match elapsed time for display and statistics

3. **Button Enablement:**
   - "Add Goal" button only enabled when match is running
   - Prevents accidental goal registration during pause or before match starts

4. **Player Selection:**
   - Shows all players in the team, not just active players
   - Allows for goals scored by substitute players who might have just entered

5. **UI Placement:**
   - Scoreboard integrated into MatchTimeCard for visibility
   - "Add Goal" button placed prominently above match controls
   - Modal dialog for player selection to focus user's attention

## Files Changed

**Created:**
- `domain/src/main/kotlin/.../domain/model/Goal.kt`
- `data/local/src/main/java/.../entity/GoalEntity.kt`
- `data/local/src/main/java/.../dao/GoalDao.kt`
- `data/local/src/main/java/.../datasource/GoalLocalDataSourceImpl.kt`
- `data/core/src/main/kotlin/.../datasource/GoalLocalDataSource.kt`
- `data/core/src/main/kotlin/.../repository/GoalRepositoryImpl.kt`
- `usecase/src/main/kotlin/.../repository/GoalRepository.kt`
- `usecase/src/main/kotlin/.../RegisterGoalUseCase.kt`
- `usecase/src/main/kotlin/.../GetGoalsForMatchUseCase.kt`
- `usecase/src/test/kotlin/.../RegisterGoalUseCaseTest.kt`
- `usecase/src/test/kotlin/.../GetGoalsForMatchUseCaseTest.kt`
- `viewmodel/src/test/java/.../MatchViewModelGoalTest.kt`

**Modified:**
- `data/local/src/main/java/.../database/TeamFlowManagerDatabase.kt`
- `data/local/src/main/java/.../di/DataLocalModule.kt`
- `data/core/src/main/kotlin/.../di/DataCoreModule.kt`
- `usecase/src/main/kotlin/.../di/UseCaseModule.kt`
- `viewmodel/src/main/java/.../MatchViewModel.kt`
- `viewmodel/src/main/java/.../di/ViewModelModule.kt`
- `app/src/main/java/.../ui/matches/CurrentMatchScreen.kt`
- `app/src/main/res/values/strings.xml`
- `app/src/main/res/values-es/strings.xml`

## Summary

This implementation successfully adds goal tracking functionality to the TeamFlow Manager application. Coaches can now:
- View the current scoreboard during matches
- Add goals by selecting the scorer from a list of players
- Have goals automatically associated with the correct match and player
- See goals persist in the database with proper time tracking

The implementation follows the established architecture with proper separation of concerns across layers, includes comprehensive unit tests, and provides a clean, intuitive user interface for managing goals during matches.
