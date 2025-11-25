# US-6.1.2: Match Creation Wizard - Implementation Summary

## Overview

This document summarizes the implementation of the multi-step match creation wizard (US-6.1.2) for the TeamFlow Manager application. The wizard improves the user experience for creating matches by breaking the process into clear, sequential steps with proper validation and smart features like auto-captain selection.

## Implementation Details

### 1. Domain Model Updates

**File**: `domain/src/main/kotlin/com/jesuslcorominas/teamflowmanager/domain/model/Match.kt`

Added two new fields to the `Match` model:
- `squadCallUpIds: List<Long>` - Players selected for the match squad (convocatoria)
- `captainId: Long?` - The captain for this match

### 2. Use Cases

Created three new use cases for captain management:

#### GetPreviousCaptainsUseCase
**File**: `usecase/src/main/kotlin/com/jesuslcorominas/teamflowmanager/usecase/GetPreviousCaptainsUseCase.kt`

Retrieves the captain IDs from the last N matches (default 2) to support auto-captain detection.

#### GetDefaultCaptainUseCase
**File**: `usecase/src/main/kotlin/com/jesuslcorominas/teamflowmanager/usecase/GetDefaultCaptainUseCase.kt`

Retrieves the default captain ID from preferences.

#### SaveDefaultCaptainUseCase
**File**: `usecase/src/main/kotlin/com/jesuslcorominas/teamflowmanager/usecase/SaveDefaultCaptainUseCase.kt`

Saves a player ID as the default captain in preferences.

### 3. Data Layer Updates

#### Database Migration
**File**: `data/local/src/main/java/com/jesuslcorominas/teamflowmanager/data/local/di/DataLocalModule.kt`

Added migration from database version 1 to 2:
```kotlin
private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `match` ADD COLUMN `squadCallUpIds` TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE `match` ADD COLUMN `captainId` INTEGER")
    }
}
```

#### Room Entity
**File**: `data/local/src/main/java/com/jesuslcorominas/teamflowmanager/data/local/entity/MatchEntity.kt`

Updated to include the new fields with proper serialization (comma-separated IDs for squad call-up).

#### Preferences
**Files**:
- `data/core/src/main/kotlin/com/jesuslcorominas/teamflowmanager/data/core/datasource/PreferencesLocalDataSource.kt`
- `data/local/src/main/java/com/jesuslcorominas/teamflowmanager/data/local/datasource/PreferencesLocalDataSourceImpl.kt`
- `data/core/src/main/kotlin/com/jesuslcorominas/teamflowmanager/data/core/repository/PreferencesRepositoryImpl.kt`

Added methods to get and set the default captain ID using SharedPreferences.

### 4. ViewModel

**File**: `viewmodel/src/main/java/com/jesuslcorominas/teamflowmanager/viewmodel/MatchCreationWizardViewModel.kt`

Created `MatchCreationWizardViewModel` with the following features:
- Step management (4 steps: GENERAL_DATA, SQUAD_CALLUP, CAPTAIN, STARTING_LINEUP)
- Data storage for each step
- Validation logic
- Goalkeeper detection in squad and lineup
- Auto-captain detection and default captain management
- Match building from collected data

### 5. UI Components

Created a modular wizard with separate composables for each step:

#### Main Wizard Screen
**File**: `app/src/main/java/com/jesuslcorominas/teamflowmanager/ui/matches/wizard/MatchCreationWizardScreen.kt`

Orchestrates the wizard flow, handling navigation between steps and showing the default captain dialog.

#### Step 1: General Data
**File**: `app/src/main/java/com/jesuslcorominas/teamflowmanager/ui/matches/wizard/GeneralDataStep.kt`

Features:
- Text fields for opponent and location
- Validation for required fields
- "Cancel" and "Next" buttons

#### Step 2: Squad Call-Up
**File**: `app/src/main/java/com/jesuslcorominas/teamflowmanager/ui/matches/wizard/SquadCallUpStep.kt`

Features:
- Checkbox list of all players
- Counter showing selected players
- Minimum 5 players validation
- Warning dialog if no goalkeeper selected (allows to proceed)
- "Previous" and "Next" buttons

#### Step 3: Captain Selection
**File**: `app/src/main/java/com/jesuslcorominas/teamflowmanager/ui/matches/wizard/CaptainSelectionStep.kt`

Features:
- Radio button list of squad players
- Required selection validation
- Auto-detection if player was captain in last 2 matches
- Dialog asking to make player default captain
- Auto-selection of default captain when entering step
- "Previous" and "Next" buttons

#### Step 4: Starting Lineup
**File**: `app/src/main/java/com/jesuslcorominas/teamflowmanager/ui/matches/wizard/StartingLineupStep.kt`

Features:
- Checkbox list of squad players
- Counter showing selected players (must be exactly 5)
- Alert when trying to select more than 5 players
- Warning dialog if no goalkeeper selected in lineup (when goalkeepers exist in squad)
- Cannot proceed with less than 5 players
- "Previous" and "Create" buttons

### 6. Navigation Updates

**Files**:
- `app/src/main/java/com/jesuslcorominas/teamflowmanager/ui/navigation/Route.kt`
- `app/src/main/java/com/jesuslcorominas/teamflowmanager/ui/navigation/Navigation.kt`

Changes:
- Added new `CreateMatch` route
- Updated MatchListScreen to navigate to wizard for new matches
- Kept MatchDetailScreen only for editing existing matches
- Updated MatchDetailScreen to preserve new fields when editing

### 7. String Resources

Added comprehensive Spanish and English strings for:
- Wizard step titles
- Form labels and placeholders
- Validation messages
- Alert dialogs
- Button labels

**Files**:
- `app/src/main/res/values/strings.xml`
- `app/src/main/res/values-es/strings.xml`

### 8. Unit Tests

Created comprehensive unit tests following TDD principles:

#### Use Case Tests
- `GetPreviousCaptainsUseCaseTest.kt` - Tests captain history retrieval
- `GetDefaultCaptainUseCaseTest.kt` - Tests default captain retrieval
- `SaveDefaultCaptainUseCaseTest.kt` - Tests default captain saving

#### ViewModel Tests
- `MatchCreationWizardViewModelTest.kt` - Tests all wizard functionality including:
  - Step navigation
  - Data management
  - Goalkeeper detection
  - Auto-captain logic
  - Match building

## Key Features Implemented

### 1. Multi-Step Wizard Flow
✅ 4 sequential steps with clear navigation
✅ "Previous" and "Next" buttons (except first/last)
✅ "Cancel" on first step, "Create" on last step

### 2. Squad Call-Up (Convocatoria)
✅ Minimum 5 players required
✅ Warning when no goalkeeper selected (allows to proceed)
✅ Players not in call-up won't appear in match details

### 3. Captain Selection
✅ Radio button selection from squad players
✅ Auto-detection: same captain in last 2 matches triggers default captain prompt
✅ Default captain auto-selected in future matches
✅ Skip step when default captain is set

### 4. Starting Lineup (Alineación)
✅ Exactly 5 players required
✅ Alert prevents selecting more than 5 players
✅ Warning when no goalkeeper in lineup (if goalkeepers in squad)
✅ Different from substitutes (calculated as squad - lineup)

### 5. Validation & UX
✅ All required field validation
✅ User-friendly error messages
✅ Confirmation dialogs for important actions
✅ Cannot proceed with invalid data
✅ Spanish and English localization

### 6. Data Persistence
✅ Database migration for new fields
✅ SharedPreferences for default captain
✅ Proper serialization of lists

### 7. Edit Mode
✅ Existing match editing preserved
✅ New fields maintained when editing
✅ Simplified edit flow (no wizard for edits)

## Testing

All use cases and view model logic have comprehensive unit tests using:
- **JUnit** - Test framework
- **Mockk** - Mocking framework
- **Turbine** - Flow testing
- **Kotlin Coroutines Test** - Async testing

Test coverage includes:
- Happy path scenarios
- Edge cases
- Validation logic
- State management
- Auto-captain detection logic

## Architecture Compliance

The implementation follows the project's clean architecture:

```
app (UI Layer)
  └─> viewmodel (Presentation Layer)
      └─> usecase (Business Logic Layer)
          └─> domain (Domain Models)
              └─> data:core (Repository Interfaces)
                  └─> data:local (Room Implementation)
```

All layers are properly separated with clear responsibilities and dependencies flowing in the correct direction.

## Future Enhancements

Potential improvements that could be added:
1. Date and time pickers in Step 1 (currently uses current timestamp)
2. Player photos in selection lists
3. Position-based filtering in lineup selection
4. Formation selector (4-1, 3-2, 2-3, etc.)
5. Save draft functionality to resume wizard later
6. Validation for balanced team formation

## Notes

- The wizard provides a much better UX than the original single-screen form
- All validations are in place as specified in the requirements
- The auto-captain feature is smart and user-friendly
- The code is modular, testable, and follows Kotlin best practices
- Database migration ensures existing data is preserved
