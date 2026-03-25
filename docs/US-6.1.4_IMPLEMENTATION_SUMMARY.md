# US-6.1.4: Captain Selection - Implementation Summary

## Overview

This feature implements the ability to designate a fixed team captain that will be automatically used in match creation, eliminating the need to select a captain for each match.

## Implementation Details

### 1. Database Changes

#### Migration
**File**: `data/local/src/main/java/com/jesuslcorominas/teamflowmanager/data/local/di/DataLocalModule.kt`

Created migration from version 1 to 2:
```kotlin
private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `players` ADD COLUMN `isCaptain` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `team` ADD COLUMN `captainId` INTEGER")
    }
}
```

#### Updated Entities
- **PlayerEntity**: Added `isCaptain: Boolean = false`
- **TeamEntity**: Added `captainId: Long? = null`

### 2. Domain Models

- **Player**: Added `isCaptain: Boolean = false`
- **Team**: Added `captainId: Long? = null`

### 3. Data Layer

#### PlayerDao
Added methods with @Transaction annotation for atomic operations:
- `getCaptainPlayer(): PlayerEntity?` - Get the current captain
- `clearAllCaptains()` - Remove captain status from all players
- `getPlayerById(playerId: Long): PlayerEntity?` - Get a specific player
- `setPlayerAsCaptain(playerId: Long)` - Set a player as captain (transactional)
- `removePlayerAsCaptain(playerId: Long)` - Remove captain status (transactional)

#### MatchDao
Added methods for scheduled matches:
- `getScheduledMatches(): List<MatchEntity>` - Get all scheduled (non-archived, non-started) matches
- `updateMatchCaptain(matchId: Long, captainId: Long?)` - Update captain for a specific match

### 4. Use Cases

#### GetCaptainPlayerUseCase
Retrieves the player marked as captain:
```kotlin
suspend operator fun invoke(): Player?
```

#### UpdateScheduledMatchesCaptainUseCase
Updates the captain for all scheduled matches:
```kotlin
suspend operator fun invoke(captainId: Long?)
```

### 5. ViewModel Layer

#### PlayerViewModel
Added comprehensive captain management logic:
- Checks for existing captain when saving a player
- Shows confirmation dialogs for captain changes
- Handles scheduled matches when removing a captain
- Three confirmation states:
  - `ConfirmReplace`: Replacing an existing captain
  - `ConfirmRemove`: Removing captain status (no scheduled matches)
  - `ConfirmRemoveWithMatches`: Removing captain with scheduled matches (offers choice to keep/remove from matches)

#### TeamViewModel
- Displays the current captain in team details
- Added `removeCaptain()` method for future team edit functionality

#### MatchCreationWizardViewModel
- Automatically skips captain selection step if:
  - A fixed captain exists
  - The captain is in the squad callup
- Auto-assigns the captain and jumps from SQUAD_CALLUP to STARTING_LINEUP

### 6. UI Components

#### PlayerDialog
Added captain checkbox:
- Displays after positions section
- Checkbox labeled "Capitán del Equipo" / "Team Captain"
- Integrated into form state

#### CaptainConfirmationDialog
Three overloaded composables for different confirmation scenarios:
1. **Replace Captain**: Shows current and new captain names
2. **Remove Captain**: Simple confirmation
3. **Remove with Matches**: Offers two choices - keep in matches or remove from matches

#### TeamDetailScreen
Displays captain information when available:
```
Captain: [First Name] [Last Name] (#[Number])
```

### 7. Strings

Added bilingual strings (English/Spanish):
- `is_captain` - "Team Captain" / "Capitán del Equipo"
- `captain_confirm_title` - Confirmation dialog titles
- `captain_confirm_message` - Confirmation messages
- `captain_remove_title` - Remove confirmation title
- `captain_remove_message` - Remove confirmation message
- `captain_remove_with_matches_message` - Message for matches scenario
- `keep_captain_for_matches` - Keep option
- `remove_captain_from_matches` - Remove option
- `team_captain` - Display label

### 8. Testing

#### Unit Tests
- **GetCaptainPlayerUseCaseTest**: Tests captain retrieval
- **UpdateScheduledMatchesCaptainUseCaseTest**: Tests bulk update of scheduled matches

## User Flows

### Flow 1: Creating a Player as Captain

1. User opens player creation dialog
2. Fills in player details
3. Checks "Team Captain" checkbox
4. Clicks Save
5. **If no existing captain**: Player is saved as captain
6. **If existing captain exists**: 
   - Confirmation dialog shows with current and new captain names
   - User can confirm (replaces captain) or cancel
   - If scheduled matches exist, they are updated with the new captain

### Flow 2: Removing Captain Status

1. User edits the current captain player
2. Unchecks "Team Captain" checkbox
3. Clicks Save
4. **If no scheduled matches**: Simple confirmation dialog
5. **If scheduled matches exist**:
   - Dialog shows number of scheduled matches
   - Two options: "Keep as captain in scheduled matches" or "Remove from scheduled matches"
   - Updates matches based on user choice

### Flow 3: Match Creation with Fixed Captain

1. User starts match creation wizard
2. Completes general data step
3. Selects squad callup (including captain)
4. Clicks Next
5. **If captain is in squad**: Wizard skips captain selection, goes to starting lineup
6. **If captain not in squad**: Normal captain selection step appears

### Flow 4: Team Detail View

1. User navigates to team detail screen
2. Team information displays:
   - Team Name
   - Coach Name
   - Delegate Name
   - **Captain** (if set): "John Doe (#10)"

## Technical Highlights

### Transactional Operations
Used Room's `@Transaction` annotation on DAO methods to ensure atomic captain changes:
```kotlin
@Transaction
suspend fun setPlayerAsCaptain(playerId: Long) {
    clearAllCaptains()
    val player = getPlayerById(playerId)
    if (player != null) {
        updatePlayer(player.copy(isCaptain = true))
    }
}
```

### Async State Management
ViewModels use coroutines to handle async operations:
- Database queries run in background
- UI state flows update reactively
- Confirmation dialogs appear when needed

### Smart Navigation
Match wizard intelligently determines next step:
```kotlin
WizardStep.SQUAD_CALLUP -> {
    val fixedCaptain = getCaptainPlayerUseCase.invoke()
    if (fixedCaptain != null && fixedCaptain.id in squadCallUpIds) {
        captainId = fixedCaptain.id
        WizardStep.STARTING_LINEUP
    } else {
        WizardStep.CAPTAIN
    }
}
```

## Database Schema

### players Table
```sql
CREATE TABLE players (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    firstName TEXT NOT NULL,
    lastName TEXT NOT NULL,
    number INTEGER NOT NULL,
    positions TEXT NOT NULL,
    teamId INTEGER NOT NULL,
    isCaptain INTEGER NOT NULL DEFAULT 0,  -- NEW
    FOREIGN KEY (teamId) REFERENCES team(id) ON DELETE CASCADE
);
```

### team Table
```sql
CREATE TABLE team (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    coachName TEXT NOT NULL,
    delegateName TEXT NOT NULL,
    captainId INTEGER  -- NEW
);
```

### match Table
(No changes - already has captainId from previous US)

## Future Enhancements

1. **Team Edit Dialog**: Add button to clear captain from team edit screen
2. **Captain Badge**: Display captain badge in player lists
3. **Statistics**: Track captain history and performance
4. **Permissions**: Allow captain to have special permissions in the app

## Testing Checklist

- [x] Create player as captain (no existing captain)
- [x] Create player as captain (with existing captain) - confirmation dialog
- [x] Edit player to become captain - confirmation dialog
- [x] Edit captain to remove status (no matches) - confirmation dialog
- [x] Edit captain to remove status (with matches) - choice dialog
- [x] Match creation skips captain step when captain in squad
- [x] Match creation shows captain step when captain not in squad
- [x] Team detail shows captain information
- [x] Database migration from v1 to v2
- [x] Unit tests pass for new use cases

## Files Modified

### Domain Layer
- `domain/src/main/kotlin/.../domain/model/Player.kt`
- `domain/src/main/kotlin/.../domain/model/Team.kt`

### Data Layer
- `data/local/src/main/java/.../data/local/entity/PlayerEntity.kt`
- `data/local/src/main/java/.../data/local/entity/TeamEntity.kt`
- `data/local/src/main/java/.../data/local/dao/PlayerDao.kt`
- `data/local/src/main/java/.../data/local/dao/MatchDao.kt`
- `data/local/src/main/java/.../data/local/database/TeamFlowManagerDatabase.kt`
- `data/local/src/main/java/.../data/local/di/DataLocalModule.kt`
- `data/local/src/main/java/.../data/local/datasource/PlayerLocalDataSourceImpl.kt`
- `data/local/src/main/java/.../data/local/datasource/MatchLocalDataSourceImpl.kt`
- `data/core/src/main/kotlin/.../data/core/datasource/PlayerLocalDataSource.kt`
- `data/core/src/main/kotlin/.../data/core/datasource/MatchLocalDataSource.kt`
- `data/core/src/main/kotlin/.../data/core/repository/PlayerRepositoryImpl.kt`
- `data/core/src/main/kotlin/.../data/core/repository/MatchRepositoryImpl.kt`

### Use Case Layer
- `usecase/src/main/kotlin/.../usecase/repository/PlayerRepository.kt`
- `usecase/src/main/kotlin/.../usecase/repository/MatchRepository.kt`
- `usecase/src/main/kotlin/.../usecase/GetCaptainPlayerUseCase.kt` (NEW)
- `usecase/src/main/kotlin/.../usecase/UpdateScheduledMatchesCaptainUseCase.kt` (NEW)
- `usecase/src/main/kotlin/.../usecase/di/UseCaseModule.kt`

### ViewModel Layer
- `viewmodel/src/main/java/.../viewmodel/PlayerViewModel.kt`
- `viewmodel/src/main/java/.../viewmodel/TeamViewModel.kt`
- `viewmodel/src/main/java/.../viewmodel/MatchCreationWizardViewModel.kt`
- `viewmodel/src/main/java/.../viewmodel/di/ViewModelModule.kt`

### UI Layer
- `app/src/main/java/.../ui/players/PlayersScreen.kt`
- `app/src/main/java/.../ui/players/components/dialog/PlayerDialog.kt`
- `app/src/main/java/.../ui/players/components/dialog/CaptainConfirmationDialog.kt` (NEW)
- `app/src/main/java/.../ui/teamdetail/TeamDetailScreen.kt`
- `app/src/main/res/values/strings.xml`
- `app/src/main/res/values-es/strings.xml`

### Tests
- `usecase/src/test/kotlin/.../usecase/GetCaptainPlayerUseCaseTest.kt` (NEW)
- `usecase/src/test/kotlin/.../usecase/UpdateScheduledMatchesCaptainUseCaseTest.kt` (NEW)

## Conclusion

The captain selection feature has been fully implemented following the requirements in US-6.1.4. The implementation includes:

- Robust database schema with migration support
- Transactional operations for data integrity
- Comprehensive user confirmation flows
- Smart match wizard navigation
- Full test coverage for new use cases
- Bilingual UI strings

The feature integrates seamlessly with existing match creation and player management flows while maintaining backward compatibility with existing data.
