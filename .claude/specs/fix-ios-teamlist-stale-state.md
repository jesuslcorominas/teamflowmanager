# Spec: Fix iOS team list stale state on first load (VM caching)

## Task summary
Add reset methods to president-tab ViewModels and call them via `LaunchedEffect(Unit)` in each screen composable so that iOS users never see stale cached state when switching tabs.

## Files to read (before implementing)
- `shared-ui/src/commonMain/kotlin/.../ui/team/TeamListScreen.kt` — screen to patch
- `viewmodel/src/commonMain/kotlin/.../viewmodel/TeamListViewModel.kt` — ViewModel to add `resetState()`
- `shared-ui/src/commonMain/kotlin/.../ui/club/ClubMembersScreen.kt` — same issue (president tab)
- `viewmodel/src/commonMain/kotlin/.../viewmodel/ClubMembersViewModel.kt` — ViewModel to add `resetState()`
- `shared-ui/src/commonMain/kotlin/.../ui/club/PresidentNotificationsScreen.kt` — same issue (president tab)
- `viewmodel/src/commonMain/kotlin/.../viewmodel/PresidentNotificationsViewModel.kt` — ViewModel to add `resetState()`
- `shared-ui/src/commonMain/kotlin/.../ui/club/ClubSettingsScreen.kt` — same issue (president tab)
- `viewmodel/src/commonMain/kotlin/.../viewmodel/ClubSettingsViewModel.kt` — ViewModel to add `resetState()`
- `shared-ui/src/commonMain/kotlin/.../ui/players/wizard/PlayerWizardScreen.kt` — reference for the established `LaunchedEffect(Unit) { vm.resetStep() }` pattern
- `shared-ui/src/commonMain/kotlin/.../ui/matches/wizard/MatchCreationWizardScreen.kt` — reference for `LaunchedEffect(Unit) { vm.resetForMatchId(matchId) }` pattern

## Architecture decisions
- **Reset = set uiState back to Loading and re-trigger data load**: The VMs load data via `init { loadXxx() }`. On iOS the cached VM never re-runs `init`, so `resetState()` must set `_uiState.value = UiState.Loading` and call `loadXxx()` again. This ensures fresh data from Firestore snapshot listeners.
- **No-op guard to avoid redundant reloads on Android**: Because Android VMs are scoped to `NavBackStackEntry` and do get destroyed/recreated, the `LaunchedEffect(Unit)` reset will fire on every composition entry on both platforms. To avoid a redundant reload on Android (where `init` already ran correctly), **do NOT add the guard**. The `LaunchedEffect(Unit)` + `resetState()` is harmless on Android: it just re-sets to Loading and re-launches the same Flow collection, which will immediately emit the current Firestore snapshot. The brief flash of Loading state is negligible and the simplicity outweighs adding platform checks.
- **Cancel previous load coroutines before re-launching**: Each `resetState()` must cancel any in-flight `loadXxx()` job before launching a new one, to avoid duplicate collectors on the same Firestore snapshot listener. Store the launch job in a `private var loadJob: Job?` field.
- **ClubSettingsViewModel is different**: Its `UiState` is a single data class (not sealed). Reset must set it back to `UiState(loading = true)` and clear `clubId`, `savedName`, `savedHomeGround`.

## Implementation steps

### 1. `TeamListViewModel.kt` — add `resetState()`
File: `viewmodel/src/commonMain/kotlin/com/jesuslcorominas/teamflowmanager/viewmodel/TeamListViewModel.kt`

- Add a `private var loadJob: Job?` field (initialized to `null`).
- In `init {}`, assign `loadJob = viewModelScope.launch { ... }` wrapping the existing `loadTeams()` call logic. Alternatively, keep `loadTeams()` but have it store its jobs.
- Refactor: extract the two `viewModelScope.launch` blocks inside `loadTeams()` into a single parent coroutine stored in `loadJob`.
- Add public method:
  ```kotlin
  fun resetState() {
      loadJob?.cancel()
      _uiState.value = UiState.Loading
      _searchQuery.value = ""
      _coachFilter.value = CoachFilter.ALL
      _assignCoachDialogTeam.value = null
      _assignCoachError.value = null
      _assigningCoachToTeamId.value = null
      _matchStatusByTeam.value = emptyMap()
      allTeamsCache.value = emptyList()
      loadTeams()
  }
  ```
- Update `loadTeams()` to store the launched coroutine in `loadJob`:
  ```kotlin
  private fun loadTeams() {
      loadJob = viewModelScope.launch {
          // ... existing body (both inner launches are children of this scope)
      }
  }
  ```
  Also move the match-status `viewModelScope.launch` block inside the same parent launch so it gets cancelled together.

### 2. `TeamListScreen.kt` — add `LaunchedEffect(Unit)`
File: `shared-ui/src/commonMain/kotlin/com/jesuslcorominas/teamflowmanager/ui/team/TeamListScreen.kt`

- Add immediately after `TrackScreenView(...)` (line 91), before `val uiState by ...`:
  ```kotlin
  LaunchedEffect(Unit) {
      viewModel.resetState()
  }
  ```
- No new imports needed (`LaunchedEffect` is already imported).

### 3. `ClubMembersViewModel.kt` — add `resetState()`
File: `viewmodel/src/commonMain/kotlin/com/jesuslcorominas/teamflowmanager/viewmodel/ClubMembersViewModel.kt`

- Add `private var loadJob: Job?` field.
- Update `loadMembers()` to store in `loadJob`.
- Add:
  ```kotlin
  fun resetState() {
      loadJob?.cancel()
      _uiState.value = UiState.Loading
      loadMembers()
  }
  ```

### 4. `ClubMembersScreen.kt` — add `LaunchedEffect(Unit)`
File: `shared-ui/src/commonMain/kotlin/com/jesuslcorominas/teamflowmanager/ui/club/ClubMembersScreen.kt`

- Add `LaunchedEffect(Unit) { viewModel.resetState() }` right after the ViewModel is obtained and before the first `collectAsState()` call.
- Add `import androidx.compose.runtime.LaunchedEffect` if not present.

### 5. `PresidentNotificationsViewModel.kt` — add `resetState()`
File: `viewmodel/src/commonMain/kotlin/com/jesuslcorominas/teamflowmanager/viewmodel/PresidentNotificationsViewModel.kt`

- Add `private var loadJob: Job?` field.
- Update `load()` to store in `loadJob`.
- Add:
  ```kotlin
  fun resetState() {
      loadJob?.cancel()
      _uiState.value = UiState.Loading
      _unreadCount.value = 0
      load()
  }
  ```

### 6. `PresidentNotificationsScreen.kt` — add `LaunchedEffect(Unit)`
File: `shared-ui/src/commonMain/kotlin/com/jesuslcorominas/teamflowmanager/ui/club/PresidentNotificationsScreen.kt`

- Add `LaunchedEffect(Unit) { viewModel.resetState() }` right after the ViewModel is obtained.
- Verify `LaunchedEffect` import exists (it does, line 27).

### 7. `ClubSettingsViewModel.kt` — add `resetState()`
File: `viewmodel/src/commonMain/kotlin/com/jesuslcorominas/teamflowmanager/viewmodel/ClubSettingsViewModel.kt`

- Add `private var loadJob: Job?` field.
- Update `loadClub()` to store in `loadJob`.
- Add:
  ```kotlin
  fun resetState() {
      loadJob?.cancel()
      clubId = null
      savedName = ""
      savedHomeGround = ""
      _uiState.value = UiState()
      loadClub()
  }
  ```

### 8. `ClubSettingsScreen.kt` — add `LaunchedEffect(Unit)`
File: `shared-ui/src/commonMain/kotlin/com/jesuslcorominas/teamflowmanager/ui/club/ClubSettingsScreen.kt`

- Add `LaunchedEffect(Unit) { viewModel.resetState() }` right after the ViewModel is obtained and before the first `collectAsState()` call.
- Verify `LaunchedEffect` import exists (it does, line 34).

## Source set rules
- All ViewModel changes go in `commonMain` (the VMs are already in `viewmodel/src/commonMain/`).
- All Screen changes go in `commonMain` (the screens are already in `shared-ui/src/commonMain/`).
- No platform-specific (iosMain/androidMain) code needed.

## Test coverage points

### Unit tests to add/update in `viewmodel/src/androidUnitTest/`:

1. **`TeamListViewModelTest`** (existing file):
   - Add test: `resetState sets uiState to Loading and reloads teams` -- call `resetState()`, assert `uiState.value == UiState.Loading`, then `advanceUntilIdle()` and verify teams are loaded again.
   - Add test: `resetState clears search query and coach filter` -- set search query and filter, call `resetState()`, assert both are reset to defaults.

2. **`ClubMembersViewModelTest`** (new or existing):
   - Test: `resetState sets uiState to Loading and reloads members`.

3. **`PresidentNotificationsViewModelTest`** (new or existing):
   - Test: `resetState sets uiState to Loading and reloads notifications`.
   - Test: `resetState resets unreadCount to 0`.

4. **`ClubSettingsViewModelTest`** (new or existing):
   - Test: `resetState resets to default UiState and reloads club`.

## Risks / Ambiguities

1. **Duplicate Flow collectors**: The biggest risk is launching a new `loadTeams()` without cancelling the previous one. The `loadJob?.cancel()` pattern is critical. Verify that cancelling the parent job also cancels child `launch {}` blocks inside `loadTeams()` -- it will, as long as the inner launches are structured (children of the same coroutine scope, not `viewModelScope.launch` siblings). This means `TeamListViewModel.loadTeams()` needs refactoring: currently it has two separate `viewModelScope.launch` blocks (one for teams+filters at line 111, one for match status at line 168). Both must become children of a single parent `loadJob`.

2. **Brief Loading flash on Android**: On Android, `LaunchedEffect(Unit)` fires after `init` has already loaded data. The reset will briefly show Loading before the Flow re-emits. This is acceptable -- the data re-emits almost immediately from the Firestore snapshot listener cache. If this becomes noticeable, a future optimization could use `expect`/`actual` to skip the reset on Android, but that is out of scope for this issue.

3. **ClubSettingsScreen has form state**: If the user is editing club settings and switches tabs then comes back, `resetState()` will discard unsaved edits. This is the correct behavior -- the edit mode flag (`isEditing`) is reset to `false`, matching a fresh screen entry.

4. **Coach-mode tabs (Matches, Players, Analysis, Team)**: These are the bottom nav tabs for coach users. They may have the same caching issue but are NOT in scope for this issue (#371 is specifically about president screens). File a separate issue if needed.