# Spec: Reuse original Android app icons in shared-ui

## Task summary
Migrate custom vector drawable icons from `app/src/main/res/drawable/` to CMP compose resources in `shared-ui/src/commonMain/composeResources/drawable/`, replace Material icon fallbacks in shared-ui screens with `painterResource(Res.drawable.*)`, and remove the `TeamFlowManagerIcon` expect/actual pattern now that `ic_launcher` already exists in CMP resources.

## Files to read (before implementing)
- `app/src/main/res/drawable/ic_goal.xml` — custom goal icon (32dp, corner flag design), NOT used anywhere currently
- `app/src/main/res/drawable/ic_google.xml` — Google logo (multi-color), NOT referenced in shared-ui (login button has no icon currently)
- `app/src/main/res/drawable/ic_launcher.xml` — app logo, already in CMP resources
- `app/src/main/res/drawable/ic_launcher_black_and_white.xml` — monochrome app logo, used only in Android notification services
- `app/src/main/res/drawable/ic_pause.xml` — standard Material Pause icon (white fill), used in PDF exporter
- `app/src/main/res/drawable/ic_people.xml` — standard Material People icon (white fill), used in PDF exporter
- `app/src/main/res/drawable/ic_sports_soccer.xml` — standard Material SportsSoccer icon (white fill), used in PDF exporter
- `app/src/main/res/drawable/ic_swap_horiz.xml` — standard Material SwapHoriz icon (white fill), used in PDF exporter
- `app/src/main/res/drawable/ic_timeout.xml` — custom timeout icon (512dp, whistle-with-timer design), NOT referenced in shared-ui
- `app/src/main/res/drawable/ic_timer.xml` — standard Material Timer icon (white fill), used in PDF exporter
- `app/src/main/res/drawable/ic_whistle.xml` — custom whistle icon (512dp, detailed whistle design), NOT referenced in shared-ui
- `shared-ui/src/commonMain/composeResources/drawable/ic_launcher.xml` — already migrated
- `shared-ui/src/commonMain/kotlin/.../ui/TeamFlowManagerIcon.kt` — expect declaration
- `shared-ui/src/androidMain/kotlin/.../ui/TeamFlowManagerIcon.kt` — actual using `R.drawable.ic_launcher`
- `shared-ui/src/iosMain/kotlin/.../ui/TeamFlowManagerIcon.kt` — actual (check implementation)
- `shared-ui/src/commonMain/kotlin/.../ui/matches/components/TimelineContent.kt` — uses `Icons.Default.{People,SportsSoccer,SwapHoriz,Timer,Pause}`
- `shared-ui/src/commonMain/kotlin/.../ui/matches/MatchScreen.kt` — uses `Icons.Default.{SportsSoccer,Timer,TimerOff,PlayArrow,Pause,Stop}`
- `shared-ui/src/commonMain/kotlin/.../ui/navigation/BottomNavigationBar.kt` — uses `Icons.Default.SportsSoccer`
- `shared-ui/src/commonMain/kotlin/.../ui/login/LoginScreen.kt` — Google sign-in button has NO icon, only text
- `app/src/main/java/.../ui/util/MatchReportPdfExporterImpl.kt` — uses `R.drawable.{ic_people,ic_sports_soccer,ic_swap_horiz,ic_timer,ic_pause}` for PDF rendering
- `app/src/main/java/.../service/MatchNotificationManager.kt` — uses `R.drawable.ic_launcher_black_and_white`
- `app/src/main/java/.../service/TeamFlowFirebaseMessagingService.kt` — uses `R.drawable.ic_launcher_black_and_white`

## Architecture decisions
- **Decision 1**: Migrate only truly custom icons (ic_goal, ic_whistle, ic_timeout, ic_google) to CMP resources — the others (ic_pause, ic_people, ic_sports_soccer, ic_swap_horiz, ic_timer) are exact copies of Material icons with white fill and provide no visual benefit over `Icons.Default.*`
- **Decision 2**: Add `ic_google.xml` to CMP resources and use it in LoginScreen — the Google logo has brand-specific multi-color paths that `Icons.Default.*` cannot reproduce
- **Decision 3**: Add `ic_whistle.xml` and `ic_timeout.xml` to CMP resources for potential future use in MatchScreen timeout/whistle buttons — these are unique custom icons with no Material equivalent
- **Decision 4**: Add `ic_goal.xml` to CMP resources — it is a unique corner-flag goal icon with no Material equivalent, suitable for replacing `Icons.Default.SportsSoccer` in goal-related contexts where differentiation matters
- **Decision 5**: Eliminate `TeamFlowManagerIcon` expect/actual — `ic_launcher.xml` is already in CMP resources, so all call sites can use `painterResource(Res.drawable.ic_launcher)` directly from commonMain
- **Decision 6**: Keep `ic_launcher_black_and_white.xml`, `ic_pause.xml`, `ic_people.xml`, `ic_sports_soccer.xml`, `ic_swap_horiz.xml`, `ic_timer.xml` in `app/src/main/res/drawable/` — they are used by Android-only code (notification services, PDF exporter) that cannot use CMP resources
- **Decision 7**: Do NOT change `EventCard` signature from `ImageVector` to `Painter` — this would be a large refactor. Instead, only replace icons in places where a custom drawable adds clear visual value (e.g., Google logo on login button)

## Implementation steps

### Step 1: Copy custom icons to CMP resources
1. `shared-ui/src/commonMain/composeResources/drawable/ic_google.xml` — copy from `app/src/main/res/drawable/ic_google.xml` (no changes needed, CMP 1.7.3 supports Android XML vector format)
2. `shared-ui/src/commonMain/composeResources/drawable/ic_goal.xml` — copy from `app/src/main/res/drawable/ic_goal.xml`
3. `shared-ui/src/commonMain/composeResources/drawable/ic_whistle.xml` — copy from `app/src/main/res/drawable/ic_whistle.xml`
4. `shared-ui/src/commonMain/composeResources/drawable/ic_timeout.xml` — copy from `app/src/main/res/drawable/ic_timeout.xml`

### Step 2: Add Google icon to LoginScreen
1. `shared-ui/src/commonMain/kotlin/com/jesuslcorominas/teamflowmanager/ui/login/LoginScreen.kt` — inside the `OutlinedButton` content (the `else` branch at line 162), add before the `Text`:
   ```kotlin
   Image(
       painter = painterResource(Res.drawable.ic_google),
       contentDescription = null,
       modifier = Modifier.size(20.dp),
   )
   Spacer(modifier = Modifier.width(8.dp))
   ```
   Add imports: `import org.jetbrains.compose.resources.painterResource`, `import teamflowmanager.shared_ui.generated.resources.ic_google`, `import androidx.compose.foundation.Image` (if not present), `import androidx.compose.foundation.layout.width`

### Step 3: Eliminate TeamFlowManagerIcon expect/actual
1. Delete `shared-ui/src/commonMain/kotlin/com/jesuslcorominas/teamflowmanager/ui/TeamFlowManagerIcon.kt`
2. Delete `shared-ui/src/androidMain/kotlin/com/jesuslcorominas/teamflowmanager/ui/TeamFlowManagerIcon.kt`
3. Delete `shared-ui/src/iosMain/kotlin/com/jesuslcorominas/teamflowmanager/ui/TeamFlowManagerIcon.kt`
4. Update all call sites (4 files) to replace `TeamFlowManagerIcon()` with inline composable:
   - `shared-ui/src/commonMain/kotlin/.../ui/club/ClubSelectionScreen.kt` (line 48) — replace `TeamFlowManagerIcon()` with:
     ```kotlin
     Icon(
         modifier = Modifier.size(144.dp),
         painter = painterResource(Res.drawable.ic_launcher),
         contentDescription = null,
         tint = Color.Unspecified,
     )
     ```
   - `shared-ui/src/commonMain/kotlin/.../ui/club/JoinClubScreen.kt` (line 212) — same replacement
   - `shared-ui/src/commonMain/kotlin/.../ui/team/components/TeamForm.kt` (line 112) — same replacement
   - `shared-ui/src/commonMain/kotlin/.../ui/club/CreateClubScreen.kt` (line 181) — same replacement

   Each file needs imports: `import org.jetbrains.compose.resources.painterResource`, `import teamflowmanager.shared_ui.generated.resources.ic_launcher`, `import androidx.compose.ui.graphics.Color` (if not present). Remove the `import com.jesuslcorominas.teamflowmanager.ui.TeamFlowManagerIcon` from each.

### Step 4: No removal of app drawables
All 11 drawables in `app/src/main/res/drawable/` must remain because:
- `ic_launcher.xml` — referenced by Android manifest/launcher
- `ic_launcher_black_and_white.xml` — used by notification services
- `ic_pause.xml`, `ic_people.xml`, `ic_sports_soccer.xml`, `ic_swap_horiz.xml`, `ic_timer.xml` — used by `MatchReportPdfExporterImpl` (Android Canvas API, not Compose)
- `ic_goal.xml`, `ic_google.xml`, `ic_timeout.xml`, `ic_whistle.xml` — may still be referenced by Android-specific code or future features

## Source set rules
- All new CMP drawable resources go in `shared-ui/src/commonMain/composeResources/drawable/`
- All screen code changes are in `commonMain` — no expect/actual needed
- The `TeamFlowManagerIcon` expect/actual is being DELETED, not moved

## Repository / DataSource rules
- No repository or datasource changes needed — this is a pure UI/resource task

## DI wiring
- No DI changes needed

## Test coverage points
- Verify the project compiles for both Android and iOS after changes (`./gradlew build --no-daemon` and `./gradlew :shared-ui:compileKotlinIosSimulatorArm64 --no-daemon`)
- Verify LoginScreen renders Google icon (visual/manual test)
- Verify ClubSelectionScreen, JoinClubScreen, TeamForm, CreateClubScreen render the app logo correctly after removing expect/actual (visual/manual test)
- Verify no broken imports after deleting TeamFlowManagerIcon files

## Risks / Ambiguities
- **CMP XML vector compatibility**: CMP 1.7.3 supports Android XML vector format but large/complex vectors (ic_whistle at 512dp, ic_timeout at 512dp) should be tested on iOS to ensure correct rendering. If they fail, consider converting to SVG or simplifying paths.
- **ic_google.xml color rendering**: The Google icon uses multiple `fillColor` values (#EA4335, #4285F4, #FBBC05, #34A853). When using `painterResource`, ensure `tint = Color.Unspecified` is set (or use `Image` instead of `Icon`) so CMP does not apply a monochrome tint.
- **EventCard refactor deferred**: The `EventCard` composable in `TimelineContent.kt` takes `icon: ImageVector`, not `Painter`. Replacing timeline icons with custom drawables would require changing this signature and all callers. This is intentionally deferred — the Material icons work fine visually for timeline events. If desired later, change the `icon` parameter type to `Painter` and update all call sites.
- **MatchReportPdfExporterImpl remains untouched**: This Android-specific class uses `ContextCompat.getDrawable(context, R.drawable.*)` which requires Android resource IDs. It cannot use CMP resources. The drawables it references must stay in `app/src/main/res/drawable/`.
- **ic_goal.xml is currently unused**: Neither the app nor shared-ui reference it in code. It is being migrated to CMP resources for future use (e.g., replacing `Icons.Default.SportsSoccer` in goal-specific contexts).