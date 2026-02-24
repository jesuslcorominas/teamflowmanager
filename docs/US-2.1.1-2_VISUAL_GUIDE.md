# US-2.1.1/2: Visual Guide - Register Match and Define Lineup

## User Flow

```
Players Screen
    │
    ├─→ [FAB: Matches] → Match List Screen (Empty)
    │                         │
    │                         └─→ [FAB: +] → Match Detail Screen (Create Mode)
    │                                              │
    │                                              ├─ Fill Opponent
    │                                              ├─ Fill Location  
    │                                              ├─ Select Starting Lineup
    │                                              ├─ Select Substitutes
    │                                              └─→ [Save] → Match List Screen (With Matches)
    │                                                                │
    │                                                                ├─→ [Tap Match] → Match Detail Screen (Edit Mode)
    │                                                                │                       │
    │                                                                │                       └─→ [Save] → Match List Screen
    │                                                                │
    │                                                                └─→ [Delete Button] → Confirmation Dialog
    │                                                                                          │
    │                                                                                          └─→ [Confirm] → Match Deleted
    └─→ [FAB: Play] → Session Screen
```

## Screen Mockups

### 1. Players Screen (Updated)
```
┌─────────────────────────────────┐
│        Team Name         [i]    │
├─────────────────────────────────┤
│                                 │
│  Player Cards List...           │
│                                 │
│                                 │
│                                 │
│                                 │
│                    ┌──┐ ┌──┐ ┌──┐│
│                    │📄│ │▶ │ │+│││ ← New Matches FAB
│                    └──┘ └──┘ └──┘│
└─────────────────────────────────┘
   Matches  Session  Add Player
```

### 2. Match List Screen (Empty State)
```
┌─────────────────────────────────┐
│        Team Name         [i]    │
├─────────────────────────────────┤
│                                 │
│                                 │
│  Aún no hay partidos           │
│      registrados                │
│                                 │
│                                 │
│                             ┌──┐│
│                             │+│││ ← Add Match FAB
│                             └──┘│
└─────────────────────────────────┘
```

### 3. Match List Screen (With Matches)
```
┌─────────────────────────────────┐
│        Team Name         [i]    │
├─────────────────────────────────┤
│ ┌─────────────────────────────┐ │
│ │ Rival FC              [✏][🗑]│ │
│ │ Stadium Name                │ │
│ │ 25/10/2025 18:00           │ │
│ │ Once Inicial: 11           │ │
│ │ Suplentes: 5               │ │
│ └─────────────────────────────┘ │
│ ┌─────────────────────────────┐ │
│ │ Team B FC             [✏][🗑]│ │
│ │ Away Stadium               │ │
│ │ 01/11/2025 20:00           │ │
│ │ Once Inicial: 11           │ │
│ │ Suplentes: 4               │ │
│ └─────────────────────────────┘ │
│                             ┌──┐│
│                             │+│││
│                             └──┘│
└─────────────────────────────────┘
```

### 4. Match Detail Screen (Create Mode)
```
┌─────────────────────────────────┐
│ [←] Registrar Partido           │
├─────────────────────────────────┤
│ Rival:                          │
│ ┌─────────────────────────────┐ │
│ │ [Rival FC____________]      │ │
│ └─────────────────────────────┘ │
│                                 │
│ Lugar:                          │
│ ┌─────────────────────────────┐ │
│ │ [Stadium Name_______]       │ │
│ └─────────────────────────────┘ │
│                                 │
│ Seleccionar Once Inicial        │
│ ┌─────────────────────────────┐ │
│ │ ☑ 10 - John Doe             │ │
│ │ ☐  7 - Jane Smith           │ │
│ │ ☑  5 - Bob Johnson          │ │
│ │ ...                         │ │
│ └─────────────────────────────┘ │
│                                 │
│ Seleccionar Suplentes           │
│ ┌─────────────────────────────┐ │
│ │ ☐ 10 - John Doe             │ │
│ │ ☑  7 - Jane Smith           │ │
│ │ ☐  5 - Bob Johnson          │ │
│ │ ...                         │ │
│ └─────────────────────────────┘ │
│                                 │
│ [Cancelar]         [Guardar]    │
└─────────────────────────────────┘
```

### 5. Delete Confirmation Dialog
```
┌─────────────────────────────────┐
│                                 │
│  ┌───────────────────────────┐  │
│  │ Eliminar Partido          │  │
│  │                           │  │
│  │ ¿Estás seguro de que      │  │
│  │ quieres eliminar este     │  │
│  │ partido?                  │  │
│  │                           │  │
│  │  [Cancelar]   [Eliminar]  │  │
│  └───────────────────────────┘  │
│                                 │
└─────────────────────────────────┘
```

## Data Flow Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                         UI Layer                            │
│  ┌───────────────────┐           ┌─────────────────────┐   │
│  │ MatchListScreen   │           │ MatchDetailScreen   │   │
│  │                   │           │                     │   │
│  │ - Display matches │           │ - Form fields       │   │
│  │ - Delete action   │           │ - Player selection  │   │
│  │ - Navigation      │           │ - Save action       │   │
│  └─────────┬─────────┘           └──────────┬──────────┘   │
└────────────┼────────────────────────────────┼──────────────┘
             │                                │
             ▼                                ▼
┌────────────────────────────────────────────────────────────┐
│                      ViewModel Layer                       │
│  ┌──────────────────┐           ┌────────────────────────┐│
│  │MatchListViewModel│           │ MatchDetailViewModel   ││
│  │                  │           │                        ││
│  │ - uiState Flow   │           │ - uiState Flow         ││
│  │ - CRUD actions   │           │ - Load match           ││
│  │ - Delete confirm │           │ - Load players         ││
│  └────────┬─────────┘           └──────────┬─────────────┘│
└───────────┼────────────────────────────────┼──────────────┘
            │                                │
            ▼                                ▼
┌──────────────────────────────────────────────────────────┐
│                    Use Case Layer                        │
│  ┌──────────────────────────────────────────────────┐   │
│  │ CreateMatchUseCase                               │   │
│  │ UpdateMatchUseCase                               │   │
│  │ GetAllMatchesUseCase                             │   │
│  │ GetMatchByIdUseCase                              │   │
│  │ DeleteMatchUseCase                               │   │
│  │ GetPlayersUseCase                                │   │
│  └─────────────────────┬────────────────────────────┘   │
└────────────────────────┼─────────────────────────────────┘
                         │
                         ▼
┌──────────────────────────────────────────────────────────┐
│                   Repository Layer                       │
│  ┌──────────────────────────────────────────────────┐   │
│  │ MatchRepository (interface)                      │   │
│  │  └─ MatchRepositoryImpl                          │   │
│  │                                                   │   │
│  │ PlayerRepository (interface)                     │   │
│  │  └─ PlayerRepositoryImpl                         │   │
│  └─────────────────────┬────────────────────────────┘   │
└────────────────────────┼─────────────────────────────────┘
                         │
                         ▼
┌──────────────────────────────────────────────────────────┐
│                  DataSource Layer                        │
│  ┌──────────────────────────────────────────────────┐   │
│  │ MatchLocalDataSource (interface)                 │   │
│  │  └─ MatchLocalDataSourceImpl                     │   │
│  └─────────────────────┬────────────────────────────┘   │
└────────────────────────┼─────────────────────────────────┘
                         │
                         ▼
┌──────────────────────────────────────────────────────────┐
│                     Database Layer                       │
│  ┌──────────────────────────────────────────────────┐   │
│  │ MatchDao (Room DAO)                              │   │
│  │  - getMatchById()                                │   │
│  │  - getAllMatches()                               │   │
│  │  - insertMatch()                                 │   │
│  │  - updateMatch()                                 │   │
│  │  - deleteMatch()                                 │   │
│  └─────────────────────┬────────────────────────────┘   │
└────────────────────────┼─────────────────────────────────┘
                         │
                         ▼
┌──────────────────────────────────────────────────────────┐
│              Room SQLite Database                        │
│                                                          │
│  Table: match                                            │
│  - id (PRIMARY KEY, AUTOINCREMENT)                       │
│  - teamId                                                │
│  - opponent                                              │
│  - location                                              │
│  - date (timestamp)                                      │
│  - startingLineupIds (comma-separated)                   │
│  - substituteIds (comma-separated)                       │
│  - elapsedTimeMillis                                     │
│  - isRunning                                             │
│  - lastStartTimeMillis                                   │
└──────────────────────────────────────────────────────────┘
```

## Key Components Interaction

### Creating a Match
```
User Action: Tap "Save" in MatchDetailScreen
    ↓
MatchListViewModel.createMatch(match)
    ↓
CreateMatchUseCase(match)
    ↓
MatchRepository.createMatch(match)
    ↓
MatchLocalDataSource.insertMatch(match)
    ↓
MatchDao.insertMatch(entity)
    ↓
Room Database: INSERT INTO match...
    ↓
Flow Update: getAllMatches() emits new list
    ↓
MatchListViewModel.uiState updates
    ↓
UI: MatchListScreen shows new match
```

### Loading Matches
```
App Start / Screen Navigation
    ↓
MatchListViewModel.init
    ↓
GetAllMatchesUseCase()
    ↓
MatchRepository.getAllMatches()
    ↓
MatchLocalDataSource.getAllMatches()
    ↓
MatchDao.getAllMatches() returns Flow<List<MatchEntity>>
    ↓
Convert to Domain: List<Match>
    ↓
MatchListViewModel.uiState = Success(matches)
    ↓
UI: MatchListScreen displays match cards
```

## Domain Model

```kotlin
data class Match(
    val id: Long = 0L,                    // Auto-generated
    val teamId: Long = 1L,                // Team reference
    val opponent: String? = null,         // Opponent name
    val location: String? = null,         // Match venue
    val date: Long? = null,               // Timestamp
    val startingLineupIds: List<Long>,    // Player IDs
    val substituteIds: List<Long>,        // Player IDs
    // Timer fields (existing functionality)
    val elapsedTimeMillis: Long = 0L,
    val isRunning: Boolean = false,
    val lastStartTimeMillis: Long? = null
)
```

## Testing Coverage

### Use Case Tests (5 classes, 6 tests)
```
✓ CreateMatchUseCaseTest
  ✓ invoke should create match in repository and return id

✓ UpdateMatchUseCaseTest
  ✓ invoke should update match in repository

✓ GetAllMatchesUseCaseTest
  ✓ invoke should return all matches from repository

✓ DeleteMatchUseCaseTest
  ✓ invoke should delete match from repository

✓ GetMatchByIdUseCaseTest
  ✓ invoke should return match from repository by id
  ✓ invoke should return null when match not found
```

### ViewModel Tests (2 classes, 12 tests)
```
✓ MatchListViewModelTest
  ✓ initial state should be Loading
  ✓ should emit Empty state when no matches available
  ✓ should emit Success state with matches
  ✓ createMatch should invoke createMatchUseCase
  ✓ updateMatch should invoke updateMatchUseCase
  ✓ requestDeleteMatch should update deleteConfirmationState
  ✓ confirmDeleteMatch should invoke deleteMatchUseCase
  ✓ cancelDeleteMatch should reset deleteConfirmationState

✓ MatchDetailViewModelTest
  ✓ initial state should be Loading
  ✓ loadMatch with null id should emit Create state
  ✓ loadMatch with valid id should emit Edit state
  ✓ loadMatch with invalid id should emit NotFound state
```

## Bilingual Support

### Spanish Strings (values-es/strings.xml)
- matches_title: "Partidos"
- add_match_title: "Registrar Partido"
- opponent: "Rival"
- location: "Lugar"
- starting_lineup: "Once Inicial"
- substitutes: "Suplentes"
- And 15 more...

### English Strings (values/strings.xml)
- matches_title: "Matches"
- add_match_title: "Register Match"
- opponent: "Opponent"
- location: "Location"
- starting_lineup: "Starting Lineup"
- substitutes: "Substitutes"
- And 15 more...

## Files Summary

### Created: 32 files
- 5 Use Cases
- 5 Use Case Tests
- 2 ViewModels
- 2 ViewModel Tests
- 2 UI Screens
- 1 Utility (DateFormatter)
- 1 Documentation (this file)

### Modified: 15 files
- Domain models
- Database entities and DAOs
- DataSources
- Repositories
- DI modules
- Navigation
- String resources

---

## Next Steps

1. **Manual Testing**
   - Install app on device/emulator
   - Test complete flow from creating to deleting matches
   - Verify lineup selection works correctly
   - Test with many players in roster

2. **Code Formatting**
   - Run ktlint when build environment is fixed
   - Ensure all code follows project style guidelines

3. **Potential Enhancements** (Future work)
   - Date/time picker for match scheduling
   - Match result recording
   - Match statistics
   - Export match lineups
   - Match history views
