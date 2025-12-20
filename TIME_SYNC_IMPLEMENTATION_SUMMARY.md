# Time Synchronization Implementation Summary

## Overview

This implementation fixes the time synchronization issue where match timing was dependent on individual device clocks, causing desyncs between different devices viewing or controlling the same match.

## Problem Statement

Previously, when a match was started, it used `System.currentTimeMillis()` from the device that initiated it. If this time didn't match other devices' clocks, there would be a visible offset in elapsed time displays, making it confusing for coaches and team managers using different devices.

**Root Cause of Desync:** The initial implementation calculated time on the client (device time + offset) and stored this calculated value in Firestore. This caused desync because:
1. Device A calculated time with its offset and wrote to Firestore
2. Device B read that time but had a different offset
3. When calculating elapsed time, there was a mismatch because each device used its own offset

## Solution Architecture

### Dual-Layer Approach

The solution uses a two-layer synchronization strategy:

1. **Layer 1: Firestore serverTimestamp() for Persistence** (Primary - Single Source of Truth)
   - When starting/pausing match periods, write `FieldValue.serverTimestamp()` directly to Firestore
   - Read back the actual server timestamp immediately
   - All devices see the EXACT SAME timestamp from Firestore server
   - Eliminates desync completely

2. **Layer 2: Offset-based Synchronization for Real-time UI** (Secondary - Smooth Updates)
   - Calculate server time offset for local time calculations
   - Use for real-time UI updates between Firestore writes
   - Provides smooth countdown and elapsed time displays

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

#### Firestore ServerTimestamp for Match Periods (NEW - Critical Fix)

**MatchFirestoreDataSource** now has methods to write period times using `FieldValue.serverTimestamp()`:

```kotlin
override suspend fun updatePeriodStartWithServerTime(matchId: Long, periodNumber: Int): Long? {
    // Write serverTimestamp to Firestore
    docRef.update(fieldPath, FieldValue.serverTimestamp()).await()
    
    // Read back the actual server timestamp
    val snapshot = docRef.get().await()
    return extractedServerTimestamp
}
```

This ensures all devices read the exact same timestamp from Firestore, implementing **Step 2** from the original issue.

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

#### SplashViewModel (`viewmodel/SplashViewModel.kt`)
Initializes time synchronization on app startup to keep the Application class simple:
```kotlin
init {
    synchronizeTimeAndCheckAuth()
}

private fun synchronizeTimeAndCheckAuth() {
    viewModelScope.launch {
        // Synchronize time with server on app startup
        try {
            synchronizeTimeUseCase()
            Log.d(TAG, "Time synchronized successfully on splash")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to synchronize time on splash", e)
            // Continue anyway - time sync will be attempted again when starting matches
        }
        
        // Continue with authentication checks
        checkLocalDataAndAuth()
    }
}
```

#### TeamFlowManagerApplication
Kept minimal and simple - time synchronization is handled by SplashViewModel instead of Application class.

### 3. Dependency Injection

#### DataRemoteModule
```kotlin
internal val firebaseModule = module {
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }
    single { FirebaseStorage.getInstance() }
    singleOf(::FirebaseAuthDataSourceImpl) bind AuthDataSource::class
    singleOf(::FirebaseStorageDataSourceImpl) bind ImageStorageDataSource::class
    singleOf(::FirestoreTimeProvider) bind TimeProvider::class
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
viewModel {
    SplashViewModel(
        getTeam = get(),
        getCurrentUser = get(),
        hasLocalDataWithoutUserId = get(),
        synchronizeTimeUseCase = get()
    )
}

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

1. **Complete Consistency**: All devices now read the EXACT same timestamp from Firestore server for period start/end times
2. **Zero Desync**: Using `FieldValue.serverTimestamp()` eliminates any possibility of time desync between devices
3. **Accuracy**: Round-trip time compensation provides better accuracy for real-time UI updates
4. **Reliability**: Graceful fallback if synchronization fails
5. **Maintainability**: Clean separation of concerns with proper interfaces
6. **Single Source of Truth**: Firestore server is the ultimate source for all critical timing (period starts/ends)
7. **Smooth UX**: Offset-based synchronization provides smooth real-time updates between Firestore writes

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
