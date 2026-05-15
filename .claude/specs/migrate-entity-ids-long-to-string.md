# Spec: Migrate entity IDs from Long to String (remove toStableId)

## Task summary
Replace all domain entity `id: Long` fields (computed via `toStableId()` hash of Firestore document IDs) with `id: String` matching Firestore document IDs directly, eliminating the hash indirection and enabling direct document lookups.

## Files to read (before implementing)
- `data/remote/src/commonMain/.../util/IdUtils.kt` -- the `toStableId()` function to delete
- All domain models in `domain/src/commonMain/.../model/` -- current `id: Long` fields
- All repository interfaces in `usecase/src/commonMain/.../repository/` -- method signatures with Long IDs
- All use case interfaces in `domain/src/commonMain/.../usecase/` -- method signatures with Long IDs
- All Firestore models in `data/remote/src/androidMain/.../firestore/` and `data/remote/src/iosMain/.../firestore/` -- `toDomain()` and `toFirestoreModel()` mappers
- All datasource impls in `data/remote/src/androidMain/.../datasource/` and `data/remote/src/iosMain/.../datasource/` -- `findDocumentIdByMatchId` patterns
- `data/core/src/commonMain/.../datasource/*.kt` -- datasource interfaces
- `data/core/src/commonMain/.../repository/*.kt` -- repository implementations
- `viewmodel/src/commonMain/.../viewmodel/*.kt` -- all ViewModels using Long IDs
- `shared-ui/src/commonMain/.../ui/navigation/Route.kt` -- navigation routes with Long args
- `app/src/main/java/.../ui/navigation/Navigation.kt` -- Android navigation using NavType.LongType
- `viewmodel/src/androidMain/.../di/ViewModelModule.kt` -- DI wiring
- `di/src/iosMain/.../di/IosModule.kt` -- iOS DI wiring
- All existing tests in `usecase/src/androidUnitTest/`, `data/core/src/androidUnitTest/`, `data/remote/src/androidUnitTest/`

## Architecture decisions

- **Decision 1**: All entity IDs become `String` -- Firestore document IDs are already strings; the Long hash was a Room-era artifact that is no longer needed.
- **Decision 2**: Cross-reference ID fields in Firestore models (`matchId`, `playerId`, `scorerId`, `playerOutId`, `playerInId`, `captainId`) also change from `Long` to `String` -- these stored hashed values in Firestore documents; going forward they store the raw Firestore document ID string.
- **Decision 3**: `List<Long>` fields (`squadCallUpIds`, `startingLineupIds` in `Match`/`SkeletonMatch`) become `List<String>` -- these reference player IDs.
- **Decision 4**: Navigation route args change from `NavType.LongType` to `NavType.StringType` -- match IDs and player IDs passed via nav are now strings.
- **Decision 5**: Return types of `createMatch()`, `insertMatch()`, `addPlayer()`, `insertPlayer()`, `insertGoal()`, `insertSubstitution()`, `insertPlayerTimeHistory()` change from `Long` to `String`.
- **Decision 6**: `findDocumentIdByMatchId()` and similar reverse-lookup helpers are deleted -- with String IDs matching Firestore document IDs, direct `collection.document(id)` lookups replace them.
- **Decision 7**: `getMatchById(matchId, teamId)` signature changes: `matchId` becomes `String`, the `teamId: String?` parameter is REMOVED (no longer needed because we can do direct document lookups by ID instead of querying all team matches and filtering in-memory).
- **Decision 8**: Same removal of `teamId: String?` from `getMatchGoals`, `getMatchSubstitutions`, `getPlayerTimesByMatch`, `getMatchPlayerTimeHistory`, and corresponding use cases.
- **Decision 9**: `PreferencesRepository.getDefaultCaptainId()` / `setDefaultCaptainId()` change from `Long?` to `String?`.
- **Decision 10**: `Club.remoteId`, `Team.remoteId`, `ClubMember.remoteId`, `ClubMember.clubRemoteId`, `Team.clubRemoteId` fields are REMOVED -- they are redundant when `id` is already the Firestore document ID string. `Club.remoteId`, `Team.clubRemoteId`, `ClubMember.clubRemoteId` collapse into the `id`/`clubId` fields.
- **Decision 11**: `Team.clubId: Long?` becomes `clubId: String?` and absorbs `clubRemoteId`.
- **Decision 12**: `ClubMemberRepository.createOrUpdateClubMember` and `ClubMemberDataSource.createOrUpdateClubMember` -- remove `clubNumericId: Long` parameter (was the hashed club ID); `clubId: String` is sufficient.
- **Decision 13**: `TeamRepository.updateTeamClubId` -- remove `clubNumericId: Long` parameter; keep only `teamId: String` and `clubId: String`.
- **Decision 14**: Firestore data migration is NOT needed at the schema level. New Firestore documents will store String IDs in cross-reference fields. Existing documents with Long cross-reference fields (`matchId: Long` in goals/substitutions/playerTimes/playerTimeHistory/matchOperations) become orphaned from direct lookups and are effectively legacy data. This is acceptable because: (a) the app already fetches these by `teamId` + `matchId` filter, and (b) existing users will need their in-progress data to remain accessible. The implementer MUST add backward compatibility: when reading, check if `matchId` is numeric (legacy Long) vs alphanumeric (new String doc ID) in Firestore documents.

**WAIT -- REVISED Decision 14**: Actually, the cross-reference fields in Firestore (`matchId`, `playerId`, `playerOutId`, `playerInId`, `scorerId` in Goal/Substitution/PlayerTimeHistory/MatchOperation Firestore models) are stored as `Long` values. These hashed Long values exist in production Firestore. The Firestore models need to transition these to `String` for new documents, but existing documents still have `Long` values. However, since we cannot change existing Firestore data without a migration, the simplest approach is:
- **Decision 14 (final)**: Change Firestore cross-reference fields to store String document IDs for all NEW documents. For existing data, write a Firestore migration utility OR accept that existing matches (pre-migration) will not be accessible via the new code. Given this is a personal project, the migration utility is optional -- the spec assumes a clean break. Document this as a risk.

## Implementation steps

### Phase 1: Match (highest impact)

#### 1.1 Domain model
1. `domain/src/commonMain/.../model/Match.kt` -- Change `id: Long = 0L` to `id: String = ""`, `teamId: Long = 1L` to `teamId: String = ""`, `captainId: Long` to `captainId: String = ""`, `squadCallUpIds: List<Long>` to `List<String>`, `startingLineupIds: List<Long>` to `List<String>`
2. `domain/src/commonMain/.../model/SkeletonMatch.kt` -- Change `captainId: Long` to `String`, `squadCallUpIds: List<Long>` to `List<String>`, `startingLineupIds: List<Long>` to `List<String>`
3. `domain/src/commonMain/.../model/MatchOperation.kt` -- Change `matchId: Long` to `String`, `teamId: Long` to `String`

#### 1.2 Use case interfaces (domain)
4. `domain/src/commonMain/.../usecase/GetMatchByIdUseCase.kt` -- `matchId: Long` -> `String`, remove `teamId: String?` param
5. `domain/src/commonMain/.../usecase/GetAllPlayerTimesUseCase.kt` -- `matchId: Long` -> `String`, remove `teamId: String?`
6. `domain/src/commonMain/.../usecase/GetMatchSummaryUseCase.kt` -- `matchId: Long` -> `String`, remove `teamId: String?`
7. `domain/src/commonMain/.../usecase/GetMatchTimelineUseCase.kt` -- `matchId: Long` -> `String`, remove `teamId: String?`
8. `domain/src/commonMain/.../usecase/GetMatchSubstitutionsUseCase.kt` -- `matchId: Long` -> `String`
9. `domain/src/commonMain/.../usecase/GetMatchReportDataUseCase.kt` -- `matchId: Long` -> `String`
10. `domain/src/commonMain/.../usecase/StartMatchTimerUseCase.kt` -- `matchId: Long` -> `String`
11. `domain/src/commonMain/.../usecase/PauseMatchUseCase.kt` -- `matchId: Long` -> `String`
12. `domain/src/commonMain/.../usecase/ResumeMatchUseCase.kt` -- `matchId: Long` -> `String`
13. `domain/src/commonMain/.../usecase/FinishMatchUseCase.kt` -- `matchId: Long` -> `String`
14. `domain/src/commonMain/.../usecase/ArchiveMatchUseCase.kt` -- `matchId: Long` -> `String`
15. `domain/src/commonMain/.../usecase/UnarchiveMatchUseCase.kt` -- `matchId: Long` -> `String`
16. `domain/src/commonMain/.../usecase/DeleteMatchUseCase.kt` -- `matchId: Long` -> `String`
17. `domain/src/commonMain/.../usecase/StartTimeoutUseCase.kt` -- `matchId: Long` -> `String`
18. `domain/src/commonMain/.../usecase/EndTimeoutUseCase.kt` -- `matchId: Long` -> `String`
19. `domain/src/commonMain/.../usecase/RegisterGoalUseCase.kt` -- `matchId: Long` -> `String`, `scorerId: Long?` -> `String?`
20. `domain/src/commonMain/.../usecase/RegisterPlayerSubstitutionUseCase.kt` -- `matchId: Long` -> `String`, `playerOutId: Long` -> `String`, `playerInId: Long` -> `String`
21. `domain/src/commonMain/.../usecase/StartPlayerTimersBatchUseCase.kt` -- `matchId: Long` -> `String`, `playerIds: List<Long>` -> `List<String>`
22. `domain/src/commonMain/.../usecase/SaveDefaultCaptainUseCase.kt` -- `playerId: Long?` -> `String?`
23. `domain/src/commonMain/.../usecase/PausePlayerTimerForMatchPauseUseCase.kt` -- `playerId: Long` -> `String`

#### 1.3 Repository interfaces (usecase module)
24. `usecase/src/commonMain/.../repository/MatchRepository.kt` -- All `matchId: Long` -> `String`, `captainId: Long?` -> `String?`, remove `teamId: String?` from `getMatchById`, `createMatch` returns `String` instead of `Long`, `deleteMatch(matchId: String)`
25. `usecase/src/commonMain/.../repository/GoalRepository.kt` -- `matchId: Long` -> `String`, remove `teamId: String?` from `getMatchGoals`, `insertGoal` returns `String`
26. `usecase/src/commonMain/.../repository/PlayerSubstitutionRepository.kt` -- `matchId: Long` -> `String`, remove `teamId: String?` from `getMatchSubstitutions`, `insertSubstitution` returns `String`
27. `usecase/src/commonMain/.../repository/PlayerTimeRepository.kt` -- All `matchId: Long` -> `String`, all `playerId: Long` -> `String`, all `playerIds: List<Long>` -> `List<String>`, remove `teamId: String?` from `getPlayerTimesByMatch`
28. `usecase/src/commonMain/.../repository/PlayerTimeHistoryRepository.kt` -- `playerId: Long` -> `String`, `matchId: Long` -> `String`, remove `teamId: String?` from `getMatchPlayerTimeHistory`, `insertPlayerTimeHistory` returns `String`
29. `usecase/src/commonMain/.../repository/PreferencesRepository.kt` -- `getDefaultCaptainId(): String?`, `setDefaultCaptainId(playerId: String?)`

#### 1.4 DataSource interfaces (data/core)
30. `data/core/src/commonMain/.../datasource/MatchDataSource.kt` -- Mirror changes from MatchRepository: all `matchId: Long` -> `String`, `captainId: Long?` -> `String?`, remove `teamId: String?`, `insertMatch` returns `String`
31. `data/core/src/commonMain/.../datasource/GoalDataSource.kt` -- Mirror changes from GoalRepository
32. `data/core/src/commonMain/.../datasource/PlayerSubstitutionDataSource.kt` -- Mirror changes
33. `data/core/src/commonMain/.../datasource/PlayerTimeDataSource.kt` -- Mirror changes, all `matchId: Long` -> `String`, `playerId: Long` -> `String`, remove `teamId: String?`
34. `data/core/src/commonMain/.../datasource/PlayerTimeHistoryDataSource.kt` -- Mirror changes
35. `data/core/src/commonMain/.../datasource/MatchOperationDataSource.kt` -- No signature changes (already uses `String` operation ID)

#### 1.5 Data/core repository implementations
36. `data/core/src/commonMain/.../repository/MatchRepositoryImpl.kt` -- Update all method signatures, remove `teamId` passthrough
37. `data/core/src/commonMain/.../repository/GoalRepositoryImpl.kt` -- Update signatures
38. `data/core/src/commonMain/.../repository/PlayerSubstitutionRepositoryImpl.kt` -- Update signatures
39. `data/core/src/commonMain/.../repository/PlayerTimeRepositoryImpl.kt` -- Update all signatures, change all `Long` params to `String`
40. `data/core/src/commonMain/.../repository/PlayerTimeHistoryRepositoryImpl.kt` -- Update signatures
41. `data/core/src/commonMain/.../repository/MatchOperationRepositoryImpl.kt` -- No changes expected
42. `data/core/src/commonMain/.../repository/PreferencesRepositoryImpl.kt` -- Update `getDefaultCaptainId`/`setDefaultCaptainId`

#### 1.6 Firestore models (Android)
43. `data/remote/src/androidMain/.../firestore/MatchFirestoreModel.kt`:
    - `squadCallUpIds: List<Long>` -> `List<String>`, `captainId: Long` -> `String`, `startingLineupIds: List<Long>` -> `List<String>`
    - `toDomain()`: replace `id.toStableId()` with `id`, `teamId.toStableId()` with `teamId`
    - `toFirestoreModel()`: now sets `id` and `teamId` from domain model strings (not empty)
44. `data/remote/src/androidMain/.../firestore/PlayerFirestoreModel.kt`:
    - `toDomain()`: replace `id.toStableId()` with `id`, `teamId.toStableId()` with `teamId`
45. `data/remote/src/androidMain/.../firestore/GoalFirestoreModel.kt`:
    - `matchId: Long` -> `String`, `scorerId: Long?` -> `String?`
    - `toDomain()`: replace `id.toStableId()` with `id`
46. `data/remote/src/androidMain/.../firestore/PlayerSubstitutionFirestoreModel.kt`:
    - `matchId: Long` -> `String`, `playerOutId: Long` -> `String`, `playerInId: Long` -> `String`
    - `toDomain()`: replace `id.toStableId()` with `id`
47. `data/remote/src/androidMain/.../firestore/PlayerTimeHistoryFirestoreModel.kt`:
    - `playerId: Long` -> `String`, `matchId: Long` -> `String`
    - `toDomain()`: replace `id.toStableId()` with `id`
48. `data/remote/src/androidMain/.../firestore/MatchOperationFirestoreModel.kt`:
    - `matchId: Long` -> `String`
    - `toDomain()`: replace `teamId.toStableId()` with `teamId`
49. `data/remote/src/androidMain/.../firestore/TeamFirestoreModel.kt`:
    - `captainId: Long?` -> `String?`
    - `toDomain()`: replace `id.toStableId()` with `id`, remove `clubId?.toStableId()`, remove `remoteId = id` (redundant)
50. `data/remote/src/androidMain/.../firestore/ClubFirestoreModel.kt`:
    - `toDomain()`: replace `id.toStableId()` with `id`, remove `remoteId = id`
51. `data/remote/src/androidMain/.../firestore/ClubMemberFirestoreModel.kt`:
    - `toDomain()`: replace `id.toStableId()` with `id`, `clubId.toStableId()` with `clubId`, remove `remoteId`/`clubRemoteId`

#### 1.7 Firestore models (iOS) -- same changes as Android
52. `data/remote/src/iosMain/.../firestore/MatchFirestoreModel.kt` -- same changes as step 43
53. `data/remote/src/iosMain/.../firestore/PlayerFirestoreModel.kt` -- same changes as step 44
54. `data/remote/src/iosMain/.../firestore/GoalFirestoreModel.kt` -- same changes as step 45
55. `data/remote/src/iosMain/.../firestore/PlayerSubstitutionFirestoreModel.kt` -- same changes as step 46
56. `data/remote/src/iosMain/.../firestore/PlayerTimeHistoryFirestoreModel.kt` -- same changes as step 47
57. `data/remote/src/iosMain/.../firestore/MatchOperationFirestoreModel.kt` -- same changes as step 48
58. `data/remote/src/iosMain/.../firestore/TeamFirestoreModel.kt` -- same changes as step 49
59. `data/remote/src/iosMain/.../firestore/ClubFirestoreModel.kt` -- same changes as step 50
60. `data/remote/src/iosMain/.../firestore/ClubMemberFirestoreModel.kt` -- same changes as step 51

#### 1.8 Firestore datasource implementations (Android)
61. `data/remote/src/androidMain/.../datasource/MatchFirestoreDataSourceImpl.kt`:
    - Delete `findDocumentIdByMatchId()` helper
    - `getMatchById(matchId: String)`: use `firestore.collection(MATCHES_COLLECTION).document(matchId).addSnapshotListener` for direct doc lookup (no more query + in-memory filter)
    - `updateMatch`: use `match.id` directly as document ID
    - `deleteMatch(matchId: String)`: use `firestore.collection(MATCHES_COLLECTION).document(matchId).delete()`
    - `updateMatchCaptain(matchId: String, captainId: String?)`: direct doc update
    - `insertMatch`: return `docRef.id` (String) instead of `docRef.id.toStableId()`
    - Remove `toStableId` import
62. `data/remote/src/androidMain/.../datasource/GoalFirestoreDataSourceImpl.kt`:
    - Delete `findMatchDocumentId()` helper
    - `getMatchGoals(matchId: String)`: query `.whereEqualTo("matchId", matchId)` (now String field)
    - `insertGoal`: set `matchId` as String, `matchDocId` as same matchId string, return `docRef.id`
    - Remove `toStableId` import
63. `data/remote/src/androidMain/.../datasource/PlayerFirestoreDataSourceImpl.kt`:
    - Update all methods that take `playerId: Long` to `String`
    - Direct document lookups via `document(playerId)` instead of iterating
    - `insertPlayer`: return `docRef.id` instead of `docRef.id.toStableId()`
    - `setPlayerAsCaptain(playerId: String)`, `removePlayerAsCaptain(playerId: String)`: direct doc update
64. `data/remote/src/androidMain/.../datasource/PlayerSubstitutionFirestoreDataSourceImpl.kt`:
    - `getMatchSubstitutions(matchId: String)`: query with String matchId
    - `insertSubstitution`: return `docRef.id`
65. `data/remote/src/androidMain/.../datasource/PlayerTimeHistoryFirestoreDataSourceImpl.kt`:
    - `getPlayerTimeHistory(playerId: String)`, `getMatchPlayerTimeHistory(matchId: String)`: query with String
    - `insertPlayerTimeHistory`: return `docRef.id`
66. `data/remote/src/androidMain/.../datasource/MatchFirestoreDataSourceImpl.kt` (MatchOperation): update if needed

#### 1.9 Firestore datasource implementations (iOS)
67-72. Same changes as steps 61-66 but for `data/remote/src/iosMain/.../datasource/` files.

#### 1.10 Delete `toStableId`
73. Delete `data/remote/src/commonMain/.../util/IdUtils.kt`

### Phase 2: Team

#### 2.1 Domain model
74. `domain/src/commonMain/.../model/Team.kt` -- Change `id: Long` to `String`, `captainId: Long?` to `String?`, `clubId: Long?` to `String?`. Remove `clubRemoteId: String?` and `remoteId: String?` (redundant).

#### 2.2 Repository / DataSource
75. No additional repository interface changes needed for Team (already uses `String` teamId in most methods). Verify `TeamRepository.updateTeamClubId` -- remove `clubNumericId: Long` param.
76. Update `ClubMemberRepository.createOrUpdateClubMember` -- remove `clubNumericId: Long` param.
77. Update `ClubMemberDataSource.createOrUpdateClubMember` -- remove `clubNumericId: Long` param.

### Phase 3: Player

#### 3.1 Domain model
78. `domain/src/commonMain/.../model/Player.kt` -- Change `id: Long` to `String`, `teamId: Long` to `String`

#### 3.2 Repository / DataSource
79. `usecase/src/commonMain/.../repository/PlayerRepository.kt` -- `getPlayerById(playerId: String)`, `addPlayer` returns `String`, `deletePlayer(playerId: String)`, `setPlayerAsCaptain(playerId: String)`, `removePlayerAsCaptain(playerId: String)`
80. `data/core/src/commonMain/.../datasource/PlayerDataSource.kt` -- same changes
81. `domain/src/commonMain/.../usecase/GetPlayerByIdUseCase.kt` -- `playerId: String`
82. `domain/src/commonMain/.../usecase/SetPlayerAsCaptainUseCase.kt` -- `playerId: String`
83. `domain/src/commonMain/.../usecase/RemovePlayerAsCaptainUseCase.kt` -- `playerId: String`
84. `domain/src/commonMain/.../usecase/DeletePlayerUseCase.kt` -- `playerId: String`

### Phase 4: Goal, PlayerSubstitution, PlayerTime, PlayerTimeHistory

#### 4.1 Domain models
85. `domain/src/commonMain/.../model/Goal.kt` -- `id: String = ""`, `matchId: String`, `scorerId: String?`
86. `domain/src/commonMain/.../model/PlayerSubstitution.kt` -- `id: String = ""`, `matchId: String`, `playerOutId: String`, `playerInId: String`
87. `domain/src/commonMain/.../model/PlayerTime.kt` -- `playerId: String`, `matchId: String = ""`
88. `domain/src/commonMain/.../model/PlayerTimeHistory.kt` -- `id: String = ""`, `playerId: String`, `matchId: String`

### Phase 5: Club, ClubMember

#### 5.1 Domain models
89. `domain/src/commonMain/.../model/Club.kt` -- `id: String`, remove `remoteId: String?`
90. `domain/src/commonMain/.../model/ClubMember.kt` -- `id: String`, `clubId: String`, remove `remoteId: String?`, `clubRemoteId: String?`

### Phase 6: Navigation & UI

#### 6.1 Route.kt
91. `shared-ui/src/commonMain/.../navigation/Route.kt`:
    - `Route.Match.FULL_ROUTE`: keep pattern `match/{matchId}` but arg is now String
    - `Route.PlayerWizard.FULL_ROUTE`: arg is now String, use `"new"` instead of `0L` for create mode
    - `Route.CreateMatch.FULL_ROUTE`: arg is now String, `DEFAULT_MATCH_ID = ""` instead of `0L`
    - `Route.PresidentMatchDetail.createRoute(teamId: String, matchId: String)`: matchId becomes String

#### 6.2 Navigation.kt (Android)
92. `app/src/main/java/.../navigation/Navigation.kt`:
    - All `NavType.LongType` for match/player IDs -> `NavType.StringType`
    - `backStackEntry.arguments?.getLong(...)` -> `getString(...)`
    - Deep link pattern remains the same but arg interpretation changes
    - `Route.PlayerWizard.createRoute("new")` for create, pass player.id for edit
    - `Route.CreateMatch.createRoute("")` for create

#### 6.3 ViewModels
93. `viewmodel/src/commonMain/.../MatchViewModel.kt`:
    - `matchId: Long` -> `String`
    - `teamId: String?` -- REMOVE this parameter entirely (no longer needed)
    - `_selectedPlayerOut: MutableStateFlow<Long?>` -> `String?`
    - All `beginMatch(matchId: Long)` -> `String`, `resumeMatch(matchId: Long)` -> `String`
    - `selectPlayerOut(playerId: Long)` -> `String`
    - `substitutePlayer(playerInId: Long)` -> `String`
    - `substitutePlayerDirect(playerInId: Long, playerOutId: Long)` -> `String, String`
    - `registerGoal(scorerId: Long?)` -> `String?`
    - Remove `teamId` from all `getMatchById(matchId, teamId)` calls
94. `viewmodel/src/commonMain/.../MatchCreationWizardViewModel.kt`:
    - `matchId: Long` -> `String`
    - `squadCallUpIds: Set<Long>` -> `Set<String>`, `captainId: Long` -> `String`, `startingLineupIds: Set<Long>` -> `Set<String>`
    - `activeMatchId: Long` -> `String`
    - `isEditMode = matchId != 0L` -> `matchId.isNotEmpty()`
95. `viewmodel/src/commonMain/.../PlayerWizardViewModel.kt`:
    - `playerId: Long` -> `String`
    - Edit mode check: `playerId != 0L` -> `playerId.isNotEmpty()` or `playerId != "new"`
96. `viewmodel/src/commonMain/.../MatchUiState.kt`:
    - `toPlayerItems(playerTimes, currentTime, captainId: Long)` -> `captainId: String`
97. `viewmodel/src/commonMain/.../MatchListViewModel.kt` -- No constructor changes, but use case calls now pass String IDs
98. `viewmodel/src/commonMain/.../ArchivedMatchesViewModel.kt` -- Same
99. `viewmodel/src/commonMain/.../PresidentTeamDetailViewModel.kt` -- `onNavigateToMatch` callback changes from `(Long)` to `(String)`

#### 6.4 Shared-UI screens
100. `shared-ui/src/commonMain/.../matches/MatchScreen.kt`:
     - `fun MatchScreen(matchId: Long, ...)` -> `String`
     - Remove `teamId` parameter if present
101. `shared-ui/src/commonMain/.../matches/MatchListScreen.kt`:
     - `onNavigateToEditMatch: (Long) -> Unit` -> `(String) -> Unit`
     - `onNavigateToMatch: (Match) -> Unit` -- still passes Match object, navigation extracts `.id` (now String)
102. `shared-ui/src/commonMain/.../matches/wizard/MatchCreationWizardScreen.kt`:
     - `matchId: Long` -> `String`
103. `shared-ui/src/commonMain/.../players/wizard/PlayerWizardScreen.kt`:
     - `playerId: Long` -> `String`
104. `shared-ui/src/commonMain/.../club/PresidentTeamDetailScreen.kt`:
     - `onNavigateToMatch: (Long) -> Unit` -> `(String) -> Unit`
105. `shared-ui/src/commonMain/.../matches/ArchivedMatchesScreen.kt` -- navigation uses `match.id` (String)
106. `shared-ui/src/commonMain/.../matches/wizard/SquadCallUpStep.kt`, `StartingLineupStep.kt`, `CaptainSelectionStep.kt` -- update player ID types from Long to String

#### 6.5 DI wiring
107. `viewmodel/src/androidMain/.../di/ViewModelModule.kt`:
     - `MatchViewModel` params: first param is now `String`, remove second `String?` param (teamId)
     - `MatchCreationWizardViewModel` params: first param is now `String`
     - `PlayerWizardViewModel` params: first param is now `String`
108. `di/src/iosMain/.../di/IosModule.kt` -- Same DI changes as Android

### Phase 7: Use case implementations
109. Update ALL use case `Impl` classes in `usecase/src/commonMain/` to match their updated interface signatures. Key files:
     - `GetMatchByIdUseCaseImpl.kt` -- remove `teamId` param
     - `RegisterGoalUseCaseImpl.kt` -- `matchId: String, scorerId: String?`
     - `RegisterPlayerSubstitutionUseCaseImpl.kt` -- `matchId: String, playerOutId: String, playerInId: String`
     - `StartMatchTimerUseCaseImpl.kt` -- `matchId: String`
     - `FinishMatchUseCaseImpl.kt` -- `matchId: String`
     - `PauseMatchUseCaseImpl.kt` -- `matchId: String`
     - `ResumeMatchUseCaseImpl.kt` -- `matchId: String`
     - `StartPlayerTimersBatchUseCaseImpl.kt` -- `matchId: String, playerIds: List<String>`
     - `GetAllPlayerTimesUseCaseImpl.kt` -- `matchId: String`, remove `teamId`
     - `GetMatchSummaryUseCaseImpl.kt` -- `matchId: String`, remove `teamId`
     - `GetMatchTimelineUseCaseImpl.kt` -- `matchId: String`, remove `teamId`
     - `GetMatchSubstitutionsUseCaseImpl.kt` -- `matchId: String`
     - `GetMatchReportDataUseCaseImpl.kt` -- `matchId: String`
     - All other use cases with Long IDs

### Phase 8: Tests
110. Update ALL test files in:
     - `usecase/src/androidUnitTest/` -- update test data from Long to String IDs, fix mock signatures
     - `data/core/src/androidUnitTest/` -- update repository impl tests
     - `data/remote/src/androidUnitTest/` -- update datasource tests
     - `domain/src/androidUnitTest/` -- update MatchTest

## Source set rules
- Domain models: commonMain only
- Use case interfaces: commonMain (domain module)
- Use case implementations: commonMain (usecase module)
- Repository interfaces: commonMain (usecase module)
- Repository implementations: commonMain (data/core module)
- DataSource interfaces: commonMain (data/core module)
- Firestore models: androidMain and iosMain (data/remote) -- separate implementations
- Firestore datasource impls: androidMain and iosMain (data/remote) -- separate implementations
- ViewModels: commonMain (viewmodel module)
- Navigation Route.kt: commonMain (shared-ui module)
- Navigation.kt: Android-only (app module)
- DI: androidMain (ViewModelModule.kt) and iosMain (IosModule.kt)
- Expected/actual needed: No -- this is a pure type change, no platform-specific logic introduced

## Repository / DataSource rules
- Extend existing: All existing repository and datasource interfaces/implementations are modified in place. No new classes needed.
- Delete: `data/remote/src/commonMain/.../util/IdUtils.kt` (contains only `toStableId()`)

## DI wiring
- `viewmodel/src/androidMain/.../di/ViewModelModule.kt`:
  - `MatchViewModel { params -> ... }`: change `matchId = params.get()` from Long to String, remove `teamId = params.getOrNull()`
  - `MatchCreationWizardViewModel { params -> ... }`: change `matchId = params.get()` from Long to String
  - `PlayerWizardViewModel { params -> ... }`: change `playerId = params.get()` from Long to String
- `di/src/iosMain/.../di/IosModule.kt`:
  - Same changes in `factory { params -> ... }` blocks for MatchViewModel, MatchCreationWizardViewModel, PlayerWizardViewModel

## Test coverage points
- All existing use case tests must pass with String IDs
- All existing data/core repository tests must pass with String IDs
- `MatchTest` in domain must be updated for String IDs
- Verify navigation routes correctly pass and parse String IDs
- Verify Firestore datasource direct document lookups work (replacing list+filter pattern)
- Verify `getMatchById` works WITHOUT `teamId` parameter

## Risks / Ambiguities

1. **Existing Firestore data with Long cross-references**: Production Firestore documents (goals, substitutions, playerTimes, playerTimeHistory, matchOperations) store `matchId`, `playerId`, `scorerId`, `playerOutId`, `playerInId` as `Long` values (hashed from document IDs). After migration, new documents will store `String` document IDs. This means:
   - Existing goals/substitutions/etc. will have `matchId: 12345678` (Long) while new ones will have `matchId: "abc123XYZ"` (String).
   - Firestore queries like `.whereEqualTo("matchId", "abc123")` will NOT match old documents with `matchId: 12345678`.
   - **Mitigation**: For a personal project, this is a clean break. Old match data is effectively archived/inaccessible after migration. If backward compat is needed, a Cloud Function migration script would be required to update all existing cross-reference fields.

2. **MatchFirestoreModel.squadCallUpIds and startingLineupIds in Firestore**: These are currently stored as `List<Long>` in Firestore. Changing to `List<String>` means existing match documents with Long arrays will fail deserialization for these fields. **Mitigation**: The Firestore model must handle mixed types gracefully (try String first, fall back to Long conversion) or accept data loss for existing scheduled matches.

3. **TeamFirestoreModel.captainId**: Currently stored as `Long` in Firestore. Changing to `String` means existing teams with a captainId set as Long will need migration or will lose their captain assignment.

4. **PreferencesRepository.defaultCaptainId**: Stored in SharedPreferences/NSUserDefaults as Long. Changing to String means the stored value becomes invalid after app update. The implementation should handle the migration (read old Long, clear it, or map it).

5. **Navigation deep links**: The deep link `teamflowmanager://match/{matchId}` currently expects a Long. After migration it expects a String. Existing notification deep links pointing to old-format match IDs will break.

6. **PlayerWizard "create" mode sentinel**: Currently uses `0L` to indicate "create new player". Must change to empty string `""` or a sentinel like `"new"`. The implementer must choose one convention and apply consistently.

7. **MatchCreationWizard "create" mode sentinel**: Same as above -- `0L` -> `""` or `"new"`.

8. **Firestore PlayerTime**: `PlayerTime` has no `id` field -- it uses `playerId` + `matchId` as composite key. The Firestore document ID for PlayerTime is typically `{playerId}_{matchId}`. After migration, the document ID format remains the same but the `playerId` and `matchId` fields inside become String.

9. **`GoalDataSource`/`PlayerSubstitutionDataSource`**: These interfaces (in `data/core`) are missing from the explicit read list but need `matchId: Long` -> `String` changes. The implementer must also check `data/core/src/commonMain/.../datasource/GoalDataSource.kt` for method signatures.

10. **Batch operations in PlayerTimeRepository**: Methods like `startTimersBatch(matchId, playerIds, ...)` take `List<Long>` -> `List<String>`. The Firestore implementation builds document IDs from these -- verify the document ID construction pattern still works.