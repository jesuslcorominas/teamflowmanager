# US-2.1.8: Pausar un partido - Visual Guide

## UI Changes Overview

This document provides a visual description of the UI changes made for the pause match functionality.

## Current Match Screen - Before Implementation

### Match Running State
```
┌─────────────────────────────────────────┐
│        Tiempo de Partido                │
│                                         │
│         15:23  [ACTIVO]                 │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│         Tiempos de Jugadores            │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│ John Doe          [ACTIVO]  15:23       │
│ #10                                     │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│ Jane Smith                    7:45      │
│ #5                                      │
└─────────────────────────────────────────┘

         ┌───────────────────┐
         │ Finalizar partido │  ← Only button available
         └───────────────────┘
```

## Current Match Screen - After Implementation

### Match Running State (New Pause Button)
```
┌─────────────────────────────────────────┐
│        Tiempo de Partido                │
│                                         │
│         15:23  [ACTIVO]                 │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│         Tiempos de Jugadores            │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│ John Doe          [ACTIVO]  15:23       │
│ #10                                     │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│ Jane Smith        [ACTIVO]  15:23       │
│ #5                                      │
└─────────────────────────────────────────┘

         ┌───────────────────┐
         │    Descanso      │  ← NEW! Pause button
         └───────────────────┘
         
         ┌───────────────────┐
         │ Finalizar partido │
         └───────────────────┘
```

### Match Paused State (Button Hidden)
```
┌─────────────────────────────────────────┐
│        Tiempo de Partido                │
│                                         │
│         15:23                           │  ← No [ACTIVO] indicator
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│         Tiempos de Jugadores            │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│ John Doe                      15:23     │  ← No [ACTIVO] indicator
│ #10                                     │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│ Jane Smith                    15:23     │
│ #5                                      │
└─────────────────────────────────────────┘

                                              ← Pause button HIDDEN
         ┌───────────────────┐
         │ Finalizar partido │  ← Only button visible
         └───────────────────┘
```

## Button Behavior

### "Descanso" Button (New)

**Visibility:**
- ✅ Shown when: `matchIsRunning == true`
- ❌ Hidden when: `matchIsRunning == false`

**Action:**
- Calls `viewModel.pauseMatch()`
- Pauses match timer
- Pauses all active player timers
- Consolidates all elapsed times in database

**Label:**
- Spanish: "Descanso"
- English: "Half Time"

### "Finalizar partido" Button (Existing)

**Visibility:**
- ✅ Always shown (both running and paused states)

**Action:**
- Calls `viewModel.saveMatch()`
- Finishes the match permanently
- Match moves to "Played Matches" list

## User Flow

### Starting and Pausing a Match

```
1. Match List Screen
   │
   ├─→ User clicks "Empezar" on a match
   │
   └─→ Navigate to Current Match Screen

2. Current Match Screen (Running)
   │
   ├─→ Match timer running (e.g., 15:23 [ACTIVO])
   ├─→ Player timers running
   ├─→ "Descanso" button visible
   └─→ "Finalizar partido" button visible
   
3. User Action
   │
   └─→ User clicks "Descanso" button
   
4. Current Match Screen (Paused)
   │
   ├─→ Match timer stopped at 15:23 (no [ACTIVO])
   ├─→ Player timers stopped
   ├─→ "Descanso" button HIDDEN
   └─→ "Finalizar partido" button visible
```

## UI State Transitions

```
┌─────────────────┐
│  Match Created  │
└────────┬────────┘
         │ User clicks "Empezar"
         ↓
┌─────────────────┐     User clicks "Descanso"     ┌─────────────────┐
│  Match Running  │ ──────────────────────────────→ │  Match Paused   │
│                 │                                 │                 │
│ • Timer active  │                                 │ • Timer stopped │
│ • [ACTIVO] tags │                                 │ • No [ACTIVO]   │
│ • Pause button  │                                 │ • No pause btn  │
│ • Finish button │                                 │ • Finish button │
└─────────────────┘                                 └────────┬────────┘
         │                                                   │
         │                                                   │
         │           User clicks "Finalizar partido"        │
         └────────────────────────┬─────────────────────────┘
                                  ↓
                         ┌─────────────────┐
                         │ Match Finished  │
                         │                 │
                         │ → Moves to      │
                         │   "Played       │
                         │    Matches"     │
                         └─────────────────┘
```

## Visual Indicators

### Running State Indicators

**Match Time Card:**
```
┌─────────────────────────────────────────┐
│        Tiempo de Partido                │
│                                         │
│    15:23  ┌──────────┐                 │
│           │ ACTIVO   │  ← Red background
│           └──────────┘                  │
└─────────────────────────────────────────┘
```

**Player Time Card (Running):**
```
┌─────────────────────────────────────────┐
│ John Doe      ┌──────────┐      15:23   │
│ #10           │ ACTIVO   │              │
│               └──────────┘               │
└─────────────────────────────────────────┘
       ↑ Secondary container background
```

### Paused State Indicators

**Match Time Card:**
```
┌─────────────────────────────────────────┐
│        Tiempo de Partido                │
│                                         │
│           15:23                         │  ← No indicator
│                                         │
└─────────────────────────────────────────┘
```

**Player Time Card (Paused):**
```
┌─────────────────────────────────────────┐
│ John Doe                        15:23   │
│ #10                                     │
│                                         │
└─────────────────────────────────────────┘
       ↑ Surface background (not highlighted)
```

## Code Snippets

### Button Implementation
```kotlin
if (state.matchIsRunning) {
    Button(
        onClick = onPauseMatch,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(text = stringResource(R.string.pause_match_button))
    }
}
```

### String Resources

**Spanish (values-es/strings.xml):**
```xml
<string name="pause_match_button">Descanso</string>
```

**English (values/strings.xml):**
```xml
<string name="pause_match_button">Half Time</string>
```

## Accessibility Considerations

### Button Visibility Logic
The pause button uses conditional rendering based on match state:
- This prevents user confusion (can't pause what's not running)
- Reduces UI clutter in paused state
- Provides clear visual feedback of match state

### Visual Feedback
- **[ACTIVO]** badge provides immediate visual feedback of running state
- Color changes (red for active, normal for paused) enhance visibility
- Button presence/absence clearly indicates available actions

### Text Labels
- Clear, action-oriented labels ("Descanso" / "Half Time")
- Appropriate translations for Spanish and English audiences
- Consistent with existing button styles in the app

## Testing the UI

### Manual Testing Steps

1. **Verify button appears when running:**
   - Start a match
   - Check "Descanso" button is visible
   - Check button is clickable

2. **Verify button functionality:**
   - Click "Descanso" button
   - Verify match timer stops
   - Verify player timers stop
   - Verify [ACTIVO] indicators disappear

3. **Verify button hides when paused:**
   - After pausing
   - Check "Descanso" button is no longer visible
   - Check only "Finalizar partido" button remains

4. **Verify localization:**
   - Test in Spanish: Should show "Descanso"
   - Test in English: Should show "Half Time"

## Summary

The UI changes for US-2.1.8 are minimal and focused:
- **1 new button** - "Descanso" / "Half Time"
- **Conditional visibility** - Only shown when match is running
- **Clear visual feedback** - [ACTIVO] indicators show running/paused state
- **Consistent design** - Follows existing UI patterns in the app

The implementation maintains the clean, user-friendly interface of the app while adding essential functionality for managing match flow during half-time.
