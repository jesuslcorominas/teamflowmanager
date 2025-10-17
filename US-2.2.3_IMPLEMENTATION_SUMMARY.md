# US-2.2.3: Añadir goles al marcador contrario - Implementation Summary

## Overview
This user story implements the functionality for a coach to add goals to the opponent team's scoreboard during an ongoing match, complementing the existing functionality to add goals for their own team.

## User Story
**Como entrenador, quiero añadir goles al marcador del equipo oponente para reflejar el resultado real del partido.**

## Acceptance Criteria ✅
- [x] El marcador contrario se actualiza correctamente cuando se añade un gol
- [x] Los goles del equipo propio y del oponente se distinguen claramente en el marcador
- [x] Se puede añadir goles al equipo oponente solo cuando el partido está en curso
- [x] El marcador muestra ambos equipos (Mi Equipo - Rival) durante el partido

## Technical Implementation

### 1. Domain Model Update: Goal
**Location:** `domain/src/main/kotlin/com/jesuslcorominas/teamflowmanager/domain/model/Goal.kt`

**Changes:**
- Added `isOpponentGoal: Boolean = false` field to distinguish between team and opponent goals

```kotlin
data class Goal(
    val id: Long = 0L,
    val matchId: Long,
    val scorerId: Long,
    val goalTimeMillis: Long,
    val matchElapsedTimeMillis: Long,
    val isOpponentGoal: Boolean = false,
)
```

### 2. Database Layer Updates

#### GoalEntity
**Location:** `data/local/src/main/java/com/jesuslcorominas/teamflowmanager/data/local/entity/GoalEntity.kt`

**Changes:**
- Added `isOpponentGoal: Boolean = false` field to entity
- Updated `toDomain()` mapper to include the new field
- Updated `toEntity()` mapper to include the new field

#### Database Version
**Location:** `data/local/src/main/java/com/jesuslcorominas/teamflowmanager/data/local/database/TeamFlowManagerDatabase.kt`

**Changes:**
- Incremented database version from 1 to 2
- Database will use fallback to destructive migration (existing functionality)

### 3. Use Case Updates

#### RegisterGoalUseCase
**Location:** `usecase/src/main/kotlin/com/jesuslcorominas/teamflowmanager/usecase/RegisterGoalUseCase.kt`

**Changes:**
- Added `isOpponentGoal: Boolean = false` parameter to interface and implementation
- The parameter is passed to the Goal domain object when creating it

**Interface:**
```kotlin
interface RegisterGoalUseCase {
    suspend operator fun invoke(
        matchId: Long,
        scorerId: Long,
        currentTimeMillis: Long,
        isOpponentGoal: Boolean = false,
    ): Long
}
```

**Test Coverage:**
- Added test for registering opponent goals
- Verifies that `isOpponentGoal` flag is correctly set
- All existing tests still pass

### 4. ViewModel Updates

#### MatchViewModel
**Location:** `viewmodel/src/main/java/com/jesuslcorominas/teamflowmanager/viewmodel/MatchViewModel.kt`

**New State:**
- `_showOpponentGoalDialog: MutableStateFlow<Boolean>` - Controls opponent goal confirmation dialog visibility
- `showOpponentGoalDialog: StateFlow<Boolean>` - Public state flow

**New Methods:**
- `showOpponentGoalDialog()` - Shows the opponent goal confirmation dialog
- `dismissOpponentGoalDialog()` - Dismisses the opponent goal confirmation dialog
- `registerOpponentGoal()` - Registers an opponent goal with scorerId = 0L (dummy ID)

**Updated Methods:**
- `registerGoal()` - Now explicitly passes `isOpponentGoal = false`
- `loadMatchData()` - Separates goals into team goals and opponent goals counts

**Updated UI State:**
- `MatchUiState.Success` - Added `opponentGoalsCount: Int = 0` property

**Test Coverage:**
- Added test for `showOpponentGoalDialog()`
- Added test for `dismissOpponentGoalDialog()`
- Added test for `registerOpponentGoal()`
- Added test for opponent goals count in UI state
- Verified that team and opponent goals are counted separately

### 5. UI Updates

#### CurrentMatchScreen
**Location:** `app/src/main/java/com/jesuslcorominas/teamflowmanager/ui/matches/CurrentMatchScreen.kt`

**Changes:**

1. **State Management:**
   - Added `showOpponentGoalDialog` state collection
   - Added opponent goal confirmation dialog to UI composition

2. **MatchTimeCard Updates:**
   - Added `opponentGoalsCount` parameter
   - Updated scoreboard display to show both teams side by side:
     - "Mi Equipo" with team goals count (in primary color)
     - "-" separator
     - "Rival" with opponent goals count (in error/red color)

3. **OngoingMatchView Updates:**
   - Added `onAddOpponentGoal` callback parameter
   - Replaced single "Add Goal" button with a Row containing two buttons:
     - "Añadir Gol" button for team goals
     - "Añadir Gol Rival" button for opponent goals
   - Both buttons are only enabled when match is running

4. **New Component: OpponentGoalConfirmationDialog:**
   - AlertDialog with title "Añadir Gol al Marcador Contrario"
   - Asks for confirmation: "¿Quieres añadir un gol al equipo rival?"
   - "Añadir" button confirms and registers the goal
   - "Cancelar" button dismisses the dialog

### 6. String Resources

#### Spanish (values-es/strings.xml)
```xml
<string name="add_goal_button">Añadir Gol</string>
<string name="add_opponent_goal_button">Añadir Gol Rival</string>
<string name="add_opponent_goal_title">Añadir Gol al Marcador Contrario</string>
<string name="add_opponent_goal_message">¿Quieres añadir un gol al equipo rival?</string>
<string name="scoreboard_label">Marcador</string>
<string name="my_team_label">Mi Equipo</string>
<string name="opponent_team_label">Rival</string>
```

#### English (values/strings.xml)
```xml
<string name="add_goal_button">Add Goal</string>
<string name="add_opponent_goal_button">Add Opponent Goal</string>
<string name="add_opponent_goal_title">Add Opponent Goal</string>
<string name="add_opponent_goal_message">Do you want to add a goal for the opponent team?</string>
<string name="scoreboard_label">Scoreboard</string>
<string name="my_team_label">My Team</string>
<string name="opponent_team_label">Opponent</string>
```

## Architecture Flow

```
UI Layer (CurrentMatchScreen)
    ↓ (User taps "Añadir Gol Rival")
    ↓ (OpponentGoalConfirmationDialog shown)
    ↓ (User confirms)
ViewModel (MatchViewModel.registerOpponentGoal())
    ↓ (isOpponentGoal = true, scorerId = 0L)
Use Case (RegisterGoalUseCase)
    ↓ (Creates Goal with isOpponentGoal flag)
Repository (GoalRepository)
    ↓
Data Source (GoalLocalDataSource)
    ↓
DAO (GoalDao)
    ↓
Room Database (Goal table with isOpponentGoal column)
    ↓ (Goals retrieved via GetGoalsForMatchUseCase)
    ↓ (Separated into team and opponent counts)
ViewModel (MatchViewModel.loadMatchData())
    ↓ (teamGoalsCount, opponentGoalsCount)
UI State (MatchUiState.Success)
    ↓
UI Layer (CurrentMatchScreen.MatchTimeCard)
    ↓ (Displays: "Mi Equipo: X - Rival: Y")
```

## User Flow

1. **During Match:**
   - Coach sees the current scoreboard in the MatchTimeCard
   - Scoreboard shows: "Mi Equipo: X - Rival: Y"
   - Team goals appear in primary color (blue)
   - Opponent goals appear in error color (red)

2. **Adding an Opponent Goal:**
   - Coach taps "Añadir Gol Rival" button (only enabled when match is running)
   - A confirmation dialog appears asking "¿Quieres añadir un gol al equipo rival?"
   - Coach taps "Añadir" to confirm
   - The opponent goal is registered with:
     - Match ID
     - Scorer ID = 0L (dummy ID for opponent)
     - Current system time
     - Match elapsed time
     - isOpponentGoal = true
   - Dialog closes automatically
   - Scoreboard updates immediately to reflect the new opponent goal

3. **Goal Storage:**
   - Goals are stored in the database with the `isOpponentGoal` flag
   - Team goals have `isOpponentGoal = false`
   - Opponent goals have `isOpponentGoal = true` and `scorerId = 0L`
   - Goals are automatically deleted if the match is deleted (cascade)

## Technical Decisions

1. **Opponent Goal Scorer ID:**
   - Used scorerId = 0L as a dummy value for opponent goals
   - Opponent players are not tracked in the system
   - This avoids needing to create a separate entity for opponent teams/players

2. **Database Schema:**
   - Added `isOpponentGoal` boolean field instead of creating separate tables
   - Keeps the data model simple and maintainable
   - Allows for easy filtering of goals by type

3. **UI Design:**
   - Side-by-side scoreboard display (Team - Opponent)
   - Color coding: primary for team, error/red for opponent
   - Separate buttons for clarity instead of a dropdown or toggle
   - Confirmation dialog for opponent goals to prevent accidental taps

4. **Button Enablement:**
   - Both goal buttons only enabled when match is running
   - Prevents accidental goal registration during pause or before match starts

5. **Database Version:**
   - Incremented version to 2 to trigger schema update
   - Uses fallback to destructive migration (existing approach)
   - For production, proper migration scripts should be implemented

## Testing

### Unit Tests Created/Updated:

1. **RegisterGoalUseCaseTest** - Added 1 new test
   - ✅ Records opponent goal correctly with isOpponentGoal flag

2. **MatchViewModelGoalTest** - Added 4 new tests
   - ✅ showOpponentGoalDialog sets state to true
   - ✅ dismissOpponentGoalDialog sets state to false
   - ✅ registerOpponentGoal calls use case with correct parameters
   - ✅ Opponent goals count is reflected in UI state separately from team goals

### Total Test Coverage:
- Use Cases: 5 tests (4 existing + 1 new)
- ViewModel: 8 tests (4 existing + 4 new)

## Files Changed

**Modified:**
- `domain/src/main/kotlin/com/jesuslcorominas/teamflowmanager/domain/model/Goal.kt`
- `data/local/src/main/java/com/jesuslcorominas/teamflowmanager/data/local/entity/GoalEntity.kt`
- `data/local/src/main/java/com/jesuslcorominas/teamflowmanager/data/local/database/TeamFlowManagerDatabase.kt`
- `usecase/src/main/kotlin/com/jesuslcorominas/teamflowmanager/usecase/RegisterGoalUseCase.kt`
- `usecase/src/test/kotlin/com/jesuslcorominas/teamflowmanager/usecase/RegisterGoalUseCaseTest.kt`
- `viewmodel/src/main/java/com/jesuslcorominas/teamflowmanager/viewmodel/MatchViewModel.kt`
- `viewmodel/src/test/java/com/jesuslcorominas/teamflowmanager/viewmodel/MatchViewModelGoalTest.kt`
- `app/src/main/java/com/jesuslcorominas/teamflowmanager/ui/matches/CurrentMatchScreen.kt`
- `app/src/main/res/values/strings.xml`
- `app/src/main/res/values-es/strings.xml`

**Created:**
- None (all changes were modifications to existing files)

## Summary

This implementation successfully adds opponent goal tracking functionality to the TeamFlow Manager application. Coaches can now:
- View both team and opponent scoreboards side by side during matches
- Add goals to the opponent team's scoreboard
- Have opponent goals automatically tracked with the correct match and time information
- See goals persist in the database with proper distinction between team and opponent

The implementation follows the established architecture with proper separation of concerns across layers, includes comprehensive unit tests covering the new functionality, and provides a clean, intuitive user interface that makes it clear which scoreboard is being updated.

The scoreboard display clearly shows:
- **Mi Equipo** (My Team) score in primary color
- A separator "-"
- **Rival** (Opponent) score in error/red color

This visual distinction helps coaches quickly understand the match status at a glance.
