# Spec: Centralized iOS safe area + back button fix (no Android duplication)

## Task summary
Remove per-screen `Scaffold + TopAppBar` workarounds from `MatchCreationWizardScreen`, `PlayerWizardScreen`, and `TeamScreen` (NoTeam branch), and instead fix the root cause in `MainScreen` (status bar inset) plus add an `expect/actual` `IosBackButton` composable that only renders on iOS.

## Files to read (before implementing)
- `shared-ui/src/commonMain/kotlin/.../ui/main/MainScreen.kt` — iOS shell Scaffold; fix status bar padding here
- `shared-ui/src/commonMain/kotlin/.../ui/matches/wizard/MatchCreationWizardScreen.kt` — remove Scaffold+TopAppBar wrapper
- `shared-ui/src/commonMain/kotlin/.../ui/players/wizard/PlayerWizardScreen.kt` — remove Scaffold+TopAppBar wrapper
- `shared-ui/src/commonMain/kotlin/.../ui/team/TeamScreen.kt` — remove Scaffold+TopAppBar in NoTeam/create branch
- `shared-ui/src/commonMain/kotlin/.../ui/components/AppBackHandler.kt` — expect/actual pattern reference
- `shared-ui/src/iosMain/kotlin/.../ui/components/AppBackHandler.kt` — iosMain actual reference
- `shared-ui/src/androidMain/kotlin/.../ui/components/AppBackHandler.kt` — androidMain actual reference
- `shared-ui/src/commonMain/kotlin/.../ui/components/BackPressController.kt` — BackPressController API
- `shared-ui/src/commonMain/kotlin/.../ui/navigation/Route.kt` — which routes have `showTopBar = false`

## Architecture decisions
- **Decision 1**: Fix safe area in `MainScreen` only — when `showTopBar != true`, compute top padding from `WindowInsets.safeDrawing` (top) instead of `paddingValues.calculateTopPadding()`. This is the single place that controls iOS content offset. Android does not use `MainScreen` so there is zero Android risk.
- **Decision 2**: Create `expect/actual` `IosBackButton` composable — `iosMain` renders an `IconButton` with back arrow; `androidMain` renders nothing. This avoids duplicating TopAppBars that Android's shell already provides.
- **Decision 3**: Each affected screen wraps its content in a `Box` and places `IosBackButton` as an overlay at `TopStart` with appropriate padding. The `IosBackButton` composable handles its own positioning internally (small left/top padding).
- **Decision 4**: Follow the exact `expect/actual` pattern of `AppBackHandler` — top-level `@Composable expect fun` in commonMain, `@Composable actual fun` in androidMain and iosMain.

## Implementation steps

### Step 1: Fix `MainScreen` safe area padding
`shared-ui/src/commonMain/kotlin/.../ui/main/MainScreen.kt`

Replace line 144:
```kotlin
content(PaddingValues(top = paddingValues.calculateTopPadding()))
```
with:
```kotlin
val topPadding = if (uiConfig?.showTopBar == true) {
    paddingValues.calculateTopPadding()
} else {
    WindowInsets.safeDrawing.asPaddingValues().calculateTopPadding()
}
content(PaddingValues(top = topPadding))
```

Add imports:
```kotlin
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.asPaddingValues
```
Note: `WindowInsets` is already imported (line 6). Only `safeDrawing` and `asPaddingValues` need adding.

### Step 2: Create `IosBackButton` expect declaration
`shared-ui/src/commonMain/kotlin/com/jesuslcorominas/teamflowmanager/ui/components/IosBackButton.kt`

```kotlin
package com.jesuslcorominas.teamflowmanager.ui.components

import androidx.compose.runtime.Composable

@Composable
expect fun IosBackButton(onBack: () -> Unit)
```

### Step 3: Create `IosBackButton` androidMain actual (no-op)
`shared-ui/src/androidMain/kotlin/com/jesuslcorominas/teamflowmanager/ui/components/IosBackButton.kt`

```kotlin
package com.jesuslcorominas.teamflowmanager.ui.components

import androidx.compose.runtime.Composable

@Composable
actual fun IosBackButton(onBack: () -> Unit) {
    // No-op on Android — the Android shell provides its own back navigation.
}
```

### Step 4: Create `IosBackButton` iosMain actual (renders back arrow)
`shared-ui/src/iosMain/kotlin/com/jesuslcorominas/teamflowmanager/ui/components/IosBackButton.kt`

```kotlin
package com.jesuslcorominas.teamflowmanager.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
actual fun IosBackButton(onBack: () -> Unit) {
    IconButton(
        onClick = onBack,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp),
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = null,
        )
    }
}
```

### Step 5: Revert `MatchCreationWizardScreen` — remove Scaffold+TopAppBar, add IosBackButton
`shared-ui/src/commonMain/kotlin/.../ui/matches/wizard/MatchCreationWizardScreen.kt`

**Remove** the entire `Scaffold(topBar = { TopAppBar(...) }) { paddingValues -> ... }` wrapper (lines 77-102 + closing brace at 206).

**Replace** with a `Box` containing the original `Column` content plus `IosBackButton` overlay:

```kotlin
Box(modifier = Modifier.fillMaxSize()) {
    when (val state = uiState) {
        is MatchCreationWizardUiState.Loading -> Loading()
        is MatchCreationWizardUiState.Saving -> Loading()
        is MatchCreationWizardUiState.Ready -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = TFMSpacing.spacing02)
            ) {
                // ... wizard steps remain exactly as they are (lines 108-202)
            }
        }
    }

    IosBackButton(onBack = { wizardViewModel.requestBack(onNavigateBack) })
}
```

**Remove** unused imports: `Scaffold`, `TopAppBar`, `ExperimentalMaterial3Api`, `Text`, `Icons`, `Icon`, `IconButton`, and the string resources `add_match_title`, `edit_match_title`.

**Add** import: `com.jesuslcorominas.teamflowmanager.ui.components.IosBackButton`, `androidx.compose.foundation.layout.Box`.

### Step 6: Revert `PlayerWizardScreen` — remove Scaffold+TopAppBar, add IosBackButton
`shared-ui/src/commonMain/kotlin/.../ui/players/wizard/PlayerWizardScreen.kt`

**Remove** the `Scaffold(topBar = { TopAppBar(...) }) { paddingValues -> ... }` wrapper (lines 66-91 + closing brace at 179).

**Replace** with a `Box` containing the original content plus `IosBackButton` overlay:

```kotlin
Box(modifier = Modifier.fillMaxSize()) {
    when (uiState) {
        is PlayerWizardUiState.Loading -> Loading()
        is PlayerWizardUiState.Error -> {
            LaunchedEffect(Unit) { onNavigateBack() }
        }
        is PlayerWizardUiState.Ready -> {
            Column(modifier = Modifier.fillMaxSize()) {
                // ... steps remain exactly as they are (lines 99-130)
            }

            // captain confirmation dialogs remain (lines 133-165)
        }
    }

    IosBackButton(onBack = { wizardViewModel.requestBack(onNavigateBack) })
}
```

**Remove** unused imports: `Scaffold`, `TopAppBar`, `ExperimentalMaterial3Api`, `Text`, `Icons`, `Icon`, `IconButton`, and the string resources `add_player_title`, `edit_player_title`.

**Add** import: `com.jesuslcorominas.teamflowmanager.ui.components.IosBackButton`, `androidx.compose.foundation.layout.Box`.

### Step 7: Revert `TeamScreen` NoTeam branch — remove Scaffold+TopAppBar, add IosBackButton
`shared-ui/src/commonMain/kotlin/.../ui/team/TeamScreen.kt`

In the `TeamUiState.NoTeam` branch, **only in the `else` block** (lines 101-140), remove the `Scaffold(topBar = { TopAppBar(...) }) { paddingValues -> Surface(...) { ... } }` wrapper.

**Replace** with:

```kotlin
Box(modifier = Modifier.fillMaxSize()) {
    TeamForm(
        clubNumericId = state.clubNumericId,
        clubId = state.clubId,
        isPresident = state.isPresident,
        onSave = { team, _ ->
            viewModel.createTeam(team) {
                if (state.isPresident && onNavigateToTeamList != null) {
                    onNavigateToTeamList()
                } else {
                    onNavigateToMatches(team.name)
                }
            }
        },
    )

    IosBackButton(onBack = { viewModel.requestBack(onNavigateBackRequest) })
}
```

**Remove** unused imports (only if no longer needed elsewhere in the file): `Scaffold`, `TopAppBar`, `Icons`, `Icon`, `IconButton`, `ExperimentalMaterial3Api`. Check: `ExperimentalMaterial3Api` is used on the function itself at line 46 — keep it only if still needed. `Icons`, `Icon`, `IconButton` — check if used elsewhere; if not, remove. `Scaffold`, `TopAppBar` — check if used elsewhere; if not, remove. The `create_team_title` string resource — remove if no longer used.

**Add** import: `com.jesuslcorominas.teamflowmanager.ui.components.IosBackButton`, `androidx.compose.foundation.layout.Box`.

Note: The `Surface` wrapper that was inside the Scaffold is no longer needed because the parent `Surface` at line 66 already provides the background.

## Source set rules
- **commonMain**: `expect fun IosBackButton(onBack: () -> Unit)` declaration + all screen modifications (screens are in commonMain)
- **androidMain**: `actual fun IosBackButton(...)` — empty composable (no-op)
- **iosMain**: `actual fun IosBackButton(...)` — renders `IconButton` with back arrow icon
- **Expected/actual needed?** Yes — the back button must render only on iOS. Android has its own shell with a back button.

## Repository / DataSource rules
- No repository or data source changes needed. This is purely a UI/composable refactor.

## DI wiring
- No DI changes needed. `IosBackButton` is a stateless composable with no injected dependencies.

## Test coverage points
- **Visual regression on iOS**: verify that screens with `showTopBar = false` (CreateMatch, PlayerWizard, Team/create) show content below the Dynamic Island, not behind it.
- **Visual regression on Android**: verify that the same screens do NOT show a duplicate top bar.
- **Back button on iOS**: verify that `IosBackButton` appears on MatchCreationWizardScreen, PlayerWizardScreen, and TeamScreen (create mode), and that tapping it triggers the unsaved-changes dialog when applicable.
- **Back button on Android**: verify that `IosBackButton` renders nothing (no extra icon/button visible).

## Risks / Ambiguities
1. **`WindowInsets.safeDrawing` availability**: Compose Multiplatform 1.7.3 supports `WindowInsets.safeDrawing` in commonMain. If for any reason it is not available, fall back to `WindowInsets.statusBars`. Verify by compiling `iosSimulatorArm64`.
2. **Screens where `showTopBar = false` but no back button is needed**: `Splash`, `Login`, `Migration`, `ClubSelection`, `CreateClub`, `JoinClub`, `AcceptTeamInvitation`, `PendingTeamAssignment` all have `showTopBar = false`. These screens do NOT add `IosBackButton` — only the three screens listed above do. The `MainScreen` safe area fix (Step 1) benefits all of them automatically.
3. **`IosBackButton` positioning**: The button renders at `TopStart` of the enclosing `Box`. Since `MainScreen` already applies status bar top padding to the content, the button will sit just below the safe area. If it overlaps content on certain screens, adjust the top padding inside the iosMain actual.
4. **Import cleanup**: After removing `Scaffold`/`TopAppBar` from each screen, verify no compilation errors from leftover/missing imports. Run `./gradlew ktlintCheck` after changes.
5. **`TeamScreen` `ExperimentalMaterial3Api` annotation**: The `@OptIn(ExperimentalMaterial3Api::class)` on `TeamScreen` (line 46) may still be needed if `AlertDialog` or other Material3 APIs require it. Check compilation. If no longer needed, remove it.
