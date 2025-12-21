# C1-S3 — Firestore Security Rules Implementation

## Overview

This document describes the implementation of hierarchical Firestore security rules for TeamFlow Manager, establishing permission boundaries based on user roles and ownership relationships.

## Requirements

### Gherkin Scenarios

#### Write/Modification Permissions
- **Given** a User (USER_X) is authenticated
- **When** USER_X attempts to modify document `clubs/CLUB_A`
- **Then** the operation is allowed ONLY IF USER_X is the `ownerId` of CLUB_A
- **When** USER_X attempts to modify documents in `players`, `matches` or statistics (teamId = TEAM_B)
- **Then** the operation is allowed ONLY IF USER_X is the `coachId` of TEAM_B

#### Read Permissions
- **Given** a User (USER_X) is authenticated
- **When** USER_X attempts to read documents from `teams`, `players` or `matches` (teamId = TEAM_B)
- **Then** the operation is allowed IF:
  - USER_X is the `ownerId` of TEAM_B, OR
  - USER_X is the `coachId` of TEAM_B, OR
  - USER_X is the "Presidente" of the Club to which TEAM_B is linked

## Implementation

### Files Created

1. **`firestore.rules`** - Firestore security rules file
2. **`firebase.json`** - Firebase project configuration
3. **`firestore-tests/package.json`** - Test dependencies
4. **`firestore-tests/test/security-rules.test.js`** - Security rules tests
5. **`C1-S3_FIRESTORE_SECURITY_RULES.md`** - This documentation

### Data Model Notes

#### Important Field Mappings

In the TeamFlow Manager data model:
- **Teams**: The `ownerId` field represents the **coach's user ID** (coachId)
- **Players**: The `teamId` field stores the **Firestore document ID** of the team
- **Matches**: The `teamId` field stores the **Firestore document ID** of the team
- **Clubs**: The `ownerId` field represents the club owner's user ID
- **Teams with Club**: Optional `clubId` field stores the Firestore document ID of the club

## Security Rules Structure

### Collections Covered

1. **clubs** - Club documents
2. **clubMembers** - Club membership documents
3. **teams** - Team documents
4. **players** - Player documents
5. **matches** - Match documents
6. **Statistics subcollections** - Team stats, player stats, match stats

### Permission Matrix

| Collection | Create | Read | Update | Delete |
|------------|--------|------|--------|--------|
| **clubs** | User (self as owner) | Owner OR Member | Owner only | Owner only |
| **clubMembers** | Club Owner | Member OR Club Owner | Club Owner | Club Owner |
| **teams** | User (self as owner) | Coach OR Presidente | Coach only | Coach only |
| **players** | Coach | Coach OR Presidente | Coach only | Coach only |
| **matches** | Coach | Coach OR Presidente | Coach only | Coach only |
| **statistics** | Coach | Coach OR Presidente | Coach only | Coach only |

### Key Rules

#### Clubs
```javascript
// Write: Only owner can modify/delete
allow update, delete: if isAuthenticated() && 
  resource.data.ownerId == request.auth.uid;

// Read: Owner or any club member
allow read: if isAuthenticated() && (
  resource.data.ownerId == request.auth.uid ||
  exists(/databases/$(database)/documents/clubMembers/$(request.auth.uid + '_' + clubId))
);
```

#### Teams
```javascript
// Write: Only coach (ownerId) can modify/delete
allow update, delete: if isAuthenticated() && 
  resource.data.ownerId == request.auth.uid;

// Read: Coach OR Presidente of linked club
allow read: if isAuthenticated() && (
  resource.data.ownerId == request.auth.uid ||
  isPresidenteOfTeamClub(teamId)
);
```

#### Players & Matches
```javascript
// Write: Only coach of the team
allow create, update, delete: if isAuthenticated() && 
  isTeamCoach(request.resource.data.teamId);

// Read: Coach OR Presidente of linked club
allow read: if isAuthenticated() && (
  isTeamCoach(resource.data.teamId) ||
  isPresidenteOfTeamClub(resource.data.teamId)
);
```

### Helper Functions

#### `isPresidenteOfTeamClub(teamId)`
Checks if the authenticated user is a "Presidente" of the club linked to the specified team:
1. Gets the team's `clubId` using `getTeamClubId(teamId)` 
2. Checks if `clubId` is not null
3. Verifies user is Presidente of that club using `isPresidenteOfClub(clubId)`

**Implementation:**
```javascript
function isPresidenteOfTeamClub(teamId) {
  return isAuthenticated() 
    && getTeamClubId(teamId) != null
    && isPresidenteOfClub(getTeamClubId(teamId));
}

function getTeamClubId(teamId) {
  return ('clubId' in getTeam(teamId).data) ? getTeam(teamId).data.clubId : null;
}

function isPresidenteOfClub(clubId) {
  return clubId != null 
    && exists(/databases/$(database)/documents/clubMembers/$(request.auth.uid + '_' + clubId))
    && get(/databases/$(database)/documents/clubMembers/$(request.auth.uid + '_' + clubId)).data.role == "Presidente";
}
```

#### `isTeamCoach(teamId)`
Checks if the authenticated user is the coach (owner) of the specified team:
- Retrieves team document and compares `ownerId` with authenticated user ID

#### `isClubOwner(clubId)`
Checks if the authenticated user is the owner of the specified club:
- Retrieves club document and compares `ownerId` with authenticated user ID

## Test Coverage

### Test Scenarios Implemented

#### Clubs - Write Permissions
✅ Allow USER_X to modify club where USER_X is ownerId  
✅ Deny USER_Y from modifying club owned by USER_X  
✅ Allow USER_X to create a club with themselves as owner  
✅ Deny USER_X from creating a club with USER_Y as owner  
✅ Allow USER_X to delete their own club  
✅ Deny unauthenticated access to clubs  

#### Teams - Write Permissions
✅ Allow coach (USER_X) to modify their team  
✅ Deny USER_Y from modifying team coached by USER_X  
✅ Allow coach to create a team  
✅ Allow coach to delete their team  

#### Players - Write Permissions
✅ Allow coach to modify players in their team  
✅ Deny non-coach from modifying players  
✅ Allow coach to create a player in their team  
✅ Allow coach to delete a player from their team  

#### Matches - Write Permissions
✅ Allow coach to modify matches in their team  
✅ Deny non-coach from modifying matches  
✅ Allow coach to create a match for their team  
✅ Allow coach to delete a match from their team  

#### Teams - Read Permissions
✅ Allow coach to read their own team  
✅ Allow Presidente to read teams in their club  
✅ Deny unauthorized user from reading team  
✅ Deny non-Presidente club member from reading team  

#### Players - Read Permissions
✅ Allow coach to read players in their team  
✅ Allow Presidente to read players in club teams  
✅ Deny unauthorized user from reading players  

#### Matches - Read Permissions
✅ Allow coach to read matches in their team  
✅ Allow Presidente to read matches in club teams  
✅ Deny unauthorized user from reading matches  

### Running Tests

#### Prerequisites
```bash
cd firestore-tests
npm install
```

#### Start Firebase Emulator
```bash
npm run emulator:start
```

#### Run Tests (in another terminal)
```bash
cd firestore-tests
npm test
```

#### Run Tests with Emulator (automated)
```bash
npm run emulator:test
```

## Examples

### Example 1: Club Owner Modifying Club

**Scenario**: USER_X owns CLUB_A and wants to update the club name.

**Firestore Data**:
```json
// clubs/club_a
{
  "ownerId": "user_x",
  "name": "Club A",
  "invitationCode": "INVITE123"
}
```

**Operation**:
```javascript
// USER_X authenticated
await updateDoc(doc(firestore, 'clubs', 'club_a'), {
  name: 'Club A Updated'
});
```

**Result**: ✅ **ALLOWED** - USER_X is the ownerId of CLUB_A

---

### Example 2: Coach Modifying Team Player

**Scenario**: USER_X is coach of TEAM_B and wants to change a player's number.

**Firestore Data**:
```json
// teams/team_b
{
  "ownerId": "user_x",  // Coach ID
  "name": "Team B",
  "coachName": "Coach X",
  "delegateName": "Delegate X",
  "teamType": 11
}

// players/player_1
{
  "teamId": "team_b",
  "firstName": "John",
  "lastName": "Doe",
  "number": 10,
  "positions": "FW",
  "captain": false,
  "deleted": false
}
```

**Operation**:
```javascript
// USER_X authenticated
await updateDoc(doc(firestore, 'players', 'player_1'), {
  number: 11
});
```

**Result**: ✅ **ALLOWED** - USER_X is the coach (ownerId) of TEAM_B

---

### Example 3: Presidente Reading Club Team Data

**Scenario**: USER_P is "Presidente" of CLUB_A and wants to read data from TEAM_B which belongs to CLUB_A.

**Firestore Data**:
```json
// clubs/club_a
{
  "ownerId": "user_club_owner",
  "name": "Club A",
  "invitationCode": "INVITE123"
}

// clubMembers/user_p_club_a
{
  "userId": "user_p",
  "name": "Presidente User",
  "email": "presidente@club.com",
  "clubId": "club_a",
  "role": "Presidente"
}

// teams/team_b
{
  "ownerId": "user_coach",  // Different from USER_P
  "name": "Team B",
  "coachName": "Coach X",
  "delegateName": "Delegate X",
  "teamType": 11,
  "clubId": "club_a"  // Linked to CLUB_A
}

// players/player_1
{
  "teamId": "team_b",
  "firstName": "John",
  "lastName": "Doe",
  "number": 10,
  "positions": "FW",
  "captain": false,
  "deleted": false
}
```

**Operation**:
```javascript
// USER_P authenticated
await getDoc(doc(firestore, 'teams', 'team_b'));
await getDoc(doc(firestore, 'players', 'player_1'));
```

**Result**: ✅ **ALLOWED** - USER_P is "Presidente" of CLUB_A, which TEAM_B belongs to

---

### Example 4: Unauthorized User Attempting Modification (Denied)

**Scenario**: USER_Y attempts to modify TEAM_B which is owned by USER_X.

**Firestore Data**:
```json
// teams/team_b
{
  "ownerId": "user_x",  // Coach ID
  "name": "Team B",
  "coachName": "Coach X",
  "delegateName": "Delegate X",
  "teamType": 11
}
```

**Operation**:
```javascript
// USER_Y authenticated (not USER_X)
await updateDoc(doc(firestore, 'teams', 'team_b'), {
  name: 'Team B Hacked'
});
```

**Result**: ❌ **DENIED** - USER_Y is not the coach (ownerId) of TEAM_B

---

### Example 5: Non-Presidente Club Member Attempting Read (Denied)

**Scenario**: USER_R is a regular club member (not Presidente) and attempts to read TEAM_B data.

**Firestore Data**:
```json
// clubs/club_a
{
  "ownerId": "user_club_owner",
  "name": "Club A",
  "invitationCode": "INVITE123"
}

// clubMembers/user_r_club_a
{
  "userId": "user_r",
  "name": "Regular Member",
  "email": "member@club.com",
  "clubId": "club_a",
  "role": "Socio"  // Not "Presidente"
}

// teams/team_b
{
  "ownerId": "user_coach",
  "name": "Team B",
  "coachName": "Coach X",
  "delegateName": "Delegate X",
  "teamType": 11,
  "clubId": "club_a"
}
```

**Operation**:
```javascript
// USER_R authenticated
await getDoc(doc(firestore, 'teams', 'team_b'));
```

**Result**: ❌ **DENIED** - USER_R is not "Presidente" and is not the coach of TEAM_B

---

### Example 6: Orphaned Team (No Club Linkage)

**Scenario**: TEAM_C has no club linkage (clubId is null). Only the coach can read/write.

**Firestore Data**:
```json
// teams/team_c
{
  "ownerId": "user_x",  // Coach ID
  "name": "Team C",
  "coachName": "Coach X",
  "delegateName": "Delegate X",
  "teamType": 11,
  "clubId": null  // No club linkage
}
```

**Operations**:
```javascript
// USER_X (coach) authenticated
await getDoc(doc(firestore, 'teams', 'team_c'));  // ✅ ALLOWED

// USER_P (Presidente of any club) authenticated
await getDoc(doc(firestore, 'teams', 'team_c'));  // ❌ DENIED
```

**Result**: Only the coach (USER_X) can access orphaned teams

## Query Patterns

### Allowed Queries

#### Coach Querying Their Teams
```javascript
// USER_X authenticated as coach
const teamsRef = collection(firestore, 'teams');
const q = query(teamsRef, where('ownerId', '==', 'user_x'));
const snapshot = await getDocs(q);
// ✅ ALLOWED - Returns teams where USER_X is coach
```

#### Coach Querying Their Team's Players
```javascript
// USER_X authenticated as coach of team_b
const playersRef = collection(firestore, 'players');
const q = query(playersRef, where('teamId', '==', 'team_b'));
const snapshot = await getDocs(q);
// ✅ ALLOWED - Returns players in TEAM_B (if USER_X is coach)
```

#### Presidente Querying Club Teams
```javascript
// USER_P authenticated as Presidente of club_a
const teamsRef = collection(firestore, 'teams');
const q = query(teamsRef, where('clubId', '==', 'club_a'));
const snapshot = await getDocs(q);
// ✅ ALLOWED - Returns teams in CLUB_A (if USER_P is Presidente)
```

### Denied Queries

#### Unauthorized User Querying All Teams
```javascript
// USER_Y authenticated (not coach or presidente)
const teamsRef = collection(firestore, 'teams');
const snapshot = await getDocs(teamsRef);
// ❌ DENIED - Cannot query all teams without proper permissions
```

#### Coach Attempting to Query Other Teams
```javascript
// USER_X authenticated as coach of team_a
const teamsRef = collection(firestore, 'teams');
const q = query(teamsRef, where('ownerId', '==', 'user_y'));
const snapshot = await getDocs(q);
// ❌ DENIED - Cannot query teams coached by USER_Y
```

## Security Considerations

### Strengths

1. **Hierarchical Permissions**: Clear ownership and role-based access control
2. **Least Privilege**: Users can only access data they need based on their role
3. **Club Presidente Access**: Enables club-level management while restricting to specific role
4. **Coach Autonomy**: Coaches maintain full control over their teams
5. **Orphaned Teams**: Teams without clubs remain private to the coach

### Potential Issues

1. **Document Reads for Permission Checks**: Each permission check requires reading related documents (team, club, clubMember)
   - **Impact**: Increased read operations and potential cost
   - **Mitigation**: Cache frequently accessed documents in client-side code
   - **Note**: Firestore Security Rules language doesn't support variable caching or memoization within rules
   - **Example**: Checking if a user is Presidente requires reading team document, then club member document

2. **Subcollection Rules**: Statistics subcollections require fetching parent documents (player, match)
   - **Impact**: Multiple document reads per permission check
   - **Limitation**: Inherent to Firestore Security Rules - parent document data must be fetched for validation
   - **Trade-off**: Security and proper access control vs. read operation costs
   - **Mitigation**: Consider denormalizing critical permission data if read costs become significant

3. **clubMember Document ID Convention**: Uses `userId_clubId` pattern
   - **Impact**: Requires consistent ID format across application
   - **Mitigation**: Document this pattern clearly and enforce in application code

4. **Role String Matching**: Checks for exact "Presidente" string
   - **Impact**: Case-sensitive, typos will fail
   - **Mitigation**: Use constants in application code for role values

### Best Practices

1. **Always authenticate users** before making Firestore requests
2. **Use consistent document IDs** for clubMembers (userId_clubId)
3. **Set ownerId correctly** when creating clubs and teams
4. **Set teamId correctly** when creating players, matches, and statistics
5. **Set clubId correctly** when linking teams to clubs
6. **Use role constants** ("Presidente") to avoid typos
7. **Handle permission errors** gracefully in the UI

## Deployment

### Prerequisites

1. Firebase CLI installed: `npm install -g firebase-tools`
2. Firebase project initialized: `firebase init`
3. Firestore enabled in Firebase Console

### Deploy Security Rules

```bash
firebase deploy --only firestore:rules
```

### Verify Deployment

```bash
firebase firestore:rules:get
```

### Monitor Security Rule Activity

1. Open Firebase Console
2. Navigate to Firestore → Rules
3. Review rule evaluation metrics and denied requests

## Maintenance

### Adding New Collections

When adding new collections with similar permission patterns:

1. Identify the ownership field (ownerId, coachId, etc.)
2. Add appropriate helper functions if needed
3. Define read/write rules following existing patterns
4. Add comprehensive tests for all scenarios
5. Update this documentation

### Modifying Roles

If adding new roles beyond "Presidente":

1. Update `isPresidenteOfTeamClub` function or create new helper
2. Update permission checks in relevant collections
3. Add tests for new role scenarios
4. Document the new role and its permissions

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2025-12-14 | Initial implementation with hierarchical permissions |

---

**Document Status**: Complete  
**Last Updated**: 2025-12-14  
**Related Issues**: C1-S3 - Seguridad de Propiedad (Reglas de Firestore)
