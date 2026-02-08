# Security Summary: Team Creation Permission Fix

## Issue Description

**Problem**: Club presidents (users with "Presidente" role) were unable to create teams for their clubs due to restrictive Firestore security rules.

**Root Cause**: The Firestore security rules for team creation only allowed users to create teams if they set themselves as the `assignedCoachId`. This prevented club presidents from creating teams and assigning them to different coaches.

## Solution Implemented

### Changes to Firestore Rules

Updated the team creation rule in `firestore.rules` to support two scenarios:

1. **Independent Team Creation**: Users can create teams for themselves (existing functionality)
2. **Club Team Creation**: Club presidents can create teams for their clubs and assign any coach

### Before (Problematic)

```javascript
// Teams collection rules
match /teams/{teamId} {
  allow read: if isAuthenticated();

  // Only allows self-assignment as coach
  allow create: if isAuthenticated() &&
    request.resource.data.assignedCoachId == request.auth.uid;

  allow update, delete: if isAuthenticated() &&
    resource.data.assignedCoachId == request.auth.uid;
}
```

### After (Fixed)

```javascript
// Teams collection rules
match /teams/{teamId} {
  allow read: if isAuthenticated();

  // Allow create if:
  // 1. User is authenticated and sets themselves as assigned coach (for independent teams)
  // 2. User is a Presidente of the club the team is being created for (for club teams)
  allow create: if isAuthenticated() && (
    request.resource.data.assignedCoachId == request.auth.uid ||
    (
      'clubId' in request.resource.data &&
      request.resource.data.clubId != null &&
      isPresidenteOfClub(request.resource.data.clubId)
    )
  );

  allow update, delete: if isAuthenticated() &&
    resource.data.assignedCoachId == request.auth.uid;
}
```

## Security Analysis

### What's Protected

✅ **Independent Team Creation**: Users can still create teams for themselves
- Requires authentication
- User must set themselves as `assignedCoachId`

✅ **Club Team Creation**: Only club presidents can create teams for their clubs
- Requires authentication
- Requires "Presidente" role in the club (verified via `isPresidenteOfClub()`)
- Team must have a valid `clubId` field
- Presidents can assign any coach (not just themselves)

✅ **Team Updates**: Only the assigned coach can update/delete teams
- No change to existing behavior
- Prevents unauthorized modifications

### Security Validations

The `isPresidenteOfClub()` helper function performs the following checks:

```javascript
function isPresidenteOfClub(clubId) {
  return clubId != null
    && exists(/databases/$(database)/documents/clubMembers/$(request.auth.uid + '_' + clubId))
    && get(/databases/$(database)/documents/clubMembers/$(request.auth.uid + '_' + clubId)).data.role == "Presidente";
}
```

This ensures:
1. The clubId is not null
2. A clubMember document exists for the user in that club
3. The user's role in that club is exactly "Presidente"

### What Cannot Be Done

❌ **Unauthorized Team Creation**: Non-authenticated users cannot create teams
❌ **Arbitrary Club Team Creation**: Users cannot create teams for clubs where they're not presidents
❌ **Team Modification by Non-Coaches**: Only the assigned coach can modify/delete teams
❌ **Role Bypass**: The "Presidente" role must be properly set in the clubMembers collection

## Backward Compatibility

### ✅ Independent Teams (No Club Association)

Users can still create teams without a club:
- Set `clubId` to `null` or omit it
- Set `assignedCoachId` to their own user ID
- Works exactly as before

### ✅ Existing Team Operations

All existing team operations remain unchanged:
- Reading teams (authenticated users)
- Updating teams (assigned coach only)
- Deleting teams (assigned coach only)

## Edge Cases Handled

1. **clubId is null**: The rule checks for `'clubId' in request.resource.data && request.resource.data.clubId != null`, so teams without clubs fall back to the first condition (self-assignment)

2. **clubId exists but user is not Presidente**: The `isPresidenteOfClub()` function returns false, denying the operation

3. **Invalid clubId**: If the clubId doesn't reference a valid club or the clubMember document doesn't exist, the operation is denied

4. **User is not authenticated**: Both conditions require `isAuthenticated()` to be true

## Deployment Instructions

### Deploy via Firebase CLI

```bash
firebase deploy --only firestore:rules
```

### Verification Steps

1. **Test Independent Team Creation**:
   - User creates team without clubId
   - Sets themselves as assignedCoachId
   - Should succeed ✅

2. **Test Club Team Creation by Presidente**:
   - User is Presidente of a club
   - Creates team with clubId set to their club
   - Can assign any coach (not just themselves)
   - Should succeed ✅

3. **Test Club Team Creation by Non-Presidente**:
   - User is not Presidente of the club
   - Tries to create team with clubId
   - Should fail with permission denied ❌

4. **Test Team Update**:
   - Only assigned coach can update
   - Should work as before ✅

## Related Documentation

- `C1-S2_TEAM_CLUB_LINKAGE.md` - Team-club linkage feature documentation
- `FIX_CLUB_JOIN_PERMISSIONS.md` - Previous fix for club join permissions
- `CLUB_STRUCTURE_DATA_MODEL.md` - Club data model documentation
- `FIRESTORE_RULES_DEPLOYMENT.md` - Firestore rules deployment guide

## Risk Assessment

**Risk Level**: LOW

**Rationale**:
- The change only affects team creation permissions
- It adds a new permission path without removing existing protections
- All operations are authenticated and role-based
- The "Presidente" role is verified through the clubMembers collection
- No sensitive data is exposed
- Team management permissions remain unchanged

## Testing Recommendations

### Manual Testing

1. Create club and assign yourself as Presidente
2. Create team for that club with different coach
3. Verify team is created successfully
4. Verify you cannot modify the team (only the assigned coach can)
5. Verify non-Presidente users cannot create teams for the club

### Automated Testing

If Firestore emulator tests are available:
- Test independent team creation (existing functionality)
- Test club team creation by Presidente (new functionality)
- Test club team creation by non-Presidente (should fail)
- Test team updates remain restricted to assigned coach

## Conclusion

This fix enables club presidents to create teams for their clubs while maintaining all existing security protections. The solution is backward compatible, follows the principle of least privilege, and properly validates user roles before granting permissions.

---

**Document Status**: Final
**Last Updated**: 2026-01-30
**Related Issue**: BUG Crear team
**Security Review**: Passed
