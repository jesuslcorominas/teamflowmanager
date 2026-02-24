# US-2.2.3: Añadir goles al marcador contrario - Visual Guide

## Overview
This guide shows the visual changes made to the TeamFlow Manager application to support adding goals to the opponent team's scoreboard.

## User Interface Changes

### 1. Scoreboard Display

**Before (US-2.2.2):**
```
┌─────────────────────────────────────┐
│         Primera Parte               │
│                                     │
│         Marcador                    │
│            2                        │  <- Single score, only my team
│                                     │
│         12:30                       │
└─────────────────────────────────────┘
```

**After (US-2.2.3):**
```
┌─────────────────────────────────────┐
│         Primera Parte               │
│                                     │
│         Marcador                    │
│                                     │
│  Mi Equipo    -    Rival            │
│     2                1              │  <- Dual scoreboard
│   (blue)          (red)             │
│                                     │
│         12:30                       │
└─────────────────────────────────────────┘
```

The scoreboard now displays both team scores:
- **Mi Equipo** (My Team) on the left in primary blue color
- **Rival** (Opponent) on the right in error red color
- Separated by a dash "-"

### 2. Goal Buttons

**Before (US-2.2.2):**
```
┌─────────────────────────────────────┐
│                                     │
│  [  Player Times List  ]            │
│                                     │
├─────────────────────────────────────┤
│                                     │
│     [ Añadir Gol ]                  │  <- Single button
│                                     │
│     [▶]        [■]                  │
│   (Play)     (Stop)                 │
│                                     │
└─────────────────────────────────────┘
```

**After (US-2.2.3):**
```
┌─────────────────────────────────────┐
│                                     │
│  [  Player Times List  ]            │
│                                     │
├─────────────────────────────────────┤
│                                     │
│  [Añadir Gol]  [Añadir Gol Rival]   │  <- Two buttons
│                                     │
│     [▶]        [■]                  │
│   (Play)     (Stop)                 │
│                                     │
└─────────────────────────────────────┘
```

The goal buttons are now side by side:
- **Añadir Gol** - Adds a goal for my team (opens player selection dialog)
- **Añadir Gol Rival** - Adds a goal for the opponent (shows confirmation dialog)
- Both buttons are only enabled when the match is running

### 3. Opponent Goal Confirmation Dialog

**New Dialog:**
```
┌─────────────────────────────────────────────┐
│ Añadir Gol al Marcador Contrario           │
├─────────────────────────────────────────────┤
│                                             │
│ ¿Quieres añadir un gol al equipo rival?    │
│                                             │
├─────────────────────────────────────────────┤
│                    [Cancelar]  [Añadir]     │
└─────────────────────────────────────────────┘
```

This confirmation dialog:
- Prevents accidental opponent goal registration
- Shows a clear message asking for confirmation
- Has Cancel and Add buttons

### 4. Complete Match View Flow

**Full Screen Layout:**
```
┌─────────────────────────────────────────────┐
│  ┌───────────────────────────────────────┐  │
│  │         Primera Parte                 │  │
│  │                                       │  │
│  │         Marcador                      │  │
│  │  Mi Equipo    -    Rival              │  │
│  │     2                1                │  │
│  │   (blue)          (red)               │  │
│  │                                       │  │
│  │         12:30                         │  │
│  └───────────────────────────────────────┘  │
│                                             │
│  Tiempos de Jugadores       [▼ Por Tiempo] │
│                                             │
│  ┌───────────────────────────────────────┐  │
│  │ 👕 #10 John Doe (C)     15:30 ACTIVO │  │
│  ├───────────────────────────────────────┤  │
│  │ 👕 #5  Jane Smith       12:00        │  │
│  ├───────────────────────────────────────┤  │
│  │ 👕 #7  Bob Jones        10:30 ACTIVO │  │
│  └───────────────────────────────────────┘  │
│                                             │
│  ┌─────────────────────────────────────┐    │
│  │  [Añadir Gol]  [Añadir Gol Rival]   │    │
│  └─────────────────────────────────────┘    │
│                                             │
│         [▶]         [■]                     │
│      (Play)      (Stop)                     │
│                                             │
└─────────────────────────────────────────────┘
```

## User Interaction Flow

### Scenario 1: Adding a Goal for My Team

1. Coach taps **"Añadir Gol"** button
2. Player selection dialog appears:
   ```
   ┌─────────────────────────────────────┐
   │ Seleccionar Goleador                │
   ├─────────────────────────────────────┤
   │ ┌─────────────────────────────────┐ │
   │ │ 10  John Doe                    │ │
   │ ├─────────────────────────────────┤ │
   │ │ 5   Jane Smith                  │ │
   │ ├─────────────────────────────────┤ │
   │ │ 7   Bob Jones                   │ │
   │ └─────────────────────────────────┘ │
   ├─────────────────────────────────────┤
   │                        [Cancelar]   │
   └─────────────────────────────────────┘
   ```
3. Coach taps on the player who scored
4. Goal is registered
5. Dialog closes
6. Scoreboard updates: **Mi Equipo: 3** - Rival: 1

### Scenario 2: Adding a Goal for Opponent Team

1. Coach taps **"Añadir Gol Rival"** button
2. Confirmation dialog appears:
   ```
   ┌─────────────────────────────────────┐
   │ Añadir Gol al Marcador Contrario    │
   ├─────────────────────────────────────┤
   │ ¿Quieres añadir un gol al equipo    │
   │ rival?                              │
   ├─────────────────────────────────────┤
   │              [Cancelar]  [Añadir]   │
   └─────────────────────────────────────┘
   ```
3. Coach taps **"Añadir"** to confirm
4. Opponent goal is registered
5. Dialog closes
6. Scoreboard updates: Mi Equipo: 2 - **Rival: 2**

## Visual Design Elements

### Color Coding

**Team Score (Mi Equipo):**
- Text color: `MaterialTheme.colorScheme.primary` (typically blue)
- Emphasizes "my team" score
- Matches app's primary color scheme

**Opponent Score (Rival):**
- Text color: `MaterialTheme.colorScheme.error` (typically red)
- Creates visual contrast with team score
- Makes opponent score immediately distinguishable

**Score Display:**
- Large display typography: `MaterialTheme.typography.displayLarge`
- Bold font weight for emphasis
- Centered alignment for balance

### Button Layout

**Goal Buttons:**
- Equal width (using `weight(1f)` modifier)
- Horizontal spacing: `TFMSpacing.spacing02`
- Full-width row container
- Enabled only when match is running
- Disabled state shows reduced opacity

### Scoreboard Layout

**Structure:**
```
Scoreboard Label (centered)
    ↓
┌────────────────────────────────────┐
│ Mi Equipo    -    Rival            │
│ ┌────────┐      ┌────────┐         │
│ │ Label  │      │ Label  │         │
│ │  #     │      │  #     │         │
│ └────────┘      └────────┘         │
└────────────────────────────────────┘
    ↓
Time Display
```

**Spacing:**
- Horizontal spacing between scores: `TFMSpacing.spacing04`
- Vertical padding: `TFMSpacing.spacing01`
- Labels use smaller typography: `bodySmall`
- Scores use large typography: `displayLarge`

## Accessibility Considerations

1. **Clear Labels:**
   - "Mi Equipo" and "Rival" clearly identify each score
   - Button text is descriptive: "Añadir Gol" vs "Añadir Gol Rival"

2. **Color + Text:**
   - Doesn't rely solely on color to distinguish scores
   - Labels provide context even without color

3. **Confirmation Dialog:**
   - Prevents accidental taps
   - Clear question and action buttons
   - Easy to cancel

4. **Button States:**
   - Disabled state when match is not running
   - Visual feedback prevents confusion

## Responsive Design

The layout adapts to different screen sizes:

**Small Screens:**
- Buttons stack properly with equal width
- Scoreboard remains centered and readable
- Text sizes scale appropriately

**Large Screens:**
- Scoreboard has comfortable spacing
- Buttons maintain proportion
- Labels and scores are clearly visible

## Comparison with Previous Implementation

| Aspect | Before (US-2.2.2) | After (US-2.2.3) |
|--------|------------------|------------------|
| Scoreboard | Single score | Dual score (Team - Opponent) |
| Goal Buttons | 1 button | 2 buttons |
| Goal Registration | Player selection for all goals | Player selection for team, confirmation for opponent |
| Color Coding | Single color (primary) | Dual colors (primary + error) |
| Visual Clarity | Shows only team performance | Shows full match result |
| User Actions | 2 taps for team goal | 2 taps for either goal type |

## Implementation Details

### Scoreboard Code Structure
```kotlin
// Team score
Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text("Mi Equipo")
    Text("$goalsCount", color = primary)
}

Spacer(width = spacing04)
Text("-")
Spacer(width = spacing04)

// Opponent score
Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text("Rival")
    Text("$opponentGoalsCount", color = error)
}
```

### Button Row Structure
```kotlin
Row(modifier = Modifier.fillMaxWidth()) {
    Button(
        onClick = onAddGoal,
        modifier = Modifier.weight(1f),
        enabled = isRunning
    ) {
        Text("Añadir Gol")
    }
    
    Button(
        onClick = onAddOpponentGoal,
        modifier = Modifier.weight(1f),
        enabled = isRunning
    ) {
        Text("Añadir Gol Rival")
    }
}
```

## Summary

The visual changes in US-2.2.3 provide:
1. **Clear dual scoreboard** showing both team and opponent scores
2. **Intuitive button layout** with two clearly labeled buttons
3. **Confirmation dialog** to prevent mistakes
4. **Color coding** for quick visual identification
5. **Consistent design** that follows Material Design principles

These changes make it easy for coaches to track and update both team and opponent scores during matches, providing a complete view of the match status at all times.
