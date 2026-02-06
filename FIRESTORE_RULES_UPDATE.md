# Firestore Security Rules Update for Multiple Roles Support

## Problem
The security rules still check for `role` (singular) field, but we changed the data model to use `roles` (list). This causes permission errors when Presidents try to self-assign as coaches.

## Required Changes

### Update the `isPresidenteOfClub` helper function

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

### Explanation
- Changed from `data.role == "Presidente"` to `'Presidente' in data.roles`
- The `in` operator checks if "Presidente" exists in the `roles` array
- This works with the new multiple roles data model where `roles` is `List<String>`

### Impact Analysis
This change affects the following functions that depend on `isPresidenteOfClub`:
- `isPresidenteOfTeamClub()` - Used to check if user is President of a team's club
- Team creation rules - Allows Presidents to create teams
- ClubMembers update rules - Referenced for club owner checks

All existing functionality should continue to work correctly with this change, as we're just updating the field name from singular to plural and using array membership check.

## Testing Checklist
After applying this rule:
1. ✅ Presidents should be able to self-assign as coaches
2. ✅ Presidents should still be able to create teams in their clubs
3. ✅ Presidents should still be able to manage club members
4. ✅ Non-Presidents should NOT be able to perform President-only actions
5. ✅ Coaches and Staff members should maintain their existing permissions

## Deployment
1. Copy the updated function to Firebase Console > Firestore > Rules
2. Replace the old `isPresidenteOfClub` function with the new version
3. Publish the rules
4. Test the self-assignment feature

## Alternative: Support Both Old and New Format (Migration Period)
If you want to support both formats during migration:

```javascript
function isPresidenteOfClub(clubId) {
  let member = get(/databases/$(database)/documents/clubMembers/$(request.auth.uid + '_' + clubId)).data;
  return clubId != null
    && exists(/databases/$(database)/documents/clubMembers/$(request.auth.uid + '_' + clubId))
    && (
      // New format: roles array
      ('roles' in member && 'Presidente' in member.roles)
      // Old format: role string (for backward compatibility)
      || ('role' in member && member.role == "Presidente")
    );
}
```

This version checks both the new `roles` array and the old `role` field for backward compatibility.
