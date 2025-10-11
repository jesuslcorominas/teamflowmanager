# US-1.2.1 Implementation Summary: General Timer Start/Pause Functionality

## Overview
This implementation adds functionality to start and pause a general timer (cronómetro general) for training sessions, following clean architecture principles with clear separation of layers.

## What Was Implemented

### 1. Domain Layer (`domain` module)
- **Session.kt**: Domain model representing the session timer state
  - `id`: Session identifier (default 1L for single session)
  - `elapsedTimeMillis`: Total accumulated time in milliseconds
  - `isRunning`: Boolean flag indicating if timer is currently running
  - `lastStartTimeMillis`: Timestamp when timer was last started (null when paused)

### 2. Use Case Layer (`usecase` module)

#### Interfaces and Implementations
- **SessionRepository.kt**: Repository interface defining session operations
  - `getSession()`: Flow to observe session state changes
  - `startTimer(currentTimeMillis)`: Start or resume the timer
  - `pauseTimer(currentTimeMillis)`: Pause the timer and update elapsed time

- **GetSessionUseCase.kt**: Use case to observe session state
  - Returns Flow<Session?> for reactive updates

- **StartSessionTimerUseCase.kt**: Use case to start the timer
  - Takes current timestamp as parameter
  - Creates new session if none exists, or resumes existing one

- **PauseSessionTimerUseCase.kt**: Use case to pause the timer
  - Takes current timestamp as parameter
  - Calculates and adds elapsed time since last start

#### Unit Tests
- **GetSessionUseCaseTest.kt**: Tests session retrieval
- **StartSessionTimerUseCaseTest.kt**: Tests timer start functionality
- **PauseSessionTimerUseCaseTest.kt**: Tests timer pause functionality

All tests use MockK and JUnit as required, following existing test patterns.

#### Dependency Injection
- Updated **UseCaseModule.kt** to register all three session use cases

### 3. Data Core Layer (`data:core` module)

#### Data Source Interface
- **SessionLocalDataSource.kt**: Interface for local session storage
  - `getSession()`: Flow<Session?> to observe session
  - `upsertSession(session)`: Insert or update session

#### Repository Implementation
- **SessionRepositoryImpl.kt**: Implements SessionRepository
  - **startTimer**: Creates new session or updates existing one to running state with current timestamp
  - **pauseTimer**: Calculates elapsed time and updates session to paused state
  - Logic handles edge cases (no session, already paused, etc.)

#### Unit Tests
- **SessionRepositoryImplTest.kt**: Comprehensive tests for repository logic
  - Tests new session creation on start
  - Tests resuming existing session
  - Tests pause with time calculation
  - Tests edge cases (pausing non-running timer, no session exists)

#### Dependency Injection
- Updated **DataCoreModule.kt** to register SessionRepositoryImpl

### 4. Data Local Layer (`data:local` module)

#### Room Database Components
- **SessionEntity.kt**: Room entity for session table
  - Fields mirror domain model
  - Extension functions for domain/entity conversion

- **SessionDao.kt**: Room DAO for session operations
  - `getSession()`: Flow query for reactive updates
  - `upsertSession()`: Insert with REPLACE strategy for upsert behavior

- **SessionLocalDataSourceImpl.kt**: Implementation of SessionLocalDataSource
  - Maps between entity and domain models
  - Delegates to SessionDao

#### Database Updates
- **TeamFlowManagerDatabase.kt**: Updated to include SessionEntity
  - Database version incremented from 3 to 4
  - Added SessionDao accessor

#### Dependency Injection
- Updated **DataLocalModule.kt** to:
  - Register SessionDao from database
  - Register SessionLocalDataSourceImpl

## Architecture Flow

```
UI Layer (to be implemented)
    ↓
ViewModel (to be implemented)
    ↓
Use Cases (GetSessionUseCase, StartSessionTimerUseCase, PauseSessionTimerUseCase)
    ↓
Repository Interface (SessionRepository)
    ↓
Repository Implementation (SessionRepositoryImpl)
    ↓
DataSource Interface (SessionLocalDataSource)
    ↓
DataSource Implementation (SessionLocalDataSourceImpl)
    ↓
Room DAO (SessionDao)
    ↓
Room Database (session table)
```

## Timer Logic

### Starting the Timer
1. Get current session from database
2. If no session exists: Create new session with elapsed time 0, set running=true, record start time
3. If session exists: Update session to running=true, record new start time, keep accumulated time
4. Save to database

### Pausing the Timer
1. Get current session from database
2. If session exists and is running:
   - Calculate time elapsed since last start: `currentTime - lastStartTime`
   - Add to accumulated time: `elapsedTime + elapsed`
   - Set running=false, clear lastStartTime
   - Save to database
3. If not running or no session: Do nothing

### Observing the Timer
- UI can observe session Flow from GetSessionUseCase
- When running, UI should:
  - Get lastStartTimeMillis from session
  - Calculate display time: `elapsedTimeMillis + (currentTime - lastStartTimeMillis)`
  - Update display every second
- When paused: Display elapsedTimeMillis directly

## Testing Strategy

All use cases and repository have comprehensive unit tests using:
- **MockK**: For mocking dependencies
- **JUnit**: Test framework
- **kotlinx-coroutines-test**: For testing coroutines with runTest

Tests follow the Given-When-Then pattern and verify:
- Happy path scenarios
- Edge cases
- Proper delegation to dependencies
- Correct state transitions

## Next Steps (Not Implemented)

To complete the user story, the following is still needed:
1. **ViewModel**: Create SessionViewModel to manage UI state
2. **UI Components**: Create timer display and start/pause buttons
3. **UI Integration**: Wire up ViewModel to UI in the session screen
4. **Manual Testing**: Verify timer works correctly in the app

## Files Changed

### Created (14 files):
- domain/src/main/kotlin/.../domain/model/Session.kt
- usecase/src/main/kotlin/.../usecase/GetSessionUseCase.kt
- usecase/src/main/kotlin/.../usecase/StartSessionTimerUseCase.kt
- usecase/src/main/kotlin/.../usecase/PauseSessionTimerUseCase.kt
- usecase/src/main/kotlin/.../usecase/repository/SessionRepository.kt
- usecase/src/test/kotlin/.../usecase/GetSessionUseCaseTest.kt
- usecase/src/test/kotlin/.../usecase/StartSessionTimerUseCaseTest.kt
- usecase/src/test/kotlin/.../usecase/PauseSessionTimerUseCaseTest.kt
- data/core/src/main/kotlin/.../data/core/datasource/SessionLocalDataSource.kt
- data/core/src/main/kotlin/.../data/core/repository/SessionRepositoryImpl.kt
- data/core/src/test/kotlin/.../data/core/repository/SessionRepositoryImplTest.kt
- data/local/src/main/java/.../data/local/dao/SessionDao.kt
- data/local/src/main/java/.../data/local/datasource/SessionLocalDataSourceImpl.kt
- data/local/src/main/java/.../data/local/entity/SessionEntity.kt

### Modified (4 files):
- usecase/src/main/kotlin/.../usecase/di/UseCaseModule.kt
- data/core/src/main/kotlin/.../data/core/di/DataCoreModule.kt
- data/local/src/main/java/.../data/local/database/TeamFlowManagerDatabase.kt
- data/local/src/main/java/.../data/local/di/DataLocalModule.kt

## Compliance with Requirements

✅ Modular architecture with clear layer separation
✅ Unit tests with MockK and JUnit
✅ Room database for persistence (KMM-ready structure)
✅ Clean separation: Domain → UseCase → Repository → DataSource → Database
✅ Dependency injection with Koin
✅ Follows existing code patterns and conventions
