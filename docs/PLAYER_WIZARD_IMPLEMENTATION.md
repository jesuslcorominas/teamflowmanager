# Player Creation/Edit Wizard - Implementation Summary

## Overview
This implementation transforms the player creation and editing experience from a single dialog to a multi-step wizard interface, similar to the match creation wizard. The new wizard provides better UX with more screen space, progressive disclosure, and image management capabilities.

## Key Features

### 1. Two-Step Wizard Flow

#### Step 1: Player Data
- First Name (required)
- Last Name (required)
- Jersey Number (required, numeric only)
- Team Captain checkbox
- Player image selector (camera or gallery)
  - Default generic player icon when no image
  - Circular avatar preview
  - Camera permission handling
  - Gallery integration

#### Step 2: Player Positions
- Multi-select position checkboxes
- All available positions displayed
- Localized position names (Spanish/English)
- Navigation buttons (Previous/Save)

### 2. Image Management

**Camera Support:**
- Requests CAMERA permission when needed
- Creates temporary file using FileProvider
- Saves captured image URI to player record

**Gallery Support:**
- Uses standard Android image picker
- No permissions required
- Supports all image formats

**Image Loading:**
- Coil library for efficient async loading
- Handles URIs from both camera and gallery
- Fallback to generic icon when no image

### 3. Captain Management

The implementation reuses existing captain logic from `PlayerViewModel`:

**Scenarios Handled:**
1. **New Captain Selected:** Warns if replacing existing captain, asks about scheduled matches
2. **Captain Removed:** Confirms removal, asks about keeping in scheduled matches  
3. **No Changes:** Saves directly without confirmation
4. **Single Captain Constraint:** Enforces only one captain per team

### 4. Navigation

**Routes:**
- Create: `/player_wizard` (no parameters)
- Edit: `/player_wizard?playerId={id}` (with player ID)

**Entry Points:**
- FAB button in PlayersScreen (create new)
- Player list item click (edit existing)

**Exit:**
- Back navigation returns to players list
- Save completes wizard and returns

## Technical Implementation

### Architecture Layers

#### Domain Layer
**Changes:**
- Added `imageUri: String?` to `Player` data class

#### Use Case Layer
**New:**
- `GetPlayerByIdUseCase` - Fetches single player by ID

**Updated:**
- Use case DI module with new use case binding

#### Repository Layer
**Updated:**
- `PlayerRepository` interface with `getPlayerById`
- `PlayerRepositoryImpl` implementation
- `PlayerLocalDataSource` interface
- `PlayerLocalDataSourceImpl` implementation

#### Data Layer
**Updated:**
- `PlayerEntity` with `imageUri` field
- `PlayerDao` already had `getPlayerById` query
- Database version bumped from 2 to 3
- Migration added: `ALTER TABLE players ADD COLUMN imageUri TEXT DEFAULT NULL`

#### ViewModel Layer
**New:**
- `PlayerWizardViewModel` - Manages wizard state and flow
  - Two steps: PLAYER_DATA, POSITIONS
  - Player data state management
  - Captain confirmation logic (reused)
  - Save/Update player logic

**States:**
- `PlayerWizardUiState`: Loading, Ready, Error
- `PlayerWizardStep`: PLAYER_DATA, POSITIONS
- `CaptainConfirmationState`: Reused from existing implementation

#### UI Layer
**New Components:**
- `PlayerWizardScreen` - Main wizard coordinator
- `PlayerDataStep` - First step composable
- `PlayerPositionsStep` - Second step composable

**Updated:**
- `PlayersScreen` - Navigation callbacks instead of dialog
- `Navigation.kt` - Added wizard route
- `Route.kt` - PlayerWizard route definition

**Removed:**
- `PlayerDialog.kt` - Replaced by wizard

### Dependencies Added

```kotlin
// Coil for image loading
implementation("io.coil-kt:coil-compose:2.5.0")
```

### Permissions

**Manifest:**
```xml
<!-- Camera permission requested at runtime -->
<uses-permission android:name="android.permission.CAMERA" />
```

**FileProvider Configuration:**
```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

### String Resources

**English:**
- `player_data_step_title`: Player Data
- `player_positions_step_title`: Player Positions
- `player_positions_step_subtitle`: Select the positions this player can play
- `player_image`: Player image
- `tap_to_add_photo`: Tap to add photo
- `select_image_source`: Select image source
- `camera`: Camera
- `gallery`: Gallery

**Spanish:**
- `player_data_step_title`: Datos del Jugador
- `player_positions_step_title`: Posiciones del Jugador
- `player_positions_step_subtitle`: Selecciona las posiciones en las que puede jugar este jugador
- `player_image`: Imagen del jugador
- `tap_to_add_photo`: Toca para añadir foto
- `select_image_source`: Selecciona origen de imagen
- `camera`: Cámara
- `gallery`: Galería

## User Flow

### Creating a New Player
1. User taps FAB button in Players screen
2. Navigation to PlayerWizardScreen (no playerId)
3. Step 1: Enter player data, optionally add photo
4. Tap "Next" to proceed (validation required)
5. Step 2: Select player positions
6. Tap "Save" to create player
7. Captain confirmation dialogs if applicable
8. Return to Players screen with new player visible

### Editing an Existing Player
1. User taps player in list
2. Navigation to PlayerWizardScreen with playerId
3. Wizard loads existing player data
4. Step 1: Edit player data, change photo if desired
5. Tap "Next" to proceed
6. Step 2: Edit positions
7. Tap "Save" to update player
8. Captain confirmation dialogs if applicable
9. Return to Players screen with updated player

## Database Migration

**Version 2 → 3:**
```sql
ALTER TABLE players ADD COLUMN imageUri TEXT DEFAULT NULL
```

This migration is applied automatically on app upgrade. Existing players will have `NULL` for imageUri, which is handled gracefully (shows generic icon).

## Code Reuse

The implementation maximizes code reuse:

1. **Captain Logic**: Reused from `PlayerViewModel`
2. **Captain Dialogs**: Reused `CaptainConfirmationDialog` component
3. **Position Handling**: Reused `Position.getAllPositions()` and localization
4. **Form Components**: Reused `AppTextField`, `AppAlertDialog`
5. **Navigation Pattern**: Followed same pattern as `MatchCreationWizardScreen`
6. **DI Pattern**: Followed existing Koin configuration patterns

## Testing Notes

As per the issue requirements, tests were not prioritized and may be broken. The implementation focuses on functionality and can be manually tested using:

1. Create new player with/without image
2. Edit existing player
3. Change player image
4. Change captain status (all scenarios)
5. Select multiple positions
6. Validation error handling
7. Back navigation at each step
8. Database migration from version 2

## Future Enhancements

Potential improvements not included in this implementation:

1. Image cropping/editing before save
2. Compress images to reduce storage
3. Multiple image sizes (thumbnail, full)
4. Image cache management
5. Offline image sync
6. Player photo gallery view
7. Share player profile with image
8. Backup/restore player images

## Comparison: Dialog vs Wizard

### Old Dialog Approach
- ❌ Single cramped screen
- ❌ All fields visible at once
- ❌ Limited space for image handling
- ❌ Less intuitive UX
- ❌ Scrolling required
- ✅ Fewer screens to navigate

### New Wizard Approach
- ✅ Two focused steps
- ✅ Progressive disclosure
- ✅ More space for each field
- ✅ Better image handling
- ✅ Clearer flow
- ✅ Matches app pattern (match wizard)
- ❌ One more screen to navigate

The wizard approach provides significantly better UX, especially for the image selection feature which would be very cramped in a dialog.
