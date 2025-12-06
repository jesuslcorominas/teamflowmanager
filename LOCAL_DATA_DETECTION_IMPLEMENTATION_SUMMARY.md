# Local Data Detection Implementation Summary

## Issue
**Detección de Datos Locales**

As a Developer, I want the system to verify at application startup if there are data in the Room tables (e.g., local TeamEntity without an associated userId).

## Implementation Overview

This implementation adds functionality to detect local data without an associated user ID at application startup. This is particularly useful for identifying data created before user authentication was added to the application.

## Changes Made

### 1. Data Layer

#### TeamDao.kt
Added a new query method to check for teams without a coachId:
```kotlin
@Query("SELECT EXISTS(SELECT 1 FROM team WHERE coachId IS NULL LIMIT 1)")
suspend fun hasLocalTeamWithoutUserId(): Boolean
```

This query uses `EXISTS` for efficient checking without loading the entire entity.

#### TeamDataSource.kt
Added interface method:
```kotlin
suspend fun hasLocalTeamWithoutUserId(): Boolean
```

#### TeamLocalDataSourceImpl.kt
Implemented the method to delegate to the DAO:
```kotlin
override suspend fun hasLocalTeamWithoutUserId(): Boolean =
    teamDao.hasLocalTeamWithoutUserId()
```

#### TeamFirestoreDataSourceImpl.kt
Added default implementation returning `false` (not applicable for remote storage):
```kotlin
override suspend fun hasLocalTeamWithoutUserId(): Boolean = false
```

### 2. Repository Layer

#### TeamRepository.kt
Added interface method with documentation:
```kotlin
/**
 * Check if there is local data (team) without an associated user ID.
 * This is useful for detecting data created before user authentication was added.
 * @return true if there is a team without a coachId, false otherwise
 */
suspend fun hasLocalTeamWithoutUserId(): Boolean
```

#### TeamRepositoryImpl.kt
Implemented the method to delegate to data source:
```kotlin
override suspend fun hasLocalTeamWithoutUserId(): Boolean =
    teamDataSource.hasLocalTeamWithoutUserId()
```

### 3. Use Case Layer

#### HasLocalDataWithoutUserIdUseCase.kt
Created new use case:
```kotlin
interface HasLocalDataWithoutUserIdUseCase {
    suspend operator fun invoke(): Boolean
}

internal class HasLocalDataWithoutUserIdUseCaseImpl(
    private val teamRepository: TeamRepository,
) : HasLocalDataWithoutUserIdUseCase {
    override suspend fun invoke(): Boolean = teamRepository.hasLocalTeamWithoutUserId()
}
```

#### UseCaseModule.kt
Registered the new use case in the DI container:
```kotlin
singleOf(::HasLocalDataWithoutUserIdUseCaseImpl) bind HasLocalDataWithoutUserIdUseCase::class
```

### 4. Application Layer

#### TeamFlowManagerApplication.kt
Integrated the check at app startup:
```kotlin
// Check for local data without user ID
val hasLocalDataWithoutUserIdUseCase: HasLocalDataWithoutUserIdUseCase by inject()
applicationScope.launch(Dispatchers.IO) {
    try {
        val hasLocalData = hasLocalDataWithoutUserIdUseCase()
        if (hasLocalData) {
            Log.i(TAG, "Local data without user ID detected. Team exists without coachId.")
        } else {
            Log.d(TAG, "No local data without user ID found.")
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error checking for local data without user ID", e)
    }
}
```

### 5. Tests

#### HasLocalDataWithoutUserIdUseCaseTest.kt
Created unit tests for the use case:
- Test when local team exists without coachId
- Test when no local team exists without coachId
- Test when team has coachId

#### TeamRepositoryImplTest.kt
Added tests for the new repository method:
- Test when team without coachId exists (returns true)
- Test when no team without coachId exists (returns false)

## Technical Details

### Design Decisions
1. **Suspend Function**: The check is implemented as a suspend function to avoid blocking the main thread
2. **IO Dispatcher**: The check runs in the IO dispatcher via coroutine scope to ensure it doesn't impact app startup performance
3. **Logging**: Results are logged for debugging purposes (INFO level for detection, DEBUG for no detection, ERROR for exceptions)
4. **SQL Efficiency**: Uses `EXISTS` query for efficient checking without loading the entire entity
5. **Remote Implementation**: Returns `false` for Firestore implementation since remote storage always has userId association

### Architecture
- Follows the clean architecture pattern already established in the project
- Maintains separation of concerns across layers (Data, Repository, Use Case, Application)
- Uses dependency injection (Koin) for all dependencies
- Implements proper error handling

### Testing
- Unit tests at the use case layer mock the repository
- Unit tests at the repository layer mock the data source
- All tests use MockK for mocking and follow existing test patterns
- Tests cover both positive and negative scenarios

## Code Quality

### Code Review
✅ Code review passed with no issues

### Security
✅ No security vulnerabilities detected (CodeQL check passed)

### Formatting
- Follows project's Kotlin code style
- Matches existing file formatting patterns
- Uses proper documentation comments

## Usage

The check runs automatically at application startup. When local data without a userId is detected, it will be logged:

```
I/TeamFlowManagerApp: Local data without user ID detected. Team exists without coachId.
```

When no such data exists:
```
D/TeamFlowManagerApp: No local data without user ID found.
```

## Future Enhancements

This implementation can be extended to:
1. Trigger a migration/sync process when local data is detected
2. Show a UI notification to the user about the detected data
3. Automatically associate the local data with the current authenticated user
4. Check other entities (players, matches) for missing userId associations

## Files Changed
- `app/src/main/java/com/jesuslcorominas/teamflowmanager/TeamFlowManagerApplication.kt`
- `data/core/src/main/kotlin/com/jesuslcorominas/teamflowmanager/data/core/datasource/TeamDataSource.kt`
- `data/core/src/main/kotlin/com/jesuslcorominas/teamflowmanager/data/core/repository/TeamRepositoryImpl.kt`
- `data/local/src/main/java/com/jesuslcorominas/teamflowmanager/data/local/dao/TeamDao.kt`
- `data/local/src/main/java/com/jesuslcorominas/teamflowmanager/data/local/datasource/TeamLocalDataSourceImpl.kt`
- `data/remote/src/main/java/com/jesuslcorominas/teamflowmanager/data/remote/datasource/TeamFirestoreDataSourceImpl.kt`
- `usecase/src/main/kotlin/com/jesuslcorominas/teamflowmanager/usecase/HasLocalDataWithoutUserIdUseCase.kt`
- `usecase/src/main/kotlin/com/jesuslcorominas/teamflowmanager/usecase/di/UseCaseModule.kt`
- `usecase/src/main/kotlin/com/jesuslcorominas/teamflowmanager/usecase/repository/TeamRepository.kt`

## Files Added
- `usecase/src/main/kotlin/com/jesuslcorominas/teamflowmanager/usecase/HasLocalDataWithoutUserIdUseCase.kt`
- `usecase/src/test/kotlin/com/jesuslcorominas/teamflowmanager/usecase/HasLocalDataWithoutUserIdUseCaseTest.kt`
- `data/core/src/test/kotlin/com/jesuslcorominas/teamflowmanager/data/core/repository/TeamRepositoryImplTest.kt` (tests added)

## Conclusion

The implementation successfully fulfills the requirement to detect local data without an associated user ID at application startup. The solution:
- ✅ Is minimal and focused
- ✅ Follows the existing architecture patterns
- ✅ Is well-tested
- ✅ Handles errors gracefully
- ✅ Provides clear logging for debugging
- ✅ Passed all code quality and security checks
