# Push Notifications — FCM Infrastructure

This document describes the Firebase Cloud Messaging (FCM) push notification infrastructure implemented for TeamFlowManager.

## Architecture

```
Firebase Cloud Functions (trigger)
    ↓ sends FCM message to topic
FCM topic: club_{clubFirestoreId}
    ↓ delivered to subscribed devices
TeamFlowFirebaseMessagingService (Android)
    ↓ shows local notification
```

## Permission and Token Lifecycle

### Login flow

```
1. User logs in successfully
2. Check IsNotificationPermissionGrantedUseCase
   ├── Granted → SyncFcmTokenUseCase(userId, platform, clubFirestoreId)
   └── Not granted → emit UiEvent.RequestNotificationPermission
                         └── UI shows system dialog
                               ├── Granted → SyncFcmTokenUseCase(...)
                               └── Denied  → no token saved, no subscription
```

### SyncFcmTokenUseCase internals

```
1. Get FCM token from device
2. If token is empty → return (Firebase not ready yet)
3. Query Firestore for docs with same token but different userId
   └── For each found doc:
         a. Unsubscribe from stored topic (if any)
         b. Delete the Firestore doc
4. Save new doc: {userId, token, platform, topic}
5. If clubFirestoreId != null → subscribeToTopic("club_{clubFirestoreId}")
```

Steps 3–4 (cleanup) always run, regardless of whether the user was granted permission — the
caller (LoginViewModel) decides whether to invoke the use case based on the permission check.

### Logout flow

```
DeleteFcmTokenUseCase:
1. Get FCM token
2. If empty → return
3. Read stored topic from Firestore doc
4. If topic exists → unsubscribeFromRawTopic(topic)
5. Delete Firestore doc
```

### Shared device scenario

```
User A (permission granted):
  Login  → doc {userA, token, platform, topic:"club_A"} + subscribed to club_A
  Logout → unsubscribe club_A + delete doc

User B on same device (permission denied):
  Login  → no doc created, no subscription

User A re-logs in → no leftover B doc to clean → saves A doc + subscribes to club_A ✓

---

No-logout scenario:
User A logged in → doc {userA, token, topic:"club_A"}
User B logs in (with permission) →
  SyncFcmToken finds A's doc → unsubscribes club_A → deletes A doc → saves B doc ✓
User A re-logs in →
  No B doc found (B did not create one if denied) → saves A doc + subscribes ✓
```

## Layers

| Layer | Class | Responsibility |
|---|---|---|
| Domain | `SyncFcmTokenUseCase` | Save device FCM token + subscribe to club topic |
| Domain | `DeleteFcmTokenUseCase` | Unsubscribe + remove FCM token on sign-out |
| Domain | `IsNotificationPermissionGrantedUseCase` | Check if POST_NOTIFICATIONS is granted |
| Domain | `SubscribeToClubNotificationsUseCase` | Subscribe to club topic (standalone) |
| Domain | `UnsubscribeFromClubNotificationsUseCase` | Unsubscribe from club topic (standalone) |
| UseCase (Android) | `SyncFcmTokenUseCaseImpl` | Cleanup + save + subscribe |
| UseCase (Android) | `DeleteFcmTokenUseCaseImpl` | Read topic → unsubscribe → delete |
| UseCase (Android) | `IsNotificationPermissionGrantedUseCaseImpl` | Delegates to repository |
| Data (Android) | `FcmTokenProviderDataSourceImpl` | Gets token via `FirebaseMessaging.getInstance().token.await()` |
| Data (Android) | `FcmTokenFirestoreDataSourceImpl` | CRUD on `fcmTokens` collection |
| Data (Android) | `FcmNotificationTopicDataSourceImpl` | Calls `FirebaseMessaging.subscribeToTopic` / `unsubscribeFromTopic` |
| Data (Android) | `NotificationPermissionDataSourceImpl` | `ContextCompat.checkSelfPermission(POST_NOTIFICATIONS)`. API < 33 always true. |
| Data (iOS) | `IosFcmTokenDataSourceImpl` | No-op stub |
| Data (iOS) | `IosFcmTokenProviderDataSourceImpl` | No-op stub — returns empty string |
| Data (iOS) | `IosNotificationTopicDataSourceImpl` | No-op stub |
| Data (iOS) | `IosNotificationPermissionDataSourceImpl` | Always returns `false` (APNs not configured) |
| App (Android) | `TeamFlowFirebaseMessagingService` | Extends `FirebaseMessagingService`, shows notifications via `NotificationChannel` |

## Firestore Schema

### Collection: `fcmTokens`

Document ID: `{userId}_{last16charsOfToken}`

```json
{
  "userId": "uid123",
  "token": "fcm_token_string",
  "platform": "android",
  "topic": "club_abc123",
  "updatedAt": 1711234567890
}
```

**`topic`** is nullable. Documents created before this field was added will have `null`.

### Firestore Index Required

Collection: `fcmTokens`
Fields: `token` (Ascending)

This index is needed for the `findTokensForOtherUsers` query.
Create it in Firebase Console → Firestore → Indexes → Single field, or via `firestore.indexes.json`.

### Firestore Security Rules

```
match /fcmTokens/{docId} {
  // Only the owning user can write their own token
  allow write: if request.auth != null
               && request.resource.data.userId == request.auth.uid;
  // Cloud Functions read with admin SDK (no rule needed)
  allow read: if false;
}
```

> Note: the `findTokensForOtherUsers` query reads docs from other users. This requires an
> admin SDK (Cloud Function) or relaxed read rules. Consider running cleanup via Cloud Function
> trigger instead of client-side if security rules are a concern.

### FCM Topics

Topic name pattern: `club_{clubFirestoreId}`

Example: `club_abc123` for the club with Firestore ID `abc123`.

## Android Notification Channel

Channel ID: `push_notifications_channel`
Channel name: "Notificaciones"
Importance: `IMPORTANCE_DEFAULT`

The channel is created lazily in `TeamFlowFirebaseMessagingService.onMessageReceived`.

## When to call SyncFcmTokenUseCase

Currently called in `LoginViewModel.handlePostLogin` with `clubFirestoreId = null` (token saved
without topic subscription at login time).

**TODO**: Re-call `SyncFcmTokenUseCase` with the actual `clubFirestoreId` when:
- User joins a club (`JoinClubByCodeUseCase` success)
- User is assigned to a club
- User's club assignment is loaded on app startup (SplashViewModel)

This ensures the club topic subscription is always up to date after a club change.

## Activation Steps (DEV project)

The following was configured on the DEV Firebase project (`teamflow-manager-dev`):

1. **FCM enabled** — Firebase Cloud Messaging is enabled by default on any Firebase project.
2. **Firestore rules** — add the `fcmTokens` rules above to `firestore.rules`
3. **Firestore index** — create index on `fcmTokens.token` (Ascending)
4. **Android manifest** — `TeamFlowFirebaseMessagingService` registered with `MESSAGING_EVENT` intent-filter
5. **`google-services.json`** — already present for DEV flavor

## iOS — Current Status

iOS APNs integration is **not yet implemented**. All iOS datasources are no-op stubs and
`IosNotificationPermissionDataSourceImpl` returns `false`.

Requirements to activate on iOS:
- Apple Developer account with APNs key or certificate
- Upload APNs key to Firebase Console → Project Settings → Cloud Messaging → Apple app configuration
- Add `firebase-messaging` pod in `iosApp/Podfile`
- Request notification authorization (`UNUserNotificationCenter.requestAuthorization`)
- Register for remote notifications and forward token to FCM
- Replace iOS stub implementations with real ones

## PROD Project

When ready to activate in PROD:
1. Deploy Firestore rules and indexes to prod project
2. Deploy Cloud Functions (see below) to `teamflow-manager-prod`
3. Ensure `google-services.json` (prod flavor) points to the prod project

## Cloud Functions (Pending)

Server-side notification sending is not yet implemented. Future Cloud Functions will trigger on
Firestore writes and send FCM messages to club topics:

- **`notifyClubOnMatchCreated`** — write to `matches/` → notify `club_{clubFirestoreId}`
- **`notifyClubOnMatchResult`** — match status → FINISHED → notify club
- **`notifyPlayerOnSubstitution`** — write to `playerSubstitutions/` → notify specific player

Deployment:
```bash
cd functions
firebase deploy --only functions --project teamflow-manager-dev
```

> Requires Firebase CLI authenticated and Blaze (pay-as-you-go) plan.

## Known Risks

- **`findTokensForOtherUsers` reads other users' docs**: client-side Firestore rules may block
  this query. Consider running cleanup server-side via Cloud Functions if this becomes a problem.
- **Race condition on shared device**: if A and B log in simultaneously, both may read each
  other's docs before deletion. Impact: transient duplicate subscription. Low probability.
- **Token empty on first login**: Firebase may take time to generate a token. If `getToken()`
  returns empty, the use case returns early. Subscription is deferred to next login or explicit
  re-sync call.
- **`topic` field absent on legacy docs**: documents created before this field was added will
  have `null` topic. `DeleteFcmTokenUseCaseImpl` handles `null` gracefully (skips unsubscribe).
