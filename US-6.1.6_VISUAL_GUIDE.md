# US-6.1.6: Filtrado de partido - Visual Guide

## Overview
This guide provides visual descriptions and code snippets showing the match filtering functionality.

## UI Components

### 1. Filter Button (Default State)

**Location**: Top of the matches list
**Appearance**: 
- Icon: FilterList (three horizontal lines with dots)
- Color: OnSurface (default gray)
- Position: Right-aligned

```
┌──────────────────────────────────┐
│                        [≡] Filter │  ← Filter button (inactive)
├──────────────────────────────────┤
│  🗄️  Archived                    │
├──────────────────────────────────┤
│  Pending Matches                  │
│  ┌──────────────────────────────┐│
│  │ Real Madrid                   ││
│  │ Santiago Bernabéu            ││
│  │ Oct 16, 2025                 ││
│  └──────────────────────────────┘│
└──────────────────────────────────┘
```

### 2. Filter Button (Active State)

**Appearance**:
- Icon: FilterList
- Color: Primary (blue/theme color)
- Indicates filter mode is enabled

```
┌──────────────────────────────────┐
│                        [≡] Filter │  ← Filter button (ACTIVE - blue)
├──────────────────────────────────┤
│  🔍 Search by opponent or loc... │  ← Search bar appears
│     [×]                           │  ← Clear button
├──────────────────────────────────┤
│  🗄️  Archived                    │
├──────────────────────────────────┤
```

### 3. Search Bar

**Components**:
- Leading Icon: Search (🔍)
- Text Input: "Search by opponent or location"
- Trailing Icon: Clear (×) - only when text entered
- Type: OutlinedTextField

**Example - Empty**:
```
┌────────────────────────────────────┐
│ 🔍 Search by opponent or location  │
└────────────────────────────────────┘
```

**Example - With Text**:
```
┌────────────────────────────────────┐
│ 🔍 Madrid                      [×] │
└────────────────────────────────────┘
```

### 4. Filtered Results View

When filter is active (search text entered), the view changes to show all matching matches in a flat list:

```
┌──────────────────────────────────┐
│                        [≡] Filter │  ← Active (blue)
├──────────────────────────────────┤
│  🔍 Madrid                   [×] │  ← Search bar with text
├──────────────────────────────────┤
│  ┌──────────────────────────────┐│
│  │ Real Madrid                   ││  ← Active match
│  │ Santiago Bernabéu            ││
│  │ Oct 16, 2025                 ││
│  └──────────────────────────────┘│
│  ┌──────────────────────────────┐│
│  │ Atletico Madrid [Archived]   ││  ← Archived match with badge
│  │ Wanda Metropolitano          ││
│  │ Mar 9, 2024                  ││  ← Italic text
│  └──────────────────────────────┘│
└──────────────────────────────────┘
```

### 5. Filtered Match Card

**Components**:
- Opponent name (bold)
- Archived badge (if archived) - secondary container color
- Location (medium text)
- Date (small text, italic if archived)
- Clickable entire card

**Layout**:
```
┌────────────────────────────────────┐
│ Real Madrid [Archived]              │  ← Badge only if archived
│ Santiago Bernabéu                   │
│ October 16, 2025                    │  ← Italic if archived
└────────────────────────────────────┘
```

**Code Structure**:
```kotlin
@Composable
fun FilteredMatchCard(
    match: Match,
    onNavigateToDetail: () -> Unit,
) {
    Card(clickable) {
        Column {
            Row {
                Text(match.opponent, bold)
                if (match.archived) {
                    Badge { Text("Archived") }
                }
            }
            Text(match.location)
            Text(match.date, italic if archived)
        }
    }
}
```

## User Flows

### Flow 1: Enable Filter and Search

**Steps**:
1. User sees matches list (categorized: pending/played)
2. Taps filter button → Search bar appears
3. Types "Barcelona" → Results update in real-time
4. Sees only matches with "Barcelona" in opponent or location
5. Archived matches appear with badge

**Visual Sequence**:

**Before (Step 1-2)**:
```
┌──────────────────────────────────┐
│                        [≡]       │  ← User taps
├──────────────────────────────────┤
│  Pending Matches                  │
│  Real Madrid                      │
│  Barcelona                        │
│  Atletico Madrid                  │
└──────────────────────────────────┘
```

**After (Step 3-5)**:
```
┌──────────────────────────────────┐
│                        [≡]       │  ← Blue (active)
├──────────────────────────────────┤
│  🔍 Barcelona               [×] │  ← User typing
├──────────────────────────────────┤
│  Barcelona                        │  ← Only matching
│  Camp Nou                         │     results shown
│  Oct 20, 2025                     │
└──────────────────────────────────┘
```

### Flow 2: Clear Search

**Steps**:
1. User has active search "Madrid"
2. Sees filtered results (2 matches)
3. Taps [×] clear button
4. Search text clears
5. View returns to showing all non-archived matches
6. Filter mode stays enabled (search bar remains visible)

**Visual Sequence**:

**Before**:
```
┌──────────────────────────────────┐
│  🔍 Madrid                   [×] │  ← User taps X
├──────────────────────────────────┤
│  Real Madrid                      │
│  Atletico Madrid [Archived]      │
└──────────────────────────────────┘
```

**After**:
```
┌──────────────────────────────────┐
│  🔍 Search by opponent or loc... │  ← Cleared
├──────────────────────────────────┤
│  🗄️  Archived                    │
│  Pending Matches                  │
│  Real Madrid                      │
│  Barcelona                        │
│  Sevilla                          │
└──────────────────────────────────┘
```

### Flow 3: Disable Filter Mode

**Steps**:
1. Filter mode is enabled (search bar visible)
2. User taps filter button again
3. Search bar disappears
4. All filters are cleared
5. View returns to normal categorized display

**Visual Sequence**:

**Before**:
```
┌──────────────────────────────────┐
│                        [≡]       │  ← User taps (blue)
├──────────────────────────────────┤
│  🔍 Barcelona               [×] │
├──────────────────────────────────┤
│  Barcelona                        │
└──────────────────────────────────┘
```

**After**:
```
┌──────────────────────────────────┐
│                        [≡]       │  ← Gray (inactive)
├──────────────────────────────────┤
│  🗄️  Archived                    │  ← Normal view
│  Pending Matches                  │     returns
│  Real Madrid                      │
│  Barcelona                        │
└──────────────────────────────────┘
```

## Code Highlights

### ViewModel - Filter State Management

```kotlin
// Filter state
data class FilterState(
    val isFilterModeEnabled: Boolean = false,
    val searchText: String = "",
    val startDate: Long? = null,
    val endDate: Long? = null,
) {
    val isActive: Boolean
        get() = isFilterModeEnabled && 
                (searchText.isNotBlank() || 
                 (startDate != null && endDate != null))
}

// Methods
fun toggleFilterMode() {
    _filterState.value = _filterState.value.copy(
        isFilterModeEnabled = !_filterState.value.isFilterModeEnabled,
        searchText = if (_filterState.value.isFilterModeEnabled) "" 
                     else _filterState.value.searchText,
        // Clear filters when disabling
    )
}

fun updateSearchText(text: String) {
    _filterState.value = _filterState.value.copy(searchText = text)
}
```

### UI - Conditional Rendering

```kotlin
when (val state = uiState) {
    is Success -> {
        val isFiltering = filterState.isActive
        
        if (isFiltering) {
            // Flat list of filtered matches
            LazyColumn {
                item { FilterButton() }
                item { SearchBar() }
                items(state.matches) { match ->
                    FilteredMatchCard(match)
                }
            }
        } else {
            // Normal categorized view
            LazyColumn {
                item { FilterButton() }
                item { ArchivedMatchesCard() }
                // Pending, paused, played sections...
            }
        }
    }
}
```

### UseCase - Filtering Logic

```kotlin
override fun invoke(
    filterText: String,
    startDate: Long?,
    endDate: Long?,
): Flow<List<Match>> =
    combine(
        matchRepository.getAllMatches(),
        matchRepository.getArchivedMatches(),
    ) { activeMatches, archivedMatches ->
        val allMatches = activeMatches + archivedMatches
        
        allMatches.filter { match ->
            // Text filtering
            val matchesText = if (filterText.isBlank()) {
                true
            } else {
                val searchText = filterText.trim().lowercase()
                val opponent = match.opponent?.lowercase() ?: ""
                val location = match.location?.lowercase() ?: ""
                opponent.contains(searchText) || 
                location.contains(searchText)
            }
            
            // Date filtering
            val matchesDateRange = 
                if (startDate != null && endDate != null && 
                    match.date != null) {
                    match.date in startDate..endDate
                } else {
                    true
                }
            
            matchesText && matchesDateRange
        }
    }
```

## Visual Indicators

### Archived Match Badge

**Appearance**:
- Small rounded rectangle
- Secondary container background color
- Label text in small font
- Positioned next to opponent name

**Code**:
```kotlin
Badge(
    containerColor = MaterialTheme.colorScheme.secondaryContainer,
) {
    Text(
        text = stringResource(R.string.archived_indicator),
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier.padding(horizontal = TFMSpacing.spacing01)
    )
}
```

**Visual**:
```
┌─────────────────────────────────┐
│ Real Madrid [ Archived ]         │
│                ^            ^    │
│                Badge with    │
│                background    │
└─────────────────────────────────┘
```

### Filter Button State Colors

**Inactive** (filter mode disabled):
```
[≡]  ← MaterialTheme.colorScheme.onSurface
     (standard gray)
```

**Active** (filter mode enabled):
```
[≡]  ← MaterialTheme.colorScheme.primary
     (theme primary color, typically blue)
```

## Responsive Behavior

### Keyboard Handling

1. **Show Keyboard**: When search bar is focused
2. **Hide Keyboard**: 
   - When user presses "Search" on keyboard
   - When user navigates away
   - When user taps outside search bar

```kotlin
keyboardActions = KeyboardActions(
    onSearch = {
        keyboardController?.hide()
    }
)
```

### Real-time Updates

- Filter results update **immediately** as user types
- Uses Flow reactive streams
- No "Search" button needed
- Smooth user experience

```
User types: "M"      → Shows: Madrid, Miami, ...
User types: "Ma"     → Shows: Madrid, ...
User types: "Mad"    → Shows: Madrid
User types: "Madr"   → Shows: Madrid
User types: "Madri"  → Shows: Madrid
User types: "Madrid" → Shows: Real Madrid, Atletico Madrid
```

## Material Design 3 Components Used

1. **IconButton** - Filter button
2. **OutlinedTextField** - Search bar
3. **Badge** - Archived indicator
4. **Card** - Match cards
5. **MaterialTheme** - Colors and typography
6. **Icon** - Material icons (FilterList, Search, Clear)

## Accessibility Features

1. **Content Descriptions**: All icons have descriptions
   ```kotlin
   Icon(
       imageVector = Icons.Default.FilterList,
       contentDescription = stringResource(R.string.filter_matches),
   )
   ```

2. **Keyboard Actions**: Search action on IME
3. **Clear Button**: Visible when needed
4. **Visual Feedback**: Color changes indicate state

## Performance Considerations

1. **Reactive Filtering**: Uses Kotlin Flow for efficiency
2. **Lazy Loading**: LazyColumn for list virtualization
3. **State Management**: Minimal recompositions
4. **Flow Operators**: flatMapLatest prevents stale results

## Testing Examples

### Example Test Case

```kotlin
@Test
fun `should filter matches by opponent name`() = runTest {
    // Given
    val matches = listOf(
        Match(opponent = "Real Madrid"),
        Match(opponent = "Barcelona"),
    )
    every { repository.getAllMatches() } returns flowOf(matches)
    every { repository.getArchivedMatches() } returns flowOf(emptyList())

    // When
    useCase.invoke("Madrid").test {
        // Then
        val result = awaitItem()
        assertEquals(1, result.size)
        assertEquals("Real Madrid", result.first().opponent)
        awaitComplete()
    }
}
```

## Summary

The match filtering implementation provides:
- ✅ Clean, intuitive UI following Material Design 3
- ✅ Real-time search as user types
- ✅ Clear visual feedback for all states
- ✅ Proper handling of archived matches
- ✅ Keyboard-friendly interaction
- ✅ Accessible design
- ✅ Efficient performance with Flows

The implementation seamlessly integrates with existing match management features while maintaining the app's visual consistency and user experience patterns.
