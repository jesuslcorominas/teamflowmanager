# Spec: iOS — implement president-role Firestore datasources

## Task summary
Replace stub/NotImplementedError datasource implementations in `data/remote/src/iosMain/` with real Firestore implementations using the GitLive Firebase SDK, mirroring the existing Android implementations.

## Files to read (before implementing)
- `data/remote/src/iosMain/kotlin/.../datasource/IosDataSourceStubs.kt` — current stubs to replace
- `data/remote/src/iosMain/kotlin/.../di/DataRemoteModule.kt` — iOS DI wiring to update
- `data/remote/src/androidMain/kotlin/.../datasource/PresidentNotificationFirestoreDataSourceImpl.kt` — Android reference
- `data/remote/src/androidMain/kotlin/.../datasource/NotificationPreferencesFirestoreDataSourceImpl.kt` — Android reference
- `data/remote/src/androidMain/kotlin/.../datasource/PendingCoachAssignmentFirestoreDataSourceImpl.kt` — Android reference
- `data/remote/src/androidMain/kotlin/.../datasource/ClubFirestoreDataSourceImpl.kt` — Android reference (all 5 methods)
- `data/remote/src/iosMain/kotlin/.../datasource/ClubMemberFirestoreDataSourceImpl.kt` — iOS GitLive pattern reference
- `data/remote/src/iosMain/kotlin/.../datasource/MatchFirestoreDataSourceImpl.kt` — iOS GitLive pattern reference (snapshots, `doc.data<T>()`)
- `data/remote/src/androidMain/kotlin/.../firestore/PresidentNotificationFirestoreModel.kt` — Firestore model (Android, uses `@DocumentId`)
- `data/remote/src/androidMain/kotlin/.../firestore/NotificationPreferencesFirestoreModel.kt` — Firestore model (Android)
- `data/remote/src/androidMain/kotlin/.../firestore/ClubFirestoreModel.kt` — Firestore model (Android, uses `@DocumentId`)

## Architecture decisions

- **Decision 1**: Create new iOS Firestore model files in `data/remote/src/iosMain/.../firestore/` using `@Serializable` (GitLive pattern) instead of `@DocumentId` (Android Firebase SDK). This mirrors the pattern already used for `ClubMemberFirestoreModel`, `MatchFirestoreModel`, etc. on iOS.
- **Decision 2**: Each new datasource gets its own file in `data/remote/src/iosMain/.../datasource/` — do NOT add to `IosDataSourceStubs.kt`. The stubs file should only shrink.
- **Decision 3**: Use `firestore.collection(...).snapshots` + `Flow.map {}` for reactive reads (GitLive pattern), not `callbackFlow` + `addSnapshotListener` (Android SDK pattern). See `ClubMemberFirestoreDataSourceImpl` (iosMain) for the canonical pattern.
- **Decision 4**: For one-shot reads/writes, use suspend functions directly on GitLive's `DocumentReference` and `CollectionReference` (e.g., `doc.set(model)`, `doc.get()`, `doc.delete()`, `collection.where { ... }.get()`). No `.await()` needed — GitLive suspend functions are already coroutine-native.
- **Decision 5**: `PendingCoachAssignmentDataSource` is currently not wired at all in the iOS DI module. It must be added.
- **Decision 6**: Remove stubs from `IosDataSourceStubs.kt` as they are replaced. Keep `NoOpImageStorageDataSource` and `NoOpDynamicLinkDataSource` (out of scope).

## Implementation steps

### Step 1: Create `PresidentNotificationFirestoreModel` (iosMain)
`data/remote/src/iosMain/kotlin/com/jesuslcorominas/teamflowmanager/data/remote/firestore/PresidentNotificationFirestoreModel.kt`

```kotlin
@Serializable
data class PresidentNotificationFirestoreModel(
    val id: String = "",
    val type: String = "",
    val title: String = "",
    val body: String = "",
    val userData: Map<String, String> = emptyMap(),
    val createdAt: Long = 0L,
    val read: Boolean = false,
)

fun PresidentNotificationFirestoreModel.toDomain(): PresidentNotification
```

Same field mapping as Android version. Use `NotificationType.entries.firstOrNull { it.key == type }` for type conversion.

### Step 2: Create `NotificationPreferencesFirestoreModel` (iosMain)
`data/remote/src/iosMain/kotlin/com/jesuslcorominas/teamflowmanager/data/remote/firestore/NotificationPreferencesFirestoreModel.kt`

```kotlin
@Serializable
data class NotificationPreferencesFirestoreModel(
    val matchEvents: Boolean = true,
    val goals: Boolean = true,
    val teams: Map<String, TeamPrefsModel> = emptyMap(),
) {
    @Serializable
    data class TeamPrefsModel(
        val matchEvents: Boolean = true,
        val goals: Boolean = true,
    )
}

fun NotificationPreferencesFirestoreModel.toDomain(userId: String): UserNotificationPreferences
```

### Step 3: Create `ClubFirestoreModel` (iosMain)
`data/remote/src/iosMain/kotlin/com/jesuslcorominas/teamflowmanager/data/remote/firestore/ClubFirestoreModel.kt`

```kotlin
@Serializable
data class ClubFirestoreModel(
    val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    val invitationCode: String = "",
    val homeGround: String? = null,
)

fun ClubFirestoreModel.toDomain(): Club
fun Club.toFirestoreModel(): ClubFirestoreModel
```

Use `toStableId()` from `data/remote/src/commonMain/.../util/IdUtils.kt` for id conversion, same as ClubMember iOS model.

### Step 4: Create `PresidentNotificationFirestoreDataSourceImpl` (iosMain)
`data/remote/src/iosMain/kotlin/com/jesuslcorominas/teamflowmanager/data/remote/datasource/PresidentNotificationFirestoreDataSourceImpl.kt`

Constructor: `(firestore: FirebaseFirestore)`

Implements `PresidentNotificationDataSource` (6 methods):

- `getNotifications(clubId)`: Use `firestore.collection("presidentNotifications").document(clubId).collection("notifications").orderBy("createdAt", Direction.DESCENDING).snapshots` then `.map { qs -> qs.documents.mapNotNull { doc.data<Model>().copy(id = doc.id).toDomain() } }`. Wrap in `flow { emitAll(...) }` with `.catch` for `FirebaseFirestoreException`.
- `getUnreadCount(clubId)`: Use `.where { "read" equalTo false }.snapshots` then `.map { qs -> qs.documents.size }`.
- `createNotification(clubId, notification)`: Build model, generate UUID for id if empty (`kotlin.uuid.Uuid.random().toString()` or `dev.gitlive.firebase.firestore` auto-id). Use `notificationsCollection(clubId).document(docId).set(model)`.
- `markAsRead(clubId, notificationId)`: `notificationsCollection(clubId).document(notificationId).update("read" to true)`.
- `markAsUnread(clubId, notificationId)`: `notificationsCollection(clubId).document(notificationId).update("read" to false)`.
- `deleteNotification(clubId, notificationId)`: `notificationsCollection(clubId).document(notificationId).delete()`.

Error handling: catch `CancellationException` and rethrow, catch other exceptions and rethrow (matching Android pattern but without `android.util.Log` — use `println` or omit logging).

### Step 5: Create `NotificationPreferencesFirestoreDataSourceImpl` (iosMain)
`data/remote/src/iosMain/kotlin/com/jesuslcorominas/teamflowmanager/data/remote/datasource/NotificationPreferencesFirestoreDataSourceImpl.kt`

Constructor: `(firestore: FirebaseFirestore)`

Implements `NotificationPreferencesDataSource` (3 methods):

- `getPreferences(userId, clubId)`: Snapshot listener on `firestore.collection("clubs").document(clubId).collection("notificationPreferences").document(userId)`. Use `.snapshots.map { doc -> if (doc.exists) doc.data<NotificationPreferencesFirestoreModel>().toDomain(userId) else UserNotificationPreferences(userId = userId) }`. Wrap in `flow { emitAll(...) }`.
- `updateGlobalPreference(userId, clubId, type, enabled)`: Map `NotificationEventType` to field name (`MATCH_EVENTS -> "matchEvents"`, `GOALS -> "goals"`). Use `document(clubId, userId).set(mapOf(fieldName to enabled), merge = true)` — GitLive supports merge via `set(data, merge = true)` or `set(data, buildSettings { merge = true })`. Check GitLive API — likely `set(strategy = MergeStrategy.Merge, data = ...)` or use `update(fieldName to enabled)`.
- `updateTeamPreference(userId, clubId, teamRemoteId, type, enabled)`: Use `update("teams.$teamRemoteId.$fieldName" to enabled)`. On NOT_FOUND error, fallback to `set(nested map, merge = true)` like the Android version.

**GitLive merge set pattern**: Use `set(data, merge = true)` — GitLive's `DocumentReference.set()` accepts a merge boolean. Verify by looking at other iOS datasource code or GitLive docs.

### Step 6: Create `PendingCoachAssignmentFirestoreDataSourceImpl` (iosMain)
`data/remote/src/iosMain/kotlin/com/jesuslcorominas/teamflowmanager/data/remote/datasource/PendingCoachAssignmentFirestoreDataSourceImpl.kt`

Constructor: `(firestore: FirebaseFirestore)`

Implements `PendingCoachAssignmentDataSource` (3 methods):

- `create(teamId, clubId, email)`: `firestore.collection("pendingCoachAssignments").document(teamId).set(mapOf("teamId" to teamId, "clubId" to clubId, "email" to email))`.
- `delete(teamId)`: `firestore.collection("pendingCoachAssignments").document(teamId).delete()`.
- `getByEmail(email)`: `firestore.collection("pendingCoachAssignments").where { "email" equalTo email }.get()`. Map documents to `PendingCoachAssignment(teamId, clubId, email)` by reading fields via `doc.get<String>("teamId")` or `doc.data<Map<String, String>>()`. Prefer a simple `@Serializable` inline model or direct field access.

### Step 7: Create full `ClubFirestoreDataSourceImpl` (iosMain)
`data/remote/src/iosMain/kotlin/com/jesuslcorominas/teamflowmanager/data/remote/datasource/ClubFirestoreDataSourceImpl.kt`

This replaces the stub version currently in `IosDataSourceStubs.kt`.

Constructor: `(firestore: FirebaseFirestore)`

Implements `ClubDataSource` (5 methods):

- `createClubWithOwner(clubName, currentUserId, currentUserName, currentUserEmail)`:
  1. `require` all params are not blank
  2. Generate invitation code via `InvitationCodeGenerator.generate()` (commonMain utility)
  3. Create club doc: `firestore.collection("clubs").document` (auto-id) → `docRef.set(clubModel)`
  4. Create clubMember doc with id `"${currentUserId}_${clubId}"` in `"clubMembers"` collection with roles `["Presidente"]`
  5. Return `clubModel.toDomain()`
  6. Use `ClubFirestoreModel` and `ClubMemberFirestoreModel` (iosMain versions)

- `getClubByInvitationCode(invitationCode)`: `firestore.collection("clubs").where { "invitationCode" equalTo invitationCode }.limit(1).get()`. Parse first doc as `ClubFirestoreModel`.

- `getClubById(id)`: `firestore.collection("clubs").document(id).get()`. Return `doc.data<ClubFirestoreModel>().copy(id = doc.id).toDomain()` if exists.

- `regenerateInvitationCode(id)`: Generate new code with `InvitationCodeGenerator.generate()`, then `firestore.collection("clubs").document(id).update("invitationCode" to newCode)`. Return new code.

- `updateClub(id, name, homeGround)`: `firestore.collection("clubs").document(id).update("name" to name, "homeGround" to homeGround)`. Re-fetch and return.

### Step 8: Update `IosDataSourceStubs.kt`
`data/remote/src/iosMain/kotlin/com/jesuslcorominas/teamflowmanager/data/remote/datasource/IosDataSourceStubs.kt`

Remove the following classes (they are now in their own files):
- `ClubFirestoreDataSourceImpl`
- `PresidentNotificationDataSourceStub`
- `NotificationPreferencesStubDataSourceImpl`

Keep:
- `NoOpImageStorageDataSource`
- `NoOpDynamicLinkDataSource`

Remove unused imports accordingly.

### Step 9: Update `DataRemoteModule.kt` (iosMain)
`data/remote/src/iosMain/kotlin/com/jesuslcorominas/teamflowmanager/data/remote/di/DataRemoteModule.kt`

Replace:
```kotlin
// Phase 2 stubs
single<ClubDataSource> { ClubFirestoreDataSourceImpl() }
```
With:
```kotlin
singleOf(::ClubFirestoreDataSourceImpl) bind ClubDataSource::class
```

Replace:
```kotlin
single<PresidentNotificationDataSource> { PresidentNotificationDataSourceStub() }
```
With:
```kotlin
singleOf(::PresidentNotificationFirestoreDataSourceImpl) bind PresidentNotificationDataSource::class
```

Replace:
```kotlin
singleOf(::NotificationPreferencesStubDataSourceImpl) bind NotificationPreferencesDataSource::class
```
With:
```kotlin
singleOf(::NotificationPreferencesFirestoreDataSourceImpl) bind NotificationPreferencesDataSource::class
```

Add new binding:
```kotlin
singleOf(::PendingCoachAssignmentFirestoreDataSourceImpl) bind PendingCoachAssignmentDataSource::class
```

Add required imports:
```kotlin
import com.jesuslcorominas.teamflowmanager.data.core.datasource.PendingCoachAssignmentDataSource
import com.jesuslcorominas.teamflowmanager.data.remote.datasource.PresidentNotificationFirestoreDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.remote.datasource.NotificationPreferencesFirestoreDataSourceImpl
import com.jesuslcorominas.teamflowmanager.data.remote.datasource.PendingCoachAssignmentFirestoreDataSourceImpl
```

Remove old stub imports:
```kotlin
// Remove:
import com.jesuslcorominas.teamflowmanager.data.remote.datasource.PresidentNotificationDataSourceStub
import com.jesuslcorominas.teamflowmanager.data.remote.datasource.NotificationPreferencesStubDataSourceImpl
```

## Source set rules
- All new datasource implementations go in `data/remote/src/iosMain/` — they use `dev.gitlive.firebase` APIs (iOS-specific).
- All new Firestore model files go in `data/remote/src/iosMain/.../firestore/` — they use `@Serializable` (not `@DocumentId`).
- Domain models (`PresidentNotification`, `PendingCoachAssignment`, `Club`, etc.) are already in `domain/src/commonMain/`.
- `InvitationCodeGenerator` is already in `data/remote/src/commonMain/` — reusable from iosMain.
- `toStableId()` is already in `data/remote/src/commonMain/.../util/IdUtils.kt` — reusable from iosMain.
- Expected/actual needed? **No** — all platform differences are handled by separate implementations in androidMain/iosMain, bound via Koin DI.

## Repository / DataSource rules
- Extend existing: `ClubDataSource` — the iOS stub already implements the interface; replace with full implementation
- Extend existing: `PresidentNotificationDataSource` — replace stub with real implementation
- Extend existing: `NotificationPreferencesDataSource` — replace stub with real implementation
- Extend existing: `PendingCoachAssignmentDataSource` — no iOS implementation exists yet; create new Impl class
- No new interfaces needed. All interfaces already exist in `data/core/src/commonMain/`.

## DI wiring
Update `data/remote/src/iosMain/.../di/DataRemoteModule.kt`:

```kotlin
// Replace stubs with real implementations:
singleOf(::ClubFirestoreDataSourceImpl) bind ClubDataSource::class
singleOf(::PresidentNotificationFirestoreDataSourceImpl) bind PresidentNotificationDataSource::class
singleOf(::NotificationPreferencesFirestoreDataSourceImpl) bind NotificationPreferencesDataSource::class

// New binding (was missing entirely):
singleOf(::PendingCoachAssignmentFirestoreDataSourceImpl) bind PendingCoachAssignmentDataSource::class
```

All use `singleOf(::Impl) bind Interface::class` pattern (constructor injection of `FirebaseFirestore` from Koin graph).

## Test coverage points
- `PresidentNotificationFirestoreDataSourceImpl`: verify `getNotifications` emits list from snapshots, `getUnreadCount` filters by `read == false`, `markAsRead`/`markAsUnread` call update with correct field, `deleteNotification` calls delete, `createNotification` sets document with correct model
- `NotificationPreferencesFirestoreDataSourceImpl`: verify `getPreferences` returns default when doc doesn't exist, `updateGlobalPreference` maps `NotificationEventType` to correct field, `updateTeamPreference` builds nested path correctly and handles NOT_FOUND fallback
- `PendingCoachAssignmentFirestoreDataSourceImpl`: verify `create` sets correct doc at `teamId`, `delete` removes doc, `getByEmail` queries by email field
- `ClubFirestoreDataSourceImpl`: verify `createClubWithOwner` creates both club and clubMember docs sequentially, `getClubByInvitationCode` queries correctly, `getClubById` fetches single doc, `regenerateInvitationCode` generates and updates, `updateClub` updates fields and re-fetches

## Risks / Ambiguities

1. **GitLive `set` with merge**: The Android version uses `SetOptions.merge()` for notification preferences. GitLive's API may differ — check if `DocumentReference.set(data, merge = true)` or `DocumentReference.set(data, buildSettings { merge = true })` is the correct syntax. Look at existing iOS datasource code for precedent.

2. **GitLive `orderBy` + `Direction`**: The Android version uses `Query.Direction.DESCENDING`. In GitLive, the equivalent is likely `orderBy("field", dev.gitlive.firebase.firestore.Direction.DESCENDING)`. Verify import.

3. **GitLive `where` + `equalTo`**: Existing iOS code uses `where { "field" equalTo value }` DSL. This is confirmed working for the project.

4. **GitLive document snapshot for single document**: For `NotificationPreferencesFirestoreDataSourceImpl.getPreferences`, listening to a single document's snapshots differs from collection snapshots. Use `firestore.collection(...).document(...).snapshots` (returns `Flow<DocumentSnapshot>`), not `.snapshots` on a collection query. Verify that single-document snapshot flow works with `doc.exists` / `doc.data<T>()`.

5. **UUID generation**: Android uses `java.util.UUID.randomUUID()`. On KMP, use `kotlin.uuid.Uuid.random().toString()` (Kotlin 2.1.0 supports this in `kotlin.uuid`).

6. **Missing `createNotification` in current stub**: The current `PresidentNotificationDataSourceStub` does NOT implement `createNotification` (which is in the interface). This means the iOS build may already fail if this code path is exercised. The new implementation resolves this.

7. **ClubFirestoreDataSourceImpl stub is incomplete**: The current stub only implements 2 of 5 interface methods (`createClubWithOwner`, `getClubByInvitationCode`). The methods `getClubById`, `regenerateInvitationCode`, and `updateClub` are missing from the stub, which would cause a compile error. This needs full implementation of all 5 methods.

8. **Logging**: Android implementations use `android.util.Log`. iOS implementations should omit logging or use `println()` / a KMP logging library if one is already in the project. Check if `co.touchlab.kermit` or similar is available.
