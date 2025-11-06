# US-7.1.6: Local Testing Checklist

## Pre-Testing Setup

- [ ] Clone repository and checkout branch `copilot/migrate-navigation-to-voyager`
- [ ] Open project in Android Studio (Arctic Fox or newer recommended)
- [ ] Sync Gradle files
- [ ] Verify all Voyager dependencies download successfully
- [ ] Build project (`./gradlew clean build`)
- [ ] Install app on emulator or device

## Functional Testing

### 1. Basic Navigation

- [ ] App launches successfully
- [ ] Splash screen displays
- [ ] First-time flow: Creates team and navigates to matches
- [ ] Existing team flow: Goes directly to matches tab

### 2. Tab Navigation

- [ ] Can switch between all 4 tabs (Matches, Players, Analysis, Team)
- [ ] Bottom bar highlights current tab correctly
- [ ] Tab content displays correctly
- [ ] Switching tabs preserves state within tabs
- [ ] Clicking same tab resets to first screen in that tab

### 3. Screen Navigation

#### From Matches Tab
- [ ] Can navigate to match detail
- [ ] Can navigate to archived matches
- [ ] Can navigate to create/edit match wizard
- [ ] All navigations pass correct parameters

#### From Players Tab
- [ ] Can navigate to create player wizard
- [ ] Can navigate to edit player wizard
- [ ] Player ID parameter passes correctly

#### From Team Tab
- [ ] Can switch between view and edit modes
- [ ] FAB opens edit mode
- [ ] Edit mode shows correctly

### 4. Parameters

- [ ] Player wizard receives correct playerId
  - [ ] New player: playerId = 0
  - [ ] Edit player: playerId = actual ID
- [ ] Match detail receives correct matchId, teamName, opponent
- [ ] Team screen mode works correctly (create/view/edit)

### 5. Back Navigation

#### System Back Button

- [ ] From Matches tab: exits app
- [ ] From Players tab: goes to Matches tab
- [ ] From Analysis tab: goes to Matches tab
- [ ] From Team view tab: goes to Matches tab
- [ ] From Team edit mode: shows confirmation dialog
- [ ] From detail screens: pops back to previous screen
- [ ] From wizards: shows confirmation if unsaved changes

#### Search Bar Back

- [ ] Open search on Matches screen
- [ ] Type some text
- [ ] Press back: closes search (doesn't exit app)
- [ ] Query clears
- [ ] Search icon reappears in topbar

#### TopBar Back Button

- [ ] Matches tab: no back button visible
- [ ] Players tab: no back button visible
- [ ] Analysis tab: no back button visible
- [ ] Team view: no back button visible
- [ ] Team edit: back button visible, shows confirmation
- [ ] Detail screens: back button visible, pops to previous
- [ ] Archived matches: back button visible, returns to matches
- [ ] Wizards: no back button (custom wizard navigation)

### 6. Edit Screen Confirmations

#### Team Edit
- [ ] Make changes to team data
- [ ] Press system back: confirmation dialog shows
- [ ] Press topbar back: confirmation dialog shows
- [ ] Press "Discard": returns to view mode
- [ ] Press "Cancel": stays in edit mode
- [ ] Save changes: automatically returns to view mode

#### Player Wizard
- [ ] Fill in player data (step 1)
- [ ] Press back: confirmation dialog shows
- [ ] Press "Discard": closes wizard
- [ ] Press "Cancel": stays in wizard
- [ ] Complete wizard: saves and closes

#### Match Wizard
- [ ] Fill in match data
- [ ] Press back: confirmation dialog shows
- [ ] Press "Discard": closes wizard
- [ ] Press "Cancel": stays in wizard
- [ ] Complete wizard: saves and closes

### 7. UI Elements

#### TopBar
- [ ] Shows on appropriate screens
- [ ] Hides on splash, wizards
- [ ] Back button shows when `canGoBack = true`
- [ ] Search icon shows on Matches tab
- [ ] Title displays correctly per screen
- [ ] Transitions smoothly between search and normal mode

#### BottomBar
- [ ] Shows on main tabs (Matches, Players, Analysis, Team)
- [ ] Hides on splash, detail screens, wizards
- [ ] Correct tab highlighted
- [ ] Icons display correctly
- [ ] Labels display correctly (if enabled)
- [ ] Smooth transitions

#### FAB
- [ ] Shows on Matches tab (Add match)
- [ ] Shows on Team view tab (Edit team)
- [ ] Hides on other tabs
- [ ] Hides on detail/wizard screens
- [ ] Clicking Matches FAB: opens match creation wizard
- [ ] Clicking Team FAB: switches to edit mode

### 8. Search Functionality

- [ ] Search icon appears on Matches screen topbar
- [ ] Clicking search icon: topbar transitions to search mode
- [ ] Search field has placeholder text
- [ ] Can type in search field
- [ ] Search filters matches list correctly
- [ ] Clear button appears when text entered
- [ ] Clear button clears search query
- [ ] Back button closes search and restores normal topbar
- [ ] Search state persists during tab switch (if same tab)

### 9. Edge Cases

#### Tab Reset
- [ ] On Matches tab, click Matches tab again: scrolls to top / resets
- [ ] On Players tab, click Players tab again: scrolls to top / resets
- [ ] On Analysis tab, click Analysis tab again: scrolls to top / resets
- [ ] On Team tab, click Team tab again: switches to view mode

#### State Preservation
- [ ] Navigate Matches → Match Detail
- [ ] Press Home button (app goes to background)
- [ ] Restore app: should be on Match Detail
- [ ] Press back: returns to Matches

#### Deep Navigation
- [ ] Matches → Match Detail → Edit Match
- [ ] Press back from Edit Match: returns to Match Detail
- [ ] Press back from Match Detail: returns to Matches

#### Configuration Changes
- [ ] Rotate device while on various screens
- [ ] Verify navigation state preserved
- [ ] Verify no crashes on rotation

### 10. Performance

- [ ] Tab switching is smooth (no lag)
- [ ] Screen transitions are smooth
- [ ] No memory leaks (check Android Studio Profiler)
- [ ] App doesn't freeze during navigation
- [ ] No ANRs (Application Not Responding)

## Regression Testing

- [ ] All existing features still work
- [ ] No broken navigation paths
- [ ] All screens accessible
- [ ] Deep links work (if implemented)
- [ ] Notifications work (if implemented)
- [ ] Widget updates work (if implemented)

## Code Quality

- [ ] No compilation errors
- [ ] No runtime exceptions
- [ ] No deprecation warnings (related to new code)
- [ ] Lint checks pass: `./gradlew lint`
- [ ] KtLint checks pass: `./gradlew ktlintCheck`
- [ ] All tests pass: `./gradlew test`

## Cleanup Tasks

After all tests pass:

- [ ] Remove old navigation files:
  - [ ] `/ui/navigation/Navigation.kt`
  - [ ] `/ui/navigation/BottomNavigationBar.kt`
  - [ ] `/ui/main/MainScreen.kt`
  - [ ] `/ui/components/topbar/AppTopBar.kt` (keep if still needed elsewhere)

- [ ] Remove old dependency:
  - [ ] In `app/build.gradle.kts`, remove: `implementation(libs.androidx.navigation.compose)`

- [ ] Update any navigation-related tests

- [ ] Run full test suite: `./gradlew test connectedAndroidTest`

- [ ] Final build: `./gradlew clean build`

- [ ] Commit cleanup changes

## Documentation Verification

- [ ] Read VOYAGER_GUIDE.md
- [ ] Verify all examples work as documented
- [ ] Test all code snippets in the guide
- [ ] Update guide if anything is incorrect

## Final Checks

- [ ] All checklist items completed
- [ ] No known issues
- [ ] Performance acceptable
- [ ] Ready for code review
- [ ] Ready to merge

## Known Issues to Log

Use this section to document any issues found during testing:

```
Issue 1: [Description]
- Steps to reproduce:
- Expected behavior:
- Actual behavior:
- Severity: [Critical/High/Medium/Low]

Issue 2: [Description]
...
```

## Sign-Off

- [ ] Tested by: ________________
- [ ] Date: ________________
- [ ] All critical tests passed: YES / NO
- [ ] Ready for merge: YES / NO

---

## Testing Notes

Add any relevant notes, observations, or context here:

```
[Your notes here]
```

---

## Resources

- **Implementation Guide**: `US-7.1.6_IMPLEMENTATION_SUMMARY.md`
- **Visual Guide**: `US-7.1.6_VISUAL_GUIDE.md`
- **Voyager Documentation**: `VOYAGER_GUIDE.md`
- **Final Summary**: `US-7.1.6_FINAL_SUMMARY.md`
- **Voyager Official Docs**: https://voyager.adriel.cafe/

---

**Version**: 1.0  
**Last Updated**: November 2025
