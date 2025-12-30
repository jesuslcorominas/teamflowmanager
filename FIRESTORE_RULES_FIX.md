# Firestore Rules Fix for Join Club Feature

## Problem

When a user tries to join a club using an invitation code, they need to create a `clubMember` document for themselves. However, the current Firestore security rules only allow the club owner to create `clubMembers`:

```javascript
// ClubMembers collection rules
match /clubMembers/{memberId} {
  // Allow create/update/delete only by club owner
  allow create, update, delete: if isAuthenticated() && 
    isClubOwner(request.resource.data.clubId);
}
```

This prevents users from joining clubs because they are not the club owner.

## Solution

Update the `clubMembers` rule to allow users to create their own clubMember document when joining a club. The rule should:

1. Allow users to create a clubMember for themselves (userId == auth.uid)
2. Ensure the document ID follows the format: `userId_clubId`
3. Ensure the club exists (can be verified by reading the club document)
4. Still allow club owners to create/update/delete any clubMember in their club

## Updated Rule

Replace the existing `clubMembers` rule with:

```javascript
// ClubMembers collection rules
match /clubMembers/{memberId} {
  // Allow read if user is the member or the club owner
  allow read: if isAuthenticated() && (
    resource.data.userId == request.auth.uid ||
    isClubOwner(resource.data.clubId)
  );
  
  // Allow create by:
  // 1. Club owner (for any member)
  // 2. User creating their own membership (userId matches auth.uid and document ID is userId_clubId)
  allow create: if isAuthenticated() && (
    isClubOwner(request.resource.data.clubId) ||
    (
      request.resource.data.userId == request.auth.uid &&
      memberId == request.auth.uid + '_' + request.resource.data.clubId &&
      exists(/databases/$(database)/documents/clubs/$(request.resource.data.clubId))
    )
  );
  
  // Allow update/delete only by club owner
  allow update, delete: if isAuthenticated() && 
    isClubOwner(request.resource.data.clubId);
}
```

## Explanation

The updated `create` rule allows:

1. **Club owners** to create any clubMember in their club (existing behavior)
2. **Any authenticated user** to create a clubMember for themselves IF:
   - The `userId` in the document matches their authenticated user ID
   - The document ID follows the required format: `userId_clubId`
   - The club exists (verified by checking if the club document exists)

This allows users to join clubs using invitation codes while maintaining security by:
- Preventing users from creating memberships for other users
- Enforcing the document ID format required by security rules
- Verifying the club exists before allowing membership creation
- Keeping update/delete permissions restricted to club owners only
