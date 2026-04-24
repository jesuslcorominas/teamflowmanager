# Spec: Fix per-team notification switches (issue #346)

## Task summary
Per-team notification switches in the president's team detail screen are inert because `NotificationPreferencesFirestoreDataSourceImpl.updateTeamPreference()` uses `set` with `SetOptions.merge()` and a dot-notation key, which Firestore treats as a literal top-level field name instead of a nested path — so the per-team value is never actually persisted into the `teams` map and the UI always falls back to the global value.

## Files to read (before implementing)
- `data/remote/src/androidMain/kotlin/com/jesuslcorominas/teamflowmanager/data/remote/datasource/NotificationPreferencesFirestoreDataSourceImpl.kt` — contains the buggy `updateTeamPreference()` method
- `data/remote/src/androidMain/kotlin/com/jesuslcorominas/teamflowmanager/data/remote/firestore/NotificationPreferencesFirestoreModel.kt` — Firestore model with `teams: Map<String, TeamPrefsModel>` structure
- `viewmodel/src/commonMain/kotlin/com/jesuslcorominas/teamflowmanager/viewmodel/PresidentTeamDetailViewModel.kt` — ViewModel that reads `teamPreferences[teamId]` with fallback to global
- `viewmodel/src/androidUnitTest/kotlin/com/jesuslcorominas/teamflowmanager/viewmodel/PresidentTeamDetailViewModelTest.kt` — existing tests (no notification tests exist yet)

## Architecture decisions
- **Fix location**: `NotificationPreferencesFirestoreDataSourceImpl.updateTeamPreference()` only — the domain model, repository interface, use case, and ViewModel logic are all correct; the bug is isolated to the Firestore write call.
- **Fix approach**: Replace `document.set(dotted-key-map, SetOptions.merge())` with `document.update(dotted-path, value)` — Firestore's `update()` correctly interprets dot-notation as nested field paths, while `set` with merge treats dotted keys as literal field names.
- **Fallback for missing document**: `update()` fails if the document does not exist. Add a try-catch for `FirebaseFirestoreException` with code `NOT_FOUND`: on catch, use `set` with a properly nested map structure. This covers the first-time write edge case.
- **No ViewModel changes**: The ViewModel's fallback `teamPref?.matchEvents ?: prefs.globalMatchEvents` is correct behavior. Once the Firestore write is fixed the snapshot listener will emit the correct nested data and the UI will reflect it.

## Implementation steps

1. `[data/remote/src/androidMain/kotlin/com/jesuslcorominas/teamflowmanager/data/remote/datasource/NotificationPreferencesFirestoreDataSourceImpl.kt]`
   In `updateTeamPreference()`, replace the current `set(..., SetOptions.merge())` call with:
   ```kotlin
   try {
       document(clubId, userId)
           .update("$FIELD_TEAMS.$teamRemoteId.$fieldName", enabled)
           .await()
   } catch (e: com.google.firebase.firestore.FirebaseFirestoreException) {
       if (e.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.NOT_FOUND) {
           val nested = mapOf(
               FIELD_TEAMS to mapOf(
                   teamRemoteId to mapOf(fieldName to enabled)
               )
           )
           document(clubId, userId)
               .set(nested, com.google.firebase.firestore.SetOptions.merge())
               .await()
       } else {
           throw e
       }
   }
   ```

2. `[viewmodel/src/androidUnitTest/kotlin/com/jesuslcorominas/teamflowmanager/viewmodel/PresidentTeamDetailViewModelTest.kt]`
   Add tests:
   - `when team preference exists, teamNotificationState reflects team-specific values` — mock `getNotificationPreferences` to return prefs with `teamPreferences[teamId]` populated, assert `teamNotificationState` uses team values not global.
   - `when team preference absent, teamNotificationState falls back to global values`.
   - `updateTeamMatchEvents calls use case with correct args`.
   - `updateTeamGoals calls use case with correct args`.

## Source set rules
- Fix is in `data/remote/src/androidMain` — Android-only code (Firestore SDK). No changes in `commonMain` or `iosMain`.
- Tests go in `data/remote/src/androidUnitTest` (if datasource test exists) and `viewmodel/src/androidUnitTest`.

## Repository / DataSource rules
- Extend existing: `NotificationPreferencesFirestoreDataSourceImpl` — fix `updateTeamPreference()` only. No new classes needed.

## DI wiring
- No DI changes required. Existing binding in DataRemoteModule already covers this.

## Test coverage points
- `updateTeamPreference` calls Firestore `update()` (not `set`) with path `"teams.<teamId>.matchEvents"` and correct boolean.
- `updateTeamPreference` falls back to `set` with nested map when document does not exist (`NOT_FOUND`).
- `teamNotificationState.matchEvents` reflects per-team value when `teamPreferences[teamId]` is non-null.
- `teamNotificationState.matchEvents` falls back to `globalMatchEvents` when `teamPreferences[teamId]` is null.
- `updateTeamMatchEvents(false)` invokes `updateTeamNotificationPreference(clubId, teamId, MATCH_EVENTS, false)`.

## Risks / Ambiguities
- **Document-not-found on first toggle**: `update()` throws `NOT_FOUND` if no Firestore document exists yet. The try-catch fallback handles this.
- **Existing corrupted data**: Users who previously toggled per-team switches may have literal top-level fields like `"teams.teamA.matchEvents"` in their Firestore documents. These orphaned fields are harmless (ignored by `toObject`) but could be cleaned up in a migration. Not blocking for this fix.
- **Silent error swallowing in ViewModel**: `updateTeamMatchEvents` and `updateTeamGoals` wrap calls in `runCatching {}` with no error handling. Pre-existing issue, not introduced by this fix.
- **`updateGlobalPreference` is NOT affected**: Its keys (`matchEvents`, `goals`) are top-level fields with no dots, so `set` + merge works correctly there. No changes needed.