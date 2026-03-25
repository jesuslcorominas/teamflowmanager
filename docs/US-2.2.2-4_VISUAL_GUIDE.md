# US-2.2.2/4: Visual Guide - Añadir goles y registrar goleadores

## UI Flow Overview

### 1. Match Screen with Scoreboard

When a match is running, the coach sees the MatchTimeCard with the scoreboard:

```
┌──────────────────────────────────────────┐
│                                          │
│            First Half                    │
│                                          │
│            Scoreboard                    │
│                3                         │
│                                          │
│             25:00                        │
│                                          │
└──────────────────────────────────────────┘
```

### 2. Add Goal Button

Below the player list, the "Add Goal" button is prominently displayed:

```
┌──────────────────────────────────────────┐
│  Player Times                    ▼ Sort  │
├──────────────────────────────────────────┤
│ ┌────────────────────────────────────┐   │
│ │ 10  John Doe         [C]  15:30    │   │
│ │ (Green card - active)              │   │
│ └────────────────────────────────────┘   │
│ ┌────────────────────────────────────┐   │
│ │  5  Jane Smith           10:20     │   │
│ │ (White card - inactive)            │   │
│ └────────────────────────────────────┘   │
│                                          │
├──────────────────────────────────────────┤
│                                          │
│    ┌────────────────────────────────┐   │
│    │       ⚽ Add Goal              │   │
│    └────────────────────────────────┘   │
│                                          │
│    ▶️  Pause           ⏹️  Stop          │
│                                          │
└──────────────────────────────────────────┘
```

### 3. Goal Scorer Selection Dialog

When the coach taps "Add Goal", a dialog appears:

```
┌──────────────────────────────────────────┐
│                                          │
│      Select Goal Scorer                  │
│                                          │
│  ┌────────────────────────────────────┐  │
│  │                                    │  │
│  │  ┌──────────────────────────────┐ │  │
│  │  │ 10  John Doe                 │ │  │
│  │  └──────────────────────────────┘ │  │
│  │                                    │  │
│  │  ┌──────────────────────────────┐ │  │
│  │  │  5  Jane Smith               │ │  │
│  │  └──────────────────────────────┘ │  │
│  │                                    │  │
│  │  ┌──────────────────────────────┐ │  │
│  │  │  7  Mike Johnson             │ │  │
│  │  └──────────────────────────────┘ │  │
│  │                                    │  │
│  │  ┌──────────────────────────────┐ │  │
│  │  │  3  Sarah Williams           │ │  │
│  │  └──────────────────────────────┘ │  │
│  │                                    │  │
│  └────────────────────────────────────┘  │
│                                          │
│                         [Cancel]         │
│                                          │
└──────────────────────────────────────────┘
```

### 4. After Goal is Registered

The scoreboard updates immediately:

```
┌──────────────────────────────────────────┐
│                                          │
│            First Half                    │
│                                          │
│            Scoreboard                    │
│                4  ← Updated!             │
│                                          │
│             25:30                        │
│                                          │
└──────────────────────────────────────────┘
```

## Component Breakdown

### MatchTimeCard Enhancement

**Before:**
```
┌──────────────────────────────┐
│      First Half              │
│        25:00                 │
└──────────────────────────────┘
```

**After:**
```
┌──────────────────────────────┐
│      First Half              │
│      Scoreboard              │
│          3                   │ ← New scoreboard
│        25:00                 │
└──────────────────────────────┘
```

### Button States

#### Match Running (Enabled)
```
┌────────────────────────────────┐
│       ⚽ Add Goal              │ ← Green/Primary color
└────────────────────────────────┘
```

#### Match Paused (Disabled)
```
┌────────────────────────────────┐
│       ⚽ Add Goal              │ ← Gray/Disabled
└────────────────────────────────┘
```

## Data Flow Diagram

```
User Action: Tap "Add Goal"
         ↓
    ViewModel.showGoalScorerDialog()
         ↓
    Show Dialog with Players List
         ↓
User Action: Tap Player "John Doe"
         ↓
    ViewModel.registerGoal(playerId)
         ↓
    RegisterGoalUseCase
         ↓
    Calculate match elapsed time
         ↓
    Create Goal object
         ↓
    GoalRepository.insertGoal()
         ↓
    GoalLocalDataSource.insertGoal()
         ↓
    GoalDao.insert()
         ↓
    Room Database
         ↓
    Goal stored in DB
         ↓
    GetGoalsForMatchUseCase triggered
         ↓
    Updated goals count
         ↓
    ViewModel updates UI state
         ↓
    Scoreboard displays new count
```

## Database Schema

### Goal Table

```
┌─────────────────────────────────────────┐
│              goal                       │
├─────────────────────────────────────────┤
│ id (PK, AUTOINCREMENT)                  │
│ matchId (FK → match.id)                 │
│ scorerId (FK → player.id)               │
│ goalTimeMillis                          │
│ matchElapsedTimeMillis                  │
└─────────────────────────────────────────┘
```

### Relationships

```
     match                goal              player
┌─────────────┐    ┌──────────────┐    ┌─────────────┐
│ id (PK)     │←───│ matchId (FK) │    │ id (PK)     │
│ ...         │    │ scorerId (FK)│───→│ firstName   │
└─────────────┘    │ ...          │    │ lastName    │
                   └──────────────┘    │ number      │
                                       └─────────────┘
```

## Code Structure

### Layer Organization

```
app/
└── ui/
    └── matches/
        └── CurrentMatchScreen.kt
            ├── GoalScorerSelectionDialog()
            └── MatchTimeCard(goalsCount)

viewmodel/
└── MatchViewModel.kt
    ├── registerGoal()
    ├── showGoalScorerDialog()
    └── dismissGoalScorerDialog()

usecase/
├── RegisterGoalUseCase.kt
└── GetGoalsForMatchUseCase.kt

data/
├── core/
│   ├── repository/
│   │   └── GoalRepositoryImpl.kt
│   └── datasource/
│       └── GoalLocalDataSource.kt
└── local/
    ├── dao/
    │   └── GoalDao.kt
    ├── datasource/
    │   └── GoalLocalDataSourceImpl.kt
    └── entity/
        └── GoalEntity.kt

domain/
└── model/
    └── Goal.kt
```

## State Management

### UI State Flow

```
Loading
   ↓
Success (goalsCount = 0)
   ↓
User adds goal
   ↓
Success (goalsCount = 1)
   ↓
User adds goal
   ↓
Success (goalsCount = 2)
```

### Dialog State Flow

```
showGoalScorerDialog = false
   ↓
User taps "Add Goal"
   ↓
showGoalScorerDialog = true
   ↓
Dialog appears
   ↓
User selects player or cancels
   ↓
showGoalScorerDialog = false
   ↓
Dialog disappears
```

## User Interactions

### Happy Path

1. ✅ Match is running
2. ✅ Coach sees "Add Goal" button (enabled)
3. ✅ Coach taps "Add Goal"
4. ✅ Dialog shows all players
5. ✅ Coach taps player who scored
6. ✅ Goal is registered
7. ✅ Dialog closes
8. ✅ Scoreboard updates
9. ✅ Coach sees new goal count

### Edge Cases Handled

1. ❌ Match is paused
   - "Add Goal" button is disabled
   - Cannot add goals

2. ❌ Match hasn't started
   - "Add Goal" button is disabled
   - Cannot add goals

3. ✅ Cancel dialog
   - Coach taps "Cancel"
   - Dialog closes
   - No goal is registered

4. ✅ Multiple goals
   - Coach can add multiple goals
   - Each increments the counter
   - Each associates with the selected player

## Testing Coverage

### Unit Tests Structure

```
RegisterGoalUseCaseTest
├── ✅ Records goal when match is running
├── ✅ Uses elapsed time when match is paused
├── ✅ Calculates correct elapsed time
└── ✅ Throws error when no match

GetGoalsForMatchUseCaseTest
├── ✅ Returns goals from repository
└── ✅ Returns empty list when no goals

MatchViewModelGoalTest
├── ✅ Shows goal scorer dialog
├── ✅ Dismisses goal scorer dialog
├── ✅ Registers goal and dismisses dialog
└── ✅ Goals count reflected in UI state
```

## Accessibility Features

- **Large Text**: Scoreboard uses large display font
- **Clear Labels**: "Scoreboard" label for context
- **Button States**: Visual feedback for enabled/disabled
- **List Navigation**: Scrollable player list
- **Modal Dialog**: Focus on player selection

## Performance Considerations

- **Flow-based**: Reactive updates using Kotlin Flow
- **Lazy Loading**: Player list uses LazyColumn
- **Minimal Recomposition**: Only scoreboard updates
- **Efficient Queries**: Indexed database queries

## Future Enhancements

Potential improvements for future iterations:

1. **Goal Details**
   - Add assist player
   - Add goal type (header, penalty, etc.)
   - Add video timestamp

2. **Statistics**
   - Top scorers list
   - Goals per match average
   - Goals by period analysis

3. **Undo Functionality**
   - Remove last goal
   - Edit goal scorer

4. **Notifications**
   - Celebrate goals with animation
   - Sound effects for goals

## Summary

This visual guide demonstrates how the goal tracking functionality integrates seamlessly into the existing match management UI. The implementation provides:

- **Clear Visual Feedback**: Scoreboard always visible
- **Simple Interaction**: One tap to add, one tap to select
- **Robust Data**: Proper time tracking and relationships
- **Quality Assurance**: Comprehensive test coverage

The feature enhances the coach's ability to track match progress in real-time while maintaining the application's clean, intuitive design.
