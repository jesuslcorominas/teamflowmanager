# US-6.1.8: Vista de partidos - Verification Checklist

## Build Verification

### Prerequisites
- [ ] Android Studio or IntelliJ IDEA with Android plugin installed
- [ ] JDK 17 configured
- [ ] Android SDK with API level 36 installed
- [ ] Internet connection to download Gradle dependencies

### Build Steps
1. [ ] Open the project in Android Studio
2. [ ] Wait for Gradle sync to complete
3. [ ] Run `./gradlew clean build` or use Android Studio's Build menu
4. [ ] Verify build succeeds without errors
5. [ ] Run unit tests: `./gradlew test`
6. [ ] Verify all tests pass, especially:
   - `RouteTest.uiConfig returns correct configuration for each route`
   - `RouteTest.UiConfig equality and copy work as expected`

## Functional Verification

### 1. Top Bar Visibility
- [ ] Launch the app and navigate to the Teams screen
- [ ] Navigate to the Matches screen using bottom navigation
- [ ] **Verify**: Top bar is visible showing the team name
- [ ] **Verify**: No back button is shown in the top bar (as expected for main navigation)

### 2. Bottom Bar Visibility
- [ ] On the Matches screen
- [ ] **Verify**: Bottom navigation bar is visible with three tabs:
  - Players (Jugadores)
  - Team (Equipo)
  - Matches (Partidos) - should be selected/highlighted

### 3. Floating Action Button (FAB)
- [ ] On the Matches screen
- [ ] **Verify**: FAB with a "+" icon is visible in the bottom-right corner
- [ ] Tap the FAB
- [ ] **Verify**: Navigation occurs to the Create Match screen (wizard)

### 4. Content Display
- [ ] **Empty State**: If no matches exist
  - [ ] **Verify**: "Archived Matches" card is visible at the top
  - [ ] **Verify**: "No matches" message is displayed
  - [ ] Content is properly padded and not overlapping with top/bottom bars
  
- [ ] **With Matches**: If matches exist
  - [ ] **Verify**: "Archived Matches" card is visible at the top
  - [ ] **Verify**: Matches are organized in sections:
    - Paused match (if exists)
    - Pending matches
    - Played matches
  - [ ] Content scrolls properly without being cut off by top/bottom bars
  - [ ] FAB doesn't overlap with content

### 5. Navigation Flow
- [ ] From Matches screen, tap bottom navigation to Players
- [ ] **Verify**: Top bar and bottom bar remain visible
- [ ] Navigate back to Matches using bottom navigation
- [ ] **Verify**: Matches screen state is preserved (if any)
- [ ] Tap FAB to create a new match
- [ ] **Verify**: Create Match screen opens
- [ ] **Verify**: Top bar shows team name with back button on Create Match screen

### 6. Archived Matches Navigation
- [ ] On the Matches screen, tap "Archived Matches" card
- [ ] **Verify**: Navigation to Archived Matches screen
- [ ] **Verify**: Archived Matches screen has its own UI configuration (check if top/bottom bars appear as expected)

## Code Review Checklist

### Route.kt
- [ ] `showFab` parameter added to Route class constructor with default value `false`
- [ ] `UiConfig` data class includes `showFab: Boolean` property
- [ ] `Route.Matches` object sets `showFab = true`
- [ ] Other routes maintain `showFab = false` (default)

### MainScreen.kt
- [ ] Imports include `FloatingActionButton` and `Icons.Default.Add`
- [ ] Scaffold includes `floatingActionButton` parameter
- [ ] FAB only shows when `uiConfig?.showFab == true`
- [ ] FAB click navigates to `Route.CreateMatch` when on `Route.Matches`
- [ ] FAB has proper content description for accessibility

### MatchListScreen.kt
- [ ] No `Scaffold` wrapper (removed)
- [ ] No `FloatingActionButton` (removed)
- [ ] Root container is `Box` with `Modifier.fillMaxSize()`
- [ ] No `paddingValues` parameter handling
- [ ] Content rendering unchanged (Empty, Loading, Success states)
- [ ] Unused imports removed: `Icons.filled.Add`, `FloatingActionButton`, `Scaffold`

### RouteTest.kt
- [ ] Test `uiConfig returns correct configuration for each route` updated
- [ ] Assertions for `showFab` added for all tested routes
- [ ] Test for `Route.Matches` verifies `showFab = true`
- [ ] Test `UiConfig equality and copy work as expected` updated with `showFab` parameter

## Regression Testing

### Other Screens
- [ ] **Players Screen**: Verify top bar and bottom bar still work correctly
- [ ] **Team Detail Screen**: Verify top bar and bottom bar still work correctly
- [ ] **Create Match Screen**: Verify back button works
- [ ] **Current Match Screen**: Verify back button works
- [ ] **Match Detail Screen**: Verify back button works
- [ ] **Match Summary Screen**: Verify back button works

### Existing Functionality
- [ ] Creating a new match still works
- [ ] Editing a match still works
- [ ] Deleting a match (with confirmation) still works
- [ ] Starting a match still works
- [ ] Archiving a match still works
- [ ] Viewing archived matches still works

## Performance
- [ ] Screen transitions are smooth
- [ ] No lag when showing/hiding FAB
- [ ] Scrolling performance is not degraded
- [ ] No memory leaks observed

## Accessibility
- [ ] FAB has proper content description
- [ ] Top bar title is readable by screen readers
- [ ] Bottom navigation items have proper labels

## Known Limitations
- **Search field**: Not implemented in this US (planned for future)
- **Additional action buttons**: Not implemented in this US (planned for future)

## Sign-off
- [ ] All verification steps completed successfully
- [ ] No regressions detected
- [ ] Code changes are minimal and focused
- [ ] Tests pass
- [ ] Ready for merge

---

## Notes
Add any observations, issues, or additional notes here:

