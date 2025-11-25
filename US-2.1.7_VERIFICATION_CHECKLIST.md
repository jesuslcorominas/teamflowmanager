# US-2.1.7: Start Match Timer - Verification Checklist

## Prerequisites
- [ ] App is built and installed on device/emulator
- [ ] Database is clean or has sample data
- [ ] At least one team exists with players

## Test Scenario 1: Basic Match Start Flow

### Setup:
1. Create a new match:
   - Add opponent name: "Test Team"
   - Add location: "Home Stadium"
   - Select 5-7 players for starting lineup
   - Select 2-3 players as substitutes

### Verification Steps:
- [ ] Match appears in "Pending Matches" section
- [ ] "Start Match" button is visible and enabled
- [ ] No other match is currently running

### Action:
- [ ] Click "Start Match" button on the test match

### Expected Results:
- [ ] Navigation occurs to "Current Match" screen
- [ ] Match timer is visible and running
- [ ] Match timer shows 00:00 and starts incrementing
- [ ] All starting lineup players are displayed
- [ ] Each starting lineup player has timer running (showing "RUNNING" indicator)
- [ ] Player timers show 00:00 and start incrementing
- [ ] Players NOT in starting lineup do not have running timers

## Test Scenario 2: Single Active Match Enforcement

### Setup:
- [ ] Continue from Scenario 1 (match is running)
- [ ] Navigate back to Match List screen

### Verification Steps:
- [ ] The running match shows visual indication of being active
- [ ] Other pending matches are displayed
- [ ] Other pending matches have "Start Match" button DISABLED
- [ ] Error message displayed: "There is already an active match"

### Action:
- [ ] Attempt to click "Start Match" on another pending match

### Expected Result:
- [ ] Button does not respond (is disabled)
- [ ] No navigation occurs
- [ ] Original match remains running

## Test Scenario 3: Match Lifecycle Completion

### Setup:
- [ ] Continue from Scenario 2 (match is running)

### Action Steps:
1. [ ] Navigate to "Current Match" screen
2. [ ] Wait for at least 10-15 seconds to accumulate time
3. [ ] Note the match time (e.g., "00:15")
4. [ ] Note the player times
5. [ ] Click "Finish Match" button

### Expected Results:
- [ ] Match is marked as finished
- [ ] Navigation returns to previous screen or home
- [ ] Match no longer appears in "Current Match" screen
- [ ] Match appears in "Played Matches" section
- [ ] Match shows final time accumulated
- [ ] "Start Match" buttons on other pending matches are now ENABLED

## Test Scenario 4: Empty Starting Lineup

### Setup:
1. Create a new match with:
   - Opponent: "Empty Lineup Test"
   - Location: "Test Stadium"
   - NO players in starting lineup (leave empty)
   - Some players as substitutes

### Action:
- [ ] Click "Start Match" button

### Expected Results:
- [ ] Navigation occurs to "Current Match" screen
- [ ] Match timer is running
- [ ] No player timers are running (empty list or all showing 00:00 without RUNNING indicator)
- [ ] No errors or crashes occur

## Test Scenario 5: Match with Only Substitutes

### Setup:
1. Create a new match with:
   - Opponent: "Subs Only Test"
   - Location: "Test Stadium"
   - NO players in starting lineup
   - 3-5 players as substitutes only

### Action:
- [ ] Click "Start Match" button

### Expected Results:
- [ ] Navigation occurs to "Current Match" screen
- [ ] Match timer is running
- [ ] Substitute players are visible but timers are NOT running
- [ ] No errors or crashes occur

## Test Scenario 6: Multiple Match Creation and Selection

### Setup:
1. Create Match A: Opponent "Team A", with starting lineup
2. Create Match B: Opponent "Team B", with starting lineup  
3. Create Match C: Opponent "Team C", with starting lineup

### Verification Steps:
- [ ] All three matches appear in "Pending Matches" section
- [ ] Each match shows opponent name correctly
- [ ] Each match has "Start Match" button enabled

### Action Sequence:
1. [ ] Start Match A
2. [ ] Verify Match A is running in "Current Match" screen
3. [ ] Navigate back to Match List
4. [ ] Verify Matches B and C have disabled "Start Match" buttons
5. [ ] Finish Match A
6. [ ] Verify Matches B and C have enabled "Start Match" buttons again
7. [ ] Start Match B
8. [ ] Verify Match B is running in "Current Match" screen
9. [ ] Verify Match A appears in "Played Matches" section

## Test Scenario 7: Data Persistence

### Setup:
- [ ] Start a match
- [ ] Wait for some time to accumulate (e.g., 30 seconds)

### Action:
1. [ ] Close the app (kill process)
2. [ ] Reopen the app
3. [ ] Navigate to "Current Match" screen

### Expected Results:
- [ ] Match is still running
- [ ] Match timer continues from where it left off (approximately)
- [ ] Player timers continue running
- [ ] Times are correctly calculated including the time app was closed

## Test Scenario 8: Timer Accuracy

### Setup:
- [ ] Start a match
- [ ] Use a separate stopwatch/timer

### Verification:
1. [ ] Start both timers simultaneously
2. [ ] Wait for 60 seconds
3. [ ] Compare displayed time with actual stopwatch time

### Expected Results:
- [ ] Match timer shows approximately 01:00 (± 2 seconds)
- [ ] Player timers show approximately 01:00 (± 2 seconds)
- [ ] Timers update at least every second

## Test Scenario 9: UI State During Match

### Verification Steps:
- [ ] Match timer displays prominently at top of screen
- [ ] "RUNNING" indicator is visible next to match time
- [ ] Player list is scrollable if needed
- [ ] Each player card shows:
  - [ ] Player name
  - [ ] Player number
  - [ ] Current time
  - [ ] "RUNNING" indicator if timer is active
- [ ] "Finish Match" button is visible and enabled

## Test Scenario 10: Error Handling

### Test 10.1: Starting Deleted Match
1. [ ] Create and start a match
2. [ ] Force-delete the match from database (if possible via debug)
3. [ ] Try to interact with the match

### Test 10.2: Network/Storage Issues
1. [ ] Start a match
2. [ ] Simulate storage full condition (if possible)
3. [ ] Verify app doesn't crash

### Expected Results:
- [ ] Appropriate error handling (no crashes)
- [ ] User-friendly error messages if applicable

## Performance Checks

- [ ] Match starts within 1 second of button press
- [ ] Navigation is smooth (no lag)
- [ ] Timer updates are smooth (no stuttering)
- [ ] No memory leaks (observe in profiler if available)
- [ ] Battery consumption is reasonable during match

## Code Quality Checks

- [ ] All unit tests pass (run `./gradlew :usecase:test`)
- [ ] Ktlint passes (run `./gradlew ktlintCheck`)
- [ ] No warnings in build output
- [ ] Code follows existing patterns and conventions

## Regression Checks

Ensure existing functionality still works:
- [ ] Creating matches still works
- [ ] Editing matches still works
- [ ] Deleting matches still works
- [ ] Viewing match details still works
- [ ] Player management still works
- [ ] Team management still works

## Acceptance Criteria Validation

✅ **Criterio 1:** Todos los cronómetros se arrancan al iniciar un partido
- [ ] Match timer starts
- [ ] All starting lineup player timers start
- [ ] Timers are visible and updating

✅ **Criterio 2:** El partido se establece como partido arrancado
- [ ] Match has `isRunning = true` in database
- [ ] Match appears as active in UI
- [ ] Only one match can be active at a time
- [ ] Other matches cannot be started while one is running

## Sign-Off

### Tester Information:
- Name: _______________
- Date: _______________
- Device/Emulator: _______________
- OS Version: _______________

### Test Results:
- [ ] All scenarios passed
- [ ] Minor issues found (list below)
- [ ] Major issues found (list below)

### Issues Found:
1. 
2. 
3. 

### Additional Notes:
