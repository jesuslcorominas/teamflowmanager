# C1-S2 Team-Club Linkage - Implementation Guide

## Overview

This document describes the implementation of the team-club linkage feature (issue C1-S2) for TeamFlow Manager. This feature allows teams to optionally belong to a club, establishing a relationship between the `teams` and `clubs` collections in Firestore.

## Feature Description

Teams can now be linked to a club or remain as "orphaned" teams (without club association). This provides flexibility for:
- **Orphaned Teams**: Teams that operate independently without belonging to any club (clubId = null)
- **Club Teams**: Teams that belong to a specific club (clubId = club's Firestore document ID)

## Changes Summary

### 1. Domain Model Updates

**File**: `domain/src/main/kotlin/com/jesuslcorominas/teamflowmanager/domain/model/Team.kt`

Added two new optional fields to the `Team` data class:

```kotlin
data class Team(
    val id: Long,
    val name: String,
    val coachName: String,
    val delegateName: String,
    val captainId: Long? = null,
    val teamType: TeamType,
    val coachId: String? = null,
    val clubId: Long? = null,              // NEW: Club ID (Long) - null for orphaned teams
    val clubFirestoreId: String? = null,   // NEW: Original Firestore document ID of the club
)
```

#### Field Descriptions

- **clubId** (`Long?`): The club's ID converted to a Long using the `toStableId()` utility function. This maintains consistency with other domain model IDs. Can be `null` for orphaned teams.
- **clubFirestoreId** (`String?`): The original Firestore document ID of the club. Stored for bidirectional conversion between domain and Firestore models. Can be `null` for orphaned teams.

### 2. Firestore Model Updates

**File**: `data/remote/src/main/java/com/jesuslcorominas/teamflowmanager/data/remote/firestore/TeamFirestoreModel.kt`

Added one new optional field to the `TeamFirestoreModel` data class:

```kotlin
data class TeamFirestoreModel(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val coachName: String = "",
    val delegateName: String = "",
    val captainId: Long? = null,
    val teamType: Int = TeamType.FOOTBALL_5.players,
    val ownerId: String = "",
    val clubId: String? = null,  // NEW: Club Firestore document ID - can be null
) {
    constructor() : this(
        id = "",
        name = "",
        coachName = "",
        delegateName = "",
        captainId = null,
        teamType = TeamType.FOOTBALL_5.players,
        ownerId = "",
        clubId = null,
    )
}
```

#### Updated Conversion Functions

**From Firestore to Domain** (`toDomain()`):
```kotlin
fun TeamFirestoreModel.toDomain(): Team =
    Team(
        id = id.toStableId(),
        name = name,
        coachName = coachName,
        delegateName = delegateName,
        captainId = captainId,
        teamType = TeamType.fromPlayers(teamType),
        coachId = id,
        clubId = clubId?.takeIf { it.isNotEmpty() }?.toStableId(), // Convert to Long, null if empty/null
        clubFirestoreId = clubId?.takeIf { it.isNotEmpty() },      // Store original Firestore ID
    )
```

**From Domain to Firestore** (`toFirestoreModel()`):
```kotlin
fun Team.toFirestoreModel(): TeamFirestoreModel =
    TeamFirestoreModel(
        id = coachId ?: "",
        name = name,
        coachName = coachName,
        delegateName = delegateName,
        captainId = captainId,
        teamType = teamType.players,
        clubId = clubFirestoreId,  // Use stored Firestore ID, null for orphaned teams
    )
```

### 3. Data Source Compatibility

**File**: `data/remote/src/main/java/com/jesuslcorominas/teamflowmanager/data/remote/datasource/TeamFirestoreDataSourceImpl.kt`

**No changes required** - The data source implementation automatically handles the new field through:
- Firestore's automatic serialization/deserialization
- The updated conversion functions (`toDomain()` and `toFirestoreModel()`)
- The `.copy()` method preserving all fields including the new `clubId`

### 4. Test Updates

**File**: `data/core/src/test/kotlin/com/jesuslcorominas/teamflowmanager/data/core/repository/TeamRepositoryImplTest.kt`

Added new test cases to validate clubId handling:

1. **Test creating team with clubId**: Validates that teams can be created with a club association
2. **Test creating orphaned team**: Validates that teams can be created without a club (null clubId)
3. **Test updating team with clubId**: Validates that team club associations can be updated
4. **Test getting team with clubId**: Validates that teams with club associations are properly retrieved

## Usage Examples

### Creating an Orphaned Team

```kotlin
val orphanedTeam = Team(
    id = 0,
    name = "Independent Team",
    coachName = "John Doe",
    delegateName = "Jane Smith",
    teamType = TeamType.FOOTBALL_11,
    clubId = null,              // No club association
    clubFirestoreId = null,     // No club association
)

createTeamUseCase(orphanedTeam)
```

### Creating a Team Belonging to a Club

```kotlin
val clubTeam = Team(
    id = 0,
    name = "Club Youth Team",
    coachName = "John Doe",
    delegateName = "Jane Smith",
    teamType = TeamType.FOOTBALL_11,
    clubId = 123456789L,              // Club's Long ID (converted from Firestore ID)
    clubFirestoreId = "club_abc123",  // Original Firestore document ID
)

createTeamUseCase(clubTeam)
```

### Linking an Existing Team to a Club

```kotlin
val existingTeam = getTeamUseCase().first()

val linkedTeam = existingTeam?.copy(
    clubId = 987654321L,
    clubFirestoreId = "club_xyz789",
)

if (linkedTeam != null) {
    updateTeamUseCase(linkedTeam)
}
```

### Unlinking a Team from a Club

```kotlin
val existingTeam = getTeamUseCase().first()

val orphanedTeam = existingTeam?.copy(
    clubId = null,
    clubFirestoreId = null,
)

if (orphanedTeam != null) {
    updateTeamUseCase(orphanedTeam)
}
```

## Firestore Document Examples

### Orphaned Team Document

```json
{
  "id": "team_abc123",
  "ownerId": "user_firebase_uid_001",
  "name": "Independent Team",
  "coachName": "John Doe",
  "delegateName": "Jane Smith",
  "captainId": null,
  "teamType": 11,
  "clubId": null
}
```

### Team Belonging to a Club

```json
{
  "id": "team_def456",
  "ownerId": "user_firebase_uid_002",
  "name": "Club Youth Team",
  "coachName": "John Doe",
  "delegateName": "Jane Smith",
  "captainId": 789,
  "teamType": 11,
  "clubId": "club_abc123xyz"
}
```

## Migration Guide for Existing Teams

### Automatic Migration

**No migration required for existing teams.** All existing team documents will automatically be treated as orphaned teams because:

1. The `clubId` field is optional (nullable)
2. Existing documents without a `clubId` field will deserialize with `clubId = null`
3. The conversion functions handle null values correctly
4. All existing functionality continues to work without modification

### Manual Migration (If Needed)

If you need to bulk-assign existing teams to clubs, you can use a script like this:

```kotlin
// Example Kotlin script to assign teams to a club
suspend fun assignTeamsToClub(clubFirestoreId: String, teamIds: List<String>) {
    val firestore = FirebaseFirestore.getInstance()
    
    teamIds.forEach { teamId ->
        firestore.collection("teams")
            .document(teamId)
            .update("clubId", clubFirestoreId)
            .await()
    }
    
    Log.d("Migration", "Assigned ${teamIds.size} teams to club $clubFirestoreId")
}
```

Or using Firebase Console:
1. Navigate to Firestore Database
2. Select the `teams` collection
3. Open a team document
4. Add field: `clubId` (String), value: the club's document ID
5. Click "Update"

## Schema Validation

### Team Schema

| Field | Type | Required | Description | Default |
|-------|------|----------|-------------|---------|
| id | String | Yes | Firestore document ID (auto-generated) | - |
| ownerId | String | Yes | Firebase user ID of the team owner | - |
| name | String | Yes | Team name | - |
| coachName | String | Yes | Coach name | - |
| delegateName | String | Yes | Delegate name | - |
| captainId | Long | No | Captain player ID | null |
| teamType | Int | Yes | Number of players (5, 7, 9, 11) | 5 |
| clubId | String | No | **NEW** - Club Firestore document ID | null |

### Validation Rules

1. **clubId can be null**: Teams don't need to belong to a club
2. **clubId can be any valid Firestore document ID**: No validation against the clubs collection (allows flexibility)
3. **clubId can be changed**: Teams can be moved between clubs or made orphaned
4. **No cascade delete**: Deleting a club doesn't automatically delete its teams (teams become orphaned)

## Security Considerations

### Firestore Security Rules

The existing team security rules don't need changes, but you may want to add additional rules for club-team relationships:

```javascript
// Optional: Add rule to allow club owners to read their teams
match /teams/{teamId} {
  // Existing rules...
  
  // Allow club owner to read teams belonging to their club
  allow read: if request.auth != null && 
    resource.data.clubId != null &&
    exists(/databases/$(database)/documents/clubs/$(resource.data.clubId)) &&
    get(/databases/$(database)/documents/clubs/$(resource.data.clubId)).data.ownerId == request.auth.uid;
}
```

### Recommended Queries

**Get all teams belonging to a club:**
```kotlin
firestore.collection("teams")
    .whereEqualTo("clubId", clubFirestoreId)
    .get()
```

**Get all orphaned teams owned by a user:**
```kotlin
firestore.collection("teams")
    .whereEqualTo("ownerId", userId)
    .whereEqualTo("clubId", null)
    .get()
```

**Get all teams (orphaned or with club) owned by a user:**
```kotlin
firestore.collection("teams")
    .whereEqualTo("ownerId", userId)
    .get()
```

## Testing

### Unit Tests

New tests added to `TeamRepositoryImplTest.kt`:
- ✅ Creating team with clubId
- ✅ Creating orphaned team (null clubId)
- ✅ Updating team with clubId
- ✅ Getting team with clubId

### Integration Testing

To test the full flow:

1. **Create a club** using the club creation flow
2. **Create a team** and associate it with the club by setting `clubId` and `clubFirestoreId`
3. **Verify** the team is correctly linked by querying teams by clubId
4. **Update** the team to remove the club association (set clubId to null)
5. **Verify** the team is now orphaned

## Future Enhancements

Potential features to build on this foundation:

1. **Cascade Operations**: When a club is deleted, prompt to either delete teams or make them orphaned
2. **Team Transfer**: UI flow to move teams between clubs
3. **Club Dashboard**: Show all teams belonging to a club
4. **Access Control**: Allow club members to view/manage club teams based on their role
5. **Statistics**: Aggregate statistics across all teams in a club
6. **Validation**: Optionally validate that clubId references an existing club

## Acceptance Criteria Status

All acceptance criteria from issue C1-S2 have been met:

✅ **Updated Team schema**: Added `clubId: String | null` field to TeamFirestoreModel
✅ **Updated domain model**: Added `clubId: Long?` and `clubFirestoreId: String?` to Team
✅ **Updated validations**: Conversion functions handle null clubId correctly
✅ **Code handles orphaned teams**: All create/read/update operations support null clubId
✅ **Documentation provided**: This document serves as migration guide and documentation
✅ **Examples provided**: Multiple usage examples and Firestore document examples included
✅ **Tests added**: Unit tests validate orphaned and club-linked teams

## Version History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-12-14 | TeamFlow Manager | Initial implementation of team-club linkage |

---

**Document Status**: Final  
**Last Updated**: 2025-12-14  
**Related Issues**: C1-S2 - Vinculación de Equipo  
**Related Documents**: 
- `CLUB_STRUCTURE_DATA_MODEL.md`
- `CLUB_STRUCTURE_MIGRATION_GUIDE.md`
- `C1-S1_IMPLEMENTATION_SUMMARY.md`
