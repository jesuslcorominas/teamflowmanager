# US-1.2.3: Visualizar tiempo acumulado en tiempo real - Implementation Summary

## Overview
This implementation provides a real-time visualization of accumulated time for players and the match during an active session. Coaches can see exact times for each player with active timers and the total match time, all automatically updated every second.

## User Story
**Como entrenador**, quiero ver el tiempo exacto acumulado por cada jugador en tiempo real durante una sesión para tomar mejores decisiones.

### Acceptance Criteria
✅ Clear and automatically updated visualization
✅ Includes all players with active timers
✅ Shows exact accumulated time per player
✅ Shows total match time

## Technical Implementation

### Architecture
The implementation follows clean architecture principles with minimal changes:

```
┌─────────────────────────────────────┐
│         MatchViewModel              │
│  - Combines Match + PlayerTimes     │
│  - Calculates real-time values      │
│  - Updates every 1 second           │
└──────────────┬──────────────────────┘
               │
       ┌───────┴────────┐
       │                │
┌──────▼──────┐  ┌─────▼─────────┐
│ GetMatch    │  │ GetAllPlayer  │
│ UseCase     │  │ TimesUseCase  │
└─────────────┘  └───────────────┘
```

### Components Created

#### 1. ViewModel Layer (`viewmodel` module)

**MatchViewModel.kt**: Main ViewModel for session screen
- Observes match state from `GetMatchUseCase`
- Observes all player times from `GetAllPlayerTimesUseCase`
- Observes players from `GetPlayersUseCase`
- Combines all data streams using `combine` operator
- Updates current time every second using coroutine delay loop
- Calculates real-time elapsed time for running timers:
  ```kotlin
  currentTime = if (isRunning && lastStartTime != null) {
      elapsedTime + (currentTimeMillis - lastStartTime)
  } else {
      elapsedTime
  }
  ```

**Data Classes**:
- `PlayerTimeItem`: UI model combining Player + time info
- `MatchUiState`: Sealed class for UI states (Loading, NoMatch, Success)

#### 2. UI Layer (`app` module)

**SessionScreen.kt**: Composable screen for session visualization
- **MatchTimeCard**: Large card showing total match time
  - Displays time in MM:SS format
  - Shows "ACTIVE" badge when timer is running
  - Uses primary container color for emphasis
- **PlayerTimeCard**: Card for each player showing:
  - Player name and number
  - Accumulated time in MM:SS format
  - "ACTIVE" badge for running timers
  - Different background color for active timers (secondaryContainer)
- Uses `LazyColumn` for efficient scrolling of player list
- Automatically updates via `collectAsState()`

**TimeFormatter.kt**: Utility function
```kotlin
fun formatTime(timeMillis: Long): String {
    val totalSeconds = timeMillis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
```

#### 3. Navigation Integration

**MainScreen.kt**: Updated navigation logic
- Added `showSession` state variable
- Conditional rendering: TeamScreen → PlayersScreen → SessionScreen
- Imports SessionScreen component

**PlayersScreen.kt**: Added navigation button
- Added FAB button with play icon to navigate to session
- Uses `Icons.Outlined.PlayArrow`
- Arranged FABs in a Row at bottom-right
- Passes `onNavigateToSession` callback

#### 4. Resources

**strings.xml** (English):
```xml
<string name="session_title">Match Session</string>
<string name="no_match_message">No active match</string>
<string name="match_time_label">Match Time</string>
<string name="player_times_title">Player Times</string>
<string name="running_indicator">ACTIVE</string>
<string name="player_number_format">#%d</string>
```

**strings.xml** (Spanish):
```xml
<string name="session_title">Sesión de Partido</string>
<string name="no_match_message">No hay partido activo</string>
<string name="match_time_label">Tiempo de Partido</string>
<string name="player_times_title">Tiempos de Jugadores</string>
<string name="running_indicator">ACTIVO</string>
<string name="player_number_format">#%d</string>
```

### Key Features

#### Real-Time Updates
The implementation uses a coroutine that updates every second:
```kotlin
private fun startTimeUpdater() {
    viewModelScope.launch {
        while (isActive) {
            delay(1000)
            _currentTime.value = System.currentTimeMillis()
        }
    }
}
```

This triggers recalculation of all running timers through the `combine` operator.

#### Time Calculation Logic
For each timer (match or player):
1. If NOT running: Display `elapsedTimeMillis` as-is
2. If running: Display `elapsedTimeMillis + (currentTime - lastStartTimeMillis)`

This ensures accurate real-time display without persisting to database every second.

#### UI Design
- **Clear hierarchy**: Match time at top, player times below
- **Visual indicators**: "ACTIVE" badges for running timers
- **Color coding**: Different background colors for active vs. paused timers
- **Readable format**: MM:SS format for all times
- **Scalable**: LazyColumn handles any number of players efficiently

### Testing

#### Unit Tests Created

**MatchViewModelTest.kt** (7 test cases):
1. ✅ Initial state should be Loading
2. ✅ UI state should be NoMatch when match is null
3. ✅ UI state should be Success when match exists
4. ✅ Success state should include players without timer
5. ✅ Running match time should be calculated correctly
6. ✅ Running player time should be calculated correctly
7. ✅ Time should update every second

All tests use MockK, Kotlin Coroutines Test, and JUnit as per project standards.

### Dependency Injection

**ViewModelModule.kt**: Updated with MatchViewModel
```kotlin
viewModel {
    MatchViewModel(
        getMatchUseCase = get(),
        getAllPlayerTimesUseCase = get(),
        getPlayersUseCase = get(),
    )
}
```

## Files Created/Modified

### Created (4 files):
1. `viewmodel/MatchViewModel.kt` - ViewModel (116 lines)
2. `app/ui/session/SessionScreen.kt` - Session screen UI (253 lines)
3. `app/ui/util/TimeFormatter.kt` - Time formatting utility (7 lines)
4. `viewmodel/test/MatchViewModelTest.kt` - Unit tests (210 lines)

### Modified (5 files):
1. `viewmodel/di/ViewModelModule.kt` - Added MatchViewModel DI
2. `app/ui/main/MainScreen.kt` - Added session navigation
3. `app/ui/players/PlayersScreen.kt` - Added FAB to navigate to session
4. `app/res/values/strings.xml` - Added English strings
5. `app/res/values-es/strings.xml` - Added Spanish strings

**Total**: 4 new files, 5 modified files

## Technical Compliance
✅ Follows clean architecture principles
✅ Minimal changes - only essential components added
✅ Uses existing use cases (no new business logic needed)
✅ Proper dependency injection with Koin
✅ Unit tests with MockK and JUnit
✅ Reactive UI with Kotlin Flow and Compose
✅ Material Design 3 components
✅ Internationalization (English + Spanish)

## Usage Flow

1. **User opens app** → Sees TeamScreen or PlayersScreen (if team exists)
2. **User taps play FAB** → Navigates to SessionScreen
3. **SessionScreen loads** → MatchViewModel fetches match, players, and player times
4. **Display updates** → Every second, all running timers recalculate
5. **Coach monitors** → Can see exact times for each player and match

## Edge Cases Handled

1. **No active match**: Shows "No active match" message
2. **No players**: Shows empty list (handled by LazyColumn)
3. **Players without timers**: Shows 00:00 for those players
4. **Paused timers**: Shows accumulated time without live updates
5. **Running timers**: Shows live-updating accumulated time

## Example Scenarios

### Scenario 1: Mid-Match with Active Timers
```
Match Time: 15:30 [ACTIVE]

Player Times:
┌──────────────────────────────┐
│ John Doe #10                 │
│ 07:45 [ACTIVE]              │
└──────────────────────────────┘
┌──────────────────────────────┐
│ Jane Smith #8                │
│ 08:00                        │
└──────────────────────────────┘
```

### Scenario 2: Paused Match
```
Match Time: 20:00

Player Times:
┌──────────────────────────────┐
│ John Doe #10                 │
│ 10:00                        │
└──────────────────────────────┘
┌──────────────────────────────┐
│ Jane Smith #8                │
│ 10:00                        │
└──────────────────────────────┘
```

## Performance Considerations

1. **Efficient updates**: Only recalculates when `_currentTime` changes (1/second)
2. **LazyColumn**: Efficiently handles large player lists
3. **No database writes**: Time calculation is pure computation
4. **Coroutine-based**: Non-blocking updates using structured concurrency
5. **Flow combining**: Reactive updates only when data changes

## Future Enhancements (Out of Scope)

- Timer controls (start/pause) directly from session screen
- Filters to show only active players
- Sort options (by time, name, number)
- Match statistics summary
- Export session data
- Historical session comparison

## Acceptance Criteria Verification

| Criteria | Status | Implementation |
|----------|--------|----------------|
| Clear visualization | ✅ | Material Design cards with clear typography |
| Automatically updated | ✅ | Updates every second via coroutine |
| Includes all players with active timers | ✅ | Shows all players, highlights active ones |
| Exact accumulated time | ✅ | Calculates precisely using milliseconds |
| Real-time display | ✅ | Live updates for running timers |

## Conclusion

This implementation provides a complete, production-ready solution for real-time time visualization during match sessions. The architecture is clean, the code is tested, and the UI is intuitive. The feature seamlessly integrates with existing functionality while maintaining the project's architectural standards.

**Implementation Status**: ✅ **COMPLETE**
