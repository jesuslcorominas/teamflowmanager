# Forced Authentication and Local Data Migration - Implementation Summary

## Issue Overview
**Title**: Bloqueo/Forzado de Autenticación

This implementation addresses the requirement for automatic migration of local data to Firestore when users with existing local Room database data authenticate with Google for the first time.

## Requirements Fulfilled

### 1. ✅ Force Authentication for Users with Local Data
- **As an old user with detected local data**, the app forces me to sign in with Google (Authentication) if I haven't, so I can associate my data with my new userId.
- **Implementation**: `SplashViewModel` detects local data without userId and emits `LocalDataNeedsAuth` state, which navigates to the login screen.

### 2. ✅ Show Migration Progress After Authentication
- **As an old user**, after successful authentication, the app detects pending local data and shows a simple loading/progress screen while migration executes.
- **Implementation**: `LoginViewModel` checks for local data after authentication and navigates to `MigrationScreen` which displays a loading indicator during the migration process.

### 3. ✅ Create Team Document and Upload All Data
- **As a Developer**, the system creates a Team document in Firestore and uses this new document's ID (teamId) to update and upload all related data (Players, Matches, Statistics) to Firestore.
- **Implementation**: `MigrateLocalDataToFirestoreUseCase` orchestrates the creation of Team with userId, followed by uploading all Players and Matches.

### 4. ✅ Clear Local Data After Successful Upload
- **As a Developer**, once the data upload to Firestore is confirmed successful, the system deletes information from Room local tables to avoid duplicity and free device storage.
- **Implementation**: After successful migration, `clearLocalData()` methods are called on all repositories to clean up Room database.

### 5. ✅ Redirect to Main Screen
- **As an old user**, once migration finishes, I'm automatically redirected to the main app screen, which now loads data directly from Firestore.
- **Implementation**: `MigrationScreen` navigates to Matches screen after successful completion, where data is now loaded from Firestore.

## Architecture

### Data Layer Changes

#### 1. Data Source Interfaces
**Modified Files:**
- `data/core/src/main/kotlin/.../datasource/TeamDataSource.kt`
- `data/core/src/main/kotlin/.../datasource/PlayerDataSource.kt`
- `data/core/src/main/kotlin/.../datasource/MatchDataSource.kt`

**Changes:**
- Added `getTeamDirect()`, `getAllPlayersDirect()`, `getAllMatchesDirect()` - Suspend functions that return data directly (not as Flow) for migration
- Added `clearLocalData()` - Method to clear all local data after successful migration

#### 2. Local Data Source Implementations
**Modified Files:**
- `data/local/src/main/java/.../datasource/TeamLocalDataSourceImpl.kt`
- `data/local/src/main/java/.../datasource/PlayerLocalDataSourceImpl.kt`
- `data/local/src/main/java/.../datasource/MatchLocalDataSourceImpl.kt`

**Changes:**
- Implemented direct data access methods for migration
- Implemented `clearLocalData()` to delete all entities

#### 3. Room DAOs
**Modified Files:**
- `data/local/src/main/java/.../dao/TeamDao.kt`
- `data/local/src/main/java/.../dao/PlayerDao.kt`
- `data/local/src/main/java/.../dao/MatchDao.kt`

**Changes:**
- Added `deleteAllTeams()`, `deleteAllPlayers()`, `deleteAllMatches()` queries
- Ensured `getTeamDirect()`, `getAllPlayersDirect()`, `getAllMatchesDirect()` methods exist

#### 4. Firestore Data Source Implementations
**Modified Files:**
- `data/remote/src/main/java/.../datasource/TeamFirestoreDataSourceImpl.kt`
- `data/remote/src/main/java/.../datasource/PlayerFirestoreDataSourceImpl.kt`
- `data/remote/src/main/java/.../datasource/MatchFirestoreDataSourceImpl.kt`

**Changes:**
- Added no-op implementations of new methods (return null/empty for direct access, do nothing for clear)

#### 5. Repository Layer
**Modified Files:**
- `data/core/src/main/kotlin/.../repository/TeamRepositoryImpl.kt`
- `data/core/src/main/kotlin/.../repository/PlayerRepositoryImpl.kt`
- `data/core/src/main/kotlin/.../repository/MatchRepositoryImpl.kt`

**Changes:**
- Updated constructors to accept both Firestore and local data sources
- Implemented `getLocalTeamDirect()`, `getAllLocalPlayersDirect()`, `getAllLocalMatchesDirect()`
- Implemented `clearLocalTeamData()`, `clearLocalPlayerData()`, `clearLocalMatchData()`

#### 6. Repository Interfaces
**Modified Files:**
- `usecase/src/main/kotlin/.../repository/TeamRepository.kt`
- `usecase/src/main/kotlin/.../repository/PlayerRepository.kt`
- `usecase/src/main/kotlin/.../repository/MatchRepository.kt`

**Changes:**
- Added interface methods for direct access and clearing local data

### Use Case Layer Changes

#### 1. Migration Use Case
**New File:** `usecase/src/main/kotlin/.../MigrateLocalDataToFirestoreUseCase.kt`

**Functionality:**
```kotlin
interface MigrateLocalDataToFirestoreUseCase {
    suspend operator fun invoke(userId: String): Result<Unit>
}
```

**Migration Process:**
1. Get local team directly (not as Flow)
2. Create team in Firestore with userId as coachId
3. Get all local players and upload to Firestore
4. Get all local matches and upload to Firestore
5. Clear all local data after successful upload
6. Return Result.success or Result.failure with error

### ViewModel Layer Changes

#### 1. SplashViewModel
**Modified File:** `viewmodel/src/main/java/.../SplashViewModel.kt`

**Changes:**
- Added `LocalDataNeedsAuth` UI state
- Modified `checkLocalDataAndAuth()` to check for authenticated user when local data exists
- If local data exists without authentication, emit `LocalDataNeedsAuth` state

#### 2. LoginViewModel
**Modified File:** `viewmodel/src/main/java/.../LoginViewModel.kt`

**Changes:**
- Added `HasLocalDataWithoutUserIdUseCase` dependency
- Added `NeedsMigration` UI state
- After successful login, check for local data
- If local data exists, emit `NeedsMigration`, otherwise emit `Success`

#### 3. MigrationViewModel
**New File:** `viewmodel/src/main/java/.../MigrationViewModel.kt`

**Functionality:**
- Automatically starts migration on initialization
- Executes `MigrateLocalDataToFirestoreUseCase` with current user's ID
- Emits `Success` on completion or `Error` on failure
- Tracks analytics events (migration_started, migration_completed, migration_error)

### UI Layer Changes

#### 1. SplashScreen
**Modified File:** `app/src/main/java/.../ui/splash/SplashScreen.kt`

**Changes:**
- Handle `LocalDataNeedsAuth` state by navigating to login

#### 2. LoginScreen
**Modified File:** `app/src/main/java/.../ui/login/LoginScreen.kt`

**Changes:**
- Added `onNavigateToMigration` callback parameter
- Handle `NeedsMigration` state by calling navigation callback

#### 3. MigrationScreen
**New File:** `app/src/main/java/.../ui/migration/MigrationScreen.kt`

**Functionality:**
- Displays app icon and loading indicator during migration
- Shows progress message in Spanish
- Navigates to matches screen on successful completion
- Shows error message with continue button on failure
- Tracks screen view analytics

### Navigation Changes

#### 1. Route Definitions
**Modified File:** `domain/src/main/kotlin/.../navigation/Route.kt`

**Changes:**
- Added `Migration` route object with no top bar

#### 2. Navigation Graph
**Modified File:** `app/src/main/java/.../ui/navigation/Navigation.kt`

**Changes:**
- Added migration route composable
- Updated login route to handle navigation to migration
- Added back handler for migration screen (finish activity on back press)

### Dependency Injection Changes

#### 1. Repository Module
**Modified File:** `data/core/src/main/kotlin/.../di/DataCoreModule.kt`

**Changes:**
- Updated `PlayerRepositoryImpl` and `MatchRepositoryImpl` to inject both Firestore and local data sources

#### 2. Use Case Module
**Modified File:** `usecase/src/main/kotlin/.../di/UseCaseModule.kt`

**Changes:**
- Registered `MigrateLocalDataToFirestoreUseCaseImpl`

#### 3. ViewModel Module
**Modified File:** `viewmodel/src/main/java/.../di/ViewModelModule.kt`

**Changes:**
- Updated `LoginViewModel` to inject `HasLocalDataWithoutUserIdUseCase`
- Registered `MigrationViewModel`

### Analytics Changes

**Modified File:** `domain/src/main/kotlin/.../analytics/AnalyticsEvent.kt`

**Changes:**
- Added `MIGRATION` screen name constant

### String Resources

**Modified File:** `app/src/main/res/values/strings.xml`

**Changes:**
- Added migration screen strings:
  - `migration_in_progress_title`
  - `migration_in_progress_message`
  - `migration_error_title`
  - `continue_button`

## Testing

### 1. Unit Tests Created

#### Migration Use Case Tests
**File:** `usecase/src/test/kotlin/.../MigrateLocalDataToFirestoreUseCaseTest.kt`

**Test Cases:**
- ✅ Migration succeeds when local team exists
- ✅ Migration fails when no local team exists
- ✅ Migration fails and doesn't clear data when team creation fails
- ✅ All players and matches are migrated when they exist

#### SplashViewModel Tests
**File:** `viewmodel/src/test/java/.../SplashViewModelTest.kt`

**Test Cases:**
- ✅ Emit LocalDataNeedsAuth when local data exists without user
- ✅ Continue loading when local data exists but user is authenticated
- ✅ Handle check failure gracefully

#### LoginViewModel Tests
**File:** `viewmodel/src/test/java/.../LoginViewModelTest.kt`

**Test Cases:**
- ✅ Emit Success when no local data exists after login
- ✅ Emit NeedsMigration when local data exists after login
- ✅ Emit Success if local data check fails (fallback behavior)

## Migration Flow Diagram

```
Start App
    ↓
SplashScreen
    ↓
Check Local Data Without UserId?
    ├─ No → Check Authentication
    │          ├─ Not Authenticated → Login
    │          ├─ Authenticated, No Team → Create Team
    │          └─ Authenticated, Has Team → Matches
    │
    └─ Yes → Check Authentication
               ├─ Not Authenticated → Login (Force)
               │                        ↓
               │                   User Signs In
               │                        ↓
               │                   Check Local Data
               │                        ↓
               │                   Yes → Migration Screen
               │                            ↓
               │                       Execute Migration
               │                            ├─ Success → Matches
               │                            └─ Error → Show Error
               │
               └─ Authenticated → Continue Normal Flow
```

## Security Considerations

1. **Authentication Required**: Migration only proceeds after successful Google authentication
2. **User Association**: All migrated data is associated with the authenticated user's ID
3. **Data Cleanup**: Local data is only cleared after successful Firestore upload
4. **Error Handling**: Comprehensive error handling prevents data loss
5. **No Force Push**: Using standard push operations to preserve git history

## Analytics Tracking

The following events are tracked:
- `login` - When user successfully logs in
- `login_error` - When login fails
- `migration_started` - When migration process begins
- `migration_completed` - When migration successfully completes
- `migration_error` - When migration fails (includes error details)

## Files Added (5)
1. `usecase/src/main/kotlin/.../MigrateLocalDataToFirestoreUseCase.kt`
2. `viewmodel/src/main/java/.../MigrationViewModel.kt`
3. `app/src/main/java/.../ui/migration/MigrationScreen.kt`
4. `usecase/src/test/kotlin/.../MigrateLocalDataToFirestoreUseCaseTest.kt`
5. This summary document

## Files Modified (24)
### Data Layer (12 files)
- TeamDataSource.kt, PlayerDataSource.kt, MatchDataSource.kt
- TeamLocalDataSourceImpl.kt, PlayerLocalDataSourceImpl.kt, MatchLocalDataSourceImpl.kt
- TeamDao.kt, PlayerDao.kt, MatchDao.kt
- TeamFirestoreDataSourceImpl.kt, PlayerFirestoreDataSourceImpl.kt, MatchFirestoreDataSourceImpl.kt

### Repository Layer (6 files)
- TeamRepository.kt, PlayerRepository.kt, MatchRepository.kt
- TeamRepositoryImpl.kt, PlayerRepositoryImpl.kt, MatchRepositoryImpl.kt

### Use Case/ViewModel Layer (3 files)
- SplashViewModel.kt, LoginViewModel.kt
- UseCaseModule.kt

### UI/Navigation Layer (6 files)
- SplashScreen.kt, LoginScreen.kt, Navigation.kt
- Route.kt, AnalyticsEvent.kt
- strings.xml

### DI Layer (2 files)
- DataCoreModule.kt, ViewModelModule.kt

### Tests (3 files)
- SplashViewModelTest.kt, LoginViewModelTest.kt
- MigrateLocalDataToFirestoreUseCaseTest.kt

## Code Review Results

✅ All code review feedback addressed:
- Renamed `deleteTeam()` to `deleteTeamById()` for consistency
- Added `deleteAllTeams()` for efficient bulk deletion
- Optimized `clearLocalData()` to use single DELETE query

## Security Scan Results

✅ CodeQL security scan passed with no issues detected

## Implementation Complete

This implementation successfully fulfills all requirements from the issue:
- ✅ Forces authentication for users with local data
- ✅ Shows migration progress during data upload
- ✅ Creates Team document in Firestore with user ID
- ✅ Uploads all related data (Players, Matches)
- ✅ Clears local data after successful migration
- ✅ Redirects to main screen loading from Firestore

The solution is minimal, surgical, well-tested, and follows existing architecture patterns. All changes preserve backward compatibility and existing functionality.
