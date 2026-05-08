# Spec: Fix iOS layout broken on TeamScreen create mode (Dynamic Island / safe area)

## Task summary
When `TeamScreen` renders in `MODE_CREATE` (the `NoTeam` state), the navigation shell provides no top bar and no bottom bar (`showTopBar = false`, `showBottomBar = false`). The screen uses a bare `Surface` so content renders under the Dynamic Island on iPhone. Add a `Scaffold` with `TopAppBar` (back button + "Create Team" title) wrapping the create-mode `TeamForm` inside `TeamScreen.kt`.

## Files to read (before implementing)
- `shared-ui/src/commonMain/kotlin/.../ui/team/TeamScreen.kt` — the file to modify
- `shared-ui/src/commonMain/kotlin/.../ui/team/components/TeamForm.kt` — must NOT be changed (used in edit mode too)
- `shared-ui/src/commonMain/kotlin/.../ui/navigation/Route.kt` — `Route.Team.uiConfig` confirms `showTopBar = false` for `MODE_CREATE`
- `shared-ui/src/commonMain/kotlin/.../ui/club/PresidentTeamDetailScreen.kt` — reference for inline `TopAppBar` + back button pattern
- `shared-ui/src/commonMain/composeResources/values/strings.xml` — existing `create_team_title` string (line 136)
- `.claude/specs/fix-ios-wizard-safe-area.md` — reference spec for same pattern applied to wizard screens

## Architecture decisions
- **Wrap only the create-mode `TeamForm` (inside `NoTeam` branch) with `Scaffold + TopAppBar`** — The edit-mode `TeamForm` already has the shell top bar (`showTopBar = true` when `mode == MODE_EDIT`). The view-mode `TeamDetailContent` also has the shell top bar. Only the `NoTeam` → non-president branch (which renders `TeamForm` for creation) lacks a top bar.
- **Use inline `TopAppBar` inside `TeamScreen`, NOT `AppTopBar`** — `AppTopBar` is coupled to `Route.UiConfig` and the main navigation shell. Following the `PresidentTeamDetailScreen` pattern (inline `TopAppBar` with back arrow) is simpler and consistent with the wizard fix spec.
- **No `safeDrawingPadding()` modifier needed** — Adding a Material3 `TopAppBar` inside the Scaffold's `topBar` slot is sufficient. Scaffold handles WindowInsets automatically. This is the idiomatic CMP/M3 approach.
- **Reuse existing `create_team_title` string** — Already present in both `values/strings.xml` ("Create Team") and `values-es/strings.xml` ("Crear Equipo"). No new strings needed.
- **Back button calls `onNavigateBackRequest`** — Same callback already used in the permission-error dialog's close button. Consistent behavior.
- **`TeamForm` must NOT be modified** — It is shared between create mode and edit mode. The TopAppBar and safe area handling must be done at the `TeamScreen` level only.

## Implementation steps

### 1. Modify `TeamScreen.kt` — wrap create-mode TeamForm with Scaffold + TopAppBar

File: `shared-ui/src/commonMain/kotlin/com/jesuslcorominas/teamflowmanager/ui/team/TeamScreen.kt`

**Add imports:**
```kotlin
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
```

**Add string resource import:**
```kotlin
import teamflowmanager.shared_ui.generated.resources.create_team_title
```

**Add `@OptIn(ExperimentalMaterial3Api::class)` to the `TeamScreen` function.**

**Replace the `NoTeam` → non-president `else` branch** (lines 91-104). Currently:
```kotlin
} else {
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
}
```

Replace with:
```kotlin
} else {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(Res.string.create_team_title))
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.requestBack(onNavigateBackRequest) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background,
        ) {
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
        }
    }
}
```

**Add `padding` import** (if not already present):
```kotlin
import androidx.compose.foundation.layout.padding
```

**Note on the outer `Surface`**: The outer `Surface` at line 56-58 wraps the entire `when` block. The new inner `Scaffold` will sit inside that outer `Surface`. The inner `Surface` with `padding(paddingValues)` ensures the `TeamForm` content starts below the TopAppBar. This double-Surface is harmless (same background color) and avoids restructuring the entire `when` block.

### 2. No changes to `TeamForm.kt`

`TeamForm.kt` must remain untouched. It still renders `TeamFlowManagerIcon()` + `create_team_title` text at the top of the `LazyColumn` when `team == null`. This is acceptable — the title will appear both in the TopAppBar and in the form body. If the team later wants to remove the duplicate title from `TeamForm`, that is a separate follow-up task.

### 3. No changes to `Route.kt`

The `Route.Team.uiConfig` correctly returns `showTopBar = false` for `MODE_CREATE`. The new inline `Scaffold + TopAppBar` inside `TeamScreen` handles the safe area independently of the navigation shell. No route changes needed.

## Source set rules
- All changes in `commonMain` — no platform-specific code needed.
- Expected/actual needed? **No** — `TopAppBar`, `Scaffold`, `Icons.AutoMirrored.Filled.ArrowBack` are fully available in CMP Material3.

## Repository / DataSource rules
- No repository or datasource changes needed. This is a pure UI fix.

## DI wiring
- No DI changes needed.

## Test coverage points
- **Visual regression on iOS**: verify TopAppBar renders above Dynamic Island safe area on iPhone 17 Pro (or any device with notch/island).
- **Back navigation**: TopAppBar back arrow calls `onNavigateBackRequest()` and navigates back correctly.
- **Title correctness**: TopAppBar shows "Create Team" / "Crear Equipo".
- **Edit mode unaffected**: when `mode == MODE_EDIT`, the shell top bar is shown (not the inline one). Verify the edit flow is unchanged.
- **View mode unaffected**: when `mode == MODE_VIEW`, `TeamDetailContent` renders with the shell top bar as before.
- **Android regression**: verify the create-team flow on Android still works (TopAppBar appears, layout is correct).

## Risks / Ambiguities
- **Duplicate title**: The `TeamForm` renders its own "Create Team" title + icon when `team == null`. The new `TopAppBar` also shows "Create Team". This means the title appears twice. Removing the duplicate from `TeamForm` would require modifying `TeamForm` which is shared with edit mode (where `team != null` so the icon/title block is not shown anyway). If removing the duplicate is desired, `TeamForm` could accept an optional `showHeader: Boolean = true` parameter — but that is a separate follow-up to keep this fix minimal.
- **`@OptIn(ExperimentalMaterial3Api::class)`**: `TopAppBar` is still marked experimental in M3. The annotation is already used in other screens (`PresidentTeamDetailScreen`, `MatchCreationWizardScreen`, `PlayerWizardScreen`). Safe to use.
- **`AppBackHandler` still active**: The existing `AppBackHandler` on line 52 calls `viewModel.requestBack(onNavigateBackRequest)` which shows the unsaved-changes dialog. The new TopAppBar back button calls `onNavigateBackRequest()` directly (no unsaved-changes check). This is intentional for consistency with how the permission-error dialog's close button works. However, if the team prefers the back button to also check for unsaved changes, change the onClick to `{ viewModel.requestBack(onNavigateBackRequest) }` instead. The implementer should decide based on the fact that in create mode, the `TeamForm` starts empty so there are typically no "unsaved changes" to warn about — but if the user has typed something, `requestBack` would be safer. **Recommendation: use `viewModel.requestBack(onNavigateBackRequest)` for the back button onClick to be consistent with the existing `AppBackHandler` behavior.**
