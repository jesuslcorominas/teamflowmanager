# US-1.2.2: Implementation Visual Guide

## 🎯 Feature Overview

**User Story**: As a coach, I want to measure the individual participation time of each player to ensure equitable distribution of minutes.

## 📊 What Was Implemented

### Complete Backend Solution
✅ **Domain Layer** - Data models  
✅ **UseCase Layer** - Business logic  
✅ **Data Layer** - Persistence with Room  
✅ **Tests** - Comprehensive unit tests  
✅ **Documentation** - Technical specs & guides  

---

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────┐
│             Domain Layer                     │
│  ┌─────────────────────────────────────┐   │
│  │       PlayerTime                    │   │
│  │  - playerId: Long                   │   │
│  │  - elapsedTimeMillis: Long          │   │
│  │  - isRunning: Boolean               │   │
│  │  - lastStartTimeMillis: Long?       │   │
│  └─────────────────────────────────────┘   │
└─────────────────────────────────────────────┘
                    ▲
                    │
┌─────────────────────────────────────────────┐
│           UseCase Layer                      │
│  ┌─────────────────────────────────────┐   │
│  │  4 Use Cases:                       │   │
│  │  • StartPlayerTimerUseCase          │   │
│  │  • PausePlayerTimerUseCase          │   │
│  │  • GetPlayerTimeUseCase             │   │
│  │  • GetAllPlayerTimesUseCase         │   │
│  └─────────────────────────────────────┘   │
│  ┌─────────────────────────────────────┐   │
│  │  PlayerTimeRepository (interface)   │   │
│  └─────────────────────────────────────┘   │
└─────────────────────────────────────────────┘
                    ▲
                    │
┌─────────────────────────────────────────────┐
│          Data Core Layer                     │
│  ┌─────────────────────────────────────┐   │
│  │  PlayerTimeRepositoryImpl           │   │
│  │  (Time calculation logic)           │   │
│  └─────────────────────────────────────┘   │
│  ┌─────────────────────────────────────┐   │
│  │  PlayerTimeLocalDataSource (i)      │   │
│  └─────────────────────────────────────┘   │
└─────────────────────────────────────────────┘
                    ▲
                    │
┌─────────────────────────────────────────────┐
│          Data Local Layer                    │
│  ┌─────────────────────────────────────┐   │
│  │  PlayerTimeLocalDataSourceImpl      │   │
│  └─────────────────────────────────────┘   │
│  ┌─────────────────────────────────────┐   │
│  │  PlayerTimeDao (Room)               │   │
│  └─────────────────────────────────────┘   │
│  ┌─────────────────────────────────────┐   │
│  │  PlayerTimeEntity                   │   │
│  │  Table: player_time                 │   │
│  └─────────────────────────────────────┘   │
└─────────────────────────────────────────────┘
```

---

## 🔄 Player Timer Lifecycle

### Scenario: Multiple Start/Pause Cycles

```
Time     State              Action                  Result
─────────────────────────────────────────────────────────────
t = 0    No Record          -                       No data
         
t = 100  -                  START player 1          
         ↓                                           
         Running            playerId: 1             
                            elapsed: 0ms            
                            isRunning: true         
                            lastStart: 100          
         
t = 300  -                  PAUSE player 1          
         ↓                                           
         Paused             playerId: 1             
                            elapsed: 200ms          (300-100)
                            isRunning: false        
                            lastStart: null         
         
t = 500  -                  START player 1          
         ↓                                           
         Running (2nd)      playerId: 1             
                            elapsed: 200ms          (preserved)
                            isRunning: true         
                            lastStart: 500          
         
t = 900  -                  PAUSE player 1          
         ↓                                           
         Paused (2nd)       playerId: 1             
                            elapsed: 600ms          (200 + 400)
                            isRunning: false        
                            lastStart: null         
         
Result: Player 1 has accumulated 600ms total playing time
```

---

## 📁 Files Created/Modified

### Created Files (20 total)

#### Domain Layer (1 file)
```
domain/src/main/kotlin/.../domain/model/
└── PlayerTime.kt                          ← Domain model
```

#### UseCase Layer (5 files)
```
usecase/src/main/kotlin/.../usecase/
├── StartPlayerTimerUseCase.kt             ← Start timer use case
├── PausePlayerTimerUseCase.kt             ← Pause timer use case
├── GetPlayerTimeUseCase.kt                ← Get single player time
├── GetAllPlayerTimesUseCase.kt            ← Get all player times
└── repository/
    └── PlayerTimeRepository.kt            ← Repository interface
```

#### Data Core Layer (2 files)
```
data/core/src/main/kotlin/.../data/core/
├── datasource/
│   └── PlayerTimeLocalDataSource.kt       ← Data source interface
└── repository/
    └── PlayerTimeRepositoryImpl.kt        ← Repository implementation
```

#### Data Local Layer (3 files)
```
data/local/src/main/java/.../data/local/
├── dao/
│   └── PlayerTimeDao.kt                   ← Room DAO
├── datasource/
│   └── PlayerTimeLocalDataSourceImpl.kt   ← Data source impl
└── entity/
    └── PlayerTimeEntity.kt                ← Room entity
```

#### Test Files (5 files)
```
usecase/src/test/kotlin/.../usecase/
├── StartPlayerTimerUseCaseTest.kt         ← 1 test
├── PausePlayerTimerUseCaseTest.kt         ← 1 test
├── GetPlayerTimeUseCaseTest.kt            ← 2 tests
└── GetAllPlayerTimesUseCaseTest.kt        ← 2 tests

data/core/src/test/kotlin/.../data/core/repository/
└── PlayerTimeRepositoryImplTest.kt        ← 8 tests (comprehensive)
```

#### Documentation (2 files)
```
project_root/
├── US-1.2.2_IMPLEMENTATION_SUMMARY.md     ← Complete summary
└── US-1.2.2_TECHNICAL_ARCHITECTURE.md     ← Technical details
```

### Modified Files (4 files)
```
usecase/src/main/kotlin/.../usecase/di/
└── UseCaseModule.kt                       ← Added 4 use case bindings

data/core/src/main/kotlin/.../data/core/di/
└── DataCoreModule.kt                      ← Added repository binding

data/local/src/main/java/.../data/local/di/
└── DataLocalModule.kt                     ← Added DAO & data source

data/local/src/main/java/.../data/local/database/
└── TeamFlowManagerDatabase.kt             ← Added PlayerTime entity, v2
```

---

## 🧪 Test Coverage

### Use Case Tests
- ✅ **StartPlayerTimerUseCaseTest** - Verifies timer start
- ✅ **PausePlayerTimerUseCaseTest** - Verifies timer pause
- ✅ **GetPlayerTimeUseCaseTest** - Retrieves player time (with null case)
- ✅ **GetAllPlayerTimesUseCaseTest** - Retrieves all times (with empty case)

### Repository Tests (8 comprehensive test cases)
1. ✅ Get player time from data source
2. ✅ Get all player times from data source
3. ✅ Create new player time on first start
4. ✅ Update existing player time when restarting
5. ✅ Pause timer and accumulate elapsed time correctly
6. ✅ Do nothing when pausing non-running timer
7. ✅ Do nothing when pausing non-existent timer
8. ✅ Multiple start-pause cycles work correctly (integration)

**Total Test Cases**: 14 tests across 5 test classes

---

## 💾 Database Changes

### New Table: `player_time`

```sql
CREATE TABLE player_time (
    playerId           INTEGER PRIMARY KEY NOT NULL,
    elapsedTimeMillis  INTEGER NOT NULL DEFAULT 0,
    isRunning          INTEGER NOT NULL DEFAULT 0,
    lastStartTimeMillis INTEGER,
    FOREIGN KEY (playerId) REFERENCES players(id) ON DELETE CASCADE
);

CREATE INDEX index_player_time_playerId ON player_time(playerId);
```

### Database Migration
- **Previous Version**: 1
- **New Version**: 2
- **Migration Strategy**: `fallbackToDestructiveMigration()` (development mode)

---

## 🔌 Dependency Injection

All components properly configured in Koin:

```kotlin
// Use Cases
module {
    singleOf(::StartPlayerTimerUseCaseImpl) bind StartPlayerTimerUseCase::class
    singleOf(::PausePlayerTimerUseCaseImpl) bind PausePlayerTimerUseCase::class
    singleOf(::GetPlayerTimeUseCaseImpl) bind GetPlayerTimeUseCase::class
    singleOf(::GetAllPlayerTimesUseCaseImpl) bind GetAllPlayerTimesUseCase::class
}

// Repository
module {
    singleOf(::PlayerTimeRepositoryImpl) bind PlayerTimeRepository::class
}

// Data Source & DAO
module {
    single { get<TeamFlowManagerDatabase>().playerTimeDao() }
    singleOf(::PlayerTimeLocalDataSourceImpl) bind PlayerTimeLocalDataSource::class
}
```

---

## 🎨 Example Usage (For Future UI Integration)

```kotlin
// In ViewModel
class PlayerTimeViewModel(
    private val startPlayerTimer: StartPlayerTimerUseCase,
    private val pausePlayerTimer: PausePlayerTimerUseCase,
    private val getPlayerTime: GetPlayerTimeUseCase,
) : ViewModel() {

    // Observe player time
    fun observePlayerTime(playerId: Long): StateFlow<PlayerTime?> =
        getPlayerTime(playerId)
            .stateIn(viewModelScope, SharingStarted.Lazily, null)

    // Start timer
    fun startTimer(playerId: Long) {
        viewModelScope.launch {
            startPlayerTimer(playerId, System.currentTimeMillis())
        }
    }

    // Pause timer
    fun pauseTimer(playerId: Long) {
        viewModelScope.launch {
            pausePlayerTimer(playerId, System.currentTimeMillis())
        }
    }
}
```

```kotlin
// In UI (Compose)
@Composable
fun PlayerItem(
    player: Player,
    playerTime: PlayerTime?,
    onStartTimer: () -> Unit,
    onPauseTimer: () -> Unit
) {
    Row {
        Text(player.firstName)
        Text(formatTime(playerTime?.elapsedTimeMillis ?: 0L))
        
        if (playerTime?.isRunning == true) {
            Button(onClick = onPauseTimer) { Text("Pause") }
        } else {
            Button(onClick = onStartTimer) { Text("Start") }
        }
    }
}
```

---

## ✅ Acceptance Criteria Verification

| Criteria | Status | Implementation |
|----------|--------|----------------|
| Individual time correctly summed for each player | ✅ | Repository accumulates time across cycles |
| Start/pause multiple times per session | ✅ | Tested with 8 scenarios including multi-cycle |
| Time persists across app restarts | ✅ | Room database with proper entity |

---

## 📦 Deliverables Summary

| Category | Count | Details |
|----------|-------|---------|
| Domain Models | 1 | PlayerTime |
| Use Cases | 4 | Start, Pause, Get, GetAll |
| Repository Interfaces | 1 | PlayerTimeRepository |
| Repository Implementations | 1 | PlayerTimeRepositoryImpl |
| Data Source Interfaces | 1 | PlayerTimeLocalDataSource |
| Data Source Implementations | 1 | PlayerTimeLocalDataSourceImpl |
| Room DAOs | 1 | PlayerTimeDao |
| Room Entities | 1 | PlayerTimeEntity |
| Unit Tests | 5 classes | 14 total test cases |
| Documentation | 2 docs | Summary + Architecture |
| **Total Files** | **20** | **15 source + 5 tests** |

---

## 🚀 Next Steps (Not in Scope)

To complete the feature with UI:
1. Create `PlayerTimeViewModel` in viewmodel module
2. Add timer display UI in app module
3. Add start/pause buttons for each player
4. Display accumulated time in real-time
5. Add visual indicators for running timers
6. Integrate with match timer (ensure match is running)

---

## 📝 Notes

- ✅ All code follows clean architecture principles
- ✅ Properly separated by layers (Domain → UseCase → Data)
- ✅ Tests use MockK and JUnit as required
- ✅ Room database configured for KMM compatibility
- ✅ Koin dependency injection properly configured
- ✅ Database version incremented with migration strategy
- ✅ Code follows Kotlin conventions and project style

---

## 📊 Code Statistics

```
Lines of Code:
- Domain Models:        ~10 lines
- Use Cases:           ~80 lines
- Repositories:        ~60 lines
- Data Sources:        ~30 lines
- Entities & DAOs:     ~60 lines
- DI Configuration:    ~15 lines
- Tests:              ~390 lines
- Documentation:      ~650 lines
────────────────────────────────
Total:              ~1,295 lines
```

---

**Implementation Status**: ✅ **COMPLETE**

All backend components for individual player time tracking have been successfully implemented, tested, and documented. The feature is ready for UI integration.
