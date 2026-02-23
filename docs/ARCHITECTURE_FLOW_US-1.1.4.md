# Delete Player Feature - Technical Flow Diagram

## User Interaction Flow

```
┌─────────────────────────────────────────────────────────────────────┐
│                          PlayersScreen                              │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │  Player List                                                   │  │
│  │  ┌─────────────────────────────────────────────────────────┐  │  │
│  │  │  Player Item: John Doe                          [🗑️]    │  │  │
│  │  │  Positions: Forward, Midfielder                         │  │  │
│  │  └─────────────────────────────────────────────────────────┘  │  │
│  │                                                                │  │
│  │  User clicks delete icon ──────────────────────────────────┐  │  │
│  └────────────────────────────────────────────────────────────┼──┘  │
└─────────────────────────────────────────────────────────────────┼───┘
                                                                   │
                                                                   ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    Confirmation Dialog                              │
│  ╔═══════════════════════════════════════════════════════════════╗  │
│  ║               Delete Player                                   ║  │
│  ║                                                               ║  │
│  ║  Are you sure you want to delete John Doe from the roster?   ║  │
│  ║                                                               ║  │
│  ║                   [Cancel]          [Delete]                 ║  │
│  ╚═══════════════════════════════════════════════════════════════╝  │
└─────────────────────────────────────────────────────────────────────┘
                           │                     │
                    User Cancels          User Confirms
                           │                     │
                           ▼                     ▼
                    Dialog Closes        Deletion Process
```

## Technical Data Flow

```
┌──────────────────────────────────────────────────────────────────────┐
│                        UI Layer (app)                                │
│                                                                      │
│  PlayersScreen.kt                                                   │
│  ├─ PlayerItem() - Shows delete button                             │
│  └─ DeleteConfirmationDialog() - Confirmation UI                   │
└────────────────────────────┬─────────────────────────────────────────┘
                             │ User confirms deletion
                             ▼
┌──────────────────────────────────────────────────────────────────────┐
│                    ViewModel Layer (viewmodel)                       │
│                                                                      │
│  PlayerViewModel                                                     │
│  ├─ deleteConfirmationState: StateFlow<DeleteConfirmationState>    │
│  ├─ showDeleteConfirmation(player: Player)                         │
│  ├─ dismissDeleteConfirmation()                                     │
│  └─ deletePlayer(playerId: Long) ──┐                               │
└─────────────────────────────────────┼────────────────────────────────┘
                                      │
                                      ▼
┌──────────────────────────────────────────────────────────────────────┐
│                     UseCase Layer (usecase)                          │
│                                                                      │
│  DeletePlayerUseCase                                                 │
│  └─ invoke(playerId: Long) ──────┐                                  │
└───────────────────────────────────┼──────────────────────────────────┘
                                    │
                                    ▼
┌──────────────────────────────────────────────────────────────────────┐
│                  Repository Layer (data:core)                        │
│                                                                      │
│  PlayerRepository (interface - in usecase)                           │
│  PlayerRepositoryImpl                                                │
│  └─ deletePlayer(playerId: Long) ──┐                                │
└─────────────────────────────────────┼────────────────────────────────┘
                                      │
                                      ▼
┌──────────────────────────────────────────────────────────────────────┐
│                 DataSource Layer (data:local)                        │
│                                                                      │
│  PlayerLocalDataSource (interface - in data:core)                   │
│  PlayerLocalDataSourceImpl                                           │
│  └─ deletePlayer(playerId: Long) ──┐                                │
└─────────────────────────────────────┼────────────────────────────────┘
                                      │
                                      ▼
┌──────────────────────────────────────────────────────────────────────┐
│                      DAO Layer (data:local)                          │
│                                                                      │
│  PlayerDao (Room)                                                    │
│  └─ @Query("DELETE FROM players WHERE id = :playerId")              │
└──────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌──────────────────────────────────────────────────────────────────────┐
│                     Database (Room SQLite)                           │
│                                                                      │
│  players table                                                       │
│  └─ Row with id=playerId is DELETED                                 │
└──────────────────────────────────────────────────────────────────────┘
                                      │
                                      │ Room Flow emits updated list
                                      ▼
                              UI automatically updates
```

## State Management Flow

```
Initial State:
┌──────────────────────────────────────┐
│ PlayerUiState: Success(players)      │
│ DeleteConfirmationState: None        │
└──────────────────────────────────────┘
                  │
                  │ User clicks delete icon
                  ▼
┌──────────────────────────────────────┐
│ PlayerUiState: Success(players)      │
│ DeleteConfirmationState: Confirming │
└──────────────────────────────────────┘
                  │
                  │ User confirms
                  ▼
┌──────────────────────────────────────┐
│ PlayerUiState: Success(players)      │  ← Deletion in progress
│ DeleteConfirmationState: None        │
└──────────────────────────────────────┘
                  │
                  │ Database updates, Room Flow emits
                  ▼
┌──────────────────────────────────────┐
│ PlayerUiState: Success(new_players)  │  ← UI automatically refreshed
│ DeleteConfirmationState: None        │
└──────────────────────────────────────┘
```

## Class Relationships

```
┌─────────────────────────────────────────────────────────────────┐
│                         Domain Layer                            │
│  ┌───────────────┐                                              │
│  │ Player (data) │                                              │
│  │ - id: Long    │                                              │
│  │ - firstName   │                                              │
│  │ - lastName    │                                              │
│  │ - positions   │                                              │
│  └───────────────┘                                              │
└─────────────────────────────────────────────────────────────────┘
           ▲                            ▲
           │ uses                       │ uses
           │                            │
┌──────────┴────────────┐    ┌─────────┴────────────┐
│  DeletePlayerUseCase  │    │  PlayerRepository    │
│  (interface)          │    │  (interface)         │
│  - invoke(Long)       │───▶│  - deletePlayer(Long)│
└───────────────────────┘    └──────────────────────┘
           △                            △
           │ implements                 │ implements
           │                            │
┌──────────┴────────────────┐  ┌───────┴──────────────────────┐
│ DeletePlayerUseCaseImpl   │  │ PlayerRepositoryImpl         │
│ - playerRepository        │  │ - localDataSource            │
└───────────────────────────┘  └──────────────────────────────┘
                                          │ uses
                                          ▼
                           ┌──────────────────────────────────┐
                           │ PlayerLocalDataSource (interface)│
                           │ - deletePlayer(Long)             │
                           └──────────────────────────────────┘
                                          △
                                          │ implements
                                          │
                           ┌──────────────┴───────────────────┐
                           │ PlayerLocalDataSourceImpl        │
                           │ - playerDao                      │
                           └──────────────────────────────────┘
                                          │ uses
                                          ▼
                           ┌──────────────────────────────────┐
                           │ PlayerDao (@Dao)                 │
                           │ - deletePlayer(Long)             │
                           │   @Query("DELETE FROM...")       │
                           └──────────────────────────────────┘
```

## Dependency Injection Graph

```
┌─────────────────────────────────────────────────────────────────┐
│                         Koin DI Container                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ViewModelModule:                                               │
│  └─ PlayerViewModel ──┬─→ GetPlayersUseCase                    │
│                       └─→ DeletePlayerUseCase                   │
│                                                                 │
│  UseCaseModule:                                                 │
│  ├─ GetPlayersUseCaseImpl → GetPlayersUseCase                  │
│  └─ DeletePlayerUseCaseImpl → DeletePlayerUseCase ─┐           │
│                                                     │           │
│  DataCoreModule:                                    │           │
│  └─ PlayerRepositoryImpl → PlayerRepository ←──────┘           │
│                                   │                             │
│  DataLocalModule:                 │                             │
│  ├─ PlayerLocalDataSourceImpl → PlayerLocalDataSource ←────────┘
│  ├─ PlayerDao                                                   │
│  └─ TeamFlowManagerDatabase                                     │
└─────────────────────────────────────────────────────────────────┘
```

## Testing Structure

```
┌─────────────────────────────────────────────────────────────────┐
│                      Test Layer Organization                     │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ViewModelTest (viewmodel/src/test)                             │
│  └─ PlayerViewModelTest                                         │
│     ├─ Mock: GetPlayersUseCase                                  │
│     ├─ Mock: DeletePlayerUseCase                                │
│     └─ Tests:                                                   │
│        ├─ showDeleteConfirmation updates state                  │
│        ├─ dismissDeleteConfirmation resets state                │
│        └─ deletePlayer calls use case                           │
│                                                                 │
│  UseCaseTest (usecase/src/test)                                 │
│  └─ DeletePlayerUseCaseTest                                     │
│     ├─ Mock: PlayerRepository                                   │
│     └─ Tests:                                                   │
│        └─ invoke calls repository.deletePlayer                  │
│                                                                 │
│  RepositoryTest (data:core/src/test)                            │
│  └─ PlayerRepositoryImplTest                                    │
│     ├─ Mock: PlayerLocalDataSource                              │
│     └─ Tests:                                                   │
│        └─ deletePlayer delegates to data source                 │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

## Key Design Patterns Used

1. **Clean Architecture**: Separation of concerns across layers
2. **Repository Pattern**: Abstraction of data access
3. **Use Case Pattern**: Single responsibility business logic
4. **MVVM Pattern**: ViewModel manages UI state
5. **Observer Pattern**: StateFlow for reactive updates
6. **Dependency Injection**: Koin for loose coupling
7. **Interface Segregation**: Small, focused interfaces

---

This implementation follows industry best practices for Android development with Kotlin and provides a solid foundation for future enhancements.
