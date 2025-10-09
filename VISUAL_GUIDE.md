# US-1.1.1: Player List Feature - Visual Guide

## UI Screenshots Description

### Main Screen - Player List (Success State)
```
┌────────────────────────────────────────────┐
│ TeamFlow Manager              [Android]    │
├────────────────────────────────────────────┤
│                                            │
│  Plantilla                                 │
│                                            │
│  ┌──────────────────────────────────────┐ │
│  │ Carlos García                        │ │
│  │ Portero                              │ │
│  └──────────────────────────────────────┘ │
│                                            │
│  ┌──────────────────────────────────────┐ │
│  │ Miguel López                         │ │
│  │ Defensa, Lateral derecho             │ │
│  └──────────────────────────────────────┘ │
│                                            │
│  ┌──────────────────────────────────────┐ │
│  │ David Martínez                       │ │
│  │ Defensa central                      │ │
│  └──────────────────────────────────────┘ │
│                                            │
│  ┌──────────────────────────────────────┐ │
│  │ Javier Sánchez                       │ │
│  │ Defensa, Lateral izquierdo           │ │
│  └──────────────────────────────────────┘ │
│                                            │
│  ┌──────────────────────────────────────┐ │
│  │ Antonio Fernández                    │ │
│  │ Centrocampista, Pivote               │ │
│  └──────────────────────────────────────┘ │
│                                            │
│  ... (scrollable list continues)          │
│                                            │
└────────────────────────────────────────────┘
```

### Loading State
```
┌────────────────────────────────────────────┐
│ TeamFlow Manager              [Android]    │
├────────────────────────────────────────────┤
│                                            │
│                                            │
│                                            │
│                   ⟳                        │
│           Loading...                       │
│                                            │
│                                            │
│                                            │
└────────────────────────────────────────────┘
```

### Empty State
```
┌────────────────────────────────────────────┐
│ TeamFlow Manager              [Android]    │
├────────────────────────────────────────────┤
│                                            │
│                                            │
│                                            │
│    Aún no hay jugadores registrados       │
│                                            │
│                                            │
│                                            │
│                                            │
└────────────────────────────────────────────┘
```

## Code Flow Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                   PlayersScreen.kt                          │
│  • Composable function                                      │
│  • Uses koinViewModel() to get PlayerViewModel             │
│  • Observes uiState with collectAsState()                  │
│  • Displays different UI based on state:                   │
│    - Loading → CircularProgressIndicator                   │
│    - Empty → "No players" message                          │
│    - Success → LazyColumn with player cards                │
└────────────────────┬────────────────────────────────────────┘
                     │ observes StateFlow<PlayerUiState>
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                   PlayerViewModel.kt                        │
│  • Extends ViewModel                                        │
│  • init { loadPlayers() }                                   │
│  • Private: _uiState = MutableStateFlow(Loading)           │
│  • Public: uiState = _uiState.asStateFlow()                │
│  • loadPlayers() collects from use case                    │
└────────────────────┬────────────────────────────────────────┘
                     │ calls invoke()
                     ▼
┌─────────────────────────────────────────────────────────────┐
│               GetPlayersUseCaseImpl.kt                      │
│  • Implements GetPlayersUseCase                             │
│  • operator fun invoke(): Flow<List<Player>>               │
│  • Delegates to playerRepository.getAllPlayers()           │
└────────────────────┬────────────────────────────────────────┘
                     │ calls getAllPlayers()
                     ▼
┌─────────────────────────────────────────────────────────────┐
│               PlayerRepositoryImpl.kt                       │
│  • Implements PlayerRepository                              │
│  • fun getAllPlayers(): Flow<List<Player>>                 │
│  • Delegates to localDataSource.getAllPlayers()            │
└────────────────────┬────────────────────────────────────────┘
                     │ calls getAllPlayers()
                     ▼
┌─────────────────────────────────────────────────────────────┐
│           PlayerLocalDataSourceImpl.kt                      │
│  • Implements PlayerLocalDataSource                         │
│  • fun getAllPlayers(): Flow<List<Player>>                 │
│  • Calls playerDao.getAllPlayers()                         │
│  • Maps PlayerEntity → Player                              │
│  • Converts positions string → List<String>                │
└────────────────────┬────────────────────────────────────────┘
                     │ calls getAllPlayers()
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                    PlayerDao.kt                             │
│  • @Dao interface                                           │
│  • @Query("SELECT * FROM players")                         │
│  • fun getAllPlayers(): Flow<List<PlayerEntity>>           │
└────────────────────┬────────────────────────────────────────┘
                     │ queries
                     ▼
┌─────────────────────────────────────────────────────────────┐
│             TeamFlowManagerDatabase.kt                      │
│  • @Database(entities = [PlayerEntity::class])             │
│  • RoomDatabase                                             │
│  • abstract fun playerDao(): PlayerDao                     │
└─────────────────────────────────────────────────────────────┘
```

## Dependency Injection Flow

```
                     ┌─────────────────────┐
                     │   Application       │
                     │  startKoin {        │
                     │   modules(          │
                     │     appModules      │
                     │   )                 │
                     │  }                  │
                     └──────────┬──────────┘
                                │
                ┌───────────────┼───────────────┐
                │               │               │
        ┌───────▼──────┐ ┌─────▼─────┐ ┌──────▼──────┐
        │ databaseModule│ │dataSource │ │ repository  │
        │              │ │  Module   │ │   Module    │
        │ - Database   │ │           │ │             │
        │ - PlayerDao  │ │ - Player  │ │ - Player    │
        │   (with      │ │   Local   │ │   Repository│
        │   Callback)  │ │   DataSrc │ │   Impl      │
        └──────────────┘ └───────────┘ └─────────────┘
                │               │               │
                └───────────────┼───────────────┘
                                │
                ┌───────────────┼───────────────┐
                │               │               │
        ┌───────▼──────┐ ┌─────▼──────┐       │
        │  useCaseModule│ │viewModel   │       │
        │              │ │  Module    │       │
        │ - GetPlayers │ │            │       │
        │   UseCase    │ │ - Player   │       │
        │   Impl       │ │   ViewModel│       │
        └──────────────┘ └────────────┘       │
                                               │
                                    ┌──────────▼──────────┐
                                    │   PlayersScreen     │
                                    │  uses koinViewModel │
                                    └─────────────────────┘
```

## Data Model Transformations

```
Room Database (SQLite)
       ↓
 PlayerEntity
 ┌─────────────────────┐
 │ id: Long            │
 │ firstName: String   │
 │ lastName: String    │
 │ positions: String   │ ← "Defensa,Lateral derecho"
 └─────────────────────┘
       ↓ (mapping in DataSource)
 Player (Domain Model)
 ┌─────────────────────┐
 │ id: Long            │
 │ firstName: String   │
 │ lastName: String    │
 │ positions:          │
 │   List<String>      │ ← ["Defensa", "Lateral derecho"]
 └─────────────────────┘
       ↓ (used in UI)
 Display in Card
 ┌─────────────────────┐
 │ Miguel López        │ ← firstName + " " + lastName
 │ Defensa, Lateral    │ ← positions.joinToString(", ")
 │ derecho             │
 └─────────────────────┘
```

## Test Structure

```
┌────────────────────────────────────────────────────────────┐
│                GetPlayersUseCaseTest.kt                    │
├────────────────────────────────────────────────────────────┤
│ • Mocks: PlayerRepository                                  │
│ • Tests:                                                   │
│   1. invoke should return players from repository         │
│   2. invoke should return empty list when no players      │
└────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────┐
│              PlayerRepositoryImplTest.kt                   │
├────────────────────────────────────────────────────────────┤
│ • Mocks: PlayerLocalDataSource                             │
│ • Tests:                                                   │
│   1. getAllPlayers returns players from data source       │
│   2. getAllPlayers returns empty list when no players     │
└────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────┐
│                PlayerViewModelTest.kt                      │
├────────────────────────────────────────────────────────────┤
│ • Mocks: GetPlayersUseCase                                 │
│ • Uses: StandardTestDispatcher for coroutines              │
│ • Tests:                                                   │
│   1. initial state should be Loading                      │
│   2. uiState should be Success when players are loaded    │
│   3. uiState should be Empty when no players exist        │
└────────────────────────────────────────────────────────────┘
```

## Module Dependencies Graph

```
                    ┌──────────┐
                    │  :domain │
                    └────┬─────┘
                         │
         ┌───────────────┼───────────────┐
         │               │               │
    ┌────▼────┐    ┌────▼────┐    ┌─────▼────┐
    │:usecase │    │:data:   │    │:viewmodel│
    │         │    │  core   │    │          │
    └────┬────┘    └────┬────┘    └─────┬────┘
         │              │                │
         │         ┌────▼────┐           │
         │         │:data:   │           │
         │         │  local  │           │
         │         └─────────┘           │
         │                               │
         └───────────────┬───────────────┘
                         │
                    ┌────▼────┐
                    │   :di   │
                    └────┬────┘
                         │
                    ┌────▼────┐
                    │  :app   │
                    └─────────┘
```

## Key Features Visualization

### Reactive Data Flow
```
Database Changed
      ↓
Room emits new data to Flow
      ↓
DataSource transforms to domain model
      ↓
Repository passes through
      ↓
UseCase passes through
      ↓
ViewModel updates StateFlow
      ↓
Compose UI recomposes automatically
      ↓
User sees updated list
```

### State Management
```
PlayerUiState (Sealed Class)
├── Loading (data object)
│   └── Shows: CircularProgressIndicator
├── Empty (data object)
│   └── Shows: "No players" message
└── Success(players: List<Player>)
    └── Shows: LazyColumn with PlayerItem cards
```

## File Structure Tree

```
teamflowmanager/
├── app/
│   └── src/main/
│       ├── java/.../ui/players/
│       │   └── PlayersScreen.kt          ← UI Layer
│       └── res/
│           ├── values/strings.xml         ← English strings
│           └── values-es/strings.xml      ← Spanish strings
│
├── viewmodel/
│   └── src/
│       ├── main/.../viewmodel/
│       │   └── PlayerViewModel.kt         ← Presentation Logic
│       └── test/.../viewmodel/
│           └── PlayerViewModelTest.kt     ← Tests
│
├── usecase/
│   └── src/
│       ├── main/.../usecase/
│       │   └── GetPlayersUseCase.kt       ← Business Logic
│       └── test/.../usecase/
│           └── GetPlayersUseCaseTest.kt   ← Tests
│
├── data/
│   ├── core/
│   │   └── src/
│   │       ├── main/.../data/core/
│   │       │   ├── datasource/
│   │       │   │   └── PlayerLocalDataSource.kt
│   │       │   └── repository/
│   │       │       └── PlayerRepositoryImpl.kt
│   │       └── test/.../data/core/repository/
│   │           └── PlayerRepositoryImplTest.kt
│   │
│   └── local/
│       └── src/main/.../data/local/
│           ├── entity/
│           │   └── PlayerEntity.kt        ← Room Entity
│           ├── dao/
│           │   └── PlayerDao.kt           ← Room DAO
│           ├── database/
│           │   └── TeamFlowManagerDatabase.kt
│           ├── datasource/
│           │   └── PlayerLocalDataSourceImpl.kt
│           └── callback/
│               └── DatabaseCallback.kt    ← Sample Data
│
├── domain/
│   └── src/main/.../domain/
│       ├── model/
│       │   └── Player.kt                  ← Domain Model
│       └── repository/
│           └── PlayerRepository.kt        ← Repository Interface
│
└── di/
    └── src/main/.../di/
        └── AppModule.kt                    ← DI Configuration
```

This visualization provides a complete overview of the player list feature implementation!
