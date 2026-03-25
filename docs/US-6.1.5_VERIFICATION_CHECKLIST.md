# US-6.1.5: Archivar Partido - Verification Checklist

## Pre-Verification Setup

### Environment Setup
- [ ] Android Studio installed and configured
- [ ] Project synced with Gradle
- [ ] Device/Emulator running Android API 24+ (Nougat or higher)
- [ ] Test data: At least 5 played matches in the database

### Database State
Before testing, ensure you have:
- [ ] At least 3-5 finished matches (matches with `elapsedTimeMillis > 0`)
- [ ] At least 1-2 pending matches (matches with `elapsedTimeMillis == 0`)
- [ ] No existing archived matches (or some if testing unarchive)

## Functional Verification

### 1. Archive Match from Played Matches List

#### Test Case 1.1: Archive Button Visibility
- [ ] Navigate to Matches screen
- [ ] Scroll to "Played Matches" section
- [ ] Verify each played match card shows an archive icon (📦)
- [ ] Verify pending matches do NOT show archive button
- [ ] Verify paused matches do NOT show archive button

**Expected Result**: Archive icon only visible on finished matches

#### Test Case 1.2: Archive a Match
- [ ] Click archive icon on any played match
- [ ] Verify match immediately disappears from played matches list
- [ ] Verify "Archived" card remains at top of screen
- [ ] Navigate away and back to Matches screen
- [ ] Verify archived match is still not visible

**Expected Result**: Match successfully archived and hidden from main list

#### Test Case 1.3: Archive Multiple Matches
- [ ] Archive 2-3 different played matches one by one
- [ ] Verify each disappears from the list after archiving
- [ ] Verify played matches count decreases correctly

**Expected Result**: All archived matches hidden from main view

### 2. Archived Section Navigation

#### Test Case 2.1: Archived Card Visibility
- [ ] Open Matches screen
- [ ] Verify "Archived" card appears at the very top of the list
- [ ] Verify it has an archive icon (📦)
- [ ] Verify text says "Archived"
- [ ] Verify card uses `surfaceVariant` background color
- [ ] Verify card is styled differently from match cards

**Expected Result**: Archived card visible and correctly styled (WhatsApp-style)

#### Test Case 2.2: Navigate to Archived Matches
- [ ] Click on "Archived" card
- [ ] Verify navigation to Archived Matches screen
- [ ] Verify screen shows "Archived" title in top bar
- [ ] Verify back arrow in top bar
- [ ] Verify no bottom navigation bar visible

**Expected Result**: Successfully navigate to archived matches screen

### 3. Archived Matches Screen

#### Test Case 3.1: Display Archived Matches
- [ ] Navigate to Archived Matches screen
- [ ] Verify all previously archived matches appear
- [ ] Verify matches show opponent name, location, and date
- [ ] Verify matches show score (0-0 placeholder)
- [ ] Verify each match has unarchive icon (📤)

**Expected Result**: All archived matches displayed correctly

#### Test Case 3.2: Empty Archived List
- [ ] Unarchive all matches
- [ ] Navigate back to Matches screen
- [ ] Navigate to Archived Matches again
- [ ] Verify empty state message displayed

**Expected Result**: "No matches registered yet" message shown

#### Test Case 3.3: Navigate to Match Detail
- [ ] Click on any archived match card (not the icon)
- [ ] Verify navigation to Match Summary screen
- [ ] Verify match details displayed correctly
- [ ] Verify back navigation works

**Expected Result**: Can view details of archived matches

### 4. Unarchive Functionality

#### Test Case 4.1: Unarchive a Match
- [ ] Navigate to Archived Matches screen
- [ ] Click unarchive icon (📤) on any match
- [ ] Verify match immediately disappears from archived list
- [ ] Navigate back to Matches screen
- [ ] Verify match reappears in "Played Matches" section

**Expected Result**: Match successfully unarchived and visible in main list

#### Test Case 4.2: Unarchive Multiple Matches
- [ ] Archive 3 matches
- [ ] Navigate to Archived Matches
- [ ] Unarchive all 3 matches one by one
- [ ] Verify each disappears from archived list
- [ ] Verify all reappear in main played matches list

**Expected Result**: All unarchived matches return to main view

### 5. Match Detail Screen Archive (Future Enhancement)

#### Test Case 5.1: Archive from Detail Screen
Note: This test case is for future implementation if archive button is added to match detail screen.
- [ ] Navigate to a played match detail
- [ ] Click archive button
- [ ] Verify navigation back to Matches screen
- [ ] Verify match no longer visible in played matches

**Status**: Not implemented in current version

## Database Verification

### Test Case 6.1: Database Migration
- [ ] Install previous version of the app (if available)
- [ ] Create some matches
- [ ] Update to new version with archive feature
- [ ] Verify app starts without crashing
- [ ] Verify existing matches still visible
- [ ] Verify `archived` column exists in database
- [ ] Verify all existing matches have `archived = false`

**Tools**: Use Android Studio Database Inspector
**Expected Result**: Successful migration from v1 to v2

### Test Case 6.2: Data Persistence
- [ ] Archive 2-3 matches
- [ ] Close the app completely (swipe from recent apps)
- [ ] Reopen the app
- [ ] Navigate to Matches screen
- [ ] Verify archived matches not visible in main list
- [ ] Navigate to Archived Matches
- [ ] Verify archived matches are present

**Expected Result**: Archive state persists across app restarts

## UI/UX Verification

### Test Case 7.1: Visual Design
- [ ] Verify archive icons are properly sized
- [ ] Verify icons are properly aligned
- [ ] Verify text is readable
- [ ] Verify spacing/padding is consistent
- [ ] Verify cards have proper elevation
- [ ] Verify "Archived" card stands out visually
- [ ] Verify colors match Material Design guidelines

**Expected Result**: UI follows design specifications

### Test Case 7.2: Responsiveness
- [ ] Test on different screen sizes (phone, tablet)
- [ ] Test in portrait and landscape orientations
- [ ] Verify layouts adapt correctly
- [ ] Verify no text truncation
- [ ] Verify touch targets are adequate (min 48dp)

**Expected Result**: Responsive design across devices

### Test Case 7.3: Animations and Transitions
- [ ] Verify smooth navigation transitions
- [ ] Verify no lag when archiving/unarchiving
- [ ] Verify list updates smoothly
- [ ] Verify icons animate on click (if applicable)

**Expected Result**: Smooth and polished interactions

## Error Handling

### Test Case 8.1: Network Errors (Not Applicable)
Note: This feature uses local database only, no network calls.

### Test Case 8.2: Concurrent Operations
- [ ] Rapidly click archive on multiple matches
- [ ] Verify all operations complete successfully
- [ ] Verify no crashes or UI freezes

**Expected Result**: Handles rapid actions gracefully

### Test Case 8.3: Edge Cases
- [ ] Try to archive the same match multiple times quickly
- [ ] Try to unarchive the same match multiple times quickly
- [ ] Verify no duplicate operations
- [ ] Verify state remains consistent

**Expected Result**: Proper handling of edge cases

## Accessibility Verification

### Test Case 9.1: Content Descriptions
- [ ] Enable TalkBack
- [ ] Navigate to Matches screen
- [ ] Verify archive icons have content descriptions
- [ ] Verify "Archived" card is properly announced
- [ ] Navigate to Archived Matches screen
- [ ] Verify unarchive icons have content descriptions

**Expected Result**: All interactive elements accessible via TalkBack

### Test Case 9.2: Touch Targets
- [ ] Verify all buttons/icons are at least 48x48 dp
- [ ] Verify adequate spacing between touch targets
- [ ] Test with large text settings enabled

**Expected Result**: Touch targets meet accessibility guidelines

## Performance Verification

### Test Case 10.1: Large Data Sets
- [ ] Create 50+ matches in the database
- [ ] Archive 20+ matches
- [ ] Navigate between Matches and Archived screens
- [ ] Verify no lag or frame drops
- [ ] Verify smooth scrolling

**Expected Result**: Smooth performance with large datasets

### Test Case 10.2: Memory Usage
- [ ] Use Android Profiler
- [ ] Monitor memory during archive operations
- [ ] Verify no memory leaks
- [ ] Verify reasonable memory consumption

**Expected Result**: Efficient memory usage

## Automated Test Verification

### Test Case 11.1: Unit Tests
```bash
./gradlew :usecase:test
```
- [ ] Run `ArchiveMatchUseCaseTest`
- [ ] Run `UnarchiveMatchUseCaseTest`
- [ ] Run `GetArchivedMatchesUseCaseTest`
- [ ] Verify all tests pass (3/3)

**Expected Result**: ✅ All use case tests pass

### Test Case 11.2: ViewModel Tests
```bash
./gradlew :viewmodel:test
```
- [ ] Run `MatchListViewModelTest`
- [ ] Run `ArchivedMatchesViewModelTest`
- [ ] Verify all tests pass
- [ ] Verify archive/unarchive tests pass

**Expected Result**: ✅ All ViewModel tests pass

### Test Case 11.3: Code Coverage
```bash
./gradlew jacocoTestReport
```
- [ ] Generate code coverage report
- [ ] Verify coverage for new use cases is 100%
- [ ] Verify coverage for ViewModels is >80%

**Expected Result**: High test coverage for new code

## Integration Verification

### Test Case 12.1: End-to-End Flow
Complete user journey:
- [ ] Create a new match
- [ ] Start and finish the match
- [ ] Navigate to Matches screen
- [ ] Verify match appears in played matches
- [ ] Archive the match
- [ ] Verify match hidden from main list
- [ ] Navigate to Archived Matches
- [ ] Verify match visible in archived list
- [ ] Unarchive the match
- [ ] Verify match returns to played matches

**Expected Result**: Complete flow works seamlessly

### Test Case 12.2: Compatibility with Existing Features
- [ ] Archive a match
- [ ] Verify match summary still accessible
- [ ] Verify player statistics unaffected
- [ ] Verify substitutions data intact
- [ ] Verify team roster unaffected

**Expected Result**: No interference with existing features

## Regression Testing

### Test Case 13.1: Existing Match List Features
- [ ] Create new match (wizard)
- [ ] Edit existing match
- [ ] Delete a match
- [ ] Start a match
- [ ] Pause a match
- [ ] Resume a match
- [ ] Finish a match
- [ ] View match summary

**Expected Result**: All existing features work as before

### Test Case 13.2: Navigation
- [ ] Bottom navigation bar works
- [ ] All screen transitions work
- [ ] Back button behavior correct
- [ ] Deep links work (if applicable)

**Expected Result**: Navigation unchanged by new feature

## Sign-Off Checklist

### Developer Sign-Off
- [ ] All code reviewed
- [ ] All unit tests passing
- [ ] No compiler warnings
- [ ] Code follows project conventions
- [ ] Documentation updated

### QA Sign-Off
- [ ] All functional tests passed
- [ ] No critical bugs found
- [ ] Performance acceptable
- [ ] Accessibility requirements met
- [ ] UI matches design specifications

### Product Owner Sign-Off
- [ ] Feature meets acceptance criteria
- [ ] User experience satisfactory
- [ ] Ready for production deployment

## Known Issues / Limitations

Document any known issues or limitations:

1. **Archive from Match Detail**: Not implemented in current version
2. **Confirmation Dialog**: No confirmation dialog when archiving (immediate action)
3. **Bulk Operations**: Cannot archive multiple matches at once

## Notes

- Archive functionality only applies to finished matches (status = FINISHED)
- Archived matches are completely hidden from main view
- Database migration is automatic and safe
- Feature is fully backward compatible
- No network connectivity required

## Test Results Summary

| Category | Tests Run | Passed | Failed | Blocked |
|----------|-----------|--------|--------|---------|
| Functional | | | | |
| Database | | | | |
| UI/UX | | | | |
| Performance | | | | |
| Accessibility | | | | |
| Unit Tests | 8 | | | |
| Integration | | | | |
| **Total** | | | | |

## Sign-Off

| Role | Name | Date | Signature |
|------|------|------|-----------|
| Developer | | | |
| QA Engineer | | | |
| Product Owner | | | |

---

**Version**: 1.0
**Last Updated**: 2025-10-16
**Feature**: US-6.1.5 - Archivar Partido
