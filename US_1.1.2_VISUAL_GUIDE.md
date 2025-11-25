# US-1.1.2: Add Player Feature - Visual Guide

## UI Screenshots Description

### Main Screen with Add Player Button
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
│  ... (scrollable list)                    │
│                                            │
│                                      ┌───┐│
│                                      │ + ││  ← FloatingActionButton
│                                      └───┘│
└────────────────────────────────────────────┘
```

### Add Player Dialog
```
┌────────────────────────────────────────────┐
│                                            │
│  ┌────────────────────────────────────┐   │
│  │  Añadir Jugador                    │   │
│  │                                    │   │
│  │  ┌──────────────────────────────┐ │   │
│  │  │ Nombre                       │ │   │
│  │  │ ________________________     │ │   │
│  │  └──────────────────────────────┘ │   │
│  │                                    │   │
│  │  ┌──────────────────────────────┐ │   │
│  │  │ Apellido                     │ │   │
│  │  │ ________________________     │ │   │
│  │  └──────────────────────────────┘ │   │
│  │                                    │   │
│  │  ┌──────────────────────────────┐ │   │
│  │  │ Fecha de Nacimiento          │ │   │
│  │  │ YYYY-MM-DD                   │ │   │
│  │  │ ________________________     │ │   │
│  │  └──────────────────────────────┘ │   │
│  │                                    │   │
│  │              ┌──────────┬────────┐│   │
│  │              │ Cancelar │ Guardar││   │
│  │              └──────────┴────────┘│   │
│  └────────────────────────────────────┘   │
│                                            │
└────────────────────────────────────────────┘
```

### Add Player Dialog with Validation Errors
```
┌────────────────────────────────────────────┐
│                                            │
│  ┌────────────────────────────────────┐   │
│  │  Añadir Jugador                    │   │
│  │                                    │   │
│  │  ┌──────────────────────────────┐ │   │
│  │  │ Nombre               (ERROR) │ │   │
│  │  │ ________________________     │ │   │
│  │  │ El nombre es obligatorio     │ │   │
│  │  └──────────────────────────────┘ │   │
│  │                                    │   │
│  │  ┌──────────────────────────────┐ │   │
│  │  │ Apellido             (ERROR) │ │   │
│  │  │ ________________________     │ │   │
│  │  │ El apellido es obligatorio   │ │   │
│  │  └──────────────────────────────┘ │   │
│  │                                    │   │
│  │  ┌──────────────────────────────┐ │   │
│  │  │ Fecha de Nacimiento  (ERROR) │ │   │
│  │  │ invalid-date                 │ │   │
│  │  │ La fecha de nacimiento es    │ │   │
│  │  │ obligatoria                  │ │   │
│  │  └──────────────────────────────┘ │   │
│  │                                    │   │
│  │              ┌──────────┬────────┐│   │
│  │              │ Cancelar │ Guardar││   │
│  │              └──────────┴────────┘│   │
│  └────────────────────────────────────┘   │
│                                            │
└────────────────────────────────────────────┘
```

### After Adding Player - List Updated
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
│  │ Juan Nuevo                   ← NEW   │ │
│  │                                      │ │
│  └──────────────────────────────────────┘ │
│                                            │
│  ┌──────────────────────────────────────┐ │
│  │ Miguel López                         │ │
│  │ Defensa, Lateral derecho             │ │
│  └──────────────────────────────────────┘ │
│                                            │
│                                      ┌───┐│
│                                      │ + ││
│                                      └───┘│
└────────────────────────────────────────────┘
```

## Code Flow Diagram - Add Player

```
User clicks FAB (+)
       ↓
showAddPlayerDialog = true
       ↓
AddPlayerDialog displayed
       ↓
User fills form & clicks Guardar
       ↓
Validation runs
       ↓
┌──────────────────────┐
│  Validation Logic    │
├──────────────────────┤
│ 1. firstName.isBlank │
│    → firstNameError  │
│ 2. lastName.isBlank  │
│    → lastNameError   │
│ 3. dateOfBirth parse │
│    → dateOfBirthError│
└──────────────────────┘
       ↓
If valid: Player object created
       ↓
viewModel.addPlayer(player)
       ↓
┌─────────────────────────────────────────────────────────────┐
│                   PlayerViewModel.kt                        │
│  fun addPlayer(player: Player) {                            │
│    viewModelScope.launch {                                  │
│      addPlayerUseCase.invoke(player)                        │
│    }                                                        │
│  }                                                          │
└────────────────────┬────────────────────────────────────────┘
                     │ calls invoke()
                     ▼
┌─────────────────────────────────────────────────────────────┐
│               AddPlayerUseCaseImpl.kt                       │
│  suspend operator fun invoke(player: Player) {              │
│    playerRepository.addPlayer(player)                       │
│  }                                                          │
└────────────────────┬────────────────────────────────────────┘
                     │ calls addPlayer()
                     ▼
┌─────────────────────────────────────────────────────────────┐
│               PlayerRepositoryImpl.kt                       │
│  suspend fun addPlayer(player: Player) {                    │
│    localDataSource.insertPlayer(player)                     │
│  }                                                          │
└────────────────────┬────────────────────────────────────────┘
                     │ calls insertPlayer()
                     ▼
┌─────────────────────────────────────────────────────────────┐
│           PlayerLocalDataSourceImpl.kt                      │
│  suspend fun insertPlayer(player: Player) {                 │
│    playerDao.insertPlayer(player.toEntity())                │
│  }                                                          │
└────────────────────┬────────────────────────────────────────┘
                     │ calls insertPlayer()
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                    PlayerDao.kt                             │
│  @Insert                                                    │
│  suspend fun insertPlayer(player: PlayerEntity)             │
└────────────────────┬────────────────────────────────────────┘
                     │ inserts into
                     ▼
┌─────────────────────────────────────────────────────────────┐
│             Room Database (SQLite)                          │
│  INSERT INTO players (firstName, lastName, dateOfBirth,     │
│                      positions) VALUES (...)                │
└────────────────────┬────────────────────────────────────────┘
                     │ Room Flow detects change
                     ▼
┌─────────────────────────────────────────────────────────────┐
│  PlayerDao.getAllPlayers() Flow emits new list              │
│         ↓                                                   │
│  DataSource maps to domain                                  │
│         ↓                                                   │
│  Repository passes through                                  │
│         ↓                                                   │
│  UseCase passes through                                     │
│         ↓                                                   │
│  ViewModel updates uiState                                  │
│         ↓                                                   │
│  Compose UI recomposes                                      │
│         ↓                                                   │
│  User sees updated list with new player                     │
└─────────────────────────────────────────────────────────────┘
```

## Updated Data Model with Date of Birth

```
Room Database (SQLite)
       ↓
 PlayerEntity
 ┌─────────────────────┐
 │ id: Long            │
 │ firstName: String   │
 │ lastName: String    │
 │ dateOfBirth: String │ ← "2010-05-15" (ISO-8601)
 │ positions: String   │
 └─────────────────────┘
       ↓ (mapping in DataSource)
 Player (Domain Model)
 ┌─────────────────────┐
 │ id: Long            │
 │ firstName: String   │
 │ lastName: String    │
 │ dateOfBirth:        │
 │   LocalDate         │ ← LocalDate.parse("2010-05-15")
 │ positions:          │
 │   List<Position>    │
 └─────────────────────┘
       ↓ (used in UI)
 Display in Card
 ┌─────────────────────┐
 │ Juan Nuevo          │ ← firstName + " " + lastName
 │                     │ ← (dateOfBirth not shown in list)
 └─────────────────────┘
```

## Form Validation Flow

```
┌──────────────────────────────────────────────────────────┐
│                  Validation Logic                        │
├──────────────────────────────────────────────────────────┤
│                                                          │
│  var hasError = false                                    │
│                                                          │
│  ┌────────────────────────────────┐                     │
│  │ if (firstName.isBlank()) {     │                     │
│  │   firstNameError = true        │                     │
│  │   hasError = true              │                     │
│  │ }                              │                     │
│  └────────────────────────────────┘                     │
│                                                          │
│  ┌────────────────────────────────┐                     │
│  │ if (lastName.isBlank()) {      │                     │
│  │   lastNameError = true         │                     │
│  │   hasError = true              │                     │
│  │ }                              │                     │
│  └────────────────────────────────┘                     │
│                                                          │
│  ┌────────────────────────────────┐                     │
│  │ val parsedDate = try {         │                     │
│  │   LocalDate.parse(             │                     │
│  │     dateOfBirth,               │                     │
│  │     DateTimeFormatter.         │                     │
│  │       ISO_LOCAL_DATE           │                     │
│  │   )                            │                     │
│  │ } catch (e: DateTimeParser...) │                     │
│  │   dateOfBirthError = true      │                     │
│  │   hasError = true              │                     │
│  │   null                         │                     │
│  │ }                              │                     │
│  └────────────────────────────────┘                     │
│                                                          │
│  ┌────────────────────────────────┐                     │
│  │ if (!hasError && parsedDate != │                     │
│  │     null) {                    │                     │
│  │   onSave(                      │                     │
│  │     Player(                    │                     │
│  │       firstName = firstName,   │                     │
│  │       lastName = lastName,     │                     │
│  │       dateOfBirth = parsedDate,│                     │
│  │       positions = emptyList()  │                     │
│  │     )                          │                     │
│  │   )                            │                     │
│  │ }                              │                     │
│  └────────────────────────────────┘                     │
│                                                          │
└──────────────────────────────────────────────────────────┘
```

## Updated Test Structure

```
┌────────────────────────────────────────────────────────────┐
│                AddPlayerUseCaseTest.kt                     │
├────────────────────────────────────────────────────────────┤
│ • Mocks: PlayerRepository                                  │
│ • Tests:                                                   │
│   1. invoke should add player to repository               │
└────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────┐
│              PlayerRepositoryImplTest.kt                   │
├────────────────────────────────────────────────────────────┤
│ • Mocks: PlayerLocalDataSource                             │
│ • Tests:                                                   │
│   1. getAllPlayers returns players from data source       │
│   2. getAllPlayers returns empty list when no players     │
│   3. addPlayer should call insertPlayer on data source    │ ← NEW
└────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────┐
│                PlayerViewModelTest.kt                      │
├────────────────────────────────────────────────────────────┤
│ • Mocks: GetPlayersUseCase, AddPlayerUseCase               │
│ • Uses: StandardTestDispatcher for coroutines              │
│ • Tests:                                                   │
│   1. initial state should be Loading                      │
│   2. uiState should be Success when players are loaded    │
│   3. uiState should be Empty when no players exist        │
│   4. addPlayer should call addPlayerUseCase               │ ← NEW
└────────────────────────────────────────────────────────────┘
```

## Updated Module Dependencies

```
                    ┌──────────┐
                    │  :domain │
                    │ + Player │ ← dateOfBirth: LocalDate
                    └────┬─────┘
                         │
         ┌───────────────┼───────────────┐
         │               │               │
    ┌────▼────┐    ┌────▼────┐    ┌─────▼────┐
    │:usecase │    │:data:   │    │:viewmodel│
    │+ AddPlay│    │  core   │    │+ addPlay │
    │  erUse  │    │+ addPlay│    │  er()    │
    │  Case   │    │  er()   │    │          │
    └────┬────┘    └────┬────┘    └─────┬────┘
         │              │                │
         │         ┌────▼────┐           │
         │         │:data:   │           │
         │         │  local  │           │
         │         │+ insert │           │
         │         │  Player │           │
         │         └─────────┘           │
         │                               │
         └───────────────┬───────────────┘
                         │
                    ┌────▼────┐
                    │   :di   │
                    │+ AddPlay│
                    │  erUse  │
                    │  Case   │
                    └────┬────┘
                         │
                    ┌────▼────┐
                    │  :app   │
                    │+ AddPlay│
                    │  erDialog│
                    │+ FAB    │
                    └─────────┘
```

## Key Features

### 1. FloatingActionButton
- Positioned at bottom-right corner
- Icon: Add (+)
- Action: Opens AddPlayerDialog

### 2. AddPlayerDialog
- Form fields:
  - First Name (required)
  - Last Name (required)
  - Date of Birth (required, format: YYYY-MM-DD)
- Validation:
  - Shows error messages inline
  - Validates on save button click
  - Prevents saving if validation fails
- Buttons:
  - Cancel: Closes dialog without saving
  - Save: Validates and saves if valid

### 3. Automatic List Update
- Room Flow automatically emits new data
- ViewModel collects and updates UI state
- Compose recomposes with new player visible

### 4. Clean Architecture
- All layers properly separated
- Each layer has clear responsibilities
- Dependency injection with Koin
- Testable with unit tests at each layer

## Internationalization (i18n)

### English (values/strings.xml)
```xml
<string name="add_player">Add Player</string>
<string name="first_name">First Name</string>
<string name="last_name">Last Name</string>
<string name="date_of_birth">Date of Birth</string>
<string name="save">Save</string>
<string name="cancel">Cancel</string>
<string name="first_name_required">First name is required</string>
<string name="last_name_required">Last name is required</string>
<string name="date_of_birth_required">Date of birth is required</string>
```

### Spanish (values-es/strings.xml)
```xml
<string name="add_player">Añadir Jugador</string>
<string name="first_name">Nombre</string>
<string name="last_name">Apellido</string>
<string name="date_of_birth">Fecha de Nacimiento</string>
<string name="save">Guardar</string>
<string name="cancel">Cancelar</string>
<string name="first_name_required">El nombre es obligatorio</string>
<string name="last_name_required">El apellido es obligatorio</string>
<string name="date_of_birth_required">La fecha de nacimiento es obligatoria</string>
```

This feature implements US-1.1.2 completely with all acceptance criteria met!
