# Firestore Security Rules Tests

This directory contains automated tests for Firestore security rules.

## Prerequisites

- Node.js (v16 or later)
- npm or yarn
- Firebase CLI

## Installation

```bash
npm install
```

## Running Tests

### Option 1: Manual (Two Terminals)

**Terminal 1 - Start Emulator:**
```bash
npm run emulator:start
```

**Terminal 2 - Run Tests:**
```bash
npm test
```

### Option 2: Automated

```bash
npm run emulator:test
```

This will start the emulator, run tests, and stop the emulator automatically.

## Test Structure

- `test/security-rules.test.js` - Main test suite covering:
  - Club write permissions
  - Team write permissions  
  - Player write permissions
  - Match write permissions
  - Team read permissions
  - Player read permissions
  - Match read permissions

## Test Scenarios

### Write Permissions
- ✅ Club owners can modify their clubs
- ✅ Coaches can modify their teams, players, and matches
- ❌ Unauthorized users cannot modify any resources

### Read Permissions
- ✅ Coaches can read their teams, players, and matches
- ✅ Club "Presidente" can read teams, players, and matches in their club
- ❌ Regular club members cannot read team data
- ❌ Unauthorized users cannot read any resources

## Debugging Failed Tests

1. Check Firebase Emulator logs in the terminal where you ran `emulator:start`
2. Review the specific assertion that failed
3. Verify the test data setup matches expected structure
4. Ensure Firestore rules file is up to date

## Continuous Integration

To run tests in CI/CD:

```bash
npm run emulator:test
```

This command handles emulator lifecycle automatically.

## Related Documentation

- `../C1-S3_FIRESTORE_SECURITY_RULES.md` - Complete security rules documentation
- `../firestore.rules` - Security rules implementation
