# US-1.2.4 Testing Guide

## Overview
This document provides a comprehensive guide for testing the Save Session functionality implemented for User Story 1.2.4.

## Test Environment Setup

### Prerequisites
- Android Studio (latest version)
- Android device or emulator (API 29+)
- TeamFlow Manager app installed
- At least one team created
- At least 2-3 players added to the team

## Unit Tests

### Running Unit Tests

#### UseCase Tests
```bash
./gradlew :usecase:test --tests SaveSessionUseCaseTest
```

**Expected Results:**
- ✅ `invoke should do nothing when no match exists`
- ✅ `invoke should save player times to history and reset`
- ✅ `invoke should calculate final elapsed time for running players`
- ✅ `invoke should not save player times with zero elapsed time`
- ✅ `invoke should save empty list when no player times exist`

#### Repository Tests
```bash
./gradlew :data:core:test --tests PlayerTimeHistoryRepositoryImplTest
./gradlew :data:core:test --tests PlayerTimeRepositoryImplTest
```

**Expected Results:**
- ✅ All PlayerTimeHistoryRepository operations work correctly
- ✅ resetAllPlayerTimes deletes all player times

### Test Coverage Summary

| Component | Test File | Tests | Status |
|-----------|-----------|-------|--------|
| SaveSessionUseCase | SaveSessionUseCaseTest.kt | 5 | ✅ Created |
| PlayerTimeHistoryRepository | PlayerTimeHistoryRepositoryImplTest.kt | 4 | ✅ Created |
| PlayerTimeRepository | PlayerTimeRepositoryImplTest.kt | 1 added | ✅ Updated |

## Manual Testing

### Test Case 1: Basic Save Session Flow

**Objective:** Verify that saving a session records player times and resets counters

**Steps:**
1. Open the app
2. Navigate to "Match Session" screen
3. Start timers for 2-3 players
4. Let them run for 30 seconds each
5. Stop all timers
6. Tap "Guardar Sesión" button

**Expected Results:**
- ✅ All player time counters reset to 0:00
- ✅ UI updates immediately
- ✅ No error messages appear
- ✅ Button remains clickable

**Database Verification:**
1. Open Android Studio Database Inspector
2. Connect to app process
3. Query: `SELECT * FROM player_time_history ORDER BY id DESC LIMIT 10`
4. Verify 2-3 new records exist with correct player IDs and times

---

### Test Case 2: Save with Running Timers

**Objective:** Verify that running timers are correctly calculated at save time

**Steps:**
1. Navigate to "Match Session" screen
2. Start timer for Player A (e.g., John Doe)
3. Wait 10 seconds
4. Start timer for Player B (e.g., Jane Smith)
5. Wait 5 seconds
6. WITHOUT stopping timers, tap "Guardar Sesión"

**Expected Results:**
- ✅ Player A saved with ~15 seconds
- ✅ Player B saved with ~5 seconds
- ✅ Both timers reset to 0:00
- ✅ No timers are running after save

**Database Verification:**
```sql
SELECT 
    p.firstName, 
    p.lastName, 
    pth.elapsedTimeMillis / 1000.0 as seconds
FROM player_time_history pth
JOIN player_entity p ON pth.playerId = p.id
ORDER BY pth.id DESC
LIMIT 10
```

---

### Test Case 3: Save with Mixed Timer States

**Objective:** Verify correct handling of stopped and running timers

**Steps:**
1. Start timer for Player A, let run 20 seconds, then stop
2. Start timer for Player B, let run 15 seconds, keep running
3. Start timer for Player C, let run 10 seconds, then stop
4. Tap "Guardar Sesión"

**Expected Results:**
- ✅ Player A saved with exactly 20 seconds (stopped timer)
- ✅ Player B saved with ~15+ seconds (running timer)
- ✅ Player C saved with exactly 10 seconds (stopped timer)
- ✅ All timers reset to 0:00

---

### Test Case 4: Save with Zero Time Players

**Objective:** Verify that players with no time are not saved to history

**Steps:**
1. Ensure 3+ players exist in roster
2. Start timer for only 1 player
3. Let it run for 30 seconds
4. Stop the timer
5. Tap "Guardar Sesión"

**Expected Results:**
- ✅ Only 1 record created in player_time_history
- ✅ Players with 0:00 time NOT saved to history
- ✅ All player counters (including non-played) reset to 0:00

**Database Verification:**
```sql
SELECT COUNT(*) FROM player_time_history 
WHERE matchId = 1 
ORDER BY savedAtMillis DESC 
LIMIT 1
```
Should return 1, not 3.

---

### Test Case 5: Multiple Save Sessions

**Objective:** Verify that multiple sessions can be saved independently

**Steps:**
1. **Session 1:**
   - Start timers for Players A and B
   - Run for 30 seconds each
   - Tap "Guardar Sesión"
2. **Session 2:**
   - Start timers for Players B and C (different players)
   - Run for 45 seconds each
   - Tap "Guardar Sesión"

**Expected Results:**
- ✅ 4 total records in player_time_history (2 from each session)
- ✅ Each session has unique savedAtMillis timestamp
- ✅ Player B has 2 separate history records
- ✅ All times correctly saved

**Database Verification:**
```sql
SELECT 
    p.firstName,
    pth.elapsedTimeMillis / 1000.0 as seconds,
    datetime(pth.savedAtMillis/1000, 'unixepoch') as saved_at
FROM player_time_history pth
JOIN player_entity p ON pth.playerId = p.id
ORDER BY pth.savedAtMillis DESC
```

---

### Test Case 6: No Match Active

**Objective:** Verify graceful handling when no match exists

**Preconditions:**
- Ensure no match record exists in database (fresh install or delete match)

**Steps:**
1. Navigate to "Match Session" screen
2. Observe UI state

**Expected Results:**
- ✅ "No active match" message displayed
- ✅ "Guardar Sesión" button NOT visible
- ✅ No crash or error

---

### Test Case 7: Empty Player List

**Objective:** Verify handling of save with no players

**Preconditions:**
- Create match but don't add any players

**Steps:**
1. Navigate to "Match Session" screen
2. Tap "Guardar Sesión" button (if visible)

**Expected Results:**
- ✅ No error occurs
- ✅ No records created in player_time_history
- ✅ UI remains stable

---

### Test Case 8: Localization

**Objective:** Verify Spanish language support

**Steps:**
1. Change device language to Spanish
2. Open app
3. Navigate to Match Session screen

**Expected Results:**
- ✅ Button text is "Guardar Sesión" (not "Save Session")
- ✅ All other UI elements in Spanish
- ✅ Button functions correctly

---

### Test Case 9: UI Responsiveness

**Objective:** Verify UI updates immediately after save

**Setup:**
- Use LiveData/Flow observers to monitor state changes

**Steps:**
1. Start 3 timers with different times
2. Observe the times displayed
3. Tap "Guardar Sesión"
4. Immediately observe UI

**Expected Results:**
- ✅ Times update to 0:00 within 1 second
- ✅ No loading spinner needed (operation is fast)
- ✅ No UI freeze or lag
- ✅ Can immediately start new timers

---

### Test Case 10: Database Integrity

**Objective:** Verify foreign key relationships and data integrity

**Steps:**
1. Save a session with player times
2. Using Database Inspector, delete a player
3. Query player_time_history

**Expected Results:**
- ✅ Related history records CASCADE deleted
- ✅ No orphaned records remain
- ✅ Database constraints enforced

**SQL Verification:**
```sql
-- Check foreign key constraints
SELECT sql FROM sqlite_master 
WHERE type='table' AND name='player_time_history';

-- Verify no orphaned records
SELECT * FROM player_time_history pth
LEFT JOIN player_entity p ON pth.playerId = p.id
WHERE p.id IS NULL;
```

---

## Regression Testing

### Areas to Test After Implementation

1. **Existing Player Time Functionality**
   - ✅ Start timer still works
   - ✅ Pause timer still works
   - ✅ Multiple timers can run simultaneously
   - ✅ Time calculations remain accurate

2. **Match Management**
   - ✅ Creating matches still works
   - ✅ Match timer functionality unchanged
   - ✅ No impact on other match features

3. **Navigation**
   - ✅ Can navigate to/from Match Session screen
   - ✅ Back button works correctly
   - ✅ No navigation state issues

---

## Performance Testing

### Test Case: Large Player Roster

**Objective:** Verify performance with many players

**Steps:**
1. Add 20+ players to roster
2. Start timers for 15+ players
3. Let them accumulate various times
4. Tap "Guardar Sesión"
5. Measure time to complete

**Expected Results:**
- ✅ Save completes in < 2 seconds
- ✅ UI remains responsive
- ✅ No ANR (Application Not Responding)
- ✅ All records saved correctly

---

## Edge Cases

### Edge Case 1: Very Long Time Values

**Test:** Player with 2+ hours of accumulated time

**Expected:** Correctly saved and displayed

### Edge Case 2: Rapid Multiple Saves

**Test:** Tap "Guardar Sesión" button rapidly (3x in 3 seconds)

**Expected:** Each tap processes correctly, no duplicate records

### Edge Case 3: Save During Timer State Change

**Test:** Tap save exactly when timer is being started/stopped

**Expected:** Consistent state, no race condition

---

## Database Inspector Queries

### Useful Queries for Manual Verification

#### View All History Records
```sql
SELECT 
    pth.id,
    p.firstName || ' ' || p.lastName as player_name,
    p.number,
    pth.elapsedTimeMillis / 1000.0 as seconds,
    datetime(pth.savedAtMillis/1000, 'unixepoch') as saved_at
FROM player_time_history pth
JOIN player_entity p ON pth.playerId = p.id
ORDER BY pth.savedAtMillis DESC, pth.id DESC
```

#### Check Current Session (Should be empty after save)
```sql
SELECT * FROM player_time
```

#### Player Time Summary
```sql
SELECT 
    p.firstName || ' ' || p.lastName as player_name,
    COUNT(*) as sessions_played,
    SUM(pth.elapsedTimeMillis) / 1000.0 / 60.0 as total_minutes
FROM player_time_history pth
JOIN player_entity p ON pth.playerId = p.id
GROUP BY pth.playerId
ORDER BY total_minutes DESC
```

#### Match Session Summary
```sql
SELECT 
    pth.matchId,
    COUNT(*) as players,
    SUM(pth.elapsedTimeMillis) / 1000.0 / 60.0 as total_minutes,
    datetime(MIN(pth.savedAtMillis)/1000, 'unixepoch') as session_date
FROM player_time_history pth
GROUP BY pth.matchId, pth.savedAtMillis
ORDER BY pth.savedAtMillis DESC
```

---

## Known Limitations

1. **No Undo:** Once saved, session cannot be undone (by design)
2. **No Confirmation:** Save happens immediately without confirmation dialog (by design)
3. **No Success Message:** No toast/snackbar shown after save (can be added in future)

---

## Test Results Template

### Test Execution Record

**Date:** _____________  
**Tester:** _____________  
**Device:** _____________  
**OS Version:** _____________  
**App Version:** _____________

| Test Case | Result | Notes |
|-----------|--------|-------|
| TC1: Basic Save | ⬜ Pass / ⬜ Fail | |
| TC2: Running Timers | ⬜ Pass / ⬜ Fail | |
| TC3: Mixed States | ⬜ Pass / ⬜ Fail | |
| TC4: Zero Time | ⬜ Pass / ⬜ Fail | |
| TC5: Multiple Sessions | ⬜ Pass / ⬜ Fail | |
| TC6: No Match | ⬜ Pass / ⬜ Fail | |
| TC7: Empty List | ⬜ Pass / ⬜ Fail | |
| TC8: Localization | ⬜ Pass / ⬜ Fail | |
| TC9: UI Response | ⬜ Pass / ⬜ Fail | |
| TC10: DB Integrity | ⬜ Pass / ⬜ Fail | |

**Overall Result:** ⬜ Approved / ⬜ Needs Fixes

**Issues Found:**
1. _________________________
2. _________________________
3. _________________________

---

## Automated Test Execution

### Run All Related Tests
```bash
# Run all tests in usecase module
./gradlew :usecase:test

# Run all tests in data:core module
./gradlew :data:core:test

# Run specific test class
./gradlew :usecase:test --tests SaveSessionUseCaseTest

# Run with coverage
./gradlew :usecase:testDebugUnitTestCoverage
```

### Expected Test Results
- **Total Tests:** 10+ (including existing tests)
- **New Tests:** 10 (5 use case + 4 repository + 1 updated)
- **Pass Rate:** 100%
- **Code Coverage:** >80% for new code

---

## Troubleshooting

### Issue: Button not visible
**Solution:** Ensure match exists in database

### Issue: Times not resetting
**Solution:** Check PlayerTimeRepository.resetAllPlayerTimes() implementation

### Issue: History not saved
**Solution:** Check SaveSessionUseCase and database write permissions

### Issue: App crashes on save
**Solution:** Check logcat for exceptions, verify database schema migration

---

## Acceptance Criteria Verification Checklist

- [ ] ✅ Records persist in Room database
- [ ] ✅ Counter resets to zero after save
- [ ] ✅ Historical data preserved for each session
- [ ] ✅ Unit tests use Mockk and JUnit
- [ ] ✅ Clean architecture with proper layers
- [ ] ✅ KMM-compatible implementation
- [ ] ✅ UI has "Guardar Sesión" button
- [ ] ✅ Button is functional and accessible
- [ ] ✅ Spanish localization working
- [ ] ✅ No regression in existing features

**Final Sign-off:** ⬜ Approved for Production

---

**Document Version:** 1.0  
**Last Updated:** 2025-10-11  
**Status:** Ready for Testing
