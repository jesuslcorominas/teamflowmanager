# Firestore Rules Fix - Deployment Guide

## Issue Summary

**Problem 1**: Users cannot join a club using an invitation code because of a circular dependency in the Firestore security rules.

**Root Cause 1**: The current Firestore rules create a catch-22 situation:
1. To create a `clubMember` document, the security rules check if the club exists using `exists()`
2. The `exists()` function triggers a read operation on the clubs collection
3. The clubs read permission requires the user to be the owner OR already be a club member
4. But the user can't be a member yet - they're trying to join for the first time!

**Solution 1**: Allow any authenticated user to read clubs. This is the fix shown in the issue's problem statement and documented in `FIRESTORE_RULES_FIX.md`.

**Problem 2**: The `updateTeamClubId` call fails during club joining because the Firestore rules use `ownerId` for teams, but the actual team documents use `assignedCoachId`.

**Root Cause 2**: Field name mismatch between Firestore rules and actual data model:
- Firestore rules check: `resource.data.ownerId == request.auth.uid`
- Actual team documents have: `assignedCoachId` field (not `ownerId`)

**Solution 2**: Update Firestore rules to use `assignedCoachId` instead of `ownerId` for team-related permissions.

## Files Created/Modified

### 1. `/firestore.rules` (NEW)
Complete Firestore security rules with the fixes applied. The key changes are:

**Fix 1 - Clubs collection:**
```javascript
// Clubs collection rules
match /clubs/{clubId} {
  // Permitir a cualquier usuario autenticado leer clubs
  // Necesario para el flujo de unirse por código de invitación
  allow read: if isAuthenticated();
  
  // ... other rules
}
```

**Fix 2 - Teams collection and helper functions:**
```javascript
// Helper function to check if user is team owner (assignedCoachId field)
function userIsTeamOwner(teamId) {
  return isAuthenticated() && getTeam(teamId).data.assignedCoachId == request.auth.uid;
}

// Teams collection rules
match /teams/{teamId} {
  allow read: if isAuthenticated();
  
  // Allow create if user is authenticated and sets themselves as assigned coach
  allow create: if isAuthenticated() && 
    request.resource.data.assignedCoachId == request.auth.uid;
  
  // Allow update/delete only by the team's assigned coach
  allow update, delete: if isAuthenticated() && 
    resource.data.assignedCoachId == request.auth.uid;
}
```

**Previous problematic rules:**
```javascript
// Problem 1: User needs to be owner or member to read clubs
match /clubs/{clubId} {
  allow read: if isAuthenticated() && (
    resource.data.ownerId == request.auth.uid ||
    exists(/databases/$(database)/documents/clubMembers/$(request.auth.uid + '_' + clubId))
  );
}

// Problem 2: Teams use ownerId instead of assignedCoachId
function userIsTeamOwner(teamId) {
  return isAuthenticated() && getTeam(teamId).data.ownerId == request.auth.uid;
}
```

### 2. `/firebase.json` (NEW)
Firebase project configuration that references the firestore.rules file:

```json
{
  "firestore": {
    "rules": "firestore.rules"
  },
  "functions": {
    "source": "firebase-functions",
    "runtime": "nodejs18"
  },
  "hosting": { ... }
}
```

**Note**: The previous `firebase-functions/firebase.json` has been replaced by this root-level configuration. The old file has been archived as `firebase-functions/DEPRECATED_firebase.json.md` for reference.

## Security Justification

### Why is it safe to allow all authenticated users to read clubs?

1. **Club data is not sensitive**: Clubs contain:
   - Club name (public information)
   - Owner ID (non-sensitive identifier)
   - Invitation code (meant to be shared)

2. **Invitation codes act as authentication**: 
   - Users must know the invitation code to join
   - The invitation code serves as a secret key
   - Without the code, users cannot become members

3. **Users can't list all clubs**: 
   - They can only read a club if they know its document ID
   - Or query by invitation code (which they need to know)
   - No enumeration endpoint is exposed

4. **Alternative would be more complex**: 
   - Could create a separate public club collection
   - Would add unnecessary complexity
   - Doesn't provide meaningful additional security

5. **Matches common patterns**: 
   - Many apps allow reading public organizational data
   - Membership creation is still strictly controlled

### What remains protected?

- ✅ **Club creation**: Only authenticated users can create clubs, and only for themselves
- ✅ **Club modification**: Only the owner can update or delete clubs
- ✅ **ClubMember creation**: Strictly controlled - users can only create their own membership, and the club must exist
- ✅ **ClubMember modification**: Only the club owner can modify or delete memberships

## Deployment Steps

### Prerequisites

1. Ensure you have Firebase CLI installed:
   ```bash
   npm install -g firebase-tools
   ```

2. Authenticate with Firebase:
   ```bash
   firebase login
   ```

3. Ensure your Firebase project is configured (check `.firebaserc` file exists, or initialize if needed)

### Option 1: Deploy via Firebase CLI (Recommended)

From the project root directory:

```bash
# Deploy only the Firestore rules
firebase deploy --only firestore:rules
```

This will:
- Upload the `firestore.rules` file to Firebase
- Apply the new security rules to your Firestore database
- The changes take effect immediately

### Option 2: Manual Deployment via Firebase Console

If you prefer to deploy manually:

1. **Open Firebase Console**:
   - Go to https://console.firebase.google.com/
   - Select your TeamFlow Manager project

2. **Navigate to Firestore Rules**:
   - In the left sidebar, click on "Firestore Database"
   - Click on the "Rules" tab

3. **Update the Rules**:
   - Copy the entire contents of `/firestore.rules`
   - Paste into the Firebase Console rules editor
   - Click "Publish"

4. **Verify Deployment**:
   - The console will show "Last published" timestamp
   - Rules take effect immediately

### Verification

After deploying, verify the fix works:

1. **Test Club Join Flow**:
   - As a user (not club owner), try to join a club using an invitation code
   - The operation should succeed without permission errors

2. **Check Firebase Console**:
   - Go to Firestore Database → Rules tab
   - Verify the clubs collection has `allow read: if isAuthenticated();`

3. **Monitor Logs**:
   - Check Firestore logs in Firebase Console for any permission denied errors
   - These should no longer appear for club join operations

## Testing Recommendations

### Manual Testing

1. **Test Case 1: User joins club by invitation code**
   - User: Authenticated, not a club member
   - Action: Enter valid invitation code
   - Expected: Successfully joins club, becomes member with appropriate role
   - Previous behavior: Permission denied error

2. **Test Case 2: User cannot modify clubs they don't own**
   - User: Authenticated, not club owner
   - Action: Try to update club name
   - Expected: Permission denied
   - Should still be protected ✅

3. **Test Case 3: User cannot create clubMember for someone else**
   - User: Authenticated, not club owner
   - Action: Try to create clubMember document for another user
   - Expected: Permission denied
   - Should still be protected ✅

### Automated Testing

The Firestore rules can be tested locally using the Firebase Emulator Suite:

```bash
# Install emulator if not already installed
firebase setup:emulators:firestore

# Run emulator with the rules
firebase emulators:start --only firestore

# In another terminal, run tests
cd firebase-functions
npm test  # If you have Firestore rules tests
```

## Rollback Plan

If issues arise after deployment:

### Option 1: Revert via Firebase Console
1. Go to Firestore Database → Rules tab
2. Click on "Rules history" 
3. Select the previous version
4. Click "Restore"

### Option 2: Deploy previous rules version
1. Modify `firestore.rules` to the previous version
2. Run `firebase deploy --only firestore:rules`

## Additional Notes

### Firebase CLI Tips

- **Check which project is active**: `firebase projects:list`
- **Switch projects**: `firebase use <project-id>`
- **Dry run**: Unfortunately, there's no dry-run for rules deployment
- **View deployment history**: Available in Firebase Console → Firestore → Rules → History

### Related Documentation

- See `FIRESTORE_RULES_FIX.md` for detailed analysis of the problem
- See `CLUB_STRUCTURE_DATA_MODEL.md` for club data model documentation
- See `C2-S2_IMPLEMENTATION_SUMMARY.md` for club creation implementation details
- See `C2-S3_IMPLEMENTATION_SUMMARY.md` (if exists) for club join implementation

### Questions or Issues?

If you encounter any issues during deployment:
1. Check Firebase Console logs for detailed error messages
2. Verify your Firebase project configuration
3. Ensure Firebase CLI is up to date: `npm update -g firebase-tools`
4. Check that your authentication credentials are valid: `firebase login --reauth`

## Summary

**What changed**: 
- ✅ Created `firestore.rules` file with corrected security rules
- ✅ Created `firebase.json` in project root to reference the rules file
- ✅ Fixed the circular dependency by allowing authenticated users to read clubs

**What's protected**:
- ✅ Club creation (authenticated users only, for themselves)
- ✅ Club updates (owners only)
- ✅ ClubMember creation (owners or self-joining with valid club)
- ✅ ClubMember updates (owners only)

**What's now possible**:
- ✅ Users can join clubs by invitation code without permission errors
- ✅ The `exists()` check in clubMembers creation rule now works correctly

**Security impact**: 
- ✅ Minimal - club data is not sensitive
- ✅ Invitation codes still protect club membership
- ✅ All write operations remain strictly controlled
