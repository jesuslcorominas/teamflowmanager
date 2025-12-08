# Time Synchronization Implementation Summary

## Overview

This implementation fixes the time synchronization issue where match timing was dependent on individual device clocks, causing desyncs between different devices viewing or controlling the same match.

## Problem Statement

Previously, when a match was started, it used `System.currentTimeMillis()` from the device that initiated it. If this time didn't match other devices' clocks, there would be a visible offset in elapsed time displays, making it confusing for coaches and team managers using different devices.

## Solution Architecture

### 1. Core Components

#### TimeProvider Interface (`domain/utils/TimeProvider.kt`)
```kotlin
interface TimeProvider {
    fun getCurrentTime(): Long
    suspend fun synchronize()
    fun getOffset(): Long
}
```

Provides an abstraction for getting server-synchronized time throughout the application.

#### FirestoreTimeProvider (`data/remote/datasource/FirestoreTimeProvider.kt`)

Implements the TimeProvider interface using Firestore's `serverTimestamp()` functionality:

1. **Synchronization Process:**
   - Writes a document to Firestore with `FieldValue.serverTimestamp()`
   - Reads the document back to get the actual server timestamp
   - Calculates the offset between server time and device time
   - Accounts for network round-trip time for better accuracy

2. **Offset Calculation:**
   ```kotlin
   val roundTripTime = readTime - writeTime
   val estimatedWriteTime = writeTime + (roundTripTime / 2)
   serverOffset = serverTimeMillis - estimatedWriteTime
   ```

3. **Current Time:**
   ```kotlin
   fun getCurrentTime(): Long = System.currentTimeMillis() + serverOffset
   ```

#### SynchronizeTimeUseCase (`usecase/SynchronizeTimeUseCase.kt`)

Provides a use case for explicitly triggering time synchronization:
```kotlin
interface SynchronizeTimeUseCase {
    suspend operator fun invoke()
}
```

### 2. Integration Points

#### TimeTicker (`viewmodel/utils/TimeTicker.kt`)
Updated to use TimeProvider instead of `System.currentTimeMillis()`:
```kotlin
internal class RealTimeTicker(
    private val timeProvider: TimeProvider
) : TimeTicker {
    override val timeFlow: Flow<Long> = flow {
        while (true) {
            val now = timeProvider.getCurrentTime()
            val rounded = (now / 1000) * 1000
            emit(rounded)
            delay(1000)
        }
    }
}
```

#### MatchViewModel
Synchronizes time before starting or resuming a match:
```kotlin
fun beginMatch(matchId: Long) {
    viewModelScope.launch {
        try {
            synchronizeTimeUseCase()
        } catch (e: Exception) {
            // Log but continue
        }
        val currentTime = _currentTime.value
        startMatchTimerUseCase(matchId, currentTime)
        // ...
    }
}
```

#### MatchListViewModel
Synchronizes time before resuming from the match list:
```kotlin
fun resumeMatch(matchId: Long) {
    viewModelScope.launch {
        try {
            synchronizeTimeUseCase()
        } catch (e: Exception) {
            // Log but continue
        }
        resumeMatchUseCase(matchId, timeProvider.getCurrentTime())
    }
}
```

#### MatchCountdownService
Uses TimeProvider for notification timing:
```kotlin
val notification = notificationManager.buildNotification(
    match,
    timeProvider.getCurrentTime(),
)
```

#### TeamFlowManagerApplication
Initializes time synchronization on app startup:
```kotlin
override fun onCreate() {
    super.onCreate()
    startKoin {
        androidContext(this@TeamFlowManagerApplication)
        modules(appModule, teamFlowManagerModule)
    }
    
    val timeProvider: TimeProvider by inject()
    applicationScope.launch(Dispatchers.IO) {
        try {
            timeProvider.synchronize()
        } catch (e: Exception) {
            Log.w("TeamFlowManager", "Failed to synchronize time on startup", e)
        }
    }
    // ...
}
```

### 3. Dependency Injection

#### DataRemoteModule
```kotlin
internal val firebaseModule = module {
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }
    single { FirebaseStorage.getInstance() }
    singleOf(::FirebaseAuthDataSourceImpl) bind AuthDataSource::class
    singleOf(::FirebaseStorageDataSourceImpl) bind ImageStorageDataSource::class
    single<TimeProvider> { FirestoreTimeProvider(get()) }
}
```

#### UseCaseModule
```kotlin
internal val useCaseInternalModule = module {
    // ... other use cases
    singleOf(::SynchronizeTimeUseCaseImpl) bind SynchronizeTimeUseCase::class
}
```

#### ViewModelModule
```kotlin
factory { RealTimeTicker(get()) } bind TimeTicker::class

viewModel {
    MatchViewModel(
        // ... other dependencies
        synchronizeTimeUseCase = get(),
        // ...
    )
}

viewModel {
    MatchListViewModel(
        // ... other dependencies
        synchronizeTimeUseCase = get(),
        timeProvider = get(),
        // ...
    )
}
```

## Testing

### Unit Test
Created `SynchronizeTimeUseCaseTest` to verify the use case correctly delegates to TimeProvider:

```kotlin
@Test
fun `invoke should call synchronize on timeProvider`() = runTest {
    // When
    synchronizeTimeUseCase.invoke()

    // Then
    coVerify { timeProvider.synchronize() }
}
```

## Benefits

1. **Consistency**: All devices now use the same reference time from the server
2. **Accuracy**: Round-trip time compensation provides better accuracy
3. **Reliability**: Graceful fallback if synchronization fails
4. **Maintainability**: Clean separation of concerns with proper interfaces

## Future Enhancements

Possible improvements for future iterations:

1. **Periodic Re-sync**: Automatically re-synchronize every X minutes during long matches
2. **Offset Monitoring**: Track offset changes over time to detect clock drift
3. **Multiple Samples**: Take multiple time samples and use the median offset for better accuracy
4. **Network Quality**: Adjust synchronization frequency based on network stability

## Files Changed

### New Files
- `domain/src/main/kotlin/com/jesuslcorominas/teamflowmanager/domain/utils/TimeProvider.kt`
- `data/remote/src/main/java/com/jesuslcorominas/teamflowmanager/data/remote/datasource/FirestoreTimeProvider.kt`
- `usecase/src/main/kotlin/com/jesuslcorominas/teamflowmanager/usecase/SynchronizeTimeUseCase.kt`
- `usecase/src/test/kotlin/com/jesuslcorominas/teamflowmanager/usecase/SynchronizeTimeUseCaseTest.kt`

### Modified Files
- `viewmodel/src/main/java/com/jesuslcorominas/teamflowmanager/viewmodel/utils/TimeTicker.kt`
- `viewmodel/src/main/java/com/jesuslcorominas/teamflowmanager/viewmodel/MatchViewModel.kt`
- `viewmodel/src/main/java/com/jesuslcorominas/teamflowmanager/viewmodel/MatchListViewModel.kt`
- `viewmodel/src/main/java/com/jesuslcorominas/teamflowmanager/viewmodel/di/ViewModelModule.kt`
- `app/src/main/java/com/jesuslcorominas/teamflowmanager/TeamFlowManagerApplication.kt`
- `app/src/main/java/com/jesuslcorominas/teamflowmanager/service/MatchCountdownService.kt`
- `data/remote/src/main/java/com/jesuslcorominas/teamflowmanager/data/remote/di/DataRemoteModule.kt`
- `usecase/src/main/kotlin/com/jesuslcorominas/teamflowmanager/usecase/di/UseCaseModule.kt`

## Security Review

✅ CodeQL security scan passed with no vulnerabilities detected.

## Code Review

✅ All code review feedback has been addressed:
- Made `getCurrentTime()` non-suspend since it only performs simple calculation
- Fixed round-trip time calculation for better accuracy
- Updated documentation to follow professional standards
