# US-1.2.2: Player Time Tracking - Technical Architecture

## Component Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                        PRESENTATION LAYER                        │
│                         (Future: app module)                     │
│                                                                   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │              PlayerTimeViewModel (to be created)          │   │
│  │  - Exposes player time state as StateFlow/LiveData       │   │
│  │  - Handles UI events (start/pause button clicks)         │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────┐
│                         USE CASE LAYER                           │
│                         (usecase module)                         │
│                                                                   │
│  ┌──────────────────────┐  ┌──────────────────────┐            │
│  │StartPlayerTimerUseCase│  │PausePlayerTimerUseCase│            │
│  └──────────────────────┘  └──────────────────────┘            │
│                                                                   │
│  ┌──────────────────────┐  ┌──────────────────────────┐        │
│  │GetPlayerTimeUseCase  │  │GetAllPlayerTimesUseCase  │        │
│  └──────────────────────┘  └──────────────────────────┘        │
│                                    │                              │
│                                    ▼                              │
│  ┌────────────────────────────────────────────────────────┐    │
│  │         PlayerTimeRepository (interface)               │    │
│  │  - getPlayerTime(playerId): Flow<PlayerTime?>         │    │
│  │  - getAllPlayerTimes(): Flow<List<PlayerTime>>        │    │
│  │  - startTimer(playerId, currentTimeMillis)            │    │
│  │  - pauseTimer(playerId, currentTimeMillis)            │    │
│  └────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────┐
│                       DATA CORE LAYER                            │
│                       (data:core module)                         │
│                                                                   │
│  ┌────────────────────────────────────────────────────────┐    │
│  │      PlayerTimeRepositoryImpl (implementation)         │    │
│  │                                                         │    │
│  │  Business Logic:                                       │    │
│  │  - startTimer: Create new or update to running        │    │
│  │  - pauseTimer: Calculate elapsed time, set to paused  │    │
│  │  - Ensures time accumulation across cycles            │    │
│  └────────────────────────────────────────────────────────┘    │
│                                    │                              │
│                                    ▼                              │
│  ┌────────────────────────────────────────────────────────┐    │
│  │   PlayerTimeLocalDataSource (interface)                │    │
│  │  - getPlayerTime(playerId): Flow<PlayerTime?>         │    │
│  │  - getAllPlayerTimes(): Flow<List<PlayerTime>>        │    │
│  │  - upsertPlayerTime(playerTime)                       │    │
│  └────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────┐
│                       DATA LOCAL LAYER                           │
│                       (data:local module)                        │
│                                                                   │
│  ┌────────────────────────────────────────────────────────┐    │
│  │  PlayerTimeLocalDataSourceImpl (implementation)        │    │
│  │  - Maps between domain models and Room entities       │    │
│  │  - Delegates to DAO for actual persistence            │    │
│  └────────────────────────────────────────────────────────┘    │
│                                    │                              │
│                                    ▼                              │
│  ┌────────────────────────────────────────────────────────┐    │
│  │              PlayerTimeDao (Room DAO)                  │    │
│  │  @Query("SELECT * FROM player_time WHERE ...")        │    │
│  │  @Upsert suspend fun upsert(playerTime)               │    │
│  └────────────────────────────────────────────────────────┘    │
│                                    │                              │
│                                    ▼                              │
│  ┌────────────────────────────────────────────────────────┐    │
│  │            PlayerTimeEntity (Room Entity)              │    │
│  │  Table: player_time                                    │    │
│  │  - playerId (PK, FK to players)                       │    │
│  │  - elapsedTimeMillis                                   │    │
│  │  - isRunning                                           │    │
│  │  - lastStartTimeMillis                                 │    │
│  └────────────────────────────────────────────────────────┘    │
│                                    │                              │
│                                    ▼                              │
│  ┌────────────────────────────────────────────────────────┐    │
│  │         TeamFlowManagerDatabase (Room DB)              │    │
│  │  Version: 2                                            │    │
│  │  Entities: [Player, Team, Match, PlayerTime]          │    │
│  └────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
```

## Domain Model

```kotlin
┌──────────────────────────────────────┐
│         PlayerTime                   │
├──────────────────────────────────────┤
│ + playerId: Long                     │
│ + elapsedTimeMillis: Long            │
│ + isRunning: Boolean                 │
│ + lastStartTimeMillis: Long?         │
└──────────────────────────────────────┘
```

## State Transitions

```
┌─────────────┐
│   Initial   │
│  (no record)│
└──────┬──────┘
       │ startTimer(playerId, t1)
       ▼
┌─────────────────────────────────────┐
│           Running                   │
│  playerId: 1                        │
│  elapsedTimeMillis: 0               │
│  isRunning: true                    │
│  lastStartTimeMillis: t1            │
└──────┬──────────────────────────────┘
       │ pauseTimer(playerId, t2)
       ▼
┌─────────────────────────────────────┐
│           Paused                    │
│  playerId: 1                        │
│  elapsedTimeMillis: t2 - t1         │
│  isRunning: false                   │
│  lastStartTimeMillis: null          │
└──────┬──────────────────────────────┘
       │ startTimer(playerId, t3)
       ▼
┌─────────────────────────────────────┐
│        Running (again)              │
│  playerId: 1                        │
│  elapsedTimeMillis: t2 - t1         │
│  isRunning: true                    │
│  lastStartTimeMillis: t3            │
└──────┬──────────────────────────────┘
       │ pauseTimer(playerId, t4)
       ▼
┌─────────────────────────────────────┐
│        Paused (again)               │
│  playerId: 1                        │
│  elapsedTimeMillis: (t2-t1)+(t4-t3) │
│  isRunning: false                   │
│  lastStartTimeMillis: null          │
└─────────────────────────────────────┘
```

## Time Calculation Algorithm

### Start Timer
```kotlin
fun startTimer(playerId: Long, currentTimeMillis: Long) {
    val current = getPlayerTime(playerId)
    
    val updated = if (current == null) {
        // First time starting
        PlayerTime(
            playerId = playerId,
            elapsedTimeMillis = 0L,
            isRunning = true,
            lastStartTimeMillis = currentTimeMillis
        )
    } else {
        // Restarting after pause
        current.copy(
            isRunning = true,
            lastStartTimeMillis = currentTimeMillis
        )
    }
    
    save(updated)
}
```

### Pause Timer
```kotlin
fun pauseTimer(playerId: Long, currentTimeMillis: Long) {
    val current = getPlayerTime(playerId)
    
    if (current != null && current.isRunning) {
        val lastStart = current.lastStartTimeMillis ?: currentTimeMillis
        val additionalTime = currentTimeMillis - lastStart
        
        val updated = current.copy(
            elapsedTimeMillis = current.elapsedTimeMillis + additionalTime,
            isRunning = false,
            lastStartTimeMillis = null
        )
        
        save(updated)
    }
}
```

## Database Schema

```sql
CREATE TABLE player_time (
    playerId INTEGER PRIMARY KEY NOT NULL,
    elapsedTimeMillis INTEGER NOT NULL DEFAULT 0,
    isRunning INTEGER NOT NULL DEFAULT 0,  -- Boolean as INTEGER
    lastStartTimeMillis INTEGER,
    FOREIGN KEY (playerId) 
        REFERENCES players(id) 
        ON DELETE CASCADE
);

CREATE INDEX index_player_time_playerId 
    ON player_time(playerId);
```

## Dependency Injection Configuration

```kotlin
// UseCaseModule
module {
    singleOf(::StartPlayerTimerUseCaseImpl) bind StartPlayerTimerUseCase::class
    singleOf(::PausePlayerTimerUseCaseImpl) bind PausePlayerTimerUseCase::class
    singleOf(::GetPlayerTimeUseCaseImpl) bind GetPlayerTimeUseCase::class
    singleOf(::GetAllPlayerTimesUseCaseImpl) bind GetAllPlayerTimesUseCase::class
}

// DataCoreModule
module {
    singleOf(::PlayerTimeRepositoryImpl) bind PlayerTimeRepository::class
}

// DataLocalModule
module {
    single { get<TeamFlowManagerDatabase>().playerTimeDao() }
    singleOf(::PlayerTimeLocalDataSourceImpl) bind PlayerTimeLocalDataSource::class
}
```

## Test Coverage Map

```
Use Cases (Interface Tests)
├── StartPlayerTimerUseCaseTest
│   └── ✓ Verify delegation to repository
├── PausePlayerTimerUseCaseTest
│   └── ✓ Verify delegation to repository
├── GetPlayerTimeUseCaseTest
│   ├── ✓ Return player time when exists
│   └── ✓ Return null when not exists
└── GetAllPlayerTimesUseCaseTest
    ├── ✓ Return all player times
    └── ✓ Return empty list when none exist

Repository (Business Logic Tests)
└── PlayerTimeRepositoryImplTest
    ├── ✓ Get player time from data source
    ├── ✓ Get all player times from data source
    ├── ✓ Create new player time on first start
    ├── ✓ Update existing player time on restart
    ├── ✓ Accumulate time on pause
    ├── ✓ Do nothing when pausing non-running timer
    ├── ✓ Do nothing when pausing non-existent timer
    └── ✓ Multiple start-pause cycles (integration)
```

## Integration with Existing Features

### Relationship with Match Timer
The player timer feature complements the existing match timer:

```
Match Timer (Global)          Player Timer (Individual)
├── Start Match              ├── Start Player 1
├── Pause Match              ├── Pause Player 1
│                            ├── Start Player 2
│                            ├── Pause Player 2
│                            └── ...
└── End Match                └── (All player times persist)
```

### Database Relationships
```
┌──────────┐
│  teams   │
└────┬─────┘
     │ 1:N
     ▼
┌──────────┐         ┌──────────────┐
│ players  │◄────────┤ player_time  │
└──────────┘   1:1   └──────────────┘
     │
     │ N:1
     ▼
┌──────────┐
│  match   │
└──────────┘
```

## Performance Considerations

1. **Database Operations**: Upsert operations are efficient (O(1) for updates)
2. **Flow Emissions**: Real-time updates via Room's Flow support
3. **Memory**: Minimal - only stores accumulated milliseconds
4. **Concurrency**: Coroutines ensure safe async operations

## Future Enhancements

1. **Reset Player Time**: Add use case to reset individual player times
2. **Time Limits**: Add maximum time validation per player
3. **Fair Distribution**: Calculate suggested next player based on time
4. **Statistics**: Add aggregation for average time, total time, etc.
5. **Export**: Add functionality to export time data for reports
