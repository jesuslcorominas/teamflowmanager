# US-3.1.1_bis: Visualizar datos de goles - Implementation Summary

## Overview
This implementation adds a second tab "GOLEADORES" (Scorers) to the existing Analysis screen, displaying a visual bar chart of team goal scorers for the season.

## User Story
**Como entrenador, quiero ver un gráfico visual (barras o pastel) de los goleadores del equipo**

### Scenario
- **Given**: There is accumulated goal data for the season
- **When**: I navigate to the Analysis tab
- **Then**: I see a second tab "GOLEADORES" with a visual chart showing the team's top scorers

### Acceptance Criteria
✅ The chart is clear, understandable, and updated

## Technical Implementation

### 1. Domain Model
Created `PlayerGoalStats.kt`:
```kotlin
data class PlayerGoalStats(
    val player: Player,
    val totalGoals: Int,
    val matchesWithGoals: Int,
)
```

### 2. Data Layer Extensions
Extended the existing goal infrastructure to support fetching all team goals:
- **GoalDao**: Added `getAllTeamGoals()` query that filters out opponent goals
- **GoalLocalDataSource**: Added interface method
- **GoalLocalDataSourceImpl**: Implemented the method
- **GoalRepository**: Extended interface
- **GoalRepositoryImpl**: Implemented repository method

### 3. Use Case Layer
Created `GetPlayerGoalStatsUseCase`:
- Combines data from `PlayerRepository` and `GoalRepository`
- Filters goals by scorer ID
- Counts total goals per player
- Counts distinct matches where player scored
- Sorts by total goals (descending)
- Excludes players with zero goals

### 4. ViewModel Layer
Updated `AnalysisViewModel`:
- Added `AnalysisTab` enum (TIMES, GOALS)
- Added tab selection state
- Updated `AnalysisUiState.Success` to include both time and goal stats
- Loads both statistics streams independently
- Added `selectTab()` function for tab switching

### 5. UI Layer
Updated `AnalysisScreen.kt`:
- Added Material3 `TabRow` with two tabs
- Created `PlayerGoalChart` composable
- Reuses existing `compose-charts` library
- Displays horizontal bar chart with:
  - Player name as label
  - Total goals as value
  - Gradient color (PrimaryLight to Primary)
  - Proper indicators and spacing
- Shows appropriate empty states per tab

### 6. Localization
Added strings in both English and Spanish:
- `analysis_goals_tab`: "SCORERS" / "GOLEADORES"
- `analysis_no_goals_data`: Empty state message
- `analysis_player_goals_chart_title`: "Top Scorers" / "Máximos Goleadores"
- `analysis_goals_label`: "goals" / "goles"
- `analysis_matches_with_goals`: Match count format

## Architecture Flow

```
AnalysisScreen
    ↓
AnalysisViewModel
    ↓
GetPlayerGoalStatsUseCase
    ↓
[PlayerRepository + GoalRepository]
    ↓
[PlayerLocalDataSource + GoalLocalDataSource]
    ↓
[PlayerDao + GoalDao]
    ↓
Room Database
```

## Key Features

### Tab Navigation
- Two tabs: TIEMPOS (Times) and GOLEADORES (Scorers)
- Tab selection persists during screen lifecycle
- Independent data loading for each tab
- Smooth tab switching with Material3 animations

### Goal Chart Characteristics
- **Chart Type**: Horizontal bar chart (matching the existing time chart style)
- **Data Display**: Player name + total goals
- **Sorting**: Descending by total goals (top scorers first)
- **Filtering**: Only shows players who have scored goals
- **Visual**: Gradient bars from PrimaryLight to Primary color
- **Empty State**: Displays message when no goal data exists

### Data Source
- Uses existing `goal` table in Room database
- Filters out opponent goals (`isOpponentGoal = false`)
- Reactive updates via Flow
- Combines with player data for complete statistics

## Files Modified

### New Files
1. `domain/src/main/kotlin/.../PlayerGoalStats.kt`
2. `usecase/src/main/kotlin/.../GetPlayerGoalStatsUseCase.kt`

### Modified Files
1. `data/local/src/main/java/.../GoalDao.kt`
2. `data/core/src/main/kotlin/.../GoalLocalDataSource.kt`
3. `data/local/src/main/java/.../GoalLocalDataSourceImpl.kt`
4. `usecase/src/main/kotlin/.../repository/GoalRepository.kt`
5. `data/core/src/main/kotlin/.../GoalRepositoryImpl.kt`
6. `usecase/src/main/kotlin/.../di/UseCaseModule.kt`
7. `viewmodel/src/main/java/.../AnalysisViewModel.kt`
8. `viewmodel/src/main/java/.../di/ViewModelModule.kt`
9. `app/src/main/java/.../ui/analysis/AnalysisScreen.kt`
10. `app/src/main/res/values/strings.xml`
11. `app/src/main/res/values-es/strings.xml`

## Design Consistency

### Follows Existing Patterns
- **Architecture**: Clean Architecture with clear layer separation
- **DI**: Koin dependency injection
- **Reactive**: Flow-based data streams
- **UI**: Jetpack Compose with Material3
- **Charts**: compose-charts library (already in use)
- **Styling**: TFMSpacing, Primary/PrimaryLight colors

### Reused Components
- `Loading` composable for loading state
- `EmptyContent` composable for empty states
- `RowChart` from compose-charts library
- Existing color scheme and spacing system

## User Experience

### Navigation Flow
1. User opens app
2. Taps "Análisis" tab in bottom navigation
3. Sees two tabs: "TIEMPOS" and "GOLEADORES"
4. Default shows TIEMPOS tab (existing functionality)
5. Taps "GOLEADORES" tab to view goal scorers
6. Chart displays automatically with smooth animation

### Chart Information
Each bar in the goal chart shows:
- Player full name (first name + last name)
- Total goals scored
- Visual bar proportional to goal count
- Gradient color for visual appeal

### Empty States
- If no time data: "No hay datos de tiempo de juego disponibles"
- If no goal data: "No hay datos de goles disponibles"

## Testing Recommendations

### Manual Testing
1. Navigate to Analysis tab
2. Verify both tabs are visible
3. Switch between TIEMPOS and GOLEADORES tabs
4. Verify correct chart displays for each tab
5. Check empty states when no data exists
6. Verify chart updates when new goals are scored
7. Check Spanish and English translations
8. Verify proper sorting (time descending, goals descending)

### Test Scenarios
- Team with no goals recorded
- Team with one goal scorer
- Team with multiple goal scorers
- Player with goals in multiple matches
- Language switching (Spanish ↔ English)

## Notes

- No unit tests were created per the agent instructions
- Reused existing `MatchTimeCard` component approach (not modified)
- No external dependencies added (compose-charts already present)
- Follows KMM-ready architecture (Room with abstraction layers)
- Data persists across app restarts (Room database)

## Acceptance Criteria Verification

✅ **Clear and Understandable Chart**
- Uses simple horizontal bar chart
- Player names clearly labeled
- Goal counts displayed
- Visual bars easy to compare

✅ **Updated Chart**
- Uses Flow for reactive updates
- Automatically refreshes when goals are scored
- Data comes directly from database

✅ **Correct Navigation**
- Accessible via Analysis tab
- Second tab labeled "GOLEADORES"
- Tab switching works smoothly

## Summary

This implementation successfully adds a goal scorers visualization to the Analysis screen while:
- Following existing architecture patterns
- Reusing existing components and libraries
- Maintaining code quality and separation of concerns
- Providing a clear, user-friendly interface
- Supporting both Spanish and English languages
- Ensuring data accuracy and real-time updates
