# US-1.1.7 Visual Guide: Team Information Display

## Feature Overview

This guide demonstrates the Team Information Display feature (US-1.1.7) that allows coaches to view general team information from the Team Roster section.

## User Flow

### Step 1: Team Roster View
When a coach is viewing the Team Roster (Plantilla), they will see:
- Team name in the centered top app bar
- **NEW**: Info icon button (ℹ️) in the top bar actions area
- List of players below (from previous features)

```
┌─────────────────────────────────────┐
│  ☰        Team Name            ℹ️  │ ← Info button added here
├─────────────────────────────────────┤
│                                     │
│  Player List                        │
│  - Player 1                         │
│  - Player 2                         │
│  - Player 3                         │
│  ...                                │
│                                     │
│                            [+]      │
└─────────────────────────────────────┘
```

### Step 2: Clicking the Info Button
When the coach taps the info button (ℹ️), a dialog appears overlaying the current screen.

```
┌─────────────────────────────────────┐
│  ☰        Team Name            ℹ️  │
├─────────────────────────────────────┤
│ ┌─────────────────────────────────┐ │
│ │  Team Info                      │ │
│ │                                 │ │
│ │  Team Name                      │ │
│ │  Los Halcones FC                │ │
│ │                                 │ │
│ │  Coach Name                     │ │
│ │  Juan García                    │ │
│ │                                 │ │
│ │  Delegate Name                  │ │
│ │  María López                    │ │
│ │                                 │ │
│ │                        [Close]  │ │
│ └─────────────────────────────────┘ │
│                                     │
└─────────────────────────────────────┘
```

### Step 3: Closing the Dialog
The coach can close the dialog by:
1. Clicking the "Close" button
2. Tapping outside the dialog area
3. Using the back button (on Android)

After closing, they return to the Team Roster view.

## Component Breakdown

### TeamInfoDialog Component

The dialog displays three pieces of information in a clean, structured format:

**Dialog Structure**:
```
┌─────────────────────────────────────┐
│  Dialog Title (headlineSmall)       │  ← "Team Info" / "Información del Equipo"
│                                     │
│  Field Label (labelMedium)          │  ← "Team Name" / "Nombre del Equipo"
│  Field Value (bodyLarge)            │  ← Actual team name
│                                     │
│  Field Label (labelMedium)          │  ← "Coach Name" / "Nombre del Entrenador"
│  Field Value (bodyLarge)            │  ← Actual coach name
│                                     │
│  Field Label (labelMedium)          │  ← "Delegate Name" / "Nombre del Delegado"
│  Field Value (bodyLarge)            │  ← Actual delegate name
│                                     │
│                        [Close]      │  ← "Close" / "Cerrar"
└─────────────────────────────────────┘
```

## Design Specifications

### Typography
- **Dialog Title**: MaterialTheme.typography.headlineSmall
- **Field Labels**: MaterialTheme.typography.labelMedium
- **Field Values**: MaterialTheme.typography.bodyLarge

### Spacing
- **Dialog Padding**: TFMSpacing.spacing06 (outer container)
- **Vertical Spacing**: TFMSpacing.spacing04 (between sections)
- **Field Spacing**: TFMSpacing.spacing01 (between label and value)
- **Button Top Padding**: TFMSpacing.spacing02

### Colors
- **Dialog Title**: MaterialTheme.colorScheme.onSurface
- **Field Labels**: MaterialTheme.colorScheme.onSurfaceVariant
- **Field Values**: MaterialTheme.colorScheme.onSurface
- **Background**: MaterialTheme.colorScheme.surface

### Elevation
- **Dialog Surface**: TFMElevation.level3

### Shape
- **Dialog Surface**: MaterialTheme.shapes.medium (rounded corners)

## Internationalization

The feature supports both English and Spanish:

### English (Default)
- Dialog Title: "Team Info"
- Button: "Close"
- Icon Description: "Show team information"
- Field Labels: "Team Name", "Coach Name", "Delegate Name"

### Spanish (es)
- Dialog Title: "Información del Equipo"
- Button: "Cerrar"
- Icon Description: "Mostrar información del equipo"
- Field Labels: "Nombre del Equipo", "Nombre del Entrenador", "Nombre del Delegado"

## Integration Points

### MainScreen Integration
The info button is integrated into the existing top bar:

**Before**:
```kotlin
CenterAlignedTopAppBar(
    title = {
        Text(
            text = teamName ?: "",
            style = MaterialTheme.typography.titleLarge,
        )
    },
)
```

**After**:
```kotlin
CenterAlignedTopAppBar(
    title = {
        Text(
            text = teamName ?: "",
            style = MaterialTheme.typography.titleLarge,
        )
    },
    actions = {                              // ← NEW
        IconButton(onClick = { ... }) {      // ← NEW
            Icon(                             // ← NEW
                imageVector = Icons.Filled.Info,
                contentDescription = ...
            )
        }
    },
)
```

### Data Flow
```
User Interaction Flow:
1. User taps info button
   ↓
2. MainScreen sets showTeamInfo = true
   ↓
3. MainScreen extracts currentTeam from TeamViewModel.uiState
   ↓
4. TeamInfoDialog renders with team data
   ↓
5. User taps Close or outside dialog
   ↓
6. MainScreen sets showTeamInfo = false
   ↓
7. Dialog dismisses
```

### State Management
```kotlin
// In MainScreen
var showTeamInfo by remember { mutableStateOf(false) }
val uiState by viewModel.uiState.collectAsState()

val currentTeam = when (val state = uiState) {
    is TeamUiState.TeamExists -> state.team
    else -> null
}

// Dialog rendering
if (showTeamInfo && currentTeam != null) {
    TeamInfoDialog(
        team = currentTeam,
        onDismiss = { showTeamInfo = false },
    )
}
```

## User Experience Considerations

### Accessibility
- Info icon has proper content description for screen readers
- Dialog can be dismissed via back button
- Text labels provide context for values
- Proper color contrast for readability

### Material Design Principles
- **Elevation**: Dialog appears above content with appropriate shadow
- **Motion**: Standard dialog animation (slide up/fade in)
- **Spacing**: Consistent spacing using design system tokens
- **Typography**: Clear hierarchy with different text styles

### Edge Cases
- Dialog only shows when team data is available (TeamUiState.TeamExists)
- Info button only appears after team is loaded
- Dialog handles long names gracefully with proper text wrapping

## Testing Scenarios

### Visual Testing
1. **Light Theme**: Verify colors and contrast in light mode
2. **Dark Theme**: Verify colors and contrast in dark mode
3. **Long Names**: Test with very long team/coach/delegate names
4. **Short Names**: Test with single-character names

### Functional Testing
1. **Show Dialog**: Tap info button → dialog appears
2. **Close Button**: Tap Close → dialog dismisses
3. **Outside Tap**: Tap outside dialog → dialog dismisses
4. **Back Button**: Press back → dialog dismisses
5. **Multiple Opens**: Open, close, open again → works correctly

### Localization Testing
1. **English**: Verify all strings in English
2. **Spanish**: Verify all strings in Spanish
3. **RTL Support**: Test with RTL languages (future consideration)

## Example Data Display

### Example 1: Complete Data
```
Team Info

Team Name
Los Halcones FC

Coach Name
Juan García Rodríguez

Delegate Name
María López Fernández

                [Close]
```

### Example 2: Short Names
```
Team Info

Team Name
FC

Coach Name
Ana

Delegate Name
Luis

                [Close]
```

## Summary

This visual guide demonstrates how the Team Information Display feature integrates seamlessly into the existing application:

- **Minimal UI Changes**: Single info button in top bar
- **Familiar Patterns**: Standard Material Design dialog
- **Clear Information**: Well-organized data presentation
- **Easy Dismissal**: Multiple ways to close
- **Localized**: Supports multiple languages
- **Accessible**: Proper labels and descriptions

The implementation provides a clean, elegant way for coaches to quickly view their team's general information without leaving the roster view.
