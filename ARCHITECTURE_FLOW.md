# US-1.1.3: Edit Player - Architecture Flow

## Component Interaction Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                         UI Layer (:app)                          │
│                                                                   │
│  PlayersScreen.kt                                                │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │ Player List                                               │   │
│  │ ┌────────────────────────────────────────────────────┐   │   │
│  │ │ Player Card [John Doe] [Edit Button 📝]           │   │   │
│  │ └────────────────────────────────────────────────────┘   │   │
│  │                           │                               │   │
│  │                           │ onClick                       │   │
│  │                           ▼                               │   │
│  │  EditPlayerDialog.kt                                     │   │
│  │  ┌────────────────────────────────────────────────────┐  │   │
│  │  │ Edit Player                                         │  │   │
│  │  │ First Name: [John         ]                        │  │   │
│  │  │ Last Name:  [Doe          ]                        │  │   │
│  │  │ Birth Date: [01/01/2010   ]                        │  │   │
│  │  │ Positions:  ☑ Forward                              │  │   │
│  │  │             ☐ Midfielder                           │  │   │
│  │  │             [Cancel] [Save]                        │  │   │
│  │  └────────────────────────────────────────────────────┘  │   │
│  └──────────────────────────────────────────────────────────┘   │
└───────────────────────────────┬─────────────────────────────────┘
                                │ onSave(updatedPlayer)
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                    ViewModel Layer (:viewmodel)                  │
│                                                                   │
│  PlayerViewModel.kt                                              │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │ fun updatePlayer(player: Player) {                       │   │
│  │     viewModelScope.launch {                              │   │
│  │         updatePlayerUseCase.invoke(player)               │   │
│  │     }                                                     │   │
│  │ }                                                         │   │
│  └──────────────────────────────────────────────────────────┘   │
└───────────────────────────────┬─────────────────────────────────┘
                                │ invoke(player)
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                     UseCase Layer (:usecase)                     │
│                                                                   │
│  UpdatePlayerUseCase.kt                                          │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │ suspend fun invoke(player: Player) {                     │   │
│  │     playerRepository.updatePlayer(player)                │   │
│  │ }                                                         │   │
│  └──────────────────────────────────────────────────────────┘   │
└───────────────────────────────┬─────────────────────────────────┘
                                │ updatePlayer(player)
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                   Data Core Layer (:data:core)                   │
│                                                                   │
│  PlayerRepositoryImpl.kt                                         │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │ override suspend fun updatePlayer(player: Player) {      │   │
│  │     localDataSource.updatePlayer(player)                 │   │
│  │ }                                                         │   │
│  └──────────────────────────────────────────────────────────┘   │
└───────────────────────────────┬─────────────────────────────────┘
                                │ updatePlayer(player)
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                  Data Local Layer (:data:local)                  │
│                                                                   │
│  PlayerLocalDataSourceImpl.kt                                    │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │ override suspend fun updatePlayer(player: Player) {      │   │
│  │     playerDao.updatePlayer(player.toEntity())            │   │
│  │ }                                                         │   │
│  └───────────────────┬──────────────────────────────────────┘   │
│                      │ updatePlayer(playerEntity)                │
│                      ▼                                            │
│  PlayerDao.kt (Room)                                             │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │ @Update                                                   │   │
│  │ suspend fun updatePlayer(player: PlayerEntity)           │   │
│  └───────────────────┬──────────────────────────────────────┘   │
│                      │                                            │
│                      ▼                                            │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │           Room Database (SQLite)                         │   │
│  │  UPDATE players                                           │   │
│  │  SET firstName = ?, lastName = ?,                        │   │
│  │      dateOfBirth = ?, positions = ?                      │   │
│  │  WHERE id = ?                                            │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                                │
                                │ Flow<List<Player>> (automatic update)
                                ▼
                    [Player List Updates Automatically]
```

## Data Model Evolution

### Before
```kotlin
data class Player(
    val id: Long = 0,
    val firstName: String,
    val lastName: String,
    val positions: List<Position>
)
```

### After
```kotlin
data class Player(
    val id: Long = 0,
    val firstName: String,
    val lastName: String,
    val dateOfBirth: Date?,          // NEW FIELD
    val positions: List<Position>
)
```

## Database Schema

### Players Table (Version 2)
```sql
CREATE TABLE players (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    firstName TEXT NOT NULL,
    lastName TEXT NOT NULL,
    dateOfBirth INTEGER,                    -- NEW: Unix timestamp (nullable)
    positions TEXT NOT NULL                 -- CSV: "forward,midfielder"
)
```

## Key Features

1. **Clean Architecture**: Strict separation of concerns across layers
2. **Reactive Updates**: Room Flow ensures automatic UI updates
3. **Type Safety**: Domain models separated from database entities
4. **Testability**: All layers have unit tests with Mockk
5. **Dependency Injection**: Koin manages all dependencies
6. **Localization**: English and Spanish strings
7. **Validation**: Form ensures required fields are filled
8. **Multi-select**: Users can assign multiple positions to a player

## Testing Strategy

```
Domain Layer ← UseCase Layer ← Data Layer ← ViewModel Layer
     ↓              ↓               ↓              ↓
   (Pure)      Unit Tests     Unit Tests     Unit Tests
              (Mockk+JUnit)  (Mockk+JUnit)  (Mockk+JUnit)
```

Each layer is tested independently with mocked dependencies.
