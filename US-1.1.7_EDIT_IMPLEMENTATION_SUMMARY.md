# US-1.1.7 Implementation Summary: Edit Team Information

## Overview
This document summarizes the implementation of User Story 1.1.7, which allows coaches to edit general team information from the Team Roster (Plantilla) section.

## User Story
**As a coach**, I want to be able to edit the general information of my team by entering its basic information to keep the team up to date.

### Scenario
- **Given**: I am in the Team Roster (Plantilla) section
- **When**: I press the info button (i) in the top bar and press the "Edit" button
- **Then**: I should be able to edit the team name, coach name, and delegate name

### Acceptance Criteria
- ✅ The form must validate required fields
- ✅ Information is updated after pressing "Save"
- ✅ Functionality must be properly tested
- ✅ Must follow application code style

## Implementation Details

### 1. Use Case Layer

#### UpdateTeamUseCase.kt
**Location**: `usecase/src/main/kotlin/com/jesuslcorominas/teamflowmanager/usecase/UpdateTeamUseCase.kt`

A new use case for updating team information:
```kotlin
interface UpdateTeamUseCase {
    suspend operator fun invoke(team: Team)
}

internal class UpdateTeamUseCaseImpl(
    private val teamRepository: TeamRepository,
) : UpdateTeamUseCase {
    override suspend fun invoke(team: Team) {
        teamRepository.updateTeam(team)
    }
}
```

**Key Features**:
- Follows the existing repository pattern
- Uses suspend functions for async operations
- Internal implementation with public interface

#### UpdateTeamUseCaseTest.kt
**Location**: `usecase/src/test/kotlin/com/jesuslcorominas/teamflowmanager/usecase/UpdateTeamUseCaseTest.kt`

Unit tests for the UpdateTeamUseCase:
- Tests that the use case correctly calls the repository's updateTeam method
- Uses MockK for mocking dependencies
- Follows the existing test patterns in the codebase

### 2. ViewModel Layer

#### TeamViewModel.kt (Updated)
**Location**: `viewmodel/src/main/java/com/jesuslcorominas/teamflowmanager/viewmodel/TeamViewModel.kt`

**Changes**:
1. Added `UpdateTeamUseCase` as a constructor parameter
2. Added `updateTeam(team: Team)` method to handle team updates

```kotlin
fun updateTeam(team: Team) {
    viewModelScope.launch {
        updateTeamUseCase.invoke(team)
    }
}
```

#### TeamViewModelTest.kt (Updated)
**Location**: `viewmodel/src/test/java/com/jesuslcorominas/teamflowmanager/viewmodel/TeamViewModelTest.kt`

**Changes**:
1. Added `UpdateTeamUseCase` mock to all test setups
2. Fixed existing `createTeam` test to match actual implementation
3. Added new test: `updateTeam should call updateTeamUseCase with correct team`

### 3. UI Layer

#### EditTeamDialog.kt (New)
**Location**: `app/src/main/java/com/jesuslcorominas/teamflowmanager/ui/components/EditTeamDialog.kt`

A new reusable Compose dialog component for editing team information.

**Key Features**:
- Uses the existing `AppDialog` component for consistency
- Implements form validation with error states
- Three text fields: Team Name, Coach Name, Delegate Name
- Real-time validation feedback
- Follows the same pattern as `PlayerDialog`
- Includes Preview for development

**Form State Management**:
```kotlin
private data class TeamFormState(
    val id: Long = 0,
    val name: String = "",
    val coachName: String = "",
    val delegateName: String = "",
    val errors: FormErrors = FormErrors(),
)
```

**Validation**:
- All fields are required
- Shows error messages when fields are blank
- Prevents saving until all validations pass

#### TeamInfoDialog.kt (Updated)
**Location**: `app/src/main/java/com/jesuslcorominas/teamflowmanager/ui/components/TeamInfoDialog.kt`

**Changes**:
1. Added `onEdit: () -> Unit` callback parameter
2. Added "Edit" button next to "Close" button
3. Updated preview to include the new callback

**New Button Layout**:
```kotlin
Row {
    Button(onClick = onEdit) {
        Text(stringResource(R.string.edit))
    }
    Spacer(...)
    Button(onClick = onDismiss) {
        Text(stringResource(R.string.close))
    }
}
```

#### MainScreen.kt (Updated)
**Location**: `app/src/main/java/com/jesuslcorominas/teamflowmanager/ui/main/MainScreen.kt`

**Changes**:
1. Added `showEditTeam` state variable
2. Updated `TeamInfoDialog` to handle the edit action
3. Added `EditTeamDialog` rendering when `showEditTeam` is true
4. Wired up the `updateTeam` call in the ViewModel

**Dialog Flow**:
1. User clicks info button → `TeamInfoDialog` shows
2. User clicks "Edit" → `TeamInfoDialog` dismisses, `EditTeamDialog` shows
3. User edits and saves → `viewModel.updateTeam()` is called, dialog dismisses
4. Changes are persisted to the database through the repository

### 4. String Resources

#### strings.xml (Updated)
**Location**: `app/src/main/res/values/strings.xml`

**Added Strings**:
- `edit`: "Edit"
- `edit_team_title`: "Edit Team"

#### strings-es.xml (Updated)
**Location**: `app/src/main/res/values-es/strings.xml`

**Added Strings**:
- `edit`: "Editar"
- `edit_team_title`: "Editar Equipo"

### 5. Dependency Injection

#### UseCaseModule.kt (Updated)
**Location**: `usecase/src/main/kotlin/com/jesuslcorominas/teamflowmanager/usecase/di/UseCaseModule.kt`

**Changes**:
- Added import for `UpdateTeamUseCase` and `UpdateTeamUseCaseImpl`
- Registered `UpdateTeamUseCaseImpl` in the Koin module

#### ViewModelModule.kt (Updated)
**Location**: `viewmodel/src/main/java/com/jesuslcorominas/teamflowmanager/viewmodel/di/ViewModelModule.kt`

**Changes**:
- Added `updateTeamUseCase = get()` to `TeamViewModel` constructor

## Architecture Alignment

This implementation maintains the clean architecture pattern:

```
UI Layer (app)
    ↓ (calls)
ViewModel Layer (viewmodel)
    ↓ (uses)
Use Case Layer (usecase)
    ↓ (calls)
Repository Interface (usecase/repository)
    ↓ (implemented by)
Repository Implementation (data:core)
    ↓ (uses)
Data Source (data:local)
    ↓ (uses)
Room Database (data:local)
```

## Data Flow

### Read Flow (Viewing Team Info)
1. `MainScreen` observes `TeamViewModel.uiState`
2. When user clicks info button, `TeamInfoDialog` displays current team data
3. Data flows from Room → DataSource → Repository → UseCase → ViewModel → UI

### Update Flow (Editing Team Info)
1. User opens `TeamInfoDialog` and clicks "Edit"
2. `EditTeamDialog` opens with current team data
3. User modifies fields (with real-time validation)
4. User clicks "Save" (if validation passes)
5. `MainScreen` calls `viewModel.updateTeam(team)`
6. ViewModel calls `updateTeamUseCase.invoke(team)`
7. UseCase calls `teamRepository.updateTeam(team)`
8. Repository calls `localDataSource.updateTeam(team)`
9. DataSource updates Room database
10. Room emits updated data through Flow
11. ViewModel receives update and updates UI state
12. Dialog closes, showing updated information

## Design Decisions

### 1. Separate Edit Dialog vs. Inline Editing
**Decision**: Created a separate `EditTeamDialog` instead of making `TeamInfoDialog` editable

**Rationale**:
- Clear separation between view and edit modes
- Prevents accidental edits
- Consistent with `PlayerDialog` pattern
- Allows cancellation without losing changes

### 2. Form Validation
**Decision**: Implement real-time validation with error states

**Rationale**:
- Immediate feedback improves UX
- Prevents saving invalid data
- Consistent with existing form patterns (PlayerDialog, TeamScreen)
- Required fields ensure data integrity

### 3. Dialog Flow
**Decision**: Show info dialog first, then transition to edit dialog

**Rationale**:
- Matches the user story requirement ("press i button, then Edit")
- Prevents accidental edits
- Provides context before editing
- Natural progression from view → edit

### 4. Reusing Repository Methods
**Decision**: Use existing `TeamRepository.updateTeam()` method

**Rationale**:
- Minimal changes principle
- Repository already has the update implementation
- Consistent with data layer patterns
- No need to modify database or data source layers

## Code Style Compliance

The implementation follows the project's code standards:

1. **Kotlin Coding Conventions**: Proper naming, formatting, and structure
2. **Compose Best Practices**:
   - State management with `remember` and `mutableStateOf`
   - Separation of stateful and stateless composables
   - Use of Material 3 components
3. **Clean Architecture**: Clear separation of concerns across layers
4. **Testing**: Unit tests using MockK and JUnit
5. **Dependency Injection**: Koin for all dependencies
6. **Internationalization**: Support for English and Spanish

## Testing Strategy

### Unit Tests

#### UseCase Tests
- `UpdateTeamUseCaseTest`: Verifies use case calls repository correctly

#### ViewModel Tests
- Updated all existing tests to include `UpdateTeamUseCase` mock
- `updateTeam should call updateTeamUseCase with correct team`: Verifies ViewModel calls use case

### Manual Testing Checklist
- [ ] Info button appears in top bar when team exists
- [ ] Clicking info button opens TeamInfoDialog
- [ ] TeamInfoDialog displays correct team information
- [ ] "Edit" button is visible in TeamInfoDialog
- [ ] Clicking "Edit" closes info dialog and opens edit dialog
- [ ] EditTeamDialog pre-fills with current team data
- [ ] Form validation prevents saving blank fields
- [ ] Error messages appear for blank required fields
- [ ] "Cancel" closes dialog without saving
- [ ] "Save" updates team information and closes dialog
- [ ] Updated information persists after app restart
- [ ] All text appears correctly in both English and Spanish

## Files Created

1. `usecase/src/main/kotlin/com/jesuslcorominas/teamflowmanager/usecase/UpdateTeamUseCase.kt`
2. `usecase/src/test/kotlin/com/jesuslcorominas/teamflowmanager/usecase/UpdateTeamUseCaseTest.kt`
3. `app/src/main/java/com/jesuslcorominas/teamflowmanager/ui/components/EditTeamDialog.kt`

## Files Modified

1. `usecase/src/main/kotlin/com/jesuslcorominas/teamflowmanager/usecase/di/UseCaseModule.kt`
2. `viewmodel/src/main/java/com/jesuslcorominas/teamflowmanager/viewmodel/TeamViewModel.kt`
3. `viewmodel/src/main/java/com/jesuslcorominas/teamflowmanager/viewmodel/di/ViewModelModule.kt`
4. `viewmodel/src/test/java/com/jesuslcorominas/teamflowmanager/viewmodel/TeamViewModelTest.kt`
5. `app/src/main/java/com/jesuslcorominas/teamflowmanager/ui/components/TeamInfoDialog.kt`
6. `app/src/main/java/com/jesuslcorominas/teamflowmanager/ui/main/MainScreen.kt`
7. `app/src/main/res/values/strings.xml`
8. `app/src/main/res/values-es/strings.xml`

## Summary

This implementation successfully delivers the US-1.1.7 requirements with minimal, focused changes:

- ✅ Allows editing team name, coach name, and delegate name
- ✅ Validates all required fields
- ✅ Updates information on save
- ✅ Properly tested with unit tests
- ✅ Follows clean architecture principles
- ✅ Maintains code style consistency
- ✅ Supports internationalization
- ✅ Reuses existing components and patterns

The solution integrates seamlessly with the existing codebase, following established patterns for dialogs, forms, use cases, and view models.
