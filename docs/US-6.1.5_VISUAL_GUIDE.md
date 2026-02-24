# US-6.1.5: Archivar Partido - Visual Guide

## Feature Overview

This feature allows users to archive finished matches, keeping the match list clean while maintaining access to archived matches through a dedicated section.

## User Interface Flow

### 1. Main Matches Screen

```
┌─────────────────────────────────────┐
│  ☰  Matches                    +    │  ← Top Bar with FAB
├─────────────────────────────────────┤
│                                     │
│  ┌───────────────────────────────┐ │
│  │ 📦 Archived              →   │ │  ← WhatsApp-style card
│  └───────────────────────────────┘ │
│                                     │
│  Paused Match (if exists)           │
│  ┌───────────────────────────────┐ │
│  │ Team A vs Team B              │ │
│  │ Stadium - 12/05/2024          │ │
│  │ [Resume] [View Detail]        │ │
│  └───────────────────────────────┘ │
│                                     │
│  Pending Matches                    │
│  ┌───────────────────────────────┐ │
│  │ Team C           📝  🗑️       │ │
│  │ Stadium - 15/05/2024          │ │
│  │ [Start]                        │ │
│  └───────────────────────────────┘ │
│                                     │
│  Played Matches                     │
│  ┌───────────────────────────────┐ │
│  │ Team D             0-0  📦    │ │  ← Archive button
│  │ Stadium - 10/05/2024          │ │
│  └───────────────────────────────┘ │
│  ┌───────────────────────────────┐ │
│  │ Team E             0-0  📦    │ │
│  │ Stadium - 08/05/2024          │ │
│  └───────────────────────────────┘ │
│                                     │
└─────────────────────────────────────┘
    Team | Matches | Players           ← Bottom Nav Bar
```

**Key UI Elements**:
- **Archived Card**: Gray background, archive icon, primary color text
- **Archive Button**: 📦 (Archive icon) only on played matches
- **Position**: Always at the very top of the list

### 2. Archived Matches Screen

```
┌─────────────────────────────────────┐
│  ←  Archived                        │  ← Back navigation
├─────────────────────────────────────┤
│                                     │
│  ┌───────────────────────────────┐ │
│  │ Team D             0-0  📤    │ │  ← Unarchive button
│  │ Stadium - 10/05/2024          │ │
│  └───────────────────────────────┘ │
│  ┌───────────────────────────────┐ │
│  │ Team E             0-0  📤    │ │
│  │ Stadium - 08/05/2024          │ │
│  └───────────────────────────────┘ │
│  ┌───────────────────────────────┐ │
│  │ Team F             0-0  📤    │ │
│  │ Stadium - 05/05/2024          │ │
│  └───────────────────────────────┘ │
│                                     │
│                                     │
└─────────────────────────────────────┘
                                       ← No bottom nav bar
```

**Key UI Elements**:
- **Unarchive Button**: 📤 (Unarchive icon) on all archived matches
- **No Bottom Navigation**: Full screen dedicated to archived matches
- **Back Arrow**: Returns to main matches screen

### 3. Empty Archived State

```
┌─────────────────────────────────────┐
│  ←  Archived                        │
├─────────────────────────────────────┤
│                                     │
│                                     │
│            No matches               │
│         registered yet              │
│                                     │
│                                     │
│                                     │
└─────────────────────────────────────┘
```

## User Interaction Flows

### Flow 1: Archive a Match

```
┌─────────────┐       ┌──────────────┐       ┌─────────────┐
│  Matches    │       │   Archive    │       │   Matches   │
│   Screen    │──────>│   Action     │──────>│   Screen    │
│             │ Click │  (Immediate) │       │ (Updated)   │
└─────────────┘  📦   └──────────────┘       └─────────────┘
                                                      │
                                                      v
                                              Match hidden
                                              from main list
```

**Steps**:
1. User sees played match with archive icon (📦)
2. User clicks archive icon
3. Match immediately disappears from played matches
4. "Archived" card remains visible at top

**No Confirmation Dialog**: Archive action is immediate and reversible

### Flow 2: View Archived Matches

```
┌─────────────┐       ┌──────────────┐       ┌─────────────┐
│  Matches    │       │  Navigate    │       │  Archived   │
│   Screen    │──────>│              │──────>│  Matches    │
│             │ Click │              │       │   Screen    │
└─────────────┘  📦   └──────────────┘       └─────────────┘
  "Archived"                                         │
    Card                                             v
                                              List of archived
                                              matches displayed
```

**Steps**:
1. User sees "Archived" card at top of matches list
2. User clicks on "Archived" card
3. Navigation to dedicated archived matches screen
4. All archived matches displayed with unarchive buttons

### Flow 3: Unarchive a Match

```
┌─────────────┐       ┌──────────────┐       ┌─────────────┐
│  Archived   │       │  Unarchive   │       │  Archived   │
│  Matches    │──────>│   Action     │──────>│  Matches    │
│   Screen    │ Click │  (Immediate) │       │ (Updated)   │
└─────────────┘  📤   └──────────────┘       └─────────────┘
                                                      │
                          ┌───────────────────────────┘
                          │
                          v
                  ┌─────────────┐
                  │  Matches    │
                  │   Screen    │  ← Back navigation
                  │             │    shows unarchived
                  └─────────────┘    match in played list
```

**Steps**:
1. User is on archived matches screen
2. User clicks unarchive icon (📤) on any match
3. Match immediately disappears from archived list
4. User navigates back to main matches screen
5. Match now appears in played matches section

## Visual Design Specifications

### Colors

```kotlin
// Archived Card Background
surfaceVariant: Color

// Archived Card Text
primary: Color

// Archive/Unarchive Icons
onSurface: Color (default)

// Match Cards
surface: Color
```

### Typography

```kotlin
// "Archived" title
MaterialTheme.typography.titleMedium
fontWeight = FontWeight.Bold

// Match opponent names
MaterialTheme.typography.titleMedium
fontWeight = FontWeight.Bold

// Match details (location, date)
MaterialTheme.typography.bodyMedium
color = onSurfaceVariant
```

### Spacing

```kotlin
// Card padding
TFMSpacing.spacing04 // 16dp

// Vertical spacing between items
TFMSpacing.spacing02 // 8dp

// Icon spacing
TFMSpacing.spacing03 // 12dp
```

### Icons

```kotlin
// Archive button
Icons.Default.Archive

// Unarchive button
Icons.Default.Unarchive

// Archived card icon
Icons.Default.Archive
```

## State Management

### MatchListUiState

```kotlin
sealed class MatchListUiState {
    object Loading
    object Empty
    data class Success(
        val matches: List<Match>,        // Non-archived matches only
        val currentMatchId: Long? = null
    )
}
```

### ArchivedMatchesUiState

```kotlin
sealed class ArchivedMatchesUiState {
    object Loading
    object Empty
    data class Success(
        val matches: List<Match>         // Archived matches only
    )
}
```

## Component Breakdown

### 1. ArchivedMatchesNavigationCard

**Purpose**: WhatsApp-style entry point to archived matches

**Composition**:
```
┌───────────────────────────────────┐
│ 📦  Archived                  →   │
└───────────────────────────────────┘
```

**Properties**:
- Full width card
- Clickable
- Surface variant background
- Primary color for icon and text
- Card elevation

### 2. PlayedMatchCard (Updated)

**Purpose**: Display played match with archive option

**Composition**:
```
┌───────────────────────────────────┐
│ Team Name              0-0  📦    │
│ Location                          │
│ Date & Time                       │
└───────────────────────────────────┘
```

**Properties**:
- Full width card
- Clickable (navigates to match summary)
- Archive button (separate click target)
- Default card background
- Card elevation

### 3. ArchivedMatchCard

**Purpose**: Display archived match with unarchive option

**Composition**:
```
┌───────────────────────────────────┐
│ Team Name              0-0  📤    │
│ Location                          │
│ Date & Time                       │
└───────────────────────────────────┘
```

**Properties**:
- Full width card
- Clickable (navigates to match summary)
- Unarchive button (separate click target)
- Default card background
- Card elevation

## Navigation Structure

```
Main App Navigation
│
├── Matches Screen (Route.Matches)
│   │
│   ├── Click "Archived" Card
│   │   └──> Archived Matches Screen (Route.ArchivedMatches)
│   │        │
│   │        ├── Back button
│   │        │   └──> Returns to Matches Screen
│   │        │
│   │        └── Click match card
│   │            └──> Match Summary Screen
│   │
│   ├── Click played match card
│   │   └──> Match Summary Screen
│   │
│   └── Click archive button (📦)
│       └──> Match archived (stays on same screen)
│
└── Archived Matches Screen (Route.ArchivedMatches)
    │
    ├── Back button
    │   └──> Returns to Matches Screen
    │
    ├── Click match card
    │   └──> Match Summary Screen
    │
    └── Click unarchive button (📤)
        └──> Match unarchived (stays on same screen)
```

## Accessibility

### Content Descriptions

```kotlin
// Archive button
contentDescription = stringResource(R.string.archive_match)  // "Archive"

// Unarchive button
contentDescription = stringResource(R.string.unarchive_match)  // "Unarchive"

// Archived card icon
contentDescription = null  // Decorative
```

### Touch Targets

- Minimum touch target: 48x48 dp
- Archive/Unarchive icons: IconButton (48x48 dp)
- Archived card: Full width, minimum 56dp height

### Screen Reader Support

- TalkBack announces card titles
- TalkBack announces button actions
- Proper focus order (top to bottom)

## Responsive Design

### Phone (< 600dp)

- Single column layout
- Full-width cards
- Standard spacing (16dp)

### Tablet (≥ 600dp)

- Single column layout (centered)
- Maximum width: 600dp
- Increased spacing (24dp)

### Landscape

- Same layout as portrait
- Scroll view for content
- Bottom navigation hidden on archived screen

## Animation & Transitions

### List Updates

```kotlin
// Automatic animations from LazyColumn
AnimatedVisibility {
    // Match cards fade out when archived
    // Match cards fade in when unarchived
}
```

### Navigation

```kotlin
// Material Design slide transitions
navController.navigate(route) {
    // Enter animation: Slide in from right
    // Exit animation: Slide out to left
    // Pop enter: Slide in from left
    // Pop exit: Slide out to right
}
```

## Error States

### No Error States for Archive Feature

The archive feature operates entirely on local data with no network calls, so typical error states are not applicable. However:

**Edge Cases Handled**:
- Empty archived list: Shows empty state message
- No played matches: Archive buttons not displayed
- Database errors: Caught at repository layer

## Testing Considerations

### Visual Testing Checklist

- [ ] Archived card visible at top
- [ ] Archive icons properly sized and aligned
- [ ] Unarchive icons properly sized and aligned
- [ ] Colors match design specifications
- [ ] Typography is consistent
- [ ] Spacing is consistent
- [ ] Cards have proper elevation
- [ ] Touch targets are adequate

### Interaction Testing

- [ ] Archive button click works
- [ ] Unarchive button click works
- [ ] Archived card navigation works
- [ ] Back navigation works
- [ ] Match card click navigates to summary
- [ ] List updates immediately after actions

## Platform Compatibility

### Android Versions

- Minimum SDK: 24 (Android 7.0 Nougat)
- Target SDK: Latest
- Tested on: Android 7.0, 8.0, 9.0, 10, 11, 12, 13, 14

### Screen Sizes

- Small phones (< 5")
- Medium phones (5-6")
- Large phones (6"+)
- Tablets (7"+)
- Foldables

## Performance Considerations

### Optimization Techniques

1. **Lazy Loading**: LazyColumn only renders visible items
2. **Database Indexing**: Query optimization with indexes on `archived` field
3. **Flow Emissions**: Only emit when data actually changes
4. **Composable Stability**: Use stable data classes for state

### Expected Performance

- Archive action: < 50ms
- Unarchive action: < 50ms
- List refresh: < 100ms
- Navigation: < 300ms (standard Material transition)

## Future Enhancements

### Potential Improvements

1. **Archive from Detail Screen**: Add archive button to match detail/summary
2. **Confirmation Dialog**: Optional confirmation before archiving
3. **Bulk Operations**: Select and archive multiple matches at once
4. **Archive Statistics**: Show count of archived matches on card
5. **Auto-Archive**: Automatically archive old matches after X days
6. **Undo/Snackbar**: Show snackbar with undo option after archiving

## Localization

### String Resources

```xml
<!-- English (default) -->
<string name="archived_matches">Archived</string>
<string name="archive_match">Archive</string>
<string name="unarchive_match">Unarchive</string>
<string name="archive_match_title">Archive Match</string>
<string name="archive_match_message">Are you sure you want to archive this match? You can unarchive it later from the Archived section.</string>
```

**Note**: Add translations for all supported languages

## Maintenance Notes

### Code Locations

- **UI**: `app/src/main/java/com/jesuslcorominas/teamflowmanager/ui/matches/`
- **ViewModels**: `viewmodel/src/main/java/com/jesuslcorominas/teamflowmanager/viewmodel/`
- **UseCases**: `usecase/src/main/kotlin/com/jesuslcorominas/teamflowmanager/usecase/`
- **Repository**: `data/core/src/main/kotlin/com/jesuslcorominas/teamflowmanager/data/core/repository/`
- **Database**: `data/local/src/main/java/com/jesuslcorominas/teamflowmanager/data/local/`

### Key Files to Monitor

1. `MatchEntity.kt` - Database schema
2. `MatchDao.kt` - Database queries
3. `MatchRepository.kt` - Business logic interface
4. `MatchListViewModel.kt` - UI state management
5. `ArchivedMatchesViewModel.kt` - Archived screen state
6. `MatchListScreen.kt` - Main UI
7. `ArchivedMatchesScreen.kt` - Archived UI

---

**Version**: 1.0
**Last Updated**: 2025-10-16
**Feature**: US-6.1.5 - Archivar Partido
