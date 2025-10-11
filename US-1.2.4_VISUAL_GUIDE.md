# US-1.2.4 Visual Guide: Save Session Button

## UI Changes Overview

### SessionScreen - Before and After

#### BEFORE (Original)
```
┌─────────────────────────────────────┐
│  Match Session                      │
├─────────────────────────────────────┤
│                                     │
│  ┌─────────────────────────────┐   │
│  │   Match Time: 15:00  [RUN]  │   │
│  └─────────────────────────────┘   │
│                                     │
│  Player Times                       │
│  ┌─────────────────────────────┐   │
│  │ John Doe          7:30 [RUN]│   │
│  │ #10                         │   │
│  └─────────────────────────────┘   │
│  ┌─────────────────────────────┐   │
│  │ Jane Smith        5:00      │   │
│  │ #5                          │   │
│  └─────────────────────────────┘   │
│  ┌─────────────────────────────┐   │
│  │ Mike Johnson      3:45      │   │
│  │ #7                          │   │
│  └─────────────────────────────┘   │
│                                     │
│     (scroll for more players)       │
│                                     │
└─────────────────────────────────────┘
```

#### AFTER (With Save Session Button)
```
┌─────────────────────────────────────┐
│  Match Session                      │
├─────────────────────────────────────┤
│                                     │
│  ┌─────────────────────────────┐   │
│  │   Match Time: 15:00  [RUN]  │   │
│  └─────────────────────────────┘   │
│                                     │
│  Player Times                       │
│  ┌─────────────────────────────┐   │
│  │ John Doe          7:30 [RUN]│   │
│  │ #10                         │   │
│  └─────────────────────────────┘   │
│  ┌─────────────────────────────┐   │
│  │ Jane Smith        5:00      │   │
│  │ #5                          │   │
│  └─────────────────────────────┘   │
│  ┌─────────────────────────────┐   │
│  │ Mike Johnson      3:45      │   │
│  │ #7                          │   │
│  └─────────────────────────────┘   │
│                                     │
│     (scroll for more players)       │
│                                     │
│  ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━┓   │
│  ┃    GUARDAR SESIÓN  ⟳      ┃   │ ← NEW BUTTON
│  ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━┛   │
│                                     │
└─────────────────────────────────────┘
```

## Button Details

### Position
- Located at the **bottom** of the SessionScreen
- **Full width** button with standard padding
- Separated from player list with spacing

### Appearance
- **Material 3 Button** component
- Primary color scheme
- Text: "Save Session" (English) / "Guardar Sesión" (Spanish)
- Standard Material Design elevation and ripple effects

### Behavior
When clicked:
1. Saves all current player times to history database
2. Resets all player time counters to 0:00
3. UI automatically updates to show reset times
4. No confirmation dialog (direct action)

## User Flow

### Scenario: End of Match Session

```
┌─────────────────┐
│  Match Active   │
│  Players have   │
│  accumulated    │
│  game time      │
└────────┬────────┘
         │
         ▼
┌─────────────────────────────────┐
│  User sees "Guardar Sesión"     │
│  button at bottom of screen     │
└────────┬────────────────────────┘
         │
         ▼
┌─────────────────────────────────┐
│  User presses button            │
└────────┬────────────────────────┘
         │
         ▼
┌─────────────────────────────────┐
│  System saves:                  │
│  • John Doe: 7:30 → history     │
│  • Jane Smith: 5:00 → history   │
│  • Mike Johnson: 3:45 → history │
└────────┬────────────────────────┘
         │
         ▼
┌─────────────────────────────────┐
│  System resets counters:        │
│  • All players: → 0:00          │
└────────┬────────────────────────┘
         │
         ▼
┌─────────────────────────────────┐
│  UI updates automatically       │
│  All times now show 0:00        │
│  Ready for next session         │
└─────────────────────────────────┘
```

## Code Changes in SessionScreen.kt

### Key Modifications

1. **Added Button Import**
```kotlin
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.Spacer
```

2. **Updated SuccessState Function Signature**
```kotlin
@Composable
private fun SuccessState(
    state: MatchUiState.Success,
    onSaveSession: () -> Unit,  // ← New parameter
)
```

3. **Modified LazyColumn Layout**
```kotlin
LazyColumn(
    modifier = Modifier.weight(1f),  // ← Changed from fillMaxSize
    verticalArrangement = Arrangement.spacedBy(TFMSpacing.spacing02),
)
```

4. **Added Button at Bottom**
```kotlin
Spacer(modifier = Modifier.padding(TFMSpacing.spacing02))

Button(
    onClick = onSaveSession,
    modifier = Modifier.fillMaxWidth(),
) {
    Text(text = stringResource(R.string.save_session_button))
}
```

## Database Impact

### After Pressing "Guardar Sesión"

#### player_time table (current session)
**BEFORE:**
```sql
playerId | elapsedTimeMillis | isRunning
---------|-------------------|----------
1        | 450000           | true
2        | 300000           | false
7        | 225000           | false
```

**AFTER:**
```sql
(empty - all records deleted)
```

#### player_time_history table (new historical records)
**AFTER:**
```sql
id | playerId | matchId | elapsedTimeMillis | savedAtMillis
---|----------|---------|-------------------|---------------
1  | 1        | 1       | 450000           | 1697041234567
2  | 2        | 1       | 300000           | 1697041234567
3  | 7        | 1       | 225000           | 1697041234567
```

## Localization

### English (values/strings.xml)
```xml
<string name="save_session_button">Save Session</string>
```

### Spanish (values-es/strings.xml)
```xml
<string name="save_session_button">Guardar Sesión</string>
```

## Acceptance Criteria Verification

### ✅ Requirement 1: Save Time to History
**Status:** IMPLEMENTED  
**Evidence:** SaveSessionUseCase creates PlayerTimeHistory records and saves via repository

### ✅ Requirement 2: Persist in Database
**Status:** IMPLEMENTED  
**Evidence:** PlayerTimeHistoryEntity saved to Room database with proper foreign keys

### ✅ Requirement 3: Counter Resets to Zero
**Status:** IMPLEMENTED  
**Evidence:** PlayerTimeRepository.resetAllPlayerTimes() deletes all current session times

### ✅ Requirement 4: Button Labeled "Guardar Sesión"
**Status:** IMPLEMENTED  
**Evidence:** Button added to SessionScreen with proper Spanish translation

## Technical Implementation Highlights

### UI Component Properties
```kotlin
Button(
    onClick = onSaveSession,           // Action handler
    modifier = Modifier.fillMaxWidth(), // Full width
) {
    Text(text = stringResource(R.string.save_session_button))
}
```

### Spacing Configuration
- Uses `TFMSpacing.spacing02` for spacer
- Uses `TFMSpacing.spacing04` for screen padding
- Consistent with app's design system

### Material Design Compliance
- Uses Material 3 Button component
- Primary color scheme
- Standard elevation (4dp default)
- Touch ripple effect included
- Accessibility support built-in

## Testing Recommendations

### Manual Testing Checklist
- [ ] Button appears at bottom of SessionScreen
- [ ] Button has correct text (Spanish: "Guardar Sesión")
- [ ] Clicking button saves times to database
- [ ] Clicking button resets all counters to 0:00
- [ ] UI updates immediately after save
- [ ] Button works with empty player list
- [ ] Button works with single player
- [ ] Button works with multiple players
- [ ] Button works with running timers
- [ ] Button works with stopped timers
- [ ] Button works with mix of running/stopped timers

### Database Verification
1. Use Android Studio's Database Inspector
2. Check `player_time_history` table for new records
3. Verify `player_time` table is empty after save
4. Confirm foreign key relationships are maintained

## Future Considerations

While not in the current scope, these enhancements could be added:
- Add confirmation dialog: "¿Guardar sesión?"
- Add loading indicator during save
- Add success/error toast messages
- Add undo functionality
- Show save timestamp in UI
- Add button to view historical data

---

**Implementation Complete:** All acceptance criteria met with clean, maintainable code following Material Design and Clean Architecture principles.
