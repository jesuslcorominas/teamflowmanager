# Firestore Security Rules Update for Multiple Roles Support

## Problem
The self-assignment feature requires THREE updates to Firestore security rules:
1. The `isPresidenteOfClub` helper function checks `role` (singular) instead of `roles` (array)
2. Teams update rules don't allow Presidents to update coachId
3. ClubMembers update rules don't allow Presidents or users to update their own roles

## Required Changes

### 1. Update the `isPresidenteOfClub` helper function

**Current version (line ~52):**
```javascript
function isPresidenteOfClub(clubId) {
  return clubId != null
    && exists(/databases/$(database)/documents/clubMembers/$(request.auth.uid + '_' + clubId))
    && get(/databases/$(database)/documents/clubMembers/$(request.auth.uid + '_' + clubId)).data.role == "Presidente";
}
```

**Updated version:**
```javascript
function isPresidenteOfClub(clubId) {
  return clubId != null
    && exists(/databases/$(database)/documents/clubMembers/$(request.auth.uid + '_' + clubId))
    && ('Presidente' in get(/databases/$(database)/documents/clubMembers/$(request.auth.uid + '_' + clubId)).data.roles);
}
```

**Explanation:** Changed from `data.role == "Presidente"` to `'Presidente' in data.roles` to check if "Presidente" exists in the roles array.

---

### 2. Update Teams collection rules to allow Presidents to set coachId

**Current version (around line 120):**
```javascript
match /teams/{teamId} {
  allow read: if isAuthenticated();
  
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

**Updated version:**
```javascript
match /teams/{teamId} {
  allow read: if isAuthenticated();
  
  allow create: if isAuthenticated() && (
    request.resource.data.assignedCoachId == request.auth.uid ||
    (
      'clubId' in request.resource.data &&
      request.resource.data.clubId != null &&
      isPresidenteOfClub(request.resource.data.clubId)
    )
  );

  // Allow update by:
  // 1. The team's assigned coach (for all fields)
  // 2. Presidents of the team's club (only for coachId field - for self-assignment)
  allow update: if isAuthenticated() && (
    resource.data.assignedCoachId == request.auth.uid ||
    (
      getTeamClubId(teamId) != null &&
      isPresidenteOfClub(getTeamClubId(teamId)) &&
      request.resource.data.diff(resource.data).affectedKeys().hasOnly(['coachId'])
    )
  );
  
  allow delete: if isAuthenticated() &&
    resource.data.assignedCoachId == request.auth.uid;
}
```

**Explanation:** Added a second condition to allow Presidents to update ONLY the `coachId` field of teams in their club. This is specifically for self-assignment. The `hasOnly(['coachId'])` ensures Presidents can't modify other team fields.

---

### 3. Update ClubMembers collection rules to allow role updates

**Current version (around line 95):**
```javascript
match /clubMembers/{memberId} {
  allow read: if isAuthenticated() && (
    resource.data.userId == request.auth.uid ||
    isClubOwner(resource.data.clubId)
  );

  allow create: if isAuthenticated() && (
    isClubOwner(request.resource.data.clubId) ||
    (
      request.resource.data.userId == request.auth.uid &&
      memberId == request.auth.uid + '_' + request.resource.data.clubId &&
      exists(/databases/$(database)/documents/clubs/$(request.resource.data.clubId))
    )
  );

  allow update, delete: if isAuthenticated() &&
    isClubOwner(resource.data.clubId);
}
```

**Updated version:**
```javascript
match /clubMembers/{memberId} {
  allow read: if isAuthenticated() && (
    resource.data.userId == request.auth.uid ||
    isClubOwner(resource.data.clubId)
  );

  allow create: if isAuthenticated() && (
    isClubOwner(request.resource.data.clubId) ||
    (
      request.resource.data.userId == request.auth.uid &&
      memberId == request.auth.uid + '_' + request.resource.data.clubId &&
      exists(/databases/$(database)/documents/clubs/$(request.resource.data.clubId))
    )
  );

  // Allow update by:
  // 1. Club owner (for any changes)
  // 2. Presidents adding roles to themselves (only roles field)
  // 3. User updating their own roles (only roles field, for self-assignment)
  allow update: if isAuthenticated() && (
    isClubOwner(resource.data.clubId) ||
    (
      isPresidenteOfClub(resource.data.clubId) &&
      resource.data.userId == request.auth.uid &&
      request.resource.data.diff(resource.data).affectedKeys().hasOnly(['roles'])
    )
  );
  
  allow delete: if isAuthenticated() &&
    isClubOwner(resource.data.clubId);
}
```

**Explanation:** Added a condition to allow Presidents to update ONLY the `roles` field of their own clubMember document. This enables self-assignment where Presidents add "Coach" to their roles.

---

## Summary of All Changes

1. **`isPresidenteOfClub` function**: Check `roles` array instead of `role` string
2. **Teams update rule**: Allow Presidents to update `coachId` field
3. **ClubMembers update rule**: Allow Presidents to update their own `roles` field

## Impact Analysis

These changes:
- ✅ Enable President self-assignment to teams
- ✅ Maintain all existing security (Presidents can only update specific fields)
- ✅ Don't break any existing functionality
- ✅ Use `hasOnly()` to restrict which fields can be modified

## Testing Checklist

After applying these rules:
1. ✅ Presidents should be able to self-assign as coaches
2. ✅ Presidents should still be able to create teams in their clubs
3. ✅ Presidents should NOT be able to modify other team fields
4. ✅ Presidents should NOT be able to modify other users' roles
5. ✅ Non-Presidents should NOT be able to perform President-only actions
6. ✅ Club owners maintain full control
7. ✅ Coaches maintain their existing permissions

## Deployment Steps

1. Go to Firebase Console > Firestore > Rules
2. Find and update the `isPresidenteOfClub` function (line ~52)
3. Find and update the `teams` match block (line ~120)
4. Find and update the `clubMembers` match block (line ~95)
5. Publish the rules
6. Test the self-assignment feature

## Complete Updated Functions

Here are the three complete updated sections for easy copy-paste:

```javascript
// Helper function to check if user is Presidente of a specific club
function isPresidenteOfClub(clubId) {
  return clubId != null
    && exists(/databases/$(database)/documents/clubMembers/$(request.auth.uid + '_' + clubId))
    && ('Presidente' in get(/databases/$(database)/documents/clubMembers/$(request.auth.uid + '_' + clubId)).data.roles);
}
```

```javascript
// Teams collection rules
match /teams/{teamId} {
  allow read: if isAuthenticated();
  
  allow create: if isAuthenticated() && (
    request.resource.data.assignedCoachId == request.auth.uid ||
    (
      'clubId' in request.resource.data &&
      request.resource.data.clubId != null &&
      isPresidenteOfClub(request.resource.data.clubId)
    )
  );

  allow update: if isAuthenticated() && (
    resource.data.assignedCoachId == request.auth.uid ||
    (
      getTeamClubId(teamId) != null &&
      isPresidenteOfClub(getTeamClubId(teamId)) &&
      request.resource.data.diff(resource.data).affectedKeys().hasOnly(['coachId'])
    )
  );
  
  allow delete: if isAuthenticated() &&
    resource.data.assignedCoachId == request.auth.uid;
}
```

```javascript
// ClubMembers collection rules
match /clubMembers/{memberId} {
  allow read: if isAuthenticated() && (
    resource.data.userId == request.auth.uid ||
    isClubOwner(resource.data.clubId)
  );

  allow create: if isAuthenticated() && (
    isClubOwner(request.resource.data.clubId) ||
    (
      request.resource.data.userId == request.auth.uid &&
      memberId == request.auth.uid + '_' + request.resource.data.clubId &&
      exists(/databases/$(database)/documents/clubs/$(request.resource.data.clubId))
    )
  );

  allow update: if isAuthenticated() && (
    isClubOwner(resource.data.clubId) ||
    (
      isPresidenteOfClub(resource.data.clubId) &&
      resource.data.userId == request.auth.uid &&
      request.resource.data.diff(resource.data).affectedKeys().hasOnly(['roles'])
    )
  );
  
  allow delete: if isAuthenticated() &&
    isClubOwner(resource.data.clubId);
}
```
