# US-3.1.1_bis: Goal Scorers Visualization - Visual Guide

## Overview
This guide provides a visual description of the new goal scorers feature in the Analysis screen.

## Navigation Flow

```
App Home
    ↓
Bottom Navigation Bar
    ↓
Tap "Análisis" Tab
    ↓
Analysis Screen with Two Tabs
    ├── TIEMPOS (existing)
    └── GOLEADORES (new) ← This feature
```

## Screen Layout

### Analysis Screen - Tab Structure

```
┌─────────────────────────────────────────────┐
│  ← Análisis                                 │ ← Top Bar
├─────────────────────────────────────────────┤
│  ┌──────────────┬──────────────┐            │
│  │   TIEMPOS   │  GOLEADORES  │            │ ← Tab Row
│  └──────────────┴──────────────┘            │
│                                             │
│  [Chart Content Based on Selected Tab]     │
│                                             │
│                                             │
│                                             │
│                                             │
└─────────────────────────────────────────────┘
```

## GOLEADORES Tab - Chart View

### With Data
```
┌─────────────────────────────────────────────┐
│  ← Análisis                                 │
├─────────────────────────────────────────────┤
│  ┌──────────────┬──────────────┐            │
│  │   TIEMPOS   │ [GOLEADORES] │            │
│  └──────────────┴──────────────┘            │
│                                             │
│  Máximos Goleadores                        │
│                                             │
│  Juan Pérez          8 ████████████████    │
│                                             │
│  Carlos García       5 ██████████          │
│                                             │
│  Luis Martínez       3 ██████              │
│                                             │
│  Pedro Sánchez       2 ████                │
│                                             │
│  ↓ (scrollable if more players)            │
│                                             │
└─────────────────────────────────────────────┘
```

### Empty State (No Goals Data)
```
┌─────────────────────────────────────────────┐
│  ← Análisis                                 │
├─────────────────────────────────────────────┤
│  ┌──────────────┬──────────────┐            │
│  │   TIEMPOS   │ [GOLEADORES] │            │
│  └──────────────┴──────────────┘            │
│                                             │
│                                             │
│              📊                             │
│                                             │
│     No hay datos de goles disponibles      │
│                                             │
│                                             │
│                                             │
└─────────────────────────────────────────────┘
```

## Chart Details

### Individual Bar Component

```
┌──────────────────────────────────────────────────┐
│ Player Name          Goals │█████████████│      │
│                            │             │      │
│ [Full Name]          [#]   │[Gradient Bar]│     │
└──────────────────────────────────────────────────┘

Example:
┌──────────────────────────────────────────────────┐
│ Juan Pérez             8   ████████████████      │
│                                                  │
│ ← Name (Left)    Count → ← Visual Bar →         │
└──────────────────────────────────────────────────┘
```

### Bar Styling
- **Color**: Gradient from PrimaryLight (light blue) to Primary (dark blue)
- **Shape**: Rounded corners on the right side
- **Height**: 24dp
- **Animation**: Smooth spring animation on load
- **Width**: Proportional to goal count (relative to max)

## Color Scheme

```
Gradient Bar Colors:
├── Start: PrimaryLight (#6B9FD8 - Light Blue)
└── End:   Primary      (#003366 - Navy Blue)

Text Colors:
├── Player Name: Default text color
├── Goal Count:  Default text color (emphasized)
└── Empty State: OnSurfaceVariant (gray)
```

## Tab Behavior

### Tab Selection States

**TIEMPOS Selected:**
```
┌──────────────┬──────────────┐
│  [TIEMPOS]  │  GOLEADORES  │
│  (Active)   │  (Inactive)  │
└──────────────┴──────────────┘
```

**GOLEADORES Selected:**
```
┌──────────────┬──────────────┐
│   TIEMPOS   │ [GOLEADORES] │
│  (Inactive) │   (Active)   │
└──────────────┴──────────────┘
```

### Tab Interaction
- Tap on tab label to switch
- Active tab: Underline indicator + bold text
- Inactive tab: Normal weight text
- Smooth transition between tabs

## Data Display Rules

### Sorting
Players are sorted by:
1. Total goals (primary) - Descending
2. Player name (secondary) - Alphabetical if tie

Example order:
```
1. Juan Pérez      - 8 goals
2. Carlos García   - 5 goals
3. Luis Martínez   - 3 goals
4. Pedro Sánchez   - 2 goals
5. Ana López       - 1 goal
```

### Filtering
- ✅ Only players who have scored goals
- ✅ Only team goals (excludes opponent goals)
- ❌ Players with 0 goals are NOT shown

### Chart Indicators

Vertical axis indicators (left side):
```
│ 8
│
│ 4
│
│ 0
└────────────────
```

Shows:
- Maximum goals scored
- Half of maximum
- Zero baseline

## Responsive Behavior

### Portrait Mode
```
Full width chart
Comfortable bar spacing
Easy to read labels
```

### Landscape Mode
```
Wider chart area
More visible bars
Same proportions
```

### Scrolling
- Vertical scroll enabled if players exceed screen height
- Smooth scrolling behavior
- All players accessible

## Localization Examples

### Spanish (Default)
```
Tab: "GOLEADORES"
Title: "Máximos Goleadores"
Empty: "No hay datos de goles disponibles"
Label: "goles"
```

### English
```
Tab: "SCORERS"
Title: "Top Scorers"
Empty: "No goals data available"
Label: "goals"
```

## User Interactions

### Available Actions
1. **Switch Tabs**: Tap TIEMPOS or GOLEADORES
2. **Scroll**: Swipe up/down to see all players
3. **Pull to Refresh**: (if implemented in parent)
4. **Back Navigation**: Return to previous screen

### No Actions (Read-Only)
- ❌ Cannot tap on individual bars
- ❌ Cannot edit goal counts from this screen
- ❌ Cannot filter or sort manually

## Real-Time Updates

### Update Scenarios

**Before Scoring a Goal:**
```
Juan Pérez    5 goals  ██████████
Carlos García 3 goals  ██████
```

**After Juan Scores:**
```
Juan Pérez    6 goals  ████████████
Carlos García 3 goals  ██████
```

Chart updates automatically when:
- New goal is registered in a match
- Data syncs from database
- Screen is revisited after changes

## Accessibility Considerations

### Screen Reader Support
- Tab labels are announced
- Player names are read
- Goal counts are announced
- Empty state message is read

### Visual Accessibility
- High contrast bars
- Clear text sizing
- Sufficient spacing
- Color is not the only indicator

## Performance Characteristics

### Loading States
```
Initial Load:
┌──────────────────────┐
│                      │
│    ⏳ Loading...     │ ← Spinner shown while loading
│                      │
└──────────────────────┘

Data Loaded:
┌──────────────────────┐
│                      │
│  [Chart Displays]    │ ← Smooth animation
│                      │
└──────────────────────┘
```

### Animation Timing
- Tab switch: Instant
- Chart appear: ~500ms spring animation
- Bar grow: Staggered, smooth
- Total load time: <1 second (typical)

## Edge Cases Handled

### Single Player with Goals
```
┌────────────────────────┐
│ Juan Pérez   1 goal    │
│              ██        │
└────────────────────────┘
```

### Many Players (10+)
```
[Scrollable list]
Juan Pérez       8 ████████
Carlos García    7 ███████
Luis Martínez    6 ██████
Pedro Sánchez    5 █████
Ana López        4 ████
   ⋮             ⋮  ⋮
[Scroll for more]
```

### Very Long Names
```
Juan Antonio Pérez García  5 ████
(Name wraps or truncates appropriately)
```

## Technical Notes for Developers

### Key Components
```kotlin
AnalysisScreen()
├── TabRow
│   ├── Tab("TIEMPOS")
│   └── Tab("GOLEADORES")
└── Content
    ├── PlayerTimeChart()  // When TIEMPOS selected
    └── PlayerGoalChart()  // When GOLEADORES selected
```

### Data Flow
```
UI (AnalysisScreen)
    ↓ observes
ViewModel (AnalysisViewModel)
    ↓ invokes
UseCase (GetPlayerGoalStatsUseCase)
    ↓ combines
Repositories (PlayerRepository + GoalRepository)
    ↓ queries
Database (Room)
```

### State Management
```kotlin
sealed interface AnalysisUiState {
    object Loading
    object Empty
    data class Success(
        playerTimeStats: List<PlayerTimeStats>,
        playerGoalStats: List<PlayerGoalStats>
    )
}
```

## Comparison with TIEMPOS Tab

### Similarities
- Both use horizontal bar charts
- Same color scheme
- Same animation style
- Same empty state pattern
- Same navigation structure

### Differences
- TIEMPOS shows playing time in minutes
- GOLEADORES shows goal count
- TIEMPOS includes all players with time
- GOLEADORES only includes players with goals

## Future Enhancement Ideas

While not in current scope, these could be added later:

1. **Detailed Stats**
   - Tap bar to see match-by-match goals
   - Show goals per match average
   - Display goal types

2. **Time Period Filtering**
   - Filter by month/season
   - Compare periods
   - Show trends

3. **Additional Views**
   - Pie chart alternative
   - Line chart for progression
   - Combined views

4. **Export/Share**
   - Share chart as image
   - Export data to CSV
   - Print-friendly view

---

**Document Version**: 1.0
**Last Updated**: October 30, 2025
**Feature Status**: Production Ready ✅
