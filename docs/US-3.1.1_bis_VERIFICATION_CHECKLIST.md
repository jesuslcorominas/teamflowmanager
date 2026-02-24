# US-3.1.1_bis: Goal Scorers Visualization - Verification Checklist

## Pre-requisites
- App compiled and running
- At least one team created
- At least one player in the team
- At least one match played with goals scored

## Test Scenarios

### Scenario 1: Basic Navigation
- [ ] Open the app
- [ ] Navigate to "Análisis" tab in the bottom navigation bar
- [ ] Verify two tabs are visible: "TIEMPOS" and "GOLEADORES"
- [ ] Default tab shown is "TIEMPOS"
- [ ] Tap on "GOLEADORES" tab
- [ ] Verify the tab switches smoothly
- [ ] Verify the chart displays goal scorer data

### Scenario 2: Goal Chart Display (with data)
**Pre-condition**: Team has players who have scored goals

- [ ] Navigate to "Análisis" → "GOLEADORES" tab
- [ ] Verify chart displays with horizontal bars
- [ ] Verify each bar shows:
  - [ ] Player full name on the left
  - [ ] Total goals count visible
  - [ ] Colored bar (gradient from light to dark blue)
- [ ] Verify players are sorted by total goals (descending)
- [ ] Verify top scorer appears first
- [ ] Verify chart animates smoothly when first displayed

### Scenario 3: Empty State
**Pre-condition**: No goals have been scored by the team

- [ ] Navigate to "Análisis" → "GOLEADORES" tab
- [ ] Verify empty state message is displayed
- [ ] Spanish: "No hay datos de goles disponibles"
- [ ] English: "No goals data available"

### Scenario 4: Real-time Updates
- [ ] Navigate to "Análisis" → "GOLEADORES" tab
- [ ] Note the current goal counts
- [ ] Navigate to an active match
- [ ] Score a goal for a player
- [ ] Return to "Análisis" → "GOLEADORES" tab
- [ ] Verify the chart updated with the new goal
- [ ] Verify player position updated if goal count changed ranking

### Scenario 5: Multiple Players with Goals
**Pre-condition**: At least 3 players have scored goals

- [ ] Navigate to "Análisis" → "GOLEADORES" tab
- [ ] Verify all players with goals are displayed
- [ ] Verify no players without goals are displayed
- [ ] Verify correct sorting (highest goals first)
- [ ] Verify bar lengths are proportional to goal counts

### Scenario 6: Tab Switching
- [ ] Navigate to "Análisis" tab
- [ ] View "TIEMPOS" tab (default)
- [ ] Switch to "GOLEADORES" tab
- [ ] Switch back to "TIEMPOS" tab
- [ ] Verify smooth transition
- [ ] Verify each chart displays correctly when selected
- [ ] Verify no lag or performance issues

### Scenario 7: Localization (Spanish)
**Pre-condition**: Device/app language set to Spanish

- [ ] Navigate to "Análisis" tab
- [ ] Verify tab label is "GOLEADORES"
- [ ] Verify chart displays player names correctly
- [ ] If no data, verify message: "No hay datos de goles disponibles"

### Scenario 8: Localization (English)
**Pre-condition**: Device/app language set to English

- [ ] Navigate to "Análisis" tab
- [ ] Verify tab label is "SCORERS"
- [ ] Verify chart displays player names correctly
- [ ] If no data, verify message: "No goals data available"

### Scenario 9: Player with Multiple Goals
**Pre-condition**: One player has scored multiple goals across different matches

- [ ] Navigate to "Análisis" → "GOLEADORES" tab
- [ ] Find the player with multiple goals
- [ ] Verify total goals count is accurate
- [ ] Verify bar length reflects total goals

### Scenario 10: Edge Cases
- [ ] Team with exactly 1 player with 1 goal
  - [ ] Verify chart displays correctly
  - [ ] Verify no errors
- [ ] Team with 10+ players with goals
  - [ ] Verify chart is scrollable
  - [ ] Verify all players are visible
  - [ ] Verify no UI overflow
- [ ] Player name is very long
  - [ ] Verify name displays without overlap
  - [ ] Verify chart layout remains intact

### Scenario 11: Data Consistency
- [ ] Navigate to match history
- [ ] Note which players scored goals
- [ ] Navigate to "Análisis" → "GOLEADORES"
- [ ] Verify the same players appear in the chart
- [ ] Verify goal counts match match records

### Scenario 12: App Lifecycle
- [ ] Navigate to "Análisis" → "GOLEADORES" tab
- [ ] Note the current display
- [ ] Put app in background
- [ ] Bring app to foreground
- [ ] Verify chart still displays correctly
- [ ] Verify data is still accurate

## Expected Results Summary

### Visual Characteristics
- ✅ Horizontal bar chart
- ✅ Gradient bars (light blue to dark blue)
- ✅ Player names clearly visible
- ✅ Goal counts displayed
- ✅ Smooth animations
- ✅ Proper spacing and padding

### Data Accuracy
- ✅ Only shows players who have scored
- ✅ Correct goal counts
- ✅ Correct sorting (descending)
- ✅ Real-time updates
- ✅ Matches database records

### User Experience
- ✅ Easy navigation
- ✅ Clear tab labels
- ✅ Smooth tab switching
- ✅ Appropriate empty states
- ✅ Responsive layout
- ✅ No performance issues

## Bug Report Template

If any issues are found, report using this format:

```
**Issue Title**: [Brief description]

**Steps to Reproduce**:
1. 
2. 
3. 

**Expected Behavior**:


**Actual Behavior**:


**Screenshots**: (if applicable)


**Device/Platform**:


**App Version**:


**Additional Context**:

```

## Sign-off

- [ ] All test scenarios passed
- [ ] No critical bugs found
- [ ] Feature ready for production

**Tester Name**: ___________________
**Date**: ___________________
**Signature**: ___________________
