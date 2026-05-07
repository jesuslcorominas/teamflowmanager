# Spec: Fix iOS layout broken on Dynamic Island (iPhone 17 Pro)

## Task summary
Wizard screens (MatchCreationWizard, PlayerWizard) render content under the Dynamic Island / status bar because they use `Scaffold` without a `topBar` and rely solely on `paddingValues` from Scaffold, which does not account for safe area insets on iOS. Add a `TopAppBar` with back navigation to both wizard screens.

## Files to read (before implementing)
- `shared-ui/src/commonMain/kotlin/.../ui/matches/wizard/MatchCreationWizardScreen.kt` — current Scaffold without topBar
- `shared-ui/src/commonMain/kotlin/.../ui/players/wizard/PlayerWizardScreen.kt` — same issue
- `shared-ui/src/commonMain/kotlin/.../ui/components/topbar/AppTopBar.kt` — existing reusable TopAppBar (uses Route.UiConfig, too coupled for wizard use)
- `shared-ui/src/commonMain/kotlin/.../ui/club/PresidentTeamDetailScreen.kt` — reference for inline TopAppBar + back button pattern
- `shared-ui/src/commonMain/composeResources/values/strings.xml` — existing strings: `add_match_title`, `edit_player_title`, `add_player_title`
- `shared-ui/src/commonMain/composeResources/values-es/strings.xml` — Spanish translations

## Architecture decisions
- **Use inline `TopAppBar` inside each wizard Scaffold, NOT `AppTopBar`** — `AppTopBar` is tightly coupled to `Route.UiConfig` and the main navigation shell. Wizard screens already have `showTopBar = false` in their `Route` definition. Following the `PresidentTeamDetailScreen` pattern (inline `TopAppBar` with back arrow) is simpler and consistent.
- **No `safeDrawingPadding()` modifier needed** — Adding a Material3 `TopAppBar` inside the Scaffold's `topBar` slot is sufficient. Scaffold already handles WindowInsets when it has a topBar. This is the idiomatic CMP/M3 approach.
- **Add `edit_match_title` string resource** — Currently missing. Needed to differentiate "Register Match" (create) from "Edit Match" (edit) in the TopAppBar title.
- **Cancel button in GeneralDataStep becomes redundant but keep it** — The TopAppBar back arrow triggers `onNavigateBack` (with unsaved-changes dialog). The Cancel button in GeneralDataStep still calls `wizardViewModel.requestBack(onNavigateBack)` which shows the same dialog. Both can coexist; no removal needed.

## Implementation steps

### 1. Add missing string resource `edit_match_title`

1. `shared-ui/src/commonMain/composeResources/values/strings.xml` — Add after `add_match_title` (line 168):
   ```xml
   <string name="edit_match_title">Edit Match</string>
   ```

2. `shared-ui/src/commonMain/composeResources/values-es/strings.xml` — Add after `add_match_title` (line 168):
   ```xml
   <string name="edit_match_title">Editar Partido</string>
   ```

### 2. Add TopAppBar to `MatchCreationWizardScreen`

File: `shared-ui/src/commonMain/kotlin/com/jesuslcorominas/teamflowmanager/ui/matches/wizard/MatchCreationWizardScreen.kt`

Add imports:
```kotlin
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
```

Add string resource imports:
```kotlin
import teamflowmanager.shared_ui.generated.resources.add_match_title
import teamflowmanager.shared_ui.generated.resources.edit_match_title
```

Change `Scaffold { paddingValues ->` (line 68) to:
```kotlin
@OptIn(ExperimentalMaterial3Api::class)
Scaffold(
    topBar = {
        TopAppBar(
            title = {
                Text(
                    stringResource(
                        if (matchId != 0L) Res.string.edit_match_title
                        else Res.string.add_match_title
                    )
                )
            },
            navigationIcon = {
                IconButton(onClick = { wizardViewModel.requestBack(onNavigateBack) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                    )
                }
            },
        )
    },
) { paddingValues ->
```

No other changes needed inside the Scaffold body — it already uses `Modifier.padding(paddingValues)`.

### 3. Add TopAppBar to `PlayerWizardScreen`

File: `shared-ui/src/commonMain/kotlin/com/jesuslcorominas/teamflowmanager/ui/players/wizard/PlayerWizardScreen.kt`

Add imports:
```kotlin
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
```

Add string resource imports:
```kotlin
import teamflowmanager.shared_ui.generated.resources.add_player_title
import teamflowmanager.shared_ui.generated.resources.edit_player_title
```

Change `Scaffold { paddingValues ->` (line 57) to:
```kotlin
@OptIn(ExperimentalMaterial3Api::class)
Scaffold(
    topBar = {
        TopAppBar(
            title = {
                Text(
                    stringResource(
                        if (playerId != 0L) Res.string.edit_player_title
                        else Res.string.add_player_title
                    )
                )
            },
            navigationIcon = {
                IconButton(onClick = { wizardViewModel.requestBack(onNavigateBack) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                    )
                }
            },
        )
    },
) { paddingValues ->
```

No other changes needed inside the Scaffold body — it already uses `Modifier.padding(paddingValues)`.

## Source set rules
- All changes in `commonMain` — no platform-specific code needed.
- Expected/actual needed? **No** — `TopAppBar` and `Scaffold` are fully available in CMP Material3.

## Repository / DataSource rules
- No repository or datasource changes needed. This is a pure UI fix.

## DI wiring
- No DI changes needed.

## Test coverage points
- **Visual regression on iOS**: verify TopAppBar renders above Dynamic Island safe area on iPhone 17 Pro / any device with notch/island.
- **Back navigation**: TopAppBar back arrow triggers unsaved-changes dialog when wizard has changes, navigates back directly when no changes.
- **Title correctness**: "Register Match" / "Add Player" for new wizards, "Edit Match" / "Edit Player" for editing existing entries.
- **Android regression**: verify existing wizard behavior on Android is not broken (TopAppBar should appear but layout should remain functional).

## Risks / Ambiguities
- **Cancel button duplication**: `GeneralDataStep` has a Cancel button that also triggers `requestBack`. With the new TopAppBar back arrow, there are two ways to go back from step 1. This is intentional — the Cancel button is part of the step's button row (Cancel/Next) and removing it would break the visual symmetry. If the team prefers removing it, that is a separate follow-up.
- **`@OptIn(ExperimentalMaterial3Api::class)`**: `TopAppBar` is still marked experimental in M3. The annotation is already used in other screens (`PresidentTeamDetailScreen`, `GeneralDataStep`). If it stabilizes in a future CMP release the annotation can be removed.
- **Step files unchanged**: The step composables (`GeneralDataStep`, `SquadCallUpStep`, `CaptainSelectionStep`, `StartingLineupStep`, `PlayerDataStep`, `PlayerPositionsStep`) receive a `modifier` from the parent and do not need changes. The safe area is resolved at the Scaffold level.