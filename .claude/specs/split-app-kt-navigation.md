# Spec: refactor: split App.kt into focused navigation components (IosNavHost, extensions)

## Task summary
Split `App.kt` (321 lines, 6 responsibilities) into three focused files so that each has a single responsibility, with zero behavior changes.

## Files to read (before implementing)
- `iosApp/src/commonMain/kotlin/com/jesuslcorominas/teamflowmanager/ui/App.kt` — the file being split (all current code lives here)
- `iosApp/src/commonMain/kotlin/com/jesuslcorominas/teamflowmanager/IosNavController.kt` — already separated, no changes needed; read to confirm `IosDestination` sealed class and `IosNavController` API
- `shared-ui/src/commonMain/kotlin/com/jesuslcorominas/teamflowmanager/ui/navigation/Route.kt` — Route constants used by `toRouteString()` and `navigateToBottomNav()`

## Architecture decisions
- **Keep all three files in `iosApp/src/commonMain`**: The code is iOS-only CMP navigation; it stays in the `iosApp` module, not `shared-ui`.
- **Package placement**: `IosNavHost.kt` and `IosNavigationExtensions.kt` go in `com.jesuslcorominas.teamflowmanager.ui` (same package as `App.kt`) so existing `internal`/`private` composables become `internal` without needing `public` exposure.
- **Visibility**: `IosNavHost` composable becomes `internal`. Extension functions in `IosNavigationExtensions.kt` become `internal`. `App` remains the only public composable (called from `MainViewController.kt`).
- **No new classes or abstractions**: This is a pure file-level extraction; no new interfaces, sealed classes, or state holders.

## Implementation steps

1. **`iosApp/src/commonMain/kotlin/com/jesuslcorominas/teamflowmanager/ui/IosNavHost.kt`** (new file)
   - Extract the entire `when (val dest = navController.current) { ... }` block from `App.kt` into a new composable:
     ```kotlin
     @Composable
     internal fun IosNavHost(
         navController: IosNavController,
         onSignInWithGoogle: suspend () -> String,
         onShareFile: (String) -> Unit,
     )
     ```
   - This composable owns the two `remember { mutableStateOf(null) }` for `matchTitle` and `presidentMatchTitle` (they are navigation-level state, not app-level).
   - Contains both the full-screen branch and the MainScreen-wrapped `else` branch.
   - Move all screen-specific imports here.

2. **`iosApp/src/commonMain/kotlin/com/jesuslcorominas/teamflowmanager/ui/IosNavigationExtensions.kt`** (new file)
   - Move `IosDestination.toRouteString(): String` extension (lines 288-304). Change from `private` to `internal`.
   - Move `IosNavController.navigateToBottomNav(route: String)` extension (lines 306-320). Change from `private` to `internal`.
   - Move `PresidentMatchDetailScaffold` composable (lines 258-286). Change from `private` to `internal`.
   - Imports needed: `Route`, `IosDestination`, `IosNavController`, Compose/Material3 for Scaffold/TopAppBar.

3. **`iosApp/src/commonMain/kotlin/com/jesuslcorominas/teamflowmanager/ui/App.kt`** (modify)
   - Reduce to entry-point only (~15-20 lines):
     ```kotlin
     @Composable
     fun App(
         onSignInWithGoogle: suspend () -> String = { throw NotImplementedError("KMP-17") },
         onShareFile: (String) -> Unit = {},
     ) {
         MaterialTheme(colorScheme = LightColorScheme) {
             val navController = remember { IosNavController() }
             IosNavHost(
                 navController = navController,
                 onSignInWithGoogle = onSignInWithGoogle,
                 onShareFile = onShareFile,
             )
         }
     }
     ```
   - Remove all screen imports, Route imports, and navigation logic.

## Source set rules
- All three files live in `iosApp/src/commonMain/kotlin/com/jesuslcorominas/teamflowmanager/ui/`.
- No `androidMain` or `iosMain` source sets affected.
- No expect/actual needed — No.

## Repository / DataSource rules
- N/A

## DI wiring
- N/A (no DI changes; all Koin injection happens inside individual screen composables via `koinViewModel()`).

## Test coverage points
- **Compilation test**: `./gradlew :iosApp:compileKotlinIosSimulatorArm64 --no-daemon --stacktrace` must pass.
- **Android build unaffected**: `./gradlew :app:assembleDevDebug --no-daemon` (App.kt is iosApp-only, but verify no accidental cross-module breakage).
- **ktlint**: `./gradlew ktlintCheck --no-daemon --stacktrace` must pass.
- No runtime behavior tests needed (pure refactor, no logic changes).

## Risks / Ambiguities
- **Visibility escalation**: The three extracted symbols (`toRouteString`, `navigateToBottomNav`, `PresidentMatchDetailScaffold`) change from `private` to `internal`. This is acceptable since they remain module-internal to `iosApp` and are not exposed outside the module.
- **State hoisting of `matchTitle` / `presidentMatchTitle`**: These two `mutableStateOf` variables must move into `IosNavHost`, NOT stay in `App`. They are consumed only within the navigation host and should not leak to the entry point.
- **Import cleanup**: After extraction, `App.kt` should retain only `MaterialTheme`, `remember`, `Composable`, `IosNavController`, `LightColorScheme`, and the new `IosNavHost` import. Verify no unused imports remain in any of the three files.