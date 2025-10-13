# US-2.1.3/4: Player Substitution - Visual Guide

## Feature Overview

This feature allows coaches to register player substitutions during a match with automatic time control. When a substitution is made, the system automatically stops the outgoing player's timer and starts the incoming player's timer, ensuring precise time tracking.

## User Flow Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                     Current Match Screen                     │
│                                                              │
│  ┌────────────────────────────────────────────────────────┐ │
│  │             Match Time: 15:34                          │ │
│  │                [ACTIVE]                                │ │
│  └────────────────────────────────────────────────────────┘ │
│                                                              │
│  Player Times                                                │
│                                                              │
│  ┌────────────────────────────────────────────────────────┐ │
│  │ John Doe #10                        [ACTIVE]  8:24    │ │  ← Player is on field
│  └────────────────────────────────────────────────────────┘ │
│  ┌────────────────────────────────────────────────────────┐ │
│  │ Jane Smith #5                                  7:10    │ │  ← Player on field (paused)
│  └────────────────────────────────────────────────────────┘ │
│  ┌────────────────────────────────────────────────────────┐ │
│  │ Mike Johnson #7                                0:00    │ │  ← Substitute (not playing)
│  └────────────────────────────────────────────────────────┘ │
│                                                              │
│               [Pause]  [Stop]                                │
└─────────────────────────────────────────────────────────────┘

                         USER TAPS JOHN DOE
                                 ↓

┌─────────────────────────────────────────────────────────────┐
│                     Current Match Screen                     │
│                                                              │
│  ┌────────────────────────────────────────────────────────┐ │
│  │             Match Time: 15:34                          │ │
│  │                [ACTIVE]                                │ │
│  └────────────────────────────────────────────────────────┘ │
│                                                              │
│  Player Times                                                │
│  ℹ️ Tap on the player to bring in                           │  ← Instruction message
│                                                              │
│  ┌────────────────────────────────────────────────────────┐ │
│  │ John Doe #10                        [ACTIVE]  8:24    │ │  ← SELECTED (highlighted)
│  └────────────────────────────────────────────────────────┘ │
│  ┌────────────────────────────────────────────────────────┐ │
│  │ Jane Smith #5                                  7:10    │ │
│  └────────────────────────────────────────────────────────┘ │
│  ┌────────────────────────────────────────────────────────┐ │
│  │ Mike Johnson #7                                0:00    │ │  ← Can tap to substitute in
│  └────────────────────────────────────────────────────────┘ │
│                                                              │
│               [Pause]  [Stop]                                │
└─────────────────────────────────────────────────────────────┘

                      USER TAPS MIKE JOHNSON
                                 ↓

┌─────────────────────────────────────────────────────────────┐
│                     Current Match Screen                     │
│                                                              │
│  ┌────────────────────────────────────────────────────────┐ │
│  │             Match Time: 15:34                          │ │
│  │                [ACTIVE]                                │ │
│  └────────────────────────────────────────────────────────┘ │
│                                                              │
│  Player Times                                                │
│                                                              │
│  ┌────────────────────────────────────────────────────────┐ │
│  │ John Doe #10                                   8:24    │ │  ← Timer STOPPED
│  └────────────────────────────────────────────────────────┘ │
│  ┌────────────────────────────────────────────────────────┐ │
│  │ Jane Smith #5                                  7:10    │ │
│  └────────────────────────────────────────────────────────┘ │
│  ┌────────────────────────────────────────────────────────┐ │
│  │ Mike Johnson #7                     [ACTIVE]   0:00    │ │  ← Timer STARTED
│  └────────────────────────────────────────────────────────┘ │
│                                                              │
│               [Pause]  [Stop]                                │
└─────────────────────────────────────────────────────────────┘

                    ✅ SUBSTITUTION COMPLETE
```

## Interaction States

### State 1: Normal View (No Selection)
```
┌────────────────────────────────────────────────────────┐
│ Player Name #10                    [ACTIVE]   12:45   │  ← Running timer
└────────────────────────────────────────────────────────┘
  Background: secondaryContainer (light green)

┌────────────────────────────────────────────────────────┐
│ Player Name #5                                 08:30   │  ← Stopped timer
└────────────────────────────────────────────────────────┘
  Background: surface (white)

┌────────────────────────────────────────────────────────┐
│ Player Name #7                                 00:00   │  ← Never played
└────────────────────────────────────────────────────────┘
  Background: surface (white)
```

### State 2: Player Selected for Substitution
```
┌────────────────────────────────────────────────────────┐
│ Player Name #10                    [ACTIVE]   12:45   │  ← SELECTED
└────────────────────────────────────────────────────────┘
  Background: primaryContainer (blue)
  User can tap same player to cancel or tap another to substitute

ℹ️ Tap on the player to bring in
   (Message appears above player list)
```

## Data Flow

```
┌─────────────────┐
│   UI Layer      │
│ CurrentMatch    │
│   Screen        │
└────────┬────────┘
         │ 1. User taps Player Out (ID: 2)
         ↓
┌─────────────────┐
│  ViewModel      │
│ MatchViewModel  │
│                 │
│ selectPlayerOut │
│     (2)         │
└────────┬────────┘
         │ 2. User taps Player In (ID: 3)
         ↓
┌─────────────────┐
│  ViewModel      │
│ MatchViewModel  │
│                 │
│ substitutePlayer│
│     (3)         │
└────────┬────────┘
         │ 3. Call use case with:
         │    matchId: 1
         │    playerOutId: 2
         │    playerInId: 3
         │    currentTime: System.currentTimeMillis()
         ↓
┌─────────────────┐
│  Use Case       │
│   Register      │
│ Substitution    │
└────────┬────────┘
         │
         │ 4. Get match to calculate elapsed time
         ↓
┌─────────────────┐      ┌─────────────────┐
│  Repository     │◄────►│  Database       │
│    Match        │      │  match table    │
└─────────────────┘      └─────────────────┘
         │
         │ 5. Stop player out timer (ID: 2)
         ↓
┌─────────────────┐      ┌─────────────────┐
│  Repository     │◄────►│  Database       │
│  PlayerTime     │      │ player_time     │
└─────────────────┘      └─────────────────┘
         │
         │ 6. Start player in timer (ID: 3)
         ↓
┌─────────────────┐      ┌─────────────────┐
│  Repository     │◄────►│  Database       │
│  PlayerTime     │      │ player_time     │
└─────────────────┘      └─────────────────┘
         │
         │ 7. Insert substitution record
         ↓
┌─────────────────┐      ┌─────────────────┐
│  Repository     │◄────►│  Database       │
│ Substitution    │      │ player_         │
│                 │      │ substitution    │
└─────────────────┘      └─────────────────┘
         │
         │ 8. Update UI with new state
         ↓
┌─────────────────┐
│   UI Layer      │
│ CurrentMatch    │
│   Screen        │
└─────────────────┘
```

## Database Schema

### player_substitution Table
```
┌─────────────────────────┬──────────┬──────────┬──────────┐
│ Column Name             │ Type     │ Nullable │ Key      │
├─────────────────────────┼──────────┼──────────┼──────────┤
│ id                      │ INTEGER  │ NO       │ PRIMARY  │
│ matchId                 │ INTEGER  │ NO       │ FOREIGN  │
│ playerOutId             │ INTEGER  │ NO       │ FOREIGN  │
│ playerInId              │ INTEGER  │ NO       │ FOREIGN  │
│ substitutionTimeMillis  │ INTEGER  │ NO       │          │
│ matchElapsedTimeMillis  │ INTEGER  │ NO       │          │
└─────────────────────────┴──────────┴──────────┴──────────┘

Indexes:
- index_player_substitution_matchId
- index_player_substitution_playerOutId
- index_player_substitution_playerInId

Foreign Keys:
- matchId → match(id) ON DELETE CASCADE
- playerOutId → player(id) ON DELETE CASCADE
- playerInId → player(id) ON DELETE CASCADE
```

### Example Data
```sql
-- Match started at 14:00:00
-- First substitution at 14:08:24 (match time: 8:24)
INSERT INTO player_substitution VALUES (
    1,                      -- id
    1,                      -- matchId
    2,                      -- playerOutId (John Doe)
    7,                      -- playerInId (Mike Johnson)
    1704982104000,          -- substitutionTimeMillis (2024-01-11 14:08:24)
    504000                  -- matchElapsedTimeMillis (8:24 in milliseconds)
);

-- Second substitution at 14:20:15 (match time: 20:15)
INSERT INTO player_substitution VALUES (
    2,                      -- id
    1,                      -- matchId
    5,                      -- playerOutId (Jane Smith)
    2,                      -- playerInId (John Doe - coming back)
    1704982815000,          -- substitutionTimeMillis (2024-01-11 14:20:15)
    1215000                 -- matchElapsedTimeMillis (20:15 in milliseconds)
);
```

## Color Scheme

### Player Card States
```
┌──────────────────────────────────────────────────────────┐
│                    Card Background Colors                 │
├──────────────────────────────────────────────────────────┤
│                                                           │
│  SELECTED FOR SUBSTITUTION                                │
│  ┌────────────────────────────────────────────┐          │
│  │  Background: primaryContainer (blue)       │          │
│  │  Border: None                              │          │
│  └────────────────────────────────────────────┘          │
│                                                           │
│  TIMER RUNNING (Player on field)                         │
│  ┌────────────────────────────────────────────┐          │
│  │  Background: secondaryContainer (green)    │          │
│  │  Badge: [ACTIVE] in error color (red)     │          │
│  └────────────────────────────────────────────┘          │
│                                                           │
│  TIMER STOPPED (Player not on field)                     │
│  ┌────────────────────────────────────────────┐          │
│  │  Background: surface (white)               │          │
│  │  Badge: None                               │          │
│  └────────────────────────────────────────────┘          │
│                                                           │
└──────────────────────────────────────────────────────────┘
```

## Time Calculation Examples

### Example 1: Substitution When Match Is Running
```
Match started: 14:00:00
Current time:  14:08:24
Match elapsed: 300000ms (5:00) + (14:08:24 - 14:05:00) = 504000ms (8:24)

Substitution record:
- substitutionTimeMillis: 1704982104000 (14:08:24)
- matchElapsedTimeMillis: 504000 (8:24)
```

### Example 2: Substitution When Match Is Paused
```
Match started:  14:00:00
Match paused:   14:15:00 (elapsed: 900000ms = 15:00)
Current time:   14:20:00 (5 minutes after pause)
Match elapsed:  900000ms (15:00) - no additional time since paused

Substitution record:
- substitutionTimeMillis: 1704983200000 (14:20:00)
- matchElapsedTimeMillis: 900000 (15:00)
```

## API Contract

### ViewModel Public API
```kotlin
class MatchViewModel {
    // State
    val uiState: StateFlow<MatchUiState>
    val selectedPlayerOut: StateFlow<Long?>
    
    // Actions
    fun selectPlayerOut(playerId: Long)
    fun clearPlayerOutSelection()
    fun substitutePlayer(playerInId: Long)
    fun pauseMatch()
    fun resumeMatch()
    fun saveMatch()
}
```

### Use Case Public API
```kotlin
interface RegisterPlayerSubstitutionUseCase {
    suspend operator fun invoke(
        matchId: Long,
        playerOutId: Long,
        playerInId: Long,
        currentTimeMillis: Long,
    )
}

interface GetMatchSubstitutionsUseCase {
    operator fun invoke(matchId: Long): Flow<List<PlayerSubstitution>>
}
```

## Error Scenarios

### Scenario 1: No Active Match
```
User attempts substitution → Use case checks for active match
→ requireNotNull throws IllegalArgumentException
→ App should prevent this by disabling UI when no match active
```

### Scenario 2: Database Error
```
User attempts substitution → Database insert fails
→ Exception caught in repository
→ UI shows error message (not implemented yet)
→ Timers remain in previous state
```

### Scenario 3: Rapid Taps
```
User rapidly taps multiple players
→ First tap: selects player out
→ Second tap (same player): cancels selection
→ Third tap (different player): selects new player out
→ Fourth tap: completes substitution
→ No duplicate substitutions recorded
```

## Accessibility Considerations

### Touch Targets
- All player cards are full-width for easy tapping
- Minimum height of 64dp ensures comfortable touch target
- Cards are spaced with 8dp gap for clear separation

### Visual Feedback
- Color changes provide immediate visual feedback
- [ACTIVE] badge clearly indicates which players are on field
- Message text guides user through substitution process

### Screen Readers
- Player cards should be announced as "Player Name, Number X, Time Y:YY, [Running/Stopped]"
- Selection state should be announced: "Selected for substitution"
- Action completion should be announced: "Substitution completed"

## Internationalization

### Supported Languages
- English (en)
- Spanish (es)

### String Keys
```xml
<!-- English -->
<string name="select_player_in_message">Tap on the player to bring in</string>

<!-- Spanish -->
<string name="select_player_in_message">Toca al jugador que entra al campo</string>
```

## Future UI Enhancements

### Substitution History Panel
```
┌─────────────────────────────────────────────┐
│         Substitutions (3)                   │
├─────────────────────────────────────────────┤
│  8:24  OUT: John Doe #10                   │
│        IN:  Mike Johnson #7                 │
├─────────────────────────────────────────────┤
│ 20:15  OUT: Jane Smith #5                  │
│        IN:  John Doe #10                    │
├─────────────────────────────────────────────┤
│ 35:42  OUT: Mike Johnson #7                │
│        IN:  Sarah Lee #8                    │
└─────────────────────────────────────────────┘
```

### Undo Functionality
```
┌─────────────────────────────────────────────┐
│  Substitution completed!                    │
│  OUT: John Doe #10                         │
│  IN:  Mike Johnson #7                       │
│                                             │
│  [Undo] (available for 30 seconds)          │
└─────────────────────────────────────────────┘
```

### Match Statistics
```
┌─────────────────────────────────────────────┐
│         Match Statistics                    │
├─────────────────────────────────────────────┤
│  Total Substitutions: 5                     │
│  Most Substituted: John Doe (2 times)       │
│  Longest On Field: Jane Smith (42:18)       │
└─────────────────────────────────────────────┘
```

## Testing Scenarios

### Manual Test Script
```
Test Case: Basic Substitution
1. Start match with 11 players in starting lineup
2. Verify all timers show 0:00:00
3. Wait 5 seconds → verify all timers show 0:00:05
4. Tap Player #10 (currently on field)
5. VERIFY: Card highlights in blue
6. VERIFY: Message "Tap on the player to bring in" appears
7. Tap Player #7 (on bench)
8. VERIFY: Player #10 timer stops at current value
9. VERIFY: Player #7 timer starts from 0:00:00
10. VERIFY: Message disappears
11. VERIFY: Player #10 card no longer highlighted
12. Wait 5 seconds → verify Player #7 timer shows 0:00:05
13. PASS ✅

Test Case: Cancellation
1. Tap Player #10
2. VERIFY: Card highlights
3. Tap Player #10 again (same player)
4. VERIFY: Card no longer highlighted
5. VERIFY: Message disappears
6. PASS ✅

Test Case: Multiple Substitutions
1. Complete substitution: Player #10 → Player #7
2. Complete substitution: Player #5 → Player #8
3. Complete substitution: Player #3 → Player #10
4. VERIFY: All timers are accurate
5. VERIFY: Database has 3 substitution records
6. PASS ✅
```

---

**Note**: This visual guide is intended for development and QA teams. Actual UI screenshots should be captured during manual testing and added to this document.
